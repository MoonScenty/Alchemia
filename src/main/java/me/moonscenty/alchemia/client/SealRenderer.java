package me.moonscenty.alchemia.client;

import org.joml.Matrix4f;
import org.joml.Quaternionf;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.client.legacy.LegacyRenderTypes;
import me.moonscenty.alchemia.golem.seal.SealDisplayer;
import me.moonscenty.alchemia.golem.seal.SealEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * Seals, as the original showed them to whoever was holding a seal, a golem or a bell: each seal's picture flat on
 * the face it is stuck to, a slowly turning ring over it in the colour of the golems it is set for, and the corners
 * of the area it works over. They fade with distance and are not shown past sixteen blocks. Crouching, they show
 * through walls. A seal stopped by redstone is drawn dim and gives off the odd red spark.
 */
@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public final class SealRenderer {
    private static final ResourceLocation RING = Alchemia.id("textures/misc/seal_area.png");
    private static final ResourceLocation CORNER = Alchemia.id("textures/misc/frame_corner.png");
    private static final double SEEN_SQUARED = 256.0;
    private static final int GLOW = 0xF000F0;

    /** The faces of each corner of an area its frame is drawn on, and how far each is turned: the original's. */
    private static final Direction[][] CORNER_FACES = {
            {Direction.DOWN, Direction.NORTH, Direction.WEST},
            {Direction.UP, Direction.NORTH, Direction.WEST},
            {Direction.DOWN, Direction.NORTH, Direction.EAST},
            {Direction.UP, Direction.NORTH, Direction.EAST},
            {Direction.DOWN, Direction.SOUTH, Direction.EAST},
            {Direction.UP, Direction.SOUTH, Direction.EAST},
            {Direction.DOWN, Direction.SOUTH, Direction.WEST},
            {Direction.UP, Direction.SOUTH, Direction.WEST}};
    private static final int[][] CORNER_TURNS = {{0, 270, 0}, {270, 180, 270}, {90, 0, 90}, {180, 90, 180},
            {180, 180, 0}, {90, 270, 270}, {270, 90, 90}, {0, 0, 180}};

    private SealRenderer() {
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || !(player.getMainHandItem().getItem() instanceof SealDisplayer)
                || ClientSeals.all().isEmpty()) {
            return;
        }
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        boolean through = player.isShiftKeyDown();
        Vec3 camera = event.getCamera().getPosition();
        PoseStack pose = new PoseStack();
        pose.translate(-camera.x, -camera.y, -camera.z);
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        for (SealEntity seal : ClientSeals.all()) {
            double away = player.blockPosition().distSqr(seal.pos().pos());
            if (away > SEEN_SQUARED) {
                continue;
            }
            float alpha = 1.0F - (float) (away / SEEN_SQUARED);
            boolean dim = false;
            if (seal.stoppedByRedstone(player.level())) {
                dim = true;
                if (player.level().random.nextFloat() < partialTick / 12.0F) {
                    spark(player, seal);
                    dim = false;
                }
            }
            icon(pose, buffers, seal, alpha, dim, through);
            ring(pose, buffers, seal, alpha, player.tickCount + partialTick, through);
        }
        buffers.endBatch();
    }

    private static void spark(LocalPlayer player, SealEntity seal) {
        Direction face = seal.pos().face();
        float red = 0.75F - player.level().random.nextFloat() * 0.2F;
        player.level().addParticle(new DustParticleOptions(new org.joml.Vector3f(red, 0.0F, 0.0F), 0.6F),
                seal.pos().pos().getX() + 0.5 + face.getStepX() * 0.66, seal.pos().pos().getY() + 0.5
                        + face.getStepY() * 0.66, seal.pos().pos().getZ() + 0.5 + face.getStepZ() * 0.66,
                0.0, 0.0, 0.0);
    }

    /** The seal's own picture, half a block across, laid just off the face it is on. */
    private static void icon(PoseStack pose, MultiBufferSource buffers, SealEntity seal, float alpha, boolean dim,
            boolean through) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(seal.seal().icon());
        float x = seal.pos().pos().getX();
        float y = seal.pos().pos().getY();
        float z = seal.pos().pos().getZ();
        pose.pushPose();
        switch (seal.pos().face()) {
            case UP -> {
                pose.translate(x + 0.25F, y + 1.0F, z + 0.75F);
                pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90.0F));
            }
            case DOWN -> {
                pose.translate(x + 0.25F, y, z + 0.25F);
                pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90.0F));
            }
            case SOUTH -> pose.translate(x + 0.25F, y + 0.25F, z + 1.0F);
            case NORTH -> {
                pose.translate(x + 0.75F, y + 0.25F, z);
                pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F));
            }
            case EAST -> {
                pose.translate(x + 1.0F, y + 0.25F, z + 0.75F);
                pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90.0F));
            }
            case WEST -> {
                pose.translate(x, y + 0.25F, z + 0.25F);
                pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90.0F));
            }
        }
        pose.translate(0.0F, 0.0F, 0.05F);
        pose.scale(0.5F, 0.5F, 0.5F);
        float shade = dim ? 0.5F : 1.0F;
        int colour = colour(shade, shade, shade, alpha);
        VertexConsumer consumer = buffers.getBuffer(LegacyRenderTypes.seal(TextureAtlas.LOCATION_BLOCKS, through));
        // the original drew the picture with its sides swapped, and the turns above allow for it
        quad(consumer, pose, 0.0F, 1.0F, sprite.getU1(), sprite.getU0(), sprite.getV0(), sprite.getV1(), colour);
        pose.popPose();
    }

    /** The turning ring on the face, and for an area seal the corners of its area. */
    private static void ring(PoseStack pose, MultiBufferSource buffers, SealEntity seal, float alpha, float ticks,
            boolean through) {
        float r;
        float g;
        float b;
        if (seal.colour() > 0) {
            int dye = DyeColor.byId(seal.colour() - 1).getMapColor().col;
            r = (dye >> 16 & 0xFF) / 255.0F;
            g = (dye >> 8 & 0xFF) / 255.0F;
            b = (dye & 0xFF) / 255.0F;
        } else {
            r = 0.7F + Mth.sin((ticks + seal.pos().pos().getX()) / 4.0F) * 0.1F;
            g = 0.7F + Mth.sin((ticks + seal.pos().pos().getY()) / 5.0F) * 0.1F;
            b = 0.7F + Mth.sin((ticks + seal.pos().pos().getZ()) / 6.0F) * 0.1F;
        }
        Direction face = seal.pos().face();
        pose.pushPose();
        pose.translate(seal.pos().pos().getX() + 0.5, seal.pos().pos().getY() + 0.5, seal.pos().pos().getZ() + 0.5);
        turnTo(pose, face);
        pose.translate(0.0F, 0.0F, face.getStepZ() < 0 ? -0.51F : 0.51F);
        pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(ticks % 360.0F));
        centred(buffers.getBuffer(LegacyRenderTypes.seal(RING, through)), pose, 0.45F, colour(r, g, b, alpha * 0.8F));
        pose.popPose();
        if (!seal.seal().usesArea()) {
            return;
        }
        AABB area = seal.bounds();
        double[][] corners = {{area.minX, area.minY, area.minZ}, {area.minX, area.maxY - 1, area.minZ},
                {area.maxX - 1, area.minY, area.minZ}, {area.maxX - 1, area.maxY - 1, area.minZ},
                {area.maxX - 1, area.minY, area.maxZ - 1}, {area.maxX - 1, area.maxY - 1, area.maxZ - 1},
                {area.minX, area.minY, area.maxZ - 1}, {area.minX, area.maxY - 1, area.maxZ - 1}};
        VertexConsumer consumer = buffers.getBuffer(LegacyRenderTypes.seal(CORNER, through));
        int colour = colour(r, g, b, alpha * 0.7F);
        for (int corner = 0; corner < corners.length; corner++) {
            pose.pushPose();
            pose.translate(corners[corner][0] + 0.5, corners[corner][1] + 0.5, corners[corner][2] + 0.5);
            for (int which = 0; which < 3; which++) {
                Direction side = CORNER_FACES[corner][which];
                pose.pushPose();
                turnTo(pose, side);
                pose.translate(0.0F, 0.0F, side.getStepZ() < 0 ? -0.49F : 0.49F);
                pose.mulPose(com.mojang.math.Axis.ZN.rotationDegrees(90.0F));
                pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(CORNER_TURNS[corner][which]));
                centred(consumer, pose, 0.5F, colour);
                pose.popPose();
            }
            pose.popPose();
        }
    }

    /** Turns a flat picture facing south round to face this way, as the original turned it. */
    private static void turnTo(PoseStack pose, Direction face) {
        float x = -face.getStepY();
        float y = face.getStepX();
        float z = -face.getStepZ();
        if (x != 0.0F || y != 0.0F || z != 0.0F) {
            pose.mulPose(new Quaternionf().rotationAxis(Mth.HALF_PI, x, y, z));
        }
    }

    private static int colour(float r, float g, float b, float a) {
        return (int) (Mth.clamp(a, 0.0F, 1.0F) * 255) << 24 | (int) (Mth.clamp(r, 0.0F, 1.0F) * 255) << 16
                | (int) (Mth.clamp(g, 0.0F, 1.0F) * 255) << 8 | (int) (Mth.clamp(b, 0.0F, 1.0F) * 255);
    }

    /** A square about the middle, with its picture laid on turned a quarter, as the original's centred quads were. */
    private static void centred(VertexConsumer consumer, PoseStack pose, float half, int colour) {
        Matrix4f matrix = pose.last().pose();
        PoseStack.Pose at = pose.last();
        float[][] corners = {{-half, half, 1.0F, 1.0F}, {half, half, 1.0F, 0.0F}, {half, -half, 0.0F, 0.0F},
                {-half, -half, 0.0F, 1.0F}};
        for (float[] corner : corners) {
            consumer.addVertex(matrix, corner[0], corner[1], 0.0F).setColor(colour).setUv(corner[2], corner[3])
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(GLOW).setNormal(at, 0.0F, 0.0F, 1.0F);
        }
    }

    /** A square from {@code low} to {@code high} each way, at no depth, lit full. */
    private static void quad(VertexConsumer consumer, PoseStack pose, float low, float high, float u0, float u1,
            float v0, float v1, int colour) {
        Matrix4f matrix = pose.last().pose();
        PoseStack.Pose at = pose.last();
        consumer.addVertex(matrix, low, low, 0.0F).setColor(colour).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(GLOW).setNormal(at, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(matrix, high, low, 0.0F).setColor(colour).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(GLOW).setNormal(at, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(matrix, high, high, 0.0F).setColor(colour).setUv(u1, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(GLOW).setNormal(at, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(matrix, low, high, 0.0F).setColor(colour).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(GLOW).setNormal(at, 0.0F, 0.0F, 1.0F);
    }
}
