package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aura.node.AuraNode;
import me.moonscenty.alchemia.block.entity.NodeStabilizerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

/**
 * Draws the arms on top of a stabiliser, and the shell it keeps around whatever it is holding.
 * <p>
 * The arms work in and out rather than turning, which is how the original read: a thing bracing something rather
 * than spinning for show.
 */
public class NodeStabilizerRenderer implements BlockEntityRenderer<NodeStabilizerBlockEntity> {
    public static final ModelResourceLocation[] ARMS = {
            arm("piston1"), arm("piston2"), arm("piston3"), arm("piston4"),
    };
    private static final ResourceLocation BUBBLE = Alchemia.id("textures/entity/node_bubble.png");

    /** Which way each arm points, in the order the models are numbered. */
    private static final Direction[] FACING = {
            Direction.SOUTH, Direction.NORTH, Direction.WEST, Direction.EAST,
    };

    /** How far out an arm reaches when it is holding something. */
    private static final float THROW = 0.37F;
    /** How much wider than the node the shell around it is drawn. */
    private static final float SHELL = 1.35F;

    private final Minecraft client = Minecraft.getInstance();

    public NodeStabilizerRenderer(BlockEntityRendererProvider.Context context) {
    }

    private static ModelResourceLocation arm(String name) {
        return ModelResourceLocation.standalone(Alchemia.id("block/node_stabilizer/" + name));
    }

    @Override
    public void render(NodeStabilizerBlockEntity stabilizer, float partialTick, PoseStack pose,
            MultiBufferSource buffers, int light, int overlay) {
        float age = stabilizer.getLevel() == null ? 0 : stabilizer.getLevel().getGameTime() + partialTick;
        // the arms are out only as far as the stabiliser has hold of something, so an idle one sits closed
        float out = stabilizer.reachOut() * THROW;

        pose.pushPose();
        // the models carry their own centring, so nothing is shifted here
        for (int index = 0; index < ARMS.length; index++) {
            Direction way = FACING[index];
            pose.pushPose();
            pose.translate(way.getStepX() * out, 0.0F, way.getStepZ() * out);
            drawArm(pose, buffers, ARMS[index], light, overlay);
            pose.popPose();
        }
        pose.popPose();

        if (stabilizer.reachOut() > 0.0F) {
            drawShellAroundHeldNode(stabilizer, pose, buffers, age);
        }
    }

    private void drawArm(PoseStack pose, MultiBufferSource buffers, ModelResourceLocation model, int light,
            int overlay) {
        BakedModel baked = client.getModelManager().getModel(model);
        client.getBlockRenderer().getModelRenderer().renderModel(pose.last(),
                buffers.getBuffer(RenderType.cutout()), null, baked, 1.0F, 1.0F, 1.0F, light, overlay);
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
            pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(age * 0.4F));
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
}
