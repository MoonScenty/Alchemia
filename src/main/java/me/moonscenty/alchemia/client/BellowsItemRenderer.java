package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * The bellows in a hand or a slot: the same model the block draws, facing west and breathing on a slow clock of
 * its own, as the original drew it when there was no block to ask how full the bag was.
 */
public class BellowsItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final BellowsItemRenderer INSTANCE = new BellowsItemRenderer();

    private BellowsItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        var player = Minecraft.getInstance().player;
        float age = player == null ? 0.0F : player.tickCount + Minecraft.getInstance().getTimer()
                .getGameTimeDeltaPartialTick(false);
        float inflation = Mth.sin(age / 8.0F) * 0.3F + 0.7F;
        BellowsRenderer.draw(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(BellowsRenderer.TEXTURE)),
                Direction.WEST, inflation, light, overlay);
    }
}
