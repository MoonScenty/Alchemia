package me.moonscenty.alchemia.client;

import java.util.List;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.wand.ArchitectArea;
import me.moonscenty.alchemia.wand.spell.Swapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;

@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public final class TradePreview {
    private static BlockPos lastAt;
    private static Direction lastFace;
    private static ArchitectArea lastArea;
    private static int lastBeat = -1;
    private static List<BlockPos> blocks = List.of();

    private TradePreview() {
    }

    @SubscribeEvent
    public static void onHighlight(RenderHighlightEvent.Block event) {
        Minecraft client = Minecraft.getInstance();
        var player = client.player;
        if (player == null || client.level == null) {
            return;
        }
        ItemStack wand = player.getMainHandItem();
        if (!(wand.getItem() instanceof WandItem) || !Swapping.architect(wand)) {
            return;
        }
        BlockPos at = event.getTarget().getBlockPos();
        if (client.level.getBlockEntity(at) != null) {
            return;
        }
        Direction face = event.getTarget().getDirection();
        ArchitectArea area = Swapping.chosen(wand);
        int beat = player.tickCount / 5;
        if (!at.equals(lastAt) || face != lastFace || !area.equals(lastArea) || beat != lastBeat) {
            lastAt = at;
            lastFace = face;
            lastArea = area;
            lastBeat = beat;
            blocks = Swapping.area(client.level, player, wand, at, face);
        }
        if (blocks.isEmpty()) {
            return;
        }
        Vec3 camera = event.getCamera().getPosition();
        var lines = event.getMultiBufferSource().getBuffer(RenderType.lines());
        float pulse = 0.4F + 0.2F * (float) Math.sin(player.tickCount / 4.0);
        for (BlockPos block : blocks) {
            LevelRenderer.renderLineBox(event.getPoseStack(), lines,
                    new AABB(block).inflate(0.002).move(-camera.x, -camera.y, -camera.z), 1.0F, 1.0F, 1.0F, pulse);
        }
        event.setCanceled(true);
    }
}
