package me.moonscenty.alchemia.client;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

public final class PurifyingFluidLook implements IClientFluidTypeExtensions {
    public static final PurifyingFluidLook INSTANCE = new PurifyingFluidLook();

    // original's 0x77FFCCAA over plain water's 0x3F76E4
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
