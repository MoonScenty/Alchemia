package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.systems.RenderSystem;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.menu.PechMenu;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Trading with a pech, as the original drew it: the offer on the left, what comes back on the right, and between
 * them a question mark that turns into the bones once something it prizes is offered and the right is clear.
 */
public class PechScreen extends AbstractContainerScreen<PechMenu> {
    private static final ResourceLocation PANEL = Alchemia.id("textures/gui/pech.png");
    private static final int SHEET = 256;
    /** The original's panel is a pixel narrower than most, and stands taller, its top half see-through. */
    private static final int PANEL_W = 175;
    private static final int PANEL_H = 232;
    private static final int BONES_X = 67;
    private static final int BONES_Y = 24;
    private static final int BONES_SIZE = 25;
    private static final int BONES_FROM_X = 176;

    public PechScreen(PechMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = PANEL_W;
        imageHeight = PANEL_H;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(PANEL, leftPos, topPos, 0, 0, PANEL_W, PANEL_H, SHEET, SHEET);
        if (menu.canRoll()) {
            graphics.blit(PANEL, leftPos + BONES_X, topPos + BONES_Y, BONES_FROM_X, 0, BONES_SIZE, BONES_SIZE, SHEET,
                    SHEET);
        }
        RenderSystem.disableBlend();
    }

    /** The bones roll when clicked, if they are showing. */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (menu.canRoll() && mouseX >= leftPos + BONES_X && mouseX < leftPos + BONES_X + BONES_SIZE
                && mouseY >= topPos + BONES_Y && mouseY < topPos + BONES_Y + BONES_SIZE && minecraft != null
                && minecraft.gameMode != null && minecraft.player != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, PechMenu.ROLL);
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.PECH_DICE.get(),
                    0.95F + minecraft.player.getRandom().nextFloat() * 0.1F, 0.5F));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /** The panel carries no words, as the original's did not. */
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
