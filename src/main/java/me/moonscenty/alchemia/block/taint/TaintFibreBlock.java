package me.moonscenty.alchemia.block.taint;

import java.util.EnumMap;
import java.util.Map;

import me.moonscenty.alchemia.player.effect.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The creeping edge of the taint: fibres clinging to whatever solid face is next to them.
 * <p>
 * They are the part that moves. A fibre only lives while it has something untainted to hold on to, so as the land
 * behind it turns, the fibres there wither and the front carries on. What stays behind are the growths: a few
 * fibres in every fifty, chosen by where they stand, that sprout something and keep it.
 */
public class TaintFibreBlock extends Block implements Tainted {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    /**
     * What has sprouted here, if anything: nothing, three kinds standing on the floor, and one hanging from the
     * ceiling. Two of them glow.
     */
    public static final IntegerProperty GROWTH = IntegerProperty.create("growth", 0, 4);
    public static final int HANGING = 4;

    private static final Map<Direction, BooleanProperty> FACES = new EnumMap<>(Map.of(
            Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH,
            Direction.WEST, WEST, Direction.UP, UP, Direction.DOWN, DOWN));
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Map.of(
            Direction.NORTH, Block.box(0, 0, 0, 16, 16, 1), Direction.SOUTH, Block.box(0, 0, 15, 16, 16, 16),
            Direction.WEST, Block.box(0, 0, 0, 1, 16, 16), Direction.EAST, Block.box(15, 0, 0, 16, 16, 16),
            Direction.UP, Block.box(0, 15, 0, 16, 16, 16), Direction.DOWN, Block.box(0, 0, 0, 16, 1, 16)));
    private static final VoxelShape SPROUT = Block.box(3, 0, 3, 13, 14, 13);

    /** How rarely brushing through fibres gives a creature the flu. */
    private static final int FLU_CHANCE = 750;
    private static final int FLU_LASTS = 200;

    public TaintFibreBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false).setValue(WEST, false)
                .setValue(UP, false).setValue(DOWN, false).setValue(GROWTH, 0));
    }

    /** The glowing kinds give a little light, the way the original had them shine in the dark. */
    public static int lightOf(BlockState state) {
        int growth = state.getValue(GROWTH);
        return growth == 2 || growth == HANGING ? 6 : 0;
    }

    @Override
    public void die(ServerLevel level, BlockPos pos, BlockState state) {
        level.removeBlock(pos, false);
    }

    // --- taking hold ---------------------------------------------------------

    /** Reads which faces there are to cling to, and what grows here, off the surroundings. */
    public static BlockState fitted(LevelReader level, BlockPos pos, BlockState state) {
        for (Direction side : Direction.values()) {
            BlockPos next = pos.relative(side);
            state = state.setValue(FACES.get(side), level.getBlockState(next).isFaceSturdy(level, next, side.getOpposite()));
        }
        return state.setValue(GROWTH, growthAt(pos, state.getValue(DOWN), state.getValue(UP)));
    }

    /**
     * What sprouts is settled by where the fibre stands, so it does not flicker between kinds as neighbours change,
     * and the same spot always grows the same thing.
     */
    private static int growthAt(BlockPos pos, boolean floor, boolean ceiling) {
        int roll = RandomSource.create(pos.asLong()).nextInt(50);
        if (ceiling && roll > 47) {
            return HANGING;
        }
        if (floor) {
            if (roll < 4) {
                return 1;
            }
            if (roll < 6) {
                return 2;
            }
            if (roll == 6) {
                return 3;
            }
        }
        return 0;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return fitted(context.getLevel(), context.getClickedPos(), defaultBlockState());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        BlockState refitted = fitted(level, pos, state);
        // a plain fibre with nothing untainted to hold has lost its purpose
        if (refitted.getValue(GROWTH) == 0 && TaintSpread.onlyBesideTaint(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return refitted;
    }

    // --- living and dying ----------------------------------------------------

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(GROWTH) == 0 && TaintSpread.onlyBesideTaint(level, pos)) {
            die(level, pos, state);
        } else if (TaintSpread.starved(level, pos)) {
            die(level, pos, state);
        } else {
            TaintSpread.spread(level, pos, random);
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity living && !living.isInvertedHealAndHarm()
                && level.random.nextInt(FLU_CHANCE) == 0) {
            living.addEffect(new MobEffectInstance(ModEffects.FLUX_FLU, FLU_LASTS, 0, false, true));
        }
    }

    // --- shape ---------------------------------------------------------------

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = Shapes.empty();
        for (Map.Entry<Direction, BooleanProperty> face : FACES.entrySet()) {
            if (state.getValue(face.getValue())) {
                shape = Shapes.or(shape, SHAPES.get(face.getKey()));
            }
        }
        if (state.getValue(GROWTH) != 0) {
            shape = Shapes.or(shape, SPROUT);
        }
        return shape.isEmpty() ? Shapes.block() : shape;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return true;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 100;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 60;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, GROWTH);
    }
}
