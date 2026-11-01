package me.moonscenty.alchemia.entity;

import java.util.UUID;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.client.FireBatEffects;
import me.moonscenty.alchemia.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FireBat extends Monster {
    private static final EntityDataAccessor<Byte> FLAGS =
            SynchedEntityData.defineId(FireBat.class, EntityDataSerializers.BYTE);
    private static final int HANGING = 0;
    private static final int SUMMONED = 1;
    private static final int EXPLOSIVE = 2;
    private static final int DEVIL = 3;
    private static final int VAMPIRE = 4;

    // original's values
    private static final double LIVES = 5.0;
    // the original meant 15 but applied it before the bat knew it was a devil, so devils got 5
    private static final double DEVIL_LIVES = 15.0;
    private static final double WILD_BITE = 1.0;
    private static final double SUMMONED_BITE = 2.0;
    private static final double DEVIL_BITE = 3.0;
    // target speeds across and up, and the share of the gap closed per tick
    private static final double ACROSS = 0.5;
    private static final double UP = 0.7;
    private static final double CLOSES = 0.1;
    private static final double FALLS_OFF = 0.6;
    private static final double HUNTS = 12.0;
    private static final double WAKES = 4.0;
    private static final int BITES_EVERY = 20;
    private static final float REACHES = 2.5F;
    private static final int BURSTS_ONE_IN = 10;
    private static final float BURST = 1.5F;
    private static final float BURST_A_RANK = 0.33F;
    // seconds alight
    private static final int BURNS_SUMMONED = 4;
    private static final int BURNS_WILD = 2;
    // damage per tick to a summoned bat whose target is gone
    private static final float WITHERS = 2.0F;
    private static final float SIPS = 1.0F;
    private static final int GIVES_FOR = 26;
    private static final int GIVES_STRENGTH = 1;

    @Nullable
    private BlockPos flyingTo;
    private int biteIn;
    private int bonus;
    // not saved
    @Nullable
    private UUID summoner;

    public FireBat(EntityType<? extends FireBat> type, Level level) {
        super(type, level);
        setHanging(true);
    }

    public FireBat(Level level) {
        this(ModEntities.FIRE_BAT.get(), level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, LIVES)
                .add(Attributes.ATTACK_DAMAGE, WILD_BITE)
                .add(Attributes.ATTACK_KNOCKBACK, 0.0);
    }

    public static boolean checkSpawnRules(EntityType<FireBat> type, net.minecraft.world.level.ServerLevelAccessor level,
            net.minecraft.world.entity.MobSpawnType reason, BlockPos pos, net.minecraft.util.RandomSource random) {
        return level.getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL
                && level.getMaxLocalRawBrightness(pos) <= random.nextInt(7)
                && checkMobSpawnRules(type, level, reason, pos, random);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FLAGS, (byte) 0);
    }

    private boolean flag(int bit) {
        return (entityData.get(FLAGS) & (1 << bit)) != 0;
    }

    private void flag(int bit, boolean on) {
        byte flags = entityData.get(FLAGS);
        entityData.set(FLAGS, (byte) (on ? flags | (1 << bit) : flags & ~(1 << bit)));
    }

    public boolean isHanging() {
        return flag(HANGING);
    }

    public void setHanging(boolean hanging) {
        flag(HANGING, hanging);
    }

    public boolean isSummoned() {
        return flag(SUMMONED);
    }

    public boolean isExplosive() {
        return flag(EXPLOSIVE);
    }

    public boolean isDevil() {
        return flag(DEVIL);
    }

    public boolean isVampire() {
        return flag(VAMPIRE);
    }

    public FireBat summon(LivingEntity target, @Nullable LivingEntity summoner, int force, boolean bomb,
            boolean devil, boolean vampire) {
        bonus = force;
        flag(SUMMONED, true);
        flag(EXPLOSIVE, bomb);
        flag(DEVIL, devil);
        flag(VAMPIRE, vampire);
        setHanging(false);
        this.summoner = summoner == null ? null : summoner.getUUID();
        setTarget(target);
        settle();
        return this;
    }

    private void settle() {
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(
                isSummoned() ? (isDevil() ? DEVIL_BITE : SUMMONED_BITE) + bonus : WILD_BITE);
        double lives = isDevil() ? DEVIL_LIVES : LIVES;
        if (getAttribute(Attributes.MAX_HEALTH).getBaseValue() != lives) {
            getAttribute(Attributes.MAX_HEALTH).setBaseValue(lives);
            setHealth((float) lives);
        }
    }

    @Nullable
    private LivingEntity summoner() {
        if (summoner == null || !(level() instanceof ServerLevel served)) {
            return null;
        }
        return served.getEntity(summoner) instanceof LivingEntity living ? living : null;
    }

    @Override
    public void tick() {
        super.tick();
        if (isHanging()) {
            setDeltaMovement(Vec3.ZERO);
            setPosRaw(getX(), Mth.floor(getY()) + 1.0 - getBbHeight(), getZ());
        } else {
            setDeltaMovement(getDeltaMovement().multiply(1.0, FALLS_OFF, 1.0));
        }
        if (level().isClientSide) {
            smoulder();
        }
    }

    private void smoulder() {
        if (isExplosive()) {
            FireBatEffects.fuse(this);
        }
        if (isVampire()) {
            return;
        }
        double middle = yo + getBbHeight() / 2.0;
        level().addParticle(ParticleTypes.SMOKE, xo + scatter(), middle, zo + scatter(), 0.0, 0.0, 0.0);
        level().addParticle(ParticleTypes.FLAME, xo + scatter(), middle, zo + scatter(), 0.0, 0.0, 0.0);
    }

    private double scatter() {
        return (level().random.nextFloat() - level().random.nextFloat()) * 0.2F;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (biteIn > 0) {
            biteIn--;
        }
        BlockPos here = blockPosition();
        BlockPos above = here.above();
        LivingEntity target = getTarget();

        if (isHanging()) {
            if (!level().getBlockState(above).isRedstoneConductor(level(), above)) {
                takeOff(here);
            } else {
                if (random.nextInt(200) == 0) {
                    yHeadRot = random.nextInt(360);
                }
                if (level().getNearestPlayer(this, WAKES) != null) {
                    takeOff(here);
                }
            }
        } else if (target == null) {
            if (isSummoned()) {
                hurt(damageSources().generic(), WITHERS);
            }
            wander(above);
        } else {
            steer(target.getX() - getX(), target.getY() + target.getEyeHeight() * 0.66F - getY(),
                    target.getZ() - getZ());
        }

        if (getTarget() == null) {
            setTarget(prey());
        } else if (getTarget().isAlive()) {
            if (hasLineOfSight(getTarget())) {
                bite(getTarget());
            }
        } else {
            setTarget(null);
        }
        if (getTarget() instanceof Player player && player.getAbilities().invulnerable) {
            setTarget(null);
        }
    }

    private void takeOff(BlockPos here) {
        setHanging(false);
        if (!isSilent()) {
            level().levelEvent(null, LevelEvent.SOUND_BAT_LIFTOFF, here, 0);
        }
    }

    private void wander(BlockPos above) {
        if (flyingTo != null && (!level().isEmptyBlock(flyingTo) || flyingTo.getY() <= level().getMinBuildHeight())) {
            flyingTo = null;
        }
        if (flyingTo == null || random.nextInt(30) == 0 || flyingTo.closerToCenterThan(position(), 2.0)) {
            flyingTo = BlockPos.containing(getX() + random.nextInt(7) - random.nextInt(7),
                    getY() + random.nextInt(6) - 2.0, getZ() + random.nextInt(7) - random.nextInt(7));
        }
        steer(flyingTo.getX() + 0.5 - getX(), flyingTo.getY() + 0.1 - getY(), flyingTo.getZ() + 0.5 - getZ());
        if (random.nextInt(100) == 0 && level().getBlockState(above).isRedstoneConductor(level(), above)) {
            setHanging(true);
        }
    }

    private void steer(double dx, double dy, double dz) {
        Vec3 moving = getDeltaMovement();
        Vec3 now = moving.add((Math.signum(dx) * ACROSS - moving.x) * CLOSES,
                (Math.signum(dy) * UP - moving.y) * CLOSES,
                (Math.signum(dz) * ACROSS - moving.z) * CLOSES);
        setDeltaMovement(now);
        float facing = (float) (Mth.atan2(now.z, now.x) * Mth.RAD_TO_DEG) - 90.0F;
        zza = 0.5F;
        setYRot(getYRot() + Mth.wrapDegrees(facing - getYRot()));
    }

    @Nullable
    private LivingEntity prey() {
        return isSummoned() ? null : level().getNearestPlayer(this, HUNTS);
    }

    private void bite(LivingEntity target) {
        if (biteIn > 0 || distanceTo(target) >= Math.max(REACHES, target.getBbWidth() * 1.1F)
                || target.getBoundingBox().maxY <= getBoundingBox().minY
                || target.getBoundingBox().minY >= getBoundingBox().maxY) {
            return;
        }
        LivingEntity master = summoner();
        if (isSummoned() && master instanceof Player player) {
            target.setLastHurtByPlayer(player);
        }
        if (isVampire()) {
            if (master != null && !master.hasEffect(MobEffects.REGENERATION)) {
                master.addEffect(new MobEffectInstance(MobEffects.REGENERATION, GIVES_FOR, GIVES_STRENGTH));
            }
            heal(SIPS);
        }
        biteIn = BITES_EVERY;

        if ((isExplosive() || level().random.nextInt(BURSTS_ONE_IN) == 0) && !isDevil()) {
            target.invulnerableTime = 0;
            // breaks no blocks, as the original's
            level().explode(this, getX(), getY(), getZ(), BURST + (isExplosive() ? bonus * BURST_A_RANK : 0.0F),
                    false, Level.ExplosionInteraction.NONE);
            discard();
        } else if (isVampire() || level().random.nextBoolean()) {
            // no knockback: restore the target's motion after the bite
            Vec3 moving = target.getDeltaMovement();
            doHurtTarget(target);
            target.setDeltaMovement(moving);
            target.hasImpulse = false;
        } else {
            target.igniteForSeconds(isSummoned() ? BURNS_SUMMONED : BURNS_WILD);
        }
        playSound(SoundEvents.BAT_HURT, 0.5F, 0.9F + level().random.nextFloat() * 0.2F);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source) || source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_EXPLOSION)) {
            return false;
        }
        if (!level().isClientSide && isHanging()) {
            setHanging(false);
        }
        return super.hurt(source, amount);
    }

    @Override
    public void aiStep() {
        if (!level().isClientSide && isInWaterRainOrBubble()) {
            hurt(damageSources().drown(), 1.0F);
        }
        super.aiStep();
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (isSummoned()) {
            return;
        }
        int count = random.nextInt(3);
        if (source.getEntity() instanceof LivingEntity killer) {
            count += random.nextInt(1 + Looting.of(killer));
        }
        if (count > 0) {
            spawnAtLocation(new ItemStack(Items.GUNPOWDER, count));
        }
    }

    @Override
    protected float getSoundVolume() {
        return 0.1F;
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * 0.95F;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return isHanging() && random.nextInt(4) != 0 ? null : SoundEvents.BAT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BAT_DEATH;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("BatFlags", entityData.get(FLAGS));
        tag.putByte("damBonus", (byte) bonus);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(FLAGS, tag.getByte("BatFlags"));
        bonus = tag.getByte("damBonus");
        settle();
    }
}
