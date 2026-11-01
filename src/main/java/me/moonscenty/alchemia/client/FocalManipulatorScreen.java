package me.moonscenty.alchemia.client;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.block.entity.FocalManipulatorBlockEntity;
import me.moonscenty.alchemia.menu.FocalManipulatorMenu;
import me.moonscenty.alchemia.registry.ModSounds;
import me.moonscenty.alchemia.wand.FocusUpgrade;
import me.moonscenty.alchemia.wand.FocusUpgrades;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class FocalManipulatorScreen extends AbstractContainerScreen<FocalManipulatorMenu> {
    private static final ResourceLocation PANEL = Alchemia.id("textures/gui/focal_manipulator.png");
    private static final int SHEET = 256;
    private static final int PANEL_W = 192;
    private static final int PANEL_H = 233;

    private static final int ICON = 16;
    private static final int PICKED_U = 200;
    private static final int PICKED_V = 0;
    private static final int READY_U = 200;
    private static final int READY_V = 16;

    private static final int WORKED_X = 56;
    private static final int WORKED_Y = 32;
    private static final int OFFERED_X = 48;
    private static final int OFFERED_Y = 104;

    private static final int BAR_X = 48;
    private static final int BAR_Y = 88;
    private static final int BAR_W = 96;
    private static final int BAR_H = 8;
    private static final int BAR_EMPTY_U = 8;
    private static final int BAR_FULL_U = 112;
    private static final int BAR_V = 240;

    private static final int LEVELS_X = 125;
    private static final int LEVELS_Y = 64;
    private static final int READY_X = 108;
    private static final int READY_Y = 59;
    private static final int LIST_X = 49;
    private static final int LIST_Y = 68;
    private static final float HALF = 0.5F;
    private static final int LINE = 10;
    private static final int AMOUNT_X = 48;

    private FocusUpgrade picked;

    public FocalManipulatorScreen(FocalManipulatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = PANEL_W;
        imageHeight = PANEL_H;
    }

    private FocalManipulatorBlockEntity bench() {
        return menu.bench();
    }

    private ItemStack focus() {
        return menu.getSlot(0).getItem();
    }

    private List<FocusUpgrade> offered() {
        return FocusUpgrades.offers(focus(), FocusUpgrades.nextRank(focus()), minecraft.player);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partial, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(PANEL, leftPos, topPos, 0, 0, PANEL_W, PANEL_H, SHEET, SHEET);

        FocalManipulatorBlockEntity bench = bench();
        if (bench == null) {
            return;
        }
        if (!offered().contains(picked)) {
            picked = null;
        }

        worked(graphics);
        offers(graphics);
        bill(graphics, bench);
    }

    private void worked(GuiGraphics graphics) {
        List<FocusUpgrade> fitted = FocusUpgrades.on(focus());
        for (int rank = 0; rank < fitted.size(); rank++) {
            graphics.blit(fitted.get(rank).icon(), leftPos + WORKED_X + rank * ICON, topPos + WORKED_Y,
                    0, 0, ICON, ICON, ICON, ICON);
        }
    }

    private void offers(GuiGraphics graphics) {
        List<FocusUpgrade> offered = offered();
        for (int which = 0; which < offered.size(); which++) {
            int x = leftPos + OFFERED_X + which * ICON;
            if (offered.get(which) == picked) {
                graphics.blit(PANEL, x, topPos + OFFERED_Y, PICKED_U, PICKED_V, ICON, ICON, SHEET, SHEET);
            }
            graphics.blit(offered.get(which).icon(), x, topPos + OFFERED_Y, 0, 0, ICON, ICON, ICON, ICON);
        }
    }

    private void bill(GuiGraphics graphics, FocalManipulatorBlockEntity bench) {
        boolean busy = bench.bill() > 0;
        int rank = busy ? bench.rank() : FocusUpgrades.nextRank(focus());
        FocusUpgrade shown = busy ? bench.working() : picked;
        if (rank < 1 || shown == null) {
            return;
        }
        AspectList owing = busy ? bench.owed() : FocusUpgrades.price(shown, rank);
        int whole = busy ? bench.bill() : owing.total();

        graphics.blit(PANEL, leftPos + BAR_X, topPos + BAR_Y, BAR_EMPTY_U, BAR_V, BAR_W, BAR_H, SHEET, SHEET);
        int along = 0;
        for (Holder<Aspect> aspect : owing.sortedByName()) {
            int width = owing.get(aspect) * BAR_W / Math.max(1, whole);
            graphics.blit(PANEL, leftPos + BAR_X + along, topPos + BAR_Y, BAR_FULL_U + along, BAR_V,
                    width, BAR_H, SHEET, SHEET);
            along += width;
        }

        int levels = FocusUpgrades.levels(rank);
        boolean affordable = minecraft != null && minecraft.player != null
                && (minecraft.player.experienceLevel >= levels || minecraft.player.getAbilities().instabuild);
        graphics.drawString(font, String.valueOf(levels), leftPos + LEVELS_X, topPos + LEVELS_Y,
                affordable ? 0x99FF8D : 0xF66D78);
        if (!busy && affordable) {
            graphics.blit(PANEL, leftPos + READY_X, topPos + READY_Y, READY_U, READY_V, ICON, ICON, SHEET, SHEET);
        }

        graphics.pose().pushPose();
        graphics.pose().scale(HALF, HALF, 1.0F);
        int line = 0;
        for (Holder<Aspect> aspect : owing.sortedByName()) {
            int x = (leftPos + LIST_X) * 2;
            int y = (int) ((topPos + LIST_Y - owing.size() * 2.5F) * 2) + line * LINE;
            graphics.drawString(font, aspect.value().displayName(), x, y, aspect.value().color());
            graphics.drawString(font, String.valueOf(owing.get(aspect)), x + AMOUNT_X, y, aspect.value().color());
            line++;
        }
        graphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        FocalManipulatorBlockEntity bench = bench();
        if (bench != null && bench.bill() <= 0) {
            List<FocusUpgrade> offered = offered();
            for (int which = 0; which < offered.size(); which++) {
                if (over(mouseX, mouseY, OFFERED_X + which * ICON, OFFERED_Y, ICON, ICON)) {
                    picked = offered.get(which) == picked ? null : offered.get(which);
                    clack();
                    return true;
                }
            }
            if (picked != null && over(mouseX, mouseY, BAR_X, BAR_Y, BAR_W, BAR_H)) {
                begin();
                clack();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void clack() {
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.CAMERA_CLACK.get(), 1.0F, 0.4F));
        }
    }

    private void begin() {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, picked.ordinal());
        }
    }

    private boolean over(double mouseX, double mouseY, int x, int y, int width, int height) {
        double inX = mouseX - (leftPos + x);
        double inY = mouseY - (topPos + y);
        return inX >= 0 && inY >= 0 && inX < width && inY < height;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        super.render(graphics, mouseX, mouseY, partial);
        renderTooltip(graphics, mouseX, mouseY);

        List<FocusUpgrade> offered = offered();
        for (int which = 0; which < offered.size(); which++) {
            if (over(mouseX, mouseY, OFFERED_X + which * ICON, OFFERED_Y, ICON, ICON)) {
                explain(graphics, offered.get(which), mouseX, mouseY);
                return;
            }
        }
        List<FocusUpgrade> fitted = FocusUpgrades.on(focus());
        for (int rank = 0; rank < fitted.size(); rank++) {
            if (over(mouseX, mouseY, WORKED_X + rank * ICON, WORKED_Y, ICON, ICON)) {
                explain(graphics, fitted.get(rank), mouseX, mouseY);
                return;
            }
        }
    }

    private void explain(GuiGraphics graphics, FocusUpgrade upgrade, int mouseX, int mouseY) {
        graphics.renderComponentTooltip(font, List.of(
                Component.translatable(upgrade.translationKey())
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.UNDERLINE),
                Component.translatable(upgrade.translationKey() + ".note").withStyle(ChatFormatting.GRAY)),
                mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }
}
