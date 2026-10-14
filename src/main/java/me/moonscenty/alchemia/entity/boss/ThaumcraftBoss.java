package me.moonscenty.alchemia.entity.boss;

import java.util.HashMap;
import java.util.Map;

import me.moonscenty.alchemia.entity.ChampionModifier;
import me.moonscenty.alchemia.entity.Champions;
import me.moonscenty.alchemia.entity.EldritchMob;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/**
 * What the Outer Lands' great halls hold, and what they share: the Warden, the Construct and the Praetor.
 * <p>
 * Each is a champion of something whenever it comes into the world, and is named for it; it does not get a
 * champion's extra health or blows, only what the kind of champion adds. It wakes slowly -- untouchable and still
 * while it does -- mends a point every second and a half, and never despawns, drowns or sticks in a web. No blow
 * does it more than thirty-five; one that tries makes it angry for ten seconds, which mends it, strengthens it and
 * speeds it by how hard it was hit, and it tells whoever did it.
 * <p>
 * It keeps count of who has hurt it how much and turns on whoever has hurt it most, once that is twenty-five more
 * and a tenth more than what it is fighting now. For every player past the first among those it is fighting it
 * has fifty more health and half a point more to its blows, up to five more players. (The original meant all this
 * and, by two slips, almost never did it; here it does.)
 * <p>
 * It dies leaving one or two primordial pearls and a bag of rare loot.
 */
public abstract class ThaumcraftBoss extends Monster {
    private static final EntityDataAccessor<Integer> ANGER = SynchedEntityData.defineId(ThaumcraftBoss.class,
            EntityDataSerializers.INT);
    public static final float MOST_A_BLOW_DOES = 35.0F;
    private static final ResourceLocation MORE_HEALTH = me.moonscenty.alchemia.Alchemia.id("boss_health");
    private static final ResourceLocation MORE_DAMAGE = me.moonscenty.alchemia.Alchemia.id("boss_damage");

    private final Map<Integer, Integer> aggro = new HashMap<>();
    /** While above nought it is waking: untouchable, still, and not to be pushed. Seen on both sides. */
    private static final EntityDataAccessor<Integer> WAKING = SynchedEntityData.defineId(ThaumcraftBoss.class,
            EntityDataSerializers.INT);
    private final ServerBossEvent bar = new ServerBossEvent(Component.empty(), BossEvent.BossBarColor.PURPLE,
            BossEvent.BossBarOverlay.PROGRESS);

