package me.moonscenty.alchemia.client;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.item.tool.ElementalShovelItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;

// original's architect overlay: the elemental shovel outlines where it would lay blocks, in place of the usual box
@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public final class ShovelPreview {
    private ShovelPreview() {
    }

    @SubscribeEvent
    public static void onHighlight(RenderHighlightEvent.Block event) {
        Minecraft client = Minecraft.getInstance();
        var player = client.player;
        if (player == null || client.level == null || player.isShiftKeyDown()
                || !(player.getMainHandItem().getItem() instanceof ElementalShovelItem)) {
            return;
        }
        BlockPos clicked = event.getTarget().getBlockPos();
        if (client.level.getBlockEntity(clicked) != null) {
            return;
        }
        var squares = ElementalShovelItem.square(player.getMainHandItem(), clicked, event.getTarget().getDirection(),
                player.getYRot()).stream().filter(at -> client.level.getBlockState(at).canBeReplaced()).toList();
        if (squares.isEmpty()) {
            return;
        }
        Vec3 camera = event.getCamera().getPosition();
        var lines = event.getMultiBufferSource().getBuffer(RenderType.lines());
        float pulse = 0.4F + 0.2F * (float) Math.sin(player.tickCount / 4.0);
        for (BlockPos at : squares) {
            LevelRenderer.renderLineBox(event.getPoseStack(), lines, new AABB(at).move(-camera.x, -camera.y, -camera.z),
                    1.0F, 1.0F, 1.0F, pulse);
        }
        event.setCanceled(true);
    }
}
