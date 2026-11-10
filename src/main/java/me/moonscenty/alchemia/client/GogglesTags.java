package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.essentia.EssentiaHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

// original's goggles: what a container holds floats over it, and a note block shows its note
@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public final class GogglesTags {
    private static final int ROW = 5;
    private static float scale;
    private static BlockPos lastLooked;

    private GogglesTags() {
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null || !NodeSight.wearsLenses(client.player)
                || !(client.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
            scale = 0.0F;
            return;
        }
        BlockPos at = hit.getBlockPos();
        if (!at.equals(lastLooked)) {
            scale = 0.0F;
            lastLooked = at;
        }
        boolean roomAbove = client.level.isEmptyBlock(at.above());
        Vec3 camera = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffers = client.renderBuffers().bufferSource();
        PoseStack pose = event.getPoseStack();
        BlockState state = client.level.getBlockState(at);
        if (state.getBlock() instanceof NoteBlock) {
            text(pose, buffers, client.font, camera, at.getX(), at.getY() + 1, at.getZ(),
                    "Note: " + state.getValue(NoteBlock.NOTE));
        }
        if (client.level.getBlockEntity(at) instanceof EssentiaHolder holder && !holder.held().isEmpty()) {
            if (scale < 0.3F) {
                scale += 0.031F - scale / 10.0F;
            }
            float lift = client.level.getBlockEntity(at)
                    instanceof me.moonscenty.alchemia.block.entity.RechargePedestalBlockEntity ? 0.6F : 0.0F;
            tags(pose, buffers, client.font, camera, at.getX(), at.getY() + (roomAbove ? 0.4F : 0.0F) + lift, at.getZ(),
                    holder.held(), roomAbove ? Direction.UP : hit.getDirection());
        }
        buffers.endBatch();
    }

    private static void tags(PoseStack pose, MultiBufferSource buffers, Font font, Vec3 camera, double x, double y,
            double z, AspectList tags, Direction side) {
        int current = 0;
        float down = 0.0F;
        int left = tags.size();
        for (Holder<Aspect> tag : tags.sortedByName()) {
            int across = Math.min(left, ROW);
            if (current >= ROW) {
                current = 0;
                down -= scale * 1.05F;
                left -= ROW;
                if (left < ROW) {
                    across = left % ROW;
                }
            }
            float shift = (current - across / 2.0F + 0.5F) * scale * 4.0F * scale;
            pose.pushPose();
            pose.translate(x + 0.5 + scale * 2.0F * side.getStepX() - camera.x,
                    y - down + 0.5 + scale * 2.0F * side.getStepY() - camera.y,
                    z + 0.5 + scale * 2.0F * side.getStepZ() - camera.z);
            float yaw = (float) Math.toDegrees(Math.atan2(camera.x - (x + 0.5), camera.z - (z + 0.5)));
            pose.mulPose(Axis.YP.rotationDegrees(yaw));
            pose.translate(shift, 0.0F, 0.0F);
            pose.scale(scale, scale, scale);
            int colour = tag.value().color();
            VertexConsumer quad = buffers.getBuffer(RenderType.textSeeThrough(tag.value().icon()));
            var matrix = pose.last().pose();
            int red = (colour >> 16) & 0xFF;
            int green = (colour >> 8) & 0xFF;
            int blue = colour & 0xFF;
            int light = LightTexture.FULL_BRIGHT;
            quad.addVertex(matrix, -0.5F, -0.5F, 0.0F).setColor(red, green, blue, 191).setUv(1, 1).setLight(light);
            quad.addVertex(matrix, 0.5F, -0.5F, 0.0F).setColor(red, green, blue, 191).setUv(0, 1).setLight(light);
            quad.addVertex(matrix, 0.5F, 0.5F, 0.0F).setColor(red, green, blue, 191).setUv(0, 0).setLight(light);
            quad.addVertex(matrix, -0.5F, 0.5F, 0.0F).setColor(red, green, blue, 191).setUv(1, 0).setLight(light);
            String amount = String.valueOf(tags.get(tag));
            pose.scale(-0.04F, -0.04F, 0.04F);
            pose.translate(0.0F, -6.0F, -0.1F);
            int wide = font.width(amount);
            font.drawInBatch(amount, 14 - wide, 1, 0x111111, false, pose.last().pose(), buffers,
                    Font.DisplayMode.SEE_THROUGH, 0, light);
            pose.translate(0.0F, 0.0F, -0.1F);
            font.drawInBatch(amount, 13 - wide, 0, 0xFFFFFF, false, pose.last().pose(), buffers,
                    Font.DisplayMode.SEE_THROUGH, 0, light);
            pose.popPose();
            current++;
        }
    }

    private static void text(PoseStack pose, MultiBufferSource buffers, Font font, Vec3 camera, double x, double y,
            double z, String said) {
        pose.pushPose();
        pose.translate(x + 0.5 - camera.x, y + 0.5 - camera.y, z + 0.5 - camera.z);
        float yaw = (float) Math.toDegrees(Math.atan2(camera.x - (x + 0.5), camera.z - (z + 0.5)));
        pose.mulPose(Axis.YP.rotationDegrees(yaw));
        pose.scale(-0.02F, -0.02F, 0.02F);
        int wide = font.width(said);
        font.drawInBatch(said, 1 - wide / 2F, 1, 0x111111, false, pose.last().pose(), buffers,
                Font.DisplayMode.SEE_THROUGH, 0, LightTexture.FULL_BRIGHT);
        pose.translate(0.0F, 0.0F, -0.1F);
        font.drawInBatch(said, -wide / 2F, 0, 0xFFFFFF, false, pose.last().pose(), buffers,
                Font.DisplayMode.SEE_THROUGH, 0, LightTexture.FULL_BRIGHT);
        pose.popPose();
    }
}
