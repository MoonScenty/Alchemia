package me.moonscenty.alchemia.block;

import java.util.EnumMap;
import java.util.Map;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.block.entity.LevitatorBlockEntity;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * An arcane levitator: a box of wood and brass that lifts whatever is in front of it.
 *
 * <p>It pushes along the way it faces -- up, if it was put down on the ground -- as far as its range or the first
 * solid block, and holds anything it pushes sideways aloft as it goes. It burns aer from the aura to do it, more the
 * further it reaches. A redstone signal stops it.
 *
 * <p>It faces the way a piston would, back at whoever put it down; crouching turns it round. An empty hand on it
 * steps its range through four, eight, sixteen and thirty-two blocks.
 */
public class LevitatorBlock extends BaseEntityBlock {
    public static final MapCodec<LevitatorBlock> CODEC = simpleCodec(LevitatorBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    /** Working, which is what it does unless a redstone signal is holding it still. */
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;
    /** The body, an eighth short at the back, and the toggle in the gap there. The original's measures. */
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    static {
        for (Direction facing : Direction.values()) {
            double f = 2.0;
            VoxelShape body = Block.box(
                    facing.getStepX() > 0 ? f : 0, facing.getStepY() > 0 ? f : 0, facing.getStepZ() > 0 ? f : 0,
                    16 - (facing.getStepX() < 0 ? f : 0), 16 - (facing.getStepY() < 0 ? f : 0),
                    16 - (facing.getStepZ() < 0 ? f : 0));
            SHAPES.put(facing, Shapes.or(body, toggle(facing)));
        }
    }

    public LevitatorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP).setValue(ENABLED, true));
    }

    /** The toggle on the back, a quarter wide and a sixteenth deep. */
    private static VoxelShape toggle(Direction facing) {
        double low = 6.0;
        double high = 10.0;
        return switch (facing) {
            case DOWN -> Block.box(low, 14.0, low, high, 15.0, high);
            case UP -> Block.box(low, 1.0, low, high, 2.0, high);
            case NORTH -> Block.box(low, low, 14.0, high, high, 15.0);
            case SOUTH -> Block.box(low, low, 1.0, high, high, 2.0);
            case WEST -> Block.box(14.0, low, low, 15.0, high, high);
            case EAST -> Block.box(1.0, low, low, 2.0, high, high);
        };
    }

    @Override
    protected MapCodec<LevitatorBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ENABLED);
    }

    /** Facing back at whoever put it down, as a piston does; crouching turns it round. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getNearestLookingDirection().getOpposite();
        boolean crouching = context.getPlayer() != null && context.getPlayer().isShiftKeyDown();
        return defaultBlockState()
                .setValue(FACING, crouching ? facing.getOpposite() : facing)
                .setValue(ENABLED, !context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block from, BlockPos fromPos,
            boolean moving) {
        boolean enabled = !level.hasNeighborSignal(pos);
        if (enabled != state.getValue(ENABLED)) {
            level.setBlock(pos, state.setValue(ENABLED, enabled), Block.UPDATE_ALL);
        }
        super.neighborChanged(state, level, pos, from, fromPos, moving);
    }

    /** Something in hand is put down against it as against any block; only an empty hand works the toggle. */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        return stack.isEmpty() ? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
                : ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
    }

    /**
     * Steps the range on.
     * <p>
     * <b>Different from the original</b>, which only did this for a click on the toggle itself, a sixteenth of a
     * block deep in the gap at the back -- which on a levitator set on the ground could only be reached from the
     * side, at a slant. Here any face will do, for an empty hand.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof LevitatorBlockEntity levitator) {
            levitator.stepRange();
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                    "block.alchemia.levitator.range", levitator.range(), levitator.cost()), true);
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.WOODEN_BUTTON_CLICK_ON,
                    net.minecraft.sounds.SoundSource.BLOCKS, 0.5F, 1.0F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    /** What stands on it stands on a whole block, as it did in the original. */
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return Shapes.block();
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LevitatorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.LEVITATOR.get(), LevitatorBlockEntity::tick);
    }
}
