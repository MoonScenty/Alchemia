package me.moonscenty.alchemia.client;

import me.moonscenty.alchemia.entity.TaintSwarm;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

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
