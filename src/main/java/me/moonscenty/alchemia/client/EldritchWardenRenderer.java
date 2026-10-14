package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.entity.boss.EldritchWarden;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * The Eldritch Warden: the guardian's model in the Warden's own skin, half as big again, rising out of the floor
 * while it wakes.
 */
public class EldritchWardenRenderer extends MobRenderer<EldritchWarden, EldritchGuardianModel<EldritchWarden>> {
    private static final ResourceLocation SKIN = Alchemia.id("textures/entity/eldritch_warden.png");

    public EldritchWardenRenderer(EntityRendererProvider.Context context) {
        super(context, new EldritchGuardianModel<>(), 0.6F);
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

    @Override
    public ResourceLocation getTextureLocation(EldritchWarden warden) {
        return SKIN;
    }
}
