package me.moonscenty.alchemia.block.taint;

import java.util.ArrayList;
import java.util.List;

import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.registry.ModAspects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Flux that has fallen out of the air and lies about as a puddle.
 * <p>
 * It is a finite liquid rather than a proper fluid: there is only ever as much of it as was spilt, in eight depths,
 * and it runs downhill and levels out with its neighbours until it is spread thin. Then it dries, and each depth
 * that goes puts a point of flux back in the aura; the last of it, half the time, is where fibres start.
 * <p>
 * Now and then a deep enough puddle congeals into an alchemic slime instead, and an alchemic slime that wades through
 * goo deeper than it is big may soak some of it up and grow.
 */
public class FluxGooBlock extends Block {
    /** How deep the puddle is: nothing but a film, up to a full block. */
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 7);
    public static final int FULL = 7;

    /** How often it thinks about running somewhere, in ticks. */
    private static final int FLOW_EVERY = 5;
    /** A drop of one depth is not worth running for; the puddle only evens out where it is two or more down. */
    private static final int STEP = 2;
    /** How often, on a random tick, it dries a little instead of lying there. */
    private static final int DRY_CHANCE = 4;

    private static final VoxelShape[] SHAPES = new VoxelShape[FULL + 1];

    static {
        for (int level = 0; level <= FULL; level++) {
            SHAPES[level] = Block.box(0, 0, 0, 16, 2 + level * 2, 16);
        }
    }

    public FluxGooBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LEVEL, FULL));
    }

    public static BlockState of(Block goo, int level) {
        return goo.defaultBlockState().setValue(LEVEL, Math.clamp(level, 0, FULL));
    }

    // --- running downhill ----------------------------------------------------

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        level.scheduleTick(pos, this, FLOW_EVERY);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        level.scheduleTick(pos, this, FLOW_EVERY);
        return state;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int depth = state.getValue(LEVEL);

        // straight down first: a puddle over nothing falls as one
        BlockPos below = pos.below();
        if (roomAt(level, below)) {
            level.removeBlock(pos, false);
            level.setBlock(below, state, Block.UPDATE_ALL);
            return;
        }
        if (level.getBlockState(below).is(this)) {
            int theirs = level.getBlockState(below).getValue(LEVEL);
            int give = Math.min(depth + 1, FULL - theirs);
            if (give > 0) {
                level.setBlock(below, of(this, theirs + give), Block.UPDATE_ALL);
                shrink(level, pos, state, give);
                return;
            }
        }

        // otherwise it evens out sideways, a depth at a time, to wherever is lowest
        if (depth == 0) {
            return;
        }
        List<BlockPos> lower = new ArrayList<>();
        int lowest = depth - STEP;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos next = pos.relative(side);
            int theirs = depthAt(level, next);
            if (theirs != Integer.MAX_VALUE && theirs <= lowest) {
                if (theirs < lowest) {
                    lower.clear();
                    lowest = theirs;
                }
                lower.add(next);
            }
        }
        if (lower.isEmpty()) {
            return;
        }
        BlockPos into = lower.get(random.nextInt(lower.size()));
        level.setBlock(into, of(this, lowest + 1), Block.UPDATE_ALL);
        shrink(level, pos, state, 1);
    }

    /** A depth counted so that open ground reads as one lower than the thinnest film. */
    private int depthAt(LevelReader level, BlockPos pos) {
        BlockState there = level.getBlockState(pos);
        if (there.is(this)) {
            return there.getValue(LEVEL);
        }
        return roomAt(level, pos) ? -1 : Integer.MAX_VALUE;
    }

    private static boolean roomAt(LevelReader level, BlockPos pos) {
        BlockState there = level.getBlockState(pos);
        return (there.isAir() || there.canBeReplaced()) && there.getFluidState().isEmpty();
    }

    /** Takes some depth off a puddle, and takes the puddle away when there is none left to take. */
    private void shrink(ServerLevel level, BlockPos pos, BlockState state, int by) {
        int left = state.getValue(LEVEL) - by;
        if (left < 0) {
            level.removeBlock(pos, false);
        } else {
            level.setBlock(pos, of(this, left), Block.UPDATE_ALL);
        }
    }

    // --- drying up -----------------------------------------------------------

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (congeal(state, level, pos, random) || random.nextInt(DRY_CHANCE) != 0) {
            return;
        }
        int depth = state.getValue(LEVEL);
        if (depth > 0) {
            level.setBlock(pos, of(this, depth - 1), Block.UPDATE_CLIENTS);
            AuraHandler.add(level, pos, ModAspects.FLUX, 1);
        } else if (random.nextBoolean()) {
            AuraHandler.add(level, pos, ModAspects.FLUX, 1);
            level.removeBlock(pos, false);
        } else {
            level.setBlock(pos, TaintSpread.fibres(level, pos), Block.UPDATE_ALL);
        }
    }

    /**
     * One time in fifty, with open air over it, a puddle of some depth congeals: a little slime out of a middling one,
     * a bigger one out of a deep one. The original's depths.
     *
     * @return whether it did
     */
    private static boolean congeal(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int depth = state.getValue(LEVEL);
        if (depth < 2 || !level.isEmptyBlock(pos.above()) || random.nextInt(50) != 0) {
            return false;
        }
        level.removeBlock(pos, false);
        var slime = new me.moonscenty.alchemia.entity.AlchemicSlime(level);
        slime.setSize(depth >= 6 ? 2 : 1, true);
        slime.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        level.addFreshEntity(slime);
        level.playSound(null, pos, me.moonscenty.alchemia.registry.ModSounds.GORE.get(),
                net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 1.0F);
        return true;
    }

    // --- being waded through -------------------------------------------------

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        // an alchemic slime smaller than the goo is deep may soak up a depth of it and grow
        if (entity instanceof me.moonscenty.alchemia.entity.AlchemicSlime slime) {
            int depth = state.getValue(LEVEL);
            if (!level.isClientSide && slime.getSize() < depth && level.random.nextBoolean()) {
                slime.setSize(slime.getSize() + 1, true);
                if (depth > 1) {
                    level.setBlock(pos, of(this, depth - 1), Block.UPDATE_ALL);
                } else {
                    level.removeBlock(pos, false);
                }
            }
            return;
        }
        // thick enough to wade through, not to drown in
        entity.makeStuckInBlock(state, new Vec3(0.5, 0.35, 0.5));
        // and it drains whoever wades in it: half a minute of vis exhaustion, worse the deeper it is
        if (!level.isClientSide && entity instanceof net.minecraft.world.entity.LivingEntity living) {
            living.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    me.moonscenty.alchemia.player.effect.ModEffects.FLUX_FLU, 600, state.getValue(LEVEL) / 3, true,
                    true));
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(LEVEL)];
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }
}
