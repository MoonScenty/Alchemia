package me.moonscenty.alchemia.entity.boss;

import java.util.List;

import me.moonscenty.alchemia.entity.ArmLifting;
import me.moonscenty.alchemia.entity.ChampionModifier;
import me.moonscenty.alchemia.entity.EldritchGuardian;
import me.moonscenty.alchemia.entity.EldritchMob;
import me.moonscenty.alchemia.entity.EldritchOrb;
import me.moonscenty.alchemia.entity.cult.Cultist;
import me.moonscenty.alchemia.player.WarpData;
import me.moonscenty.alchemia.player.WarpHandler;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraft.world.level.block.Block;

/**
 * The Eldritch Warden, keeper of a great hall: a guardian grown huge, with one of fifteen old names.
 * <p>
 * It rises out of the floor over seven and a half seconds in a swirl of smoke. A shield two thirds of its health
 * deep stands over it and comes back a point every second and a quarter when it is not being hit. Further off than
 * three blocks it throws dark orbs, from alternate hands -- or one time in five, if it can see its mark, screeches:
 * a blast that throws the mark back, withers and weakens it for twenty seconds, and leaves warp in a player's mind.
 * Close up it strikes. Where it walks it leaves sapping.
 * <p>
 * The first time a blow finds it with its shield down, it goes into a frenzy: seven and a half seconds untouchable
 * and still, in which it goes home and lays ring after widening ring of sapping round itself, each gone again in
 * thirteen to twenty seconds. Drowning and withering do nothing to it.
 */
public class EldritchWarden extends ThaumcraftBoss implements RangedAttackMob, EldritchMob, ArmLifting {
    private static final EntityDataAccessor<Byte> TITLE = SynchedEntityData.defineId(EldritchWarden.class,
            EntityDataSerializers.BYTE);
    public static final List<String> TITLES = List.of("Aphoom-Zhah", "Basatan", "Chaugnar Faugn", "Mnomquah",
            "Nyogtha", "Oorn", "Shaikorth", "Rhan-Tegoth", "Rhogog", "Shudde M'ell", "Vulthoom", "Yag-Kosha",
            "Yibb-Tstll", "Zathog", "Zushakon");
    public static final int RISING = 150;
    private static final byte LEFT = 15;
    private static final byte RIGHT = 16;
    private static final byte BOTH = 17;

    private boolean frenzied;
    private int frenzy;
    private boolean lastRight;
    public float armLiftL;
    public float armLiftR;

