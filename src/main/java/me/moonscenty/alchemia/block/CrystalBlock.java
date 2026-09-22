package me.moonscenty.alchemia.block;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A vis crystal growing on a solid face. It has four sizes and drops one shard per size.
 * <p>
 * It lives off the aura, growing and seeding where there is plenty and wasting away where there is none; the rules
 * are in {@link CrystalGrowth}. Which generation a crystal is says how far it is from one the world made.
 */
public class CrystalBlock extends Block implements SimpleWaterloggedBlock {
    public static final int MAX_AGE = 3;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    /** The first generation is what the world made; each seeding is one further on, and the last does not seed. */
    public static final int LAST_GENERATION = 4;
    public static final IntegerProperty GENERATION = IntegerProperty.create("generation", 1, LAST_GENERATION);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // height and horizontal inset per age, matching the amethyst bud/cluster proportions
    private static final int[] HEIGHTS = {3, 4, 5, 7};
    private static final int[] INSETS = {4, 3, 3, 3};
    private static final Map<Direction, VoxelShape[]> SHAPES = makeShapes();

    private final CrystalType type;

    public CrystalBlock(CrystalType type, Properties properties) {
        super(properties);
        this.type = type;
        registerDefaultState(stateDefinition.any().setValue(AGE, 0).setValue(FACING, Direction.UP).setValue(WATERLOGGED, false)
                .setValue(GENERATION, 1));
    }

    public CrystalType getType() {
        return type;
    }

    public static int getLightLevel(BlockState state) {
        return 2 + state.getValue(AGE) * 3;
    }

    private static Map<Direction, VoxelShape[]> makeShapes() {
        Map<Direction, VoxelShape[]> shapes = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            VoxelShape[] byAge = new VoxelShape[MAX_AGE + 1];
            for (int age = 0; age <= MAX_AGE; age++) {
                int h = HEIGHTS[age];
                int i = INSETS[age];
                byAge[age] = switch (direction) {
                    case UP -> Block.box(i, 0, i, 16 - i, h, 16 - i);
                    case DOWN -> Block.box(i, 16 - h, i, 16 - i, 16, 16 - i);
                    case NORTH -> Block.box(i, i, 16 - h, 16 - i, 16 - i, 16);
                    case SOUTH -> Block.box(i, i, 0, 16 - i, 16 - i, h);
                    case EAST -> Block.box(0, i, i, h, 16 - i, 16 - i);
                    case WEST -> Block.box(16 - h, i, i, 16, 16 - i, 16 - i);
                };
            }
            shapes.put(direction, byAge);
        }
        return shapes;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING))[state.getValue(AGE)];
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction direction = state.getValue(FACING);
        BlockPos supportPos = pos.relative(direction.getOpposite());
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, direction);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return direction == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        LevelAccessor level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        return defaultBlockState()
                .setValue(WATERLOGGED, level.getFluidState(pos).getType() == Fluids.WATER)
                .setValue(FACING, context.getClickedFace());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        CrystalGrowth.tick(state, level, pos, random);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, FACING, WATERLOGGED, GENERATION);
    }
}
