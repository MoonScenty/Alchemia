package me.moonscenty.alchemia.entity;

import me.moonscenty.alchemia.block.taint.Tainted;
import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * A taintacle: a tentacle of taint three blocks tall, rooted where it rose out of tainted ground.
 *
 * <p>It never moves, only lashes at whatever comes within two or three blocks of it. Off the taint it withers, a point
 * a second. Something it has taken against standing on taint four to sixteen blocks off, it sends a little tendril up
 * out of the ground under them, every two seconds.
 */
public class Taintacle extends Monster implements TaintedMob {
    /** Event: it lashes out, and flails hard for a while. */
    public static final byte FLAILS = 16;

    /** The original's numbers. */
    private static final double LIVES = 50.0;
    private static final double LASHES = 7.0;
    private static final float REACHES = 2.0F;

    /** How hard it is flailing, client-side; it settles back to one by a hundredth a tick. */
    public float flail = 1.0F;

    public Taintacle(EntityType<? extends Taintacle> type, Level level) {
        super(type, level);
        xpReward = 10;
    }

    public Taintacle(Level level) {
        this(ModEntities.TAINTACLE.get(), level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, LIVES)
                .add(Attributes.ATTACK_DAMAGE, LASHES)
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, false));
        goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 6.0F));
        goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        targetSelector.addGoal(0, new HurtByTargetGoal(this));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** Only on tainted ground, and not in peace. */
    public static boolean checkSpawnRules(EntityType<? extends Taintacle> type, ServerLevelAccessor level,
            MobSpawnType reason, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && onTaint(level, pos);
    }

    /** Whether this spot, or the one under it, is tainted. */
    public static boolean onTaint(LevelAccessor level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof Tainted
                || level.getBlockState(pos.below()).getBlock() instanceof Tainted;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !TaintedMob.allied(target) && super.canAttack(target);
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        return TaintedMob.allied(other) || super.isAlliedTo(other);
    }

    /** Rooted: it goes nowhere but down, if the ground goes from under it. */
    @Override
    public void move(MoverType type, Vec3 movement) {
        super.move(type, new Vec3(0.0, Math.min(0.0, movement.y), 0.0));
    }

    /** How far it can reach to lash, beyond its own girth: two blocks and a bit. */
    @Override
    protected AABB getAttackBoundingBox() {
        double reach = reach();
        return super.getAttackBoundingBox().inflate(reach, 0.0, reach);
    }

    protected double reach() {
        return REACHES + random.nextFloat();
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel served && tickCount % 20 == 0) {
            if (!onTaint(served, blockPosition())) {
                hurt(damageSources().starve(), 1.0F);
            }
            LivingEntity target = getTarget();
            if (sendsTendrils() && tickCount % 40 == 0 && target != null && distanceToSqr(target) > 16.0
                    && distanceToSqr(target) < 256.0 && getSensing().hasLineOfSight(target)) {
                tendrilUnder(served, target);
            }
        }
        if (level().isClientSide) {
            if (flail > 1.0F) {
                flail -= 0.01F;
            }
            if (tickCount < getBbHeight() * 10.0F && onGround()) {
                me.moonscenty.alchemia.client.TaintEffects.arise(this);
            }
        }
    }

    protected boolean sendsTendrils() {
        return true;
    }

    /** A tendril up out of the ground under something that stands on taint. */
    private void tendrilUnder(ServerLevel level, LivingEntity target) {
        boolean outer = level.getBiome(target.blockPosition()).is(me.moonscenty.alchemia.worldgen.ModBiomes.ELDRITCH);
        if (!outer && !onTaint(level, target.blockPosition())) {
            return;
        }
        if (outer && level.isEmptyBlock(target.blockPosition())) {
            level.setBlock(target.blockPosition(), me.moonscenty.alchemia.block.taint.TaintFibreBlock.fitted(level,
                    target.blockPosition(), me.moonscenty.alchemia.registry.ModBlocks.TAINT_FIBRE.get().defaultBlockState()), 3);
        }
        TaintacleTiny tendril = new TaintacleTiny(level);
        tendril.moveTo(target.getX() + random.nextFloat() - random.nextFloat(), target.getY(),
                target.getZ() + random.nextFloat() - random.nextFloat(), 0.0F, 0.0F);
        level.addFreshEntity(tendril);
        playSound(ModSounds.TENTACLE.get(), getSoundVolume(), getVoicePitch());
    }

    /** A lash: it flails, and the sound of it. */
    @Override
    public boolean doHurtTarget(Entity target) {
        level().broadcastEntityEvent(this, FLAILS);
        playSound(ModSounds.TENTACLE.get(), getSoundVolume(), getVoicePitch());
        return super.doHurtTarget(target);
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == FLAILS) {
            flail = 3.0F;
        } else {
            super.handleEntityEvent(event);
        }
    }

    /** A scrap of goo or a tendril of it. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        spawnAtLocation(new ItemStack(random.nextBoolean() ? ModItems.TAINTED_GOO.get()
                : ModItems.TAINT_TENDRIL.get()), getBbHeight() / 2.0F);
    }

    @Override
    public int getAmbientSoundInterval() {
        return 200;
    }

    @Override
    protected float getSoundVolume() {
        return getBbHeight() / 8.0F;
    }

    @Override
    public float getVoicePitch() {
        return 1.3F - getBbHeight() / 10.0F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ROOTS.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.TENTACLE.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.TENTACLE.get();
    }

    @Override
    public boolean isPushable() {
        return false;
    }
}
