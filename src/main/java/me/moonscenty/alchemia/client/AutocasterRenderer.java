package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyCached;
import me.moonscenty.alchemia.client.legacy.model.LegacyMesh;
import me.moonscenty.alchemia.entity.Autocaster;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

// not a LivingEntityRenderer: that flips the pose for player-style models, this mesh is built upright
public class AutocasterRenderer extends EntityRenderer<Autocaster> {
    private static final ResourceLocation SHEET = Alchemia.id("textures/entity/autocaster.png");
    private static final ResourceLocation ELDRITCH = Alchemia.id("textures/entity/eldritch_autocaster.png");
    private static final float MIDDLE = 0.5F;
    // flipped: the original's old OBJ loader turned the texture V over
    private static final LegacyCached<LegacyMesh> MESH = LegacyCached.flippedMesh(LegacyAssets.TURRET_MESH);

    public AutocasterRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.5F;
    }

    @Override
    public void render(Autocaster turret, float yaw, float partialTick, PoseStack pose,
            MultiBufferSource buffers, int light) {
        LegacyMesh mesh = MESH.get();
        if (mesh != null) {
            drawBracket(mesh, turret, pose, buffers, light);
            drawBall(mesh, turret, partialTick, pose, buffers, light);
        }
        super.render(turret, yaw, partialTick, pose, buffers, light);
    }

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
                // UP is how the mesh was modelled
            }
        }
        pose.translate(0.0F, -MIDDLE, 0.0F);
        mesh.render(bracket, pose.last(), buffers.getBuffer(RenderType.entityCutoutNoCull(SHEET)), -1, light,
                OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }

    private static void drawBall(LegacyMesh mesh, Autocaster turret, float partialTick, PoseStack pose,
            MultiBufferSource buffers, int light) {
        int ball = mesh.group("ball");
        if (ball < 0) {
            return;
        }
        pose.pushPose();
        AimPose.shake(pose, turret);
        pose.translate(0.0F, MIDDLE, 0.0F);
        AimPose.aim(pose, turret, partialTick);
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

    @Override
    public ResourceLocation getTextureLocation(Autocaster turret) {
        return turret instanceof me.moonscenty.alchemia.entity.EldritchAutocaster ? ELDRITCH : SHEET;
    }
}
