package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import org.joml.Matrix4f;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aura.node.AuraNode;
import me.moonscenty.alchemia.block.entity.NodeStabilizerBlockEntity;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
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

/**
 * Draws a stabiliser, all of it, as the original drew it in its renderer: its body, and four arms that push out as
 * it takes hold of a node; and the shell it keeps around whatever it is holding.
 */
public class NodeStabilizerRenderer implements BlockEntityRenderer<NodeStabilizerBlockEntity> {
    private static final ResourceLocation BUBBLE = Alchemia.id("textures/entity/node_bubble.png");
    /** The original's sheet for its body and arms, and the glow over the arms. */
    private static final ResourceLocation LEGACY_TEXTURE = Alchemia.id("textures/entity/node_stabilizer.png");
    private static final ResourceLocation LEGACY_GLOW = Alchemia.id("textures/entity/node_stabilizer_over.png");
    /** The block light the original gave the glow when idle, and how much more a full grip added. */
    private static final int GLOW_DIM = 50;
    private static final int GLOW_RANGE = 170;

    /** How much wider than the node the shell around it is drawn. */
    private static final float SHELL = 1.35F;

    private final Minecraft client = Minecraft.getInstance();

    public NodeStabilizerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(NodeStabilizerBlockEntity stabilizer, float partialTick, PoseStack pose,
            MultiBufferSource buffers, int light, int overlay) {
        float age = stabilizer.getLevel() == null ? 0 : stabilizer.getLevel().getGameTime() + partialTick;
        LegacyMesh legacy = legacyMesh();
        if (legacy != null) {
            renderLegacy(legacy, stabilizer.reachOut(), age, pose, buffers, light, overlay);
        }
        if (stabilizer.reachOut() > 0.0F) {
            drawShellAroundHeldNode(stabilizer, pose, buffers, age);
        }
    }

    /** The original's mesh as of the import {@link #meshFrom} names, turned over for the loader that drew it. */
    private static int meshFrom = -1;
    private static LegacyMesh mesh;

    /**
     * The original's mesh, once per import. Its renderer read it with the older loader, which turned the texture
     * corners over, so it is turned over here too.
     */
    private static LegacyMesh legacyMesh() {
        int generation = LegacyModels.generation();
        if (generation != meshFrom) {
            meshFrom = generation;
            mesh = LegacyModels.mesh(LegacyAssets.STABILIZER_MESH).map(LegacyMesh::flippedV).orElse(null);
        }
        return mesh;
    }

    /**
     * The stabiliser as the original's renderer drew it, all of it: the mesh is modelled lying down, so it is stood
     * up first; then its body, and four arms a quarter turn apart, each leant over by forty-five degrees and pushed
     * out along its own length as the stabiliser takes hold. Each arm is drawn twice, the second time in its glow,
     * lit brighter the harder it is working and breathing a little out of step with the next.
     */
    private static void renderLegacy(LegacyMesh mesh, float reach, float age, PoseStack pose,
            MultiBufferSource buffers, int light, int overlay) {
        int body = mesh.group("lock");
        int arm = mesh.group("piston");
        if (body < 0 || arm < 0) {
            return;
        }
        // the original counted its grip to thirty-seven, and moved an arm a hundredth of a block for each
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
            // the original lit the glow by hand, as block light alone, from dim to nearly full
            int glow = Math.min(LightTexture.FULL_BLOCK,
                    GLOW_DIM + (int) (GLOW_RANGE * (count / NodeStabilizerBlockEntity.STROKE) * pulse));
            mesh.render(arm, pose.last(), buffers.getBuffer(RenderType.entityCutoutNoCull(LEGACY_GLOW)), -1, glow,
                    overlay);
            pose.popPose();
        }
        pose.popPose();
    }

    /** A shell around whatever the stabiliser is holding, which is how a player can see that it is working. */
    private void drawShellAroundHeldNode(NodeStabilizerBlockEntity stabilizer, PoseStack pose,
            MultiBufferSource buffers, float age) {
        if (stabilizer.getLevel() == null) {
            return;
        }
        BlockPos at = stabilizer.getBlockPos();
        var anchor = stabilizer.anchor();
        AABB around = new AABB(anchor, anchor).inflate(3.0);

        for (AuraNode node : stabilizer.getLevel().getEntitiesOfClass(AuraNode.class, around, AuraNode::isHeld)) {
            // it fades in with the arms, so the shell arrives as the thing closes on the node
            float size = (0.15F + node.getSize() / 150.0F) * SHELL;
            int alpha = (int) (stabilizer.reachOut() * 190);
            pose.pushPose();
            pose.translate(node.getX() - at.getX(), node.getY() - at.getY(), node.getZ() - at.getZ());
            pose.mulPose(client.getEntityRenderDispatcher().cameraOrientation());
            // turns slowly, so a still node still looks held rather than frozen
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

    /** The shell has to be drawn even when the block itself is out of the usual range. */
    @Override
    public int getViewDistance() {
        return 96;
    }

    /** The shell sits wherever the node is, which is up and away from the stone the arms stand on. */
    @Override
    public AABB getRenderBoundingBox(NodeStabilizerBlockEntity stabilizer) {
        var anchor = stabilizer.anchor();
        return new AABB(anchor, anchor).inflate(4.0);
    }
}
