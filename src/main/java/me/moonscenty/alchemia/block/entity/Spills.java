package me.moonscenty.alchemia.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface Spills {
    void spill();

    static void onRemove(BlockState state, BlockState replacement, Level level, BlockPos pos) {
        if (!state.is(replacement.getBlock()) && level.getBlockEntity(pos) instanceof Spills spills) {
            spills.spill();
        }
    }
}
