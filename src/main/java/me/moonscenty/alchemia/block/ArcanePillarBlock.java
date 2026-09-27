package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A tapering stone column, two blocks and a little more of it.
 * <p>
 * It leans as it rises, so four of them set at the corners of a matrix lean outward together and the whole works
 * reads as one building rather than four posts. Which way it leans is what the facing is for: the model is drawn
 * leaning one way and turned a quarter at a time from there.
 * <p>
 * The model stands taller than the block it is in. Minecraft draws that without complaint, and the alternative --
 * a second block on top holding nothing but the top half of a picture -- is worse to place and worse to break.
 */
public class ArcanePillarBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public static final MapCodec<ArcanePillarBlock> CODEC = simpleCodec(ArcanePillarBlock::new);

    /** What you can stand on and walk into: the whole of the foot, and the shaft above it. */
    private static final VoxelShape SHAPE = net.minecraft.world.phys.shapes.Shapes.or(
            Block.box(0, 0, 0, 16, 8, 16),
            Block.box(2, 8, 2, 14, 32, 14));

    public ArcanePillarBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<ArcanePillarBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }
}
