package me.moonscenty.alchemia.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A point of light an arcane lamp has set down in the dark, with nothing there to be seen.
 *
 * <p>It is not the game's own light block, though it is made the same way. A lamp takes back every one of these
 * round it when it is put out, and taking back the game's would take back lights somebody else had set down on
 * purpose. These are the lamp's and nobody else's.
 *
 * <p>Nothing stands in it, nothing drops from it, and anything can be built where it is.
 */
public class LampLightBlock extends Block {
    public LampLightBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }
}
