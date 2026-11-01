package me.moonscenty.alchemia.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

public enum LegacySheet implements ParticleRenderType {
    GLOW("legacy_particles", true),
    COVER("legacy_particles", false),
    GLOW_2("legacy_particles2", true),
    COVER_2("legacy_particles2", false);

    private final ResourceLocation texture;
    private final boolean adds;

    LegacySheet(String sheet, boolean adds) {
        this.texture = Alchemia.id("textures/misc/" + sheet + ".png");
        this.adds = adds;
    }

    @Override
    public BufferBuilder begin(Tesselator tesselator, TextureManager textures) {
        RenderSystem.depthMask(false);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                adds ? GlStateManager.DestFactor.ONE : GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
    }

    @Override
    public String toString() {
        return "alchemia:legacy_" + name().toLowerCase(java.util.Locale.ROOT);
    }
}
