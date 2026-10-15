package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.entity.ArcaneEarBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Quaternionf;

/**
 * What goggles show of a note block or an arcane ear looked at: the note it is tuned to, written in the air a block
 * and a half above it and turned to face whoever is looking, as the original's {@code drawTextInAir} wrote it. It
 * is the raw note, nought to twenty-four, and, as in the original, simply "Note: " and the number.
 */
@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public final class NoteSight {
    private static final float SCALE = 0.02F;
    private static final int SHADOW = 0x111111;

    private NoteSight() {
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        HitResult hit = minecraft.hitResult;
        if (player == null || !(hit instanceof BlockHitResult aimed) || hit.getType() != HitResult.Type.BLOCK
                || !NodeSight.wearsLenses(player)) {
            return;
        }
        BlockPos pos = aimed.getBlockPos();
        int note = ArcaneEarBlockEntity.noteAt(player.level().getBlockState(pos), player.level().getBlockEntity(pos));
        if (note < 0) {
            return;
        }
        Vec3 camera = event.getCamera().getPosition();
        PoseStack pose = new PoseStack();
        pose.translate(pos.getX() + 0.5 - camera.x, pos.getY() + 1.5 - camera.y, pos.getZ() + 0.5 - camera.z);
        // turned about the upright only, as a name is
        pose.mulPose(new Quaternionf().rotationY((float) Math.PI - event.getCamera().getYRot() * ((float) Math.PI / 180F)));
        pose.scale(SCALE, -SCALE, SCALE);
        Font font = minecraft.font;
        String text = "Note: " + note;
        float x = -font.width(text) / 2.0F;
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        font.drawInBatch(text, x + 1, 1, SHADOW, false, pose.last().pose(), buffers, Font.DisplayMode.NORMAL, 0,
                LightTexture.FULL_BRIGHT);
        pose.translate(0.0F, 0.0F, -0.1F);
        font.drawInBatch(text, x, 0, 0xFFFFFF, false, pose.last().pose(), buffers, Font.DisplayMode.NORMAL, 0,
                LightTexture.FULL_BRIGHT);
        buffers.endBatch();
    }
}