    protected ThaumcraftBoss(EntityType<? extends ThaumcraftBoss> type, Level level) {
        super(type, level);
        xpReward = 50;
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder bossAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.95)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANGER, 0);
        builder.define(WAKING, 0);
    }

    public int anger() {
        return entityData.get(ANGER);
    }

    public void setAnger(int anger) {
        entityData.set(ANGER, anger);
    }

    public int spawnTimer() {
        return entityData.get(WAKING);
    }

    public void setSpawnTimer(int ticks) {
        entityData.set(WAKING, ticks);
    }

    /** What it is called: set once it is known what it is a champion of. */
    public abstract Component title(ChampionModifier modifier);

    public void name() {
        ChampionModifier modifier = Champions.modifier(this);
        if (modifier != null) {
            setCustomName(title(modifier));
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            SpawnGroupData data) {
        restrictTo(blockPosition(), 24);
        name();
        return data;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (getTarget() != null && !getTarget().isAlive()) {
            setTarget(null);
        }
    }

    /** Whether it is too busy with something of its own to think: waking, here; the Warden's frenzy too. */
    protected boolean frozen() {
        return spawnTimer() > 0;
    }

    @Override
    public void aiStep() {
        boolean frozen = frozen();
        for (net.minecraft.world.entity.ai.goal.Goal.Flag flag : net.minecraft.world.entity.ai.goal.Goal.Flag.values()) {
            goalSelector.setControlFlag(flag, !frozen);
            targetSelector.setControlFlag(flag, !frozen);
        }
        if (frozen) {
            setDeltaMovement(getDeltaMovement().multiply(0.0, 1.0, 0.0));
            getNavigation().stop();
        }
        super.aiStep();
    }

    @Override
    public void tick() {
        super.tick();
        if (spawnTimer() > 0 && !level().isClientSide) {
            setSpawnTimer(spawnTimer() - 1);
        }
        if (anger() > 0 && !level().isClientSide) {
            setAnger(anger() - 1);
        }
        if (level().isClientSide) {
            if (random.nextInt(15) == 0 && anger() > 0) {
                level().addParticle(ParticleTypes.ANGRY_VILLAGER,
                        getX() + random.nextFloat() * getBbWidth() - getBbWidth() / 2.0,
                        getBoundingBox().minY + getBbHeight() + random.nextFloat() * 0.5,
                        getZ() + random.nextFloat() * getBbWidth() - getBbWidth() / 2.0,
                        random.nextGaussian() * 0.02, random.nextGaussian() * 0.02, random.nextGaussian() * 0.02);
            }
            return;
        }
        if (tickCount % 30 == 0) {
            heal(1.0F);
        }
        if (getTarget() != null && tickCount % 20 == 0) {
            reckon();
        }
        bar.setName(getDisplayName());
        bar.setProgress(getHealth() / getMaxHealth());
    }

    /** Turns on whoever has hurt it most, and grows with how many are fighting it. */
    private void reckon() {
        LivingEntity target = getTarget();
        int current = aggro.getOrDefault(target.getId(), 0);
        int best = current;
        LivingEntity chosen = null;
        int players = 0;
        for (var it = aggro.entrySet().iterator(); it.hasNext();) {
            var entry = it.next();
            Entity other = level().getEntity(entry.getKey());
            if (other == null || !other.isAlive() || distanceToSqr(other) > 16384.0) {
                it.remove();
                continue;
            }
            if (other instanceof Player) {
                players++;
            }
            int dealt = entry.getValue();
            if (dealt > current + 25 && dealt > current * 1.1 && dealt > best && other instanceof LivingEntity living) {
                best = dealt;
                chosen = living;
            }
        }
        if (chosen != null && chosen != target) {
            setTarget(chosen);
        }
        float before = getMaxHealth();
        int extra = Math.min(5, players - 1);
        scale(Attributes.MAX_HEALTH, MORE_HEALTH, 50.0 * Math.max(0, extra));
        scale(Attributes.ATTACK_DAMAGE, MORE_DAMAGE, 0.5 * Math.max(0, extra));
        setHealth(getHealth() * getMaxHealth() / before);
    }

    private void scale(net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
            ResourceLocation id, double amount) {
        AttributeInstance instance = getAttribute(attribute);
        if (instance == null) {
            return;
        }
        instance.removeModifier(id);
        if (amount > 0) {
            instance.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return spawnTimer() > 0 && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)
                || super.isInvulnerableTo(source);
    }

    @Override
    public boolean isPushable() {
        return spawnTimer() <= 0 && super.isPushable();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!level().isClientSide) {
            if (source.getEntity() instanceof LivingEntity attacker) {
                aggro.merge(attacker.getId(), (int) amount, Integer::sum);
            }
            if (amount > MOST_A_BLOW_DOES) {
                if (anger() == 0) {
                    enrage(amount, 10.0F);
                    if (source.getEntity() instanceof Player player) {
                        player.sendSystemMessage(Component.translatable("entity.alchemia.boss.enraged",
                                getDisplayName()));
                    }
                }
                amount = MOST_A_BLOW_DOES;
            }
        }
        return super.hurt(source, amount);
    }

    /** Angry for ten seconds: mended, stronger and quicker by how hard the blow was. */
    protected void enrage(float amount, float strengthBy) {
        addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, (int) (amount / 15.0F)));
        addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, (int) (amount / strengthBy)));
        addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, (int) (amount / 40.0F)));
        setAnger(200);
    }

    @Override
    protected int decreaseAirSupply(int air) {
        return air;
    }

    @Override
    public void makeStuckInBlock(BlockState state, net.minecraft.world.phys.Vec3 multiplier) {
    }

    @Override
    public boolean canPickUpLoot() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        return other instanceof EldritchMob || super.isAlliedTo(other);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        dropPearls(this);
        ItemEntity bag = spawnAtLocation(new ItemStack(ModItems.LOOT_BAG_RARE.get()), 1.5F);
        if (bag != null) {
            bag.setUnlimitedLifetime();
        }
    }

    /** One or two primordial pearls, floating where it fell. */
    public static void dropPearls(LivingEntity from) {
        ItemEntity pearls = new ItemEntity(from.level(), from.getX(), from.getY() + from.getBbHeight() / 2.0,
                from.getZ(), new ItemStack(ModItems.PRIMORDIAL_PEARL.get(), 1 + from.getRandom().nextInt(2)));
        pearls.setDeltaMovement(0.0, 0.1, 0.0);
        pearls.setNoGravity(true);
        pearls.setUnlimitedLifetime();
        pearls.setDefaultPickUpDelay();
        from.level().addFreshEntity(pearls);
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

    @Override
    public void setCustomName(Component name) {
        super.setCustomName(name);
        bar.setName(getDisplayName());
    }

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
        if (hasCustomName()) {
            bar.setName(getDisplayName());
        }
    }
}
