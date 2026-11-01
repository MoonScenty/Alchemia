package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.BellowsBlock;
import me.moonscenty.alchemia.block.entity.BellowsBlockEntity;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyCached;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public class BellowsRenderer implements BlockEntityRenderer<BellowsBlockEntity> {
    static final ResourceLocation TEXTURE = Alchemia.id("textures/entity/bellows.png");
    private static final float CLOSEST = 0.125F;
    private static final float SPREADS = 0.875F;

    private static final LegacyCached<ModelPart> BAG = LegacyCached.field(LegacyAssets.BELLOWS, "Bag");
    private static final LegacyCached<ModelPart> TOP = LegacyCached.field(LegacyAssets.BELLOWS, "TopPlank");
    private static final LegacyCached<ModelPart> MIDDLE = LegacyCached.field(LegacyAssets.BELLOWS, "MiddlePlank");
    private static final LegacyCached<ModelPart> BOTTOM = LegacyCached.field(LegacyAssets.BELLOWS, "BottomPlank");
    private static final LegacyCached<ModelPart> NOZZLE = LegacyCached.field(LegacyAssets.BELLOWS, "Nozzle");

    public BellowsRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BellowsBlockEntity bellows, float partial, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        Direction facing = bellows.getBlockState().getBlock() instanceof BellowsBlock
                ? bellows.getBlockState().getValue(BellowsBlock.FACING) : Direction.WEST;
        draw(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), facing, bellows.inflation(partial),
                light, overlay);
        if (bellows.getLevel() != null && bellows.getLevel().getBlockEntity(bellows.getBlockPos().relative(facing))
                instanceof me.moonscenty.alchemia.block.entity.BufferTubeBlockEntity) {
            pose.pushPose();
            pose.translate(facing.getStepX(), facing.getStepY(), facing.getStepZ());
            ArcaneBoreBaseRenderer.drawNozzle(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(
                    ArcaneBoreRenderer.TEXTURE)), facing.getOpposite(), light, overlay);
            pose.popPose();
        }
    }

    static void draw(PoseStack pose, VertexConsumer buffer, Direction facing, float inflation, int light,
            int overlay) {
        ModelPart bag = BAG.get();
        ModelPart top = TOP.get();
        ModelPart middle = MIDDLE.get();
        ModelPart bottom = BOTTOM.get();
        ModelPart nozzle = NOZZLE.get();
        if (bag == null || top == null || middle == null || bottom == null || nozzle == null) {
            return;
        }
        float apart = CLOSEST + inflation * SPREADS;
        pose.pushPose();
        orient(pose, facing);
        pose.translate(0.0F, 1.0F, 0.0F);

        pose.pushPose();
        pose.scale(0.5F, (inflation + 0.1F) / 2.0F, 0.5F);
        bag.x = 0.0F;
        bag.y = 0.5F;
        bag.z = 0.0F;
        bag.render(pose, buffer, light, overlay);
        pose.popPose();

        pose.translate(0.0F, -1.0F, 0.0F);
        pose.pushPose();
        pose.translate(0.0F, -apart / 2.0F + 0.5F, 0.0F);
        top.render(pose, buffer, light, overlay);
        pose.popPose();
        pose.pushPose();
        pose.translate(0.0F, apart / 2.0F - 0.5F, 0.0F);
        bottom.render(pose, buffer, light, overlay);
        pose.popPose();

        middle.render(pose, buffer, light, overlay);
        nozzle.render(pose, buffer, light, overlay);
        pose.popPose();
    }

    private static void orient(PoseStack pose, Direction facing) {
        pose.translate(0.5F, -0.5F, 0.5F);
        switch (facing) {
            case DOWN -> {
                pose.translate(0.0F, 1.0F, -1.0F);
                pose.mulPose(Axis.XP.rotationDegrees(90.0F));
            }
            case UP -> {
                pose.translate(0.0F, 1.0F, 1.0F);
                pose.mulPose(Axis.XP.rotationDegrees(270.0F));
            }
            case NORTH -> pose.mulPose(Axis.YP.rotationDegrees(180.0F));
            case WEST -> pose.mulPose(Axis.YP.rotationDegrees(270.0F));
            case EAST -> pose.mulPose(Axis.YP.rotationDegrees(90.0F));
            default -> {
                // south is how the model was built
            }
        }
    }
}
