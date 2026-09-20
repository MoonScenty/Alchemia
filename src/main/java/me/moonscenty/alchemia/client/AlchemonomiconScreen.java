package me.moonscenty.alchemia.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.research.ModResearch;
import me.moonscenty.alchemia.research.ResearchCategory;
import me.moonscenty.alchemia.research.ResearchEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/**
 * The alchemonomicon: a branch of study per tab, and everything in it laid out as a tree that can be dragged about.
 */
public class AlchemonomiconScreen extends Screen {
    /** The open book, cut through in the middle so the tree behind it shows. Drawn over the sky, not under it. */
    private static final ResourceLocation FRAME = Alchemia.id("textures/gui/research_frame.png");

    /** The book takes up the top of its sheet; the rest holds odds and ends. */
    private static final int BOOK_W = 512;
    private static final int BOOK_H = 356;
    // The window cut in the frame, measured off the sheet. It is not centred: the bottom board is the widest.
    private static final int HOLE_X = 20;
    private static final int HOLE_Y = 21;
    private static final int HOLE_W = 472;
    private static final int HOLE_H = 302;
    /** Where the run of tabs starts, clear of the ornament worked into the frame's top corner. */
    private static final int TAB_TOP = 30;

    /** The star field behind every branch, shared by all of them. */
    private static final ResourceLocation OVERLAY = Alchemia.id("textures/gui/research_overlay.png");
    private static final int SKY = 1024;
    /** How far the tree can be pulled from the middle. */
    private static final float PAN_LIMIT = 400F;
    // The two sky layers travel at different speeds, which is what gives the window its depth.
    private static final float BACK_DRIFT = 0.5F;
    private static final float OVER_DRIFT = 0.667F;

    /** A node plate is a touch wider than the item it frames, which sits in the middle of it. */
    private static final int PLATE = 26;
    private static final int ICON_INSET = (PLATE - 16) / 2;
    /** How far apart two neighbouring entries sit. */
    private static final int STEP = 42;
    private static final int TAB = 24;
    /** Wide enough that the outlines of two tabs do not run into one another and read as a single bar. */
    private static final int TAB_GAP = 6;
    /** How far a tab tucks in behind the edge of the page, so it reads as bound into the book. */
    private static final int TAB_TUCK = 4;

    // Tab plates, alongside the node plates in the GUI atlas. The picture faces away from the book, so the two sides
    // are mirror images of one another rather than the same plate drawn twice.
    private static final ResourceLocation TAB_LEFT = Alchemia.id("research/tab_left");
    private static final ResourceLocation TAB_LEFT_OPEN = Alchemia.id("research/tab_left_open");
    private static final ResourceLocation TAB_RIGHT = Alchemia.id("research/tab_right");
    private static final ResourceLocation TAB_RIGHT_OPEN = Alchemia.id("research/tab_right_open");

    /** How far the world behind the book is taken down. Dark enough to settle, light enough to still be the world. */
    private static final int DIM_TOP = 0xB0101018;
    private static final int DIM_BOTTOM = 0xC0101018;

    private final List<ResourceKey<ResearchCategory>> categories = new ArrayList<>();
    private ResourceKey<ResearchCategory> openCategory;

    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    // the page within the book's border, which everything in the book is laid out against
    private int pageX;
    private int pageY;
    private int pageW;
    private int pageH;
    /** How far the book is from the size its picture is drawn at. */
    private float scale;
    /** Whatever the mouse is over this frame, remembered so its note can be drawn after the frame. */
    private ResearchEntry hovered;
    private ResourceLocation hoveredId;
    private float scrollX;
    private float scrollY;
    private boolean dragging;

    public AlchemonomiconScreen() {
        super(Component.translatable("item.alchemia.alchemonomicon"));
    }

