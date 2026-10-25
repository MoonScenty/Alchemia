package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.systems.RenderSystem;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.entity.AdvancedAutomatedCrossbow;
import me.moonscenty.alchemia.entity.Autocaster;
import me.moonscenty.alchemia.entity.AutomatedCrossbow;
import me.moonscenty.alchemia.menu.CrossbowMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * An automated crossbow opened: the square its arrows go in and how battered it is, and on the advanced one the four
 * things it may be told to mind. The original's two panels, laid out as it laid them, which share the autocaster's
 * strip of hearts and toggles.
 */
public class CrossbowScreen extends AbstractContainerScreen<CrossbowMenu> {
    private static final ResourceLocation BASIC = Alchemia.id("textures/gui/automated_crossbow.png");
    private static final ResourceLocation ADVANCED = Alchemia.id("textures/gui/advanced_automated_crossbow.png");
    private static final int SHEET = 256;
    private static final int PANEL_W = 175;
    /** What the panel actually covers. The original declared it taller and painted nothing down there. */
    private static final int PANEL_H = 166;

    private static final int HEARTS_U = 192;
    private static final int HEARTS_V = 48;
    private static final int HEARTS_W = 39;
    private static final int HEARTS_H = 6;
    private static final int HEARTS_X = 68;
    private static final int HEARTS_X_ADVANCED = 30;
    private static final int HEARTS_Y = 59;

    private static final int BUTTON = 8;
    private static final int FRAME_V = 16;
    private static final int TICK_V = 24;
    private static final int BUTTONS_X = 90;
    private static final int BUTTONS_Y = 13;
    private static final int BUTTONS_PITCH = 14;
    private static final int LABEL_GAP = 12;

    public CrossbowScreen(CrossbowMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = PANEL_W;
        imageHeight = PANEL_H;
    }

    private boolean advanced() {
        return menu.turret() instanceof AdvancedAutomatedCrossbow;
    }

    private ResourceLocation panel() {
        return advanced() ? ADVANCED : BASIC;
    }

    @Override
    protected void init() {
        super.init();
        if (advanced()) {
            for (int which = Autocaster.ANIMALS; which <= Autocaster.FRIENDS; which++) {
                addRenderableWidget(new Toggle(which, leftPos + BUTTONS_X,
                        topPos + BUTTONS_Y + which * BUTTONS_PITCH));
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partial, int mouseX, int mouseY) {
        AutomatedCrossbow turret = menu.turret();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(panel(), leftPos, topPos, 0, 0, PANEL_W, PANEL_H, SHEET, SHEET);
        if (turret == null) {
            return;
        }
        int left = (int) (HEARTS_W * (turret.getHealth() / turret.getMaxHealth()));
        graphics.blit(panel(), leftPos + (advanced() ? HEARTS_X_ADVANCED : HEARTS_X), topPos + HEARTS_Y,
                HEARTS_U, HEARTS_V, left, HEARTS_H, SHEET, SHEET);
    }

    /** The original wrote nothing on these panels, not even their names. */
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        super.render(graphics, mouseX, mouseY, partial);
        renderTooltip(graphics, mouseX, mouseY);
    }

    /** One of the four, read straight off the turret, the autocaster's toggle on this panel. */
    private class Toggle extends AbstractButton {
        private final int which;

        Toggle(int which, int x, int y) {
            super(x, y, BUTTON, BUTTON, Component.translatable("gui.alchemia.autocaster.minds." + which));
            this.which = which;
        }

        @Override
        public void onPress() {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, which);
            }
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            graphics.blit(panel(), getX(), getY(), HEARTS_U, FRAME_V, BUTTON, BUTTON, SHEET, SHEET);
            if (menu.turret() instanceof AdvancedAutomatedCrossbow turret && turret.minds(which)) {
                graphics.blit(panel(), getX(), getY(), HEARTS_U, TICK_V, BUTTON, BUTTON, SHEET, SHEET);
            }
            graphics.drawString(font, getMessage(), getX() + LABEL_GAP, getY(), 0xFFFFFF);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }
}
