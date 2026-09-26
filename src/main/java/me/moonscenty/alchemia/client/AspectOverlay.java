package me.moonscenty.alchemia.client;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.AlchemiaConfig;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aspect.Aspects;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

/**
 * Draws the aspects of whatever the cursor is resting on, in a row above the slot.
 * <p>
 * Hidden until sneak is held, unless the player has asked for the opposite in the config. No goggles needed — this is
 * just reading a label, not seeing the unseen.
 */
@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public class AspectOverlay {
    private static final ResourceLocation BACKGROUND = Alchemia.id("textures/aspect/background.png");
    private static final ResourceLocation UNKNOWN = Alchemia.id("textures/aspect/unknown.png");
    private static final int ICON = 16;
    private static final int SPACING = 18;
    /** How far above the slot the row sits. */
    private static final int RISE = 26;

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen) || !shouldShow()) {
            return;
        }

        Slot slot = screen.getSlotUnderMouse();
        if (slot == null || !slot.hasItem()) {
            return;
        }

        AspectList aspects = Aspects.of(slot.getItem());
        if (!aspects.isEmpty()) {
            draw(event.getGuiGraphics(), screen, slot, aspects, PlayerKnowledge.of(screen.getMinecraft().player));
        }
    }

    /** Sneak reveals the aspects, or hides them when the player has turned the config around. */
    private static boolean shouldShow() {
        return Screen.hasShiftDown() != AlchemiaConfig.ALWAYS_SHOW_ASPECTS.get();
    }

    private static void draw(GuiGraphics graphics, AbstractContainerScreen<?> screen, Slot slot, AspectList aspects,
            PlayerKnowledge knowledge) {
        List<Holder<Aspect>> order = aspects.sortedByAmount();
        int width = order.size() * SPACING;
        // keep the row on screen when the slot sits near an edge
        int left = Math.clamp(screen.getGuiLeft() + slot.x + 8 - width / 2, 2, screen.width - width - 2);
        int top = screen.getGuiTop() + slot.y - RISE;

        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 400);
        RenderSystem.enableBlend();

        for (int index = 0; index < order.size(); index++) {
            Holder<Aspect> holder = order.get(index);
            int x = left + index * SPACING;
            if (knowledge.knows(holder)) {
                drawIcon(graphics, holder.value(), x, top);
                drawAmount(graphics, screen.getMinecraft().font, aspects.get(holder), x, top);
            } else {
                // the aspect is there, but the player has no idea what it is yet
                drawUnknown(graphics, x, top);
            }
        }

        RenderSystem.disableBlend();
        graphics.pose().popPose();
    }

    /**
     * Blending is turned on again for every icon, not once for the list: the count drawn beside the last one ends
     * the font's batch, and that puts blending back off. Without this the first icon fades at its edge and the rest
     * come out as hard discs.
     */
    private static void drawIcon(GuiGraphics graphics, Aspect aspect, int x, int y) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.setColor(0.1F, 0.1F, 0.1F, 0.65F);
        graphics.blit(BACKGROUND, x - 1, y - 1, 0, 0, ICON + 2, ICON + 2, ICON + 2, ICON + 2);

        int color = aspect.color();
        graphics.setColor(((color >> 16) & 0xFF) / 255F, ((color >> 8) & 0xFF) / 255F, (color & 0xFF) / 255F, 1F);
        graphics.blit(aspect.icon(), x, y, 0, 0, ICON, ICON, ICON, ICON);
        graphics.setColor(1F, 1F, 1F, 1F);
    }

    private static void drawUnknown(GuiGraphics graphics, int x, int y) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.setColor(0.1F, 0.1F, 0.1F, 0.65F);
        graphics.blit(BACKGROUND, x - 1, y - 1, 0, 0, ICON + 2, ICON + 2, ICON + 2, ICON + 2);
        graphics.setColor(0.55F, 0.55F, 0.6F, 1F);
        graphics.blit(UNKNOWN, x, y, 0, 0, ICON, ICON, ICON, ICON);
        graphics.setColor(1F, 1F, 1F, 1F);
    }

    private static void drawAmount(GuiGraphics graphics, Font font, int amount, int x, int y) {
        String text = String.valueOf(amount);
        graphics.pose().pushPose();
        // the count rides small in the bottom right of the icon
        graphics.pose().translate(x + ICON - font.width(text) * 0.5F, y + ICON - font.lineHeight * 0.5F, 0);
        graphics.pose().scale(0.5F, 0.5F, 1F);
        graphics.drawString(font, text, 0, 0, 0xFFFFFF, true);
        graphics.pose().popPose();
    }
}
