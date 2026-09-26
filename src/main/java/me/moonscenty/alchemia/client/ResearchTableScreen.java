package me.moonscenty.alchemia.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.menu.ResearchTableMenu;
import me.moonscenty.alchemia.network.MixAspects;
import me.moonscenty.alchemia.network.PlaceAspect;
import me.moonscenty.alchemia.research.HexGrid;
import me.moonscenty.alchemia.research.NoteSolving;
import me.moonscenty.alchemia.research.ResearchEntry;
import me.moonscenty.alchemia.research.ResearchNote;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * The desk as the reader sees it.
 * <p>
 * The board is laid out on the leather, what the sheet still holds is racked down the left, and the two dishes at the
 * foot of the panel are where two of those are mixed into the one thing they make between them.
 */
public class ResearchTableScreen extends AbstractContainerScreen<ResearchTableMenu> {
    private static final ResourceLocation PANEL = Alchemia.id("textures/gui/research_table.png");
    /**
     * Cell plates exist at these widths and are only ever drawn at one of them.
     * <p>
     * The screen picks the largest that fits rather than scaling one to taste: a sprite stretched by some fraction is
     * resampled by nearest neighbour, which leaves its outline a pixel thick in places and two in others.
     */
    private static final int[] CELL_SIZES = {32, 24, 20, 16};
    /**
     * How far apart cells of each size sit: across, then down.
     * <p>
     * Whole numbers rather than the irrational height of a regular hexagon, and the second of each pair even, since
     * odd columns sit half a pitch down. Cells then land on exactly the pixels their plates were drawn for, and the
     * edge two neighbours share is the same pixels twice over instead of two lines a hair apart.
     * <p>
     * These are measured off MoonScenty's drawing by thaumref/tools/apply_user_hex.py, not worked out from a regular
     * hexagon, since the drawing is not quite one; redraw the plate and run that again. The measurement has a couple
     * of pixels of daylight added, so the board reads as separate cells rather than one mesh.
     */
    private static final int[][] CELL_PITCH = {{22, 28}, {17, 22}, {14, 18}, {12, 16}};

    private static final int SHEET = 256;
    private static final int PANEL_W = 256;
    private static final int PANEL_H = 231;

    /** The leather the board is laid out on. */
    private static final int BOARD_X = 85;
    private static final int BOARD_Y = 7;
    private static final int BOARD_W = 164;
    private static final int BOARD_H = 138;
    /** Kept clear of the gilding around the leather, so a cell never sits on the frame. */
    private static final int BOARD_PAD = 4;

    /** The recess down the left of the panel, where what the sheet holds is racked up, a page at a time. */
    private static final int POOL_X = 7;
    private static final int POOL_Y = 31;
    private static final int POOL_COLUMNS = 4;
    private static final int POOL_ROWS = 4;
    private static final int POOL_STEP = 18;
    private static final int POOL_PAGE = POOL_COLUMNS * POOL_ROWS;

    // The two dishes, and the button between them that mixes what is in them.
    private static final int DISH_A_X = 7;
    private static final int DISH_B_X = 61;
    private static final int DISH_Y = 129;
    private static final int MIX_X = 32;
    private static final int MIX_Y = 132;
    private static final int MIX_W = 20;
    private static final int MIX_H = 10;
    private static final int MIX_FROM_X = 48;
    private static final int MIX_FROM_Y = 246;

    // The two page buttons under the rack. Each is painted faintly into the panel, and the lit sprite goes over it
    // only while there is a page that way to turn to.
    private static final int TURN = 20;
    private static final int PREV_X = 6;
    private static final int NEXT_X = 58;
    private static final int TURN_Y = 105;
    private static final int PREV_FROM_X = 0;
    private static final int NEXT_FROM_X = 24;
    private static final int TURN_FROM_Y = 236;

    private static final int ICON = 16;

    /** What the reader has picked up and is about to write down. */
    private Holder<Aspect> held;
    private Holder<Aspect> dishA;
    private Holder<Aspect> dishB;
    /** Which page of the rack is showing. Clamped every time it is read, since the sheet's stock grows as it mixes. */
    private int page;

    public ResearchTableScreen(ResearchTableMenu menu, Inventory inventory, Component title) {
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
        drawBoard(graphics, mouseX, mouseY);
        drawPool(graphics);
        drawButtons(graphics, mouseX, mouseY);
        drawDishes(graphics);
        renderTooltip(graphics, mouseX, mouseY);
    }

    /** The panel is already labelled in its own painting, so no words are put over it. */
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    // --- the board ---------------------------------------------------------

