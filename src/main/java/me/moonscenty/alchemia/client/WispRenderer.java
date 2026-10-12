package me.moonscenty.alchemia.client;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.client.legacy.LegacyRenderTypes;
import me.moonscenty.alchemia.entity.Wisp;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * A wisp as the original drew it: no body, only light. A node's halo in the colour of its aspect, turning through
 * its sixteen frames, and over it a white sparkle off the old particle sheet, both always facing whoever looks and
 * both adding light to what is behind them.
 */
public class WispRenderer extends EntityRenderer<Wisp> {
    private static final ResourceLocation HALO = Alchemia.id("textures/entity/node_halo.png");
    private static final ResourceLocation SHEET = Alchemia.id("textures/misc/legacy_particles.png");
    /** The halo's frames across; the sparkle's row and frames on the sixteen-by-sixteen sheet. The original's. */
    private static final int FRAMES = 16;
    private static final int SPARKLE_ROW = 5;
    private static final int SHEET_CELLS = 16;
    /** Half the width of either, in blocks. The original's. */
    private static final float SIZE = 0.75F;

    public WispRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(Wisp wisp, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        if (wisp.getHealth() <= 0.0F) {
            return;
        }
        int frame = wisp.tickCount % FRAMES;
        int colour = wisp.aspect() == null ? 0 : wisp.aspect().value().color();
        pose.pushPose();
        pose.mulPose(entityRenderDispatcher.cameraOrientation());
        Matrix4f matrix = pose.last().pose();
        facing(buffers.getBuffer(LegacyRenderTypes.glow(HALO)), matrix, frame / (float) FRAMES,
                (frame + 1) / (float) FRAMES, 0.0F, 1.0F, colour);
        float row = SPARKLE_ROW / (float) SHEET_CELLS;
        facing(buffers.getBuffer(LegacyRenderTypes.glow(SHEET)), matrix, frame / (float) SHEET_CELLS,
                (frame + 1) / (float) SHEET_CELLS, row, row + 1.0F / SHEET_CELLS, 0xFFFFFF);
        pose.popPose();
        super.render(wisp, yaw, partial, pose, buffers, light);
    }

    private static void facing(VertexConsumer into, Matrix4f matrix, float u0, float u1, float v0, float v1,
            int colour) {
        corner(into, matrix, -SIZE, -SIZE, u0, v1, colour);
        corner(into, matrix, SIZE, -SIZE, u1, v1, colour);
        corner(into, matrix, SIZE, SIZE, u1, v0, colour);
        corner(into, matrix, -SIZE, SIZE, u0, v0, colour);
    }

    private static void corner(VertexConsumer into, Matrix4f matrix, float x, float y, float u, float v, int colour) {
        into.addVertex(matrix, x, y, 0.0F).setColor(0xFF000000 | colour).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0.0F, 1.0F, 0.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(Wisp wisp) {
        return HALO;
    }
}
