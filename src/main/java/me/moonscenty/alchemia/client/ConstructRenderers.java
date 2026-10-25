package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
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

/** How the constructs that stand on tripods are drawn: the two automated crossbows and the node magnet. */
public final class ConstructRenderers {
    private ConstructRenderers() {
    }

    /** The basic crossbow, on the original's model built in code. */
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

    /**
     * The advanced crossbow, on the original's mesh: legs that stay put, and a body that aims, with its loader sliding
     * as it reloads and its two bows snapping forward as it shoots. It flushes red and shakes when it is hit.
     */
    public static class Advanced extends EntityRenderer<AdvancedAutomatedCrossbow> {
        private static final ResourceLocation SHEET = Alchemia.id("textures/entity/advanced_automated_crossbow.png");
        private static final float STANDS = 0.75F;
        private static final float RATTLES = 500.0F;
        private static final int HURT = 0xFFFF8080;

        private static int meshFrom = -1;
        private static LegacyMesh mesh;

        public Advanced(EntityRendererProvider.Context context) {
            super(context);
            shadowRadius = 0.5F;
        }

        @Override
        public void render(AdvancedAutomatedCrossbow turret, float yaw, float partial, PoseStack pose,
                MultiBufferSource buffers, int light) {
            LegacyMesh mesh = mesh();
            if (mesh != null) {
                var buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(SHEET));
                int colour = turret.hurtTime > 0 ? HURT : -1;
                pose.pushPose();
                pose.translate(0.0F, STANDS, 0.0F);
                part(mesh, "legs", pose, buffer, colour, light);
                if (turret.hurtTime > 0) {
                    float shake = turret.hurtTime / RATTLES;
                    pose.translate((float) (turret.getRandom().nextGaussian() * shake),
                            (float) (turret.getRandom().nextGaussian() * shake),
                            (float) (turret.getRandom().nextGaussian() * shake));
                }
                pose.mulPose(Axis.YN.rotationDegrees(Mth.rotLerp(partial, turret.yHeadRotO, turret.yHeadRot)));
                pose.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partial, turret.xRotO, turret.getXRot())));
                for (String still : new String[] {"mech", "box", "shield", "brain"}) {
                    part(mesh, still, pose, buffer, colour, light);
                }
                pose.pushPose();
                pose.translate(0.0F, 0.0F, Mth.sin(Mth.sqrt(turret.winding(partial)) * Mth.TWO_PI) / 12.0F);
                part(mesh, "loader", pose, buffer, colour, light);
                pose.popPose();

                float snap = Mth.sin(Mth.sqrt(turret.getAttackAnim(partial)) * Mth.TWO_PI) * 20.0F;
                pose.translate(0.0F, 0.0F, 0.375F);
                pose.pushPose();
                pose.mulPose(Axis.YP.rotationDegrees(snap));
                part(mesh, "bow1", pose, buffer, colour, light);
                pose.popPose();
                pose.pushPose();
                pose.mulPose(Axis.YN.rotationDegrees(snap));
                part(mesh, "bow2", pose, buffer, colour, light);
                pose.popPose();
                pose.popPose();
            }
            super.render(turret, yaw, partial, pose, buffers, light);
        }

        private static void part(LegacyMesh mesh, String name, PoseStack pose,
                com.mojang.blaze3d.vertex.VertexConsumer buffer, int colour, int light) {
            int group = mesh.group(name);
            if (group >= 0) {
                mesh.render(group, pose.last(), buffer, colour, light, OverlayTexture.NO_OVERLAY);
            }
        }

        private static LegacyMesh mesh() {
            int generation = LegacyModels.generation();
            if (generation != meshFrom) {
                meshFrom = generation;
                mesh = LegacyModels.mesh(LegacyAssets.CROSSBOW_ADVANCED_MESH).map(LegacyMesh::flippedV).orElse(null);
            }
            return mesh;
        }

        @Override
        public ResourceLocation getTextureLocation(AdvancedAutomatedCrossbow turret) {
            return SHEET;
        }
    }

    /**
     * The node magnet, on the original's model built in code and drawn see-through where its sheet is, and while it
     * pulls, the ripples it sends out ahead of it: the original's {@code FXSonic}, a hemisphere of rippling sound
     * thrown forward along its aim, fifteen frames to a wave.
     */
    public static class Magnet extends MobRenderer<NodeMagnet, NodeMagnetModel> {
        private static final ResourceLocation SHEET = Alchemia.id("textures/entity/node_magnet.png");
        private static final int FRAMES = 15;
        private static final float WAVE = 10.0F;

        private static int meshFrom = -1;
        private static LegacyMesh hemisphere;

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
            LegacyMesh mesh = hemisphere();
            if (mesh == null) {
                return;
            }
            float through = ((magnet.tickCount + partial) % WAVE) / WAVE;
            int frame = Math.min(FRAMES, (int) ((FRAMES - 1) * through) + 1);
            ResourceLocation sheet = Alchemia.id("textures/entity/ripple" + frame + ".png");
            float tall = magnet.getBbHeight();
            pose.pushPose();
            pose.translate(0.0F, magnet.getEyeHeight(), 0.0F);
            pose.mulPose(Axis.YN.rotationDegrees(Mth.rotLerp(partial, magnet.yHeadRotO, magnet.yHeadRot)));
            pose.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partial, magnet.xRotO, magnet.getXRot())));
            pose.translate(0.0F, 0.0F, 2.0F * tall + magnet.getBbWidth() / 2.0F);
            pose.scale(0.25F * tall, 0.25F * tall, -tall);
            mesh.render(pose.last(), buffers.getBuffer(RenderType.entityTranslucent(sheet)), 0xFF808080,
                    LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            pose.popPose();
        }

        private static LegacyMesh hemisphere() {
            int generation = LegacyModels.generation();
            if (generation != meshFrom) {
                meshFrom = generation;
                hemisphere = LegacyModels.mesh(LegacyAssets.HEMISPHERE_MESH).map(LegacyMesh::flippedV).orElse(null);
            }
            return hemisphere;
        }

        @Override
        public ResourceLocation getTextureLocation(NodeMagnet magnet) {
            return SHEET;
        }
    }
}
