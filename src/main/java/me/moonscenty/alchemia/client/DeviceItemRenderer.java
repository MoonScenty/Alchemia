package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;

import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * The centrifuge and the crystallizer in a hand or a slot: what their blocks draw, standing still, as the original
 * drew them from a block entity with no world. A crystallizer with no world faced up, so this one does too.
 */
public class DeviceItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final DeviceItemRenderer INSTANCE = new DeviceItemRenderer();

    private DeviceItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        if (stack.is(ModBlocks.CENTRIFUGE.get().asItem())) {
            CentrifugeRenderer.draw(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(CentrifugeRenderer.TEXTURE)),
                    0.0F, light, overlay);
        } else if (stack.is(ModBlocks.CRYSTALLIZER.get().asItem())) {
            float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
            CrystallizerRenderer.draw(pose, buffers, Direction.UP, 0.0F, 0xFFFFFFFF,
                    CrystallizerRenderer.clientAge(partial), light, overlay);
        }
    }
}
