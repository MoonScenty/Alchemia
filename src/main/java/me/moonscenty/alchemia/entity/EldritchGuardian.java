package me.moonscenty.alchemia.entity;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.entity.cult.Cultist;
import me.moonscenty.alchemia.entity.cult.CultistKnight;
import me.moonscenty.alchemia.item.WispyEssenceItem;
import me.moonscenty.alchemia.network.Mist;
import me.moonscenty.alchemia.outer.OuterLands;
import me.moonscenty.alchemia.player.WarpData;
import me.moonscenty.alchemia.player.WarpHandler;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * An eldritch guardian: a hooded thing from the Outer Lands, tall and floating, trailing black smoke.
 *
 * <h3>How it fights</h3>
 * Further off than eight blocks it casts, every one to two seconds: nine times in ten a dark orb from one hand or the
 * other that bursts on whatever it meets, weakening everything about it that is alive and not undead; otherwise, if it
 * can see its target, a screech that withers for twenty seconds and leaves a player one to three points of warp that
 * pass. Nearer, it strikes twice a second. Magic does it half the harm it would.
 *
 * <h3>What follows it</h3>
 * Out of its own lands it brings a fog with it: every five seconds whoever is within sixteen blocks (twenty-four on
 * hard) finds the fog closing in. It is drawn faint, and fainter with distance.
 *
 * <h3>Where it comes from</h3>
 * Warp calls them up in the fog. One walks the eerie lands now and then, never within thirty-two blocks of another.
 *
 * <h3>Where this differs from the original</h3>
 * The original's screech was a ripple drawn on a hemisphere of its own; here it is the game's own sonic boom.
 */
public class EldritchGuardian extends Monster implements RangedAttackMob, EldritchMob, ArmLifting {
    /** How far an arm is raised, for the drawing. */
    public float armLiftL;
    public float armLiftR;

    @Override
    public float leftArmLift() {
        return armLiftL;
    }

    @Override
    public float rightArmLift() {
        return armLiftR;
    }
    private boolean lastBlast;

    public EldritchGuardian(EntityType<? extends EldritchGuardian> type, Level level) {
        super(type, level);
        xpReward = 20;
    }