    /**
     * Where a board sits and how big its cells are drawn.
     * <p>
     * Measured from the cells the note actually has rather than from the ring it was cut out of: a note has pieces
     * torn out of it, so what is left is rarely centred on the middle of that ring. Sizing and centring on the ring
     * would push a lopsided board off one edge of the leather and leave a gap at the other.
     *
     * @param size how wide one cell is drawn
     * @param originX where the cell at column zero of the board's own reckoning lands
     */
    private record Layout(int size, int across, int down, int originX, int originY) {
        int x(HexGrid.Hex at) {
            return originX + across * at.q() - size / 2;
        }

        int y(HexGrid.Hex at) {
            // down is even, so a column offset by half a pitch still lands on a whole pixel
            return originY + (down * (2 * at.r() + at.q())) / 2 - size / 2;
        }
    }

    private Layout layout(ResearchNote note) {
        int lowQ = Integer.MAX_VALUE;
        int highQ = Integer.MIN_VALUE;
        int lowS = Integer.MAX_VALUE;
        int highS = Integer.MIN_VALUE;
        for (ResearchNote.Cell cell : note.cells()) {
            int q = cell.at().q();
            int s = 2 * cell.at().r() + q;      // twice the row, so half steps stay whole
            lowQ = Math.min(lowQ, q);
            highQ = Math.max(highQ, q);
            lowS = Math.min(lowS, s);
            highS = Math.max(highS, s);
        }

        // a cell reaches half its width either side of its middle, and half its height above and below
        int across = BOARD_W - 2 * BOARD_PAD;
        int down = BOARD_H - 2 * BOARD_PAD;
        int chosen = CELL_SIZES.length - 1;
        for (int index = 0; index < CELL_SIZES.length; index++) {
            int wide = CELL_PITCH[index][0] * (highQ - lowQ) + CELL_SIZES[index];
            int tall = CELL_PITCH[index][1] * (highS - lowS) / 2 + CELL_PITCH[index][1];
            if (wide <= across && tall <= down) {
                chosen = index;
                break;
            }
        }

        int size = CELL_SIZES[chosen];
        int pitchX = CELL_PITCH[chosen][0];
        int pitchY = CELL_PITCH[chosen][1];
        int middleX = leftPos + BOARD_X + BOARD_W / 2;
        int middleY = topPos + BOARD_Y + BOARD_H / 2;
        return new Layout(size, pitchX, pitchY,
                middleX - pitchX * (lowQ + highQ) / 2,
                middleY - pitchY * (lowS + highS) / 4);
    }

    private void drawBoard(GuiGraphics graphics, int mouseX, int mouseY) {
        ResearchNote note = menu.note();
        if (note == null) {
            return;
        }
        Layout layout = layout(note);
        HexGrid.Hex over = cellUnder(mouseX, mouseY);

        // clipped to the leather, so nothing can ever creep onto the boards around it
        graphics.enableScissor(leftPos + BOARD_X, topPos + BOARD_Y,
                leftPos + BOARD_X + BOARD_W, topPos + BOARD_Y + BOARD_H);
        for (ResearchNote.Cell cell : note.cells()) {
            int x = layout.x(cell.at());
            int y = layout.y(cell.at());
            int size = layout.size();

            String kind = cell.pinned() ? "pinned"
                    : cell.at().equals(over) && !note.complete() ? "lit" : "empty";
            graphics.blitSprite(Alchemia.id("research/hex_" + kind + "_" + size), x, y, size, size);
            cell.aspect().ifPresent(aspect ->
                    drawAspect(graphics, aspect, x + (size - ICON) / 2, y + (size - ICON) / 2));
        }
        graphics.disableScissor();
    }

    /** The cell the pointer is over, if any. */
    private HexGrid.Hex cellUnder(int mouseX, int mouseY) {
        ResearchNote note = menu.note();
        if (note == null) {
            return null;
        }
        Layout layout = layout(note);
        int size = layout.size();
        HexGrid.Hex closest = null;
        double best = Double.MAX_VALUE;

        for (ResearchNote.Cell cell : note.cells()) {
            double dx = mouseX - (layout.x(cell.at()) + size / 2.0);
            double dy = mouseY - (layout.y(cell.at()) + size / 2.0);
            double away = dx * dx + dy * dy;
            if (away < best) {
                best = away;
                closest = cell.at();
            }
        }
        double reach = layout.down() / 2.0;
        return best <= reach * reach ? closest : null;
    }

    // --- what the sheet still holds ----------------------------------------

    /**
     * The sheet's own stock, in a steady order. There is no filtering by what the reader has met: the sheet starts
     * with the primals and only grows by mixing, so everything in here came from the sheet itself.
     */
    private List<Holder<Aspect>> pool() {
        ResearchNote note = menu.note();
        if (note == null) {
            return List.of();
        }
        List<Holder<Aspect>> stock = new ArrayList<>();
        for (Holder<Aspect> aspect : note.budget().sortedByName()) {
            if (note.budget().get(aspect) > 0) {
                stock.add(aspect);
            }
        }
        return stock;
    }

