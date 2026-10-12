package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.ArcaneBoreBlock;
import me.moonscenty.alchemia.block.entity.ArcaneBoreBlockEntity;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
import me.moonscenty.alchemia.client.legacy.LegacyRenderTypes;
import me.moonscenty.alchemia.client.legacy.model.LegacyModel;
import me.moonscenty.alchemia.client.legacy.model.LegacyModelBaker;
import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Draws the arcane bore as the original did: a stand that turns about the upright, a nozzle that tips on it, an
 * emitter spinning at its tip with the focus's knob on it when there is a focus, and a glass shell round the middle;
 * and while it works, two beams, green and orange, from it to the block it is eating.
 * <p>
 * The parts are the original's models ({@code ModelBore}, {@code ModelBoreEmit} and the jar's core), read out of
 * its code. The original's spinning vortex inside the shell is not drawn.
 */
public class ArcaneBoreRenderer implements BlockEntityRenderer<ArcaneBoreBlockEntity> {
    static final ResourceLocation TEXTURE = Alchemia.id("textures/entity/bore.png");
    private static final ResourceLocation GLASS = Alchemia.id("textures/entity/jar.png");
    private static final ResourceLocation BEAM_GREEN = Alchemia.id("textures/misc/bore_beam_1.png");
    private static final ResourceLocation BEAM_ORANGE = Alchemia.id("textures/misc/bore_beam_2.png");
    private static final String[] BASE = {"Base", "Side1", "Side2", "NozCrossbar"};
    private static final String[] NOZZLE = {"NozFront", "NozMid"};
    private static final String[] EMITTER = {"Cross1", "Cross3", "Cross2", "Rod"};

    /** The original's parts, as of the import {@link #bakedFrom} names. */
    private static int bakedFrom = -1;
    private static ModelPart[] base;
    private static ModelPart[] nozzle;
    private static ModelPart[] emitter;
    private static ModelPart knob;
    private static ModelPart core;

