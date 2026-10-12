package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.CrystallizerBlock;
import me.moonscenty.alchemia.block.entity.CrystallizerBlockEntity;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
import me.moonscenty.alchemia.client.legacy.model.LegacyMesh;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Draws the crystallizer as the original did: its body on the original's mesh, laid along the way the essentia
 * goes through it, and round its far end four of the vis relay's crystals, spinning while it works, tinted toward
 * the colour of what it holds, and glowing a little brighter and dimmer in turn.
 */
public class CrystallizerRenderer implements BlockEntityRenderer<CrystallizerBlockEntity> {
    static final ResourceLocation BODY = Alchemia.id("textures/entity/crystallizer.png");
    private static final ResourceLocation CRYSTAL = Alchemia.id("textures/entity/vis_relay.png");
    private static final int GLOW_DIM = 50;
    private static final int GLOW_RANGE = 150;

    /** The original's meshes as of the import {@link #meshFrom} names, turned over for the loader that drew them. */
    private static int meshFrom = -1;
    private static LegacyMesh body;
    private static LegacyMesh relay;

    public CrystallizerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CrystallizerBlockEntity crystallizer, float partial, PoseStack pose,
            MultiBufferSource buffers, int light, int overlay) {
        float age = crystallizer.getLevel() == null ? 0.0F : crystallizer.getLevel().getGameTime() + partial;
        draw(pose, buffers, crystallizer.getBlockState().getValue(CrystallizerBlock.FACING),
                crystallizer.spin(partial), crystallizer.tint(), age, light, overlay);
    }

    /** The crystallizer facing a given way, its crystals at a given turn and colour. Shared with the item. */
    static void draw(PoseStack pose, MultiBufferSource buffers, Direction facing, float spin, int tint, float age,
            int light, int overlay) {
        if (!refresh()) {
            return;
        }
        pose.pushPose();
        pose.translate(0.5F, 0.5F, 0.5F);
        // the mesh runs from the way in at z=0 to the way out at z=1, and is turned so the way in is where it faces
        switch (facing) {
            case DOWN -> pose.mulPose(Axis.XP.rotationDegrees(-90.0F));
            case UP -> pose.mulPose(Axis.XP.rotationDegrees(90.0F));
            case SOUTH -> pose.mulPose(Axis.YP.rotationDegrees(180.0F));
            case WEST -> pose.mulPose(Axis.YP.rotationDegrees(90.0F));
            case EAST -> pose.mulPose(Axis.YP.rotationDegrees(-90.0F));
            default -> {
            }
        }
        pose.translate(0.0F, 0.0F, -0.5F);
        var solid = buffers.getBuffer(RenderType.entityCutoutNoCull(BODY));
        for (int group = 0; group < body.groups(); group++) {
            body.render(group, pose.last(), solid, -1, light, overlay);
        }

        int crystal = relay.group("Crystal");
        if (crystal >= 0) {
            var glass = buffers.getBuffer(RenderType.entityTranslucent(CRYSTAL));
            for (int index = 0; index < 4; index++) {
                pose.pushPose();
                pose.scale(0.75F, 0.75F, 0.75F);
                float pulse = Mth.sin((age + index * 10) / 2.0F) * 0.05F + 0.95F;
                int glow = Math.min(LightTexture.FULL_BLOCK, (int) (GLOW_DIM + GLOW_RANGE * pulse));
                pose.mulPose(Axis.ZP.rotationDegrees(90.0F * index));
                pose.translate(0.34F, 0.0F, 1.2125F);
                pose.mulPose(Axis.ZP.rotationDegrees(spin));
                relay.render(crystal, pose.last(), glass, tint, glow, overlay);
                pose.popPose();
            }
        }
        pose.popPose();
    }

    /** The age the item uses, there being no block to ask. */
    static float clientAge(float partial) {
        var player = Minecraft.getInstance().player;
        return player == null ? 0.0F : player.tickCount + partial;
    }

    /**
     * The original's meshes, once per import. Its renderer read them with the older loader, which turned the
     * texture corners over, so they are turned over here too.
     */
    private static boolean refresh() {
        int generation = LegacyModels.generation();
        if (generation != meshFrom) {
            meshFrom = generation;
            body = LegacyModels.mesh(LegacyAssets.CRYSTALLIZER_MESH).map(LegacyMesh::flippedV).orElse(null);
            relay = LegacyModels.mesh(LegacyAssets.RELAY_MESH).map(LegacyMesh::flippedV).orElse(null);
        }
        return body != null && relay != null;
    }
}
