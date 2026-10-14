package me.moonscenty.alchemia.entity.cult;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * A ball of crackling light that follows what it was thrown at: pulled harder the nearer it gets, never faster than a
 * quarter of a block a tick on any axis, and gone after twelve seconds (eight for the blue kind, which golems were
 * to throw). It hits as hard as its thrower does, or a little more than half that for the blue, as magic.
 * <p>
 * Struck, it is batted the way the striker is looking -- and then turns back for its target, as the original's did.
 */
public class GolemOrb extends ThrowableProjectile {
    private static final EntityDataAccessor<Integer> TARGET = SynchedEntityData.defineId(GolemOrb.class,
            EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> RED = SynchedEntityData.defineId(GolemOrb.class,
            EntityDataSerializers.BOOLEAN);

    public GolemOrb(EntityType<? extends GolemOrb> type, Level level) {
        super(type, level);
    }

    public GolemOrb(Level level, LivingEntity thrower, @Nullable LivingEntity target, boolean red) {
        super(ModEntities.GOLEM_ORB.get(), thrower, level);
        entityData.set(TARGET, target == null ? -1 : target.getId());
        entityData.set(RED, red);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(TARGET, -1);
        builder.define(RED, false);
    }

    public boolean isRed() {
        return entityData.get(RED);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount > (isRed() ? 240 : 160)) {
            discard();
            return;
        }
        if (level().getEntity(entityData.get(TARGET)) instanceof LivingEntity target && target.isAlive()) {
            double away = distanceToSqr(target);
            if (away > 0.0) {
                Vec3 pull = new Vec3((target.getX() - getX()) / away,
                        (target.getBoundingBox().minY + target.getBbHeight() * 0.6 - getY()) / away,
                        (target.getZ() - getZ()) / away).scale(0.2);
                Vec3 motion = getDeltaMovement().add(pull);
                setDeltaMovement(Mth.clamp(motion.x, -0.25, 0.25), Mth.clamp(motion.y, -0.25, 0.25),
                        Mth.clamp(motion.z, -0.25, 0.25));
            }
        }
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!level().isClientSide) {
            playSound(ModSounds.SHOCK.get(), 1.0F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F);
            discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        if (!level().isClientSide && getOwner() instanceof LivingEntity thrower) {
            float hurts = (float) thrower.getAttributeValue(Attributes.ATTACK_DAMAGE) * (isRed() ? 1.0F : 0.6F);
            hit.getEntity().hurt(damageSources().indirectMagic(this, thrower), hurts);
        }
    }

    /** Batted the way the one striking it is looking. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) {
            return false;
        }
        markHurt();
        Entity striker = source.getEntity();
        if (striker == null) {
            return false;
        }
        setDeltaMovement(striker.getLookAngle().scale(0.9));
        playSound(ModSounds.ZAP.get(), 1.0F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F);
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public float getPickRadius() {
        return 1.0F;
    }
}
