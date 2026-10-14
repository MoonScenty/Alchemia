package me.moonscenty.alchemia.client;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.client.legacy.LegacyRenderTypes;
import me.moonscenty.alchemia.entity.cult.GolemOrb;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * The orb as the original drew it: a six-frame crackle off the old particle sheet, cut eight by eight, red or blue by
 * the row, swelling and easing as it goes, light added to whatever is behind it.
 */
public class GolemOrbRenderer extends EntityRenderer<GolemOrb> {
    private static final ResourceLocation SHEET = Alchemia.id("textures/misc/legacy_particles.png");
    private static final float CELL = 1.0F / 8.0F;

    public GolemOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(GolemOrb orb, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.translate(0.0F, orb.getBbHeight() / 2.0F, 0.0F);
        pose.mulPose(entityRenderDispatcher.cameraOrientation());
        float size = 1.0F + (Mth.sin(orb.tickCount / 5.0F) * 0.2F + 0.2F);
        pose.scale(size, size, size);
        float u0 = (1 + orb.tickCount % 6) * CELL;
        float v0 = orb.isRed() ? 0.75F : 0.875F;
        VertexConsumer buffer = buffers.getBuffer(LegacyRenderTypes.glow(SHEET));
        Matrix4f matrix = pose.last().pose();
        corner(buffer, matrix, -0.5F, -0.5F, u0, v0 + CELL);
        corner(buffer, matrix, 0.5F, -0.5F, u0 + CELL, v0 + CELL);
        corner(buffer, matrix, 0.5F, 0.5F, u0 + CELL, v0);
        corner(buffer, matrix, -0.5F, 0.5F, u0, v0);
        pose.popPose();
        super.render(orb, yaw, partial, pose, buffers, light);
    }

    private static void corner(VertexConsumer buffer, Matrix4f matrix, float x, float y, float u, float v) {
        buffer.addVertex(matrix, x, y, 0.0F).setColor(1.0F, 1.0F, 1.0F, 0.8F).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0.0F, 1.0F, 0.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(GolemOrb orb) {
        return SHEET;
    }
}
