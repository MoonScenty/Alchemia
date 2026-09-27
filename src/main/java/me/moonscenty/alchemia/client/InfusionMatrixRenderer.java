package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.entity.InfusionMatrixBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.Mth;

/**
 * The eight stones a matrix is made of, turning in the air.
 * <p>
 * One stone is drawn eight times: pushed out to each corner of a small cube, shrunk, and each one turned a quarter
 * differently from the last, so the same six runes read as a different face wherever you stand. The whole cluster
 * leans and spins.
 * <p>
 * Idle, it hangs still and square. Once a working starts it winds up over a few seconds rather than snapping into
 * motion, and the shakier the work the further each stone wanders off its corner -- which is the only warning
 * anybody gets before the lightning.
 */
public class InfusionMatrixRenderer implements BlockEntityRenderer<InfusionMatrixBlockEntity> {
    public static final ModelResourceLocation CUBE =
            ModelResourceLocation.standalone(Alchemia.id("block/infusion_cube"));

    /** How far each stone sits from the middle, and how big it is drawn. */
    private static final float OUT = 0.25F;
    private static final float SMALL = 0.45F;
    /** How the cluster is tipped, and how long one turn of it takes in ticks. */
    private static final float TIP_X = 35.0F, TIP_Z = 45.0F;
    private static final float TURNS_IN = 360.0F;
    /** How long the working takes to wind up to full speed, in ticks. */
    private static final float WINDS_UP = 60.0F;
    /** How far a stone wanders at its worst, and how fast it wanders there and back. */
    private static final float WANDER = 0.09F;
    private static final float[] WANDERS_IN = {15.0F, 14.0F, 13.0F};

    public InfusionMatrixRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(InfusionMatrixBlockEntity matrix, float partial, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        Minecraft client = Minecraft.getInstance();
        BakedModel stone = client.getModelManager().getModel(CUBE);
        float ticks = matrix.getLevel() == null ? 0.0F : matrix.getLevel().getGameTime() % 100000L + partial;
        // nothing snaps into motion: the working takes a few seconds to come up to speed and back down again
        float running = Math.min(1.0F, (matrix.turning() + (matrix.busy() ? partial : 0.0F)) / WINDS_UP);
        float shake = matrix.busy() ? matrix.instability() * running : 0.0F;

        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(ticks % TURNS_IN * running));
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
                    // each stone turned differently, so one cube of six runes never reads the same twice
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
                    pose.translate(-0.5, -0.5, -0.5);
                    client.getBlockRenderer().getModelRenderer().renderModel(pose.last(),
                            buffers.getBuffer(RenderType.cutout()), null, stone,
                            1.0F, 1.0F, 1.0F, light, OverlayTexture.NO_OVERLAY);
                    pose.popPose();
                }
            }
        }
        pose.popPose();
    }

    /** How far one stone has wandered off its corner just now. Shaky work shakes itself apart slowly. */
    private static float wander(float ticks, int which, int axis, float shake) {
        return shake <= 0.0F ? 0.0F
                : Mth.sin((ticks + which * 10.0F) / WANDERS_IN[axis]) * WANDER * shake
                        / InfusionMatrixBlockEntity.WORST;
    }

    /** It is drawn well outside its own block, so it must not be cut off at the edge of one. */
    @Override
    public boolean shouldRenderOffScreen(InfusionMatrixBlockEntity matrix) {
        return true;
    }
}
