package me.moonscenty.alchemia.client;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.entity.cult.Cultist;
import me.moonscenty.alchemia.entity.cult.CultistCleric;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

/**
 * A cultist as the original drew one: a plain body in the cult's one skin, which is the old sixty-four by thirty-two
 * sheet, and everything that tells the knight from the cleric is what they have on -- the robe and the plate are
 * drawn on the original's own models.
 * <p>
 * A cleric in its rite floats a little off the ground, bobbing, with a dark wisp of thread running from its head to
 * the altar it attends.
 */
public class CultistRenderer extends HumanoidMobRenderer<Cultist, HumanoidModel<Cultist>> {
    private static final ResourceLocation SKIN = Alchemia.id("textures/entity/cultist.png");
    private static final ResourceLocation THREAD = Alchemia.id("textures/misc/wispy.png");

    public CultistRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(
                LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 32).bakeRoot()),
                0.5F);
        addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
    }

    @Override
    public void render(Cultist cultist, float yaw, float partial, PoseStack pose, MultiBufferSource buffers,
            int light) {
        if (!(cultist instanceof CultistCleric cleric) || !cleric.isRitualist() || cleric.altar().isEmpty()) {
            super.render(cultist, yaw, partial, pose, buffers, light);
            return;
        }
        int seed = RandomSource.create(cleric.getId()).nextInt(1000);
        float bob = Mth.sin((cleric.tickCount + partial + seed) / 9.0F) * 0.1F + 0.21F;
        pose.pushPose();
        pose.translate(0.0F, bob, 0.0F);
        super.render(cultist, yaw, partial, pose, buffers, light);
        pose.popPose();
        thread(cleric, cleric.altar().get(), bob, partial, pose, buffers);
    }

    /** The dark thread from the cleric's head to the altar, wavering most in the middle. */
    private static void thread(CultistCleric cleric, BlockPos altar, float bob, float partial, PoseStack pose,
            MultiBufferSource buffers) {
        Vec3 here = cleric.getPosition(partial);
        Vec3 from = new Vec3(0.0, cleric.getEyeHeight() * 1.2, 0.0);
        Vec3 to = new Vec3(altar.getX() + 0.5 - here.x, altar.getY() + 1.5 - bob - here.y, altar.getZ() + 0.5 - here.z);
        Vec3 along = to.subtract(from);
        double length = along.length();
        int segments = Math.max(2, (int) Math.round(length * 6.0));
        float time = (cleric.tickCount + partial) * -0.03F;
        float fade = Math.min(cleric.tickCount, 10) / 10.0F;
        int alpha = (int) (0.8F * fade * 255);
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityTranslucent(THREAD));
        Matrix4f matrix = pose.last().pose();
        float width = 0.125F;
        for (int i = 0; i < segments; i++) {
            float a = (float) i / segments;
            float b = (float) (i + 1) / segments;
            Vec3 p = wobble(from.add(along.scale(a)), a, time);
            Vec3 q = wobble(from.add(along.scale(b)), b, time);
            float u0 = a * (float) length + time;
            float u1 = b * (float) length + time;
            quad(buffer, matrix, p, q, width, u0, u1, alpha);
        }
    }

    private static Vec3 wobble(Vec3 at, float share, float time) {
        float strength = Mth.sin(share * Mth.PI) * 0.5F;
        float t = share * 10.0F + time;
        return at.add(Mth.sin(t / 4.0F) * strength, Mth.sin(t / 3.0F) * strength, Mth.sin(t / 2.0F) * strength);
    }

    private static void quad(VertexConsumer buffer, Matrix4f matrix, Vec3 p, Vec3 q, float width, float u0, float u1,
            int alpha) {
        corner(buffer, matrix, p.x, p.y - width, p.z, u0, 1.0F, alpha);
        corner(buffer, matrix, q.x, q.y - width, q.z, u1, 1.0F, alpha);
        corner(buffer, matrix, q.x, q.y + width, q.z, u1, 0.0F, alpha);
        corner(buffer, matrix, p.x, p.y + width, p.z, u0, 0.0F, alpha);
    }

    private static void corner(VertexConsumer buffer, Matrix4f matrix, double x, double y, double z, float u, float v,
            int alpha) {
        buffer.addVertex(matrix, (float) x, (float) y, (float) z).setColor(0x11, 0x00, 0x11, alpha).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0.0F, 1.0F, 0.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(Cultist cultist) {
        return SKIN;
    }

    @Override
    public boolean shouldRender(Cultist cultist, net.minecraft.client.renderer.culling.Frustum frustum, double x,
            double y, double z) {
        return super.shouldRender(cultist, frustum, x, y, z)
                || cultist instanceof CultistCleric cleric && cleric.isRitualist();
    }
}