    public EldritchWarden(EntityType<? extends EldritchWarden> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return bossAttributes()
                .add(Attributes.MAX_HEALTH, 400.0)
                .add(Attributes.MOVEMENT_SPEED, 0.33)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.MAX_ABSORPTION, 264.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new LongRangeAttackGoal(this, 3.0, 1.0, 20, 40, 24.0F));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.1, false));
        goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 0.8));
        goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Cultist.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TITLE, (byte) 0);
    }

    @Override
    public Component title(ChampionModifier modifier) {
        return Component.translatable("entity.alchemia.eldritch_warden.titled",
                TITLES.get(entityData.get(TITLE) % TITLES.size()), Component.translatable(modifier.translationKey()));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            SpawnGroupData data) {
        setSpawnTimer(RISING);
        entityData.set(TITLE, (byte) random.nextInt(TITLES.size()));
        setAbsorptionAmount(getAbsorptionAmount() + shield());
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    private float shield() {
        return (float) (getAttribute(Attributes.MAX_HEALTH).getBaseValue() * 0.66);
    }

    @Override
    protected boolean frozen() {
        return super.frozen() || frenzy > 0;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (hurtTime <= 0 && invulnerableTime <= 0 && tickCount % 25 == 0 && getAbsorptionAmount() < (int) shield()) {
            setAbsorptionAmount(getAbsorptionAmount() + 1);
        }
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !(target instanceof EldritchGuardian) && super.canAttack(target);
    }

    @Override
    public float leftArmLift() {
        return armLiftL;
    }

    @Override
    public float rightArmLift() {
        return armLiftR;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            if (armLiftL > 0) {
                armLiftL -= 0.05F;
            }
            if (armLiftR > 0) {
                armLiftR -= 0.05F;
            }
            me.moonscenty.alchemia.client.EldritchEffects.wardenWisp(this);
            if (spawnTimer() > 0) {
                me.moonscenty.alchemia.client.EldritchEffects.wardenRise(this);
            }
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            return;
        }
        for (int a = 0; a < 4; a++) {
            BlockPos at = BlockPos.containing(getX() + ((a % 2) * 2 - 1) * 0.25, getY(),
                    getZ() + (((a / 2) % 2) * 2 - 1) * 0.25);
            if (level().isEmptyBlock(at)) {
                level().setBlock(at, ModBlocks.SAPPING.get().defaultBlockState(), Block.UPDATE_ALL);
            }
        }
        if (frenzy > 0) {
            if (frenzy == RISING) {
                goHome();
            }
            rings();
        }
    }

    /** One of the frenzy's rings, every half second, each wider and finer than the last. */
    private void rings() {
        if (frenzy < 121 && frenzy % 10 == 0) {
            level().broadcastEntityEvent(this, BOTH);
            double radius = (RISING - frenzy) / 8.0;
            int step = 1 + frenzy / 8;
            int x = Mth.floor(getX());
            int y = Mth.floor(getY());
            int z = Mth.floor(getZ());
            for (int q = 0; q < 180 / step; q++) {
                double angle = Math.toRadians(q * 2 * step);
                BlockPos at = new BlockPos(x + (int) (radius * Math.cos(angle)), y, z + (int) (radius * Math.sin(angle)));
                if (level().isEmptyBlock(at) && level().getBlockState(at.below()).isRedstoneConductor(level(), at.below())) {
                    level().setBlock(at, ModBlocks.SAPPING.get().defaultBlockState(), Block.UPDATE_ALL);
                    level().scheduleTick(at, ModBlocks.SAPPING.get(), 250 + random.nextInt(150));
                }
            }
            level().playSound(null, getX(), getY(), getZ(), ModSounds.ZAP.get(), SoundSource.HOSTILE, 1.0F,
                    0.9F + random.nextFloat() * 0.1F);
        }
        frenzy--;
    }

    /** Back to the middle of its hall, as an enderman goes, if there is room there. */
    private void goHome() {
        if (!hasRestriction()) {
            return;
        }
        BlockPos home = getRestrictCenter();
        double ox = getX();
        double oy = getY();
        double oz = getZ();
        if (randomTeleport(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, true)) {
            level().playSound(null, ox, oy, oz, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 1.0F);
            playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        }
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return frenzy > 0 && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || super.isInvulnerableTo(source);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source) || source.is(DamageTypes.DROWN) || source.is(DamageTypes.WITHER)) {
            return false;
        }
        boolean hurt = super.hurt(source, amount);
        if (!level().isClientSide && hurt && !frenzied && getAbsorptionAmount() <= 0.0F) {
            frenzied = true;
            frenzy = RISING;
        }
        return hurt;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (random.nextFloat() > 0.2F) {
            EldritchOrb orb = new EldritchOrb(level(), this);
            lastRight = !lastRight;
            level().broadcastEntityEvent(this, lastRight ? RIGHT : LEFT);
            int turn = lastRight ? 90 : 180;
            double xx = Mth.cos((getYRot() + turn) % 360.0F / 180.0F * Mth.PI) * 0.5;
            double zz = Mth.sin((getYRot() + turn) % 360.0F / 180.0F * Mth.PI) * 0.5;
            orb.setPos(orb.getX() - xx, orb.getY() - 0.13, orb.getZ() - zz);
            double dx = target.getX() + target.getDeltaMovement().x - getX();
            double dy = target.getY() - getY() - target.getBbHeight() / 2.0;
            double dz = target.getZ() + target.getDeltaMovement().z - getZ();
            orb.shoot(dx, dy, dz, 1.0F, 2.0F);
            playSound(ModSounds.EG_ATTACK.get(), 2.0F, 1.0F + random.nextFloat() * 0.1F);
            level().addFreshEntity(orb);
        } else if (getSensing().hasLineOfSight(target)) {
            level().broadcastEntityEvent(this, (byte) 19);
            target.push(-Mth.sin(getYRot() * Mth.DEG_TO_RAD) * 1.5, 0.1, Mth.cos(getYRot() * Mth.DEG_TO_RAD) * 1.5);
            target.hurtMarked = true;
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 400, 0), this);
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 400, 0), this);
            if (target instanceof Player player) {
                WarpHandler.add(player, WarpData.Kind.TEMPORARY, 3 + random.nextInt(3));
            }
            playSound(ModSounds.EG_SCREECH.get(), 4.0F, 1.0F + random.nextFloat() * 0.1F);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case LEFT -> armLiftL = 0.5F;
            case RIGHT -> armLiftR = 0.5F;
            case BOTH -> {
                armLiftL = 0.9F;
                armLiftR = 0.9F;
            }
            case 19 -> me.moonscenty.alchemia.client.EldritchEffects.screech(this);
            default -> super.handleEntityEvent(id);
        }
    }

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
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("title", entityData.get(TITLE));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(TITLE, tag.getByte("title"));
    }
}
