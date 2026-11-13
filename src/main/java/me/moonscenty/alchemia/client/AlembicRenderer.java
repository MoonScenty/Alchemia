package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.AlembicBlock;
import me.moonscenty.alchemia.block.EssentiaSmelterBlock;
import me.moonscenty.alchemia.block.entity.AlembicBlockEntity;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyCached;
import me.moonscenty.alchemia.client.legacy.model.LegacyMesh;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class AlembicRenderer implements BlockEntityRenderer<AlembicBlockEntity> {
    private static final ResourceLocation TEXTURE = Alchemia.id("textures/entity/alembic.png");
    private static final ResourceLocation LABEL = Alchemia.id("textures/entity/label.png");

    // flipped: the original's old OBJ loader turned the texture V over
    private static final LegacyCached<LegacyMesh> MESH = LegacyCached.flippedMesh(LegacyAssets.ALEMBIC_MESH);

    public AlembicRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AlembicBlockEntity alembic, float partialTick, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        LegacyMesh legacy = MESH.get();
        if (legacy == null || alembic.getLevel() == null) {
            return;
        }
        Block below = alembic.getLevel().getBlockState(alembic.getBlockPos().below()).getBlock();
        Direction facing = alembic.getBlockState().getValue(AlembicBlock.FACING);
        var buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));

        pose.pushPose();
        pose.translate(0.5F, 0.0F, 0.5F);
        // mesh is modelled lying down
        pose.mulPose(Axis.XP.rotationDegrees(-90.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(switch (facing) {
            case NORTH -> 270.0F;
            case SOUTH -> 90.0F;
            case EAST -> 180.0F;
            default -> 0.0F;
        }));
        if (below instanceof EssentiaSmelterBlock) {
            legacy.render("TubeMain", pose.last(), buffer, -1, light, overlay);
            legacy.render("Legs", pose.last(), buffer, -1, light, overlay);
        } else if (below instanceof AlembicBlock) {
            legacy.render("TubeMain", pose.last(), buffer, -1, light, overlay);
            legacy.render("TubeSmall", pose.last(), buffer, -1, light, overlay);
        } else {
            legacy.render("Legs", pose.last(), buffer, -1, light, overlay);
        }
        legacy.render("Pot", pose.last(), buffer, -1, light, overlay);
        legacy.render("Panel", pose.last(), buffer, -1, light, overlay);
        pose.popPose();
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (alembic.feeds(side)) {
                ArcaneBoreBaseRenderer.drawNozzle(pose,
                        buffers.getBuffer(RenderType.entityCutoutNoCull(ArcaneBoreRenderer.TEXTURE)), side, light,
                        overlay);
            }
        }
        alembic.filter().ifPresent(aspect -> label(alembic, aspect.value(), pose, buffers, light, overlay));
    }

    private static void label(AlembicBlockEntity alembic, me.moonscenty.alchemia.aspect.Aspect aspect, PoseStack pose,
            MultiBufferSource buffers, int light, int overlay) {
        pose.pushPose();
        pose.translate(0.5F, 0.5F, 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(180.0F - alembic.labelFace().toYRot()));
        quad(buffers.getBuffer(RenderType.entityCutoutNoCull(LABEL)), pose, -0.376F, 0.22F, 0xFFFFFF, light, overlay);
        quad(buffers.getBuffer(RenderType.entityTranslucent(aspect.icon())), pose, -0.377F, 0.16F, aspect.color(), light,
                overlay);
        pose.popPose();
    }

    private static void quad(com.mojang.blaze3d.vertex.VertexConsumer buffer, PoseStack pose, float z, float half, int tint,
            int light, int overlay) {
        corner(buffer, pose, -half, -half, z, 0.0F, 1.0F, tint, light, overlay);
        corner(buffer, pose, -half, half, z, 0.0F, 0.0F, tint, light, overlay);
        corner(buffer, pose, half, half, z, 1.0F, 0.0F, tint, light, overlay);
        corner(buffer, pose, half, -half, z, 1.0F, 1.0F, tint, light, overlay);
    }

    private static void corner(com.mojang.blaze3d.vertex.VertexConsumer buffer, PoseStack pose, float x, float y, float z,
            float u, float v, int tint, int light, int overlay) {
        buffer.addVertex(pose.last().pose(), x, y, z).setColor(tint >> 16 & 0xFF, tint >> 8 & 0xFF, tint & 0xFF, 255)
                .setUv(u, v).setOverlay(overlay).setLight(light).setNormal(pose.last(), 0.0F, 0.0F, -1.0F);
    }
}
