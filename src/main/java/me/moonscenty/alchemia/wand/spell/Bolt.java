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

/**
 * Something a wand throws: it flies, it leaves a trail, it lands, and it is gone.
 *
 * <p>Four of the twelve foci work this way and differ only in what they are made of and what happens where they
 * land. What is here is everything they have in common, which is more of each of them than what is not.
 *
 * <p>None of them is drawn. The original gave each its own small model; here each is its own trail of particles
 * and nothing else, which is also what each of them mostly looked like.
 */
public abstract class Bolt extends ThrowableProjectile {
    protected Bolt(EntityType<? extends Bolt> type, Level level) {
        super(type, level);
    }

    protected Bolt(EntityType<? extends Bolt> type, Level level, LivingEntity thrower) {
        super(type, thrower, level);
    }

    /** What it is made of, as the thing that is seen going past. */
    protected abstract ParticleOptions trail();

    /** How long it stays in the air before it gives up, in ticks. */
    protected abstract int lives();

    /** What it does where it lands. It is discarded straight after, whatever this does. */
    protected abstract void land(ServerLevel level, HitResult where);

    /** Nothing about any of these is worth telling the client beyond where it is. */
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

    /** Landing, which anything may set off and a test may watch. */
    public void lands(HitResult where) {
        if (level() instanceof ServerLevel served) {
            land(served, where);
            discard();
        }
    }

    /** It goes past whoever threw it, which is the only thing in the world it is not looking for. */
    @Override
    protected boolean canHitEntity(Entity other) {
        return super.canHitEntity(other) && other != getOwner();
    }
}
