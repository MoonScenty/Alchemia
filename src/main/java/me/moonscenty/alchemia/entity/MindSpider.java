package me.moonscenty.alchemia.entity;

import me.moonscenty.alchemia.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A mind spider: a little ghost of a spider, nearly all eyes. Real ones bite, if only for a point.
 * <p>
 * Most are not real. Warp shows one player a swarm of them that nobody else can see, that bite for nothing, are
 * worth nothing, and are gone in a minute; the original forgot the minute when the world was saved, and they stayed.
 * Here they go when their minute is up whatever happens in between.
 */
public class MindSpider extends Spider implements EldritchMob {
    private static final EntityDataAccessor<Boolean> HARMLESS = SynchedEntityData.defineId(MindSpider.class,
            EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> VIEWER = SynchedEntityData.defineId(MindSpider.class,
            EntityDataSerializers.STRING);
    /** How long a harmless one lasts. */
    private static final int ILLUSION_LASTS = 1200;

    private int lifeSpan = Integer.MAX_VALUE;

    public MindSpider(EntityType<? extends MindSpider> type, Level level) {
        super(type, level);
        xpReward = 1;
    }

    public MindSpider(Level level) {
        this(ModEntities.MIND_SPIDER.get(), level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Spider.createAttributes()
                .add(Attributes.MAX_HEALTH, 1.0)
                .add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HARMLESS, false);
        builder.define(VIEWER, "");
    }

    public boolean isHarmless() {
        return entityData.get(HARMLESS);
    }

    /** Makes it one of warp's illusions: harmless, worthless, and gone in a minute. */
    public void setHarmless(boolean harmless) {
        if (harmless) {
            lifeSpan = ILLUSION_LASTS;
        }
        entityData.set(HARMLESS, harmless);
    }

    /** Who alone can see it, or nobody in particular. */
    public String viewer() {
        return entityData.get(VIEWER);
    }

    public void setViewer(String name) {
        entityData.set(VIEWER, name);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && tickCount > lifeSpan) {
            discard();
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        return !isHarmless() && super.doHurtTarget(target);
    }

    @Override
    protected int getBaseExperienceReward() {
        return isHarmless() ? 0 : super.getBaseExperienceReward();
    }

    @Override
    public float getVoicePitch() {
        return 0.7F;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    /** None of a spider's riders or gifts. */
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            SpawnGroupData data) {
        return data;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, net.minecraft.world.damagesource.DamageSource source,
            boolean recentlyHit) {
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("harmless", isHarmless());
        tag.putString("viewer", viewer());
        if (lifeSpan != Integer.MAX_VALUE) {
            tag.putInt("LifeLeft", lifeSpan - tickCount);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(HARMLESS, tag.getBoolean("harmless"));
        setViewer(tag.getString("viewer"));
        if (tag.contains("LifeLeft")) {
            lifeSpan = tickCount + tag.getInt("LifeLeft");
        }
    }
}
