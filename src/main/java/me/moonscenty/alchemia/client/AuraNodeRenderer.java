package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aura.AuraGeneration;
import me.moonscenty.alchemia.aura.node.AuraNode;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Draws a node as two flat pictures that always face the camera: the knot itself, and a glow around it.
 * <p>
 * Neither picture has a colour of its own. They are drawn in the colour of whatever the node is made of, which is
 * why one set of frames covers every aspect there is.
 * <p>
 * None of it is drawn for someone carrying nothing that reads the aura — see {@link NodeSight}. Up close, whoever
 * can see one also gets to read it: what it is made of, how large it has grown, and what kind it is.
 */
public class AuraNodeRenderer extends EntityRenderer<AuraNode> {
    private static final ResourceLocation CORE = Alchemia.id("textures/entity/node_core.png");
    private static final ResourceLocation HALO = Alchemia.id("textures/entity/node_halo.png");
    private static final ResourceLocation TAG_BACK = Alchemia.id("textures/aspect/background.png");
    private static final ResourceLocation UNKNOWN = Alchemia.id("textures/aspect/unknown.png");
    private static final int CORE_FRAMES = 32;
    private static final int HALO_FRAMES = 16;

    /** How big a node of no size at all would be drawn, and how much its size adds. */
    private static final float LEAST = 0.15F;
    private static final float PER_SIZE = AuraGeneration.BASE * 1.5F;
    /** How hard the halo beats, and how long a beat takes in ticks. */
    private static final float BEAT_DEPTH = 0.2F;
    private static final float BEAT_LENGTH = 8.0F;

    /** How near, as a squared distance, the label shows at all and where it has finished fading in. */
    private static final double LABEL_WITHIN = 30.0;
    private static final double LABEL_FADE = 25.0;
    /** Text pixels per block once the label has been scaled down to sit in the world. */
    private static final float LABEL_SCALE = 0.025F;
    private static final int ICON = 16;
    /** How tall a written line is, and how much air sits between the icon and the writing. */
    private static final float LINE = 10.0F;
    private static final float GAP = 2.0F;

    private final Minecraft client = Minecraft.getInstance();

    public AuraNodeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(AuraNode node) {
        return CORE;
    }

    /** Nothing is drawn, and nothing is even queued, for someone who has no way of seeing a node. */
    @Override
    public boolean shouldRender(AuraNode node, Frustum frustum, double camX, double camY, double camZ) {
        return NodeSight.clarity(client.getCameraEntity(), node) > 0.0F
                && super.shouldRender(node, frustum, camX, camY, camZ);
    }

    @Override
    public void render(AuraNode node, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers,
            int light) {
        float clarity = NodeSight.clarity(client.getCameraEntity(), node);
        if (clarity <= 0.0F) {
            return;
        }

        Holder<Aspect> aspect = node.aspect();
        int colour = aspect == null ? 0x888888 : aspect.value().color();
        float size = LEAST + node.getSize() / PER_SIZE;
        float age = node.tickCount + partialTick;

        pose.pushPose();
        pose.mulPose(entityRenderDispatcher.cameraOrientation());

        drawFacing(pose, buffers, CORE, node.tickCount % CORE_FRAMES, CORE_FRAMES, size, colour, 0.75F * clarity);
        // the glow swells and shrinks a little out of step with the knot, so the two never look welded together
        float beat = 1.0F - Mth.sin(age / BEAT_LENGTH) * BEAT_DEPTH;
        drawFacing(pose, buffers, HALO, node.tickCount % HALO_FRAMES, HALO_FRAMES, size * beat, colour, 0.55F * clarity);

        pose.popPose();

        drawLabel(node, aspect, size, pose, buffers, light);
        super.render(node, yaw, partialTick, pose, buffers, light);
    }

