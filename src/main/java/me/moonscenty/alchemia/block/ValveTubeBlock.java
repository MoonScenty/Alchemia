package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import me.moonscenty.alchemia.block.entity.ValveTubeBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

public class ValveTubeBlock extends TubeBlock {
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    // last seen redstone, so only signal edges toggle the valve and a manual turn is not undone
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

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

    @Override
    public boolean passes(BlockState state, Direction in, Direction out) {
        return state.getValue(OPEN);
    }

    @Override
    public boolean spare(BlockState state, Direction side) {
        return state.getValue(FACING) != side;
    }

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

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        turn(level, pos, state, !state.getValue(OPEN));
        return level.isClientSide ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

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
        if (level.isClientSide) {
            return;
        }
        level.setBlock(pos, state.setValue(OPEN, open), Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof ValveTubeBlockEntity valve) {
            valve.worked(level.getGameTime());
        }
        level.playSound(null, pos, ModSounds.SQUEEK.get(), SoundSource.BLOCKS, 0.7F,
                0.9F + level.random.nextFloat() * 0.2F);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ValveTubeBlockEntity(pos, state);
    }
}
