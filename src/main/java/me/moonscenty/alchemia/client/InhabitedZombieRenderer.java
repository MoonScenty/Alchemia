package me.moonscenty.alchemia.client;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;

/** A zombie in the husk's skin. What it wears is drawn by the plate's own model, in the husk's stained colours. */
public class InhabitedZombieRenderer extends ZombieRenderer {
    private static final ResourceLocation SKIN = Alchemia.id("textures/entity/inhabited_zombie.png");

    public InhabitedZombieRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(Zombie zombie) {
        return SKIN;
    }
}
