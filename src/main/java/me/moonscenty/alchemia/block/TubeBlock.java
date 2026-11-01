package me.moonscenty.alchemia.block;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.block.entity.BufferTubeBlockEntity;
import me.moonscenty.alchemia.block.entity.TubeBlockEntity;
import me.moonscenty.alchemia.essentia.EssentiaHolder;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TubeBlock extends BaseEntityBlock {
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

    private static final VoxelShape CORE = Block.box(6, 6, 6, 10, 10, 10);
    private static final double MIDDLE_REACH = 2.0 / 16.0;
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

    // structures and commands skip getStateForPlacement
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

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        Direction arm = armAt(state, pos, hit.getLocation());
        if (!(stack.getItem() instanceof WandItem) || arm == null
                || !(level.getBlockEntity(pos) instanceof TubeBlockEntity tube)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide) {
            if (player.isSecondaryUseActive() && tube instanceof BufferTubeBlockEntity buffer) {
                buffer.choke(arm);
                level.playSound(null, pos, ModSounds.SQUEEK.get(), SoundSource.BLOCKS, 0.6F,
                        2.0F + level.random.nextFloat() * 0.2F);
            } else {
                boolean closed = !tube.shut(arm);
                tube.shut(arm, closed);
                if (level.getBlockEntity(pos.relative(arm)) instanceof TubeBlockEntity other) {
                    other.shut(arm.getOpposite(), closed);
                }
                level.playSound(null, pos, ModSounds.TOOL.get(), SoundSource.BLOCKS, 0.5F,
                        0.9F + level.random.nextFloat() * 0.2F);
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    public static Direction armAt(BlockState state, BlockPos pos, Vec3 hit) {
        double x = hit.x - pos.getX() - 0.5;
        double y = hit.y - pos.getY() - 0.5;
        double z = hit.z - pos.getZ() - 0.5;
        double out = Math.max(Math.abs(x), Math.max(Math.abs(y), Math.abs(z)));
        if (out <= MIDDLE_REACH + 1.0E-4) {
            return null;
        }
        Direction arm = out == Math.abs(x) ? (x > 0 ? Direction.EAST : Direction.WEST)
                : out == Math.abs(y) ? (y > 0 ? Direction.UP : Direction.DOWN)
                : z > 0 ? Direction.SOUTH : Direction.NORTH;
        return state.getValue(SIDES.get(arm)) == Link.NONE ? null : arm;
    }

    public boolean passes(BlockState state, Direction in, Direction out) {
        return true;
    }

    public Optional<Holder<Aspect>> insistsOn(BlockGetter level, BlockPos pos) {
        return Optional.empty();
    }

    public int holdsUp() {
        return 0;
    }

    public boolean spare(BlockState state, Direction side) {
        return true;
    }

    // self is passed in because during placement it is not in the world yet
    public static Link linkTo(BlockGetter level, BlockPos pos, Direction side, BlockState self) {
        if (self.getBlock() instanceof TubeBlock tube && !tube.spare(self, side)) {
            return Link.NONE;
        }
        BlockState beside = level.getBlockState(pos.relative(side));
        if (beside.getBlock() instanceof TubeBlock other) {
            return other.spare(beside, side.getOpposite()) ? Link.TUBE : Link.NONE;
        }
        return level.getBlockEntity(pos.relative(side)) instanceof EssentiaHolder holder
                && holder.reachableFrom(side.getOpposite()) ? Link.BLOCK : Link.NONE;
    }

    // not createTickerHelper: it matches a single block entity type, and every tube subtype needs this ticker
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
