package me.moonscenty.alchemia.client;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.entity.Autocaster;
import me.moonscenty.alchemia.menu.AutocasterMenu;
import me.moonscenty.alchemia.wand.Focus;
import net.minecraft.client.gui.GuiGraphics;
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
    private static final int HEARTS_X = 24;

    /** Where the reach is written, and how small. */
    private static final int REACH_X = 44;
    private static final int REACH_Y = 9;
    private static final float HALF = 0.5F;

    public AutocasterScreen(AutocasterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = TurretToggle.PANEL_W;
        imageHeight = TurretToggle.PANEL_H;
    }

    @Override
    protected void init() {
        super.init();
        for (int which = Autocaster.ANIMALS; which <= Autocaster.FRIENDS; which++) {
            addRenderableWidget(new TurretToggle(which, leftPos + TurretToggle.BUTTONS_X,
                    topPos + TurretToggle.BUTTONS_Y + which * TurretToggle.BUTTONS_PITCH, menu.containerId,
                    () -> PANEL, minds -> {
                        Autocaster turret = menu.turret();
                        return turret != null && turret.minds(minds);
                    }));
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partial, int mouseX, int mouseY) {
        Autocaster turret = menu.turret();
        TurretToggle.drawPanel(graphics, PANEL, leftPos, topPos);
        if (turret == null) {
            return;
        }
        TurretToggle.drawHearts(graphics, PANEL, leftPos + HEARTS_X, topPos + TurretToggle.HEARTS_Y, turret);
    }

    /**
     * How far it reaches, which is the focus's own range and not the turret's.
     * <p>
     * Written at half size, as the original wrote it: it is a number to glance at, not a heading.
     */
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        Autocaster turret = menu.turret();
        Focus.Turret reach = turret == null || turret.loaded() == null ? null : turret.loaded().turret();
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
}
