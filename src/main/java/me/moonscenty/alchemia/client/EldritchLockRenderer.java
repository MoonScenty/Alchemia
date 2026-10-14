package me.moonscenty.alchemia.client;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.eldritch.EldritchLockBlockEntity;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
import me.moonscenty.alchemia.client.legacy.model.LegacyModelBaker;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * The lock on a great hall's door, as the original drew it: a carved block turned to face the way out, four arms of
 * pulsing cubes reaching out over the door that draw back one by one while the lock turns, the tablet in its face
 * once there is one, and over the whole door the stars -- which is all the door is, to look at.
 */
public class EldritchLockRenderer implements BlockEntityRenderer<EldritchLockBlockEntity> {
    private static final ResourceLocation LOCK = Alchemia.id("textures/entity/eldritch_lock.png");
    private static final ResourceLocation ARM = Alchemia.id("textures/entity/eldritch_cube.png");
    /** Close enough for the door's stars to be drawn. */
    private static final double STARS_IN_RANGE = 512.0;

    private static int bakedFrom = -1;
    private static ModelPart cube;
    private ItemStack tablet;

    public EldritchLockRenderer(BlockEntityRendererProvider.Context context) {
    }

    private static boolean refresh() {
        if (bakedFrom != LegacyModels.generation()) {
            bakedFrom = LegacyModels.generation();
            cube = LegacyModels.get(LegacyAssets.CUBE).map(model -> LegacyModelBaker.field(model, "cube"))
                    .orElse(null);
        }
        return cube != null;
    }

    @Override
    public void render(EldritchLockBlockEntity lock, float partial, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        Direction facing = lock.facing();
        Minecraft minecraft = Minecraft.getInstance();
        float time = (minecraft.player == null ? 0 : minecraft.player.tickCount) + partial;
        if (refresh()) {
            pose.pushPose();
            pose.translate(0.5F, 0.5F, 0.5F);
            pose.pushPose();
            pose.mulPose(Axis.ZP.rotationDegrees(180.0F));
            pose.mulPose(Axis.YP.rotationDegrees(180.0F + facing.get2DDataValue() * 90.0F));
            pose.scale(1.0F, 1.0F, 1.0F);
            pose.translate(-0.5F, -0.5F, -0.5F);
            cube.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(LOCK)), light, overlay);
            pose.popPose();

            VertexConsumer arms = buffers.getBuffer(RenderType.entityCutoutNoCull(ARM));
            for (int i = 0; i < 4; i++) {
                pose.pushPose();
                pose.mulPose(Axis.of(new org.joml.Vector3f(facing.getStepX(), facing.getStepY(), facing.getStepZ()))
                        .rotationDegrees(90.0F * i));
                int left = 5 - (lock.count() + i * 5) / 20;
                for (int j = 1; j < left; j++) {
                    pose.pushPose();
                    pose.translate(0.0F, 0.25F + 0.5F * j, 0.0F);
                    float s = Mth.sin((time + j * 10 + i * 20) / 20.0F) * 0.1F;
                    if (j == 1 || j == 4) {
                        s = s / 2.0F + 0.2F;
                    }
                    pose.scale(0.5F + s, 0.5F, 0.5F + s);
                    pose.translate(-0.5F, -0.5F, -0.5F);
                    cube.render(pose, arms, light, overlay);
                    pose.popPose();
                }
                pose.popPose();
            }
            pose.popPose();
        }

        if (lock.count() >= 0) {
            if (tablet == null) {
                tablet = new ItemStack(ModItems.RUNED_TABLET.get());
            }
            pose.pushPose();
            pose.translate(0.5F + facing.getStepX() * 0.525F, 0.5F, 0.5F + facing.getStepZ() * 0.525F);
            pose.mulPose(Axis.YP.rotationDegrees(switch (facing) {
                case NORTH -> 180.0F;
                case WEST -> 270.0F;
                case EAST -> 90.0F;
                default -> 0.0F;
            }));
            pose.scale(0.5F, 0.5F, 0.5F);
            minecraft.getItemRenderer().renderStatic(tablet, ItemDisplayContext.FIXED, light, overlay, pose, buffers,
                    lock.getLevel(), 0);
            pose.popPose();
        }

        if (minecraft.getCameraEntity() != null
                && minecraft.getCameraEntity().distanceToSqr(lock.getBlockPos().getCenter()) < STARS_IN_RANGE) {
            stars(pose, buffers.getBuffer(RenderType.endPortal()), facing);
        }
    }

    /** The door, five by five across the lock, its stars shown from both sides. */
    private static void stars(PoseStack pose, VertexConsumer buffer, Direction facing) {
        Matrix4f matrix = pose.last().pose();
        float low = -2.0F;
        float high = 3.0F;
        if (facing.getAxis() == Direction.Axis.Z) {
            for (float z : new float[] {-0.002F, 1.002F}) {
                quad(buffer, matrix, low, low, z, high, low, z, high, high, z, low, high, z);
                quad(buffer, matrix, low, high, z, high, high, z, high, low, z, low, low, z);
            }
        } else {
            for (float x : new float[] {-0.002F, 1.002F}) {
                quad(buffer, matrix, x, low, low, x, high, low, x, high, high, x, low, high);
                quad(buffer, matrix, x, low, high, x, high, high, x, high, low, x, low, low);
            }
        }
    }

    private static void quad(VertexConsumer buffer, Matrix4f matrix, float x1, float y1, float z1, float x2,
            float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4) {
        buffer.addVertex(matrix, x1, y1, z1);
        buffer.addVertex(matrix, x2, y2, z2);
        buffer.addVertex(matrix, x3, y3, z3);
        buffer.addVertex(matrix, x4, y4, z4);
    }

    @Override
    public int getViewDistance() {
        return 96;
    }

    @Override
    public AABB getRenderBoundingBox(EldritchLockBlockEntity lock) {
        return lock.getRenderBoundingBox();
    }
}
