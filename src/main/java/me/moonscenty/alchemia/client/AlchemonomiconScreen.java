package me.moonscenty.alchemia.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.network.RequestNote;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.research.ModResearch;
import me.moonscenty.alchemia.research.NodeShape;
import me.moonscenty.alchemia.research.NoteRequests;
import me.moonscenty.alchemia.research.ResearchCategory;
import me.moonscenty.alchemia.research.ResearchEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * The alchemonomicon, laid out as the original laid it out: the whole window is the page, framed in wood, with a
 * branch's research spread over its sky on a grid of twenty-four, the tabs down the left edge, and a wheel that
 * draws the tree back to see more of it.
 * <p>
 * The places, the sizes, which plate a node sits on, how bright it is, and how the lines between nodes are pieced
 * together out of straight runs and turns are all the original's, read out of its screen. The pictures are the
 * original's when its jar is there and ours, painted to the same sheet layout, when it is not.
 */
public class AlchemonomiconScreen extends Screen {
    /** Frame pieces, node plates, arrow heads and line pieces, all on one sheet. */
    private static final ResourceLocation SHEET = Alchemia.id("textures/gui/research_browser.png");
    private static final ResourceLocation OVERLAY = Alchemia.id("textures/gui/research_overlay.png");
    private static final int SHEET_SIZE = 256;

    /** The page leaves this much of the window to its frame on every side. */
    private static final int MARGIN = 16;
    /** One step of the research grid, and the size of the plate a node sits on. */
    private static final int GRID = 24;
    private static final int PLATE = 32;
    private static final int ICON = 16;
    /** The skies are a quarter of their size per repeat, as the original drew them, and move at their own pace. */
    private static final int SKY_REPEAT = 256;
    private static final double BACK_DRIFT = 2.0;
    private static final double OVER_DRIFT = 1.5;
    /** How far the tree can be drawn back, and by how much a turn of the wheel does it. */
    private static final float ZOOM_MIN = 1F;
    private static final float ZOOM_MAX = 2F;
    private static final float ZOOM_STEP = 0.25F;

    // the frame: a corner, and the runs between corners, which are laid down a piece at a time
    private static final int FRAME_CORNER = 22;
    private static final int FRAME_RUN = 64;
    private static final int FRAME_FROM = 13;
    private static final int FRAME_RUN_FROM = 48;

    // the plates, a row for the outstanding and a row for the hidden
    private static final int PLATE_SQUARE = 80;
    private static final int PLATE_HEX = 112;
    private static final int PLATE_ROUND = 144;
    private static final int PLATE_BRACKETS = 176;
    private static final int PLATE_ROW = 48;

    // the tabs, down the left edge, each on a frame corner
    private static final int TAB_X = 1;
    private static final int TAB_Y = 10;
    private static final int TAB_STEP = 24;

    private final List<ResourceKey<ResearchCategory>> categories = new ArrayList<>();
    private ResourceKey<ResearchCategory> openCategory;

    /** The middle of the view, in the tree's own pixels, and how far the tree is drawn back. */
    private double viewX;
    private double viewY;
    private float zoom = ZOOM_MIN;
    private boolean dragging;

    /** Whatever the mouse is over this frame, remembered so its note can be drawn after the frame. */
    private ResearchEntry hovered;
    private ResourceLocation hoveredId;

    public AlchemonomiconScreen() {
        super(Component.translatable("item.alchemia.alchemonomicon"));
    }

