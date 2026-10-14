package me.moonscenty.alchemia.client;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.eldritch.EldritchObeliskBlockEntity;
import me.moonscenty.alchemia.client.legacy.LegacyRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

/**
 * An obelisk as the original drew it: three blocks of carved stone that let the stars show through, a capstone at
 * either end pointing out, and the whole of it bobbing gently in the air above its foot.
 */
public class EldritchObeliskRenderer implements BlockEntityRenderer<EldritchObeliskBlockEntity> {
    private static final ResourceLocation SIDE = Alchemia.id("textures/entity/obelisk_side.png");
    private static final float HEIGHT = 3.0F;

    public EldritchObeliskRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(EldritchObeliskBlockEntity obelisk, float partial, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        var viewer = Minecraft.getInstance().getCameraEntity();
        float bob = viewer == null ? 0.1F : Mth.sin((viewer.tickCount + partial) / 10.0F) * 0.1F + 0.1F;

        // the stars inside
        pose.pushPose();
        pose.translate(0.0F, 1.0F + bob, 0.0F);
        VertexConsumer stars = buffers.getBuffer(LegacyRenderTypes.mirrorPortal());
        Matrix4f matrix = pose.last().pose();
        plane(stars, matrix, 0.01F, 0.01F, 0.99F, 0.01F);
        plane(stars, matrix, 0.99F, 0.99F, 0.01F, 0.99F);
        plane(stars, matrix, 0.01F, 0.99F, 0.01F, 0.01F);
        plane(stars, matrix, 0.99F, 0.01F, 0.99F, 0.99F);
        pose.popPose();

        // the carved sides over them
        VertexConsumer sides = buffers.getBuffer(RenderType.entityTranslucent(SIDE));
        for (int i = 0; i < 4; i++) {
            pose.pushPose();
            pose.translate(0.5F, 1.0F + bob, 0.5F);
            pose.mulPose(Axis.YP.rotationDegrees(i * 90.0F));
            pose.translate(0.0F, 0.0F, -0.5F);
            side(sides, pose, light);
            pose.popPose();
        }

        // a cap at either end
        pose.pushPose();
        pose.translate(0.5F, 1.0F + bob, 0.5F);
        pose.mulPose(Axis.XP.rotationDegrees(90.0F));
        EldritchCapRenderer.drawCap(pose, buffers, EldritchCapRenderer.CAP, light, overlay);
        pose.popPose();
        pose.pushPose();
        pose.translate(0.5F, 1.0F + HEIGHT + bob, 0.5F);
        pose.mulPose(Axis.XN.rotationDegrees(90.0F));
        EldritchCapRenderer.drawCap(pose, buffers, EldritchCapRenderer.CAP, light, overlay);
        pose.popPose();
    }

    /** A plane three high from one corner to the other. */
    private static void plane(VertexConsumer buffer, Matrix4f matrix, float x0, float z0, float x1, float z1) {
        buffer.addVertex(matrix, x0, 0.0F, z0);
        buffer.addVertex(matrix, x0, HEIGHT, z0);
        buffer.addVertex(matrix, x1, HEIGHT, z1);
        buffer.addVertex(matrix, x1, 0.0F, z1);
    }

    private static void side(VertexConsumer buffer, PoseStack pose, int light) {
        Matrix4f matrix = pose.last().pose();
        corner(buffer, pose, matrix, -0.5F, 0.0F, 1.0F, 1.0F, light);
        corner(buffer, pose, matrix, 0.5F, 0.0F, 0.0F, 1.0F, light);
        corner(buffer, pose, matrix, 0.5F, HEIGHT, 0.0F, 0.0F, light);
        corner(buffer, pose, matrix, -0.5F, HEIGHT, 1.0F, 0.0F, light);
    }

    private static void corner(VertexConsumer buffer, PoseStack pose, Matrix4f matrix, float x, float y, float u,
            float v, int light) {
        buffer.addVertex(matrix, x, y, 0.0F).setColor(0xFFFFFFFF).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(pose.last(), 0.0F, 0.0F, -1.0F);
    }

    @Override
    public AABB getRenderBoundingBox(EldritchObeliskBlockEntity obelisk) {
        return obelisk.getRenderBoundingBox();
    }

    @Override
    public int getViewDistance() {
        return 96;
    }
}
