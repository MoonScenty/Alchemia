package me.moonscenty.alchemia.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;

// break particles take tint 0, which here is the contents rather than the block
public final class PlainBreakParticles implements IClientBlockExtensions {
    public static final PlainBreakParticles INSTANCE = new PlainBreakParticles();

    private PlainBreakParticles() {
    }

    @Override
    public boolean areBreakingParticlesTinted(BlockState state, ClientLevel level, BlockPos pos) {
        return false;
    }
}
