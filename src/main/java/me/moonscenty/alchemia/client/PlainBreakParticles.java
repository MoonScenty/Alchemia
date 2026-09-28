package me.moonscenty.alchemia.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;

/**
 * For blocks whose first tint is the stuff standing inside them rather than the block itself.
 * <p>
 * A breaking chip is a scrap of the block's particle picture, and the game dyes it with whatever colour the block
 * gives out at tint zero. That is meant for things like grass and leaves, where the block and its tint are the
 * same thing. Here tint zero is the liquid in the pot or the jar, or the aspect a filter is set to, so the chips
 * came off stone and glass wearing the colour of what was inside -- blue rubble from a crucible. This says the
 * chips are not to be dyed, and the picture is left as it was drawn.
 */
public final class PlainBreakParticles implements IClientBlockExtensions {
    public static final PlainBreakParticles INSTANCE = new PlainBreakParticles();

    private PlainBreakParticles() {
    }

    @Override
    public boolean areBreakingParticlesTinted(BlockState state, ClientLevel level, BlockPos pos) {
        return false;
    }
}
