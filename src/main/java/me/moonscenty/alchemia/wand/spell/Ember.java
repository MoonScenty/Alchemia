package me.moonscenty.alchemia.wand.spell;

import me.moonscenty.alchemia.registry.ModEntities;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * A mote of fire, thrown by the handful for as long as the button is down.
 *
 * <p>No one of them is worth much. What makes the focus is that there are two a tick and they go out in a spray
 * rather than a line, so a wand pointed at a doorway fills it.
 *
 * <p>They burn out on their own. An ember that reached nothing is an ember that stops being, which is why the
 * world is not left full of them after a long press.
 */
public class Ember extends Bolt {
    /** What it does where it lands, and how long whatever it lands on burns for, in seconds. */
    private static final float HURTS = 2.0F;
    private static final int BURNS_FOR = 3;
    /** How long a mote lasts before it goes out, in ticks. */
    private static final int GOES_OUT = 20;

    public Ember(EntityType<? extends Ember> type, Level level) {
        super(type, level);
    }

    public Ember(Level level, LivingEntity thrower) {
        super(ModEntities.EMBER.get(), level, thrower);
    }

    /** Fire does not fall. It goes where it was thrown until it burns out. */
    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    protected ParticleOptions trail() {
        return ParticleTypes.FLAME;
    }

    @Override
    protected int lives() {
        return GOES_OUT;
    }

    @Override
    protected void land(ServerLevel level, HitResult where) {
        level.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY(), getZ(), 3, 0.1, 0.1, 0.1, 0.01);
        if (!(where instanceof EntityHitResult struck)) {
            return;
        }
        Entity hit = struck.getEntity();
        if (hit.hurt(damageSources().indirectMagic(this, getOwner()), HURTS)) {
            hit.igniteForSeconds(BURNS_FOR);
        }
    }
}
