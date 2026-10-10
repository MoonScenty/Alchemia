package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.entity.InfusionMatrixBlockEntity;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
import me.moonscenty.alchemia.client.legacy.LegacyRenderTypes;
import me.moonscenty.alchemia.client.legacy.model.LegacyMesh;
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
    /**
     * Where each pillar stands, and how far it is turned.
     * <p>
     * The mesh leans towards its own south-east corner, so each of the four is turned to lean back in towards the
     * middle. A quarter turn here is counter-clockwise seen from above, the other way round from the turn a
     * blockstate gives a model, which is why these are written out rather than worked out.
     */
    private static final float[][] PILLARS = {
            {1, -2, -1, 270},
            {1, -2, 1, 180},
            {-1, -2, 1, 90},
            {-1, -2, -1, 0},
    };

    /** How far each stone sits from the middle, and how big it is drawn. */
    private static final float OUT = 0.25F;
    private static final float SMALL = 0.45F;
    /** How the cluster is tipped, and how far it turns in a tick once it is up to speed. */
    private static final float TIP_X = 35.0F, TIP_Z = 45.0F;
    private static final float TURNS_BY = 1.0F;
    /** How long the working takes to wind up to full speed, in ticks. */
    private static final float WINDS_UP = 60.0F;
    /** How far a stone wanders at its worst, and how fast it wanders there and back. */
    private static final float WANDER = 0.09F;
    private static final float[] WANDERS_IN = {15.0F, 14.0F, 13.0F};

    /** The original's pictures for its cube and its pillar. */
    private static final ResourceLocation STONE_TEXTURE = Alchemia.id("textures/entity/infusion_matrix.png");
    private static final ResourceLocation PILLAR_TEXTURE = Alchemia.id("textures/entity/arcane_pillar.png");
    /** The colour the original gave the glow. */
    private static final float GLOW_R = 0.8F, GLOW_G = 0.1F, GLOW_B = 1.0F;

    /** The original's cube, its glow and its pillar, as of the import {@link #bakedFrom} names. */
    private static int bakedFrom = -1;
    private static ModelPart legacyStone;
    private static ModelPart legacyGlow;
    private static LegacyMesh pillarMesh;

    public InfusionMatrixRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(InfusionMatrixBlockEntity matrix, float partial, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        refresh();
        if (matrix.getLevel() == null || legacyStone == null || pillarMesh == null) {
            return;
        }
        float ticks = matrix.getLevel().getGameTime() + partial;
        // how long the stones have been turning, taken from the world clock so that every frame agrees
        float since = matrix.awake() ? Math.max(0.0F, ticks - matrix.wokenAt()) : 0.0F;
        // nothing snaps into motion: a woken altar takes a few seconds to come up to speed
        float running = Math.min(1.0F, since / WINDS_UP);
        float shake = matrix.busy() ? matrix.instability() * running : 0.0F;

        // the altar itself, drawn by the matrix because it is the matrix that knows it is an altar
        if (matrix.awake()) {
            for (float[] corner : PILLARS) {
                pose.pushPose();
                pose.translate(corner[0] + 0.5, corner[1], corner[2] + 0.5);
                pose.mulPose(Axis.YP.rotationDegrees(corner[3]));
                pose.translate(-0.5, 0.0, -0.5);
                // the mesh is modelled from the block's corner, two blocks tall
                pillarMesh.render(pose.last(), buffers.getBuffer(RenderType.entityCutoutNoCull(PILLAR_TEXTURE)),
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
                    // the original's cube is a whole block about its own middle
                    legacyStone.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(STONE_TEXTURE)),
                            light, OverlayTexture.NO_OVERLAY);
                    if (legacyGlow != null && matrix.awake()) {
                        legacyGlow.render(pose, buffers.getBuffer(LegacyRenderTypes.glow(STONE_TEXTURE)),
                                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                                glow(ticks, east, up, south, running));
                    }
                    pose.popPose();
                }
            }
        }
        pose.popPose();
    }

    /** Bakes the original's cube again, and picks up its pillar, whenever the import has changed what there is. */
    private static void refresh() {
        int generation = LegacyModels.generation();
        if (generation == bakedFrom) {
            return;
        }
        bakedFrom = generation;
        legacyStone = LegacyModels.get(LegacyAssets.INFUSER).map(model -> LegacyModelBaker.field(model, "cube"))
                .orElse(null);
        legacyGlow = LegacyModels.get(LegacyAssets.INFUSER_GLOW).map(model -> LegacyModelBaker.field(model, "cube"))
                .orElse(null);
        pillarMesh = LegacyModels.mesh(LegacyAssets.PILLAR_MESH).orElse(null);
    }

    /**
     * The original's violet glow over one stone of a working matrix, breathing a little out of step with the next
     * stone. It is light added over the stone by how opaque each pixel of the glow is, as the original blended it.
     */
    private static int glow(float ticks, int east, int up, int south, float running) {
        float strength = (Mth.sin((ticks + east * 2 + up * 3 + south * 4) / 4.0F) * 0.1F + 0.2F) * running;
        return FastColor.ARGB32.colorFromFloat(strength, GLOW_R, GLOW_G, GLOW_B);
    }

    /**
     * How far round the cluster has come since it woke.
     * <p>
     * Worked out from how far it has turned rather than from how fast it is turning, because the two are not the
     * same thing while it is still winding up: multiplying the angle by the speed would wind the whole turn
     * backwards every time the speed rose, which is a stutter rather than a start.
     */
    private static float turned(float since) {
        return since < WINDS_UP
                ? TURNS_BY * since * since / (2.0F * WINDS_UP)
                : TURNS_BY * (since - WINDS_UP / 2.0F);
    }

    /** How far one stone has wandered off its corner just now. Shaky work shakes itself apart slowly. */
    private static float wander(float ticks, int which, int axis, float shake) {
        return shake <= 0.0F ? 0.0F
                : Mth.sin((ticks + which * 10.0F) / WANDERS_IN[axis]) * WANDER * shake
                        / InfusionMatrixBlockEntity.WORST;
    }

    /**
     * How much room the drawing takes, which is a good deal more than the block it belongs to.
     * <p>
     * The matrix draws the whole altar: eight stones turning in its own block, and four pillars standing two
     * blocks down, one block out and two blocks tall. Left at the usual single block, the drawing is thrown away
     * the moment that one block falls outside the view, and the pillars vanish at certain angles while you are
     * standing among them.
     */
    @Override
    public AABB getRenderBoundingBox(InfusionMatrixBlockEntity matrix) {
        return new AABB(matrix.getBlockPos()).inflate(2.0, 3.0, 2.0);
    }

    /**
     * How far off a matrix is still drawn.
     * <p>
     * Further than the usual sixty-four: an altar is a thing you walk up to across a room you built for it, and
     * the pillars popping in at the door would give that away.
     */
    @Override
    public int getViewDistance() {
        return 96;
    }
}
