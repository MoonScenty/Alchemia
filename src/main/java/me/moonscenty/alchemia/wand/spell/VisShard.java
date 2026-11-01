package me.moonscenty.alchemia.wand.spell;

import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class VisShard extends Bolt {
    private static final float HURTS = 1.0F;
    private static final double STEERS = 0.25;
    private static final double FLIES = 0.6;
    private static final int FADES_AFTER = 100;

    private static final double LOOKS = 16.0;

    private float hurts = HURTS;
    private boolean persistent;
    private int chasing = -1;

    public VisShard(EntityType<? extends VisShard> type, Level level) {
        super(type, level);
    }

    public VisShard(Level level, LivingEntity thrower, Entity mark) {
        super(ModEntities.VIS_SHARD.get(), level, thrower);
        chasing = mark.getId();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    public VisShard chasing(float hurts, boolean persistent) {
        this.hurts = hurts;
        this.persistent = persistent;
        return this;
    }

    @Override
    protected ParticleOptions trail() {
        return ParticleTypes.END_ROD;
    }

    @Override
    protected int lives() {
        return FADES_AFTER;
    }

    @Override
    public void tick() {
        Entity mark = level().getEntity(chasing);
        if (persistent && (mark == null || !mark.isAlive())) {
            mark = somethingElse();
        }
        if (mark != null && mark.isAlive()) {
            Vec3 towards = mark.position().add(0.0, mark.getBbHeight() / 2.0, 0.0)
                    .subtract(position()).normalize();
            setDeltaMovement(getDeltaMovement().add(towards.scale(STEERS)).normalize().scale(FLIES));
        }
        super.tick();
    }

    private Entity somethingElse() {
        Entity thrower = getOwner();
        LivingEntity found = level().getNearestEntity(LivingEntity.class,
                net.minecraft.world.entity.ai.targeting.TargetingConditions.forNonCombat().range(LOOKS),
                null, getX(), getY(), getZ(),
                new net.minecraft.world.phys.AABB(position(), position()).inflate(LOOKS));
        if (found != null && found != thrower) {
            chasing = found.getId();
            return found;
        }
        return null;
    }

    @Override
    protected void land(ServerLevel level, HitResult where) {
        level.sendParticles(ParticleTypes.END_ROD, getX(), getY(), getZ(), 8, 0.1, 0.1, 0.1, 0.05);
        if (where instanceof EntityHitResult struck) {
            struck.getEntity().hurt(damageSources().indirectMagic(this, getOwner()), hurts);
            if (struck.getEntity() instanceof LivingEntity) {
                level.playSound(null, getX(), getY(), getZ(), ModSounds.ZAP.get(), SoundSource.PLAYERS, 1.0F,
                        1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F);
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("chasing", chasing);
        tag.putFloat("hurts", hurts);
        tag.putBoolean("persistent", persistent);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        chasing = tag.getInt("chasing");
        hurts = tag.contains("hurts") ? tag.getFloat("hurts") : HURTS;
        persistent = tag.getBoolean("persistent");
    }
}
