package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import org.joml.Matrix4f;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aura.node.AuraNode;
import me.moonscenty.alchemia.block.entity.NodeStabilizerBlockEntity;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyCached;
import me.moonscenty.alchemia.client.legacy.model.LegacyMesh;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

public class NodeStabilizerRenderer implements BlockEntityRenderer<NodeStabilizerBlockEntity> {
    private static final ResourceLocation BUBBLE = Alchemia.id("textures/entity/node_bubble.png");
    private static final ResourceLocation LEGACY_TEXTURE = Alchemia.id("textures/entity/node_stabilizer.png");
    private static final ResourceLocation LEGACY_GLOW = Alchemia.id("textures/entity/node_stabilizer_over.png");
    private static final int GLOW_DIM = 50;
    private static final int GLOW_RANGE = 170;

    private static final float SHELL = 1.35F;

    private final Minecraft client = Minecraft.getInstance();

    public NodeStabilizerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(NodeStabilizerBlockEntity stabilizer, float partialTick, PoseStack pose,
            MultiBufferSource buffers, int light, int overlay) {
        float age = stabilizer.getLevel() == null ? 0 : stabilizer.getLevel().getGameTime() + partialTick;
        LegacyMesh legacy = MESH.get();
        if (legacy != null) {
            renderLegacy(legacy, stabilizer.reachOut(), age, pose, buffers, light, overlay);
        }
        if (stabilizer.reachOut() > 0.0F) {
            drawShellAroundHeldNode(stabilizer, pose, buffers, age);
        }
    }

    private static final LegacyCached<LegacyMesh> MESH = LegacyCached.flippedMesh(LegacyAssets.STABILIZER_MESH);

    // mesh is modelled lying down
    private static void renderLegacy(LegacyMesh mesh, float reach, float age, PoseStack pose,
            MultiBufferSource buffers, int light, int overlay) {
        int body = mesh.group("lock");
        int arm = mesh.group("piston");
        if (body < 0 || arm < 0) {
            return;
        }
        // original's grip counts to 37, an arm moving 1/100 block per count
        float count = reach * NodeStabilizerBlockEntity.STROKE;
        pose.pushPose();
        pose.translate(0.5F, 0.0F, 0.5F);
        pose.mulPose(Axis.XP.rotationDegrees(-90.0F));
        mesh.render(body, pose.last(), buffers.getBuffer(RenderType.entityCutoutNoCull(LEGACY_TEXTURE)), -1, light,
                overlay);
        for (int index = 0; index < 4; index++) {
            pose.pushPose();
            pose.mulPose(Axis.ZP.rotationDegrees(90.0F * index));
            pose.mulPose(Axis.YP.rotationDegrees(45.0F));
            pose.translate(0.0F, 0.0F, count / 100.0F);
            mesh.render(arm, pose.last(), buffers.getBuffer(RenderType.entityCutoutNoCull(LEGACY_TEXTURE)), -1,
                    light, overlay);
            float pulse = Mth.sin((age + index * 5) / 3.0F) * 0.1F + 0.9F;
            int glow = Math.min(LightTexture.FULL_BLOCK,
                    GLOW_DIM + (int) (GLOW_RANGE * (count / NodeStabilizerBlockEntity.STROKE) * pulse));
            mesh.render(arm, pose.last(), buffers.getBuffer(RenderType.entityCutoutNoCull(LEGACY_GLOW)), -1, glow,
                    overlay);
            pose.popPose();
        }
        pose.popPose();
    }

    private void drawShellAroundHeldNode(NodeStabilizerBlockEntity stabilizer, PoseStack pose,
            MultiBufferSource buffers, float age) {
        if (stabilizer.getLevel() == null) {
            return;
        }
        BlockPos at = stabilizer.getBlockPos();
        var anchor = stabilizer.anchor();
        AABB around = new AABB(anchor, anchor).inflate(3.0);

        for (AuraNode node : stabilizer.getLevel().getEntitiesOfClass(AuraNode.class, around, AuraNode::isHeld)) {
            float size = (0.15F + node.getSize() / 150.0F) * SHELL;
            int alpha = (int) (stabilizer.reachOut() * 190);
            pose.pushPose();
            pose.translate(node.getX() - at.getX(), node.getY() - at.getY(), node.getZ() - at.getZ());
            pose.mulPose(client.getEntityRenderDispatcher().cameraOrientation());
            pose.mulPose(Axis.ZP.rotationDegrees(age * 0.4F));
            drawShell(pose, buffers, size, alpha);
            pose.popPose();
            return;
        }
    }

    private static void drawShell(PoseStack pose, MultiBufferSource buffers, float size, int alpha) {
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityTranslucent(BUBBLE));
        Matrix4f matrix = pose.last().pose();
        corner(buffer, pose, matrix, -size, -size, 0.0F, 1.0F, alpha);
        corner(buffer, pose, matrix, size, -size, 1.0F, 1.0F, alpha);
        corner(buffer, pose, matrix, size, size, 1.0F, 0.0F, alpha);
        corner(buffer, pose, matrix, -size, size, 0.0F, 0.0F, alpha);
    }

    private static void corner(VertexConsumer buffer, PoseStack pose, Matrix4f matrix, float x, float y,
            float u, float v, int alpha) {
        buffer.addVertex(matrix, x, y, 0.0F)
                .setColor(255, 255, 255, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0)
                .setNormal(pose.last(), 0.0F, 0.0F, 1.0F);
    }

    @Override
    public int getViewDistance() {
        return 96;
    }

    // the shell sits at the node, away from the block
    @Override
    public AABB getRenderBoundingBox(NodeStabilizerBlockEntity stabilizer) {
        var anchor = stabilizer.anchor();
        return new AABB(anchor, anchor).inflate(4.0);
    }
}
