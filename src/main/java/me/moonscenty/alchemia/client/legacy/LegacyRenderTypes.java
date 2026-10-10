package me.moonscenty.alchemia.client.legacy;

import java.util.function.Function;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * Ways of drawing the original used that the game has no ready name for.
 *
 * <p>Only a subclass may reach the pieces a render type is put together from, hence the extending; nothing here is a
 * render type of its own.
 */
public final class LegacyRenderTypes extends RenderType {
    /**
     * Light added over what is already there, as much as the picture is opaque: the original's
     * {@code glBlendFunc(GL_SRC_ALPHA, GL_ONE)}. The game's own glowing eyes add the whole colour whatever the
     * picture's alpha, which turns a soft half-clear glow into a hard bright one.
     */
    private static final TransparencyStateShard ADD_BY_ALPHA = new TransparencyStateShard("alchemia_add_by_alpha",
            () -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
                        com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE);
            }, () -> {
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            });

    private static final Function<ResourceLocation, RenderType> GLOW = Util.memoize(texture -> create(
            "alchemia_legacy_glow", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, false, true,
            CompositeState.builder()
                    .setShaderState(RENDERTYPE_EYES_SHADER)
                    .setTextureState(new TextureStateShard(texture, false, false))
                    .setTransparencyState(ADD_BY_ALPHA)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)));

    private LegacyRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int size, boolean crumbling,
            boolean sorted, Runnable setup, Runnable clear) {
        super(name, format, mode, size, crumbling, sorted, setup, clear);
    }

    /** A glow laid over a model at full brightness, faded by its own picture's alpha and the colour's. */
    public static RenderType glow(ResourceLocation texture) {
        return GLOW.apply(texture);
    }
}
