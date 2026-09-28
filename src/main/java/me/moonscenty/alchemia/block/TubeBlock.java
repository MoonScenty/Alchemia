package me.moonscenty.alchemia.block;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.block.entity.TubeBlockEntity;
import me.moonscenty.alchemia.essentia.EssentiaHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
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
    protected MapCodec<? extends TubeBlock> codec() {
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
                    linkTo(context.getLevel(), context.getClickedPos(), side, state));
        }
        return state;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction towards, BlockState neighbour, LevelAccessor level,
            BlockPos pos, BlockPos neighbourPos) {
        return state.setValue(SIDES.get(towards), linkTo(level, pos, towards, state));
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
            joined = joined.setValue(SIDES.get(side), linkTo(level, pos, side, joined));
        }
        if (joined != state) {
            level.setBlock(pos, joined, Block.UPDATE_CLIENTS);
        }
    }

    /**
     * Whether essentia may pass through this tube, coming in by one side and leaving by another.
     * <p>
     * Plain pipe minds neither side. A valve minds whether it is shut, and a one-way minds which way it is pointed.
     *
     * @param out the side it would leave by, or null when this is the tube the essentia stops in
     */
    public boolean passes(BlockState state, Direction in, Direction out) {
        return true;
    }

    /** The one aspect this tube will let by, if it insists on one. */
    public Optional<Holder<Aspect>> insistsOn(BlockGetter level, BlockPos pos) {
        return Optional.empty();
    }

    /** How many turns a run waits for having come through this tube. */
    public int holdsUp() {
        return 0;
    }

    /**
     * Whether this tube has a side to spare there at all.
     * <p>
     * Plain pipe is open on all six. A valve has a handle on one of them, and a handle is not a socket: nothing
     * joins to the side it stands on, tube or vessel.
     */
    public boolean spare(BlockState state, Direction side) {
        return true;
    }

    /**
     * What lies on one side of a tube: another tube, something that holds essentia, or nothing to speak of.
     * <p>
     * Both ends have a say. A tube with a handle in the way refuses, and so does a neighbour with a handle in the
     * way, which is why the state being built is handed in rather than read back out of the world -- during
     * placement it is not in the world yet.
     */
    public static Link linkTo(BlockGetter level, BlockPos pos, Direction side, BlockState self) {
        if (self.getBlock() instanceof TubeBlock tube && !tube.spare(self, side)) {
            return Link.NONE;
        }
        BlockState beside = level.getBlockState(pos.relative(side));
        if (beside.getBlock() instanceof TubeBlock other) {
            return other.spare(beside, side.getOpposite()) ? Link.TUBE : Link.NONE;
        }
        // the side the neighbour is touched on is the opposite of the one the tube reaches out along
        return level.getBlockEntity(pos.relative(side)) instanceof EssentiaHolder holder
                && holder.reachableFrom(side.getOpposite()) ? Link.BLOCK : Link.NONE;
    }

    /**
     * Every kind of tube is pumped the same way.
     * <p>
     * The ticker is written out rather than got from {@code createTickerHelper}, which matches one type of block
     * entity and one only: the odd kinds of pipe keep their own, and all of them want this same pull.
     */
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return (world, pos, at, entity) -> {
            if (entity instanceof TubeBlockEntity tube) {
                TubeBlockEntity.tick(world, pos, at, tube);
            }
        };
    }
}
