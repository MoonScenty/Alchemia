package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/**
 * A length of pipe that only lets essentia one way.
 * <p>
 * It is laid pointing away from whoever placed it, and that is the way things go: in by any of the other five sides
 * and out by the one it faces. A ring of pipe with one of these in it therefore turns rather than sloshing.
 */
public class OnewayTubeBlock extends TubeBlock {
    /** The way out. */
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public static final MapCodec<OnewayTubeBlock> CODEC = simpleCodec(OnewayTubeBlock::new);

    public OnewayTubeBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<OnewayTubeBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    /** Only ever out by the one side. */
    @Override
    public boolean passes(BlockState state, Direction in, Direction out) {
        return state.getValue(FACING) == out;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // the way the placer was looking, so a run laid out ahead of you flows away from you
        return super.getStateForPlacement(context).setValue(FACING, context.getNearestLookingDirection());
    }
}
