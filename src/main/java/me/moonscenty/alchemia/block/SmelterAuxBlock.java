package me.moonscenty.alchemia.block;

import java.util.List;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/**
 * An auxiliary slurry pump: set against the side of a smelter, it draws on what the smelter holds and pours it up
 * into vessels stacked on itself, one more column of them working off the same fire.
 * <p>
 * It does nothing of its own. The smelter looks round at every pour for pumps facing it, and pours once more for
 * each, so a smelter with three is a smelter with four columns. It goes on any side but the front, where the fire is
 * fed, and only against a smelter; once there, it stays if the smelter is taken away, as the original's did.
 */
public class SmelterAuxBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<SmelterAuxBlock> CODEC = simpleCodec(SmelterAuxBlock::new);
    /** The sides a smelter looks round, in the original's order, which settles which pump or vent goes first. */
    public static final List<Direction> SIDES = List.of(Direction.SOUTH, Direction.WEST, Direction.NORTH,
            Direction.EAST);

    public SmelterAuxBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends SmelterAuxBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /** Only on the side or back of a smelter, facing into it. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = againstSmelter(context);
        return facing == null ? null : defaultBlockState().setValue(FACING, facing);
    }

    /**
     * Which way something put down here would face the smelter it was put against, or null if it was not put
     * against a smelter, or was put against its front.
     */
    static Direction againstSmelter(BlockPlaceContext context) {
        Direction side = context.getClickedFace();
        if (!side.getAxis().isHorizontal()) {
            return null;
        }
        BlockState against = context.getLevel().getBlockState(context.getClickedPos().relative(side.getOpposite()));
        if (!(against.getBlock() instanceof EssentiaSmelterBlock)
                || against.getValue(EssentiaSmelterBlock.FACING) == side) {
            return null;
        }
        return side.getOpposite();
    }
}
