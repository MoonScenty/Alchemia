package me.moonscenty.alchemia.block.taint;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A block the taint has made. When the flux that fed it runs out it dies, and what is left behind is up to it.
 */
public interface Tainted {
    void die(ServerLevel level, BlockPos pos, BlockState state);
}
