package me.moonscenty.alchemia.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** A block entity whose essentia goes into the air as flux when its block is broken. */
public interface Spills {
    void spill();

    /** For a block's onRemove: spills what is there, unless the block is only changing state. */
    static void onRemove(BlockState state, BlockState replacement, Level level, BlockPos pos) {
        if (!state.is(replacement.getBlock()) && level.getBlockEntity(pos) instanceof Spills spills) {
            spills.spill();
        }
    }
}
