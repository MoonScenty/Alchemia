package me.moonscenty.alchemia.client;

import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.systems.RenderSystem;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.crafting.ArcaneRecipe;
import me.moonscenty.alchemia.crafting.ArcaneShapedRecipe;
import me.moonscenty.alchemia.crafting.CrucibleRecipe;
import me.moonscenty.alchemia.crafting.InfusionRecipe;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.research.ResearchEntry;
import me.moonscenty.alchemia.research.ResearchPage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;

/**
 * An entry opened up: the write-up spread across the two sheets of the book, turned through a pair at a time.
 * <p>
 * Laid out as the original laid it out: the book drawn at 1.3 times a 256 by 181 pane, the writing set on that pane
 * unscaled, a sheet every 152 across, and each kind of recipe drawn over its own picture from the book's second
 * sheet, with the ingredients, the result and the aspects in the original's places.
 */
public class ResearchPageScreen extends Screen {
    private static final ResourceLocation BOOK = Alchemia.id("textures/gui/research_book.png");
    /** The pictures recipes are drawn over: a crafting grid, an arcane grid, a crucible, a furnace, an altar. */
    private static final ResourceLocation PLATES = Alchemia.id("textures/gui/research_book_overlay.png");
    private static final int SHEET = 512;

    private static final int PANE_W = 256;
    private static final int PANE_H = 181;
    private static final float BOOK_SCALE = 1.3F;
    /** How far apart the two sheets are, and how wide a sheet's writing runs. */
    private static final int SIDE_STEP = 152;
    private static final int TEXT_W = 139;
    private static final int LINE = 9;
    private static final int ICON = 16;
    /** How long an ingredient with several options rests on each of them, in ticks. */
    private static final int CYCLE_TICKS = 20;
    private static final int TITLE_COLOUR = 0xFF303030;
    private static final int HEADING_COLOUR = 0xFF505050;
    private static final int TEXT_COLOUR = 0xFF000000;

    // the turn arrows under the sheets, and the rule above and below the entry's name
    private static final int TURN_W = 12;
    private static final int TURN_H = 8;

    private final AlchemonomiconScreen parent;
    private final ResourceLocation id;
    private final List<ResearchPage> pages;

    /** The left sheet of the pair on show; the right is the one after it. */
    private int spread;
    private long ticks;

    public ResearchPageScreen(AlchemonomiconScreen parent, ResourceLocation id, ResearchEntry entry) {
        super(ResearchEntry.displayName(id));
        this.parent = parent;
        this.id = id;
        PlayerKnowledge knowledge = PlayerKnowledge.of(net.minecraft.client.Minecraft.getInstance().player);
        this.pages = entry.pagesFor(knowledge::hasResearch);
    }

    @Override
    public void tick() {
        ticks++;
    }

    /** The top left of the unscaled pane everything on the sheets is placed against. */
    private int paneX() {
        return (width - PANE_W) / 2;
    }

    private int paneY() {
        return (height - PANE_H) / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.pose().pushPose();
        graphics.pose().translate((width - PANE_W * BOOK_SCALE) / 2F, (height - PANE_H * BOOK_SCALE) / 2F, 0);
        graphics.pose().scale(BOOK_SCALE, BOOK_SCALE, 1F);
        // the pane is read off the sheet as if the sheet were 256 wide, which takes in the whole book
        graphics.blit(BOOK, 0, 0, PANE_W, PANE_H, 0, 0, SHEET, PANE_H * 2, SHEET, SHEET);
        graphics.pose().popPose();

        drawSheet(graphics, 0, spread);
        drawSheet(graphics, 1, spread + 1);
        drawTurns(graphics);
    }

