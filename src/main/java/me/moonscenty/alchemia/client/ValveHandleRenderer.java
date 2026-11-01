package me.moonscenty.alchemia.client;

import org.joml.Quaternionf;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.ValveTubeBlock;
import me.moonscenty.alchemia.block.entity.ValveTubeBlockEntity;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyCached;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class ValveHandleRenderer implements BlockEntityRenderer<ValveTubeBlockEntity> {
    static final ResourceLocation TEXTURE = Alchemia.id("textures/entity/valve.png");
    private static final float SHUT = 360.0F;
    private static final float PER_TICK = 20.0F;

    private static final LegacyCached<ModelPart> RING = LegacyCached.field(LegacyAssets.VALVE, "ValveRing");
    private static final LegacyCached<ModelPart> ROD = LegacyCached.field(LegacyAssets.VALVE, "ValveRod");

    public ValveHandleRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ValveTubeBlockEntity valve, float partial, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        BlockState state = valve.getBlockState();
        if (!(state.getBlock() instanceof ValveTubeBlock)) {
            return;
        }
        ModelPart ring = RING.get();
        ModelPart rod = ROD.get();
        if (ring == null || rod == null) {
            return;
        }
        boolean open = state.getValue(ValveTubeBlock.OPEN);
        float ticks = valve.getLevel() == null ? 0.0F : valve.getLevel().getGameTime() + partial;
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

    static ModelPart rod() {
        ModelPart ring = RING.get();
        ModelPart rod = ROD.get();
        return ring != null && rod != null ? rod : null;
    }
}
