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
    }
}
