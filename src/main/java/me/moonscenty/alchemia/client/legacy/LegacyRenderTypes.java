package me.moonscenty.alchemia.client.legacy;

import java.util.function.Function;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

// extends RenderType only to reach its protected state shards
public final class LegacyRenderTypes extends RenderType {
    // GL_SRC_ALPHA, GL_ONE: vanilla's eyes add full colour regardless of alpha
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

    private static final RenderType MIRROR_PORTAL = create("alchemia_mirror_portal", DefaultVertexFormat.POSITION,
            VertexFormat.Mode.QUADS, 256, false, false, CompositeState.builder()
                    .setShaderState(RENDERTYPE_END_PORTAL_SHADER)
                    .setTextureState(MultiTextureStateShard.builder()
                            .add(me.moonscenty.alchemia.Alchemia.id("textures/misc/mirror_tunnel.png"), false, false)
                            .add(me.moonscenty.alchemia.Alchemia.id("textures/misc/mirror_field.png"), false, false)
                            .build())
                    .createCompositeState(false));

    private static final java.util.function.BiFunction<ResourceLocation, Boolean, RenderType> SEAL = Util.memoize(
            (texture, throughWalls) -> create("alchemia_seal", DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS, 1536, false, true, CompositeState.builder()
                            .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                            .setTextureState(new TextureStateShard(texture, false, false))
                            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                            .setCullState(NO_CULL)
                            .setLightmapState(LIGHTMAP)
                            .setOverlayState(OVERLAY)
                            .setWriteMaskState(COLOR_WRITE)
                            .setDepthTestState(throughWalls ? NO_DEPTH_TEST : LEQUAL_DEPTH_TEST)
                            .createCompositeState(false)));

    private LegacyRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int size, boolean crumbling,
            boolean sorted, Runnable setup, Runnable clear) {
        super(name, format, mode, size, crumbling, sorted, setup, clear);
    }

    public static RenderType mirrorPortal() {
        return MIRROR_PORTAL;
    }

    public static RenderType glow(ResourceLocation texture) {
        return GLOW.apply(texture);
    }

    public static RenderType seal(ResourceLocation texture, boolean throughWalls) {
        return SEAL.apply(texture, throughWalls);
    }

    public static RenderType glowBothSides(ResourceLocation texture) {
        return GLOW_BOTH_SIDES.apply(texture);
    }
}
