package me.moonscenty.alchemia.wand.spell;

import me.moonscenty.alchemia.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * A hook thrown at a wall, which then hauls whoever threw it towards itself.
 *
 * <p>It flies out, bites whatever it reaches first, and from then on pulls. The pull is gentle and constant
 * rather than a yank: a little towards the hook every tick, plus a lift at the moment it bites so that the first
 * thing a climber does is clear the ledge rather than slide up the face of it.
 *
 * <p>It lets go when the thrower is on top of it, when they crouch, or when they have stopped getting any nearer
 * for a while -- which is what happens to somebody hooked round a corner. All three are the original's.
 *
 * <p>While it is falling it is short-lived. A hook that bit nothing is a hook not worth leaving in the air.
 */
public class Grapple extends ThrowableProjectile {
    /** Whether it has bitten. Sent to the client so the line can be drawn taut rather than slack. */
    private static final EntityDataAccessor<Boolean> BITING =
            SynchedEntityData.defineId(Grapple.class, EntityDataSerializers.BOOLEAN);

    /** How long a hook that has bitten nothing stays in the air, in ticks. */
    private static final int IN_FLIGHT = 10;
    /** How hard it hauls, as a share of the distance left, and the most it will haul in one tick. */
    private static final double HAULS = 5.0;
    private static final double AT_MOST = 0.25;
    /** The lift the moment it bites, so a climber clears the ledge instead of scraping it. */
    private static final double LIFTS = 0.4;
    /** A little extra upward every tick, to hold against the fall. */
    private static final double HOLDS_UP = 0.033;
    /** Close enough to have arrived. */
    private static final double ARRIVED = 2.0;
    /** How many ticks of getting no nearer before it gives up. */
    private static final int STUCK_FOR = 20;

    private boolean lifted;
    private int sameDistance;
    private int wasDistance = -1;

    public Grapple(EntityType<? extends Grapple> type, Level level) {
        super(type, level);
    }

    public Grapple(Level level, LivingEntity thrower) {
        super(ModEntities.GRAPPLE.get(), thrower, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(BITING, false);
    }

    /** A hook flies flat. Whatever it is made of, it is not thrown like a stone. */
    @Override
    protected double getDefaultGravity() {
        return biting() ? 0.0 : 0.03;
    }

    public boolean biting() {
        return entityData.get(BITING);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            level().addParticle(ParticleTypes.CRIT, getX(), getY(), getZ(), 0.0, 0.0, 0.0);
            return;
        }
        Entity thrower = getOwner();
        if (thrower == null || !thrower.isAlive()) {
            discard();
            return;
        }
        if (!biting()) {
            if (tickCount > IN_FLIGHT) {
                discard();
            }
            return;
        }
        haul(thrower);
    }

    /**
     * One tick of pulling.
     * <p>
     * The hook does not move; the thrower does. What is added is a share of the way there rather than a step of
     * fixed size, so a long swing starts fast and settles as it closes.
     */
    private void haul(Entity thrower) {
        Vec3 towards = position().subtract(thrower.position());
        double away = towards.length();
        if (away < ARRIVED || thrower.isShiftKeyDown() || sameDistance > STUCK_FOR) {
            discard();
            return;
        }

        Vec3 pull = towards.scale(1.0 / (away * HAULS));
        if (pull.length() > AT_MOST) {
            pull = pull.normalize().scale(AT_MOST);
        }
        thrower.setDeltaMovement(thrower.getDeltaMovement().add(pull.x, pull.y + HOLDS_UP, pull.z));
        thrower.fallDistance = 0.0F;
        thrower.hurtMarked = true;
        if (!lifted) {
            thrower.setDeltaMovement(thrower.getDeltaMovement().add(0.0, LIFTS, 0.0));
            lifted = true;
        }

        // getting no nearer for a while means the line is caught on something, and a line caught is a line let go
        int step = (int) (away / 2.0);
        sameDistance = step == wasDistance ? sameDistance + 1 : 0;
        wasDistance = step;
    }

    /** Biting: it stops dead where it struck and starts pulling from there. */
    @Override
    protected void onHit(HitResult where) {
        bite(where);
    }

    /** Taking hold, which anything may set off and a test may watch. */
    public void bite(HitResult where) {
        if (biting() || level().isClientSide) {
            return;
        }
        entityData.set(BITING, true);
        setDeltaMovement(Vec3.ZERO);
        setPos(where.getLocation().x, where.getLocation().y, where.getLocation().z);
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.TRIDENT_HIT_GROUND,
                SoundSource.PLAYERS, 0.4F, 1.6F);
    }

    @Override
    protected boolean canHitEntity(Entity other) {
        return super.canHitEntity(other) && other != getOwner();
    }
}
