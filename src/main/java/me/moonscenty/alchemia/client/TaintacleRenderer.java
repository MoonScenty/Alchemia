package me.moonscenty.alchemia.client;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.entity.Taintacle;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** A taintacle, or a tendril, on the original's model in the original's skin. */
public class TaintacleRenderer<T extends Taintacle> extends MobRenderer<T, TaintacleModel<T>> {
    private static final ResourceLocation SKIN = Alchemia.id("textures/entity/taintacle.png");

    /**
     * @param key    which of the original's builds to draw
     * @param length how many segments that build has
     * @param shadow how wide a shadow it throws
     */
    public TaintacleRenderer(EntityRendererProvider.Context context, String key, int length, float shadow) {
        super(context, new TaintacleModel<>(key, length), shadow);
    }

    @Override
    public ResourceLocation getTextureLocation(T tentacle) {
        return SKIN;
    }
}
