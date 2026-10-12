package me.moonscenty.alchemia.block;

import java.util.Map;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * An auxiliary venting port: set against the side of a smelter, it catches some of what the smelter loses before it
 * gets into the air.
 * <p>
 * Every point a smelter fails to keep tries each vent beside it in turn, and each catches it one time in three.
 * One vent takes a third of the flux; two, a little over half; three, seven in ten. It sits only on the side or back
 * of a smelter, facing into it, and takes up the half of its block nearest it.
 */
public class SmelterVentBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<SmelterVentBlock> CODEC = simpleCodec(SmelterVentBlock::new);
    /** The half nearest the smelter, a little in from the edges, for each way it can face. */
    private static final Map<Direction, VoxelShape> SHAPES = Map.of(
            Direction.NORTH, Block.box(2.0, 2.0, 0.0, 14.0, 14.0, 8.0),
            Direction.SOUTH, Block.box(2.0, 2.0, 8.0, 14.0, 14.0, 16.0),
            Direction.WEST, Block.box(0.0, 2.0, 2.0, 8.0, 14.0, 14.0),
            Direction.EAST, Block.box(8.0, 2.0, 2.0, 16.0, 14.0, 14.0));

    public SmelterVentBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends SmelterVentBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    /** Only on the side or back of a smelter, facing into it. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = SmelterAuxBlock.againstSmelter(context);
        return facing == null ? null : defaultBlockState().setValue(FACING, facing);
    }
}
