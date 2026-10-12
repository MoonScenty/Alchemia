package me.moonscenty.alchemia.entity;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aspect.OwnAspects;
import me.moonscenty.alchemia.item.WispyEssenceItem;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

/**
 * A wisp: a drifting knot of one aspect, which spits sparks at whoever it takes against.
 *
 * <p>Most are one of the six primals; one in ten is a compound. It drifts about at random until it takes against
 * somebody -- whoever strikes it, or, now and then, the nearest player within sixteen blocks -- and then closes in to
 * about eleven blocks and zaps them once a second or so. Something standing still is the easier to hit, and takes a
 * point more. Killed, it leaves two points of its aspect behind as wispy essence.
 *
 * <p>Wisps come of themselves in the Nether, in the dark, and from a chunk thick with flux.
 */
public class Wisp extends FlyingMob implements Enemy, OwnAspects {
    private static final EntityDataAccessor<String> ASPECT =
            SynchedEntityData.defineId(Wisp.class, EntityDataSerializers.STRING);

    /** The original's numbers. */
    private static final double LIVES = 22.0;
    private static final double ZAPS = 3.0;
    private static final double REACH = 16.0;
    /** How hard it flies across and up, how much of the gap to that it closes a tick, and how it drifts idly. */
    private static final double ACROSS = 0.5;
    private static final double UP = 0.7;
    private static final double CLOSES = 0.1;
    private static final float DRIFTING = 0.15F;
    private static final float CHASING = 0.5F;
    private static final double FALLS_OFF = 0.6;
    /** How long it holds a grudge, how long between zaps, and how likely a zap is to land. */
    private static final int GRUDGE = 200;
    private static final int ZAP_AT = 20;
    private static final float LANDS_ON_STILL = 0.66F;
    private static final float LANDS_ON_MOVING = 0.4F;
    /** At most this many wisps within sixteen blocks before another will come of itself. */
    private static final int CROWD = 8;

    @Nullable
    private BlockPos driftingTo;
    private int grudge;
    private int attackCounter;

    public Wisp(EntityType<? extends Wisp> type, Level level) {
        super(type, level);
        xpReward = 5;
    }

    public Wisp(Level level) {
        this(ModEntities.WISP.get(), level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, LIVES)
                .add(Attributes.ATTACK_DAMAGE, ZAPS);
    }

