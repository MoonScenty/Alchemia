package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.eldritch.EldritchAltarBlockEntity;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyCached;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
import me.moonscenty.alchemia.client.legacy.model.LegacyMesh;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

// the altar wears the capstone's texture too, as in the original
public class EldritchCapRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    static final ResourceLocation CAP = Alchemia.id("textures/entity/obelisk_cap.png");

    private static final LegacyCached<Cap> MESH = new LegacyCached<>(() -> LegacyModels.mesh(
            LegacyAssets.OBELISK_CAP_MESH).map(LegacyMesh::flippedV).map(mesh -> new Cap(mesh,
            Math.max(0, mesh.group("Cap")))).orElse(null));
    private ItemStack eye;

    public EldritchCapRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(T stone, float partial, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        Cap cap = MESH.get();
        if (cap != null) {
            pose.pushPose();
            pose.translate(0.5F, 0.0F, 0.5F);
            pose.mulPose(Axis.XN.rotationDegrees(90.0F));
            cap.mesh.render(cap.group, pose.last(), buffers.getBuffer(RenderType.entityCutoutNoCull(CAP)), -1, light,
                    overlay);
            pose.popPose();
        }
        if (stone instanceof EldritchAltarBlockEntity altar && altar.eyes() > 0) {
            if (eye == null) {
                eye = new ItemStack(ModItems.ELDRITCH_EYE.get());
            }
            for (int i = 0; i < altar.eyes(); i++) {
                pose.pushPose();
                pose.translate(0.5F, 0.0F, 0.5F);
                pose.mulPose(Axis.YP.rotationDegrees(i * 90.0F));
                pose.translate(0.46F, 0.45F, 0.0F);
                pose.mulPose(Axis.YP.rotationDegrees(90.0F));
                pose.mulPose(Axis.XN.rotationDegrees(18.0F));
                pose.scale(0.5F, 0.5F, 0.5F);
                Minecraft.getInstance().getItemRenderer().renderStatic(eye, ItemDisplayContext.FIXED, light, overlay,
                        pose, buffers, stone.getLevel(), 0);
                pose.popPose();
            }
        }
    }

    static void drawCap(PoseStack pose, MultiBufferSource buffers, ResourceLocation texture, int light, int overlay) {
        Cap cap = MESH.get();
        if (cap != null) {
            cap.mesh.render(cap.group, pose.last(), buffers.getBuffer(RenderType.entityCutoutNoCull(texture)), -1,
                    light, overlay);
        }
    }

    private record Cap(LegacyMesh mesh, int group) {
    }
}
