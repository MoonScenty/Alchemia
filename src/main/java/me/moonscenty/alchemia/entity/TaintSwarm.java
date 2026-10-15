package me.moonscenty.alchemia.entity;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * A taint swarm: a cloud of tiny purple flies, two blocks across, that comes up out of a taint geyser.
 *
 * <p>It drifts about low over the ground until a player comes within eight blocks, then closes on them and bites,
 * three times every two seconds or so, without knocking them back. Every bite weakens. It has no body to speak of:
 * nothing pushes it, it never falls hard, and it treads on no pressure plate.
 */
public class TaintSwarm extends Monster implements TaintedMob {
    private static final EntityDataAccessor<Byte> FLAGS = SynchedEntityData.defineId(TaintSwarm.class,
            EntityDataSerializers.BYTE);
    private static final int SUMMONED = 2;

    /** The original's numbers. */
    private static final double LIVES = 30.0;
    private static final double BITES = 2.0;
    private static final double HUNTS = 8.0;
    private static final float REACH = 3.0F;
    private static final int WEAKENS_FOR = 100;
    /** What a summoned swarm with nothing to bite takes each tick until it is gone. */
    private static final float WITHERS = 5.0F;

    @Nullable
    private BlockPos driftingTo;
    private int biteIn;

    public TaintSwarm(EntityType<? extends TaintSwarm> type, Level level) {
        super(type, level);
    }

    public TaintSwarm(Level level) {
        this(ModEntities.TAINT_SWARM.get(), level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, LIVES)
                .add(Attributes.ATTACK_DAMAGE, BITES)
                .add(Attributes.ATTACK_KNOCKBACK, 0.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FLAGS, (byte) 0);
    }

    public boolean isSummoned() {
        return (entityData.get(FLAGS) & SUMMONED) != 0;
    }

    public void setSummoned(boolean summoned) {
        byte flags = entityData.get(FLAGS);
        entityData.set(FLAGS, (byte) (summoned ? flags | SUMMONED : flags & ~SUMMONED));
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !TaintedMob.allied(target) && super.canAttack(target);
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        return TaintedMob.allied(other) || super.isAlliedTo(other);
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 moving = getDeltaMovement();
        setDeltaMovement(moving.x, moving.y * 0.6, moving.z);
        if (level().isClientSide) {
            me.moonscenty.alchemia.client.TaintEffects.swarm(this);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            return;
        }
        LivingEntity target = getTarget();
        if (target == null) {
            if (isSummoned()) {
                hurt(damageSources().generic(), WITHERS);
            }
            drift();
            setTarget(isSummoned() ? null : level().getNearestPlayer(this, HUNTS));
        } else if (target.isAlive()) {
            steer(target.getX(), target.getY() + target.getEyeHeight(), target.getZ(), 0.025);
            if (hasLineOfSight(target)) {
                bite(target, distanceTo(target));
            }
        } else {
            setTarget(null);
        }
        if (getTarget() instanceof Player player && player.getAbilities().invulnerable) {
            setTarget(null);
        }
        if (biteIn > 0) {
            biteIn--;
        }
    }

    /** About where it is, never far over the ground, and never out of tainted land. */
    private void drift() {
        if (driftingTo != null && (!level().isEmptyBlock(driftingTo)
                || driftingTo.getY() < level().getMinBuildHeight() + 1
                || driftingTo.getY() > level().getHeight(Heightmap.Types.MOTION_BLOCKING, driftingTo.getX(),
                        driftingTo.getZ()) + 2
                || !level().getBiome(driftingTo).is(me.moonscenty.alchemia.worldgen.ModBiomes.TAINTED_LAND))) {
            driftingTo = null;
        }
        if (driftingTo == null || random.nextInt(30) == 0 || driftingTo.distToCenterSqr(position()) < 4.0) {
            driftingTo = BlockPos.containing(getX() + random.nextInt(7) - random.nextInt(7),
                    getY() + random.nextInt(6) - 2, getZ() + random.nextInt(7) - random.nextInt(7));
        }
        steer(driftingTo.getX() + 0.5, driftingTo.getY() + 0.1, driftingTo.getZ() + 0.5, 0.015);
    }

    /** Some way towards a fixed speed in the direction of where it is going, closing faster up than across. */
    private void steer(double x, double y, double z, double closes) {
        Vec3 moving = getDeltaMovement();
        double dx = moving.x + (Math.signum(x - getX()) * 0.5 - moving.x) * closes;
        double dy = moving.y + (Math.signum(y - getY()) * 0.7 - moving.y) * 0.1;
        double dz = moving.z + (Math.signum(z - getZ()) * 0.5 - moving.z) * closes;
        setDeltaMovement(dx, dy, dz);
        float facing = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
        zza = 0.1F;
        setYRot(getYRot() + Mth.wrapDegrees(facing - getYRot()));
    }

    /** Within three blocks and overlapping it up and down, a bite that weakens and does not knock back. */
    public void bite(LivingEntity target, float distance) {
        if (biteIn > 0 || distance >= REACH || target.getBoundingBox().maxY <= getBoundingBox().minY
                || target.getBoundingBox().minY >= getBoundingBox().maxY) {
            return;
        }
        biteIn = 10 + random.nextInt(5);
        Vec3 was = target.getDeltaMovement();
        if (doHurtTarget(target)) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, WEAKENS_FOR, 0), this);
        }
        target.setDeltaMovement(was);
        target.hurtMarked = false;
        playSound(ModSounds.SWARM_ATTACK.get(), 0.3F, 0.9F + random.nextFloat() * 0.2F);
    }

    /** It is a cloud of light-hungry things; nothing it stands in can darken it. */
    @Override
    public float getLightLevelDependentMagicValue() {
        return 1.0F;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return true;
    }

    /** Half the time, a little of the goo it came up through. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (random.nextBoolean()) {
            spawnAtLocation(new ItemStack(ModItems.TAINTED_GOO.get()), getBbHeight() / 2.0F);
        }
    }

    @Override
    protected float getSoundVolume() {
        return 0.1F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.SWARM.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.SWARM_ATTACK.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SWARM_ATTACK.get();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("Flags", entityData.get(FLAGS));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(FLAGS, tag.getByte("Flags"));
    }
}
