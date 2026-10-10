package me.moonscenty.alchemia.client;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import me.moonscenty.alchemia.block.entity.CrucibleBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.block.Blocks;

/**
 * The water in a crucible, as the original drew it: not part of the pot's model, but one sheet of the water picture
 * laid across the pot at the height {@link CrucibleBlockEntity#surface()} works out, in the colour
 * {@link CrucibleBlockEntity#colour()} gives it.
 */
public class CrucibleRenderer implements BlockEntityRenderer<CrucibleBlockEntity> {
    public CrucibleRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CrucibleBlockEntity crucible, float partial, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        if (crucible.water() <= 0) {
            return;
        }
        TextureAtlasSprite water = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper()
                .getParticleIcon(Blocks.WATER.defaultBlockState());
        int colour = crucible.colour();
        float red = ((colour >> 16) & 0xFF) / 255.0F;
        float green = ((colour >> 8) & 0xFF) / 255.0F;
        float blue = (colour & 0xFF) / 255.0F;
        float y = crucible.surface();

        VertexConsumer buffer = buffers.getBuffer(Sheets.translucentCullBlockSheet());
        PoseStack.Pose last = pose.last();
        Matrix4f matrix = last.pose();
        corner(buffer, last, matrix, 0.0F, y, 1.0F, water.getU0(), water.getV0(), red, green, blue, light);
        corner(buffer, last, matrix, 1.0F, y, 1.0F, water.getU1(), water.getV0(), red, green, blue, light);
        corner(buffer, last, matrix, 1.0F, y, 0.0F, water.getU1(), water.getV1(), red, green, blue, light);
        corner(buffer, last, matrix, 0.0F, y, 0.0F, water.getU0(), water.getV1(), red, green, blue, light);
    }

    private static void corner(VertexConsumer buffer, PoseStack.Pose last, Matrix4f matrix, float x, float y, float z,
            float u, float v, float red, float green, float blue, int light) {
        buffer.addVertex(matrix, x, y, z).setColor(red, green, blue, 1.0F).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(last, 0.0F, 1.0F, 0.0F);
    }
}
