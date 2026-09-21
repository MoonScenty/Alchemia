package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aura.AuraGeneration;
import me.moonscenty.alchemia.aura.node.AuraNode;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Draws a node as two flat pictures that always face the camera: the knot itself, and a glow around it.
 * <p>
 * Neither picture has a colour of its own. They are drawn in the colour of whatever the node is made of, which is
 * why one set of frames covers every aspect there is.
 */
public class AuraNodeRenderer extends EntityRenderer<AuraNode> {
    private static final ResourceLocation CORE = Alchemia.id("textures/entity/node_core.png");
    private static final ResourceLocation HALO = Alchemia.id("textures/entity/node_halo.png");
    private static final int CORE_FRAMES = 32;
    private static final int HALO_FRAMES = 16;

    /** How big a node of no size at all would be drawn, and how much its size adds. */
    private static final float LEAST = 0.15F;
    private static final float PER_SIZE = AuraGeneration.BASE * 1.5F;
    /** How hard the halo beats, and how long a beat takes in ticks. */
    private static final float BEAT_DEPTH = 0.2F;
    private static final float BEAT_LENGTH = 8.0F;

    public AuraNodeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(AuraNode node) {
        return CORE;
    }

    @Override
    public void render(AuraNode node, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers,
            int light) {
        Holder<Aspect> aspect = node.aspect();
        int colour = aspect == null ? 0x888888 : aspect.value().color();
        float size = LEAST + node.getSize() / PER_SIZE;
        float age = node.tickCount + partialTick;

        pose.pushPose();
        pose.mulPose(entityRenderDispatcher.cameraOrientation());

        drawFacing(pose, buffers, CORE, node.tickCount % CORE_FRAMES, CORE_FRAMES, size, colour, 0.75F);
        // the glow swells and shrinks a little out of step with the knot, so the two never look welded together
        float beat = 1.0F - Mth.sin(age / BEAT_LENGTH) * BEAT_DEPTH;
        drawFacing(pose, buffers, HALO, node.tickCount % HALO_FRAMES, HALO_FRAMES, size * beat, colour, 0.55F);

        pose.popPose();
        super.render(node, yaw, partialTick, pose, buffers, light);
    }

    /** One frame out of a strip, drawn as a square centred on the node and tinted. */
    private static void drawFacing(PoseStack pose, MultiBufferSource buffers, ResourceLocation strip, int frame,
            int frames, float size, int colour, float alpha) {
        float u0 = frame / (float) frames;
        float u1 = (frame + 1) / (float) frames;
        int red = (colour >> 16) & 0xFF;
        int green = (colour >> 8) & 0xFF;
        int blue = colour & 0xFF;
        int opacity = (int) (alpha * 255);

        // added to what is behind rather than laid over it, which is what makes it read as light
        VertexConsumer buffer = buffers.getBuffer(RenderType.energySwirl(strip, 0.0F, 0.0F));
        Matrix4f matrix = pose.last().pose();
        quad(buffer, matrix, -size, -size, u0, 1.0F, red, green, blue, opacity);
        quad(buffer, matrix, size, -size, u1, 1.0F, red, green, blue, opacity);
        quad(buffer, matrix, size, size, u1, 0.0F, red, green, blue, opacity);
        quad(buffer, matrix, -size, size, u0, 0.0F, red, green, blue, opacity);
    }

    private static void quad(VertexConsumer buffer, Matrix4f matrix, float x, float y, float u, float v,
            int red, int green, int blue, int alpha) {
        buffer.addVertex(matrix, x, y, 0.0F)
                .setColor(red, green, blue, alpha)
                .setUv(u, v)
                .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0)
                .setNormal(0.0F, 1.0F, 0.0F);
    }

    /** A node is its own light, so it is never dimmed by the dark it hangs in. */
    @Override
    protected int getBlockLightLevel(AuraNode node, net.minecraft.core.BlockPos pos) {
        return 15;
    }
}
