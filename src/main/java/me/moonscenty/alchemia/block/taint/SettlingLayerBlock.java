package me.moonscenty.alchemia.block.taint;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * A finite layer in a few depths that falls, and runs downhill a depth at a time until it lies level: flux goo and
 * taint dust.
 */
public abstract class SettlingLayerBlock extends Block {
    /** A layer only spreads to a neighbour two or more depths lower. */
    private static final int STEP = 2;

    private final int settlesEvery;

    protected SettlingLayerBlock(Properties properties, int settlesEvery) {
        super(properties);
        this.settlesEvery = settlesEvery;
        registerDefaultState(stateDefinition.any().setValue(depthProperty(), full()));
    }

    /** The depth property. A static constant of the subclass: it is asked for while the block is being built. */
    protected abstract IntegerProperty depthProperty();

    /** The deepest it gets, a whole block. */
    protected abstract int full();

    protected BlockState withDepth(int depth) {
        return defaultBlockState().setValue(depthProperty(), Math.clamp(depth, 0, full()));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(depthProperty());
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        level.scheduleTick(pos, this, settlesEvery);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        level.scheduleTick(pos, this, settlesEvery);
        return state;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        settle(state, level, pos, random);
    }

    /** Down if it can, else a depth sideways to the lowest neighbour two or more below it. */
    protected void settle(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int depth = state.getValue(depthProperty());

        // a layer over nothing falls as one
        BlockPos below = pos.below();
        if (roomAt(level, below)) {
            level.removeBlock(pos, false);
            level.setBlock(below, state, Block.UPDATE_ALL);
            return;
        }
        if (level.getBlockState(below).is(this)) {
            int theirs = level.getBlockState(below).getValue(depthProperty());
            int give = Math.min(depth + 1, full() - theirs);
            if (give > 0) {
                level.setBlock(below, withDepth(theirs + give), Block.UPDATE_ALL);
                shrink(level, pos, state, give);
                return;
            }
        }

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
        level.setBlock(into, withDepth(lowest + 1), Block.UPDATE_ALL);
        shrink(level, pos, state, 1);
    }

    /** A depth counted so that open ground reads as one lower than the thinnest layer. */
    private int depthAt(LevelReader level, BlockPos pos) {
        BlockState there = level.getBlockState(pos);
        if (there.is(this)) {
            return there.getValue(depthProperty());
        }
        return roomAt(level, pos) ? -1 : Integer.MAX_VALUE;
    }

    private static boolean roomAt(LevelReader level, BlockPos pos) {
        BlockState there = level.getBlockState(pos);
        return (there.isAir() || there.canBeReplaced()) && there.getFluidState().isEmpty();
    }

    /** Takes some depth off, and takes the layer away when there is none left to take. */
    private void shrink(ServerLevel level, BlockPos pos, BlockState state, int by) {
        int left = state.getValue(depthProperty()) - by;
        if (left < 0) {
            level.removeBlock(pos, false);
        } else {
            level.setBlock(pos, withDepth(left), Block.UPDATE_ALL);
        }
    }
}
