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

    // Laid out as the original laid it out. Everything fills from the bottom: the fire under the input, what the
    // smelter holds in the tall gauge to its left under a pane of glass, and how far along the work is to its right.
    private static final int FIRE_X = 80;
    private static final int FIRE_Y = 26;
    private static final int FIRE_W = 16;
    private static final int FIRE_H = 20;
    private static final int FIRE_FROM_X = 176;

    private static final int HELD_X = 61;
    private static final int HELD_Y = 12;
    private static final int HELD_W = 8;
    private static final int HELD_H = 48;
    private static final int HELD_FROM_X = 200;
    private static final int GLASS_X = 60;
    private static final int GLASS_Y = 8;
    private static final int GLASS_W = 10;
    private static final int GLASS_H = 55;
    private static final int GLASS_FROM_X = 232;

    private static final int COOKED_X = 106;
    private static final int COOKED_Y = 13;
    private static final int COOKED_W = 9;
    private static final int COOKED_H = 46;
    private static final int COOKED_FROM_X = 216;

    public EssentiaSmelterScreen(EssentiaSmelterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = PANEL_W;
        imageHeight = PANEL_H;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // the original's panels have soft, half-clear edges, which come out solid without blending
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(PANEL, leftPos, topPos, 0, 0, PANEL_W, PANEL_H, SHEET, SHEET);
        gauge(graphics, FIRE_X, FIRE_Y, FIRE_W, FIRE_H, FIRE_FROM_X, menu.burning());
        gauge(graphics, HELD_X, HELD_Y, HELD_W, HELD_H, HELD_FROM_X, menu.filled());
        gauge(graphics, COOKED_X, COOKED_Y, COOKED_W, COOKED_H, COOKED_FROM_X, menu.cooked());
        // the glass is a faint sheen, mostly see-through; drawn without blending its faint pixels come out solid and
        // the pane reads as white streaks over an empty gauge
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(PANEL, leftPos + GLASS_X, topPos + GLASS_Y, GLASS_FROM_X, 0, GLASS_W, GLASS_H, SHEET, SHEET);
        RenderSystem.disableBlend();
    }

    /** One gauge, filled from the bottom by as much of its picture as the share calls for. */
    private void gauge(GuiGraphics graphics, int x, int y, int wide, int tall, int fromX, float share) {
        int high = Math.round(Math.max(0.0F, Math.min(1.0F, share)) * tall);
        if (high <= 0) {
            return;
        }
        graphics.blit(PANEL, leftPos + x, topPos + y + tall - high, fromX, tall - high, wide, high, SHEET, SHEET);
    }

    /** The panel is a picture of the smelter, as the original's was; it carries no words. */
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
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
        if (!over(mouseX, mouseY, GLASS_X, GLASS_Y, GLASS_W, GLASS_H) || minecraft == null) {
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
