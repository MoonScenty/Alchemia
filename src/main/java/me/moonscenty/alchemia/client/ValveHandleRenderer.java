package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.ValveTubeBlock;
import me.moonscenty.alchemia.block.entity.ValveTubeBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The wheel on a valve, pointed at whichever side it stands on and turned as far as the valve is open.
 * <p>
 * Drawn here rather than picked out of the blockstate because a blockstate can only choose between models written
 * out beforehand, and what shows a turn on this wheel is forty-five degrees -- which a model cannot give to corners
 * already sitting at forty-five. In code the angle is a number, so it can be any angle, and it can be partway
 * between the two while the valve is being worked.
 */
public class ValveHandleRenderer implements BlockEntityRenderer<ValveTubeBlockEntity> {
    public static final ModelResourceLocation HANDLE =
            ModelResourceLocation.standalone(Alchemia.id("block/tube/handle"));

    /** How far the wheel turns between shut and open, and how long it takes to get there. */
    private static final float TURNS_BY = 45.0F;
    private static final float TURNS_IN = 6.0F;
    /** The middle of the wheel, in the sixteenths a model is written in. */
    private static final float STEM = 8.0F / 16.0F;

    public ValveHandleRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ValveTubeBlockEntity valve, float partial, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        BlockState state = valve.getBlockState();
        if (!(state.getBlock() instanceof ValveTubeBlock)) {
            return;
        }
        boolean open = state.getValue(ValveTubeBlock.OPEN);
        float ticks = valve.getLevel() == null ? 0.0F : valve.getLevel().getGameTime() + partial;
        // how far it has got since it was last worked: one at rest, and climbing while the wheel is still moving
        float done = Math.min(1.0F, Math.max(0.0F, (ticks - valve.worked()) / TURNS_IN));
        float turned = Mth.lerp(done, open ? 0.0F : TURNS_BY, open ? TURNS_BY : 0.0F);

        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pointed(pose, state.getValue(ValveTubeBlock.FACING));
        // the wheel turns about its own stem, which stands along the way the handle points
        pose.mulPose(Axis.YP.rotationDegrees(turned));
        pose.translate(-0.5, -0.5, -0.5);

        Minecraft client = Minecraft.getInstance();
        client.getBlockRenderer().getModelRenderer().renderModel(pose.last(),
                buffers.getBuffer(RenderType.cutout()), state, client.getModelManager().getModel(HANDLE),
                1.0F, 1.0F, 1.0F, light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }

    /** The handle is drawn standing up, so every other side is a quarter turn away from that. */
    private static void pointed(PoseStack pose, Direction side) {
        switch (side) {
            case DOWN -> pose.mulPose(Axis.ZP.rotationDegrees(180.0F));
            case EAST -> pose.mulPose(Axis.ZP.rotationDegrees(-90.0F));
            case WEST -> pose.mulPose(Axis.ZP.rotationDegrees(90.0F));
            case NORTH -> pose.mulPose(Axis.XP.rotationDegrees(-90.0F));
            case SOUTH -> pose.mulPose(Axis.XP.rotationDegrees(90.0F));
            default -> {
            }
        }
    }
}
