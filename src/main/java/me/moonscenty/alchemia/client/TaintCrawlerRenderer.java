package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.entity.TaintCrawler;
import net.minecraft.client.model.SilverfishModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class TaintCrawlerRenderer extends MobRenderer<TaintCrawler, SilverfishModel<TaintCrawler>> {
    private static final ResourceLocation SKIN = Alchemia.id("textures/entity/taint_crawler.png");

    public TaintCrawlerRenderer(EntityRendererProvider.Context context) {
        super(context, new SilverfishModel<>(context.bakeLayer(ModelLayers.SILVERFISH)), 0.2F);
    }

    @Override
    protected void scale(TaintCrawler crawler, PoseStack pose, float partial) {
        pose.scale(0.7F, 0.7F, 0.7F);
    }

    @Override
    protected float getFlipDegrees(TaintCrawler crawler) {
        return 180.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(TaintCrawler crawler) {
        return SKIN;
    }
}
