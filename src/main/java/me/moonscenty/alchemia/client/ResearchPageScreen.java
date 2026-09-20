package me.moonscenty.alchemia.client;

import java.util.List;
import java.util.Optional;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.research.ResearchEntry;
import me.moonscenty.alchemia.research.ResearchPage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;

/**
 * An entry opened up: the write-up spread across the two sheets of the book, turned through a pair at a time.
 */
public class ResearchPageScreen extends Screen {
    private static final ResourceLocation BOOK = Alchemia.id("textures/gui/research_book.png");
    private static final ResourceLocation TURN_BACK = ResourceLocation.withDefaultNamespace("recipe_book/page_backward");
    private static final ResourceLocation TURN_BACK_LIT =
            ResourceLocation.withDefaultNamespace("recipe_book/page_backward_highlighted");
    private static final ResourceLocation TURN_ON = ResourceLocation.withDefaultNamespace("recipe_book/page_forward");
    private static final ResourceLocation TURN_ON_LIT =
            ResourceLocation.withDefaultNamespace("recipe_book/page_forward_highlighted");

    private static final int BOOK_W = 512;
    private static final int BOOK_H = 356;
    // The two sheets inside the cover, measured off the texture.
    private static final int LEFT_X = 24;
    private static final int RIGHT_X = 261;
    private static final int SHEET_Y = 21;
    private static final int SHEET_W = 227;
    private static final int SHEET_H = 302;

    /** Breathing room between the writing and the edge of the sheet. */
    private static final int PAD = 10;
    private static final int LINE = 9;
    private static final int TURN_W = 12;
    private static final int TURN_H = 17;

    /** A crafting slot, and the grid it sits in. */
    private static final int SLOT = 18;
    private static final int SLOT_EDGE = 0xFF5A4028;
    private static final int SLOT_FACE = 0xFF241608;
    /** How long an ingredient with several options rests on each of them. */
    private static final int CYCLE_TICKS = 20;

    private final AlchemonomiconScreen parent;
    private final ResourceLocation id;
    private final List<ResearchPage> pages;

    /** The left sheet of the pair on show; the right is the one after it. */
    private int spread;
    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    private float scale;
    private long ticks;

    public ResearchPageScreen(AlchemonomiconScreen parent, ResourceLocation id, ResearchEntry entry) {
        super(ResearchEntry.displayName(id));
        this.parent = parent;
        this.id = id;
        PlayerKnowledge knowledge = PlayerKnowledge.of(net.minecraft.client.Minecraft.getInstance().player);
        this.pages = entry.pagesFor(knowledge::hasResearch);
    }

    @Override
    protected void init() {
        panelW = Math.min(width - 32, BOOK_W);
        panelH = panelW * BOOK_H / BOOK_W;
        if (panelH > height - 16) {
            panelH = height - 16;
            panelW = panelH * BOOK_W / BOOK_H;
        }
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;
        scale = (float) panelW / BOOK_W;
    }

    @Override
    public void tick() {
        ticks++;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.blit(BOOK, panelX, panelY, panelW, panelH, 0, 0, BOOK_W, BOOK_H, 512, 512);

        drawSheet(graphics, false, spread);
        drawSheet(graphics, true, spread + 1);
        drawTurns(graphics, mouseX, mouseY);
    }

