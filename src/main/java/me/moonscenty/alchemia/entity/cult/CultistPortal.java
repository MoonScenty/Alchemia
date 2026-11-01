package me.moonscenty.alchemia.entity.cult;

import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class CultistPortal extends Monster {
    private static final EntityDataAccessor<Boolean> ACTIVE = SynchedEntityData.defineId(CultistPortal.class,
            EntityDataSerializers.BOOLEAN);
    private static final double WAKES_WITHIN = 32.0;

    private int nextIn = 100;
    // client-side, for rendering
    public int awake;
    public int pulse;

    public CultistPortal(EntityType<? extends CultistPortal> type, Level level) {
        super(type, level);
        xpReward = 10;
    }

    public CultistPortal(Level level) {
        this(ModEntities.CULTIST_PORTAL.get(), level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.ATTACK_DAMAGE, 0.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ACTIVE, false);
    }

    public boolean isActive() {
        return entityData.get(ACTIVE);
    }

    @Override
    public void tick() {
        super.tick();
        if (isActive()) {
            awake++;
        }
        if (pulse > 0) {
            pulse--;
        }
        if (level() instanceof ServerLevel served) {
            think(served);
        }
    }

    protected void think(ServerLevel served) {
        if (!isActive()) {
            if (tickCount % 10 == 0 && level().getNearestPlayer(this, WAKES_WITHIN) != null) {
                entityData.set(ACTIVE, true);
                playSound(ModSounds.CRAFT_START.get(), 1.0F, 1.0F);
            }
        } else if (nextIn-- <= 0) {
            Player near = level().getNearestPlayer(this, WAKES_WITHIN);
            if (near != null && hasLineOfSight(near)) {
                int room = switch (level().getDifficulty()) {
                    case HARD -> 6;
                    case NORMAL -> 4;
                    default -> 2;
                };
                room -= level().getEntitiesOfClass(Cultist.class, getBoundingBox().inflate(WAKES_WITHIN)).size();
                if (room > 0) {
                    level().broadcastEntityEvent(this, (byte) 16);
                    letThrough(served);
                }
            }
            nextIn = 50 + random.nextInt(50);
        }
    }

    protected void activate() {
        entityData.set(ACTIVE, true);
    }

    protected void dieQuietly(DamageSource source) {
        super.die(source);
    }

    public void letThrough(ServerLevel level) {
        bringThrough(level, newCultist(level));
        hurt(damageSources().fellOutOfWorld(), 5 + random.nextInt(5));
    }

    // two knights to each cleric
    protected Cultist newCultist(ServerLevel level) {
        return random.nextFloat() > 0.33F ? new CultistKnight(level) : new CultistCleric(level);
    }

    protected void bringThrough(ServerLevel level, Mob arrival) {
        arrival.moveTo(getX() + random.nextFloat() - random.nextFloat(), getY() + 0.25,
                getZ() + random.nextFloat() - random.nextFloat(), random.nextFloat() * 360.0F, 0.0F);
        arrival.finalizeSpawn(level, level.getCurrentDifficultyAt(arrival.blockPosition()), MobSpawnType.MOB_SUMMONED,
                null);
        settle(arrival);
        level.addFreshEntity(arrival);
        arrival.spawnAnim();
        arrival.playSound(ModSounds.WAND_FAIL.get(), 1.0F, 1.0F);
    }

    protected void settle(Mob arrival) {
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 16) {
            pulse = 10;
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void playerTouch(Player player) {
        if (distanceToSqr(player) < 3.0 && player.hurt(damageSources().indirectMagic(this, this), 4.0F)) {
            playSound(ModSounds.ZAP.get(), 1.0F, (random.nextFloat() - random.nextFloat()) * 0.1F + 1.0F);
        }
    }

    @Override
    public void die(DamageSource source) {
        if (!level().isClientSide) {
            level().explode(this, getX(), getY(), getZ(), 1.5F, false, Level.ExplosionInteraction.NONE);
        }
        super.die(source);
    }

    @Override
    public void move(MoverType type, Vec3 by) {
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return false;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void aiStep() {
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public float getLightLevelDependentMagicValue() {
        return 1.0F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.MONOLITH.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 540;
    }

    @Override
    protected float getSoundVolume() {
        return 0.75F;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.ZAP.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SHOCK.get();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Active", isActive());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(ACTIVE, tag.getBoolean("Active"));
    }

    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level, MobSpawnType reason) {
        return level.getDifficulty() != Difficulty.PEACEFUL;
    }
}
