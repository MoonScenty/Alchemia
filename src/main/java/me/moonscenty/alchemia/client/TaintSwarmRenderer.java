package me.moonscenty.alchemia.client;

import me.moonscenty.alchemia.entity.TaintSwarm;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

/** A taint swarm has no body to draw. What there is of it is its flies, which are particles of their own. */
public class TaintSwarmRenderer extends EntityRenderer<TaintSwarm> {
    public TaintSwarmRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(TaintSwarm swarm) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
