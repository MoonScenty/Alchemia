package me.moonscenty.alchemia.client;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.menu.FocusPouchMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * The pouch opened: eighteen squares in the middle of a panel built from vanilla's three-row chest.
 * <p>
 * Nothing is drawn on it beyond the panel. A pouch has no state worth showing -- what is in it is what the squares
 * already show -- and a screen that looks like a chest is a screen nobody has to be taught.
 */
public class FocusPouchScreen extends AbstractContainerScreen<FocusPouchMenu> {
    private static final ResourceLocation PANEL = Alchemia.id("textures/gui/focus_pouch.png");
    private static final int SHEET = 256;
    private static final int PANEL_W = 176;
    private static final int PANEL_H = 166;

    public FocusPouchScreen(FocusPouchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = PANEL_W;
        imageHeight = PANEL_H;
        // the panel is a three-row chest, so the bag's own label sits where a chest's does
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partial, int mouseX, int mouseY) {
        graphics.blit(PANEL, leftPos, topPos, 0, 0, PANEL_W, PANEL_H, SHEET, SHEET);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        super.render(graphics, mouseX, mouseY, partial);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