    @Override
    protected void init() {
        categories.clear();
        categoryRegistry().entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> entry.getValue().sortOrder()))
                .forEach(entry -> categories.add(entry.getKey()));
        if (openCategory == null && !categories.isEmpty()) {
            openCategory = categories.getFirst();
        }
    }

    private Registry<ResearchCategory> categoryRegistry() {
        return minecraft.level.registryAccess().registryOrThrow(ModResearch.CATEGORY_KEY);
    }

    private Registry<ResearchEntry> entries() {
        return minecraft.level.registryAccess().registryOrThrow(ModResearch.ENTRY_KEY);
    }

    // --- the page in window coordinates

    private int pageLeft() {
        return MARGIN;
    }

    private int pageTop() {
        return MARGIN;
    }

    private int pageWidth() {
        return width - 2 * MARGIN;
    }

    private int pageHeight() {
        return height - 2 * MARGIN;
    }

    /** Where a point of the tree lands in the window. */
    private double toWindowX(double treeX) {
        return pageLeft() + pageWidth() / 2.0 + (treeX - viewX) / zoom;
    }

    private double toWindowY(double treeY) {
        return pageTop() + pageHeight() / 2.0 + (treeY - viewY) / zoom;
    }

    private double toTreeX(double windowX) {
        return viewX + (windowX - pageLeft() - pageWidth() / 2.0) * zoom;
    }

    private double toTreeY(double windowY) {
        return viewY + (windowY - pageTop() - pageHeight() / 2.0) * zoom;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        drawPage(graphics, mouseX, mouseY);
        drawFrame(graphics);
        drawTabs(graphics);

        // notes come last of all, or the frame would be laid over them
        if (hovered != null) {
            graphics.renderComponentTooltip(font,
                    describe(hovered, hoveredId, PlayerKnowledge.of(minecraft.player)), mouseX, mouseY);
        } else {
            tabTooltip(graphics, mouseX, mouseY);
        }
    }

    // --- the page

    private void drawPage(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.enableScissor(pageLeft(), pageTop(), pageLeft() + pageWidth(), pageTop() + pageHeight());
        drawSky(graphics);

        graphics.pose().pushPose();
        // from here on everything is in the tree's own pixels, drawn back by the zoom
        graphics.pose().translate(toWindowX(0), toWindowY(0), 0);
        graphics.pose().scale(1F / zoom, 1F / zoom, 1F);

        Registry<ResearchEntry> entries = entries();
        PlayerKnowledge knowledge = PlayerKnowledge.of(minecraft.player);
        for (Map.Entry<ResourceKey<ResearchEntry>, ResearchEntry> entry : entries.entrySet()) {
            ResearchEntry research = entry.getValue();
            if (!research.category().equals(openCategory)) {
                continue;
            }
            float[] colour = lineColour(research, entry.getKey().location(), knowledge);
            for (ResourceLocation parentId : research.parents()) {
                ResearchEntry parent = entries.get(parentId);
                if (parent != null && parent.category().equals(openCategory)) {
                    drawLine(graphics, research.column(), research.row(), parent.column(), parent.row(), colour);
                }
            }
        }

        hovered = null;
        hoveredId = null;
        boolean overPage = mouseX >= pageLeft() && mouseX < pageLeft() + pageWidth()
                && mouseY >= pageTop() && mouseY < pageTop() + pageHeight();
        double treeMouseX = toTreeX(mouseX);
        double treeMouseY = toTreeY(mouseY);
        for (Map.Entry<ResourceKey<ResearchEntry>, ResearchEntry> entry : entries.entrySet()) {
            ResearchEntry research = entry.getValue();
            if (!research.category().equals(openCategory)) {
                continue;
            }
            ResourceLocation id = entry.getKey().location();
            int x = research.column() * GRID;
            int y = research.row() * GRID;
            drawNode(graphics, research, id, knowledge, x, y);
            if (overPage && treeMouseX >= x - 3 && treeMouseX < x + ICON + 3
                    && treeMouseY >= y - 3 && treeMouseY < y + ICON + 3) {
                hovered = research;
                hoveredId = id;
            }
        }
        graphics.pose().popPose();
        graphics.disableScissor();
    }

    /**
     * A branch's sky behind, and the shared field of stars in front, each repeating every quarter of its picture and
     * each sliding at its own pace as the tree is dragged, which is what gives the page its depth.
     */
    private void drawSky(GuiGraphics graphics) {
        ResearchCategory open = openCategory == null ? null : categoryRegistry().get(openCategory);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (open != null) {
            drawSkyLayer(graphics, open.background(), BACK_DRIFT);
        }
        drawSkyLayer(graphics, OVERLAY, OVER_DRIFT);
    }

    private void drawSkyLayer(GuiGraphics graphics, ResourceLocation texture, double drift) {
        int w = pageWidth();
        int h = pageHeight();
        float u = (float) ((viewX - w * zoom / 2.0) / drift);
        float v = (float) ((viewY - h * zoom / 2.0) / drift);
        // the sheet is said to be a quarter of its real size, so it repeats that often and the tree shows through it
        graphics.blit(texture, pageLeft(), pageTop(), w, h, u, v, Math.round(w * zoom), Math.round(h * zoom),
                SKY_REPEAT, SKY_REPEAT);
    }

    /**
     * How a line into a node is coloured: bright once the node is known, dim while it can be taken up, and nearly
     * gone while it is still out of reach.
     */
    private static float[] lineColour(ResearchEntry research, ResourceLocation id, PlayerKnowledge knowledge) {
        if (knowledge.hasResearch(id)) {
            return new float[] {0.6F, 0.6F, 0.7F};
        }
        if (research.isAvailableTo(knowledge::hasResearch)) {
            return new float[] {0.25F, 0.25F, 0.3F};
        }
        return new float[] {0.1F, 0.1F, 0.15F};
    }

    /**
     * A node: its plate, chosen by its shape, and the thing it is about on top.
     * <p>
     * Known research is drawn at full strength. Research that can be taken up now breathes in and out, so it is the
     * first thing the eye goes to, and research still out of reach is left dark.
     */
    private void drawNode(GuiGraphics graphics, ResearchEntry research, ResourceLocation id,
            PlayerKnowledge knowledge, int x, int y) {
        float bright;
        if (knowledge.hasResearch(id)) {
            bright = 1F;
        } else if (research.isAvailableTo(knowledge::hasResearch)) {
            double phase = (Util.getMillis() % 600L) / 600.0 * Math.PI * 2.0;
            bright = (float) (Math.sin(phase) * 0.25 + 0.75);
        } else {
            bright = 0.3F;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.setColor(bright, bright, bright, 1F);
        int from = research.shape() == NodeShape.MAJOR ? PLATE_ROUND : PLATE_SQUARE;
        graphics.blit(SHEET, x - 8, y - 8, from, PLATE_ROW, PLATE, PLATE, SHEET_SIZE, SHEET_SIZE);
        if (research.shape() == NodeShape.SPECIAL) {
            graphics.blit(SHEET, x - 8, y - 8, PLATE_BRACKETS, PLATE_ROW, PLATE, PLATE, SHEET_SIZE, SHEET_SIZE);
        }
        graphics.setColor(1F, 1F, 1F, 1F);
        graphics.renderItem(research.iconStack(), x, y);
        if (bright < 1F) {
            // an item cannot be tinted, so a node that is not yet known is shaded over instead
            int shade = Math.round((1F - bright) * 0xC0);
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 200);
            graphics.fill(x, y, x + ICON, y + ICON, shade << 24);
            graphics.pose().popPose();
        }
    }

    /**
     * The line from a node back to one it rests on, pieced together the way the original pieced it: a run down or up
     * from the node, a turn, and a run across to the other; the turn wide when both runs are long and tight when
     * either is a single step; and an arrow head at the node the line leads into.
     */
    private void drawLine(GuiGraphics graphics, int fromColumn, int fromRow, int toColumn, int toRow,
            float[] colour) {
        int across = Math.abs(fromColumn - toColumn);
        int down = Math.abs(fromRow - toRow);
        int stepX = across == 0 ? 0 : (fromColumn - toColumn > 0 ? -1 : 1);
        int stepY = down == 0 ? 0 : (fromRow - toRow > 0 ? -1 : 1);
        boolean wide = across > 1 && down > 1;
        int x = fromColumn * GRID - 4;
        int y = fromRow * GRID - 4;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.setColor(colour[0], colour[1], colour[2], 1F);

        // the head, at the edge of the node the line comes into, pointing in
        int head = stepY < 0 ? 64 : stepY > 0 ? 96 : stepX > 0 ? 160 : stepX < 0 ? 128 : -1;
        if (head >= 0) {
            piece(graphics, x - 4, y - 4, head, 112, 32);
        }

        int row = 1;
        int column = 0;
        for (; row < down - (wide ? 1 : 0); row++) {
            piece(graphics, x + stepX * GRID * column, y + stepY * GRID * row, 0, 228, GRID);
        }
        int atX = x + stepX * GRID * column;
        int atY = y + stepY * GRID * row;
        if (wide) {
            if (stepX < 0 && stepY > 0) {
                piece(graphics, atX - GRID, atY, 0, 180, 2 * GRID);
            } else if (stepX > 0 && stepY > 0) {
                piece(graphics, atX, atY, 48, 180, 2 * GRID);
            } else if (stepX < 0 && stepY < 0) {
                piece(graphics, atX - GRID, atY - GRID, 96, 180, 2 * GRID);
            } else if (stepX > 0 && stepY < 0) {
                piece(graphics, atX, atY - GRID, 144, 180, 2 * GRID);
            }
        } else if (stepX < 0 && stepY > 0) {
            piece(graphics, atX, atY, 48, 228, GRID);
        } else if (stepX > 0 && stepY > 0) {
            piece(graphics, atX, atY, 72, 228, GRID);
        } else if (stepX < 0 && stepY < 0) {
            piece(graphics, atX, atY, 96, 228, GRID);
        } else if (stepX > 0 && stepY < 0) {
            piece(graphics, atX, atY, 120, 228, GRID);
        }
        row += wide ? 1 : 0;
        for (column += wide ? 2 : 1; column < across; column++) {
            piece(graphics, x + stepX * GRID * column, y + stepY * GRID * row, 24, 228, GRID);
        }
        graphics.setColor(1F, 1F, 1F, 1F);
    }

    private static void piece(GuiGraphics graphics, int x, int y, int fromX, int fromY, int size) {
        graphics.blit(SHEET, x, y, fromX, fromY, size, size, SHEET_SIZE, SHEET_SIZE);
    }

    // --- the frame and the tabs

    /** The frame around the whole window: a corner at each corner and the runs between them a piece at a time. */
    private void drawFrame(GuiGraphics graphics) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        for (int x = 16; x < width - 16; x += FRAME_RUN) {
            int run = Math.min(FRAME_RUN, width - 16 - x);
            graphics.blit(SHEET, x, -2, FRAME_RUN_FROM, FRAME_FROM, run, FRAME_CORNER, SHEET_SIZE, SHEET_SIZE);
            graphics.blit(SHEET, x, height - 20, FRAME_RUN_FROM, FRAME_FROM, run, FRAME_CORNER,
                    SHEET_SIZE, SHEET_SIZE);
        }
        for (int y = 16; y < height - 16; y += FRAME_RUN) {
            int run = Math.min(FRAME_RUN, height - 16 - y);
            graphics.blit(SHEET, -2, y, FRAME_FROM, FRAME_RUN_FROM, FRAME_CORNER, run, SHEET_SIZE, SHEET_SIZE);
            graphics.blit(SHEET, width - 20, y, FRAME_FROM, FRAME_RUN_FROM, FRAME_CORNER, run,
                    SHEET_SIZE, SHEET_SIZE);
        }
        corner(graphics, -2, -2);
        corner(graphics, -2, height - 20);
        corner(graphics, width - 20, -2);
        corner(graphics, width - 20, height - 20);
    }

    private static void corner(GuiGraphics graphics, int x, int y) {
        graphics.blit(SHEET, x, y, FRAME_FROM, FRAME_FROM, FRAME_CORNER, FRAME_CORNER, SHEET_SIZE, SHEET_SIZE);
    }

    private int tabY(int index) {
        return TAB_Y + index * TAB_STEP;
    }

    /** Each tab is a frame corner with the branch's picture on it; the open one is drawn at full strength. */
    private void drawTabs(GuiGraphics graphics) {
        for (int index = 0; index < categories.size(); index++) {
            ResourceKey<ResearchCategory> key = categories.get(index);
            int y = tabY(index);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            corner(graphics, TAB_X - 3, y - 3);
            float strength = key.equals(openCategory) ? 1F : 0.66F;
            graphics.setColor(strength, strength, strength, key.equals(openCategory) ? 1F : 0.8F);
            graphics.blit(categoryRegistry().get(key).icon(), TAB_X, y, 0, 0, ICON, ICON, ICON, ICON);
            graphics.setColor(1F, 1F, 1F, 1F);
        }
    }

    private void tabTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        for (int index = 0; index < categories.size(); index++) {
            if (overTab(index, mouseX, mouseY)) {
                graphics.renderTooltip(font, ResearchCategory.displayName(categories.get(index)), mouseX, mouseY);
                return;
            }
        }
    }

    private boolean overTab(int index, double mouseX, double mouseY) {
        int y = tabY(index);
        return mouseX >= TAB_X && mouseX < TAB_X + ICON && mouseY >= y && mouseY < y + ICON;
    }

    // --- words

    private List<Component> describe(ResearchEntry research, ResourceLocation id, PlayerKnowledge knowledge) {
        List<Component> lines = new ArrayList<>();
        lines.add(ResearchEntry.displayName(id));

        if (knowledge.hasResearch(id)) {
            lines.add(Component.translatable("research.alchemia.known").withStyle(ChatFormatting.GREEN));
            lines.add(Component.translatable("research.alchemia.open").withStyle(ChatFormatting.DARK_GRAY));
        } else if (!research.isAvailableTo(knowledge::hasResearch)) {
            lines.add(Component.translatable("research.alchemia.locked").withStyle(ChatFormatting.DARK_GRAY));
        } else {
            research.missingFor(knowledge::knows).ifPresentOrElse(
                    missing -> lines.add(Component.translatable("research.alchemia.needs", names(missing))
                            .withStyle(ChatFormatting.DARK_PURPLE)),
                    () -> {
                        lines.add(Component.translatable("research.alchemia.ready").withStyle(ChatFormatting.AQUA));
                        lines.add(Component.translatable("research.alchemia.take_note")
                                .withStyle(ChatFormatting.DARK_GRAY));
                    });
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

    // --- input

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (int index = 0; index < categories.size(); index++) {
            if (overTab(index, mouseX, mouseY)) {
                openCategory = categories.get(index);
                viewX = 0;
                viewY = 0;
                return true;
            }
        }
        if (hovered != null) {
            PlayerKnowledge knowledge = PlayerKnowledge.of(minecraft.player);
            if (knowledge.hasResearch(hoveredId)) {
                minecraft.setScreen(new ResearchPageScreen(this, hoveredId, hovered));
                return true;
            }
            if (NoteRequests.isReady(knowledge, hoveredId, hovered)) {
                // the server writes the note, since it is the one holding the paper and the ink
                PacketDistributor.sendToServer(new RequestNote(hoveredId));
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
            viewX -= dragX * zoom;
            viewY -= dragY * zoom;
            keepInBounds();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    /** The wheel draws the tree back or brings it near, by quarter steps between life size and half of it. */
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY != 0) {
            zoom = Mth.clamp(zoom + (scrollY < 0 ? ZOOM_STEP : -ZOOM_STEP), ZOOM_MIN, ZOOM_MAX);
            keepInBounds();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    /** The view cannot be dragged further than the branch's research reaches, so the tree is never lost. */
    private void keepInBounds() {
        int left = 0;
        int right = 0;
        int top = 0;
        int bottom = 0;
        for (ResearchEntry research : entries()) {
            if (research.category().equals(openCategory)) {
                left = Math.min(left, research.column() * GRID);
                right = Math.max(right, research.column() * GRID);
                top = Math.min(top, research.row() * GRID);
                bottom = Math.max(bottom, research.row() * GRID);
            }
        }
        viewX = Mth.clamp(viewX, left, right);
        viewY = Mth.clamp(viewY, top, bottom);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
