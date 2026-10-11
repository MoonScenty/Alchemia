package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.block.entity.ArcaneLampBlockEntity;
import me.moonscenty.alchemia.block.entity.FertilityLampBlockEntity;
import me.moonscenty.alchemia.block.entity.GrowthLampBlockEntity;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A lamp of brass and glass, hung from whatever face it was put against.
 *
 * <p>There are three, and they share everything but what the light is for. The arcane lamp lights the dark round
 * it. The lamp of growth hurries what is planted round it, and the lamp of fertility brings the animals round it
 * together; those two burn essentia for it, drawn through the face they hang from, and go dark when they run
 * out. A redstone signal puts any of them out.
 *
 * <p>Take away what a lamp is hanging from and it falls.
 */
public class LampBlock extends BaseEntityBlock {
    public static final MapCodec<LampBlock> CODEC = com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(
            lamp -> lamp.group(Kind.CODEC.fieldOf("kind").forGetter(LampBlock::kind), propertiesCodec())
                    .apply(lamp, LampBlock::new));
    /** The face the lamp hangs from. */
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    /** Lit, which is to say working: unpowered, and, for the two that burn essentia, fed. */
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;
    private static final VoxelShape SHAPE = Block.box(4.0, 2.0, 4.0, 12.0, 14.0, 12.0);

    /** What the light is for. */
    public enum Kind implements net.minecraft.util.StringRepresentable {
        ARCANE, GROWTH, FERTILITY;

        public static final com.mojang.serialization.Codec<Kind> CODEC =
                net.minecraft.util.StringRepresentable.fromEnum(Kind::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    private final Kind kind;

    public LampBlock(Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.DOWN).setValue(ENABLED, false));
    }

    public Kind kind() {
        return kind;
    }

    @Override
    protected MapCodec<LampBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ENABLED);
    }

    /** Hanging from the face it was put against. The arcane lamp lights at once; the other two wait to be fed. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean lit = kind == Kind.ARCANE && !context.getLevel().hasNeighborSignal(context.getClickedPos());
        return defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite()).setValue(ENABLED, lit);
    }

    /** Only where there is something to hang it from. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos from = pos.relative(state.getValue(FACING));
        return !level.getBlockState(from).isAir();
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction side, BlockState other, LevelAccessor level,
            BlockPos pos, BlockPos otherPos) {
        return side == state.getValue(FACING) && !canSurvive(state, level, pos)
                ? net.minecraft.world.level.block.Blocks.AIR.defaultBlockState()
                : super.updateShape(state, side, other, level, pos, otherPos);
    }

    /**
     * A signal puts the arcane lamp out at once, and takes back every light it has scattered. The other two
     * notice on their own next tick, since whether they are lit also depends on whether they are fed.
     */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block from, BlockPos fromPos,
            boolean moving) {
        if (kind == Kind.ARCANE && !level.isClientSide) {
            boolean lit = !level.hasNeighborSignal(pos);
            if (lit != state.getValue(ENABLED)) {
                level.setBlock(pos, state.setValue(ENABLED, lit), Block.UPDATE_ALL);
                if (!lit && level.getBlockEntity(pos) instanceof ArcaneLampBlockEntity lamp) {
                    lamp.takeBackLights();
                }
            }
        }
        super.neighborChanged(state, level, pos, from, fromPos, moving);
    }

    /** Whatever light the arcane lamp scattered goes out with it. */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock()) && level.getBlockEntity(pos) instanceof ArcaneLampBlockEntity lamp) {
            lamp.takeBackLights();
        }
        super.onRemove(state, level, pos, replacement, moving);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch (kind) {
            case ARCANE -> new ArcaneLampBlockEntity(pos, state);
            case GROWTH -> new GrowthLampBlockEntity(pos, state);
            case FERTILITY -> new FertilityLampBlockEntity(pos, state);
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return switch (kind) {
            case ARCANE -> createTickerHelper(type, ModBlockEntities.ARCANE_LAMP.get(), ArcaneLampBlockEntity::tick);
            case GROWTH -> createTickerHelper(type, ModBlockEntities.GROWTH_LAMP.get(), GrowthLampBlockEntity::tick);
            case FERTILITY -> createTickerHelper(type, ModBlockEntities.FERTILITY_LAMP.get(),
                    FertilityLampBlockEntity::tick);
        };
    }
}
