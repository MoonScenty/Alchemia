package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.systems.RenderSystem;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.menu.HandMirrorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** The hand mirror's panel, the original's: one square, and a mark over the belt square the mirror is in. */
public class HandMirrorScreen extends AbstractContainerScreen<HandMirrorMenu> {
    private static final ResourceLocation PANEL = Alchemia.id("textures/gui/hand_mirror.png");
    private static final int SHEET = 256;

    public HandMirrorScreen(HandMirrorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partial, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(PANEL, leftPos, topPos, 0, 0, imageWidth, imageHeight, SHEET, SHEET);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 300);
        RenderSystem.enableBlend();
        graphics.blit(PANEL, 8 + menu.heldIn() * 18, 142, 240, 0, 16, 16, SHEET, SHEET);
        graphics.pose().popPose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        super.render(graphics, mouseX, mouseY, partial);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
