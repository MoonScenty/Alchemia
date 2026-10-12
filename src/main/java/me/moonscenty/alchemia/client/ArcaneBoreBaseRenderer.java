package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.block.ArcaneBoreBaseBlock;
import me.moonscenty.alchemia.block.entity.ArcaneBoreBaseBlockEntity;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

/**
 * Draws a bore's base as the original did: two plates on five pillars, and the nozzle turned to the side it faces.
 * The parts are the original's model ({@code ModelBoreBase}), read out of its code.
 */
public class ArcaneBoreBaseRenderer implements BlockEntityRenderer<ArcaneBoreBaseBlockEntity> {
    private static final String[] STAND = {"Base1", "Base2", "PillarMid", "Pillar1", "Pillar2", "Pillar3",
            "Pillar4"};
    private static final String[] NOZZLE = {"Nozzle1", "Nozzle2"};

    private static int bakedFrom = -1;
    private static ModelPart[] stand;
    private static ModelPart[] nozzle;

    public ArcaneBoreBaseRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ArcaneBoreBaseBlockEntity base, float partial, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        draw(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ArcaneBoreRenderer.TEXTURE)),
                base.getBlockState().getValue(ArcaneBoreBaseBlock.FACING), light, overlay);
    }

    /** The base with its nozzle to a given side. Shared with the item, which points it north. */
    static void draw(PoseStack pose, VertexConsumer buffer, Direction nozzleTo, int light, int overlay) {
        if (!refresh()) {
            return;
        }
        pose.pushPose();
        pose.translate(0.5F, 0.0F, 0.5F);
        for (ModelPart part : stand) {
            part.render(pose, buffer, light, overlay);
        }
        // the nozzle is modelled pointing east
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

    /**
     * The nozzle on its own, as the bellows draw it on a buffer tube they blow into: pointing a given way, any of six,
     * turned as the original's bellows renderer turned it.
     */
    static void drawNozzle(PoseStack pose, VertexConsumer buffer, Direction pointing, int light, int overlay) {
        if (!refresh()) {
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

    private static boolean refresh() {
        int generation = LegacyModels.generation();
        if (generation != bakedFrom) {
            bakedFrom = generation;
            stand = LegacyModels.get(LegacyAssets.BORE_BASE).map(read -> ArcaneBoreRenderer.parts(read, STAND))
                    .orElse(null);
            nozzle = LegacyModels.get(LegacyAssets.BORE_BASE).map(read -> ArcaneBoreRenderer.parts(read, NOZZLE))
                    .orElse(null);
        }
        return stand != null && nozzle != null;
    }
}