    @Override
    protected void init() {
        // the book keeps its shape, and takes as much of the window as it can, but never grows past the sheet it is
        // drawn on: stretched past that the page turns soft and the writing on it stops reading as writing
        panelW = Math.min(width - 2 * gutter(), BOOK_W);
        panelH = panelW * BOOK_H / BOOK_W;
        if (panelH > height - 16) {
            panelH = height - 16;
            panelW = panelH * BOOK_W / BOOK_H;
        }
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;

        // the frame is part of the picture, so the window shrinks with it rather than sitting a fixed few pixels in
        scale = (float) panelW / BOOK_W;
        pageX = panelX + Math.round(HOLE_X * scale);
        pageY = panelY + Math.round(HOLE_Y * scale);
        pageW = Math.round(HOLE_W * scale);
        pageH = Math.round(HOLE_H * scale);

        categories.clear();
        Registry<ResearchCategory> registry = categories();
        registry.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> entry.getValue().sortOrder()))
                .forEach(entry -> categories.add(entry.getKey()));
        if (openCategory == null && !categories.isEmpty()) {
            openCategory = categories.getFirst();
        }
    }

    private Registry<ResearchCategory> categories() {
        return minecraft.level.registryAccess().registryOrThrow(ModResearch.CATEGORY_KEY);
    }

    private Registry<ResearchEntry> entries() {
        return minecraft.level.registryAccess().registryOrThrow(ModResearch.ENTRY_KEY);
    }

    /**
     * Vanilla puts the world through a blur pass and lays a heavy panel over it before a screen draws. That leaves the
     * page washed out and hard to read, so the world is only dimmed here, the way an open book shades what is past it.
     */
    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillGradient(0, 0, width, height, DIM_TOP, DIM_BOTTOM);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Screen.render lays the background down itself, so it goes first and everything else piles on top of it.
        // Calling renderBackground here as well would paint the dimming straight back over the open page.
        super.render(graphics, mouseX, mouseY, partialTick);

        drawTabs(graphics);
        drawTree(graphics, mouseX, mouseY);
        graphics.blit(FRAME, panelX, panelY, panelW, panelH, 0, 0, BOOK_W, BOOK_H, 512, 512);

        // tooltips come last of all, or the frame would be laid over them
        nodeTooltip(graphics, mouseX, mouseY);
        tabTooltip(graphics, mouseX, mouseY);
    }

    /** Room a tab needs beside the book, which the page must leave free. */
    private static int gutter() {
        return TAB - TAB_TUCK + 10;
    }

    /** Tabs are split down the middle: the first half run down the left edge, the rest down the right. */
    private int leftCount() {
        return (categories.size() + 1) / 2;
    }

    private boolean onLeft(int index) {
        return index < leftCount();
    }

    /**
     * The open tab stands a little further out than the rest, the way a bookmark you are holding does.
     */
    private int tabX(int index) {
        boolean open = categories.get(index).equals(openCategory);
        int out = open ? 2 : 0;
        return onLeft(index)
                ? panelX - TAB + TAB_TUCK - out
                : panelX + panelW - TAB_TUCK + out;
    }

    private int tabY(int index) {
        int row = onLeft(index) ? index : index - leftCount();
        return panelY + Math.round(TAB_TOP * scale) + row * (TAB + TAB_GAP);
    }

    private void drawTabs(GuiGraphics graphics) {
        for (int index = 0; index < categories.size(); index++) {
            ResourceKey<ResearchCategory> key = categories.get(index);
            int x = tabX(index);
            int y = tabY(index);

            drawTabPlate(graphics, x, y, onLeft(index), key.equals(openCategory));
            drawCategoryIcon(graphics, key, x, y);
        }
    }

    /** The plate is a square larger than the icon it carries, drawn a pixel out so the icon lands in the middle. */
    private void drawTabPlate(GuiGraphics graphics, int x, int y, boolean left, boolean open) {
        ResourceLocation plate = left
                ? (open ? TAB_LEFT_OPEN : TAB_LEFT)
                : (open ? TAB_RIGHT_OPEN : TAB_RIGHT);
        graphics.blitSprite(plate, x - 1, y - 1, PLATE, PLATE);
    }

    private void tabTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        for (int index = 0; index < categories.size(); index++) {
            if (inside(mouseX, mouseY, tabX(index), tabY(index), TAB, TAB)) {
                graphics.renderTooltip(font, ResearchCategory.displayName(categories.get(index)), mouseX, mouseY);
                return;
            }
        }
    }

    /** A category's icon is an item for now; a drawn tab picture can take its place without touching this screen. */
    private void drawCategoryIcon(GuiGraphics graphics, ResourceKey<ResearchCategory> key, int x, int y) {
        ResourceLocation icon = categories().get(key).icon();
        ItemStack stack = new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(icon));
        graphics.renderItem(stack, x + 4, y + 4);
    }

    private void drawTree(GuiGraphics graphics, int mouseX, int mouseY) {
        Registry<ResearchEntry> entries = entries();
        PlayerKnowledge knowledge = PlayerKnowledge.of(minecraft.player);
        int centreX = pageX + pageW / 2 + (int) scrollX;
        int centreY = pageY + pageH / 2 + (int) scrollY;

        graphics.enableScissor(pageX, pageY, pageX + pageW, pageY + pageH);
        drawSky(graphics);

        // lines first, so the plates sit on top of them
        for (Map.Entry<ResourceKey<ResearchEntry>, ResearchEntry> entry : entries.entrySet()) {
            ResearchEntry research = entry.getValue();
            if (!research.category().equals(openCategory)) {
                continue;
            }
            for (ResourceLocation parentId : research.parents()) {
                ResearchEntry parent = entries.get(parentId);
                if (parent != null && parent.category().equals(openCategory)) {
                    drawLink(graphics, centreX, centreY, parent, research);
                }
            }
        }

        hovered = null;
        hoveredId = null;
        boolean overPage = inside(mouseX, mouseY, pageX, pageY, pageW, pageH);
        for (Map.Entry<ResourceKey<ResearchEntry>, ResearchEntry> entry : entries.entrySet()) {
            ResearchEntry research = entry.getValue();
            if (!research.category().equals(openCategory)) {
                continue;
            }
            int x = centreX + research.column() * STEP - PLATE / 2;
            int y = centreY + research.row() * STEP - PLATE / 2;

            boolean known = knowledge.hasResearch(entry.getKey().location());
            graphics.blitSprite(research.shape().sprite(known), x, y, PLATE, PLATE);
            graphics.renderItem(research.iconStack(), x + ICON_INSET, y + ICON_INSET);

            // a node half under the boards is only half there, so the mouse has to be over the window too
            if (overPage && inside(mouseX, mouseY, x, y, PLATE, PLATE)) {
                hovered = research;
                hoveredId = entry.getKey().location();
            }
        }
        graphics.disableScissor();
    }

    /** Held back until the frame is down, or the note would be tucked under the boards. */
    private void nodeTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (hovered != null) {
            graphics.renderComponentTooltip(font,
                    describe(hovered, hoveredId, PlayerKnowledge.of(minecraft.player)), mouseX, mouseY);
        }
    }

    /**
     * The page is a window rather than a sheet of paper: a branch's own sky behind, a shared layer of stars in front
     * of it, and the two moving at different speeds as the tree is dragged about.
     */
    private void drawSky(GuiGraphics graphics) {
        RenderSystem.enableBlend();
        ResearchCategory open = openCategory == null ? null : categories().get(openCategory);
        if (open != null) {
            drawLayer(graphics, open.background(), BACK_DRIFT);
        }
        drawLayer(graphics, OVERLAY, OVER_DRIFT);
    }

    /**
     * One sky layer, drawn large enough that dragging the tree as far as it will go never pulls an edge into view, so
     * there is no need for the picture to tile.
     */
    private void drawLayer(GuiGraphics graphics, ResourceLocation texture, float drift) {
        int span = Math.max(pageW, pageH) + Math.round(2 * PAN_LIMIT * drift);
        int x = pageX + pageW / 2 - span / 2 + Math.round(scrollX * drift);
        int y = pageY + pageH / 2 - span / 2 + Math.round(scrollY * drift);
        graphics.blit(texture, x, y, span, span, 0, 0, SKY, SKY, SKY, SKY);
    }

    private void drawLink(GuiGraphics graphics, int centreX, int centreY, ResearchEntry from, ResearchEntry to) {
        int x1 = centreX + from.column() * STEP;
        int y1 = centreY + from.row() * STEP;
        int x2 = centreX + to.column() * STEP;
        int y2 = centreY + to.row() * STEP;
        int colour = 0xBBD8C8A8;

        // an elbow rather than a diagonal, which keeps the tree looking drawn rather than plotted
        graphics.fill(Math.min(x1, x2), y1 - 1, Math.max(x1, x2), y1 + 1, colour);
        graphics.fill(x2 - 1, Math.min(y1, y2), x2 + 1, Math.max(y1, y2), colour);
    }

    private List<Component> describe(ResearchEntry research, ResourceLocation id, PlayerKnowledge knowledge) {
        List<Component> lines = new ArrayList<>();
        lines.add(ResearchEntry.displayName(id));

        if (knowledge.hasResearch(id)) {
            lines.add(Component.translatable("research.alchemia.known").withStyle(ChatFormatting.GREEN));
        } else if (!research.isAvailableTo(knowledge::hasResearch)) {
            lines.add(Component.translatable("research.alchemia.locked").withStyle(ChatFormatting.DARK_GRAY));
        } else {
            research.missingFor(knowledge::knows).ifPresentOrElse(
                    missing -> lines.add(Component.translatable("research.alchemia.needs", names(missing))
                            .withStyle(ChatFormatting.DARK_PURPLE)),
                    () -> lines.add(Component.translatable("research.alchemia.ready").withStyle(ChatFormatting.AQUA)));
        }
        return lines;
    }

    private Component names(AspectList aspects) {
        Component joined = Component.empty();
        List<Holder<Aspect>> order = aspects.sortedByName();
        for (int index = 0; index < order.size(); index++) {
            if (index > 0) {
                joined = Component.empty().append(joined).append(", ");
            }
            joined = Component.empty().append(joined).append(order.get(index).value().displayName());
        }
        return joined;
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (int index = 0; index < categories.size(); index++) {
            if (inside((int) mouseX, (int) mouseY, tabX(index), tabY(index), TAB, TAB)) {
                openCategory = categories.get(index);
                scrollX = 0;
                scrollY = 0;
                return true;
            }
        }
        dragging = true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging) {
            // the tree can be pulled about, but not so far that it leaves the page behind
            scrollX = Mth.clamp((float) (scrollX + dragX), -PAN_LIMIT, PAN_LIMIT);
            scrollY = Mth.clamp((float) (scrollY + dragY), -PAN_LIMIT, PAN_LIMIT);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