    public ArcaneBoreRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ArcaneBoreBlockEntity bore, float partial, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        Direction facing = bore.getBlockState().getValue(ArcaneBoreBlock.FACING);
        boolean hanging = bore.getLevel() != null
                && bore.getLevel().getBlockState(bore.getBlockPos().above()).is(ModBlocks.ARCANE_BORE_BASE.get());
        float aimX = 0.0F;
        float aimZ = 0.0F;
        BlockPos target = bore.target();
        if (bore.working() && target != null) {
            float[] aim = aim(bore.getBlockPos(), target, facing);
            aimX = aim[0];
            aimZ = aim[1];
        }
        draw(pose, buffers, facing, hanging, bore.hasFocus(), bore.spin(partial), aimX, aimZ, light, overlay);
        if (bore.working() && target != null) {
            beams(pose, buffers, bore.getBlockPos(), target, bore.getLevel() == null ? 0.0F
                    : bore.getLevel().getGameTime() + partial);
        }
    }

    /** The two turns the original gave the head for each way it can face: about the upright, then tipped. */
    private static float[] turns(Direction facing) {
        return switch (facing) {
            case DOWN -> new float[] {0.0F, 180.0F};
            case UP -> new float[] {0.0F, 0.0F};
            case NORTH -> new float[] {270.0F, 90.0F};
            case SOUTH -> new float[] {90.0F, 90.0F};
            case WEST -> new float[] {0.0F, 90.0F};
            case EAST -> new float[] {180.0F, 90.0F};
        };
    }

    /** How far the head turns off its rest to point at the block it is eating, as the original worked it out. */
    private static float[] aim(BlockPos bore, BlockPos target, Direction facing) {
        float[] turns = turns(facing);
        double dx = bore.getX() - target.getX();
        double dy = bore.getY() - target.getY();
        double dz = bore.getZ() - target.getZ();
        double across = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG);
        float pitch = (float) -(Mth.atan2(dy, across) * Mth.RAD_TO_DEG) + 90.0F;
        float x = Mth.wrapDegrees(turns[0]) + yaw;
        if (facing == Direction.EAST) {
            x = Mth.wrapDegrees(x);
        }
        float z = pitch - turns[1];
        if (facing.getAxis() == Direction.Axis.Y) {
            z += 180.0F;
        }
        return new float[] {Mth.wrapDegrees(x), Mth.wrapDegrees(z)};
    }

    /** The bore at rest or aiming. Shared with the item, which stands pointing up with no focus. */
    static void draw(PoseStack pose, MultiBufferSource buffers, Direction facing, boolean hanging, boolean focus,
            float spin, float aimX, float aimZ, int light, int overlay) {
        if (!refresh()) {
            return;
        }
        float[] turns = turns(facing);
        VertexConsumer solid = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        pose.pushPose();
        pose.translate(0.5F, 0.5F, 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(turns[0] - aimX));
        pose.pushPose();
        if (hanging) {
            pose.mulPose(Axis.ZP.rotationDegrees(180.0F));
        }
        pose.translate(0.0F, -0.5F, 0.0F);
        for (ModelPart part : base) {
            part.render(pose, solid, light, overlay);
        }
        pose.popPose();
        pose.mulPose(Axis.ZP.rotationDegrees(turns[1] - aimZ));
        pose.pushPose();
        pose.mulPose(Axis.ZP.rotationDegrees(90.0F));
        pose.translate(0.0F, -0.5F, 0.0F);
        for (ModelPart part : nozzle) {
            part.render(pose, solid, light, overlay);
        }
        pose.popPose();
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(spin));
        pose.translate(0.0F, 0.5F, 0.0F);
        if (focus) {
            knob.render(pose, solid, light, overlay);
        }
        for (ModelPart part : emitter) {
            part.render(pose, solid, light, overlay);
        }
        pose.popPose();
        pose.pushPose();
        pose.mulPose(Axis.ZP.rotationDegrees(180.0F));
        pose.translate(0.0F, 0.3F, 0.0F);
        pose.scale(0.6F, 0.6F, 0.6F);
        core.render(pose, buffers.getBuffer(RenderType.entityTranslucent(GLASS)), light, overlay);
        pose.popPose();
        pose.popPose();
    }

    /** The two beams from the bore to the block: green, and orange, each a pair of crossed ribbons, turning. */
    private static void beams(PoseStack pose, MultiBufferSource buffers, BlockPos from, BlockPos to, float age) {
        Vec3 along = Vec3.atCenterOf(to).subtract(Vec3.atCenterOf(from));
        ribbon(pose, buffers.getBuffer(LegacyRenderTypes.glowBothSides(BEAM_GREEN)), along, 0.15F, age * 4.0F,
                0xFF00FF66, -age * 0.1F);
        ribbon(pose, buffers.getBuffer(LegacyRenderTypes.glowBothSides(BEAM_ORANGE)), along, 0.1F, -age * 3.0F,
                0xFFFF8855, age * 0.1F);
    }

    private static void ribbon(PoseStack pose, VertexConsumer buffer, Vec3 along, float width, float turn, int colour,
            float scroll) {
        float length = (float) along.length();
        pose.pushPose();
        pose.translate(0.5F, 0.5F, 0.5F);
        Vector3f dir = along.toVector3f().normalize();
        pose.mulPose(new org.joml.Quaternionf().rotationTo(new Vector3f(0, 1, 0), dir));
        pose.mulPose(Axis.YP.rotationDegrees(turn));
        for (int side = 0; side < 2; side++) {
            pose.mulPose(Axis.YP.rotationDegrees(90.0F));
            Matrix4f matrix = pose.last().pose();
            float v0 = scroll;
            float v1 = scroll + length;
            corner(buffer, matrix, pose, -width, 0.0F, 0.0F, v0, colour);
            corner(buffer, matrix, pose, width, 0.0F, 1.0F, v0, colour);
            corner(buffer, matrix, pose, width, length, 1.0F, v1, colour);
            corner(buffer, matrix, pose, -width, length, 0.0F, v1, colour);
        }
        pose.popPose();
    }

    private static void corner(VertexConsumer buffer, Matrix4f matrix, PoseStack pose, float x, float y, float u,
            float v, int colour) {
        buffer.addVertex(matrix, x, y, 0.0F).setColor(colour).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT).setNormal(pose.last(), 0.0F, 0.0F, 1.0F);
    }

    @Override
    public boolean shouldRenderOffScreen(ArcaneBoreBlockEntity bore) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(ArcaneBoreBlockEntity bore) {
        return new AABB(bore.getBlockPos()).inflate(66.0);
    }

    /** Bakes the original's parts again whenever the import has changed what there is. */
    private static boolean refresh() {
        int generation = LegacyModels.generation();
        if (generation != bakedFrom) {
            bakedFrom = generation;
            base = LegacyModels.get(LegacyAssets.BORE).map(read -> parts(read, BASE)).orElse(null);
            nozzle = LegacyModels.get(LegacyAssets.BORE).map(read -> parts(read, NOZZLE)).orElse(null);
            emitter = LegacyModels.get(LegacyAssets.BORE_EMITTER).map(read -> parts(read, EMITTER)).orElse(null);
            knob = LegacyModels.get(LegacyAssets.BORE_EMITTER).map(read -> LegacyModelBaker.field(read, "Knob"))
                    .orElse(null);
            core = LegacyModels.get(LegacyAssets.JAR).map(read -> LegacyModelBaker.field(read, "Core"))
                    .orElse(null);
        }
        return base != null && nozzle != null && emitter != null && knob != null && core != null;
    }

    static ModelPart[] parts(LegacyModel read, String[] names) {
        ModelPart[] parts = new ModelPart[names.length];
        for (int i = 0; i < names.length; i++) {
            parts[i] = LegacyModelBaker.field(read, names[i]);
        }
        return parts;
    }
}
