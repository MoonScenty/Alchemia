package me.moonscenty.alchemia.client;

import javax.annotation.Nullable;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.GolemPressBlock;
import me.moonscenty.alchemia.block.entity.GolemPressBlockEntity;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
import me.moonscenty.alchemia.client.legacy.model.LegacyMesh;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

/**
 * Draws a golem press as the original did: the whole two-by-two of it out of one mesh, turned to face its table,
 * with the press itself coming down as it works and a pool of lava glowing in the cauldron.
 */
public class GolemPressRenderer implements BlockEntityRenderer<GolemPressBlockEntity> {
    private static final ResourceLocation SHEET = Alchemia.id("textures/entity/golem/golem_press.png");
    private static final String PRESS = "press";
    /** How far the press travels at the bottom of its swing. */
    private static final double TRAVEL = 0.625;
    /** The cauldron's lava, which glows. */
    private static final int LAVA_LIGHT = 200;
    private static final float LAVA_SIZE = 0.625F;

    public GolemPressRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(GolemPressBlockEntity press, float partialTick, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        LegacyMesh mesh = mesh();
        if (mesh == null) {
            return;
        }
        pose.pushPose();
        pose.translate(0.5F, 0.0F, 0.5F);
        switch (press.getBlockState().getValue(GolemPressBlock.FACING)) {
            case NORTH -> pose.mulPose(Axis.YP.rotationDegrees(90.0F));
            case SOUTH -> pose.mulPose(Axis.YP.rotationDegrees(270.0F));
            case WEST -> pose.mulPose(Axis.YP.rotationDegrees(180.0F));
            default -> {
                // east is how the mesh was modelled
            }
        }
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(SHEET));
        int moving = mesh.group(PRESS);
        for (int group = 0; group < mesh.groups(); group++) {
            if (group != moving) {
                mesh.render(group, pose.last(), consumer, -1, light, OverlayTexture.NO_OVERLAY);
            }
        }
        if (moving >= 0) {
            pose.pushPose();
            pose.translate(0.0, -Math.sin(Math.toRadians(press.press)) * TRAVEL, 0.0);
            mesh.render(moving, pose.last(), consumer, -1, light, OverlayTexture.NO_OVERLAY);
            pose.popPose();
        }
        lava(pose, buffers);
        pose.popPose();
    }

    /** A square of still lava over the cauldron, drawn the way the original laid it. */
    private static void lava(PoseStack pose, MultiBufferSource buffers) {
        TextureAtlasSprite lava = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper()
                .getParticleIcon(Blocks.LAVA.defaultBlockState());
        pose.pushPose();
        pose.translate(-0.3125F, 0.625F, 1.3125F);
        pose.mulPose(Axis.XN.rotationDegrees(90.0F));
        pose.scale(LAVA_SIZE, LAVA_SIZE, LAVA_SIZE);
        PoseStack.Pose at = pose.last();
        Matrix4f matrix = at.pose();
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));
        corner(consumer, matrix, at, 0.0F, 0.0F, lava.getU1(), lava.getV1());
        corner(consumer, matrix, at, 1.0F, 0.0F, lava.getU0(), lava.getV1());
        corner(consumer, matrix, at, 1.0F, 1.0F, lava.getU0(), lava.getV0());
        corner(consumer, matrix, at, 0.0F, 1.0F, lava.getU1(), lava.getV0());
        pose.popPose();
    }

    private static void corner(VertexConsumer consumer, Matrix4f matrix, PoseStack.Pose at, float x, float y,
            float u, float v) {
        consumer.addVertex(matrix, x, y, 0.0F).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LAVA_LIGHT).setNormal(at, 0.0F, 0.0F, 1.0F);
    }

    /** It reaches over all four blocks of the press, and the bars above. */
    @Override
    public AABB getRenderBoundingBox(GolemPressBlockEntity press) {
        return new AABB(press.getBlockPos()).inflate(1.0, 0.0, 1.0).expandTowards(0.0, 1.0, 0.0);
    }

    private static int meshFrom = -1;
    @Nullable
    private static LegacyMesh mesh;

    @Nullable
    private static LegacyMesh mesh() {
        int generation = LegacyModels.generation();
        if (generation != meshFrom) {
            meshFrom = generation;
            mesh = LegacyModels.mesh("models/legacy/golem/golem_press.obj").map(LegacyMesh::flippedV).orElse(null);
        }
        return mesh;
    }
}
