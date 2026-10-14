package me.moonscenty.alchemia.client;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

/** How liquid death is drawn: the original's shifting glow, still and flowing alike, in the original's purple. */
public final class LiquidDeathLook implements IClientFluidTypeExtensions {
    public static final LiquidDeathLook INSTANCE = new LiquidDeathLook();

    private static final ResourceLocation GLOW = Alchemia.id("block/legacy/liquid_death");
    /** The original's 0xF0440099. */
    private static final int TINT = 0xF0440099;

    private LiquidDeathLook() {
    }

    @Override
    public ResourceLocation getStillTexture() {
        return GLOW;
    }

    @Override
    public ResourceLocation getFlowingTexture() {
        return GLOW;
    }

    @Override
    public int getTintColor() {
        return TINT;
    }
}
