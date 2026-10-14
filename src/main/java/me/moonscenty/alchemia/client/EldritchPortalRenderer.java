package me.moonscenty.alchemia.client;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.eldritch.EldritchPortalBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * The way to the Outer Lands as the original drew it: a sixteen-frame swirl turned to face whoever looks, opening out
 * over a second and a half once the eye is opened. Shut, it is not drawn at all.
 */
public class EldritchPortalRenderer implements BlockEntityRenderer<EldritchPortalBlockEntity> {
    private static final ResourceLocation SHEET = Alchemia.id("textures/entity/eldritch_portal.png");
    private static final int FRAMES = 16;

    public EldritchPortalRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(EldritchPortalBlockEntity portal, float partial, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        if (!portal.isOpen()) {
            return;
        }
        int frame = (int) (System.nanoTime() / 50_000_000L % FRAMES);
        float u0 = frame / (float) FRAMES;
        float u1 = u0 + 1.0F / FRAMES;
        float open5 = Math.min(5.0F, portal.opened + partial);
        float open30 = Math.min(30.0F, portal.opened + partial);
        float across = open5 / 5.0F + 0.075F;
        float up = open30 / 30.0F + 0.1F;
        pose.pushPose();
        pose.translate(0.5F, 0.5F, 0.5F);
        pose.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityTranslucent(SHEET));
        Matrix4f matrix = pose.last().pose();
        corner(buffer, matrix, -across, -up, u1, 1.0F);
        corner(buffer, matrix, across, -up, u0, 1.0F);
        corner(buffer, matrix, across, up, u0, 0.0F);
        corner(buffer, matrix, -across, up, u1, 0.0F);
        pose.popPose();
    }

    private static void corner(VertexConsumer buffer, Matrix4f matrix, float x, float y, float u, float v) {
        buffer.addVertex(matrix, x, y, 0.0F).setColor(0xFFFFFFFF).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT).setNormal(0.0F, 0.0F, 1.0F);
    }
}
