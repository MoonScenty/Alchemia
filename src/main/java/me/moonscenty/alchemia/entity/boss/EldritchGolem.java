package me.moonscenty.alchemia.entity.boss;

import me.moonscenty.alchemia.block.LootBlock;
import me.moonscenty.alchemia.entity.ChampionModifier;
import me.moonscenty.alchemia.entity.EldritchMob;
import me.moonscenty.alchemia.entity.cult.GolemOrb;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * The Eldritch Construct: a great stone golem left standing in its hall.
 * <p>
 * It stands frozen five seconds when it wakes, mending as it does. Then it walks its enemy down and strikes a heavy
 * blow that throws the struck a little into the air, smashing urns and crates and whatever else is soft enough
 * underfoot; fire does nothing to it.
 * <p>
 * The blow that would have killed it takes its head off instead: an explosion at its neck that breaks nothing, and
 * another five seconds frozen and mending. Headless it is worse. It gathers a charge over seven and a half seconds,
 * crackling, then lets it go in bolts that seek their mark, every quarter second while the charge lasts; and its
 * blows now throw things back as well as up.
 */
public class EldritchGolem extends ThaumcraftBoss implements EldritchMob, RangedAttackMob {
    private static final EntityDataAccessor<Boolean> HEADLESS = SynchedEntityData.defineId(EldritchGolem.class,
            EntityDataSerializers.BOOLEAN);
    public static final int DORMANT = 100;
    private static final int FULL_CHARGE = 150;
    private static final byte SWING = 4;
    private static final byte CHARGING = 19;

    private int beamCharge;
    private boolean chargingBeam;
    private int attackTimer;
    /** For the drawing: how long the arc to the floor still crackles, and where it goes. */
    public int arcing;
    public BlockPos arcTo = BlockPos.ZERO;
    private boolean ranged;

    public EldritchGolem(EntityType<? extends EldritchGolem> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return bossAttributes()
                .add(Attributes.MAX_HEALTH, 400.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.ARMOR, 6.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.1, false));
        goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, 0.8));
        goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HEADLESS, false);
    }

    public boolean isHeadless() {
        return entityData.get(HEADLESS);
    }

    public int attackTimer() {
        return attackTimer;
    }

    @Override
    public Component title(ChampionModifier modifier) {
        return Component.translatable("entity.alchemia.eldritch_golem.titled",
                Component.translatable(modifier.translationKey()));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            SpawnGroupData data) {
        setSpawnTimer(DORMANT);
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    /** Headless, it shoots as well. */
    private void loseHead() {
        entityData.set(HEADLESS, true);
        if (!ranged) {
            ranged = true;
            goalSelector.addGoal(2, new LongRangeAttackGoal(this, 3.0, 1.0, 5, 5, 24.0F));
        }
    }

    @Override
    public void tick() {
        if (spawnTimer() > 0) {
            heal(2.0F);
        }
        super.tick();
        if (level().isClientSide) {
            if (isHeadless()) {
                me.moonscenty.alchemia.client.EldritchEffects.golemNeck(this);
            }
            return;
        }
        if (isHeadless() && beamCharge <= 0) {
            chargingBeam = true;
        }
        if (isHeadless() && chargingBeam) {
            beamCharge++;
            level().broadcastEntityEvent(this, CHARGING);
            if (beamCharge == FULL_CHARGE) {
                chargingBeam = false;
            }
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (attackTimer > 0) {
            attackTimer--;
        }
        BlockPos under = blockPosition();
        BlockState there = level().getBlockState(under);
        if (getDeltaMovement().horizontalDistanceSqr() > 2.5E-7 && random.nextInt(5) == 0) {
            if (!there.isAir()) {
                level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, there),
                        getX() + (random.nextFloat() - 0.5) * getBbWidth(), getBoundingBox().minY + 0.1,
                        getZ() + (random.nextFloat() - 0.5) * getBbWidth(), 4.0 * (random.nextFloat() - 0.5), 0.5,
                        (random.nextFloat() - 0.5) * 4.0);
            }
            if (!level().isClientSide && there.getBlock() instanceof LootBlock) {
                level().destroyBlock(under, true);
            }
        }
        if (!level().isClientSide) {
            float hardness = there.getDestroySpeed(level(), under);
            if (!there.isAir() && hardness >= 0.0F && hardness <= 0.15F) {
                level().destroyBlock(under, true);
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!level().isClientSide && amount > getHealth() && !isHeadless() && !isInvulnerableTo(source)) {
            setSpawnTimer(DORMANT);
            double xx = Mth.cos(getYRot() % 360.0F / 180.0F * Mth.PI) * 0.75;
            double zz = Mth.sin(getYRot() % 360.0F / 180.0F * Mth.PI) * 0.75;
            level().explode(this, getX() + xx, getEyeY(), getZ() + zz, 2.0F, Level.ExplosionInteraction.NONE);
            loseHead();
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (attackTimer > 0) {
            return false;
        }
        attackTimer = 10;
        level().broadcastEntityEvent(this, SWING);
        boolean hit = target.hurt(damageSources().mobAttack(this),
                (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.75F);
        if (hit) {
            target.setDeltaMovement(target.getDeltaMovement().add(0.0, 0.2, 0.0));
            if (isHeadless()) {
                target.push(-Mth.sin(getYRot() * Mth.DEG_TO_RAD) * 1.5, 0.1, Mth.cos(getYRot() * Mth.DEG_TO_RAD) * 1.5);
            }
            target.hurtMarked = true;
        }
        return hit;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (!getSensing().hasLineOfSight(target) || chargingBeam || beamCharge <= 0) {
            return;
        }
        beamCharge -= 15 + random.nextInt(5);
        getLookControl().setLookAt(target.getX(), target.getBoundingBox().minY + target.getBbHeight() / 2.0,
                target.getZ(), 30.0F, 30.0F);
        Vec3 look = getViewVector(1.0F);
        GolemOrb orb = new GolemOrb(level(), this, target, false);
        orb.setPos(orb.getX() + look.x, orb.getY(), orb.getZ() + look.z);
        double dx = target.getX() + target.getDeltaMovement().x - getX();
        double dy = target.getY() - getY() - target.getBbHeight() / 2.0;
        double dz = target.getZ() + target.getDeltaMovement().z - getZ();
        orb.shoot(dx, dy, dz, 0.66F, 5.0F);
        playSound(ModSounds.EG_ATTACK.get(), 1.0F, 1.0F + random.nextFloat() * 0.1F);
        level().addFreshEntity(orb);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == SWING) {
            attackTimer = 10;
            playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
        } else if (id == CHARGING) {
            if (arcing == 0) {
                float r = 2.0F + random.nextFloat() * 2.0F;
                double angle = Math.toRadians(random.nextInt(360));
                int i = Mth.floor(getX() + r * Math.cos(angle));
                int j = Mth.floor(getY());
                int k = Mth.floor(getZ() + r * Math.sin(angle));
                BlockPos at = new BlockPos(i, j, k);
                for (int q = 0; q < 5 && level().isEmptyBlock(at); q++) {
                    at = at.below();
                }
                if (level().isEmptyBlock(at.above()) && !level().isEmptyBlock(at)) {
                    arcTo = at;
                    arcing = 8 + random.nextInt(5);
                    level().playLocalSound(getX(), getY(), getZ(), ModSounds.JACOBS.get(), getSoundSource(), 0.8F,
                            1.0F + (random.nextFloat() - random.nextFloat()) * 0.05F, false);
                }
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.IRON_GOLEM_STEP, 1.0F, 1.0F);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("headless", isHeadless());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.getBoolean("headless")) {
            loseHead();
        }
    }
}
