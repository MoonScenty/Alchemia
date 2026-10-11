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

/**
 * All six primals at once, wound into a ball and thrown.
 *
 * <p>It is the dearest of the twelve and it is a bomb. Where it lands it goes off and takes the ground with it,
 * which is the original's doing and not a decision taken lightly here: a focus that costs sixty vis a throw and
 * leaves a scratch would be a focus nobody threw.
 *
 * <p>Once in a hundred it goes wrong instead, and what comes out of it is taint. Six primals held against each
 * other is not a stable thing to be carrying.
 */
public class PrimalOrb extends Bolt {
    /** How big a hole it makes. */
    private static final float BLAST = 2.0F;
    /** How often, out of a hundred, it goes wrong instead of merely going off. */
    private static final int WRONG_IN = 100;
    /** How many places the taint tries to take hold, and how far out it looks. */
    private static final int TRIES = 15;
    private static final int SPREADS = 6;
    private static final int BURNS_OUT = 100;
    /** How far a seeking orb looks for something to go at, and how hard it steers towards it. */
    private static final double LOOKS = 16.0;
    private static final double STEERS = 0.2;

    /** Whether it has been settled enough to aim itself, which is also what stops it going wrong. */
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

    /** Wound tight enough to steer: it goes at the nearest living thing, and nothing unusual befalls it. */
    public PrimalOrb seeking() {
        this.seeker = true;
        return this;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    /**
     * A seeker leans towards whatever is nearest.
     * <p>
     * The original gave the plain orb a small random wander and the seeker this instead. Ours flies straight to
     * begin with, so only the steering is added.
     */
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
        // a seeker is wound tight enough that nothing unusual happens to it, which is half of what it is for
        if (!seeker && random.nextInt(WRONG_IN) == 0) {
            goesWrong(level);
        }
    }

    /** The once in a hundred: taint takes hold wherever there is air with something solid to cling to. */
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
