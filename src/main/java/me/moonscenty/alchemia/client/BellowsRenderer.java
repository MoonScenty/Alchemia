package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.BellowsBlock;
import me.moonscenty.alchemia.block.entity.BellowsBlockEntity;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
import me.moonscenty.alchemia.client.legacy.model.LegacyModelBaker;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

/**
 * Draws a pair of bellows as the original drew them: two plates either side of a leather bag, a plate across the
 * middle with the nozzle on it, and the outer plates drawn in and pushed apart as the bag breathes.
 *
 * <p>The plates and the bag are the original's model ({@code ModelBellows}), read out of its code. How they move is
 * read out of its renderer, which the decompiler could not put back together and had to be followed by hand.
 */
public class BellowsRenderer implements BlockEntityRenderer<BellowsBlockEntity> {
    static final ResourceLocation TEXTURE = Alchemia.id("textures/entity/bellows.png");
    /** How far the plates sit apart when the bag is empty, and how much further a full bag pushes them. */
    private static final float CLOSEST = 0.125F;
    private static final float SPREADS = 0.875F;

    /** The original's parts, as of the import {@link #bakedFrom} names. */
    private static int bakedFrom = -1;
    private static ModelPart bag;
    private static ModelPart top;
    private static ModelPart middle;
    private static ModelPart bottom;
    private static ModelPart nozzle;

    public BellowsRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BellowsBlockEntity bellows, float partial, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        Direction facing = bellows.getBlockState().getBlock() instanceof BellowsBlock
                ? bellows.getBlockState().getValue(BellowsBlock.FACING) : Direction.WEST;
        draw(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), facing, bellows.inflation(partial),
                light, overlay);
        // blowing into a buffer tube, the original drew a bore's nozzle on the tube, pointing back at the bellows
        if (bellows.getLevel() != null && bellows.getLevel().getBlockEntity(bellows.getBlockPos().relative(facing))
                instanceof me.moonscenty.alchemia.block.entity.BufferTubeBlockEntity) {
            pose.pushPose();
            pose.translate(facing.getStepX(), facing.getStepY(), facing.getStepZ());
            ArcaneBoreBaseRenderer.drawNozzle(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(
                    ArcaneBoreRenderer.TEXTURE)), facing.getOpposite(), light, overlay);
            pose.popPose();
        }
    }

    /**
     * The bellows at a given fullness, facing a given way. Shared with the item, which breathes on a clock of its
     * own rather than a block's.
     */
    static void draw(PoseStack pose, VertexConsumer buffer, Direction facing, float inflation, int light,
            int overlay) {
        if (!refresh()) {
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

    /** Turned so the nozzle points the way the bellows face. The original's turns, in its order. */
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
                // south is the way the model was built
            }
        }
    }

    /** Bakes the original's parts again whenever the import has changed what there is. */
    private static boolean refresh() {
        int generation = LegacyModels.generation();
        if (generation != bakedFrom) {
            bakedFrom = generation;
            var model = LegacyModels.get(LegacyAssets.BELLOWS);
            bag = model.map(read -> LegacyModelBaker.field(read, "Bag")).orElse(null);
            top = model.map(read -> LegacyModelBaker.field(read, "TopPlank")).orElse(null);
            middle = model.map(read -> LegacyModelBaker.field(read, "MiddlePlank")).orElse(null);
            bottom = model.map(read -> LegacyModelBaker.field(read, "BottomPlank")).orElse(null);
            nozzle = model.map(read -> LegacyModelBaker.field(read, "Nozzle")).orElse(null);
        }
        return bag != null && top != null && middle != null && bottom != null && nozzle != null;
    }
}
