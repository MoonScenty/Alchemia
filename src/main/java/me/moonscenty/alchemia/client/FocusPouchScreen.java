package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.systems.RenderSystem;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.menu.FocusPouchMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * The pouch opened, laid out as the original laid it: its own panel, with the eighteen squares for foci above the
 * player's bag, and a mark over the square of the belt the pouch itself is in, which cannot be moved while it is open.
 * <p>
 * The panel is already labelled in its own painting, so no words are put over it.
 */
public class FocusPouchScreen extends AbstractContainerScreen<FocusPouchMenu> {
    private static final ResourceLocation PANEL = Alchemia.id("textures/gui/focus_pouch.png");
    private static final int SHEET = 256;
    private static final int PANEL_W = 175;
    private static final int PANEL_H = 232;
    /** Where the mark over the pouch's own square is on the sheet, and where the belt is on the panel. */
    private static final int MARK_U = 240;
    private static final int MARK = 16;
    private static final int BELT_X = 8;
    private static final int BELT_Y = 209;
    private static final int PITCH = 18;

    public FocusPouchScreen(FocusPouchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = PANEL_W;
        imageHeight = PANEL_H;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partial, int mouseX, int mouseY) {
        // the original's panel has soft, half-clear edges, which come out solid without blending
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(PANEL, leftPos, topPos, 0, 0, PANEL_W, PANEL_H, SHEET, SHEET);
    }

    /** No words; only the mark over the pouch's own square, drawn over what is in it. */
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int locked = menu.locked();
        if (locked < 0 || locked >= 9) {
            return;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 200);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(PANEL, BELT_X + locked * PITCH, BELT_Y, MARK_U, 0, MARK, MARK, SHEET, SHEET);
        graphics.pose().popPose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        super.render(graphics, mouseX, mouseY, partial);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
