package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyCached;
import me.moonscenty.alchemia.client.legacy.model.LegacyMesh;
import me.moonscenty.alchemia.entity.AdvancedAutomatedCrossbow;
import me.moonscenty.alchemia.entity.AutomatedCrossbow;
import me.moonscenty.alchemia.entity.NodeMagnet;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class ConstructRenderers {
    private ConstructRenderers() {
    }

    public static class Basic extends MobRenderer<AutomatedCrossbow, AutomatedCrossbowModel> {
        private static final ResourceLocation SHEET = Alchemia.id("textures/entity/automated_crossbow.png");

        public Basic(EntityRendererProvider.Context context) {
            super(context, new AutomatedCrossbowModel(), 0.5F);
        }

        @Override
        public ResourceLocation getTextureLocation(AutomatedCrossbow turret) {
            return SHEET;
        }
    }

    public static class Advanced extends EntityRenderer<AdvancedAutomatedCrossbow> {
        private static final ResourceLocation SHEET = Alchemia.id("textures/entity/advanced_automated_crossbow.png");
        private static final float STANDS = 0.75F;
        private static final int HURT = 0xFFFF8080;

        private static final LegacyCached<LegacyMesh> MESH =
                LegacyCached.flippedMesh(LegacyAssets.CROSSBOW_ADVANCED_MESH);

        public Advanced(EntityRendererProvider.Context context) {
            super(context);
            shadowRadius = 0.5F;
        }

        @Override
        public void render(AdvancedAutomatedCrossbow turret, float yaw, float partial, PoseStack pose,
                MultiBufferSource buffers, int light) {
            LegacyMesh mesh = MESH.get();
            if (mesh != null) {
                var buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(SHEET));
                int colour = turret.hurtTime > 0 ? HURT : -1;
                pose.pushPose();
                pose.translate(0.0F, STANDS, 0.0F);
                mesh.render("legs", pose.last(), buffer, colour, light, OverlayTexture.NO_OVERLAY);
                AimPose.shake(pose, turret);
                AimPose.aim(pose, turret, partial);
                for (String still : new String[] {"mech", "box", "shield", "brain"}) {
                    mesh.render(still, pose.last(), buffer, colour, light, OverlayTexture.NO_OVERLAY);
                }
                pose.pushPose();
                pose.translate(0.0F, 0.0F, Mth.sin(Mth.sqrt(turret.winding(partial)) * Mth.TWO_PI) / 12.0F);
                mesh.render("loader", pose.last(), buffer, colour, light, OverlayTexture.NO_OVERLAY);
                pose.popPose();

                float snap = Mth.sin(Mth.sqrt(turret.getAttackAnim(partial)) * Mth.TWO_PI) * 20.0F;
                pose.translate(0.0F, 0.0F, 0.375F);
                pose.pushPose();
                pose.mulPose(Axis.YP.rotationDegrees(snap));
                mesh.render("bow1", pose.last(), buffer, colour, light, OverlayTexture.NO_OVERLAY);
                pose.popPose();
                pose.pushPose();
                pose.mulPose(Axis.YN.rotationDegrees(snap));
                mesh.render("bow2", pose.last(), buffer, colour, light, OverlayTexture.NO_OVERLAY);
                pose.popPose();
                pose.popPose();
            }
            super.render(turret, yaw, partial, pose, buffers, light);
        }

        @Override
        public ResourceLocation getTextureLocation(AdvancedAutomatedCrossbow turret) {
            return SHEET;
        }
    }

    public static class Magnet extends MobRenderer<NodeMagnet, NodeMagnetModel> {
        private static final ResourceLocation SHEET = Alchemia.id("textures/entity/node_magnet.png");
        private static final int FRAMES = 15;
        private static final float WAVE = 10.0F;

        private static final LegacyCached<LegacyMesh> HEMISPHERE =
                LegacyCached.flippedMesh(LegacyAssets.HEMISPHERE_MESH);

        public Magnet(EntityRendererProvider.Context context) {
            super(context, new NodeMagnetModel(), 0.5F);
        }

        @Override
        protected RenderType getRenderType(NodeMagnet magnet, boolean visible, boolean translucent, boolean glowing) {
            return RenderType.entityTranslucent(SHEET);
        }

        @Override
        public void render(NodeMagnet magnet, float yaw, float partial, PoseStack pose, MultiBufferSource buffers,
                int light) {
            super.render(magnet, yaw, partial, pose, buffers, light);
            if (magnet.pulling()) {
                ripple(magnet, partial, pose, buffers);
            }
        }

        private static void ripple(NodeMagnet magnet, float partial, PoseStack pose, MultiBufferSource buffers) {
            LegacyMesh mesh = HEMISPHERE.get();
            if (mesh == null) {
                return;
            }
            float through = ((magnet.tickCount + partial) % WAVE) / WAVE;
            int frame = Math.min(FRAMES, (int) ((FRAMES - 1) * through) + 1);
            ResourceLocation sheet = Alchemia.id("textures/entity/ripple" + frame + ".png");
            float tall = magnet.getBbHeight();
            pose.pushPose();
            pose.translate(0.0F, magnet.getEyeHeight(), 0.0F);
            AimPose.aim(pose, magnet, partial);
            pose.translate(0.0F, 0.0F, 2.0F * tall + magnet.getBbWidth() / 2.0F);
            pose.scale(0.25F * tall, 0.25F * tall, -tall);
            mesh.render(pose.last(), buffers.getBuffer(RenderType.entityTranslucent(sheet)), 0xFF808080,
                    LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            pose.popPose();
        }

        @Override
        public ResourceLocation getTextureLocation(NodeMagnet magnet) {
            return SHEET;
        }
    }
}
