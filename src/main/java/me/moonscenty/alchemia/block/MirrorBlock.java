package me.moonscenty.alchemia.block;

import java.util.Map;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.block.entity.MirrorBlockEntity;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A mirror: a thin pane hung on a solid face, looking out the way it was put up. A magic mirror and an essentia
 * mirror are the same block apart from what they pass. Nothing collides with it, and anything a magic mirror can
 * take that comes into its block is taken. It falls off if what it hangs on goes. Drawn wholly by its renderer.
 */
public class MirrorBlock extends BaseEntityBlock {
    public static final MapCodec<MirrorBlock> CODEC = simpleCodec(properties -> new MirrorBlock(properties, false));
    /** The way the glass looks: into the room, away from what it hangs on. */
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    private static final Map<Direction, VoxelShape> PANES = Map.of(
            Direction.DOWN, Block.box(0, 14, 0, 16, 16, 16),
            Direction.UP, Block.box(0, 0, 0, 16, 2, 16),
            Direction.NORTH, Block.box(0, 0, 14, 16, 16, 16),
            Direction.SOUTH, Block.box(0, 0, 0, 16, 16, 2),
            Direction.WEST, Block.box(14, 0, 0, 16, 16, 16),
            Direction.EAST, Block.box(0, 0, 0, 2, 16, 16));

    private final boolean essentia;

    public MirrorBlock(Properties properties, boolean essentia) {
        super(properties);
        this.essentia = essentia;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    public boolean essentia() {
        return essentia;
    }

    @Override
    protected MapCodec<MirrorBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return PANES.get(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(FACING, context.getClickedFace());
        return canSurvive(state, context.getLevel(), context.getClickedPos()) ? state : null;
    }

    /** On a face that is solid where it hangs. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos behind = pos.relative(facing.getOpposite());
        return level.getBlockState(behind).isFaceSturdy(level, behind, facing);
    }

    /** Falls off, and drops, a tick after what it hangs on has gone. */
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

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MirrorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, ModBlockEntities.MIRROR.get(), MirrorBlockEntity::tick);
    }

    /** A magic mirror takes in a stack that comes into its block, once it is not one it has just let out. */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!essentia && !level.isClientSide && entity instanceof ItemEntity item && item.isAlive()
                && !item.isOnPortalCooldown() && level.getBlockEntity(pos) instanceof MirrorBlockEntity mirror) {
            mirror.send(item);
        }
    }

    /** Taken down, it tells its partner it is on its own; what it carries remembers where the partner is. */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock()) && level.getBlockEntity(pos) instanceof MirrorBlockEntity mirror) {
            mirror.unpair();
        }
        super.onRemove(state, level, pos, replacement, moving);
    }
}
