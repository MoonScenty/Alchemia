package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A mnemonic matrix: a clockwork mind boxed up and fixed to a thaumatorium, so that it can keep two more recipes in
 * mind. It goes on either half's sides, or on top, but not on the front, and it faces into the thaumatorium. It
 * falls off when the thaumatorium is gone.
 */
public class MnemonicMatrixBlock extends DirectionalBlock {
    public static final MapCodec<MnemonicMatrixBlock> CODEC = simpleCodec(MnemonicMatrixBlock::new);
    private static final VoxelShape SHAPE = Block.box(3.0, 3.0, 3.0, 13.0, 13.0, 13.0);

    public MnemonicMatrixBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
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
        BlockState state = defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
        return canSurvive(state, context.getLevel(), context.getClickedPos()) ? state : null;
    }

    /** Against a thaumatorium, and not against its front. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction into = state.getValue(FACING);
        BlockState against = level.getBlockState(pos.relative(into));
        return against.getBlock() instanceof ThaumatoriumBlock
                && against.getValue(ThaumatoriumBlock.FACING) != into.getOpposite();
    }

    /** Falls off, and drops, a tick after what it was fixed to has gone. */
    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbour,
            LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        if (!canSurvive(state, level, pos)) {
            level.scheduleTick(pos, this, 1);
        }
        return state;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
        }
    }
}
