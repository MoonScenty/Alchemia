package me.moonscenty.alchemia.client;

import org.joml.Matrix4f;
import org.joml.Quaternionf;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.entity.EldritchOrb;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * The orb as the original drew it: a dozen thin rays of light turning slowly out of a dark swirling knot, the rays
 * growing in over its first half second. The rays are drawn as the dragon's dying rays are, which is where the
 * original took them from.
 */
public class EldritchOrbRenderer extends EntityRenderer<EldritchOrb> {
    private static final ResourceLocation SHEET = Alchemia.id("textures/misc/legacy_particles.png");
    private static final float HALF_SQRT_3 = (float) (Math.sqrt(3.0) / 2.0);
    private static final int RAYS = 12;

    public EldritchOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(EldritchOrb orb, float yaw, float partial, PoseStack pose, MultiBufferSource buffers,
            int light) {
        float age = orb.tickCount + partial;
        pose.pushPose();
        pose.translate(0.0F, orb.getBbHeight() / 2.0F, 0.0F);
        rays(orb, age, pose, buffers.getBuffer(RenderType.dragonRays()));
        pose.mulPose(entityRenderDispatcher.cameraOrientation());
        int frame = orb.tickCount % 13;
        float u0 = frame / 16.0F;
        float u1 = u0 + 0.0624375F;
        VertexConsumer knot = buffers.getBuffer(RenderType.entityTranslucent(SHEET));
        Matrix4f matrix = pose.last().pose();
        corner(knot, matrix, -0.5F, -0.5F, u0, 0.2499375F);
        corner(knot, matrix, 0.5F, -0.5F, u1, 0.2499375F);
        corner(knot, matrix, 0.5F, 0.5F, u1, 0.1875F);
        corner(knot, matrix, -0.5F, 0.5F, u0, 0.1875F);
        pose.popPose();
        super.render(orb, yaw, partial, pose, buffers, light);
    }

    private static void rays(EldritchOrb orb, float age, PoseStack pose, VertexConsumer buffer) {
        float grown = Math.min(age, 10.0F) / 10.0F;
        if (grown <= 0.0F) {
            return;
        }
        float spin = age / 80.0F;
        RandomSource random = RandomSource.create(orb.getId());
        int centre = FastColor.ARGB32.color(255, 255, 255, 255);
        int tip = FastColor.ARGB32.color(0, 192, 192, 192);
        Quaternionf turn = new Quaternionf();
        pose.pushPose();
        for (int i = 0; i < RAYS; i++) {
            turn.rotationXYZ(random.nextFloat() * Mth.TWO_PI, random.nextFloat() * Mth.TWO_PI,
                    random.nextFloat() * Mth.TWO_PI).rotateXYZ(random.nextFloat() * Mth.TWO_PI,
                    random.nextFloat() * Mth.TWO_PI, random.nextFloat() * Mth.TWO_PI + spin * Mth.TWO_PI);
            pose.mulPose(turn);
            float length = (random.nextFloat() * 20.0F + 5.0F) / 30.0F * grown;
            float width = (random.nextFloat() * 2.0F + 1.0F) / 30.0F * grown;
            PoseStack.Pose at = pose.last();
            buffer.addVertex(at, 0.0F, 0.0F, 0.0F).setColor(centre);
            buffer.addVertex(at, -HALF_SQRT_3 * width, length, -0.5F * width).setColor(tip);
            buffer.addVertex(at, HALF_SQRT_3 * width, length, -0.5F * width).setColor(tip);
            buffer.addVertex(at, 0.0F, 0.0F, 0.0F).setColor(centre);
            buffer.addVertex(at, HALF_SQRT_3 * width, length, -0.5F * width).setColor(tip);
            buffer.addVertex(at, 0.0F, length, width).setColor(tip);
            buffer.addVertex(at, 0.0F, 0.0F, 0.0F).setColor(centre);
            buffer.addVertex(at, 0.0F, length, width).setColor(tip);
            buffer.addVertex(at, -HALF_SQRT_3 * width, length, -0.5F * width).setColor(tip);
        }
        pose.popPose();
    }

    private static void corner(VertexConsumer buffer, Matrix4f matrix, float x, float y, float u, float v) {
        buffer.addVertex(matrix, x, y, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0.0F, 1.0F, 0.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(EldritchOrb orb) {
        return SHEET;
    }
}
