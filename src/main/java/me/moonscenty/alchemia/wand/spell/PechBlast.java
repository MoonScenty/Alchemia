package me.moonscenty.alchemia.wand.spell;

import java.util.List;

import me.moonscenty.alchemia.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * A pech's curse: a thrown spite that bursts where it lands and leaves whoever was standing there the worse for it.
 *
 * <p>One of three afflictions at random -- poison, a dragging slowness, or a weakness in the arms. The original
 * picked one each time it burst rather than laying on all three; a focus named after somebody's curse ought to
 * be a nuisance and not a sentence.
 *
 * <p>It is not aimed at anything in particular. Whatever is within two blocks of the burst catches it, and so a
 * crowd is worth more than a duel.
 */
public class PechBlast extends ThrowableProjectile {
    /** How far from the burst the curse reaches. */
    private static final double REACHES = 2.0;
    /** What the burst itself does, before anything settles on anybody. */
    private static final float HURTS = 2.0F;
    /** How long an affliction lasts, in ticks. */
    private static final int LASTS = 100;
    /** The curse flies flat enough for the few blocks it has; it is spite, not a stone. */
    private static final float FALLS = 0.015F;
    /** Nothing this old was going to hit anything. */
    private static final int GIVES_UP = 100;

    public PechBlast(EntityType<? extends PechBlast> type, Level level) {
        super(type, level);
    }

    public PechBlast(Level level, LivingEntity thrower) {
        super(ModEntities.PECH_BLAST.get(), thrower, level);
    }

    /** Nothing about it is worth telling the client; it is a trail of particles and a burst. */
    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    @Override
    protected double getDefaultGravity() {
        return FALLS;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            level().addParticle(ParticleTypes.WITCH, getX(), getY(), getZ(), 0.0, 0.0, 0.0);
        } else if (tickCount > GIVES_UP) {
            discard();
        }
    }

    /**
     * The burst.
     * <p>
     * Whatever threw it is spared. A curse that caught the one casting it would be a curse nobody cast twice,
     * and the original spared the thrower too.
     */
    @Override
    protected void onHit(HitResult where) {
        super.onHit(where);
        burst(where);
    }

    /** The burst itself, which anything may set off and a test may watch. */
    public void burst(HitResult where) {
        if (!(level() instanceof ServerLevel served)) {
            return;
        }
        served.sendParticles(ParticleTypes.WITCH, getX(), getY(), getZ(), 24, 0.4, 0.4, 0.4, 0.1);
        served.playSound(null, getX(), getY(), getZ(), SoundEvents.SPLASH_POTION_BREAK,
                SoundSource.PLAYERS, 0.5F, 1.3F);

        Entity thrower = getOwner();
        List<LivingEntity> caught = served.getEntitiesOfClass(LivingEntity.class,
                new AABB(position(), position()).inflate(REACHES), alive -> alive != thrower);
        for (LivingEntity hit : caught) {
            hit.hurt(damageSources().indirectMagic(this, thrower), HURTS);
            hit.addEffect(new MobEffectInstance(oneOf(), LASTS));
        }
        discard();
    }

    /** One of the three, chosen when it bursts rather than when it is thrown. */
    private net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> oneOf() {
        return switch (random.nextInt(3)) {
            case 0 -> MobEffects.POISON;
            case 1 -> MobEffects.MOVEMENT_SLOWDOWN;
            default -> MobEffects.WEAKNESS;
        };
    }

    /** A curse passes through whoever cast it; it is looking for anybody else. */
    @Override
    protected boolean canHitEntity(Entity other) {
        return super.canHitEntity(other) && other != getOwner();
    }

    /** Hitting a living thing is still only a place to burst. The burst is what does the work. */
    @Override
    protected void onHitEntity(EntityHitResult struck) {
        super.onHitEntity(struck);
    }
}
