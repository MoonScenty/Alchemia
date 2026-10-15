package me.moonscenty.alchemia.client;

import me.moonscenty.alchemia.entity.PrimalArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * A primal arrow is drawn as the game's own arrow, as the original drew it; the glow of its element about it is a
 * particle it gives off as it flies.
 */
public class PrimalArrowRenderer extends ArrowRenderer<PrimalArrow> {
    private static final ResourceLocation ARROW =
            ResourceLocation.withDefaultNamespace("textures/entity/projectiles/arrow.png");

    public PrimalArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(PrimalArrow arrow) {
        return ARROW;
    }
}
