package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.entity.EldritchGuardian;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;

/**
 * The guardian as the original drew it: never quite there. Within sixteen blocks it is six tenths solid, and from
 * there it fades to nothing at thirty-two (twenty-four on hard).
 */
public class EldritchGuardianRenderer extends MobRenderer<EldritchGuardian, EldritchGuardianModel> {
    private static final ResourceLocation SKIN = Alchemia.id("textures/entity/eldritch_guardian.png");
    private static final float NEAR = 256.0F;
    private static final float SOLID = 0.6F;

    public EldritchGuardianRenderer(EntityRendererProvider.Context context) {
        super(context, new EldritchGuardianModel(), 0.5F);
    }

    @Override
    public void render(EldritchGuardian guardian, float yaw, float partial, PoseStack pose, MultiBufferSource buffers,
            int light) {
        var viewer = Minecraft.getInstance().getCameraEntity();
        float alpha = SOLID;
        if (viewer != null) {
            float far = guardian.level().getDifficulty() == Difficulty.HARD ? 576.0F : 1024.0F;
            float away = (float) guardian.distanceToSqr(viewer);
            if (away >= NEAR) {
                alpha = (1.0F - Math.min(far - NEAR, away - NEAR) / (far - NEAR)) * SOLID;
            }
        }
        model.alpha = alpha;
        super.render(guardian, yaw, partial, pose, buffers, light);
    }

    @Override
    protected RenderType getRenderType(EldritchGuardian guardian, boolean bodyVisible, boolean translucent,
            boolean glowing) {
        return RenderType.entityTranslucent(SKIN);
    }

    @Override
    public ResourceLocation getTextureLocation(EldritchGuardian guardian) {
        return SKIN;
    }
}
