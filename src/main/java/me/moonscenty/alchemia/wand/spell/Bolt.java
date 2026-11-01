package me.moonscenty.alchemia.wand.spell;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public abstract class Bolt extends ThrowableProjectile {
    protected Bolt(EntityType<? extends Bolt> type, Level level) {
        super(type, level);
    }

    protected Bolt(EntityType<? extends Bolt> type, Level level, LivingEntity thrower) {
        super(type, thrower, level);
    }

    protected abstract ParticleOptions trail();

    protected abstract int lives();

    protected abstract void land(ServerLevel level, HitResult where);

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            level().addParticle(trail(), getX(), getY(), getZ(), 0.0, 0.0, 0.0);
        } else if (tickCount > lives()) {
            discard();
        }
    }

    @Override
    protected void onHit(HitResult where) {
        super.onHit(where);
        lands(where);
    }

    public void lands(HitResult where) {
        if (level() instanceof ServerLevel served) {
            land(served, where);
            discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity other) {
        return super.canHitEntity(other) && other != getOwner();
    }
}
