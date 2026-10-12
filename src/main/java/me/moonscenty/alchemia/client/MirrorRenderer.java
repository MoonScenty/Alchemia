package me.moonscenty.alchemia.client;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.MirrorBlock;
import me.moonscenty.alchemia.block.entity.MirrorBlockEntity;
import me.moonscenty.alchemia.client.legacy.LegacyRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

/**
 * Draws a mirror as the original did: its frame, and inside it either a plain silver pane or, when it is paired and
 * someone is near enough to see, a swirl of stars behind a pane of clear glass.
 */
public class MirrorRenderer implements BlockEntityRenderer<MirrorBlockEntity> {
    private static final ResourceLocation FRAME = Alchemia.id("textures/block/legacy/mirrorframe.png");
    private static final ResourceLocation FRAME_ESSENTIA = Alchemia.id("textures/block/legacy/mirrorframe2.png");
    private static final ResourceLocation PANE = Alchemia.id("textures/block/legacy/mirrorpane.png");
    private static final ResourceLocation PANE_CLEAR = Alchemia.id("textures/block/legacy/mirrorpanetrans.png");
    /** How near one has to be to see through it, squared. */
    private static final double SEES_WITHIN = 32.0 * 32.0;
    /** The portal is inset this far from each edge, as the original's was. */
    private static final float INSET = 0.1875F;

    public MirrorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MirrorBlockEntity mirror, float partial, PoseStack pose, MultiBufferSource buffers, int light,
            int overlay) {
        Direction facing = mirror.getBlockState().getValue(MirrorBlock.FACING);
        var player = Minecraft.getInstance().player;
        boolean open = mirror.linked() && player != null
                && player.distanceToSqr(mirror.getBlockPos().getCenter()) < SEES_WITHIN;

        pose.pushPose();
        pose.translate(0.5F, 0.5F, 0.5F);
        // local up is the way the glass looks; the pane lies against what it hangs on
        pose.mulPose(new org.joml.Quaternionf().rotationTo(0.0F, 1.0F, 0.0F, facing.getStepX(), facing.getStepY(),
                facing.getStepZ()));
        if (open) {
            quad(pose, buffers.getBuffer(LegacyRenderTypes.mirrorPortal()), -0.5F + 0.01F, INSET, false, light);
            quad(pose, buffers.getBuffer(RenderType.entityTranslucent(PANE_CLEAR)), -0.5F + 0.02F, 0.0F, true, light);
        } else {
            quad(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(PANE)), -0.5F + 0.02F, 0.0F, true, light);
        }
        VertexConsumer frame = buffers.getBuffer(RenderType.entityCutoutNoCull(mirror.essentia() ? FRAME_ESSENTIA
                : FRAME));
        quad(pose, frame, -0.5F + 0.01F, 0.0F, true, light);
        quad(pose, frame, -0.5F + 0.01F + 0.0625F, 0.0F, true, light);
        pose.popPose();
    }

    /** A square lying flat at a height in the turned frame, facing up, inset from the edges by so much. */
    private static void quad(PoseStack pose, VertexConsumer buffer, float y, float inset, boolean textured,
            int light) {
        Matrix4f matrix = pose.last().pose();
        float lo = -0.5F + inset;
        float hi = 0.5F - inset;
        corner(buffer, pose, matrix, lo, y, lo, 0.0F, 0.0F, textured, light);
        corner(buffer, pose, matrix, lo, y, hi, 0.0F, 1.0F, textured, light);
        corner(buffer, pose, matrix, hi, y, hi, 1.0F, 1.0F, textured, light);
        corner(buffer, pose, matrix, hi, y, lo, 1.0F, 0.0F, textured, light);
    }

    private static void corner(VertexConsumer buffer, PoseStack pose, Matrix4f matrix, float x, float y, float z,
            float u, float v, boolean textured, int light) {
        VertexConsumer vertex = buffer.addVertex(matrix, x, y, z);
        if (textured) {
            vertex.setColor(0xFFFFFFFF).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                    .setNormal(pose.last(), 0.0F, 1.0F, 0.0F);
        }
    }
}
