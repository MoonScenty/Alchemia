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

public class AutocasterScreen extends AbstractContainerScreen<AutocasterMenu> {
    private static final ResourceLocation PANEL = Alchemia.id("textures/gui/autocaster.png");
    private static final int HEARTS_X = 24;

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
