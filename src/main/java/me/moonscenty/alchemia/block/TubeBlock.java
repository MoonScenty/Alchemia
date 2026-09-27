package me.moonscenty.alchemia.block;

import java.util.EnumMap;
import java.util.Map;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.block.entity.TubeBlockEntity;
import me.moonscenty.alchemia.essentia.EssentiaHolder;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A pipe that carries essentia between the things that hold it.
 * <p>
 * Each of its six sides is in one of three states, and the model is put together from them: nothing there, another
 * tube, or something worth reaching into. The last gets a collar on the end of the arm, so a run of pipe reads at a
 * glance as a run of pipe with its ends plugged into things.
 */
public class TubeBlock extends BaseEntityBlock {
    /** What a side is up against. */
    public enum Link implements StringRepresentable {
        NONE("none"),
        TUBE("tube"),
        BLOCK("block");

        private final String name;

        Link(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public static final Map<Direction, EnumProperty<Link>> SIDES = sides();

    public static final MapCodec<TubeBlock> CODEC = simpleCodec(TubeBlock::new);

    /** The bare middle, and one arm out of it per side. */
    private static final VoxelShape CORE = Block.box(6, 6, 6, 10, 10, 10);
    private static final Map<Direction, VoxelShape> ARMS = arms();

    private static Map<Direction, EnumProperty<Link>> sides() {
        Map<Direction, EnumProperty<Link>> out = new EnumMap<>(Direction.class);
        for (Direction side : Direction.values()) {
            out.put(side, EnumProperty.create(side.getSerializedName(), Link.class));
        }
        return Map.copyOf(out);
    }

    private static Map<Direction, VoxelShape> arms() {
        Map<Direction, VoxelShape> out = new EnumMap<>(Direction.class);
        out.put(Direction.EAST, Block.box(10, 6, 6, 16, 10, 10));
        out.put(Direction.WEST, Block.box(0, 6, 6, 6, 10, 10));
        out.put(Direction.SOUTH, Block.box(6, 6, 10, 10, 10, 16));
        out.put(Direction.NORTH, Block.box(6, 6, 0, 10, 10, 6));
        out.put(Direction.UP, Block.box(6, 10, 6, 10, 16, 10));
        out.put(Direction.DOWN, Block.box(6, 0, 6, 10, 6, 10));
        return Map.copyOf(out);
    }

    public TubeBlock(Properties properties) {
        super(properties);
        BlockState bare = stateDefinition.any();
        for (EnumProperty<Link> side : SIDES.values()) {
            bare = bare.setValue(side, Link.NONE);
        }
        registerDefaultState(bare);
    }

    @Override
    protected MapCodec<TubeBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        SIDES.values().forEach(builder::add);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TubeBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = CORE;
        for (Direction side : Direction.values()) {
            if (state.getValue(SIDES.get(side)) != Link.NONE) {
                shape = Shapes.or(shape, ARMS.get(side));
            }
        }
        return shape;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        for (Direction side : Direction.values()) {
            state = state.setValue(SIDES.get(side),
                    linkTo(context.getLevel(), context.getClickedPos(), side));
        }
        return state;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction towards, BlockState neighbour, LevelAccessor level,
            BlockPos pos, BlockPos neighbourPos) {
        return state.setValue(SIDES.get(towards), linkTo(level, pos, towards));
    }

    /**
     * Works out every side for a tube that arrived without being placed by hand.
     * <p>
     * Only a player's placement goes through {@link #getStateForPlacement}; a structure, a command or another mod
     * sets the block as it stands, and a tube that turned up that way would sit there as a bare middle with no arms
     * however much was plugged into it.
     */
    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState before, boolean moving) {
        BlockState joined = state;
        for (Direction side : Direction.values()) {
            joined = joined.setValue(SIDES.get(side), linkTo(level, pos, side));
        }
        if (joined != state) {
            level.setBlock(pos, joined, Block.UPDATE_CLIENTS);
        }
    }

    /** What lies on one side of a tube: another tube, something that holds essentia, or nothing to speak of. */
    public static Link linkTo(BlockGetter level, BlockPos pos, Direction side) {
        BlockPos at = pos.relative(side);
        if (level.getBlockState(at).getBlock() instanceof TubeBlock) {
            return Link.TUBE;
        }
        return level.getBlockEntity(at) instanceof EssentiaHolder ? Link.BLOCK : Link.NONE;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, ModBlockEntities.TUBE.get(), TubeBlockEntity::tick);
    }
}
