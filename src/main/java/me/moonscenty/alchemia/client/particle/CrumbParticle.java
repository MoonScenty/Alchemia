package me.moonscenty.alchemia.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.item.ItemStack;

/**
 * A crumb of an item that hops up, falls and fades out, as the original's {@code FXBreakingFade}: the game's own
 * crumb of a broken item, tinted, and fading evenly over however long it was given.
 */
public class CrumbParticle extends TextureSheetParticle {
    private final float uo;
    private final float vo;
    private final float opacity;

    public CrumbParticle(ClientLevel level, double x, double y, double z, ItemStack item, float r, float g, float b,
            float alpha, int lasts) {
        super(level, x, y, z, 0.0, 0.0, 0.0);
        setSprite(Minecraft.getInstance().getItemRenderer().getModel(item, level, null, 0).getParticleIcon());
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
        this.opacity = alpha;
        this.alpha = alpha;
        this.gravity = 1.0F;
        this.quadSize /= 2.0F;
        this.lifetime = lasts;
        this.uo = random.nextFloat() * 3.0F;
        this.vo = random.nextFloat() * 3.0F;
    }

    @Override
    public void tick() {
        super.tick();
        alpha = opacity * (1.0F - age / (float) lifetime);
    }

    /** The game's crumbs are drawn solid; these fade, so they are laid over the world instead. */
    private static final ParticleRenderType SEE_THROUGH = new ParticleRenderType() {
        @Override
        public BufferBuilder begin(Tesselator tesselator, TextureManager textures) {
            RenderSystem.depthMask(false);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public String toString() {
            return "alchemia:crumb";
        }
    };

    @Override
    public ParticleRenderType getRenderType() {
        return SEE_THROUGH;
    }

    // a random quarter of the item's picture, as the game's own crumbs are

    @Override
    protected float getU0() {
        return sprite.getU((uo + 1.0F) / 4.0F);
    }

    @Override
    protected float getU1() {
        return sprite.getU(uo / 4.0F);
    }

    @Override
    protected float getV0() {
        return sprite.getV(vo / 4.0F);
    }

    @Override
    protected float getV1() {
        return sprite.getV((vo + 1.0F) / 4.0F);
    }
}
