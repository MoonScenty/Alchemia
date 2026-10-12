package me.moonscenty.alchemia.client;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;

/**
 * A zombie in the original's angry skin. A furious one is drawn the same; its size is its anger, which the game
 * already draws it at.
 */
public class AngryZombieRenderer extends ZombieRenderer {
    private static final ResourceLocation SKIN = Alchemia.id("textures/entity/angry_zombie.png");

    public AngryZombieRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(Zombie zombie) {
        return SKIN;
    }
}
