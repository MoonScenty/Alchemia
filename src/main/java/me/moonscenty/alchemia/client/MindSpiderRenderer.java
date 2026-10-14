package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import me.moonscenty.alchemia.entity.MindSpider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.SpiderEyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

/**
 * A mind spider as the original drew it: a spider at six tenths the size, its body only a tenth there, so that what
 * one sees is mostly a pair of red eyes. One warp shows to a single player is drawn for that player alone -- shadow
 * and all, which the original let the others see.
 */
public class MindSpiderRenderer extends MobRenderer<MindSpider, SpiderModel<MindSpider>> {
    private static final ResourceLocation SKIN = ResourceLocation.withDefaultNamespace(
            "textures/entity/spider/spider.png");
    private static final float SCALE = 0.6F;

    public MindSpiderRenderer(EntityRendererProvider.Context context) {
        super(context, new Ghostly(context), 0.5F);
        // the eyes are drawn whole whatever the body is
        addLayer(new SpiderEyesLayer<>(this) {
            @Override
            public void render(PoseStack pose, net.minecraft.client.renderer.MultiBufferSource buffers, int light,
                    MindSpider spider, float limbSwing, float limbSwingAmount, float partial, float age,
                    float headYaw, float headPitch) {
                ((Ghostly) getParentModel()).alpha = 1.0F;
                super.render(pose, buffers, light, spider, limbSwing, limbSwingAmount, partial, age, headYaw,
                        headPitch);
            }
        });
    }

    @Override
    public boolean shouldRender(MindSpider spider, Frustum frustum, double x, double y, double z) {
        String viewer = spider.viewer();
        var player = Minecraft.getInstance().player;
        if (!viewer.isEmpty() && (player == null || !viewer.equals(player.getGameProfile().getName()))) {
            return false;
        }
        return super.shouldRender(spider, frustum, x, y, z);
    }

    @Override
    protected void scale(MindSpider spider, PoseStack pose, float partial) {
        pose.scale(SCALE, SCALE, SCALE);
    }

    @Override
    protected float getFlipDegrees(MindSpider spider) {
        return 180.0F;
    }

    @Override
    protected RenderType getRenderType(MindSpider spider, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderType.entityTranslucent(SKIN);
    }

    @Override
    public void render(MindSpider spider, float yaw, float partial, PoseStack pose,
            net.minecraft.client.renderer.MultiBufferSource buffers, int light) {
        ((Ghostly) model).alpha = Math.min(0.1F, (spider.tickCount + partial) / 100.0F);
        super.render(spider, yaw, partial, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(MindSpider spider) {
        return SKIN;
    }

    /** A spider's body drawn only so far there. */
    private static final class Ghostly extends SpiderModel<MindSpider> {
        float alpha = 0.1F;

        Ghostly(EntityRendererProvider.Context context) {
            super(context.bakeLayer(ModelLayers.SPIDER));
        }

        @Override
        public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, int colour) {
            super.renderToBuffer(pose, buffer, light, overlay,
                    FastColor.ARGB32.color((int) (FastColor.ARGB32.alpha(colour) * alpha), colour));
        }
    }
}
