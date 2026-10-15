package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.PatternCrafterBlock;
import me.moonscenty.alchemia.block.entity.PatternCrafterBlockEntity;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * The pattern crafter's face, as the original's {@code TilePatternCrafterRenderer} drew it: the pattern the dial is
 * set to in a window at the top, and two brass gears either side below, turning against each other for a moment
 * after each craft and running down.
 */
public class PatternCrafterRenderer implements BlockEntityRenderer<PatternCrafterBlockEntity> {
    /** The ten patterns side by side, one sixteen pixel picture each. */
    private static final ResourceLocation MODES = Alchemia.id("textures/block/legacy/pattern_crafter_modes.png");
    private static final float HALF = 0.25F;

    public PatternCrafterRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(PatternCrafterBlockEntity crafter, float partial, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        Direction facing = crafter.getBlockState().getValue(PatternCrafterBlock.FACING);
        pose.pushPose();
        pose.translate(0.5, 0.75, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(switch (facing) {
            case NORTH -> 180.0F;
            case WEST -> 270.0F;
            case EAST -> 90.0F;
            default -> 0.0F;
        }));

        // the window: on the face, which is the block's north side once turned
        float u0 = crafter.mode() / (float) PatternCrafterBlockEntity.PATTERNS.length;
        float u1 = (crafter.mode() + 1) / (float) PatternCrafterBlockEntity.PATTERNS.length;
        float z = -0.501F;
        VertexConsumer window = buffers.getBuffer(RenderType.entityCutoutNoCull(MODES));
        PoseStack.Pose at = pose.last();
        vertex(window, at, HALF, -HALF, z, u0, 1.0F, light);
        vertex(window, at, -HALF, -HALF, z, u1, 1.0F, light);
        vertex(window, at, -HALF, HALF, z, u1, 0.0F, light);
        vertex(window, at, HALF, HALF, z, u0, 0.0F, light);

        // the gears, turning against each other
        float turned = crafter.angle(partial) % 360.0F;
        ItemStack gear = new ItemStack(ModItems.BRASS_GEAR.get());
        for (int side = -1; side <= 1; side += 2) {
            pose.pushPose();
            pose.translate(0.2F * side, -0.40625F, 0.05F);
            pose.mulPose(Axis.ZP.rotationDegrees(turned * side));
            Minecraft.getInstance().getItemRenderer().renderStatic(gear, ItemDisplayContext.FIXED, light,
                    OverlayTexture.NO_OVERLAY, pose, buffers, crafter.getLevel(), 0);
            pose.popPose();
        }
        pose.popPose();
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u,
            float v, int light) {
        buffer.addVertex(pose, x, y, z).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(pose, 0.0F, 0.0F, -1.0F);
    }
}
