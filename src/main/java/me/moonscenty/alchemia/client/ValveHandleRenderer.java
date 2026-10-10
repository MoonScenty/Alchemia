package me.moonscenty.alchemia.client;

import org.joml.Quaternionf;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.ValveTubeBlock;
import me.moonscenty.alchemia.block.entity.ValveTubeBlockEntity;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
import me.moonscenty.alchemia.client.legacy.model.LegacyModelBaker;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The wheel on a valve, as the original drew it: a ring on a short stem standing out of the side the valve faces.
 * Shutting the valve screws it in, a turn and a half and a little way down; opening it screws it back out.
 * <p>
 * The wheel and its stem are the original's model ({@code ModelTubeValve}), read out of its code.
 */
public class ValveHandleRenderer implements BlockEntityRenderer<ValveTubeBlockEntity> {
    /** The original's sheet for the wheel. */
    static final ResourceLocation TEXTURE = Alchemia.id("textures/entity/valve.png");
    /** How far the wheel has turned when it is shut, and how far it turns in a tick on the way. */
    private static final float SHUT = 360.0F;
    private static final float PER_TICK = 20.0F;

    /** The original's wheel and stem, as of the import {@link #bakedFrom} names. */
    private static int bakedFrom = -1;
    private static ModelPart ring;
    private static ModelPart rod;

    public ValveHandleRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ValveTubeBlockEntity valve, float partial, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        BlockState state = valve.getBlockState();
        if (!(state.getBlock() instanceof ValveTubeBlock) || !refresh()) {
            return;
        }
        boolean open = state.getValue(ValveTubeBlock.OPEN);
        float ticks = valve.getLevel() == null ? 0.0F : valve.getLevel().getGameTime() + partial;
        // how far it has been screwed since it was last worked, at the original's pace
        float moved = Math.max(0.0F, (ticks - valve.worked()) * PER_TICK);
        float turned = open ? Math.max(0.0F, SHUT - moved) : Math.min(SHUT, moved);

        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pointAlong(pose, state.getValue(ValveTubeBlock.FACING));
        pose.mulPose(Axis.YP.rotationDegrees(-turned * 1.5F));
        pose.translate(0.0, -0.03 - turned / SHUT * 0.09, 0.0);
        var buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        ring.render(pose, buffer, light, OverlayTexture.NO_OVERLAY);
        pose.pushPose();
        pose.scale(0.75F, 1.0F, 0.75F);
        rod.render(pose, buffer, light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
        pose.popPose();
    }

    /**
     * Turns the model's up to the given side, as the original's valve and one-way renderers both did: a quarter turn
     * about the upright, or onto its back for up and down, and then a quarter turn about the side itself.
     */
    static void pointAlong(PoseStack pose, Direction side) {
        if (side.getStepY() == 0) {
            pose.mulPose(Axis.YP.rotationDegrees(90.0F));
        } else {
            pose.mulPose(Axis.XN.rotationDegrees(90.0F));
            pose.mulPose(Axis.XP.rotationDegrees(90.0F * side.getStepY()));
        }
        pose.mulPose(new Quaternionf().rotationAxis((float) Math.toRadians(90.0),
                side.getStepX(), side.getStepY(), side.getStepZ()));
    }

    /** The original's stem on its own, for the one-way tube's mark; null until it has been read. */
    static ModelPart rod() {
        return refresh() ? rod : null;
    }

    /** Bakes the original's parts again whenever the import has changed what there is. */
    private static boolean refresh() {
        int generation = LegacyModels.generation();
        if (generation != bakedFrom) {
            bakedFrom = generation;
            var model = LegacyModels.get(LegacyAssets.VALVE);
            ring = model.map(read -> LegacyModelBaker.field(read, "ValveRing")).orElse(null);
            rod = model.map(read -> LegacyModelBaker.field(read, "ValveRod")).orElse(null);
        }
        return ring != null && rod != null;
    }
}
