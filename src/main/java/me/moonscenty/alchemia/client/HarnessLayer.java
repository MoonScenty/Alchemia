package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
import me.moonscenty.alchemia.client.legacy.model.LegacyMesh;
import me.moonscenty.alchemia.item.Hover;
import me.moonscenty.alchemia.item.ThaumostaticHarnessItem;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * What the original's {@code ModelHoverHarness} drew over the plain harness: the pack on the back, the original's
 * mesh, and while it is switched on two rings of lightning turning behind it.
 */
public class HarnessLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation PACK = Alchemia.id("textures/entity/hover_harness_pack.png");
    private static final ResourceLocation RING = Alchemia.id("textures/entity/lightning_ring.png");
    private static final int RING_FRAMES = 16;
    /** How far the original tipped everything forward for a crouch: half a radian. */
    private static final float CROUCH = 28.64789F;

    private static int bakedFrom = -1;
    private static LegacyMesh mesh;

    public HarnessLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    private static boolean refresh() {
        if (bakedFrom != LegacyModels.generation()) {
            bakedFrom = LegacyModels.generation();
            mesh = LegacyModels.mesh(LegacyAssets.HARNESS_MESH).map(LegacyMesh::flippedV).orElse(null);
        }
        return mesh != null;
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player,
            float limbSwing, float limbSwingAmount, float partial, float age, float headYaw, float headPitch) {
        ItemStack harness = Hover.worn(player);
        if (harness.isEmpty() || !refresh()) {
            return;
        }
        boolean crouched = player.isCrouching();
        pose.pushPose();
        pose.scale(0.1F, 0.1F, 0.1F);
        pose.mulPose(Axis.XN.rotationDegrees(90.0F));
        if (crouched) {
            pose.mulPose(Axis.XP.rotationDegrees(CROUCH));
        }
        pose.translate(0.0F, 0.33F, -3.7F);
        mesh.render(pose.last(), buffers.getBuffer(RenderType.entityCutoutNoCull(PACK)), -1, light,
                OverlayTexture.NO_OVERLAY);
        pose.popPose();

        if (!ThaumostaticHarnessItem.hovering(harness)) {
            return;
        }
        pose.pushPose();
        if (crouched) {
            pose.mulPose(Axis.XP.rotationDegrees(CROUCH));
            pose.translate(0.0F, 0.075F, -0.05F);
        }
        pose.translate(0.0F, 0.2F, 0.55F);
        VertexConsumer glow = buffers.getBuffer(RenderType.eyes(RING));
        ring(pose, glow, player.tickCount % RING_FRAMES, 1.25F, 0xFFFFFFFF);
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(180.0F));
        pose.translate(0.0F, 0.0F, 0.03F);
        ring(pose, glow, (player.tickCount + 8) % RING_FRAMES, 0.75F, 0xFFFF80FF);
        pose.popPose();
        pose.popPose();
    }

    /** One frame of the ring, square, facing out of the back. */
    private static void ring(PoseStack pose, VertexConsumer buffer, int frame, float half, int colour) {
        float u0 = frame / (float) RING_FRAMES;
        float u1 = (frame + 1) / (float) RING_FRAMES;
        PoseStack.Pose at = pose.last();
        vertex(buffer, at, -half, -half, u0, 1.0F, colour);
        vertex(buffer, at, half, -half, u1, 1.0F, colour);
        vertex(buffer, at, half, half, u1, 0.0F, colour);
        vertex(buffer, at, -half, half, u0, 0.0F, colour);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float u, float v,
            int colour) {
        buffer.addVertex(pose, x, y, 0.0F).setColor(colour).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0.0F, 0.0F, 1.0F);
    }
}