    /** How many pages the sheet's stock fills, never fewer than one. */
    private int pages() {
        return Math.max(1, (pool().size() + POOL_PAGE - 1) / POOL_PAGE);
    }

    /**
     * The page being shown, brought back within range first.
     * <p>
     * Mixing takes two aspects off the sheet and puts one back, so the stock can shrink out from under a page that
     * was turned to, and the last page can stop existing while the reader is standing on it.
     */
    private int page() {
        page = Math.max(0, Math.min(page, pages() - 1));
        return page;
    }

    /** What is on the page being shown. */
    private List<Holder<Aspect>> shown() {
        List<Holder<Aspect>> stock = pool();
        int from = page() * POOL_PAGE;
        return stock.subList(Math.min(from, stock.size()), Math.min(from + POOL_PAGE, stock.size()));
    }

    /** Where the nth aspect of the page being shown is drawn. */
    private int poolX(int place) {
        return leftPos + POOL_X + (place % POOL_COLUMNS) * POOL_STEP;
    }

    private int poolY(int place) {
        return topPos + POOL_Y + (place / POOL_COLUMNS) * POOL_STEP;
    }

    private void drawPool(GuiGraphics graphics) {
        ResearchNote note = menu.note();
        if (note == null || note.complete()) {
            return;
        }
        List<Holder<Aspect>> shown = shown();
        for (int place = 0; place < shown.size(); place++) {
            Holder<Aspect> aspect = shown.get(place);
            int x = poolX(place);
            int y = poolY(place);

            if (aspect.equals(held)) {
                graphics.fill(x - 1, y - 1, x + ICON + 1, y + ICON + 1, 0x80FFD37A);
            }
            drawAspect(graphics, aspect, x, y);
            graphics.drawString(font, String.valueOf(note.budget().get(aspect)),
                    x + ICON - 6, y + ICON - 6, 0xFFFFE9C0, true);
        }
    }

    // --- the three buttons -------------------------------------------------

    /**
     * The buttons that can be pressed, drawn over the faint shapes the panel already carries.
     * <p>
     * A button that cannot do anything is simply left as that faint shape rather than being greyed out, which is how
     * the panel was painted: the recess is the off state and the sprite is the on one.
     */
    private void drawButtons(GuiGraphics graphics, int mouseX, int mouseY) {
        ResearchNote note = menu.note();
        if (note == null || note.complete()) {
            return;
        }
        button(graphics, mouseX, mouseY, PREV_X, TURN_Y, TURN, TURN, PREV_FROM_X, TURN_FROM_Y, page() > 0);
        button(graphics, mouseX, mouseY, NEXT_X, TURN_Y, TURN, TURN, NEXT_FROM_X, TURN_FROM_Y, page() < pages() - 1);
        button(graphics, mouseX, mouseY, MIX_X, MIX_Y, MIX_W, MIX_H, MIX_FROM_X, MIX_FROM_Y, mixResult().isPresent());
    }

    private void button(GuiGraphics graphics, int mouseX, int mouseY,
                        int x, int y, int wide, int tall, int fromX, int fromY, boolean live) {
        if (!live) {
            return;
        }
        graphics.blit(PANEL, leftPos + x, topPos + y, fromX, fromY, wide, tall, SHEET, SHEET);
        if (within(mouseX, mouseY, x, y, wide, tall)) {
            graphics.fill(leftPos + x, topPos + y, leftPos + x + wide, topPos + y + tall, 0x30FFFFFF);
        }
    }

    /** Whether the pointer is over a box given in panel coordinates. */
    private boolean within(double mouseX, double mouseY, int x, int y, int wide, int tall) {
        return mouseX >= leftPos + x && mouseX < leftPos + x + wide
                && mouseY >= topPos + y && mouseY < topPos + y + tall;
    }

    private void drawDishes(GuiGraphics graphics) {
        ResearchNote note = menu.note();
        if (note == null || note.complete()) {
            return;
        }
        if (dishA != null) {
            drawAspect(graphics, dishA, leftPos + DISH_A_X, topPos + DISH_Y);
        }
        if (dishB != null) {
            drawAspect(graphics, dishB, leftPos + DISH_B_X, topPos + DISH_Y);
        }
    }

    /** What the two dishes would make, if anything. */
    private Optional<Holder<Aspect>> mixResult() {
        if (dishA == null || dishB == null || minecraft == null || minecraft.level == null) {
            return Optional.empty();
        }
        return NoteSolving.mixOf(
                minecraft.level.registryAccess().registryOrThrow(me.moonscenty.alchemia.registry.ModAspects.KEY),
                dishA, dishB);
    }

