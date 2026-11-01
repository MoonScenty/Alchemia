package me.moonscenty.alchemia.client;

import java.util.function.IntPredicate;
import java.util.function.Supplier;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

final class TurretToggle extends AbstractButton {
    static final int SHEET = 256;
    static final int PANEL_W = 175;
    static final int PANEL_H = 166;

    private static final int HEARTS_U = 192;
    private static final int HEARTS_V = 48;
    private static final int HEARTS_W = 39;
    private static final int HEARTS_H = 6;
    static final int HEARTS_Y = 59;

    private static final int BUTTON = 8;
    private static final int FRAME_V = 16;
    private static final int TICK_V = 24;
    static final int BUTTONS_X = 90;
    static final int BUTTONS_Y = 13;
    static final int BUTTONS_PITCH = 14;
    private static final int LABEL_GAP = 12;

    private final int which;
    private final int containerId;
    private final Supplier<ResourceLocation> panel;
    private final IntPredicate minds;

    TurretToggle(int which, int x, int y, int containerId, Supplier<ResourceLocation> panel, IntPredicate minds) {
        super(x, y, BUTTON, BUTTON, Component.translatable("gui.alchemia.autocaster.minds." + which));
        this.which = which;
        this.containerId = containerId;
        this.panel = panel;
        this.minds = minds;
    }

    static void drawPanel(GuiGraphics graphics, ResourceLocation panel, int x, int y) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(panel, x, y, 0, 0, PANEL_W, PANEL_H, SHEET, SHEET);
    }

    static void drawHearts(GuiGraphics graphics, ResourceLocation panel, int x, int y, LivingEntity turret) {
        int left = (int) (HEARTS_W * (turret.getHealth() / turret.getMaxHealth()));
        graphics.blit(panel, x, y, HEARTS_U, HEARTS_V, left, HEARTS_H, SHEET, SHEET);
    }

    @Override
    public void onPress() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(containerId, which);
        }
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(panel.get(), getX(), getY(), HEARTS_U, FRAME_V, BUTTON, BUTTON, SHEET, SHEET);
        if (minds.test(which)) {
            graphics.blit(panel.get(), getX(), getY(), HEARTS_U, TICK_V, BUTTON, BUTTON, SHEET, SHEET);
        }
        graphics.drawString(Minecraft.getInstance().font, getMessage(), getX() + LABEL_GAP, getY(), 0xFFFFFF);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