    /** Where one may come of itself: not crowded, dark as for any monster, and not in peace. */
    public static boolean checkSpawnRules(EntityType<Wisp> type, ServerLevelAccessor level, MobSpawnType reason,
            BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL
                && level.getEntitiesOfClass(Wisp.class, new net.minecraft.world.phys.AABB(pos).inflate(REACH)).size()
                        < CROWD
                && Monster.isDarkEnoughToSpawn(level, pos, random)
                && checkMobSpawnRules(type, level, reason, pos, random);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ASPECT, "");
    }

    /** What it is a wisp of; nothing until it has drifted for a tick. */
    @Nullable
    public Holder<Aspect> aspect() {
        String id = entityData.get(ASPECT);
        if (id.isEmpty()) {
            return null;
        }
        ResourceLocation key = ResourceLocation.tryParse(id);
        return key == null ? null : ModAspects.REGISTRY.getHolder(key).map(holder -> (Holder<Aspect>) holder)
                .orElse(null);
    }

    public void setAspect(Holder<Aspect> aspect) {
        entityData.set(ASPECT, aspect.unwrapKey().map(key -> key.location().toString()).orElse(""));
    }

    /** Two of its own aspect, on top of what any wisp is. */
    @Override
    public AspectList ownAspects() {
        Holder<Aspect> aspect = aspect();
        return aspect == null ? AspectList.EMPTY : AspectList.of(aspect, 2);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof LivingEntity direct) {
            setTarget(direct);
            grudge = GRUDGE;
        }
        if (source.getEntity() instanceof LivingEntity owner) {
            setTarget(owner);
            grudge = GRUDGE;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            if (tickCount <= 1) {
                me.moonscenty.alchemia.client.WispEffects.burst(this);
            }
            if (aspect() != null && random.nextBoolean()) {
                me.moonscenty.alchemia.client.WispEffects.trail(this);
            }
        }
        Vec3 moving = getDeltaMovement();
        setDeltaMovement(moving.x, moving.y * FALLS_OFF, moving.z);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!isAlive() || level().isClientSide) {
            return;
        }
        if (aspect() == null) {
            setAspect(random.nextInt(10) != 0 ? ModAspects.randomPrimal(random)
                    : ModAspects.compounds().get(random.nextInt(ModAspects.compounds().size())));
        }
        if (level().getDifficulty() == Difficulty.PEACEFUL) {
            discard();
            return;
        }
        LivingEntity target = getTarget();
        if (target != null && hasLineOfSight(target)) {
            if (distanceToSqr(target) > REACH * REACH / 2.0) {
                steer(target.getX(), target.getY() + target.getEyeHeight() * 0.66, target.getZ(), CHASING);
            }
        } else {
            drift();
        }
        if (target instanceof Player player && player.getAbilities().invulnerable
                || target != null && !target.isAlive()) {
            setTarget(null);
        }
        grudge--;
        if (random.nextInt(1000) == 0 && (getTarget() == null || grudge-- <= 0)) {
            Player nearest = level().getNearestPlayer(this, REACH);
            setTarget(nearest);
            if (nearest != null) {
                grudge = 50;
            }
        }
        zap();
    }

    /** About where it was, now and then somewhere new: six blocks across and a little up or down. */
    private void drift() {
        if (driftingTo != null && (!level().isEmptyBlock(driftingTo)
                || driftingTo.getY() < level().getMinBuildHeight() + 1
                || driftingTo.getY() > level().getHeight(
                        net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                        driftingTo.getX(), driftingTo.getZ()) + 8)) {
            driftingTo = null;
        }
        if (driftingTo == null || random.nextInt(30) == 0 || driftingTo.distToCenterSqr(position()) < 4.0) {
            driftingTo = BlockPos.containing(getX() + random.nextInt(7) - random.nextInt(7),
                    getY() + random.nextInt(6) - 2, getZ() + random.nextInt(7) - random.nextInt(7));
        }
        steer(driftingTo.getX() + 0.5, driftingTo.getY() + 0.1, driftingTo.getZ() + 0.5, DRIFTING);
    }

    /** A tenth of the way towards a fixed speed in the direction of where it is going, facing it. */
    private void steer(double x, double y, double z, float forward) {
        Vec3 moving = getDeltaMovement();
        double dx = moving.x + (Math.signum(x - getX()) * ACROSS - moving.x) * CLOSES;
        double dy = moving.y + (Math.signum(y - getY()) * UP - moving.y) * CLOSES;
        double dz = moving.z + (Math.signum(z - getZ()) * ACROSS - moving.z) * CLOSES;
        setDeltaMovement(dx, dy, dz);
        float facing = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
        zza = forward;
        setYRot(getYRot() + Mth.wrapDegrees(facing - getYRot()));
    }

    /** Once a second or so, a spark at whatever it has taken against, if it can see them. */
    private void zap() {
        LivingEntity target = getTarget();
        if (target == null || target.distanceToSqr(this) >= REACH * REACH) {
            return;
        }
        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        float facing = -(float) Mth.atan2(dx, dz) * Mth.RAD_TO_DEG;
        setYRot(facing);
        yBodyRot = facing;
        if (!hasLineOfSight(target)) {
            if (attackCounter > 0) {
                attackCounter--;
            }
            return;
        }
        if (++attackCounter != ZAP_AT) {
            return;
        }
        playSound(ModSounds.ZAP.get(), 1.0F, 1.1F);
        spark((ServerLevel) level(), target);
        float damage = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
        Vec3 moving = target.getDeltaMovement();
        boolean still = Math.abs(moving.x) <= 0.1 && Math.abs(moving.y) <= 0.1 && Math.abs(moving.z) <= 0.1;
        if (still ? random.nextFloat() < LANDS_ON_STILL : random.nextFloat() < LANDS_ON_MOVING) {
            target.hurt(damageSources().mobAttack(this), still ? damage + 1.0F : damage);
        }
        attackCounter = -20 + random.nextInt(20);
    }

    /** The spark, drawn as a line of little sparks from the wisp to what it struck. */
    private void spark(ServerLevel level, LivingEntity target) {
        Vec3 from = position().add(0.0, getBbHeight() / 2.0, 0.0);
        Vec3 to = target.position().add(0.0, target.getBbHeight() / 2.0, 0.0);
        int steps = Math.max(2, (int) (from.distanceTo(to) * 3));
        for (int step = 0; step <= steps; step++) {
            Vec3 at = from.lerp(to, step / (double) steps);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, at.x, at.y, at.z, 1, 0.05, 0.05, 0.05, 0.0);
        }
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (level().isClientSide) {
            me.moonscenty.alchemia.client.WispEffects.burst(this);
        }
    }

    /** Two points of what it was, as wispy essence. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        Holder<Aspect> aspect = aspect();
        if (aspect != null) {
            spawnAtLocation(WispyEssenceItem.of(aspect));
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.WISP_LIVE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.FIRE_EXTINGUISH;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.WISP_DEAD.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.25F;
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 2;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return true;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("Aspect", entityData.get(ASPECT));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(ASPECT, tag.getString("Aspect"));
    }

    /**
     * A flux trouble: a wisp, five blocks over the ground, one time in three a wisp of flux itself.
     *
     * @return whether one came
     */
    public static boolean fromFlux(ServerLevel level, BlockPos ground, RandomSource random) {
        if (ground.getY() + 5 >= level.getMaxBuildHeight()) {
            return false;
        }
        Wisp wisp = new Wisp(level);
        wisp.moveTo(ground.getX() + 0.5, ground.getY() + 5, ground.getZ() + 0.5, 0.0F, 0.0F);
        if (random.nextInt(3) == 0) {
            wisp.setAspect(ModAspects.FLUX);
        }
        return level.addFreshEntity(wisp);
    }

    /** Not anything a lead can hold. */
    @Override
    public boolean canBeLeashed() {
        return false;
    }

    /** It is light and air, and pushes nothing about. */
    @Override
    protected void doPush(Entity entity) {
    }
}
