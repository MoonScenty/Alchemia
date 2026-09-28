package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A length of pipe that can be shut.
 * <p>
 * It is worked by hand or by redstone, and the two do not fight: a signal arriving shuts it and a signal leaving
 * opens it again, while a click in between does as it says for as long as nothing else happens. That is why the
 * power is remembered in the state — without it there would be no telling a signal that has just arrived from one
 * that has been there all along, and every redstone update would undo the click.
 */
public class ValveTubeBlock extends TubeBlock {
    /** Whether anything gets through. */
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    /** What the redstone was doing last time anyone looked. */
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    /** Which side the handle stands on. Nothing joins to that side: a handle is not a socket. */
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public static final MapCodec<ValveTubeBlock> CODEC = simpleCodec(ValveTubeBlock::new);

    public ValveTubeBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(OPEN, true)
                .setValue(POWERED, false)
                .setValue(FACING, Direction.UP));
    }

    @Override
    protected MapCodec<ValveTubeBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(OPEN, POWERED, FACING);
    }

    /** A shut valve is not a way through, whichever way you came at it. */
    @Override
    public boolean passes(BlockState state, Direction in, Direction out) {
        return state.getValue(OPEN);
    }

    /** The side a handle stands on is no use for joining, so it is worked out before the sides are. */
    @Override
    public boolean spare(BlockState state, Direction side) {
        return state.getValue(FACING) != side;
    }

    /**
     * Laid with the handle towards whoever laid it.
     * <p>
     * A valve is a thing you come back to and turn, so the handle faces the way you were standing. It is worked
     * out before the sides are, since the side it lands on stops being a side at all.
     */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean powered = context.getLevel().hasNeighborSignal(context.getClickedPos());
        BlockState stood = defaultBlockState()
                .setValue(FACING, context.getNearestLookingDirection().getOpposite())
                .setValue(POWERED, powered)
                .setValue(OPEN, !powered);
        for (Direction side : Direction.values()) {
            stood = stood.setValue(SIDES.get(side),
                    linkTo(context.getLevel(), context.getClickedPos(), side, stood));
        }
        return stood;
    }

    /** Opened and shut by hand. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        turn(level, pos, state, !state.getValue(OPEN));
        return level.isClientSide ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    /** Power arriving shuts it; power leaving opens it. Nothing happens while the signal merely stays as it was. */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbour, BlockPos from,
            boolean moving) {
        if (level.isClientSide) {
            return;
        }
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(POWERED)) {
            turn(level, pos, state.setValue(POWERED, powered), !powered);
        }
    }

    private static void turn(Level level, BlockPos pos, BlockState state, boolean open) {
        if (!level.isClientSide) {
            level.setBlock(pos, state.setValue(OPEN, open), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.4F, open ? 0.6F : 0.5F);
        }
    }
}
