package me.moonscenty.alchemia.client;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.crafting.ArcaneRecipe;
import me.moonscenty.alchemia.item.VisHolder;
import me.moonscenty.alchemia.menu.ArcaneWorkbenchMenu;
import me.moonscenty.alchemia.registry.ModAspects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * The bench as the maker sees it: the grid in the middle, the six primal aspects ringed around it, and the wand
 * above the square the work comes out of.
 * <p>
 * The ring is the price. A recipe on the grid lights up the aspects it costs and greys out any the wand cannot
 * cover, which is the whole reason the six are laid out around the cloth instead of written as a line of text.
 */
public class ArcaneWorkbenchScreen extends AbstractContainerScreen<ArcaneWorkbenchMenu> {
    private static final ResourceLocation PANEL = Alchemia.id("textures/gui/arcane_workbench.png");
    private static final int SHEET = 256;
    private static final int PANEL_W = 176;
    private static final int PANEL_H = 222;

    private static final int ICON = 16;

    /**
     * Where the six primals sit, clockwise from the top, and which is which.
     * <p>
     * The order is the one the aspects are always given in — air, fire, water, earth, order, entropy — so that a
     * maker who has seen one bench knows where to look on the next.
     */
    private static final int[][] RING = {{53, 18}, {98, 36}, {98, 90}, {53, 108}, {8, 90}, {8, 36}};

    public ArcaneWorkbenchScreen(ArcaneWorkbenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = PANEL_W;
        imageHeight = PANEL_H;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(PANEL, leftPos, topPos, 0, 0, PANEL_W, PANEL_H, SHEET, SHEET);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        drawPrice(graphics);
        renderTooltip(graphics, mouseX, mouseY);
    }

    /** The panel is already labelled in its own painting, so no words are put over it. */
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    // --- the price ---------------------------------------------------------

    private List<Holder<Aspect>> primals() {
        return List.of(ModAspects.AIR, ModAspects.FIRE, ModAspects.WATER,
                ModAspects.EARTH, ModAspects.ORDER, ModAspects.ENTROPY);
    }

    private void drawPrice(GuiGraphics graphics) {
        AspectList cost = menu.cost();
        ItemStack wand = menu.wand();
        VisHolder holder = VisHolder.of(wand);
        List<Holder<Aspect>> primals = primals();

        for (int place = 0; place < RING.length; place++) {
            Holder<Aspect> aspect = primals.get(place);
            int want = cost.get(aspect);
            if (want <= 0) {
                continue;
            }
            int x = leftPos + RING[place][0];
            int y = topPos + RING[place][1];
            boolean paid = holder != null && holder.held(wand, aspect) >= want;

            drawAspect(graphics, aspect, x, y, paid ? 1F : 0.35F);
            graphics.drawString(font, String.valueOf(want), x + ICON - 6, y + ICON - 6,
                    paid ? 0xFFFFE9C0 : 0xFFFF7070, true);
        }
    }

    /**
     * One aspect, painted in its own colour.
     * <p>
     * Blending is turned on every single time rather than once for the six: drawing a string ends the font batch,
     * and that puts blending back off, which would leave every aspect after the first as a solid disc.
     */
    private void drawAspect(GuiGraphics graphics, Holder<Aspect> aspect, int x, int y, float alpha) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        int colour = aspect.value().color();
        graphics.setColor(((colour >> 16) & 0xFF) / 255F, ((colour >> 8) & 0xFF) / 255F,
                (colour & 0xFF) / 255F, alpha);
        graphics.blit(aspect.value().icon(), x, y, 0, 0, ICON, ICON, ICON, ICON);
        graphics.setColor(1F, 1F, 1F, 1F);
    }

    // --- notes to the maker ------------------------------------------------

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        AspectList cost = menu.cost();
        ItemStack wand = menu.wand();
        VisHolder holder = VisHolder.of(wand);
        List<Holder<Aspect>> primals = primals();

        for (int place = 0; place < RING.length; place++) {
            Holder<Aspect> aspect = primals.get(place);
            int want = cost.get(aspect);
            if (want <= 0 || !over(mouseX, mouseY, leftPos + RING[place][0], topPos + RING[place][1])) {
                continue;
            }
            List<Component> lines = new ArrayList<>();
            lines.add(aspect.value().displayName());
            int have = holder == null ? 0 : holder.held(wand, aspect);
            lines.add(Component.translatable("gui.alchemia.vis_cost", have, want)
                    .withStyle(have >= want ? ChatFormatting.GRAY : ChatFormatting.RED));
            graphics.renderComponentTooltip(font, lines, mouseX, mouseY);
            return;
        }

        // why the result square is empty, when the grid clearly spells something out
        if (over(mouseX, mouseY, leftPos + 134, topPos + 90) && menu.getSlot(ArcaneWorkbenchMenu.SLOT_RESULT).getItem().isEmpty()) {
            menu.matching().ifPresent(found -> {
                ArcaneRecipe recipe = found.value();
                if (!menu.known(recipe)) {
                    graphics.renderTooltip(font,
                            Component.translatable("gui.alchemia.not_researched").withStyle(ChatFormatting.RED),
                            mouseX, mouseY);
                } else if (!menu.affordable()) {
                    graphics.renderTooltip(font,
                            Component.translatable(wand.isEmpty() ? "gui.alchemia.no_wand" : "gui.alchemia.not_enough_vis")
                                    .withStyle(ChatFormatting.RED),
                            mouseX, mouseY);
                }
            });
            return;
        }
        super.renderTooltip(graphics, mouseX, mouseY);
    }

    private static boolean over(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX < x + ICON && mouseY >= y && mouseY < y + ICON;
    }
}
