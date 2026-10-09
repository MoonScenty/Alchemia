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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * The bench as the maker sees it, laid out as the original laid it out: a green cloth with the grid spread across
 * it, the six primal aspects in their circles round the edge, the wand in its holder top right and the work coming
 * out below it.
 * <p>
 * The circles are the price. A recipe on the grid puts each aspect it costs in its circle: steady when the wand can
 * cover it, faint and breathing when it cannot, with the original's warning beside the result.
 */
public class ArcaneWorkbenchScreen extends AbstractContainerScreen<ArcaneWorkbenchMenu> {
    private static final ResourceLocation PANEL = Alchemia.id("textures/gui/arcane_workbench.png");
    private static final int SHEET = 256;
    private static final int PANEL_W = 190;
    private static final int PANEL_H = 234;

    private static final int ICON = 16;

    /**
     * The middle of each primal's circle, in the order the original gave the primals: air at the top, then earth,
     * fire, water at the foot, order and entropy, going round anticlockwise.
     */
    private static final int[][] CIRCLES = {{72, 21}, {24, 43}, {24, 102}, {72, 124}, {120, 102}, {120, 43}};
    /** Where the warning goes when the wand falls short: to the right of the result, at half size. */
    private static final int WARNING_X = 168;
    private static final int WARNING_Y = 46;
    private static final int WARNING_COLOUR = 0xFFEE6E6E;

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
        drawPrice(graphics, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    /** The panel is already labelled in its own painting, so no words are put over it. */
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    // --- the price ---------------------------------------------------------

    private static List<Holder<Aspect>> primals() {
        return List.of(ModAspects.AIR, ModAspects.EARTH, ModAspects.FIRE,
                ModAspects.WATER, ModAspects.ORDER, ModAspects.ENTROPY);
    }

    private void drawPrice(GuiGraphics graphics, float partialTick) {
        AspectList cost = menu.cost();
        ItemStack wand = menu.wand();
        VisHolder holder = VisHolder.of(wand);
        List<Holder<Aspect>> primals = primals();
        float ticks = minecraft != null && minecraft.player != null ? minecraft.player.tickCount + partialTick : 0F;
        boolean shortAnywhere = false;

        for (int place = 0; place < CIRCLES.length; place++) {
            Holder<Aspect> aspect = primals.get(place);
            int want = cost.get(aspect);
            if (want <= 0) {
                continue;
            }
            boolean paid = holder != null && holder.held(wand, aspect) >= want;
            shortAnywhere |= !paid;
            // the original let what cannot be paid breathe, each circle a little out of step with the last
            float alpha = paid ? 1F : 0.5F + Mth.sin((ticks + place * 10) / 2F) * 0.2F - 0.2F;
            int x = leftPos + CIRCLES[place][0] - ICON / 2;
            int y = topPos + CIRCLES[place][1] - ICON / 2;
            drawAspect(graphics, aspect, x, y, alpha);
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 200);
            String count = String.valueOf(want);
            graphics.drawString(font, count, x + ICON - font.width(count) + 1, y + ICON - 7,
                    paid ? 0xFFFFFFFF : 0xFFFF7070, true);
            graphics.pose().popPose();
        }

        if (shortAnywhere && menu.matching().isPresent()) {
            Component warning = Component.translatable("gui.alchemia.not_enough_vis");
            graphics.pose().pushPose();
            graphics.pose().translate(leftPos + WARNING_X, topPos + WARNING_Y, 200);
            graphics.pose().scale(0.5F, 0.5F, 1F);
            graphics.drawString(font, warning, -font.width(warning) / 2, 0, WARNING_COLOUR, false);
            graphics.pose().popPose();
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

        for (int place = 0; place < CIRCLES.length; place++) {
            Holder<Aspect> aspect = primals.get(place);
            int want = cost.get(aspect);
            int x = leftPos + CIRCLES[place][0] - ICON / 2;
            int y = topPos + CIRCLES[place][1] - ICON / 2;
            if (want <= 0 || !over(mouseX, mouseY, x, y)) {
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
        if (over(mouseX, mouseY, leftPos + ArcaneWorkbenchMenu.RESULT_X, topPos + ArcaneWorkbenchMenu.RESULT_Y)
                && menu.getSlot(ArcaneWorkbenchMenu.SLOT_RESULT).getItem().isEmpty()) {
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
