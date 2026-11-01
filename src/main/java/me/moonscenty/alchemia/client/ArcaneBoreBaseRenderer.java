package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.block.ArcaneBoreBaseBlock;
import me.moonscenty.alchemia.block.entity.ArcaneBoreBaseBlockEntity;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyCached;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

public class ArcaneBoreBaseRenderer implements BlockEntityRenderer<ArcaneBoreBaseBlockEntity> {
    private static final String[] STAND = {"Base1", "Base2", "PillarMid", "Pillar1", "Pillar2", "Pillar3",
            "Pillar4"};
    private static final String[] NOZZLE = {"Nozzle1", "Nozzle2"};

    private static final LegacyCached<ModelPart[]> STAND_PARTS = LegacyCached.fields(LegacyAssets.BORE_BASE, STAND);
    private static final LegacyCached<ModelPart[]> NOZZLE_PARTS = LegacyCached.fields(LegacyAssets.BORE_BASE, NOZZLE);

    public ArcaneBoreBaseRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ArcaneBoreBaseBlockEntity base, float partial, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        draw(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ArcaneBoreRenderer.TEXTURE)),
                base.getBlockState().getValue(ArcaneBoreBaseBlock.FACING), light, overlay);
    }

    static void draw(PoseStack pose, VertexConsumer buffer, Direction nozzleTo, int light, int overlay) {
        ModelPart[] stand = STAND_PARTS.get();
        ModelPart[] nozzle = NOZZLE_PARTS.get();
        if (stand == null || nozzle == null) {
            return;
        }
        pose.pushPose();
        pose.translate(0.5F, 0.0F, 0.5F);
        for (ModelPart part : stand) {
            part.render(pose, buffer, light, overlay);
        }
        // nozzle is modelled pointing east
        pose.mulPose(Axis.YP.rotationDegrees(switch (nozzleTo) {
            case NORTH -> 90.0F;
            case SOUTH -> 270.0F;
            case WEST -> 180.0F;
            default -> 0.0F;
        }));
        for (ModelPart part : nozzle) {
            part.render(pose, buffer, light, overlay);
        }
        pose.popPose();
    }

    static void drawNozzle(PoseStack pose, VertexConsumer buffer, Direction pointing, int light, int overlay) {
        ModelPart[] stand = STAND_PARTS.get();
        ModelPart[] nozzle = NOZZLE_PARTS.get();
        if (stand == null || nozzle == null) {
            return;
        }
        pose.pushPose();
        pose.translate(0.5F, 0.0F, 0.5F);
        switch (pointing) {
            case DOWN -> {
                pose.translate(-0.5F, 0.5F, 0.0F);
                pose.mulPose(Axis.ZN.rotationDegrees(90.0F));
            }
            case UP -> {
                pose.translate(0.5F, 0.5F, 0.0F);
                pose.mulPose(Axis.ZP.rotationDegrees(90.0F));
            }
            case NORTH -> pose.mulPose(Axis.YP.rotationDegrees(90.0F));
            case SOUTH -> pose.mulPose(Axis.YP.rotationDegrees(270.0F));
            case WEST -> pose.mulPose(Axis.YP.rotationDegrees(180.0F));
            default -> {
            }
        }
        for (ModelPart part : nozzle) {
            part.render(pose, buffer, light, overlay);
        }
        pose.popPose();
    }
}
