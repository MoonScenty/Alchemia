package me.moonscenty.alchemia.block;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/** A pump or vent fixed to the side or back of an essentia smelter, facing into it. */
public abstract class SmelterSideBlock extends HorizontalDirectionalBlock {
    protected SmelterSideBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /** Only on the side or back of a smelter, facing into it. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction side = context.getClickedFace();
        if (!side.getAxis().isHorizontal()) {
            return null;
        }
        BlockState against = context.getLevel().getBlockState(context.getClickedPos().relative(side.getOpposite()));
        if (!(against.getBlock() instanceof EssentiaSmelterBlock)
                || against.getValue(EssentiaSmelterBlock.FACING) == side) {
            return null;
        }
        return defaultBlockState().setValue(FACING, side.getOpposite());
    }
}
