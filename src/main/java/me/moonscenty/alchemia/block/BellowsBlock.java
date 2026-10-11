package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.block.entity.BellowsBlockEntity;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
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

/**
 * A pair of bellows, blowing on whatever is in front of them.
 *
 * <p>Set against a furnace they hurry what is cooking by half again. Set against an essentia smelter they cut the
 * time it takes to boil anything down by an eighth apiece, up to three of them on its three free sides. Either
 * way they pump for as long as nothing stops them, and a redstone signal does.
 *
 * <p>They blow the way they were facing when they were put down -- the way the one putting them down was
 * looking. Crouching turns them round.
 */
public class BellowsBlock extends BaseEntityBlock {
    public static final MapCodec<BellowsBlock> CODEC = simpleCodec(BellowsBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    /** Pumping, which is what they do unless a redstone signal is holding them still. */
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

    public BellowsBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ENABLED, true));
    }

    @Override
    protected MapCodec<BellowsBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ENABLED);
    }

    /**
     * Facing the way the one putting them down was looking, so that they blow at what was clicked.
     * <p>
     * <b>Different from the original</b>, which faced them the way a piston faces -- back at whoever put them
     * down -- and turned them round for a crouch. The crouch still turns them round; it is only which way is the
     * ordinary way that has been swapped, so that putting bellows against a furnace is done by looking at it.
     */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction looking = context.getNearestLookingDirection();
        boolean crouching = context.getPlayer() != null && context.getPlayer().isShiftKeyDown();
        return defaultBlockState()
                .setValue(FACING, crouching ? looking.getOpposite() : looking)
                .setValue(ENABLED, !context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    /** A signal holds them still; losing it sets them going again. */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block from, BlockPos fromPos,
            boolean moving) {
        boolean enabled = !level.hasNeighborSignal(pos);
        if (enabled != state.getValue(ENABLED)) {
            level.setBlock(pos, state.setValue(ENABLED, enabled), Block.UPDATE_ALL);
        }
        super.neighborChanged(state, level, pos, from, fromPos, moving);
    }

    /** Drawn by their renderer, the plates and the bag between them breathing in and out. */
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BellowsBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.BELLOWS.get(),
                level.isClientSide ? BellowsBlockEntity::clientTick : BellowsBlockEntity::serverTick);
    }

    /**
     * How many bellows are blowing on this block from the given sides: facing it, and pumping.
     * <p>
     * What a smelter counts to see how much it is hurried by.
     */
    public static int blowingOn(Level level, BlockPos at, Direction... sides) {
        int count = 0;
        for (Direction side : sides) {
            BlockState there = level.getBlockState(at.relative(side));
            if (there.getBlock() instanceof BellowsBlock && there.getValue(ENABLED)
                    && there.getValue(FACING) == side.getOpposite()) {
                count++;
            }
        }
        return count;
    }
}
