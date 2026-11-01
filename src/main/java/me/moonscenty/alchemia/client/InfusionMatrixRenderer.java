package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.entity.InfusionMatrixBlockEntity;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyCached;
import me.moonscenty.alchemia.client.legacy.LegacyRenderTypes;
import me.moonscenty.alchemia.client.legacy.model.LegacyMesh;
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

public class InfusionMatrixRenderer implements BlockEntityRenderer<InfusionMatrixBlockEntity> {
    // mesh leans to its south-east; these turns are counter-clockwise, unlike blockstate rotation
    private static final float[][] PILLARS = {
            {1, -2, -1, 270},
            {1, -2, 1, 180},
            {-1, -2, 1, 90},
            {-1, -2, -1, 0},
    };

    private static final float OUT = 0.25F;
    private static final float SMALL = 0.45F;
    private static final float TIP_X = 35.0F, TIP_Z = 45.0F;
    private static final float TURNS_BY = 1.0F;
    private static final float WINDS_UP = 60.0F;
    private static final float WANDER = 0.09F;
    private static final float[] WANDERS_IN = {15.0F, 14.0F, 13.0F};

    private static final ResourceLocation STONE_TEXTURE = Alchemia.id("textures/entity/infusion_matrix.png");
    private static final ResourceLocation PILLAR_TEXTURE = Alchemia.id("textures/entity/arcane_pillar.png");
    private static final ResourceLocation ANCIENT_STONE_TEXTURE = Alchemia.id("textures/entity/infusion_matrix_ancient.png");
    private static final ResourceLocation ANCIENT_PILLAR_TEXTURE = Alchemia.id("textures/entity/ancient_pillar.png");
    private static final ResourceLocation ELDRITCH_STONE_TEXTURE = Alchemia.id("textures/entity/infusion_matrix_eldritch.png");
    private static final ResourceLocation ELDRITCH_PILLAR_TEXTURE = Alchemia.id("textures/entity/eldritch_pillar.png");
    private static final float GLOW_R = 0.8F, GLOW_G = 0.1F, GLOW_B = 1.0F;

    private static final LegacyCached<ModelPart> STONE = LegacyCached.field(LegacyAssets.INFUSER, "cube");
    private static final LegacyCached<ModelPart> GLOW = LegacyCached.field(LegacyAssets.INFUSER_GLOW, "cube");
    private static final LegacyCached<LegacyMesh> PILLAR = LegacyCached.mesh(LegacyAssets.PILLAR_MESH);

    public InfusionMatrixRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(InfusionMatrixBlockEntity matrix, float partial, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        ModelPart legacyStone = STONE.get();
        ModelPart legacyGlow = GLOW.get();
        LegacyMesh pillarMesh = PILLAR.get();
        if (matrix.getLevel() == null || legacyStone == null || pillarMesh == null) {
            return;
        }
        float ticks = matrix.getLevel().getGameTime() + partial;
        float since = matrix.awake() ? Math.max(0.0F, ticks - matrix.wokenAt()) : 0.0F;
        float running = Math.min(1.0F, since / WINDS_UP);
        float shake = matrix.busy() ? matrix.instability() * running : 0.0F;
        var first = matrix.getLevel().getBlockState(matrix.getBlockPos().offset(-1, -2, -1));
        me.moonscenty.alchemia.block.PillarKind kind = first.hasProperty(me.moonscenty.alchemia.block.ArcanePillarBlock.KIND)
                ? first.getValue(me.moonscenty.alchemia.block.ArcanePillarBlock.KIND)
                : me.moonscenty.alchemia.block.PillarKind.NORMAL;
        ResourceLocation stoneTexture = switch (kind) {
            case ANCIENT -> ANCIENT_STONE_TEXTURE;
            case ELDRITCH -> ELDRITCH_STONE_TEXTURE;
            default -> STONE_TEXTURE;
        };
        ResourceLocation pillarTexture = switch (kind) {
            case ANCIENT -> ANCIENT_PILLAR_TEXTURE;
            case ELDRITCH -> ELDRITCH_PILLAR_TEXTURE;
            default -> PILLAR_TEXTURE;
        };

        if (matrix.awake()) {
            for (float[] corner : PILLARS) {
                pose.pushPose();
                pose.translate(corner[0] + 0.5, corner[1], corner[2] + 0.5);
                pose.mulPose(Axis.YP.rotationDegrees(corner[3]));
                pose.translate(-0.5, 0.0, -0.5);
                // mesh is modelled from the block's corner, two blocks tall
                pillarMesh.render(pose.last(), buffers.getBuffer(RenderType.entityCutoutNoCull(pillarTexture)),
                        0xFFFFFFFF, light, OverlayTexture.NO_OVERLAY);
                pose.popPose();
            }
        }

        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(turned(since)));
        pose.mulPose(Axis.XP.rotationDegrees(TIP_X * running));
        pose.mulPose(Axis.ZP.rotationDegrees(TIP_Z * running));

        for (int east = 0; east < 2; east++) {
            for (int up = 0; up < 2; up++) {
                for (int south = 0; south < 2; south++) {
                    pose.pushPose();
                    pose.translate(
                            wander(ticks, east, 0, shake) + (east == 0 ? -OUT : OUT),
                            wander(ticks, up, 1, shake) + (up == 0 ? -OUT : OUT),
                            wander(ticks, south, 2, shake) + (south == 0 ? -OUT : OUT));
                    if (east > 0) {
                        pose.mulPose(Axis.XP.rotationDegrees(90.0F));
                    }
                    if (up > 0) {
                        pose.mulPose(Axis.YP.rotationDegrees(90.0F));
                    }
                    if (south > 0) {
                        pose.mulPose(Axis.ZP.rotationDegrees(90.0F));
                    }
                    pose.scale(SMALL, SMALL, SMALL);
                    // original's cube is a whole block about its own middle
                    legacyStone.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(stoneTexture)),
                            light, OverlayTexture.NO_OVERLAY);
                    if (legacyGlow != null && matrix.awake()) {
                        legacyGlow.render(pose, buffers.getBuffer(LegacyRenderTypes.glow(stoneTexture)),
                                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                                glow(ticks, east, up, south, running));
                    }
                    pose.popPose();
                }
            }
        }
        pose.popPose();
    }

    private static int glow(float ticks, int east, int up, int south, float running) {
        float strength = (Mth.sin((ticks + east * 2 + up * 3 + south * 4) / 4.0F) * 0.1F + 0.2F) * running;
        return FastColor.ARGB32.colorFromFloat(strength, GLOW_R, GLOW_G, GLOW_B);
    }

    // integrated angle: angle * speed would spin backwards while winding up
    private static float turned(float since) {
        return since < WINDS_UP
                ? TURNS_BY * since * since / (2.0F * WINDS_UP)
                : TURNS_BY * (since - WINDS_UP / 2.0F);
    }

    private static float wander(float ticks, int which, int axis, float shake) {
        return shake <= 0.0F ? 0.0F
                : Mth.sin((ticks + which * 10.0F) / WANDERS_IN[axis]) * WANDER * shake
                        / InfusionMatrixBlockEntity.WORST;
    }

    // covers the pillars below too, or they get culled at some angles
    @Override
    public AABB getRenderBoundingBox(InfusionMatrixBlockEntity matrix) {
        return new AABB(matrix.getBlockPos()).inflate(2.0, 3.0, 2.0);
    }

    @Override
    public int getViewDistance() {
        return 96;
    }
}
