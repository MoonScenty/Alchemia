package me.moonscenty.alchemia.wand.spell;

import me.moonscenty.alchemia.registry.ModEntities;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * A splinter of raw vis that follows what it was thrown at.
 *
 * <p>It is the cheapest of the twelve and does the least, and it never misses. That is the whole bargain: the
 * focus will not even go off unless there is something to lock on to, and once it has gone off the thing it was
 * aimed at is going to be hit.
 *
 * <p>It steers rather than turns on the spot, so running round a pillar still works. It just does not work for
 * long.
 */
public class VisShard extends Bolt {
    private static final float HURTS = 1.0F;
    /** How hard it steers towards its mark each tick, and how fast it travels once it is pointed. */
    private static final double STEERS = 0.25;
    private static final double FLIES = 0.6;
    private static final int FADES_AFTER = 100;

    private int chasing = -1;

    public VisShard(EntityType<? extends VisShard> type, Level level) {
        super(type, level);
    }

    public VisShard(Level level, LivingEntity thrower, Entity mark) {
        super(ModEntities.VIS_SHARD.get(), level, thrower);
        chasing = mark.getId();
    }

    /** It is held up by what it is made of, not by its speed. */
    @Override
    protected double getDefaultGravity() {
        return 0.0;
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
        if (mark != null && mark.isAlive()) {
            Vec3 towards = mark.position().add(0.0, mark.getBbHeight() / 2.0, 0.0)
                    .subtract(position()).normalize();
            setDeltaMovement(getDeltaMovement().add(towards.scale(STEERS)).normalize().scale(FLIES));
        }
        super.tick();
    }

    @Override
    protected void land(ServerLevel level, HitResult where) {
        level.sendParticles(ParticleTypes.END_ROD, getX(), getY(), getZ(), 8, 0.1, 0.1, 0.1, 0.05);
        if (where instanceof EntityHitResult struck) {
            struck.getEntity().hurt(damageSources().indirectMagic(this, getOwner()), HURTS);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("chasing", chasing);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        chasing = tag.getInt("chasing");
    }
}
