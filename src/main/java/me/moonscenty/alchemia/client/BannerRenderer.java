package me.moonscenty.alchemia.client;

import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.BannerBlockEntity;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
import me.moonscenty.alchemia.client.legacy.model.LegacyModelBaker;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

/**
 * A banner as the original drew it, on its own model: the pole (unless it hangs from a wall), the beam, the two tabs
 * and the cloth, the cloth stirring in a slow hundred-tick sway; the cloth and tabs in the banner's colour, and the
 * aspect painted on it swinging twice as far.
 */
public class BannerRenderer implements BlockEntityRenderer<BannerBlockEntity> {
    private static final ResourceLocation CULT = Alchemia.id("textures/entity/banner_cultist.png");
    private static final ResourceLocation BLANK = Alchemia.id("textures/entity/banner_blank.png");

    private static int bakedFrom = -1;
    private static Map<String, ModelPart> parts = Map.of();

    public BannerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BannerBlockEntity banner, float partial, PoseStack pose, MultiBufferSource buffers, int light,
            int overlay) {
        if (!refresh()) {
            return;
        }
        long time = banner.getLevel() == null ? 0 : banner.getLevel().getGameTime();
        boolean cult = banner.isCult() && banner.aspect() == null;
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(cult ? CULT : BLANK));
        int colour = banner.colour() == null ? -1 : FastColor.ARGB32.opaque(banner.colour().getTextureDiffuseColor());

        pose.pushPose();
        pose.translate(0.5F, 1.5F, 0.5F);
        pose.mulPose(Axis.XP.rotationDegrees(180.0F));
        pose.mulPose(Axis.YP.rotationDegrees(180.0F));
        pose.mulPose(Axis.YP.rotationDegrees(banner.facing() * 360.0F / 16.0F));
        if (!banner.onWall()) {
            parts.get("Pole").render(pose, buffer, light, overlay);
        } else {
            pose.translate(0.0F, 1.0F, -0.4125F);
        }
        parts.get("Beam").render(pose, buffer, light, overlay);
        parts.get("B1").render(pose, buffer, light, overlay, colour);
        parts.get("B2").render(pose, buffer, light, overlay, colour);
        float t = banner.getBlockPos().getX() * 7 + banner.getBlockPos().getY() * 9 + banner.getBlockPos().getZ() * 13
                + time + partial;
        float wave = (0.005F + 0.005F * Mth.cos(t * Mth.PI * 0.02F)) * Mth.PI;
        ModelPart cloth = parts.get("Banner");
        cloth.xRot = wave;
        cloth.render(pose, buffer, light, overlay, colour);

        if (banner.aspect() != null) {
            pose.pushPose();
            pose.translate(0.0F, 0.0F, 0.05001F);
            pose.scale(0.0375F, 0.0375F, 0.0375F);
            pose.mulPose(Axis.YP.rotationDegrees(180.0F));
            pose.mulPose(Axis.XP.rotationDegrees(-wave * Mth.RAD_TO_DEG * 2.0F));
            int tint = banner.aspect().value().color();
            VertexConsumer icon = buffers.getBuffer(RenderType.entityTranslucent(banner.aspect().value().icon()));
            Matrix4f matrix = pose.last().pose();
            corner(icon, matrix, -8.0F, 4.0F, 0.0F, 0.0F, tint);
            corner(icon, matrix, -8.0F, 20.0F, 0.0F, 1.0F, tint);
            corner(icon, matrix, 8.0F, 20.0F, 1.0F, 1.0F, tint);
            corner(icon, matrix, 8.0F, 4.0F, 1.0F, 0.0F, tint);
            pose.popPose();
        }
        pose.popPose();
    }

    private static void corner(VertexConsumer buffer, Matrix4f matrix, float x, float y, float u, float v, int tint) {
        buffer.addVertex(matrix, x, y, 0.0F).setColor(tint >> 16 & 0xFF, tint >> 8 & 0xFF, tint & 0xFF, 191)
                .setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0.0F, 0.0F, 1.0F);
    }

    private static boolean refresh() {
        if (bakedFrom != LegacyModels.generation()) {
            bakedFrom = LegacyModels.generation();
            Map<String, ModelPart> baked = new HashMap<>();
            LegacyModels.get(LegacyAssets.BANNER).ifPresent(model -> {
                for (String name : new String[] {"B1", "B2", "Beam", "Banner", "Pole"}) {
                    baked.put(name, LegacyModelBaker.field(model, name));
                }
            });
            parts = baked;
        }
        return parts.size() == 5;
    }

    @Override
    public AABB getRenderBoundingBox(BannerBlockEntity banner) {
        return banner.getRenderBoundingBox();
    }
}
