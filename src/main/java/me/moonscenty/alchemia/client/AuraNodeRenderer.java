package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aura.AuraGeneration;
import me.moonscenty.alchemia.aura.node.AuraNode;
import me.moonscenty.alchemia.aura.node.NodeType;
import me.moonscenty.alchemia.client.legacy.LegacyRenderTypes;
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

public class AuraNodeRenderer extends EntityRenderer<AuraNode> {
    private static final ResourceLocation CORE = Alchemia.id("textures/entity/node_core.png");
    private static final ResourceLocation HALO = Alchemia.id("textures/entity/node_halo.png");
    private static final ResourceLocation TAG_BACK = Alchemia.id("textures/aspect/background.png");
    private static final ResourceLocation UNKNOWN = Alchemia.id("textures/aspect/unknown.png");
    private static final ResourceLocation TYPES = Alchemia.id("textures/entity/node_types.png");
    private static final int CORE_FRAMES = 32;
    private static final int HALO_FRAMES = 16;
    private static final float CORE_ALPHA = 0.75F;
    private static final float HALO_ALPHA = 0.9F;
    private static final float HALO_SIZE = 0.7F;
    private static final float TYPE_SIZE = 1.0F / 3.0F;

    private static final float LEAST = 0.15F;
    private static final float PER_SIZE = AuraGeneration.BASE * 1.5F;
    private static final float BEAT_DEPTH = 0.2F;
    private static final float BEAT_LENGTH = 8.0F;

    private static final double LABEL_WITHIN = 30.0;
    private static final double LABEL_FADE = 25.0;
    private static final float LABEL_SCALE = 0.025F;
    private static final int ICON = 16;
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

        int frame = node.tickCount % CORE_FRAMES;
        drawFacing(pose, buffers, LegacyRenderTypes.glow(CORE), frame, CORE_FRAMES, 0, 1, size, colour,
                CORE_ALPHA * clarity);
        float beat = 1.0F - Mth.sin(age / BEAT_LENGTH) * BEAT_DEPTH;
        drawFacing(pose, buffers, LegacyRenderTypes.glow(HALO), node.tickCount % HALO_FRAMES, HALO_FRAMES, 0, 1,
                size * beat * HALO_SIZE, colour, HALO_ALPHA * clarity);
        NodeType type = node.type();
        RenderType kind = type == NodeType.HUNGRY ? RenderType.entityTranslucent(TYPES)
                : LegacyRenderTypes.glow(TYPES);
        drawFacing(pose, buffers, kind, frame, CORE_FRAMES, type.ordinal(), NodeType.values().length,
                size * TYPE_SIZE, 0xFFFFFF, clarity);

        pose.popPose();

        drawLabel(node, aspect, size, pose, buffers, light);
        super.render(node, yaw, partialTick, pose, buffers, light);
    }

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
        pose.scale(LABEL_SCALE, -LABEL_SCALE, LABEL_SCALE);

        float bottom = -LINE;
        centred(pose, buffers, node.type().displayName(), bottom, alpha, light);
        if (known) {
            bottom -= LINE;
            centred(pose, buffers, Component.literal(String.valueOf(node.getSize())), bottom, alpha, light);
        }
        drawTag(pose, buffers, aspect, known, bottom - GAP - ICON, alpha, light);

        pose.popPose();
    }

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

    private static void drawFacing(PoseStack pose, MultiBufferSource buffers, RenderType type, int frame,
            int frames, int row, int rows, float size, int colour, float alpha) {
        float u0 = frame / (float) frames;
        float u1 = (frame + 1) / (float) frames;
        float v0 = row / (float) rows;
        float v1 = (row + 1) / (float) rows;
        int red = (colour >> 16) & 0xFF;
        int green = (colour >> 8) & 0xFF;
        int blue = colour & 0xFF;
        int opacity = (int) (Mth.clamp(alpha, 0.0F, 1.0F) * 255);

        VertexConsumer buffer = buffers.getBuffer(type);
        Matrix4f matrix = pose.last().pose();
        quad(buffer, matrix, -size, -size, u0, v1, red, green, blue, opacity);
        quad(buffer, matrix, size, -size, u1, v1, red, green, blue, opacity);
        quad(buffer, matrix, size, size, u1, v0, red, green, blue, opacity);
        quad(buffer, matrix, -size, size, u0, v0, red, green, blue, opacity);
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

    @Override
    protected int getBlockLightLevel(AuraNode node, net.minecraft.core.BlockPos pos) {
        return 15;
    }
}
