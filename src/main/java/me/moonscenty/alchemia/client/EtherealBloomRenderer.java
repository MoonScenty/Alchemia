package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import org.joml.Matrix4f;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.entity.EtherealBloomBlockEntity;
import me.moonscenty.alchemia.client.legacy.LegacyRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Draws an ethereal bloom as the original's {@code TileEtherealBloomRenderer} did, growing in over its first five
 * seconds: a pale glow from the node sheet over it, a small crystal at its heart, two rings of four leaves, and four
 * blades of stalk at its foot, the leaves stirring a little all the while.
 */
public class EtherealBloomRenderer implements BlockEntityRenderer<EtherealBloomBlockEntity> {
    private static final ResourceLocation GLOW = Alchemia.id("textures/entity/bloom_glow.png");
    private static final ResourceLocation CRYSTAL = Alchemia.id("textures/entity/crystal_capacitor.png");
    private static final ResourceLocation LEAVES = Alchemia.id("textures/entity/bloom_leaves.png");
    private static final ResourceLocation STALK = Alchemia.id("textures/entity/bloom_stalk.png");
    private static final int GLOW_FRAMES = 32;
    private static final int GLOW_COLOUR = 0xAADDFF;
    /** The original drew its leaves at a brightness of 200 of 240. */
    private static final int LEAF_LIGHT = 0xC800C8;

    private final ModelPart cube;

    public EtherealBloomRenderer(BlockEntityRendererProvider.Context context) {
        // the original's ModelCube: a whole block of a cube, on a 64 by 32 sheet
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(-8.0F, -8.0F, -8.0F, 16, 16, 16), PartPose.offset(8.0F, 8.0F, 8.0F));
        this.cube = LayerDefinition.create(mesh, 64, 32).bakeRoot();
    }

    @Override
    public void render(EtherealBloomBlockEntity bloom, float partial, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        float rc1 = bloom.growthCounter + partial;
        float rc2 = Math.min(rc1, 50.0F);
        float rc3 = Mth.clamp(rc1 - 33.0F, 0.0F, 33.0F);
        float rc4 = Mth.clamp(rc1 - 66.0F, 0.0F, 33.0F);
        rc1 = Math.min(rc1, 100.0F);
        float scale1 = rc1 / 100.0F;
        float scale2 = rc2 / 60.0F + 0.1666666F;
        float scale3 = rc3 / 33.0F;
        float scale4 = rc4 / 33.0F * 0.7F;

        // the glow, facing whoever looks at it
        pose.pushPose();
        pose.translate(0.5, scale1, 0.5);
        pose.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        facing(pose, buffers.getBuffer(LegacyRenderTypes.glowBothSides(GLOW)), bloom.counter % GLOW_FRAMES, scale1,
                scale1);
        pose.popPose();

        // the crystal at its heart
        pose.pushPose();
        pose.translate(0.5 - scale4 / 8.0F, scale1 - scale4 / 6.0F, 0.5 - scale4 / 8.0F);
        pose.scale(scale4 / 4.0F, scale4 / 3.0F, scale4 / 4.0F);
        cube.render(pose, buffers.getBuffer(LegacyRenderTypes.glow(CRYSTAL)), 0xF000F0, OverlayTexture.NO_OVERLAY);
        pose.popPose();

        float r1 = Mth.sin((bloom.counter + partial) / 12.0F) * 2.0F;
        float r2 = Mth.sin((bloom.counter + partial) / 11.0F) * 2.0F;
        VertexConsumer leaves = buffers.getBuffer(RenderType.entityTranslucent(LEAVES));
        ring(pose, leaves, 0.25, 0.0F, scale3, scale1, scale3, r1, r2);
        ring(pose, leaves, 0.6, 45.0F, scale4, scale1 * 0.7F, scale4, r2, r1);

        // and the stalk, four blades standing out from the middle
        VertexConsumer stalk = buffers.getBuffer(RenderType.entityTranslucent(STALK));
        pose.pushPose();
        pose.translate(0.5, 0.0, 0.5);
        pose.mulPose(Axis.ZP.rotationDegrees(90.0F));
        pose.mulPose(Axis.XP.rotationDegrees(180.0F));
        for (int a = 0; a < 4; a++) {
            pose.pushPose();
            pose.translate(scale1 / 2.0F, 0.0, 0.0);
            pose.scale(scale1, scale2, scale2);
            pose.mulPose(Axis.XP.rotationDegrees(90 * a));
            centred(pose, stalk);
            pose.popPose();
        }
        pose.popPose();
    }

    /** Four leaves round the stem at some height, each turned a quarter from the last and stirring. */
    private static void ring(PoseStack pose, VertexConsumer buffer, double height, float turn, float sx, float sy,
            float sz, float first, float second) {
        pose.pushPose();
        pose.translate(0.5, height, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(turn));
        pose.mulPose(Axis.ZP.rotationDegrees(90.0F));
        pose.mulPose(Axis.XP.rotationDegrees(180.0F));
        for (int a = 0; a < 4; a++) {
            pose.pushPose();
            pose.scale(sx, sy, sz);
            pose.mulPose(Axis.XP.rotationDegrees(90 * a));
            pose.mulPose(Axis.YP.rotationDegrees(first));
            pose.mulPose(Axis.ZP.rotationDegrees(second));
            centred(pose, buffer);
            pose.popPose();
        }
        pose.popPose();
    }

    /** The original's centred quad: a unit square about the origin in the XY plane. */
    private static void centred(PoseStack pose, VertexConsumer buffer) {
        Matrix4f matrix = pose.last().pose();
        vertex(buffer, matrix, -0.5F, 0.5F, 1.0F, 1.0F, 0xFF, LEAF_LIGHT);
        vertex(buffer, matrix, 0.5F, 0.5F, 1.0F, 0.0F, 0xFF, LEAF_LIGHT);
        vertex(buffer, matrix, 0.5F, -0.5F, 0.0F, 0.0F, 0xFF, LEAF_LIGHT);
        vertex(buffer, matrix, -0.5F, -0.5F, 0.0F, 1.0F, 0xFF, LEAF_LIGHT);
    }

    /** One frame of the glow, as big as the bloom has grown and as bright. */
    private static void facing(PoseStack pose, VertexConsumer buffer, int frame, float size, float alpha) {
        Matrix4f matrix = pose.last().pose();
        float u0 = frame / (float) GLOW_FRAMES;
        float u1 = (frame + 1) / (float) GLOW_FRAMES;
        int opacity = (int) (Mth.clamp(alpha, 0.0F, 1.0F) * 255);
        glowVertex(buffer, matrix, -size, -size, u0, 1.0F, opacity);
        glowVertex(buffer, matrix, size, -size, u1, 1.0F, opacity);
        glowVertex(buffer, matrix, size, size, u1, 0.0F, opacity);
        glowVertex(buffer, matrix, -size, size, u0, 0.0F, opacity);
    }

    private static void glowVertex(VertexConsumer buffer, Matrix4f matrix, float x, float y, float u, float v,
            int alpha) {
        buffer.addVertex(matrix, x, y, 0.0F)
                .setColor((GLOW_COLOUR >> 16) & 0xFF, (GLOW_COLOUR >> 8) & 0xFF, GLOW_COLOUR & 0xFF, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0)
                .setNormal(0.0F, 1.0F, 0.0F);
    }

    private static void vertex(VertexConsumer buffer, Matrix4f matrix, float x, float y, float u, float v, int alpha,
            int light) {
        buffer.addVertex(matrix, x, y, 0.0F)
                .setColor(0xFF, 0xFF, 0xFF, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0.0F, 0.0F, 1.0F);
    }
}
