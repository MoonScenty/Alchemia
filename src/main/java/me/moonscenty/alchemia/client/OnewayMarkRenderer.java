package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;

import me.moonscenty.alchemia.block.OnewayTubeBlock;
import me.moonscenty.alchemia.block.TubeBlock;
import me.moonscenty.alchemia.block.entity.TubeBlockEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The one-way tube's mark, as the original drew it: a stub of the valve's stem, tinted blue, standing on the side it
 * lets essentia out of, where it joins something.
 * <p>
 * The plain and restricting tubes share this block entity and draw nothing here.
 */
public class OnewayMarkRenderer implements BlockEntityRenderer<TubeBlockEntity> {
    /** The original's tint for the stub. */
    private static final int TINT = FastColor.ARGB32.colorFromFloat(1.0F, 0.45F, 0.5F, 1.0F);

    public OnewayMarkRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TubeBlockEntity tube, float partial, PoseStack pose, MultiBufferSource buffers, int light,
            int overlay) {
        BlockState state = tube.getBlockState();
        if (!(state.getBlock() instanceof OnewayTubeBlock)) {
            return;
        }
        Direction out = state.getValue(OnewayTubeBlock.FACING);
        ModelPart rod = ValveHandleRenderer.rod();
        if (rod == null || state.getValue(TubeBlock.SIDES.get(out)) == TubeBlock.Link.NONE) {
            return;
        }
        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        // the original pointed the model away from the way out and drew the stub back along it, so it stands there
        ValveHandleRenderer.pointAlong(pose, out.getOpposite());
        pose.scale(1.25F, 1.0F, 1.25F);
        pose.translate(0.0, -0.4, 0.0);
        rod.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ValveHandleRenderer.TEXTURE)), light,
                OverlayTexture.NO_OVERLAY, TINT);
        pose.popPose();
    }
}