    /** Vanilla blurs the world behind a screen, which leaves the writing hard to read. A plain dimming does instead. */
    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillGradient(0, 0, width, height, 0xB0101018, 0xC0101018);
    }

    private int sheetX(boolean right) {
        return panelX + Math.round((right ? RIGHT_X : LEFT_X) * scale);
    }

    private int sheetY() {
        return panelY + Math.round(SHEET_Y * scale);
    }

    private int sheetW() {
        return Math.round(SHEET_W * scale);
    }

    private void drawSheet(GuiGraphics graphics, boolean right, int index) {
        if (index >= pages.size()) {
            return;
        }
        int x = sheetX(right) + PAD;
        int y = sheetY() + PAD;
        int usable = sheetW() - 2 * PAD;

        // the entry names itself once, at the head of the first sheet
        if (index == 0) {
            Component title = ResearchEntry.displayName(id);
            graphics.drawString(font, title, x + (usable - font.width(title)) / 2, y, 0xFF3A2A18, false);
            y += LINE * 2;
        }

        ResearchPage page = pages.get(index);
        switch (page.type()) {
            case TEXT -> drawText(graphics, (ResearchPage.Text) page, x, y, usable);
            case RECIPE -> drawRecipe(graphics, (ResearchPage.Recipe) page, x, y, usable);
        }
    }

    private void drawText(GuiGraphics graphics, ResearchPage.Text page, int x, int y, int usable) {
        // a passage that outruns the sheet is cut off rather than written over the turn arrows
        int floor = turnY() - LINE;
        for (FormattedCharSequence line : font.split(Component.translatable(page.key()), usable)) {
            if (y > floor) {
                break;
            }
            graphics.drawString(font, line, x, y, 0xFF4A3520, false);
            y += LINE;
        }
    }

    /**
     * A recipe is shown as it is made: the ingredients on the left, the result on the right. An ingredient that
     * accepts several things cycles through them, so nothing is hidden behind a tag name.
     */
    private void drawRecipe(GuiGraphics graphics, ResearchPage.Recipe page, int x, int y, int usable) {
        Optional<RecipeHolder<?>> found = minecraft.level.getRecipeManager().byKey(page.recipe());
        if (found.isEmpty()) {
            graphics.drawString(font, Component.translatable("research.alchemia.recipe_missing"), x, y, 0xFF8A3020, false);
            return;
        }

        net.minecraft.world.item.crafting.Recipe<?> recipe = found.get().value();
        List<Ingredient> ingredients = recipe.getIngredients();
        int columns = recipe instanceof ShapedRecipe shaped ? shaped.getWidth() : gridWidth(ingredients.size());
        int rows = recipe instanceof ShapedRecipe shaped ? shaped.getHeight()
                : Math.ceilDiv(ingredients.size(), columns);

        Component heading = Component.translatable(recipe instanceof AbstractCookingRecipe
                ? "research.alchemia.smelting" : "research.alchemia.crafting");
        graphics.drawString(font, heading, x + (usable - font.width(heading)) / 2, y, 0xFF3A2A18, false);
        y += LINE * 2;

        int gridW = columns * SLOT;
        int arrow = 14;
        int wide = gridW + arrow + SLOT;
        int gx = x + Math.max(0, (usable - wide) / 2);
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                int slot = row * columns + column;
                drawSlot(graphics, gx + column * SLOT, y + row * SLOT,
                        slot < ingredients.size() ? ingredients.get(slot) : Ingredient.EMPTY);
            }
        }

        int middle = y + (rows * SLOT - SLOT) / 2;
        drawArrow(graphics, gx + gridW + 3, middle + SLOT / 2);
        drawStack(graphics, gx + gridW + arrow, middle, recipe.getResultItem(minecraft.level.registryAccess()));
    }

    /** Shapeless recipes carry no shape, so they are laid out in whatever square fits. */
    private static int gridWidth(int count) {
        return count <= 1 ? 1 : count <= 4 ? 2 : 3;
    }

    private void drawSlot(GuiGraphics graphics, int x, int y, Ingredient ingredient) {
        graphics.fill(x, y, x + SLOT, y + SLOT, SLOT_EDGE);
        graphics.fill(x + 1, y + 1, x + SLOT - 1, y + SLOT - 1, SLOT_FACE);
        if (ingredient.isEmpty()) {
            return;
        }
        ItemStack[] options = ingredient.getItems();
        if (options.length > 0) {
            graphics.renderItem(options[(int) (ticks / CYCLE_TICKS % options.length)], x + 1, y + 1);
        }
    }

    private void drawStack(GuiGraphics graphics, int x, int y, ItemStack stack) {
        graphics.fill(x, y, x + SLOT, y + SLOT, SLOT_EDGE);
        graphics.fill(x + 1, y + 1, x + SLOT - 1, y + SLOT - 1, SLOT_FACE);
        graphics.renderItem(stack, x + 1, y + 1);
        graphics.renderItemDecorations(font, stack, x + 1, y + 1);
    }

    private void drawArrow(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y - 1, x + 8, y + 1, 0xFF4A3520);
        for (int step = 0; step < 4; step++) {
            graphics.fill(x + 7 - step, y - 1 - step, x + 8 - step, y + 1 + step, 0xFF4A3520);
        }
    }

    // --- turning -----------------------------------------------------------

    /** The turn arrows sit on the foot of the sheet, just inside its bottom edge. */
    private int turnY() {
        return panelY + Math.round((SHEET_Y + SHEET_H) * scale) - TURN_H - 2;
    }

    private int turnX(boolean forward) {
        return forward ? sheetX(true) + sheetW() - PAD - TURN_W : sheetX(false) + PAD;
    }

    private void drawTurns(GuiGraphics graphics, int mouseX, int mouseY) {
        if (spread > 0) {
            boolean over = overTurn(mouseX, mouseY, false);
            graphics.blitSprite(over ? TURN_BACK_LIT : TURN_BACK, turnX(false), turnY(), TURN_W, TURN_H);
        }
        if (spread + 2 < pages.size()) {
            boolean over = overTurn(mouseX, mouseY, true);
            graphics.blitSprite(over ? TURN_ON_LIT : TURN_ON, turnX(true), turnY(), TURN_W, TURN_H);
        }
    }

    private boolean overTurn(int mouseX, int mouseY, boolean forward) {
        int x = turnX(forward);
        int y = turnY();
        return mouseX >= x && mouseX < x + TURN_W && mouseY >= y && mouseY < y + TURN_H;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (spread > 0 && overTurn((int) mouseX, (int) mouseY, false)) {
            spread -= 2;
            return true;
        }
        if (spread + 2 < pages.size() && overTurn((int) mouseX, (int) mouseY, true)) {
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
