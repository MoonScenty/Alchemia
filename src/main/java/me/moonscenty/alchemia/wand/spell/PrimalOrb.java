package me.moonscenty.alchemia.wand.spell;

import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class PrimalOrb extends Bolt {
    private static final float BLAST = 2.0F;
    // one in this many plain orbs bursts into taint instead
    private static final int WRONG_IN = 100;
    // taint placement attempts and search radius
    private static final int TRIES = 15;
    private static final int SPREADS = 6;
    private static final int BURNS_OUT = 100;
    private static final double LOOKS = 16.0;
    private static final double STEERS = 0.2;

    private boolean seeker;

    public PrimalOrb(EntityType<? extends PrimalOrb> type, Level level) {
        super(type, level);
    }

    public PrimalOrb(Level level, LivingEntity thrower) {
        super(ModEntities.PRIMAL_ORB.get(), level, thrower);
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("seeker", seeker);
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        seeker = tag.getBoolean("seeker");
    }

    public PrimalOrb seeking() {
        this.seeker = true;
        return this;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    // the original's plain orb wandered randomly; ours flies straight, so only the seeker steers
    @Override
    public void tick() {
        if (seeker && !level().isClientSide) {
            LivingEntity mark = level().getNearestEntity(LivingEntity.class,
                    net.minecraft.world.entity.ai.targeting.TargetingConditions.forNonCombat().range(LOOKS),
                    null, getX(), getY(), getZ(),
                    new net.minecraft.world.phys.AABB(position(), position()).inflate(LOOKS));
            if (mark != null && mark != getOwner()) {
                net.minecraft.world.phys.Vec3 towards = mark.position()
                        .add(0.0, mark.getBbHeight() * 0.9, 0.0).subtract(position()).normalize();
                setDeltaMovement(getDeltaMovement().add(towards.scale(STEERS)));
            }
        }
        super.tick();
    }

    @Override
    protected ParticleOptions trail() {
        return ParticleTypes.WITCH;
    }

    @Override
    protected int lives() {
        return BURNS_OUT;
    }

    @Override
    protected void land(ServerLevel level, HitResult where) {
        level.explode(getOwner(), getX(), getY(), getZ(), BLAST, Level.ExplosionInteraction.TNT);
        if (!seeker && random.nextInt(WRONG_IN) == 0) {
            goesWrong(level);
        }
    }

    private void goesWrong(ServerLevel level) {
        BlockPos middle = blockPosition();
        for (int tries = 0; tries < TRIES; tries++) {
            BlockPos at = middle.offset(random.nextInt(SPREADS) - random.nextInt(SPREADS),
                    random.nextInt(SPREADS) - random.nextInt(SPREADS),
                    random.nextInt(SPREADS) - random.nextInt(SPREADS));
            if (level.isEmptyBlock(at) && hasSomethingToClingTo(level, at)) {
                level.setBlock(at, ModBlocks.TAINT_FIBRE.get().defaultBlockState(), 3);
            }
        }
    }

    private static boolean hasSomethingToClingTo(ServerLevel level, BlockPos at) {
        for (Direction side : Direction.values()) {
            if (!level.getBlockState(at.relative(side)).isAir()) {
                return true;
            }
        }
        return false;
    }
}
