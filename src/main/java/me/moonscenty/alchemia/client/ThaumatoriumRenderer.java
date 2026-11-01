package me.moonscenty.alchemia.client;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.ThaumatoriumBlock;
import me.moonscenty.alchemia.block.entity.ThaumatoriumBlockEntity;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyCached;
import me.moonscenty.alchemia.client.legacy.model.LegacyMesh;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public class ThaumatoriumRenderer implements BlockEntityRenderer<ThaumatoriumBlockEntity> {
    private static final ResourceLocation TEXTURE = Alchemia.id("textures/entity/thaumatorium.png");
    private static final int SHOWS_EACH_FOR = 40;

    private static final LegacyCached<LegacyMesh> MESH = LegacyCached.flippedMesh(LegacyAssets.THAUMATORIUM_MESH);

    public ThaumatoriumRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ThaumatoriumBlockEntity it, float partial, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        LegacyMesh mesh = it.getBlockState().getValue(ThaumatoriumBlock.TOP) ? null : MESH.get();
        if (mesh == null) {
            return;
        }
        Direction front = it.getBlockState().getValue(ThaumatoriumBlock.FACING);
        pose.pushPose();
        pose.translate(0.5F, 0.0F, 0.5F);
        // mesh stands on its end, modelled facing west
        pose.mulPose(Axis.XN.rotationDegrees(90.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(switch (front) {
            case NORTH -> 270.0F;
            case SOUTH -> 90.0F;
            case EAST -> 180.0F;
            default -> 0.0F;
        }));
        mesh.render(pose.last(), buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), -1, light, overlay);
        pose.popPose();

        List<ResourceLocation> chosen = it.recipes();
        if (chosen.isEmpty() || it.getLevel() == null) {
            return;
        }
        var player = Minecraft.getInstance().player;
        int ticks = player == null ? 0 : player.tickCount;
        ResourceLocation showing = chosen.get((ticks / SHOWS_EACH_FOR) % chosen.size());
        ThaumatoriumBlockEntity.recipe(it.getLevel(), showing).ifPresent(recipe -> {
            ItemStack shown = recipe.value().result().copyWithCount(1);
            pose.pushPose();
            pose.translate(0.5 + front.getStepX() / 1.99, 1.25, 0.5 + front.getStepZ() / 1.99);
            pose.mulPose(Axis.YP.rotationDegrees(switch (front) {
                case NORTH -> 180.0F;
                case WEST -> 270.0F;
                case EAST -> 90.0F;
                default -> 0.0F;
            }));
            pose.scale(0.75F, 0.75F, 0.75F);
            Minecraft.getInstance().getItemRenderer().renderStatic(shown, ItemDisplayContext.GROUND, light, overlay,
                    pose, buffers, it.getLevel(), 0);
            pose.popPose();
        });
    }

    @Override
    public AABB getRenderBoundingBox(ThaumatoriumBlockEntity it) {
        return new AABB(it.getBlockPos()).inflate(0.1).expandTowards(0.0, 1.0, 0.0);
    }
}
