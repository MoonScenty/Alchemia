package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.systems.RenderSystem;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.entity.Autocaster;
import me.moonscenty.alchemia.menu.AutocasterMenu;
import me.moonscenty.alchemia.wand.Focus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * An autocaster opened: the square its focus goes in, how battered it is, how far it reaches, and the four
 * things it may be told to mind.
 *
 * <p>Laid out as the original laid it. The panel is drawn on clear glass rather than on a plate, so it is blitted
 * with blending on; the health bar is the same strip of hearts drawn over the dark ones painted into the panel.
 */
public class AutocasterScreen extends AbstractContainerScreen<AutocasterMenu> {
    private static final ResourceLocation PANEL = Alchemia.id("textures/gui/autocaster.png");
    private static final int SHEET = 256;
    private static final int PANEL_W = 175;
    /** What the panel actually covers. The original declared it taller and painted nothing down there. */
    private static final int PANEL_H = 166;

    /** The strip of red hearts on the sheet, and where it is laid over the dark ones. */
    private static final int HEARTS_U = 192;
    private static final int HEARTS_V = 48;
    private static final int HEARTS_W = 39;
    private static final int HEARTS_H = 6;
    private static final int HEARTS_X = 24;
    private static final int HEARTS_Y = 59;

    /** The toggles: a frame, a tick to lay in it when it is on, and where the four of them stand. */
    private static final int BUTTON = 8;
    private static final int FRAME_V = 16;
    private static final int TICK_V = 24;
    private static final int BUTTONS_X = 90;
    private static final int BUTTONS_Y = 13;
    private static final int BUTTONS_PITCH = 14;
    private static final int LABEL_GAP = 12;

    /** Where the reach is written, and how small. */
    private static final int REACH_X = 44;
    private static final int REACH_Y = 9;
    private static final float HALF = 0.5F;

    public AutocasterScreen(AutocasterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = PANEL_W;
        imageHeight = PANEL_H;
    }

    @Override
    protected void init() {
        super.init();
        for (int which = Autocaster.ANIMALS; which <= Autocaster.FRIENDS; which++) {
            addRenderableWidget(new Toggle(which,
                    leftPos + BUTTONS_X, topPos + BUTTONS_Y + which * BUTTONS_PITCH));
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partial, int mouseX, int mouseY) {
        Autocaster turret = menu.turret();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(PANEL, leftPos, topPos, 0, 0, PANEL_W, PANEL_H, SHEET, SHEET);
        if (turret == null) {
            return;
        }
        int left = (int) (HEARTS_W * (turret.getHealth() / turret.getMaxHealth()));
        graphics.blit(PANEL, leftPos + HEARTS_X, topPos + HEARTS_Y, HEARTS_U, HEARTS_V, left, HEARTS_H, SHEET, SHEET);
    }

    /**
     * How far it reaches, which is the focus's own range and not the turret's.
     * <p>
     * Written at half size, as the original wrote it: it is a number to glance at, not a heading.
     */
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        Autocaster turret = menu.turret();
        Focus.Turret reach = turret == null || turret.fitted() == null
                ? null : turret.fitted().focus().turret();
        if (reach == null) {
            return;
        }
        Component written = Component.translatable("gui.alchemia.autocaster.range", (int) reach.range());
        graphics.pose().pushPose();
        graphics.pose().scale(HALF, HALF, 1.0F);
        graphics.drawCenteredString(font, written, REACH_X * 2, REACH_Y * 2, 0xFFFFFF);
        graphics.pose().popPose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        super.render(graphics, mouseX, mouseY, partial);
        renderTooltip(graphics, mouseX, mouseY);
    }

    /**
     * One of the four: a frame that fills in when it is on, with its name beside it.
     * <p>
     * It reads what it is set to straight off the turret rather than keeping its own copy, so somebody else
     * changing it is seen here the tick after.
     */
    private class Toggle extends AbstractButton {
        private final int which;

        Toggle(int which, int x, int y) {
            super(x, y, BUTTON, BUTTON,
                    Component.translatable("gui.alchemia.autocaster.minds." + which));
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
            Autocaster turret = menu.turret();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            graphics.blit(PANEL, getX(), getY(), HEARTS_U, FRAME_V, BUTTON, BUTTON, SHEET, SHEET);
            if (turret != null && turret.minds(which)) {
                graphics.blit(PANEL, getX(), getY(), HEARTS_U, TICK_V, BUTTON, BUTTON, SHEET, SHEET);
            }
            graphics.drawString(font, getMessage(), getX() + LABEL_GAP, getY(), 0xFFFFFF);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }
}
