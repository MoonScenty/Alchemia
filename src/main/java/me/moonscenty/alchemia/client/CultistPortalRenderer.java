package me.moonscenty.alchemia.client;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.entity.cult.CultistPortal;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * The portal as the original drew it: one sheet of sixteen frames played backwards at twenty a second, turned to
 * face whoever looks. It opens out over its first fifty ticks awake, flinches when struck, swells when it lets one
 * through, and flickers and thins as it is worn down. Asleep, it is not drawn at all.
 */
public class CultistPortalRenderer extends EntityRenderer<CultistPortal> {
    private static final ResourceLocation SHEET = Alchemia.id("textures/entity/cultist_portal.png");
    private static final int FRAMES = 16;

    public CultistPortalRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(CultistPortal portal, float yaw, float partial, PoseStack pose, MultiBufferSource buffers,
            int light) {
        if (!portal.isActive()) {
            return;
        }
        long frameClock = System.nanoTime() / 50_000_000L;
        float halfHeight = 1.4F;
        float open = Math.min(50.0F, portal.awake + partial);
        if (portal.hurtTime > 0) {
            float flinch = Mth.sin(portal.hurtTime * 72.0F * Mth.DEG_TO_RAD);
            halfHeight -= flinch / 4.0F;
            open += 6.0F * flinch;
        }
        if (portal.pulse > 0) {
            float swell = Mth.sin(portal.pulse * 36.0F * Mth.DEG_TO_RAD);
            halfHeight += swell / 4.0F;
            open += 12.0F * swell;
        }
        float halfWidth = open / 50.0F * 1.25F;
        float worn = (1.0F - portal.getHealth() / portal.getMaxHealth()) / 3.0F;
        float a = Mth.sin(portal.awake / (5.0F - 12.0F * worn)) * worn + worn;
        float b = Mth.sin(portal.awake / (6.0F - 15.0F * worn)) * worn + worn;
        float alpha = Mth.clamp(1.0F - a, 0.0F, 1.0F);
        halfHeight -= a / 4.0F;
        halfWidth -= b / 3.0F;

        int frame = FRAMES - 1 - (int) (frameClock % FRAMES);
        float u0 = frame / (float) FRAMES;
        float u1 = u0 + 1.0F / FRAMES;
        pose.pushPose();
        pose.translate(0.0F, portal.getBbHeight() / 2.0F, 0.0F);
        pose.mulPose(entityRenderDispatcher.cameraOrientation());
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityTranslucent(SHEET));
        Matrix4f matrix = pose.last().pose();
        // the original laid its sheet bottom up
        corner(buffer, pose, matrix, -halfWidth, -halfHeight, u1, 0.0F, alpha);
        corner(buffer, pose, matrix, halfWidth, -halfHeight, u0, 0.0F, alpha);
        corner(buffer, pose, matrix, halfWidth, halfHeight, u0, 1.0F, alpha);
        corner(buffer, pose, matrix, -halfWidth, halfHeight, u1, 1.0F, alpha);
        pose.popPose();
        super.render(portal, yaw, partial, pose, buffers, light);
    }

    private static void corner(VertexConsumer buffer, PoseStack pose, Matrix4f matrix, float x, float y, float u,
            float v, float alpha) {
        buffer.addVertex(matrix, x, y, 0.0F).setColor(1.0F, 1.0F, 1.0F, alpha).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose.last(), 0.0F, 0.0F, 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(CultistPortal portal) {
        return SHEET;
    }
}
