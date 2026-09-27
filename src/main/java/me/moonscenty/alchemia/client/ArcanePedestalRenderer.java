package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;

import me.moonscenty.alchemia.block.entity.ArcanePedestalBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Stands the thing on the pedestal up where it can be seen, and turns it.
 * <p>
 * It turns because a ring of pedestals is something you read at a glance while you are walking round it, and a
 * still item seen edge on is a sliver. It is the same turn every pedestal does, taken from the world clock rather
 * than from each block, so a ring of them moves together instead of shimmering.
 */
public class ArcanePedestalRenderer implements BlockEntityRenderer<ArcanePedestalBlockEntity> {
    /** How high above the block the thing floats, and how far it bobs either side of that. */
    private static final float STANDS = 1.15F;
    private static final float BOBS = 0.05F;
    /** How long one turn takes, and one bob, in ticks. */
    private static final float TURNS_IN = 90.0F;
    private static final float BOBS_IN = 64.0F;

    public ArcanePedestalRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ArcanePedestalBlockEntity stand, float partial, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        ItemStack held = stand.held();
        if (held.isEmpty()) {
            return;
        }
        float ticks = stand.getLevel() == null ? 0.0F : stand.getLevel().getGameTime() % 100000L + partial;

        pose.pushPose();
        pose.translate(0.5, STANDS + BOBS * Math.sin(ticks / BOBS_IN * Math.PI * 2), 0.5);
        pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(ticks / TURNS_IN * 360.0F));
        Minecraft.getInstance().getItemRenderer().renderStatic(held, ItemDisplayContext.GROUND, light,
                OverlayTexture.NO_OVERLAY, pose, buffers, stand.getLevel(), 0);
        pose.popPose();
    }
}
