package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.systems.RenderSystem;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.entity.ArcaneBoreBlockEntity;
import me.moonscenty.alchemia.menu.ArcaneBoreMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * The bore's panel, the original's: the focus and the pickaxe, a mark over a pickaxe worn to its last point, and
 * beside them, in small print, how wide it digs, how much faster, and what else it does to what it mines.
 */
public class ArcaneBoreScreen extends AbstractContainerScreen<ArcaneBoreMenu> {
    private static final ResourceLocation PANEL = Alchemia.id("textures/gui/arcane_bore.png");
    private static final int SHEET = 256;

    public ArcaneBoreScreen(ArcaneBoreMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 141;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partial, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(PANEL, leftPos, topPos, 0, 0, imageWidth, imageHeight, SHEET, SHEET);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        ArcaneBoreBlockEntity bore = menu.bore();
        if (bore == null) {
            return;
        }
        ItemStack pick = bore.getItem(ArcaneBoreBlockEntity.SLOT_PICK);
        if (!pick.isEmpty() && pick.getDamageValue() + 1 >= pick.getMaxDamage()) {
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 300);
            graphics.blit(PANEL, 74, 18, 184, 0, 16, 16, SHEET, SHEET);
            graphics.pose().popPose();
        }
        graphics.pose().pushPose();
        graphics.pose().translate(112, 8, 300);
        graphics.pose().scale(0.5F, 0.5F, 1.0F);
        graphics.drawString(font, Component.translatable("gui.alchemia.arcane_bore.width", bore.width()), 0, 0,
                0xFFFFFF);
        graphics.drawString(font, Component.translatable("gui.alchemia.arcane_bore.speed", bore.speedBonus()), 0, 10,
                0xFFFFFF);
        graphics.drawString(font, Component.translatable("gui.alchemia.arcane_bore.other"), 0, 24, 0xFFFFFF);
        int line = 0;
        if (bore.refining() > 0) {
            graphics.drawString(font, Component.translatable("gui.alchemia.arcane_bore.refining", bore.refining()),
                    4, 34 + 9 * line++, 0xC0C0C0);
        }
        if (bore.fortune() > 0) {
            graphics.drawString(font, Component.translatable("gui.alchemia.arcane_bore.fortune", bore.fortune()),
                    4, 34 + 9 * line++, 0xEEC54A);
        }
        if (bore.silk()) {
            graphics.drawString(font, Component.translatable("gui.alchemia.arcane_bore.silk"), 4, 34 + 9 * line,
                    0x8080FF);
        }
        graphics.pose().popPose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        super.render(graphics, mouseX, mouseY, partial);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
