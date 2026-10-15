package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.entity.boss.EldritchWarden;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;

/**
 * The Eldritch Warden: the guardian's model in the Warden's own skin, half as big again, rising out of the floor
 * while it wakes. The eye under its hood glows, brightening and dimming.
 */
public class EldritchWardenRenderer extends MobRenderer<EldritchWarden, EldritchGuardianModel<EldritchWarden>> {
    private static final ResourceLocation SKIN = Alchemia.id("textures/entity/eldritch_warden.png");

    public EldritchWardenRenderer(EntityRendererProvider.Context context) {
        super(context, new EldritchGuardianModel<>(), 0.6F);
        addLayer(new Eye(this));
    }

    @Override
    public void render(EldritchWarden warden, float yaw, float partial, PoseStack pose, MultiBufferSource buffers,
            int light) {
        pose.pushPose();
        pose.translate(0.0F, -warden.getBbHeight() * (warden.spawnTimer() / (float) EldritchWarden.RISING), 0.0F);
        super.render(warden, yaw, partial, pose, buffers, light);
        pose.popPose();
    }

    @Override
    protected void scale(EldritchWarden warden, PoseStack pose, float partial) {
        pose.scale(1.5F, 1.5F, 1.5F);
    }

    @Override
    protected RenderType getRenderType(EldritchWarden warden, boolean bodyVisible, boolean translucent,
            boolean glowing) {
        return RenderType.entityTranslucent(SKIN);
    }

    /** The hood's eye, drawn added on top, lit from within by the original's pulse. */
    private static class Eye extends RenderLayer<EldritchWarden, EldritchGuardianModel<EldritchWarden>> {
        Eye(RenderLayerParent<EldritchWarden, EldritchGuardianModel<EldritchWarden>> parent) {
            super(parent);
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buffers, int light, EldritchWarden warden,
                float limbSwing, float limbSwingAmount, float partial, float age, float headYaw, float headPitch) {
            int glow = (int) (195.0F + Mth.sin(warden.tickCount / 3.0F) * 15.0F + 15.0F);
            getParentModel().renderEye(pose, buffers.getBuffer(RenderType.eyes(SKIN)), glow,
                    OverlayTexture.NO_OVERLAY);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(EldritchWarden warden) {
        return SKIN;
    }
}
