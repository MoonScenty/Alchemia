package me.moonscenty.alchemia.entity;

import java.util.UUID;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.client.FireBatEffects;
import me.moonscenty.alchemia.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
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
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * A firebat, as the original's: a bat that burns, chases what it is set on, and either sets it alight, bites it, or
 * bursts on it.
 *
 * <p>Two lives. A wild one hangs under a ceiling until somebody comes within four blocks, then goes for the nearest
 * player within twelve. A summoned one is let off a wand with its target already chosen, never picks another, and
 * withers within a second once that target is gone. Nothing else keeps it in the world — no timer, no leash.
 *
 * <p>It does not walk or path. Every tick it pushes itself a tenth of the way towards a fixed speed in the
 * direction of where it wants to be, which is the original's flight and vanilla's old bat's.
 *
 * <p>What a wand worked into it is carried as four flags and a bonus. A bomb always bursts on the first thing it
 * reaches; a devil never bursts at all and is bigger and harder; a vampire always bites, heals itself a little,
 * and gives its summoner a moment of regeneration. Any bat but a devil may burst one time in ten.
 */
public class FireBat extends Monster {
    /** The flags, one to a bit, in the original's order. */
    private static final EntityDataAccessor<Byte> FLAGS =
            SynchedEntityData.defineId(FireBat.class, EntityDataSerializers.BYTE);
    private static final int HANGING = 0;
    private static final int SUMMONED = 1;
    private static final int EXPLOSIVE = 2;
    private static final int DEVIL = 3;
    private static final int VAMPIRE = 4;

    /** The original's numbers. */
    private static final double LIVES = 5.0;
    /**
     * What a devil can take. The original meant fifteen and said so, but set it before the bat knew it was a devil,
     * so every devil it ever summoned had five. Fifteen is what the upgrade was sold as.
     */
    private static final double DEVIL_LIVES = 15.0;
    private static final double WILD_BITE = 1.0;
    private static final double SUMMONED_BITE = 2.0;
    private static final double DEVIL_BITE = 3.0;
    /** How hard it flies, across and up, and how much of the gap to that it closes in a tick. */
    private static final double ACROSS = 0.5;
    private static final double UP = 0.7;
    private static final double CLOSES = 0.1;
    private static final double FALLS_OFF = 0.6;
    /** How far a wild one looks for somebody, and how near they come before one hanging wakes. */
    private static final double HUNTS = 12.0;
    private static final double WAKES = 4.0;
    /** How long between bites, and the reach of one. */
    private static final int BITES_EVERY = 20;
    private static final float REACHES = 2.5F;
    /** A burst: one time in ten for any but a devil, every time for a bomb, and what a rank of force adds. */
    private static final int BURSTS_ONE_IN = 10;
    private static final float BURST = 1.5F;
    private static final float BURST_A_RANK = 0.33F;
    /** How long what it lights burns, in seconds, for one let off a wand and for a wild one. */
    private static final int BURNS_SUMMONED = 4;
    private static final int BURNS_WILD = 2;
    /** What a summoned one with nothing to chase takes each tick until it is gone. */
    private static final float WITHERS = 2.0F;
    /** A vampire's sip, and the regeneration it hands its summoner. */
    private static final float SIPS = 1.0F;
    private static final int GIVES_FOR = 26;
    private static final int GIVES_STRENGTH = 1;

    @Nullable
    private BlockPos flyingTo;
    private int biteIn;
    /** What a wand's force added to its bite and to its burst. */
    private int bonus;
    /** Who let it off, if anybody did. Not saved: a bat outlives no session it was summoned in. */
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

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FLAGS, (byte) 0);
    }

    // --- what it is --------------------------------------------------------

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

    /**
     * Sends it after something, as a wand does: awake, set on that and nothing else, and as hard as what the wand
     * worked into it makes it.
     *
     * @param summoner whoever let it off, who gets the credit for what it kills and a vampire's regeneration
     * @param force    the wand's rank of force, added to its bite, and to its burst if it is a bomb
     */
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

    /** Its health and bite, as what it now is makes them. */
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

    // --- flying ------------------------------------------------------------

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

    /** The original's trail: smoke and a flame every tick from any but a vampire, and sparks off a bomb. */
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

    /** Somewhere near to make for, changed now and then or once it is reached, and now and then a ceiling to hang from. */
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

    /** A tenth of the way towards full speed in each direction it has to go, and its face turned along it. */
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

    /** A wild one goes for the nearest player; a summoned one has its target and wants no other. */
    @Nullable
    private LivingEntity prey() {
        return isSummoned() ? null : level().getNearestPlayer(this, HUNTS);
    }

    // --- biting ------------------------------------------------------------

    /**
     * Its one attack, once a second at most, when it is close enough and level with what it is after: a burst, a
     * bite, or a light.
     */
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
            // it bursts on what it reached and takes nothing of the world with it, as the original's did
            level().explode(this, getX(), getY(), getZ(), BURST + (isExplosive() ? bonus * BURST_A_RANK : 0.0F),
                    false, Level.ExplosionInteraction.NONE);
            discard();
        } else if (isVampire() || level().random.nextBoolean()) {
            // it bites but does not knock back: what it bit is left moving as it was
            Vec3 moving = target.getDeltaMovement();
            doHurtTarget(target);
            target.setDeltaMovement(moving);
            target.hasImpulse = false;
        } else {
            target.igniteForSeconds(isSummoned() ? BURNS_SUMMONED : BURNS_WILD);
        }
        playSound(SoundEvents.BAT_HURT, 0.5F, 0.9F + level().random.nextFloat() * 0.2F);
    }

    // --- what it takes -----------------------------------------------------

    /** Neither fire nor a burst touches it, its own or another's. Being hit wakes it. */
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

    /** Water puts it out, a point at a time, rain included. */
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

    /** Gunpowder from a wild one; a summoned one leaves nothing. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (isSummoned()) {
            return;
        }
        int count = random.nextInt(3);
        if (source.getEntity() instanceof LivingEntity killer) {
            count += random.nextInt(1 + EnchantmentHelper.getEnchantmentLevel(level.registryAccess()
                    .registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.LOOTING), killer));
        }
        if (count > 0) {
            spawnAtLocation(new ItemStack(Items.GUNPOWDER, count));
        }
    }

    // --- how it sounds -----------------------------------------------------

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

    // --- saving ------------------------------------------------------------

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
