package me.moonscenty.alchemia.entity;

import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;

/**
 * Thrown alumentum, the original's {@code EntityAlumentum}: a lump of alchemical fire lobbed by hand or from a
 * dispenser, trailing dark smoke and sparks, that goes off where it lands with a little more force than a creeper's
 * worth of fire. Whether it breaks blocks is the mob griefing rule's say.
 */
public class ThrownAlumentum extends ThrowableItemProjectile {
    /** How hard it goes off. The original's. */
    public static final float BLAST = 1.66F;

    public ThrownAlumentum(EntityType<? extends ThrownAlumentum> type, Level level) {
        super(type, level);
    }

    public ThrownAlumentum(Level level, LivingEntity thrower) {
        super(ModEntities.ALUMENTUM.get(), thrower, level);
    }

    public ThrownAlumentum(Level level, double x, double y, double z) {
        super(ModEntities.ALUMENTUM.get(), x, y, z, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.ALUMENTUM.get();
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            for (int a = 0; a < 3; a++) {
                level().addParticle(ParticleTypes.SMOKE, getX() + spread(0.3), getY() + spread(0.3),
                        getZ() + spread(0.3), 0.0, 0.0, 0.0);
                level().addParticle(ParticleTypes.SMOKE, (getX() + xo) / 2.0 + spread(0.3),
                        (getY() + yo) / 2.0 + spread(0.3), (getZ() + zo) / 2.0 + spread(0.3), 0.0, 0.0, 0.0);
                level().addParticle(ParticleTypes.END_ROD, getX() + spread(0.1), getY() + spread(0.1),
                        getZ() + spread(0.1), 0.0, 0.0, 0.0);
            }
        }
    }

    private double spread(double by) {
        return (random.nextFloat() - random.nextFloat()) * by;
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!level().isClientSide) {
            level().explode(null, getX(), getY(), getZ(), BLAST, Level.ExplosionInteraction.MOB);
            discard();
        }
    }
}
