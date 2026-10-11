package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.HungryChestBlock;
import me.moonscenty.alchemia.block.entity.HungryChestBlockEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Draws a hungry chest as the original did: the game's chest as it was built when the original was written, on
 * the original's own sheet.
 *
 * <p>The game's chest has been rebuilt since, and the pictures painted for the old one come out wrong on the new
 * one -- faces upside down, the latch on backwards. So the old chest is put together here, box for box, and the
 * old picture goes on it as it was meant to.
 */
public class HungryChestRenderer implements BlockEntityRenderer<HungryChestBlockEntity> {
    static final ResourceLocation TEXTURE = Alchemia.id("textures/entity/hungry_chest.png");

    private static final ModelPart CHEST = build();
    private static final ModelPart LID = CHEST.getChild("lid");
    private static final ModelPart KNOB = CHEST.getChild("knob");
    private static final ModelPart BELOW = CHEST.getChild("below");

    public HungryChestRenderer(BlockEntityRendererProvider.Context context) {
    }

    /** The chest of that time: a lid, a latch on its front, and the box underneath. */
    private static ModelPart build() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 0)
                .addBox(0.0F, -5.0F, -14.0F, 14, 5, 14), PartPose.offset(1.0F, 7.0F, 15.0F));
        root.addOrReplaceChild("knob", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-1.0F, -2.0F, -15.0F, 2, 4, 1), PartPose.offset(8.0F, 7.0F, 15.0F));
        root.addOrReplaceChild("below", CubeListBuilder.create().texOffs(0, 19)
                .addBox(0.0F, 0.0F, 0.0F, 14, 10, 14), PartPose.offset(1.0F, 6.0F, 1.0F));
        return LayerDefinition.create(mesh, 64, 64).bakeRoot();
    }

    @Override
    public void render(HungryChestBlockEntity chest, float partial, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        Direction facing = chest.getBlockState().getBlock() instanceof HungryChestBlock
                ? chest.getBlockState().getValue(HungryChestBlock.FACING) : Direction.SOUTH;
        draw(pose, buffers.getBuffer(RenderType.entityCutout(TEXTURE)), facing, chest.lid(partial), light, overlay);
    }

    /** The chest facing a given way with its lid so far open. Shared with the item, which is always shut. */
    static void draw(PoseStack pose, VertexConsumer buffer, Direction facing, float open, int light, int overlay) {
        pose.pushPose();
        // the old renderer's turns, in its order: upside down into model space, then round to face
        pose.translate(0.0F, 1.0F, 1.0F);
        pose.scale(1.0F, -1.0F, -1.0F);
        pose.translate(0.5F, 0.5F, 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(turn(facing)));
        pose.translate(-0.5F, -0.5F, -0.5F);

        float eased = 1.0F - open;
        eased = 1.0F - eased * eased * eased;
        LID.xRot = -(eased * Mth.HALF_PI);
        KNOB.xRot = LID.xRot;
        LID.render(pose, buffer, light, overlay);
        KNOB.render(pose, buffer, light, overlay);
        BELOW.render(pose, buffer, light, overlay);
        pose.popPose();
    }

    private static float turn(Direction facing) {
        return switch (facing) {
            case NORTH -> 180.0F;
            case WEST -> 90.0F;
            case EAST -> -90.0F;
            default -> 0.0F;
        };
    }
}
