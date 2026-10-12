package me.moonscenty.alchemia.entity;

import java.util.EnumSet;

import me.moonscenty.alchemia.block.taint.TaintSpread;
import me.moonscenty.alchemia.player.effect.ModEffects;
import me.moonscenty.alchemia.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * A creeper the taint has made over. It swells and goes off as a creeper does, but the blast breaks nothing: what it
 * throws is taint -- poison into everything living within six blocks, and fibres onto the ground about it.
 */
public class TaintedCreeper extends Monster implements TaintedMob {
    private static final EntityDataAccessor<Integer> SWELLING = SynchedEntityData.defineId(TaintedCreeper.class,
            EntityDataSerializers.INT);
    /** How long its fuse is, how hard it goes off, and how far its poison reaches. The original's. */
    private static final int FUSE = 30;
    private static final float BLAST = 1.5F;
    private static final double POISONS = 6.0;
    private static final int POISONED_FOR = 100;

    private int oldSwell;
    private int swell;

    public TaintedCreeper(EntityType<? extends TaintedCreeper> type, Level level) {
        super(type, level);
    }

    public TaintedCreeper(Level level) {
        this(ModEntities.TAINTED_CREEPER.get(), level);
    }

    /** The original's numbers. */
    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new Swell(this));
        goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Ocelot.class, 6.0F, 1.0, 1.2));
        goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Cat.class, 6.0F, 1.0, 1.2));
        goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, false));
        goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SWELLING, -1);
    }

    public int swelling() {
        return entityData.get(SWELLING);
    }

    public void setSwelling(int way) {
        entityData.set(SWELLING, way);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !TaintedMob.allied(target) && super.canAttack(target);
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        return TaintedMob.allied(other) || super.isAlliedTo(other);
    }

    /** It does nothing by touching: all it has is going off. */
    @Override
    public boolean doHurtTarget(Entity target) {
        return true;
    }

    /** A fall sets its fuse burning down as a creeper's does, though never quite to the end. */
    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        boolean hurt = super.causeFallDamage(distance, multiplier, source);
        swell = Math.min(FUSE - 5, (int) (swell + distance * 1.5F));
        return hurt;
    }

    @Override
    public void tick() {
        if (isAlive()) {
            oldSwell = swell;
            int way = swelling();
            if (way > 0 && swell == 0) {
                playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 0.5F);
            }
            swell = Math.max(0, swell + way);
            if (swell >= FUSE) {
                swell = FUSE;
                if (level() instanceof ServerLevel served) {
                    goOff(served);
                } else {
                    me.moonscenty.alchemia.client.TaintEffects.taintsplosion(this);
                }
            }
        }
        super.tick();
    }

    /** A blast that breaks nothing, poison for everything living near, and fibres flung about. */
    private void goOff(ServerLevel level) {
        level.explode(this, getX(), getY() + getBbHeight() / 2.0F, getZ(), BLAST, Level.ExplosionInteraction.NONE);
        for (LivingEntity near : level.getEntitiesOfClass(LivingEntity.class, new AABB(position(), position())
                .inflate(POISONS))) {
            if (!(near instanceof TaintedMob) && !near.isInvertedHealAndHarm()) {
                near.addEffect(new MobEffectInstance(ModEffects.FLUX_FLU, POISONED_FOR, 0, false, true), this);
            }
        }
        BlockPos at = blockPosition();
        for (int tries = 0; tries < 10; tries++) {
            if (!random.nextBoolean()) {
                continue;
            }
            BlockPos spot = new BlockPos(at.getX() + (int) ((random.nextFloat() - random.nextFloat()) * 5.0F),
                    at.getY(),
                    at.getZ() + (int) ((random.nextFloat() - random.nextFloat()) * 5.0F));
            if (level.getBlockState(spot.below()).isRedstoneConductor(level, spot.below())
                    && level.getBlockState(spot).canBeReplaced()) {
                level.setBlockAndUpdate(spot, TaintSpread.fibres(level, spot));
            }
        }
        discard();
    }

    /** How near it is to going off, for the renderer, as a creeper's is. */
    public float swelling(float partial) {
        return (oldSwell + (swell - oldSwell) * partial) / (FUSE - 2);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.CREEPER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CREEPER_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return 0.7F;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        spawnAtLocation(TaintedMob.gooOrTendril(random, 0.5F), getBbHeight() / 2.0F);
    }

    /** Swells while what it goes for is within three blocks and in sight, and settles back if it gets away. */
    private static final class Swell extends Goal {
        private final TaintedCreeper creeper;
        private LivingEntity target;

        Swell(TaintedCreeper creeper) {
            this.creeper = creeper;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity aim = creeper.getTarget();
            return creeper.swelling() > 0 || aim != null && creeper.distanceToSqr(aim) < 9.0;
        }

        @Override
        public void start() {
            creeper.getNavigation().stop();
            target = creeper.getTarget();
        }

        @Override
        public void stop() {
            target = null;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            boolean near = target != null && creeper.distanceToSqr(target) <= 49.0
                    && creeper.getSensing().hasLineOfSight(target);
            creeper.setSwelling(near ? 1 : -1);
        }
    }
}