    /**
     * What the node is, written above it.
     * <p>
     * Only from close to, and it fades in as the player walks up, so a hillside of nodes is not a wall of writing.
     */
    private void drawLabel(AuraNode node, Holder<Aspect> aspect, float size, PoseStack pose,
            MultiBufferSource buffers, int light) {
        if (client.getCameraEntity() == null) {
            return;
        }
        double away = node.distanceToSqr(client.getCameraEntity());
        if (away >= LABEL_WITHIN) {
            return;
        }
        float fade = 1.0F - (float) Math.min(1.0, away / LABEL_FADE);
        int alpha = (int) (fade * 255);
        if (alpha < 8) {
            return;
        }

        boolean known = aspect != null && client.player != null && PlayerKnowledge.of(client.player).knows(aspect);

        pose.pushPose();
        pose.translate(0.0F, size + 0.2F, 0.0F);
        pose.mulPose(entityRenderDispatcher.cameraOrientation());
        // the same flip a name tag uses: down the page becomes down the screen, and the label shrinks to world size
        pose.scale(LABEL_SCALE, -LABEL_SCALE, LABEL_SCALE);

        // stacked upwards from the anchor, so a node whose aspect is a mystery has a shorter label rather than a gap
        float bottom = -LINE;
        centred(pose, buffers, node.type().displayName(), bottom, alpha, light);
        if (known) {
            bottom -= LINE;
            centred(pose, buffers, Component.literal(String.valueOf(node.getSize())), bottom, alpha, light);
        }
        drawTag(pose, buffers, aspect, known, bottom - GAP - ICON, alpha, light);

        pose.popPose();
    }

    /** The aspect icon on its plate, or a question mark when the player has not met this aspect yet. */
    private void drawTag(PoseStack pose, MultiBufferSource buffers, Holder<Aspect> aspect, boolean known, float top,
            int alpha, int light) {
        icon(pose, buffers, TAG_BACK, -ICON / 2F - 1, top - 1, ICON + 2, 0x33, 0x33, 0x33, alpha * 2 / 3, light);
        if (known) {
            int colour = aspect.value().color();
            icon(pose, buffers, aspect.value().icon(), -ICON / 2F, top, ICON,
                    (colour >> 16) & 0xFF, (colour >> 8) & 0xFF, colour & 0xFF, alpha, light);
        } else {
            icon(pose, buffers, UNKNOWN, -ICON / 2F, top, ICON, 0x8C, 0x8C, 0x99, alpha, light);
        }
    }

    private static void centred(PoseStack pose, MultiBufferSource buffers, Component text, float y, int alpha,
            int light) {
        Font font = Minecraft.getInstance().font;
        float left = -font.width(text) / 2F;
        font.drawInBatch(text, left, y, (alpha << 24) | 0xFFFFFF, false, pose.last().pose(), buffers,
                Font.DisplayMode.NORMAL, 0, light);
    }

    /** A square picture laid flat in the label's plane. */
    private static void icon(PoseStack pose, MultiBufferSource buffers, ResourceLocation texture, float x, float y,
            float size, int red, int green, int blue, int alpha, int light) {
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityTranslucent(texture));
        Matrix4f matrix = pose.last().pose();
        corner(buffer, pose, matrix, x, y, 0.0F, 0.0F, red, green, blue, alpha, light);
        corner(buffer, pose, matrix, x, y + size, 0.0F, 1.0F, red, green, blue, alpha, light);
        corner(buffer, pose, matrix, x + size, y + size, 1.0F, 1.0F, red, green, blue, alpha, light);
        corner(buffer, pose, matrix, x + size, y, 1.0F, 0.0F, red, green, blue, alpha, light);
    }

    private static void corner(VertexConsumer buffer, PoseStack pose, Matrix4f matrix, float x, float y, float u,
            float v, int red, int green, int blue, int alpha, int light) {
        buffer.addVertex(matrix, x, y, 0.0F)
                .setColor(red, green, blue, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose.last(), 0.0F, 0.0F, -1.0F);
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
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0)
                .setNormal(0.0F, 1.0F, 0.0F);
    }

    /** A node is its own light, so it is never dimmed by the dark it hangs in. */
    @Override
    protected int getBlockLightLevel(AuraNode node, net.minecraft.core.BlockPos pos) {
        return 15;
    }
}
