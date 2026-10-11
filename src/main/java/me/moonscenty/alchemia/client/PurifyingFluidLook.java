package me.moonscenty.alchemia.client;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

/**
 * How purifying fluid is drawn: on water's own pictures, as the original drew it, tinted with the original's colour.
 *
 * <p>The original's colour was laid over the old water picture, which was blue of itself. Today's water picture is
 * grey and takes its blue from the biome, so the colour here is the original's laid over the game's ordinary water
 * blue, keeping the original's faintness.
 */
public final class PurifyingFluidLook implements IClientFluidTypeExtensions {
    public static final PurifyingFluidLook INSTANCE = new PurifyingFluidLook();

    /** The original's 0x77FFCCAA over plain water's 0x3F76E4. */
    private static final int TINT = 0x773F5E98;
    private static final ResourceLocation STILL = ResourceLocation.withDefaultNamespace("block/water_still");
    private static final ResourceLocation FLOWING = ResourceLocation.withDefaultNamespace("block/water_flow");
    private static final ResourceLocation OVERLAY = ResourceLocation.withDefaultNamespace("block/water_overlay");
    private static final ResourceLocation UNDER = ResourceLocation.withDefaultNamespace("textures/misc/underwater.png");

    private PurifyingFluidLook() {
    }

    @Override
    public ResourceLocation getStillTexture() {
        return STILL;
    }

    @Override
    public ResourceLocation getFlowingTexture() {
        return FLOWING;
    }

    @Override
    public ResourceLocation getOverlayTexture() {
        return OVERLAY;
    }

    @Override
    public ResourceLocation getRenderOverlayTexture(net.minecraft.client.Minecraft minecraft) {
        return UNDER;
    }

    @Override
    public int getTintColor() {
        return TINT;
    }
}
