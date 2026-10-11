package me.moonscenty.alchemia.client;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.entity.SpaBlockEntity;
import me.moonscenty.alchemia.menu.SpaMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * The spa as the original drew it: a pocket for salts, a gauge for the tank, and a small switch between them.
 */
public class SpaScreen extends AbstractContainerScreen<SpaMenu> {
    private static final ResourceLocation PANEL = Alchemia.id("textures/gui/spa.png");
    private static final int SHEET = 256;
    private static final int PANEL_W = 176;
    private static final int PANEL_H = 166;

    // Laid out as the original laid it out.
    private static final int SWITCH_X = 89;
    private static final int SWITCH_Y = 35;
    private static final int SWITCH_SIZE = 8;
    private static final int SWITCH_FROM_X = 208;
    private static final int MIXING_FROM_Y = 16;
    private static final int POURING_FROM_Y = 32;

    /** The liquid is drawn at half size, in six squares one above the other, and covered down to how full it is. */
    private static final int LIQUID_X = 107;
    private static final int LIQUID_Y = 15;
    private static final int LIQUID_SQUARE = 8;
    private static final int LIQUID_SQUARES = 6;
    private static final int LIQUID_H = LIQUID_SQUARE * LIQUID_SQUARES;
    private static final int COVER_W = 10;

    private static final int GLASS_X = 106;
    private static final int GLASS_Y = 11;
    private static final int GLASS_W = 10;
    private static final int GLASS_H = 55;
    private static final int GLASS_FROM_X = 232;

    public SpaScreen(SpaMenu menu, Inventory inventory, Component title) {
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
        graphics.blit(PANEL, leftPos + SWITCH_X, topPos + SWITCH_Y, SWITCH_FROM_X,
                menu.mixing() ? MIXING_FROM_Y : POURING_FROM_Y, SWITCH_SIZE, SWITCH_SIZE, SHEET, SHEET);
        Fluid fluid = menu.fluid();
        if (menu.amount() > 0 && fluid != Fluids.EMPTY) {
            liquid(graphics, fluid, menu.amount() / (float) SpaBlockEntity.CAPACITY);
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(PANEL, leftPos + GLASS_X, topPos + GLASS_Y, GLASS_FROM_X, 0, GLASS_W, GLASS_H, SHEET, SHEET);
        RenderSystem.disableBlend();
    }

    /** The tank's liquid in its own picture and colour, with the empty part covered over by the panel. */
    private void liquid(GuiGraphics graphics, Fluid fluid, float full) {
        IClientFluidTypeExtensions look = IClientFluidTypeExtensions.of(fluid);
        TextureAtlasSprite sprite = minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(look.getStillTexture(new FluidStack(fluid, 1)));
        int tint = look.getTintColor(new FluidStack(fluid, 1));
        float red = (tint >> 16 & 0xFF) / 255.0F;
        float green = (tint >> 8 & 0xFF) / 255.0F;
        float blue = (tint & 0xFF) / 255.0F;
        for (int square = 0; square < LIQUID_SQUARES; square++) {
            graphics.blit(leftPos + LIQUID_X, topPos + LIQUID_Y + square * LIQUID_SQUARE, 0, LIQUID_SQUARE,
                    LIQUID_SQUARE, sprite, red, green, blue, 1.0F);
        }
        int empty = (int) (LIQUID_H - LIQUID_H * Math.min(1.0F, full));
        if (empty > 0) {
            graphics.blit(PANEL, leftPos + LIQUID_X, topPos + LIQUID_Y, LIQUID_X, LIQUID_Y, COVER_W, empty, SHEET,
                    SHEET);
        }
    }

    /** The switch flips between mixing and pouring the tank as it is. */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (over(mouseX, mouseY, SWITCH_X, SWITCH_Y, SWITCH_SIZE, SWITCH_SIZE) && minecraft != null
                && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, SpaMenu.TOGGLE_MIXING);
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /** The panel is a picture of the spa, as the original's was; it carries no words. */
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    /** What is in the tank and how much, over the gauge; what the switch is set to, over the switch. */
    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (over(mouseX, mouseY, GLASS_X - 2, GLASS_Y - 1, GLASS_W, GLASS_H) && menu.amount() > 0) {
            graphics.renderComponentTooltip(font, List.of(
                    new FluidStack(menu.fluid(), menu.amount()).getHoverName(),
                    Component.translatable("gui.alchemia.spa.amount", menu.amount())), mouseX, mouseY);
            return;
        }
        if (over(mouseX, mouseY, SWITCH_X - 1, SWITCH_Y - 1, SWITCH_SIZE + 2, SWITCH_SIZE + 2)) {
            graphics.renderTooltip(font, Component.translatable(menu.mixing() ? "gui.alchemia.spa.mixing"
                    : "gui.alchemia.spa.pouring"), mouseX, mouseY);
            return;
        }
        super.renderTooltip(graphics, mouseX, mouseY);
    }

    private boolean over(double mouseX, double mouseY, int x, int y, int wide, int tall) {
        return mouseX >= leftPos + x && mouseX < leftPos + x + wide
                && mouseY >= topPos + y && mouseY < topPos + y + tall;
    }
}
