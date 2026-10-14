package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.eldritch.EldritchAltarBlockEntity;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
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

/**
 * An eldritch capstone, and the altar, which the original drew the same way: the original's carved cap, and on the
 * altar whatever eyes have been set into it, one to a side. The original meant the altar to have a picture of its
 * own and by a slip drew the capstone's on both; so does this.
 */
public class EldritchCapRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    static final ResourceLocation CAP = Alchemia.id("textures/entity/obelisk_cap.png");

    private static int bakedFrom = -1;
    private static LegacyMesh mesh;
    private static int capGroup = -1;
    private ItemStack eye;

    public EldritchCapRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(T stone, float partial, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (refresh()) {
            pose.pushPose();
            pose.translate(0.5F, 0.0F, 0.5F);
            pose.mulPose(Axis.XN.rotationDegrees(90.0F));
            mesh.render(capGroup, pose.last(), buffers.getBuffer(RenderType.entityCutoutNoCull(CAP)), -1, light,
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

    /** The original's cap mesh, read again whenever the import has changed what there is. */
    static boolean refresh() {
        if (bakedFrom != LegacyModels.generation()) {
            bakedFrom = LegacyModels.generation();
            mesh = LegacyModels.mesh(LegacyAssets.OBELISK_CAP_MESH).map(LegacyMesh::flippedV).orElse(null);
            capGroup = mesh == null ? -1 : Math.max(0, mesh.group("Cap"));
        }
        return mesh != null;
    }

    /** The cap, for the obelisk to draw at its two ends. */
    static void drawCap(PoseStack pose, MultiBufferSource buffers, ResourceLocation texture, int light, int overlay) {
        if (refresh()) {
            mesh.render(capGroup, pose.last(), buffers.getBuffer(RenderType.entityCutoutNoCull(texture)), -1, light,
                    overlay);
        }
    }
}