    /** Vanilla blurs the world behind a screen, which leaves the writing hard to read. A plain dimming does instead. */
    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillGradient(0, 0, width, height, 0xB0101018, 0xC0101018);
    }

    private void drawSheet(GuiGraphics graphics, int side, int index) {
        if (index >= pages.size()) {
            return;
        }
        int x = paneX();
        int y = paneY();
        boolean first = index == 0 && side == 0;
        if (first) {
            drawTitle(graphics, x, y);
        }
        ResearchPage page = pages.get(index);
        switch (page.type()) {
            case TEXT -> drawText(graphics, (ResearchPage.Text) page, x - 15 + side * SIDE_STEP,
                    y - 10 + (first ? 2 * LINE : 0));
            case RECIPE -> drawRecipe(graphics, (ResearchPage.Recipe) page, x - 4 + side * SIDE_STEP, y - 8);
        }
    }

    /** The entry's name at the head of the first sheet, between two rules, made smaller if it is long. */
    private void drawTitle(GuiGraphics graphics, int x, int y) {
        rule(graphics, x + 4, y - 13);
        rule(graphics, x + 4, y + 4);
        Component title = ResearchEntry.displayName(id);
        int wide = font.width(title);
        if (wide <= 130) {
            graphics.drawString(font, title, x + 52 - wide / 2, y - 6, TITLE_COLOUR, false);
        } else {
            float shrink = 130F / wide;
            graphics.pose().pushPose();
            graphics.pose().translate(x + 52 - wide / 2F * shrink, y - 6 + 6 * (1 - shrink), 0);
            graphics.pose().scale(shrink, shrink, 1F);
            graphics.drawString(font, title, 0, 0, TITLE_COLOUR, false);
            graphics.pose().popPose();
        }
    }

    private static void rule(GuiGraphics graphics, int x, int y) {
        graphics.blit(BOOK, x, y, 96, 4, 48, 368, 192, 8, SHEET, SHEET);
    }

    private void drawText(GuiGraphics graphics, ResearchPage.Text page, int x, int y) {
        // a passage that outruns the sheet is cut off rather than written over the turn arrows
        int floor = paneY() + 180;
        for (FormattedCharSequence line : font.split(Component.translatable(page.key()), TEXT_W)) {
            if (y > floor) {
                break;
            }
            graphics.drawString(font, line, x, y, TEXT_COLOUR, false);
            y += LINE;
        }
    }

    // --- recipes -----------------------------------------------------------

    private void drawRecipe(GuiGraphics graphics, ResearchPage.Recipe page, int x, int y) {
        Optional<RecipeHolder<?>> found = minecraft.level.getRecipeManager().byKey(page.recipe());
        if (found.isEmpty()) {
            graphics.drawString(font, Component.translatable("research.alchemia.recipe_missing"), x, y, 0xFF8A3020,
                    false);
            return;
        }
        Recipe<?> recipe = found.get().value();
        ItemStack result = recipe.getResultItem(minecraft.level.registryAccess());
        if (recipe instanceof AbstractCookingRecipe) {
            drawSmelting(graphics, recipe, result, x, y);
        } else if (recipe instanceof CrucibleRecipe crucible) {
            drawCrucible(graphics, crucible, result, x, y);
        } else if (recipe instanceof InfusionRecipe infusion) {
            drawInfusion(graphics, infusion, result, x, y);
        } else if (recipe instanceof ArcaneRecipe arcane) {
            drawArcane(graphics, recipe, arcane, result, x, y);
        } else {
            drawCrafting(graphics, recipe, result, x, y);
        }
    }

    private void heading(GuiGraphics graphics, String key, int x, int y) {
        Component heading = Component.translatable(key);
        graphics.drawString(font, heading, x + 56 - font.width(heading) / 2, y, HEADING_COLOUR, false);
    }

    /** A piece of the second sheet, given in the sheet's own pixels and drawn at half that size. */
    private static void plate(GuiGraphics graphics, int x, int y, int fromX, int fromY, int wide, int tall) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(PLATES, x, y, wide, tall, fromX, fromY, wide, tall, SHEET, SHEET);
    }

    /** The ordinary bench: a three by three grid under the thing it makes. */
    private void drawCrafting(GuiGraphics graphics, Recipe<?> recipe, ItemStack result, int x, int y) {
        heading(graphics, "research.alchemia.crafting", x, y);
        plate(graphics, x + 4, y + 64, 120, 30, 104, 104);
        plate(graphics, x + 40, y + 24, 40, 6, 32, 32);
        drawStack(graphics, result, x + 48, y + 32);
        drawGrid(graphics, recipe, x + 16, y + 76);
    }

    /** The arcane bench: the same, on its own grid, with what it asks of a wand along the foot. */
    private void drawArcane(GuiGraphics graphics, Recipe<?> recipe, ArcaneRecipe arcane, ItemStack result, int x,
            int y) {
        heading(graphics, "research.alchemia.arcane_crafting", x, y);
        plate(graphics, x + 4, y + 54, 224, 30, 104, 104);
        plate(graphics, x + 40, y + 14, 40, 6, 32, 32);
        drawStack(graphics, result, x + 48, y + 22);
        drawGrid(graphics, recipe, x + 16, y + 66);
        List<Holder<Aspect>> asked = arcane.cost().sortedByAmount();
        for (int index = 0; index < asked.size(); index++) {
            drawAspect(graphics, asked.get(index), arcane.cost().get(asked.get(index)),
                    x + 14 + 18 * index + (5 - asked.size()) * 8, y + 172);
        }
    }

    /** A furnace: what goes in at the top and what comes out at the bottom, over a flame. */
    private void drawSmelting(GuiGraphics graphics, Recipe<?> recipe, ItemStack result, int x, int y) {
        heading(graphics, "research.alchemia.smelting", x, y);
        plate(graphics, x, y + 28, 0, 384, 112, 128);
        List<Ingredient> ingredients = recipe.getIngredients();
        if (!ingredients.isEmpty()) {
            drawIngredient(graphics, ingredients.getFirst(), x + 48, y + 64);
        }
        drawStack(graphics, result, x + 48, y + 144);
    }

    /** The crucible: the result over its rim, the catalyst dropped in, and what must be dissolved, in rows of three. */
    private void drawCrucible(GuiGraphics graphics, CrucibleRecipe crucible, ItemStack result, int x, int y) {
        heading(graphics, "research.alchemia.in_crucible", x, y);
        plate(graphics, x, y + 28, 0, 6, 112, 34);
        plate(graphics, x, y + 92, 0, 40, 112, 96);
        plate(graphics, x + 42, y + 76, 200, 168, 22, 26);
        drawStack(graphics, result, x + 48, y + 36);
        drawIngredient(graphics, crucible.catalyst(), x + 26, y + 72);
        drawAspectRows(graphics, crucible.aspects(), 3, x + 28, y + 128);
    }

    /** The altar: the result on top, the centrepiece in the middle, the rest round it, and the essentia below. */
    private void drawInfusion(GuiGraphics graphics, InfusionRecipe infusion, ItemStack result, int x, int y) {
        heading(graphics, "research.alchemia.in_matrix", x, y);
        plate(graphics, x, y + 20, 0, 6, 112, 34);
        plate(graphics, x, y + 58, 400, 154, 120, 88);
        drawStack(graphics, result, x + 48, y + 28);
        drawIngredient(graphics, infusion.central(), x + 48, y + 94);
        List<Ingredient> ring = infusion.ring();
        float step = ring.isEmpty() ? 0F : 360F / ring.size();
        float angle = -90F;
        for (Ingredient part : ring) {
            int at = (int) (Mth.cos(angle / 180F * Mth.PI) * 40F) - 8;
            int down = (int) (Mth.sin(angle / 180F * Mth.PI) * 40F) - 8;
            drawIngredient(graphics, part, x + 56 + at, y + 102 + down);
            angle += step;
        }
        drawAspectRows(graphics, infusion.essentia(), 5, x + 8, y + 164);
    }

    /**
     * Aspects in rows of a given length, twenty apart, the last row nudged along the way the original nudged it, and
     * the whole block lifted by half a row for every row past the first.
     */
    private void drawAspectRows(GuiGraphics graphics, AspectList aspects, int across, int x, int y) {
        List<Holder<Aspect>> order = aspects.sortedByAmount();
        int size = order.size();
        if (size == 0) {
            return;
        }
        int rows = (size - 1) / across;
        int nudge = (across - size % across) * 10;
        int top = y - 10 * rows;
        for (int index = 0; index < size; index++) {
            boolean shifted = index / across >= rows && (rows > 1 || size < across);
            int ax = x + (index % across) * 20 + (shifted ? nudge : 0);
            int ay = top + (index / across) * 20;
            drawAspect(graphics, order.get(index), aspects.get(order.get(index)), ax, ay);
        }
    }

    /** The grid of a bench recipe, three to a row, thirty-two apart. */
    private void drawGrid(GuiGraphics graphics, Recipe<?> recipe, int x, int y) {
        List<Ingredient> ingredients = recipe.getIngredients();
        int columns = recipe instanceof ShapedRecipe shaped ? shaped.getWidth()
                : recipe instanceof ArcaneShapedRecipe arcane ? arcane.pattern().width() : 3;
        for (int index = 0; index < ingredients.size(); index++) {
            drawIngredient(graphics, ingredients.get(index), x + (index % columns) * 32, y + (index / columns) * 32);
        }
    }

    private void drawIngredient(GuiGraphics graphics, Ingredient ingredient, int x, int y) {
        ItemStack[] options = ingredient.getItems();
        if (options.length > 0) {
            graphics.renderItem(options[(int) (ticks / CYCLE_TICKS % options.length)], x, y);
        }
    }

    private void drawStack(GuiGraphics graphics, ItemStack stack, int x, int y) {
        graphics.renderItem(stack, x, y);
        graphics.renderItemDecorations(font, stack, x, y);
    }

    /** One aspect in its own colour, with how much of it is wanted written in its corner. */
    private void drawAspect(GuiGraphics graphics, Holder<Aspect> aspect, int amount, int x, int y) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        int colour = aspect.value().color();
        graphics.setColor(((colour >> 16) & 0xFF) / 255F, ((colour >> 8) & 0xFF) / 255F, (colour & 0xFF) / 255F, 1F);
        graphics.blit(aspect.value().icon(), x, y, 0, 0, ICON, ICON, ICON, ICON);
        graphics.setColor(1F, 1F, 1F, 1F);
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 200);
        String count = String.valueOf(amount);
        graphics.drawString(font, count, x + ICON - font.width(count) + 1, y + ICON - 7, 0xFFFFFFFF, true);
        graphics.pose().popPose();
    }

    // --- turning -----------------------------------------------------------

    private int turnX(boolean forward) {
        return forward ? paneX() + 262 : paneX() - 16;
    }

    private int turnY() {
        return paneY() + 190;
    }

    private void drawTurns(GuiGraphics graphics) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (spread > 0) {
            graphics.blit(BOOK, turnX(false), turnY(), TURN_W, TURN_H, 0, 368, 24, 16, SHEET, SHEET);
        }
        if (spread + 2 < pages.size()) {
            graphics.blit(BOOK, turnX(true), turnY(), TURN_W, TURN_H, 24, 368, 24, 16, SHEET, SHEET);
        }
    }

    private boolean overTurn(double mouseX, double mouseY, boolean forward) {
        int x = turnX(forward);
        int y = turnY();
        return mouseX >= x && mouseX < x + TURN_W && mouseY >= y && mouseY < y + TURN_H;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (spread > 0 && overTurn(mouseX, mouseY, false)) {
            spread -= 2;
            return true;
        }
        if (spread + 2 < pages.size() && overTurn(mouseX, mouseY, true)) {
            spread += 2;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /** Closing goes back to the tree rather than out of the book, which keeps the branch and the panning. */
    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
