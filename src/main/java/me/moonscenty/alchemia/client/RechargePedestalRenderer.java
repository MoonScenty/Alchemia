package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.block.entity.RechargePedestalBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * The wand on a recharge pedestal, floating over it, bobbing and turning a degree a tick as the original's did.
 */
public class RechargePedestalRenderer implements BlockEntityRenderer<RechargePedestalBlockEntity> {
    /** How high over the block the wand floats, how far it bobs, and how slowly. The original's. */
    private static final float STANDS = 1.15F;
    private static final float BOBS = 0.05F;
    private static final float BOBS_SLOWLY = 16.0F;

    public RechargePedestalRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(RechargePedestalBlockEntity pedestal, float partial, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        ItemStack held = pedestal.getItem(0);
        if (held.isEmpty() || pedestal.getLevel() == null) {
            return;
        }
        float ticks = pedestal.getLevel().getGameTime() % 32767L + partial;
        pose.pushPose();
        pose.translate(0.5F, STANDS + Mth.sin(ticks / BOBS_SLOWLY) * BOBS, 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(ticks % 360.0F));
        Minecraft.getInstance().getItemRenderer().renderStatic(held, ItemDisplayContext.GROUND, light,
                OverlayTexture.NO_OVERLAY, pose, buffers, pedestal.getLevel(), 0);
        pose.popPose();
    }
}