    private void drawAspect(GuiGraphics graphics, Holder<Aspect> aspect, int x, int y) {
        int colour = aspect.value().color();
        graphics.setColor(((colour >> 16) & 0xFF) / 255F, ((colour >> 8) & 0xFF) / 255F, (colour & 0xFF) / 255F, 1F);
        graphics.blit(aspect.value().icon(), x, y, 0, 0, ICON, ICON, ICON, ICON);
        graphics.setColor(1F, 1F, 1F, 1F);
    }

    private static boolean over(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX < x + ICON && mouseY >= y && mouseY < y + ICON;
    }

    // --- clicking ----------------------------------------------------------

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        ResearchNote note = menu.note();
        if (note == null || note.complete()) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        if (within(mouseX, mouseY, PREV_X, TURN_Y, TURN, TURN) && page() > 0) {
            page--;
            click();
            return true;
        }
        if (within(mouseX, mouseY, NEXT_X, TURN_Y, TURN, TURN) && page() < pages() - 1) {
            page++;
            click();
            return true;
        }

        List<Holder<Aspect>> shown = shown();
        for (int place = 0; place < shown.size(); place++) {
            if (over(mouseX, mouseY, poolX(place), poolY(place))) {
                held = shown.get(place).equals(held) ? null : shown.get(place);
                click();
                return true;
            }
        }

        // the dishes take whatever has been picked up, and give it back when clicked empty-handed
        if (over(mouseX, mouseY, leftPos + DISH_A_X, topPos + DISH_Y)) {
            dishA = held;
            click();
            return true;
        }
        if (over(mouseX, mouseY, leftPos + DISH_B_X, topPos + DISH_Y)) {
            dishB = held;
            click();
            return true;
        }
        if (within(mouseX, mouseY, MIX_X, MIX_Y, MIX_W, MIX_H)) {
            mixResult().ifPresent(result -> {
                PacketDistributor.sendToServer(new MixAspects(id(dishA), id(dishB)));
                dishA = null;
                dishB = null;
                click();
            });
            return true;
        }

        HexGrid.Hex at = cellUnder((int) mouseX, (int) mouseY);
        if (at != null) {
            // the right button rubs a space out; the left writes whatever has been picked up
            if (button == 1) {
                PacketDistributor.sendToServer(new PlaceAspect(at, Optional.empty()));
                click();
            } else if (held != null) {
                PacketDistributor.sendToServer(new PlaceAspect(at, Optional.of(id(held))));
                click();
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private static ResourceLocation id(Holder<Aspect> aspect) {
        return aspect.value().id();
    }

    private void click() {
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.playSound(SoundEvents.BOOK_PAGE_TURN, 0.6F, 1.1F);
        }
    }

    // --- notes to the reader -----------------------------------------------

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        ResearchNote note = menu.note();
        if (note != null) {
            HexGrid.Hex at = cellUnder(mouseX, mouseY);
            if (at != null) {
                List<Component> lines = describeCell(note, at);
                if (!lines.isEmpty()) {
                    graphics.renderComponentTooltip(font, lines, mouseX, mouseY);
                    return;
                }
            }

            List<Holder<Aspect>> shown = shown();
            for (int place = 0; place < shown.size(); place++) {
                if (over(mouseX, mouseY, poolX(place), poolY(place))) {
                    graphics.renderTooltip(font, shown.get(place).value().displayName(), mouseX, mouseY);
                    return;
                }
            }
            // what the two dishes would make between them, read off the button that would make it
            if (within(mouseX, mouseY, MIX_X, MIX_Y, MIX_W, MIX_H)) {
                graphics.renderTooltip(font, mixResult()
                        .map(result -> result.value().displayName())
                        .orElse(Component.translatable("note.alchemia.no_such_mix")), mouseX, mouseY);
                return;
            }
        }
        super.renderTooltip(graphics, mouseX, mouseY);
    }

    private List<Component> describeCell(ResearchNote note, HexGrid.Hex at) {
        List<Component> lines = new ArrayList<>();
        ResearchNote.Cell cell = note.cellAt(at).orElse(null);
        if (cell == null) {
            return lines;
        }

        cell.aspect().ifPresent(aspect -> lines.add(aspect.value().displayName()));
        if (cell.pinned()) {
            lines.add(Component.translatable("note.alchemia.pinned").withStyle(ChatFormatting.GOLD));
        } else if (cell.isEmpty() && held != null) {
            boolean holds = at.neighbours().stream()
                    .map(next -> note.cellAt(next).flatMap(ResearchNote.Cell::aspect).orElse(null))
                    .filter(Objects::nonNull)
                    .anyMatch(other -> NoteSolving.linked(held, other));
            lines.add(Component.translatable(holds ? "note.alchemia.would_hold" : "note.alchemia.would_not_hold")
                    .withStyle(holds ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
        }

        if (note.complete()) {
            lines.add(Component.translatable("note.alchemia.claim",
                    ResearchEntry.displayName(note.research())).withStyle(ChatFormatting.AQUA));
        }
        return lines;
    }
}
