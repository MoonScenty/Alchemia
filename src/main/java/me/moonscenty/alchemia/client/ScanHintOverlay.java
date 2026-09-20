package me.moonscenty.alchemia.client;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.item.AlchemometerItem;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.research.ScanTarget;
import me.moonscenty.alchemia.research.Scanning;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * While an alchemometer is in hand, says whether whatever is in front of the player still has anything to teach, and
 * how far along a reading is.
 */
@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public class ScanHintOverlay {
    private static final int BAR_WIDTH = 60;
    private static final int BAR_HEIGHT = 3;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        if (player == null || client.options.hideGui || !holdingScanner(player)) {
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        int centreX = graphics.guiWidth() / 2;
        int centreY = graphics.guiHeight() / 2;

        if (player.isUsingItem() && player.getUseItem().is(ModItems.ALCHEMOMETER.get())) {
            drawProgress(graphics, player, centreX, centreY);
            return;
        }

        ScanTarget target = Scanning.lookingAt(player).orElse(null);
        if (target == null) {
            return;
        }

        boolean worthwhile = Scanning.hasAnythingToTeach(player, target);
        Component hint = worthwhile
                ? Component.translatable("scan.alchemia.hint.unread", target.displayName())
                : Component.translatable("scan.alchemia.hint.read", target.displayName());
        Component line = hint.copy().withStyle(worthwhile ? ChatFormatting.AQUA : ChatFormatting.DARK_GRAY);
        graphics.drawCenteredString(client.font, line, centreX, centreY + 14, 0xFFFFFF);
    }

    private static void drawProgress(GuiGraphics graphics, Player player, int centreX, int centreY) {
        int used = player.getUseItem().getUseDuration(player) - player.getUseItemRemainingTicks();
        float progress = Math.min(1F, used / (float) AlchemometerItem.SCAN_TICKS);

        int left = centreX - BAR_WIDTH / 2;
        int top = centreY + 14;
        graphics.fill(left, top, left + BAR_WIDTH, top + BAR_HEIGHT, 0x80000000);
        graphics.fill(left, top, left + (int) (BAR_WIDTH * progress), top + BAR_HEIGHT, 0xFF54C8E8);
    }

    private static boolean holdingScanner(Player player) {
        return player.getMainHandItem().is(ModItems.ALCHEMOMETER.get())
                || player.getOffhandItem().is(ModItems.ALCHEMOMETER.get());
    }
}
