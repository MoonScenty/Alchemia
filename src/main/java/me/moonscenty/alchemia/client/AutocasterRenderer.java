package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
import me.moonscenty.alchemia.client.legacy.model.LegacyMesh;
import me.moonscenty.alchemia.entity.Autocaster;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Draws an autocaster as the original drew it: a bracket bolted to whichever face it is on, and a ball sitting in
 * the bracket that turns to look at whatever the thing is aiming at.
 *
 * <p>The focus is a third piece inside the ball, drawn only when one is fitted and tinted whatever colour that
 * focus throws. It is the only way of telling from across a room what a turret is loaded with.
 *
 * <p>Not a {@link net.minecraft.client.renderer.entity.LivingEntityRenderer}: that one turns the world upside
 * down for a model built the way a player's is, and this model is built the right way up.
 */
public class AutocasterRenderer extends EntityRenderer<Autocaster> {
    private static final ResourceLocation SHEET = Alchemia.id("textures/entity/autocaster.png");
    /** How far the ball sits above the thing's feet, which is the middle of the square it stands in. */
    private static final float MIDDLE = 0.5F;
    /** How hard a fresh wound shakes it. The original's number. */
    private static final float RATTLES = 500.0F;

    public AutocasterRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.5F;
    }

    @Override
    public void render(Autocaster turret, float yaw, float partialTick, PoseStack pose,
            MultiBufferSource buffers, int light) {
        LegacyMesh mesh = mesh();
        if (mesh != null) {
            drawBracket(mesh, turret, pose, buffers, light);
            drawBall(mesh, turret, partialTick, pose, buffers, light);
        }
        super.render(turret, yaw, partialTick, pose, buffers, light);
    }

    /** The part that does not move: stood up, hung upside down or laid against a wall, by the face it is on. */
    private static void drawBracket(LegacyMesh mesh, Autocaster turret, PoseStack pose,
            MultiBufferSource buffers, int light) {
        int bracket = mesh.group("base");
        if (bracket < 0) {
            return;
        }
        pose.pushPose();
        pose.translate(0.0F, MIDDLE, 0.0F);
        switch (turret.getFacing()) {
            case DOWN -> pose.mulPose(Axis.XP.rotationDegrees(180.0F));
            case NORTH -> pose.mulPose(Axis.XP.rotationDegrees(-90.0F));
            case SOUTH -> pose.mulPose(Axis.XP.rotationDegrees(90.0F));
            case WEST -> pose.mulPose(Axis.ZP.rotationDegrees(90.0F));
            case EAST -> pose.mulPose(Axis.ZP.rotationDegrees(-90.0F));
            default -> {
                // UP is how the mesh was modelled, so there is nothing to turn
            }
        }
        pose.translate(0.0F, -MIDDLE, 0.0F);
        mesh.render(bracket, pose.last(), buffers.getBuffer(RenderType.entityCutoutNoCull(SHEET)), -1, light,
                OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }

    /** The part that aims, and the focus inside it. */
    private static void drawBall(LegacyMesh mesh, Autocaster turret, float partialTick, PoseStack pose,
            MultiBufferSource buffers, int light) {
        int ball = mesh.group("ball");
        if (ball < 0) {
            return;
        }
        pose.pushPose();
        if (turret.hurtTime > 0) {
            float shake = turret.hurtTime / RATTLES;
            pose.translate((float) (turret.getRandom().nextGaussian() * shake),
                    (float) (turret.getRandom().nextGaussian() * shake),
                    (float) (turret.getRandom().nextGaussian() * shake));
        }
        pose.translate(0.0F, MIDDLE, 0.0F);
        pose.mulPose(Axis.YN.rotationDegrees(Mth.rotLerp(partialTick, turret.yHeadRotO, turret.yHeadRot)));
        pose.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTick, turret.xRotO, turret.getXRot())));
        mesh.render(ball, pose.last(), buffers.getBuffer(RenderType.entityCutoutNoCull(SHEET)), -1, light,
                OverlayTexture.NO_OVERLAY);

        int lens = mesh.group("focus");
        if (lens >= 0 && turret.loaded() != null) {
            int colour = 0xFF000000 | turret.loaded().colour();
            mesh.render(lens, pose.last(), buffers.getBuffer(RenderType.entityTranslucent(SHEET)), colour, light,
                    OverlayTexture.NO_OVERLAY);
        }
        pose.popPose();
    }

    /** The original's mesh as of the last import, turned over for the loader its own renderer read it with. */
    private static int meshFrom = -1;
    private static LegacyMesh mesh;

    private static LegacyMesh mesh() {
        int generation = LegacyModels.generation();
        if (generation != meshFrom) {
            meshFrom = generation;
            mesh = LegacyModels.mesh(LegacyAssets.TURRET_MESH).map(LegacyMesh::flippedV).orElse(null);
        }
        return mesh;
    }

    @Override
    public ResourceLocation getTextureLocation(Autocaster turret) {
        return SHEET;
    }
}