    public EldritchGuardian(Level level) {
        this(ModEntities.ELDRITCH_GUARDIAN.get(), level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 50.0)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.ARMOR, 4.0);
    }

    /** Somewhere one may walk of itself: not where another already is, and not in the full light of day. */
    public static boolean checkSpawnRules(EntityType<EldritchGuardian> type, ServerLevelAccessor level,
            MobSpawnType reason, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL
                && level.getEntitiesOfClass(EldritchGuardian.class, new AABB(pos).inflate(32.0, 16.0, 32.0)).isEmpty()
                && level.getMaxLocalRawBrightness(pos) <= 11
                && checkMobSpawnRules(type, level, reason, pos, random);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new Casting(this));
        goalSelector.addGoal(3, new QuickMeleeGoal(this, 1.0, false));
        goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 0.8));
        goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Cultist.class, true));
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        return other instanceof EldritchMob || super.isAlliedTo(other);
    }

    @Override
    public boolean canPickUpLoot() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(net.minecraft.tags.DamageTypeTags.WITCH_RESISTANT_TO)) {
            amount /= 2.0F;
        }
        return super.hurt(source, amount);
    }

    // --- the Outer Lands' ward -------------------------------------------------

    /** How often its ward mends a point, in ticks, while nothing is hurting it. */
    public static final int WARD_MENDS_EVERY = 25;
    private static final ResourceLocation WARD = Alchemia.id("outer_lands_ward");

    /** In the Outer Lands it is warded, the original's: half its health more, worn as absorption. */
    public boolean warded() {
        return level().dimension() == OuterLands.LEVEL;
    }

    /** How much its ward holds: half its health before anything else was added. */
    public int ward() {
        return (int) getAttribute(Attributes.MAX_HEALTH).getBaseValue() / 2;
    }

    private void makeRoomForWard() {
        AttributeInstance room = getAttribute(Attributes.MAX_ABSORPTION);
        if (room != null && !room.hasModifier(WARD)) {
            room.addPermanentModifier(new AttributeModifier(WARD, ward(), AttributeModifier.Operation.ADD_VALUE));
        }
    }

    /** It arrives with the ward whole. */
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            @Nullable SpawnGroupData data) {
        SpawnGroupData spawned = super.finalizeSpawn(level, difficulty, reason, data);
        if (warded()) {
            putOnWard();
        }
        return spawned;
    }

    /** Gives it the whole ward at once. */
    public void putOnWard() {
        makeRoomForWard();
        setAbsorptionAmount(getAbsorptionAmount() + ward());
    }

    /** And mends it a point at a time while it is not being hurt. */
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (warded() && invulnerableTime <= 0 && tickCount % WARD_MENDS_EVERY == 0) {
            makeRoomForWard();
            if (getAbsorptionAmount() < ward()) {
                setAbsorptionAmount(getAbsorptionAmount() + 1.0F);
            }
        }
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return !hasRestriction();
    }

    // --- every tick ----------------------------------------------------------

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            armLiftL = Math.max(0.0F, armLiftL - 0.05F);
            armLiftR = Math.max(0.0F, armLiftR - 0.05F);
            me.moonscenty.alchemia.client.EldritchEffects.smoke(this);
            return;
        }
        if ((tickCount == 1 || tickCount % 100 == 0) && level().getDifficulty() != Difficulty.EASY) {
            double reach = level().getDifficulty() == Difficulty.HARD ? 24.0 : 16.0;
            for (Player player : level().players()) {
                if (player instanceof ServerPlayer served && player.isAlive()
                        && player.distanceToSqr(this) < reach * reach) {
                    PacketDistributor.sendToPlayer(served, Mist.GUARDIAN);
                }
            }
        }
    }

    // --- casting -------------------------------------------------------------

    @Override
    public void performRangedAttack(LivingEntity target, float distance) {
        if (random.nextFloat() > 0.1F) {
            EldritchOrb orb = new EldritchOrb(level(), this);
            lastBlast = !lastBlast;
            level().broadcastEntityEvent(this, (byte) (lastBlast ? 16 : 15));
            float side = (getYRot() + (lastBlast ? 90 : 180)) % 360.0F * net.minecraft.util.Mth.DEG_TO_RAD;
            orb.setPos(orb.getX() - Math.cos(side) * 0.5, orb.getY(), orb.getZ() - Math.sin(side) * 0.5);
            Vec3 look = getLookAngle();
            orb.shoot(look.x, look.y, look.z, 1.0F, 2.0F);
            playSound(ModSounds.EG_ATTACK.get(), 2.0F, 1.0F + random.nextFloat() * 0.1F);
            level().addFreshEntity(orb);
        } else if (hasLineOfSight(target)) {
            if (level() instanceof ServerLevel served) {
                Vec3 face = getEyePosition().add(getLookAngle().scale(1.5));
                served.sendParticles(ParticleTypes.SONIC_BOOM, face.x, face.y, face.z, 1, 0.0, 0.0, 0.0, 0.0);
            }
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 400, 0), this);
            if (target instanceof Player player) {
                WarpHandler.add(player, WarpData.Kind.TEMPORARY, 1 + random.nextInt(3));
            }
            playSound(ModSounds.EG_SCREECH.get(), 3.0F, 1.0F + random.nextFloat() * 0.1F);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 15) {
            armLiftL = 0.5F;
        } else if (id == 16) {
            armLiftR = 0.5F;
        } else {
            super.handleEntityEvent(id);
        }
    }

    // --- what it leaves ------------------------------------------------------

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (random.nextBoolean()) {
            spawnAtLocation(WispyEssenceItem.of(ModAspects.UNDEAD), 1.0F);
        }
        if (random.nextBoolean()) {
            spawnAtLocation(WispyEssenceItem.of(ModAspects.ELDRITCH), 1.0F);
        }
        int looting = 0;
        if (source.getEntity() instanceof LivingEntity killer) {
            looting = EnchantmentHelper.getEnchantmentLevel(level.registryAccess()
                    .registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.LOOTING), killer);
        }
        if (random.nextInt(10) <= looting / 2.0F) {
            spawnAtLocation(new ItemStack(ModItems.ELDRITCH_EYE.get()));
        }
    }

    // --- its voice -----------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.EG_IDLE.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.EG_DEATH.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 500;
    }

    @Override
    protected float getSoundVolume() {
        return 1.5F;
    }

    // --- where it keeps to ---------------------------------------------------

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (hasRestriction()) {
            BlockPos home = getRestrictCenter();
            tag.putInt("HomeD", (int) getRestrictRadius());
            tag.putInt("HomeX", home.getX());
            tag.putInt("HomeY", home.getY());
            tag.putInt("HomeZ", home.getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("HomeD")) {
            restrictTo(new BlockPos(tag.getInt("HomeX"), tag.getInt("HomeY"), tag.getInt("HomeZ")),
                    tag.getInt("HomeD"));
        }
    }

    /** Casts from further off than eight blocks; nearer, it strikes. */
    static class Casting extends RangedAttackGoal {
        private final EldritchGuardian guardian;

        Casting(EldritchGuardian guardian) {
            super(guardian, 1.0, 20, 40, 24.0F);
            this.guardian = guardian;
        }

        @Override
        public boolean canUse() {
            if (!super.canUse()) {
                return false;
            }
            LivingEntity target = guardian.getTarget();
            if (target == null || !target.isAlive()) {
                guardian.setTarget(null);
                return false;
            }
            return guardian.distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ()) >= 64.0;
        }
    }
}
