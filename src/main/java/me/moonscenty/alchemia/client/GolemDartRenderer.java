package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;

import me.moonscenty.alchemia.golem.GolemDart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** A golem's dart: an ordinary arrow drawn at under two thirds the size, as the original drew it. */
public class GolemDartRenderer extends ArrowRenderer<GolemDart> {
    private static final ResourceLocation ARROW =
            ResourceLocation.withDefaultNamespace("textures/entity/projectiles/arrow.png");
    /** The original's dart scale against an arrow's. */
    private static final float SMALLER = 0.033F / 0.05625F;

    public GolemDartRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(GolemDart dart, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers,
            int light) {
        pose.pushPose();
        pose.scale(SMALLER, SMALLER, SMALLER);
        super.render(dart, yaw, partialTick, pose, buffers, light);
        pose.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(GolemDart dart) {
        return ARROW;
    }
}
