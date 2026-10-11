package me.moonscenty.alchemia.block;

import java.util.EnumMap;
import java.util.Map;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.block.entity.ArcaneEarBlockEntity;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * An arcane ear, fixed to a face of a block and listening for one note.
 *
 * <p>It is tuned like a note block, a click a semitone up through the same two octaves, and it hears in the voice of
 * what it is fixed to the way a note block plays in the voice of what it stands on. When a note block within
 * sixty-four blocks plays its note in its voice, it gives a full redstone signal for half a second, through the block
 * it is fixed to as well, the way a lever does.
 */
public class ArcaneEarBlock extends BaseEntityBlock {
    public static final MapCodec<ArcaneEarBlock> CODEC = simpleCodec(ArcaneEarBlock::new);
    /** The way the ear points, away from what it is fixed to. */
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    /** Hearing its note, and signalling. */
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;
    /** Three quarters across and three eighths deep, against what it is fixed to. The original's measures. */
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Map.of(
            Direction.DOWN, Block.box(2, 10, 2, 14, 16, 14),
            Direction.UP, Block.box(2, 0, 2, 14, 6, 14),
            Direction.NORTH, Block.box(2, 2, 10, 14, 14, 16),
            Direction.SOUTH, Block.box(2, 2, 0, 14, 14, 6),
            Direction.WEST, Block.box(10, 2, 2, 16, 14, 14),
            Direction.EAST, Block.box(0, 2, 2, 6, 14, 14)));

    public ArcaneEarBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP).setValue(ENABLED, false));
    }

    @Override
    protected MapCodec<ArcaneEarBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ENABLED);
    }

    /** Pointing out of the face it was put against. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(FACING, context.getClickedFace());
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    /** Only against a face whole enough to fix it to. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos on = pos.relative(facing.getOpposite());
        return level.getBlockState(on).isFaceSturdy(level, on, facing);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction side, BlockState other, LevelAccessor level,
            BlockPos pos, BlockPos otherPos) {
        return side == state.getValue(FACING).getOpposite() && !canSurvive(state, level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, side, other, level, pos, otherPos);
    }

    /** A semitone up, and the note played so whoever is tuning it can hear what it now listens for. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof ArcaneEarBlockEntity ear) {
            ear.raisePitch();
            ear.play(true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        return state.getValue(ENABLED) ? 15 : 0;
    }

    /** Through what it is fixed to, too, as a lever's signal goes. */
    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        return state.getValue(ENABLED) ? 15 : 0;
    }

    /** What it was signalling into hears that it has stopped. */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        boolean signalling = state.getValue(ENABLED) && !state.is(replacement.getBlock());
        super.onRemove(state, level, pos, replacement, moving);
        if (signalling && !moving) {
            level.updateNeighborsAt(pos, this);
            level.updateNeighborsAt(pos.relative(state.getValue(FACING).getOpposite()), this);
        }
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
        return new ArcaneEarBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, ModBlockEntities.ARCANE_EAR.get(), ArcaneEarBlockEntity::tick);
    }
}
