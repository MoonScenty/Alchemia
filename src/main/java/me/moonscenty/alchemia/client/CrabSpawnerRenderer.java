package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.eldritch.CrabSpawnerBlockEntity;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
import me.moonscenty.alchemia.client.legacy.model.LegacyMesh;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** A crab vent: the crust it is cut into is its block model; this is the original's vent, turned to face out. */
public class CrabSpawnerRenderer implements BlockEntityRenderer<CrabSpawnerBlockEntity> {
    private static final ResourceLocation VENT = Alchemia.id("textures/entity/crabvent.png");

    private static int bakedFrom = -1;
    private static LegacyMesh mesh;

    public CrabSpawnerRenderer(BlockEntityRendererProvider.Context context) {
    }

    private static boolean refresh() {
        if (bakedFrom != LegacyModels.generation()) {
            bakedFrom = LegacyModels.generation();
            mesh = LegacyModels.mesh(LegacyAssets.CRAB_VENT_MESH).map(LegacyMesh::flippedV).orElse(null);
        }
        return mesh != null;
    }

    @Override
    public void render(CrabSpawnerBlockEntity vent, float partial, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        if (!refresh()) {
            return;
        }
        pose.pushPose();
        pose.translate(0.5F, 0.5F, 0.5F);
        switch (vent.facing()) {
            case DOWN -> pose.mulPose(Axis.XP.rotationDegrees(90.0F));
            case UP -> pose.mulPose(Axis.XP.rotationDegrees(-90.0F));
            case NORTH -> pose.mulPose(Axis.YP.rotationDegrees(180.0F));
            case WEST -> pose.mulPose(Axis.YP.rotationDegrees(-90.0F));
            case EAST -> pose.mulPose(Axis.YP.rotationDegrees(90.0F));
            default -> {
            }
        }
        mesh.render(pose.last(), buffers.getBuffer(RenderType.entityCutoutNoCull(VENT)), -1, light, overlay);
        pose.popPose();
    }
}
