package me.moonscenty.alchemia.wand.spell;

import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class VisShard extends Bolt {
    private static final float HURTS = 1.0F;
    // original: launched wide at half speed, re-aimed each second, slowing all the while
    private static final float FLIES = 0.5F;
    private static final float FANS = 60.0F;
    private static final int STEERS_EVERY = 20;
    private static final double SLOWS = 0.85;
    private static final int FADES_AFTER = 300;
    private static final double OFF_WALL = -0.8;

    private static final double LOOKS = 16.0;
    private static final double LOSES_SIGHT_SQR = 1250.0;

    private float hurts = HURTS;
    private boolean persistent;
    private int chasing = -1;
    private Class<?> kin;

    public VisShard(EntityType<? extends VisShard> type, Level level) {
        super(type, level);
    }

    public VisShard(Level level, LivingEntity thrower, Entity mark) {
        super(ModEntities.VIS_SHARD.get(), level, thrower);
        chasing = mark.getId();
        kin = mark.getClass();
        Vec3 look = thrower.getLookAngle();
        setPos(thrower.getX() + look.x / 2.0, thrower.getEyeY() + look.y / 2.0, thrower.getZ() + look.z / 2.0);
        shootFromRotation(thrower, thrower.getXRot() + (random.nextFloat() - random.nextFloat()) * FANS,
                thrower.getYRot() + (random.nextFloat() - random.nextFloat()) * FANS, 0.0F, FLIES, 0.0F);
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
        super.tick();
        if (!level().isClientSide && !isRemoved()) {
            Entity mark = level().getEntity(chasing);
            if (persistent && (mark == null || !mark.isAlive() || mark.distanceToSqr(this) > LOSES_SIGHT_SQR)) {
                mark = somethingElse(mark);
            }
            if (mark == null || !mark.isAlive()) {
                discard();
                return;
            }
            if (tickCount % STEERS_EVERY == 0) {
                setDeltaMovement(new Vec3(mark.getX(), mark.getY() + mark.getBbHeight() * 0.6, mark.getZ())
                        .subtract(position()).scale(1.0 / distanceTo(mark)));
                hasImpulse = true;
            }
        }
        setDeltaMovement(getDeltaMovement().scale(SLOWS));
    }

    private Entity somethingElse(Entity lost) {
        if (kin == null) {
            return lost;
        }
        Entity thrower = getOwner();
        for (LivingEntity found : level().getEntitiesOfClass(LivingEntity.class,
                new net.minecraft.world.phys.AABB(position(), position()).inflate(LOOKS))) {
            if (kin.isInstance(found) && found.isAlive() && found != thrower) {
                chasing = found.getId();
                return found;
            }
        }
        return lost;
    }

    @Override
    public void lands(HitResult where) {
        if (where instanceof BlockHitResult wall) {
            Direction face = wall.getDirection();
            Vec3 going = getDeltaMovement();
            setDeltaMovement(face.getStepX() != 0 ? going.x * OFF_WALL : going.x,
                    face.getStepY() != 0 ? going.y * OFF_WALL : going.y,
                    face.getStepZ() != 0 ? going.z * OFF_WALL : going.z);
            return;
        }
        super.lands(where);
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
