package me.moonscenty.alchemia.entity;

import me.moonscenty.alchemia.entity.cult.Cultist;
import me.moonscenty.alchemia.entity.cult.CultistKnight;
import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

/**
 * An eldritch crab: a scuttling thing the size of a dog that lives in husks and comes out when they die.
 *
 * <h3>Its shell</h3>
 * One out of a husk, and a third of the rest (all of them on hard), wear a shell of crimson plate on the tail worth
 * five points of armour. At half health the shell breaks off.
 *
 * <h3>Its grip</h3>
 * A bare crab that leaps at whatever last hurt it and lands high enough on it -- half its height up and within two
 * blocks -- takes hold of its head and rides there, biting every half to one second. Each bite there is one chance in
 * five it lets go.
 *
 * <h3>Where this differs from the original</h3>
 * The original's crab meant to let go one bite in five as well, but the call it made only shuffled it about on the
 * head, so in practice it never let go. Here it does. The claws snap smoothly rather than a step a tick.
 */
public class EldritchCrab extends Monster implements EldritchMob {
    private static final EntityDataAccessor<Byte> FLAGS = SynchedEntityData.defineId(EldritchCrab.class,
            EntityDataSerializers.BYTE);
    private static final int HELM = 1;

    private int biteIn;

    public EldritchCrab(EntityType<? extends EldritchCrab> type, Level level) {
        super(type, level);
        xpReward = 6;
    }

    public EldritchCrab(Level level) {
        this(ModEntities.ELDRITCH_CRAB.get(), level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FLAGS, (byte) 0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.63F));
        goalSelector.addGoal(3, new CultistKnight.QuickMelee(this));
        goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Cultist.class, true));
    }

    // --- its shell -----------------------------------------------------------

    public boolean hasHelm() {
        return (entityData.get(FLAGS) & HELM) != 0;
    }

    public void setHelm(boolean helm) {
        byte flags = entityData.get(FLAGS);
        entityData.set(FLAGS, (byte) (helm ? flags | HELM : flags & ~HELM));
    }

    @Override
    public int getArmorValue() {
        return hasHelm() ? 5 : 0;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (!level().isClientSide && hasHelm() && getHealth() / getMaxHealth() <= 0.5F) {
            setHelm(false);
            ItemStack shell = new ItemStack(ModItems.CRIMSON.get("crimson_plate_chest").get());
            playSound(SoundEvents.ITEM_BREAK, 0.8F, 0.8F + random.nextFloat() * 0.4F);
            if (level() instanceof ServerLevel served) {
                served.sendParticles(new net.minecraft.core.particles.ItemParticleOption(
                        net.minecraft.core.particles.ParticleTypes.ITEM, shell), getX(), getY() + 0.3, getZ(), 5, 0.2,
                        0.1, 0.2, 0.1);
            }
        }
        return hurt;
    }

    // --- its grip ------------------------------------------------------------

    @Override
    public void tick() {
        super.tick();
        biteIn--;
        if (tickCount < 20) {
            fallDistance = 0.0F;
        }
        if (level().isClientSide) {
            return;
        }
        LivingEntity hurtMe = getLastHurtByMob();
        if (getVehicle() == null && hurtMe != null && !hurtMe.isVehicle() && !onGround() && !hasHelm()
                && hurtMe.isAlive() && getY() - hurtMe.getY() >= hurtMe.getBbHeight() / 2.0F
                && distanceToSqr(hurtMe) < 4.0) {
            startRiding(hurtMe, true);
        }
        if (getVehicle() != null && biteIn <= 0) {
            biteIn = 10 + random.nextInt(10);
            doHurtTarget(getVehicle());
            if (getVehicle() != null && random.nextFloat() < 0.2F) {
                stopRiding();
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            playSound(ModSounds.CRAB_CLAW.get(), 1.0F, 0.9F + random.nextFloat() * 0.2F);
            return true;
        }
        return false;
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        return other instanceof EldritchCrab || super.isAlliedTo(other);
    }

    @Override
    public boolean canPickUpLoot() {
        return false;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return !effect.is(MobEffects.POISON) && super.canBeAffected(effect);
    }

    // --- coming into the world -----------------------------------------------

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            SpawnGroupData data) {
        boolean hard = level.getDifficulty() == Difficulty.HARD;
        setHelm(hard || random.nextFloat() < 0.33F);
        // a spider's gift now and then, as the original copied it
        if (hard && random.nextFloat() < 0.1F * difficulty.getSpecialMultiplier()) {
            int pick = random.nextInt(5);
            Holder<MobEffect> gift = pick <= 1 ? MobEffects.MOVEMENT_SPEED : pick == 2 ? MobEffects.DAMAGE_BOOST
                    : pick == 3 ? MobEffects.REGENERATION : MobEffects.INVISIBILITY;
            addEffect(new MobEffectInstance(gift, -1));
        }
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    // --- what it leaves ------------------------------------------------------

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        int looting = 0;
        if (source.getEntity() instanceof LivingEntity killer) {
            looting = EnchantmentHelper.getEnchantmentLevel(level.registryAccess()
                    .registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.LOOTING), killer);
        }
        if (recentlyHit && (random.nextInt(3) == 0 || random.nextInt(1 + looting) > 0)) {
            spawnAtLocation(Items.ENDER_PEARL);
        }
    }

    // --- its voice -----------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.CRAB_TALK.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.HOSTILE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CRAB_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.SPIDER_STEP, 0.15F, 1.0F);
    }

    // --- saving --------------------------------------------------------------

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
