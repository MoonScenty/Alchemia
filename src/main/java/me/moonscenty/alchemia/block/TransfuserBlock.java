package me.moonscenty.alchemia.block;

import java.util.EnumMap;
import java.util.Map;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.block.entity.TransfuserBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * An essentia transfuser, filling or emptying, the original's {@code BlockEssentiaTransport}: a half-block fitting set
 * against the face it is put on, looking out from it, with a pipe at its back.
 */
public class TransfuserBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    /** Half a block, against what it is fixed to. The original's measures. */
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Map.of(
            Direction.DOWN, Block.box(4, 8, 4, 12, 16, 12),
            Direction.UP, Block.box(4, 0, 4, 12, 8, 12),
            Direction.NORTH, Block.box(4, 4, 8, 12, 12, 16),
            Direction.SOUTH, Block.box(4, 4, 0, 12, 12, 8),
            Direction.WEST, Block.box(8, 4, 4, 16, 12, 12),
            Direction.EAST, Block.box(0, 4, 4, 8, 12, 12)));

    private final boolean filling;

    public TransfuserBlock(boolean filling, Properties properties) {
        super(properties);
        this.filling = filling;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    /** Whether this one fills jars from a pipe, rather than empties them into one. */
    public boolean filling() {
        return filling;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(properties -> new TransfuserBlock(filling, properties));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TransfuserBlockEntity(pos, state);
    }
}
