package me.moonscenty.alchemia.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;

/**
 * Particles that add their light to what is behind them instead of covering it.
 * <p>
 * The game has no such sheet. Everything it draws particles with either lays them over the world or writes them
 * into the depth of it, and both are right for smoke and snow and wrong for light: two motes of smoke on top of
 * each other are still smoke, and two motes of light are brighter light. Adding is the whole of why a handful of
 * them gathered at one spot burns white in the middle and fades to colour at the rim, which is what a flame
 * looks like and what no amount of laying one sprite over another will give.
 * <p>
 * Nothing is written to the depth buffer. A light does not hide what is behind it.
 */
public final class GlowSheet implements ParticleRenderType {
    public static final GlowSheet INSTANCE = new GlowSheet();

    private GlowSheet() {
    }

    @Override
    public BufferBuilder begin(Tesselator tesselator, TextureManager textures) {
        RenderSystem.depthMask(false);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
                com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE);
        return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
    }

    @Override
    public String toString() {
        return "alchemia:glow";
    }
}
