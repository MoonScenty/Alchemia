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

// lets go on arrival, on crouch, or after a stretch of no progress; original's rules
public class Grapple extends ThrowableProjectile {
    private static final EntityDataAccessor<Boolean> BITING =
            SynchedEntityData.defineId(Grapple.class, EntityDataSerializers.BOOLEAN);

    private static final int IN_FLIGHT = 10;
    // pull as a share of the remaining distance, and the per-tick cap
    private static final double HAULS = 5.0;
    private static final double AT_MOST = 0.25;
    // upward kick on biting so the climber clears the ledge
    private static final double LIFTS = 0.4;
    // per-tick lift against gravity
    private static final double HOLDS_UP = 0.033;
    private static final double ARRIVED = 2.0;
    private static final int STUCK_FOR = 20;

    // extra flight ticks per rank of potency
    private static final int FURTHER = 5;
    // sticky: range where the pull strengthens, and its divisor
    private static final double CLOSE = 8.0;

    private int potency;
    private boolean sticky;

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
    protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("potency", potency);
        tag.putBoolean("sticky", sticky);
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        potency = tag.getInt("potency");
        sticky = tag.getBoolean("sticky");
    }

    public Grapple hauling(int potency, boolean sticky) {
        this.potency = potency;
        this.sticky = sticky;
        return this;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(BITING, false);
    }

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
            if (tickCount > IN_FLIGHT + potency * FURTHER) {
                discard();
            }
            return;
        }
        haul(thrower);
    }

    private void haul(Entity thrower) {
        Vec3 towards = position().subtract(thrower.position());
        double away = towards.length();
        boolean done = sticky ? false : away < ARRIVED || sameDistance > STUCK_FOR;
        if (done || thrower.isShiftKeyDown()) {
            discard();
            return;
        }

        // sticky hooks pull harder up close, holding the climber against a ceiling
        double share = sticky && away < CLOSE ? away * (CLOSE - away) : away;
        Vec3 pull = towards.scale(1.0 / (Math.max(0.01, share) * HAULS));
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

        // no progress for a while means the line is snagged: let go
        int step = (int) (away / 2.0);
        sameDistance = step == wasDistance ? sameDistance + 1 : 0;
        wasDistance = step;
    }

    @Override
    protected void onHit(HitResult where) {
        bite(where);
    }

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
