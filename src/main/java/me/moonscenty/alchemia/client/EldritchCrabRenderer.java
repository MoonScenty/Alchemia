package me.moonscenty.alchemia.client;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.entity.EldritchCrab;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** The crab in the original's skin, with the original's broad shadow. */
public class EldritchCrabRenderer extends MobRenderer<EldritchCrab, EldritchCrabModel> {
    private static final ResourceLocation SKIN = Alchemia.id("textures/entity/eldritch_crab.png");

    public EldritchCrabRenderer(EntityRendererProvider.Context context) {
        super(context, new EldritchCrabModel(), 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(EldritchCrab crab) {
        return SKIN;
    }
}
