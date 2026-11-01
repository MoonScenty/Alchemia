package me.moonscenty.alchemia.client;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.entity.AdvancedAutomatedCrossbow;
import me.moonscenty.alchemia.entity.Autocaster;
import me.moonscenty.alchemia.entity.AutomatedCrossbow;
import me.moonscenty.alchemia.menu.CrossbowMenu;
import net.minecraft.client.gui.GuiGraphics;
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
    private static final int HEARTS_X = 68;
    private static final int HEARTS_X_ADVANCED = 30;

    public CrossbowScreen(CrossbowMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = TurretToggle.PANEL_W;
        imageHeight = TurretToggle.PANEL_H;
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
                addRenderableWidget(new TurretToggle(which, leftPos + TurretToggle.BUTTONS_X,
                        topPos + TurretToggle.BUTTONS_Y + which * TurretToggle.BUTTONS_PITCH, menu.containerId,
                        this::panel, minds -> menu.turret() instanceof AdvancedAutomatedCrossbow turret
                                && turret.minds(minds)));
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partial, int mouseX, int mouseY) {
        AutomatedCrossbow turret = menu.turret();
        TurretToggle.drawPanel(graphics, panel(), leftPos, topPos);
        if (turret == null) {
            return;
        }
        TurretToggle.drawHearts(graphics, panel(), leftPos + (advanced() ? HEARTS_X_ADVANCED : HEARTS_X),
                topPos + TurretToggle.HEARTS_Y, turret);
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
}
