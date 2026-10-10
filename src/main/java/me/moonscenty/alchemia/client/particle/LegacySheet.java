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

/**
 * The four layers the original drew its own particles in: two sheets, each either added to what is behind or laid
 * over it. Particles are drawn straight off the sheets as the original drew them, a cell at a time, rather than cut
 * up into the game's particle atlas.
 * <p>
 * As in the original, none of them writes to the depth buffer.
 */
public enum LegacySheet implements ParticleRenderType {
    /** {@code particles.png}, light added. The original's layer 0. */
    GLOW("legacy_particles", true),
    /** {@code particles.png}, laid over. The original's layer 1. */
    COVER("legacy_particles", false),
    /** {@code particles2.png}, light added. The original's layer 2. */
    GLOW_2("legacy_particles2", true),
    /** {@code particles2.png}, laid over. The original's layer 3. */
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
