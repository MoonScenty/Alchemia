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

    private static final Function<ResourceLocation, RenderType> GLOW_BOTH_SIDES = Util.memoize(texture -> create(
            "alchemia_legacy_glow_both_sides", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, false,
            true, CompositeState.builder()
                    .setShaderState(RENDERTYPE_EYES_SHADER)
                    .setTextureState(new TextureStateShard(texture, false, false))
                    .setTransparencyState(ADD_BY_ALPHA)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)));

    /**
     * The swirl seen in a paired mirror: the original drew it the way the game draws the end portal, layer on
     * layer, with two pictures of its own, and the game's end-portal shader does the same with whichever two it is
     * given.
     */
    private static final RenderType MIRROR_PORTAL = create("alchemia_mirror_portal", DefaultVertexFormat.POSITION,
            VertexFormat.Mode.QUADS, 256, false, false, CompositeState.builder()
                    .setShaderState(RENDERTYPE_END_PORTAL_SHADER)
                    .setTextureState(MultiTextureStateShard.builder()
                            .add(me.moonscenty.alchemia.Alchemia.id("textures/misc/mirror_tunnel.png"), false, false)
                            .add(me.moonscenty.alchemia.Alchemia.id("textures/misc/mirror_field.png"), false, false)
                            .build())
                    .createCompositeState(false));

    private LegacyRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int size, boolean crumbling,
            boolean sorted, Runnable setup, Runnable clear) {
        super(name, format, mode, size, crumbling, sorted, setup, clear);
    }

    public static RenderType mirrorPortal() {
        return MIRROR_PORTAL;
    }

    /** A glow laid over a model at full brightness, faded by its own picture's alpha and the colour's. */
    public static RenderType glow(ResourceLocation texture) {
        return GLOW.apply(texture);
    }

    /** {@link #glow}, seen from behind as well as in front, for sheets hung in the air rather than laid on a model. */
    public static RenderType glowBothSides(ResourceLocation texture) {
        return GLOW_BOTH_SIDES.apply(texture);
    }
}
