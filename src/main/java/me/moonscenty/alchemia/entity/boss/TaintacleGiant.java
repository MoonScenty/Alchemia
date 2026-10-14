package me.moonscenty.alchemia.entity.boss;

import me.moonscenty.alchemia.entity.Champions;
import me.moonscenty.alchemia.entity.EldritchMob;
import me.moonscenty.alchemia.entity.Taintacle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;

/**
 * A giant taintacle: six blocks of tentacle out of the floor of a great hall, a champion every one.
 * <p>
 * Like any taintacle it is rooted, starves off taint and sends tendrils up under what it cannot reach -- in the
 * Outer Lands, wherever that is. Like the Outer Lands' bosses no blow does it more than thirty-five, one that tries
 * angers it, and it mends a point every second and a half. The last of them in a hall leaves one or two primordial
 * pearls.
 */
public class TaintacleGiant extends Taintacle implements EldritchMob {
    private static final EntityDataAccessor<Integer> ANGER = SynchedEntityData.defineId(TaintacleGiant.class,
            EntityDataSerializers.INT);
    private final ServerBossEvent bar = new ServerBossEvent(Component.empty(), BossEvent.BossBarColor.PURPLE,
            BossEvent.BossBarOverlay.PROGRESS);

    public TaintacleGiant(EntityType<? extends TaintacleGiant> type, Level level) {
        super(type, level);
        xpReward = 20;
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder attributes() {
        return Taintacle.attributes()
                .add(Attributes.MAX_HEALTH, 175.0)
                .add(Attributes.ATTACK_DAMAGE, 9.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANGER, 0);
    }

    @Override
    protected double reach() {
        return 2.5 + random.nextFloat();
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            SpawnGroupData data) {
        Champions.makeRandom(this);
        return data;
    }

    @Override
    public void tick() {
        super.tick();
        int anger = entityData.get(ANGER);
        if (level().isClientSide) {
            if (random.nextInt(15) == 0 && anger > 0) {
                level().addParticle(ParticleTypes.ANGRY_VILLAGER,
                        getX() + random.nextFloat() * getBbWidth() - getBbWidth() / 2.0,
                        getBoundingBox().minY + getBbHeight() + random.nextFloat() * 0.5,
                        getZ() + random.nextFloat() * getBbWidth() - getBbWidth() / 2.0, 0.0, 0.0, 0.0);
            }
            return;
        }
        if (anger > 0) {
            entityData.set(ANGER, anger - 1);
        }
        if (tickCount % 30 == 0) {
            heal(1.0F);
        }
        bar.setName(getDisplayName());
        bar.setProgress(getHealth() / getMaxHealth());
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!level().isClientSide && amount > ThaumcraftBoss.MOST_A_BLOW_DOES) {
            if (entityData.get(ANGER) == 0) {
                addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, (int) (amount / 15.0F)));
                addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, (int) (amount / 40.0F)));
                addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, (int) (amount / 40.0F)));
                entityData.set(ANGER, 200);
                if (source.getEntity() instanceof Player player) {
                    player.sendSystemMessage(Component.translatable("entity.alchemia.boss.enraged",
                            getDisplayName()));
                }
            }
            amount = ThaumcraftBoss.MOST_A_BLOW_DOES;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        if (level.getEntitiesOfClass(TaintacleGiant.class, new AABB(blockPosition()).inflate(48.0),
                other -> other != this && other.isAlive()).isEmpty()) {
            ThaumcraftBoss.dropPearls(this);
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bar.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bar.removePlayer(player);
    }
}
