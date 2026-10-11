package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** A hungry chest in a hand or a slot: shut, facing whoever is looking at it. */
public class HungryChestItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static HungryChestItemRenderer instance;

    private HungryChestItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    /** Made when first asked for, once the game has the parts a renderer needs. */
    public static HungryChestItemRenderer get() {
        if (instance == null) {
            instance = new HungryChestItemRenderer();
        }
        return instance;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        HungryChestRenderer.draw(pose, buffers.getBuffer(RenderType.entityCutout(HungryChestRenderer.TEXTURE)),
                Direction.SOUTH, 0.0F, light, overlay);
    }
}
