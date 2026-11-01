package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.entity.Taintacle;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class TaintacleRenderer<T extends Taintacle> extends MobRenderer<T, TaintacleModel<T>> {
    private static final ResourceLocation SKIN = Alchemia.id("textures/entity/taintacle.png");

    private final float scale;

    public TaintacleRenderer(EntityRendererProvider.Context context, String key, int length, float shadow) {
        this(context, key, length, shadow, 1.0F);
    }

    public TaintacleRenderer(EntityRendererProvider.Context context, String key, int length, float shadow,
            float scale) {
        super(context, new TaintacleModel<>(key, length), shadow);
        this.scale = scale;
    }

    @Override
    protected void scale(T tentacle, PoseStack pose, float partial) {
        pose.scale(scale, scale, scale);
    }

    @Override
    public ResourceLocation getTextureLocation(T tentacle) {
        return SKIN;
    }
}
