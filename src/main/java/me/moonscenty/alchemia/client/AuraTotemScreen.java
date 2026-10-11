package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.systems.RenderSystem;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aura.ShardTuning;
import me.moonscenty.alchemia.menu.AuraTotemMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * The aura totem as the original drew it: a pocket for shards and a bar for how much of the burning one is left, in
 * the colour of what it moves.
 */
public class AuraTotemScreen extends AbstractContainerScreen<AuraTotemMenu> {
    private static final ResourceLocation PANEL = Alchemia.id("textures/gui/aura_totem.png");
    private static final int SHEET = 256;
    private static final int PANEL_W = 176;
    private static final int PANEL_H = 166;

    // Laid out as the original laid it out.
    private static final int BAR_X = 64;
    private static final int BAR_Y = 52;
    private static final int BAR_W = 48;
    private static final int BAR_H = 8;
    private static final int BAR_FROM_X = 176;
    private static final int TINTED_FROM_Y = 48;
    private static final int PLAIN_FROM_Y = 58;
    private static final int FRAME_X = 60;
    private static final int FRAME_Y = 51;
    private static final int FRAME_W = 56;
    private static final int FRAME_H = 10;
    private static final int FRAME_FROM_Y = 32;

    public AuraTotemScreen(AuraTotemMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = PANEL_W;
        imageHeight = PANEL_H;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(PANEL, leftPos, topPos, 0, 0, PANEL_W, PANEL_H, SHEET, SHEET);
        int wide = Math.round(Math.max(0.0F, Math.min(1.0F, menu.left())) * BAR_W);
        Holder<Aspect> aspect = ShardTuning.aspect(menu.tuning());
        if (wide > 0) {
            if (aspect != null) {
                int colour = aspect.value().color();
                graphics.setColor((colour >> 16 & 0xFF) / 255.0F, (colour >> 8 & 0xFF) / 255.0F,
                        (colour & 0xFF) / 255.0F, 1.0F);
                graphics.blit(PANEL, leftPos + BAR_X, topPos + BAR_Y, BAR_FROM_X, TINTED_FROM_Y, wide, BAR_H, SHEET,
                        SHEET);
                graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            } else {
                graphics.blit(PANEL, leftPos + BAR_X, topPos + BAR_Y, BAR_FROM_X, PLAIN_FROM_Y, wide, BAR_H, SHEET,
                        SHEET);
            }
        }
        graphics.blit(PANEL, leftPos + FRAME_X, topPos + FRAME_Y, BAR_FROM_X, FRAME_FROM_Y, FRAME_W, FRAME_H, SHEET,
                SHEET);
        RenderSystem.disableBlend();
    }

    /** The panel is a picture of the totem, as the original's was; it carries no words. */
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
