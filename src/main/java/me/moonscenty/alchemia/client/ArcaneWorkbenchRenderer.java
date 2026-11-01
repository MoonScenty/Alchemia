package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.block.entity.ArcaneWorkbenchBlockEntity;
import me.moonscenty.alchemia.item.WandItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ArcaneWorkbenchRenderer implements BlockEntityRenderer<ArcaneWorkbenchBlockEntity> {
    private static final float X = 0.55F;
    private static final float Y = 1.0625F;
    private static final float Z = 0.2F;
    private static final float TURN = 20.0F;
    // vanilla's lift for a dropped item
    private static final float DROPPED_LIFT = 0.1F;

    public ArcaneWorkbenchRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ArcaneWorkbenchBlockEntity bench, float partialTick, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        ItemStack wand = bench.wand();
        if (!(wand.getItem() instanceof WandItem) || bench.getLevel() == null) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        BakedModel model = client.getItemRenderer().getModel(wand, bench.getLevel(), null, 0);
        pose.pushPose();
        pose.translate(X, Y, Z);
        pose.mulPose(Axis.XP.rotationDegrees(90.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(TURN));
        float lift = model.getTransforms().getTransform(ItemDisplayContext.GROUND).scale.y();
        pose.translate(0.0F, DROPPED_LIFT + 0.25F * lift, 0.0F);
        client.getItemRenderer().render(wand, ItemDisplayContext.GROUND, false, pose, buffers, light,
                OverlayTexture.NO_OVERLAY, model);
        pose.popPose();
    }
}
