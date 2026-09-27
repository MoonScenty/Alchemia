package me.moonscenty.alchemia.client;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.block.entity.EssentiaSmelterBlockEntity;
import me.moonscenty.alchemia.menu.EssentiaSmelterMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * The smelter as the worker sees it: what goes in, what burns it, and two gauges.
 * <p>
 * Nothing comes out here, so there is no result square and no arrow to one. The left gauge is what has been boiled
 * off and is waiting to rise; the right is how far the thing in the top slot has got.
 */
public class EssentiaSmelterScreen extends AbstractContainerScreen<EssentiaSmelterMenu> {
    private static final ResourceLocation PANEL = Alchemia.id("textures/gui/essentia_smelter.png");
    private static final int SHEET = 256;
    private static final int PANEL_W = 176;
    private static final int PANEL_H = 166;

    // The two gauges, and the fire between the slots. All three fill from the bottom.
    private static final int GAUGE_W = 12;
    private static final int GAUGE_H = 40;
    private static final int HELD_X = 10;
    private static final int COOKED_X = 154;
    private static final int GAUGE_Y = 23;
    private static final int HELD_FROM_X = 204;
    private static final int COOKED_FROM_X = 218;

    private static final int FIRE = 14;
    private static final int FIRE_X = 81;
    private static final int FIRE_Y = 37;
    private static final int FIRE_FROM_X = 185;

    private static final int ICON = 16;

    public EssentiaSmelterScreen(EssentiaSmelterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = PANEL_W;
        imageHeight = PANEL_H;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(PANEL, leftPos, topPos, 0, 0, PANEL_W, PANEL_H, SHEET, SHEET);

        // the fire burns down rather than up, as a fire does
        int alight = Math.round(menu.burning() * FIRE);
        if (alight > 0) {
            graphics.blit(PANEL, leftPos + FIRE_X, topPos + FIRE_Y + FIRE - alight,
                    FIRE_FROM_X, FIRE - alight, FIRE, alight, SHEET, SHEET);
        }
        gauge(graphics, HELD_X, HELD_FROM_X, menu.filled());
        gauge(graphics, COOKED_X, COOKED_FROM_X, menu.cooked());
    }

    /** One gauge, filled from the bottom by as much of its picture as the share calls for. */
    private void gauge(GuiGraphics graphics, int x, int fromX, float share) {
        int high = Math.round(Math.max(0.0F, Math.min(1.0F, share)) * GAUGE_H);
        if (high <= 0) {
            return;
        }
        graphics.blit(PANEL, leftPos + x, topPos + GAUGE_Y + GAUGE_H - high,
                fromX, GAUGE_H - high, GAUGE_W, high, SHEET, SHEET);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    /**
     * What is in the smelter, listed on the gauge that holds it.
     * <p>
     * The original showed a total and nothing else, so there was no way to tell what had gone in. That matters
     * here, because a vessel above takes one kind and no other: what is mixed in the smelter is what decides how
     * many vessels are needed.
     */
    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!over(mouseX, mouseY, HELD_X, GAUGE_Y, GAUGE_W, GAUGE_H) || minecraft == null) {
            super.renderTooltip(graphics, mouseX, mouseY);
            return;
        }

        AspectList dissolved = menu.dissolved(minecraft.level);
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("gui.alchemia.essentia_held",
                menu.held(), EssentiaSmelterBlockEntity.CAPACITY));
        for (Holder<Aspect> aspect : dissolved.sortedByAmount()) {
            lines.add(Component.literal(dissolved.get(aspect) + "  ")
                    .append(aspect.value().displayName())
                    .withStyle(ChatFormatting.GRAY));
        }
        if (dissolved.isEmpty()) {
            lines.add(Component.translatable("gui.alchemia.essentia_empty").withStyle(ChatFormatting.DARK_GRAY));
        }
        graphics.renderComponentTooltip(font, lines, mouseX, mouseY);
    }

    private boolean over(double mouseX, double mouseY, int x, int y, int wide, int tall) {
        return mouseX >= leftPos + x && mouseX < leftPos + x + wide
                && mouseY >= topPos + y && mouseY < topPos + y + tall;
    }
}
