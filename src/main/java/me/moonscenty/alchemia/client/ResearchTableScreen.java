package me.moonscenty.alchemia.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;

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
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * The desk as the reader sees it, laid out as the original laid it out.
 * <p>
 * A wooden panel with the scribing tools and the note in the two holders at the top, what the note still holds
 * racked in a five by five grid down the left, the two dishes and the button that mixes them at its foot, and the
 * note itself spread on a sheet of parchment over the leather. The places, the sizes and the way the sheet is drawn
 * are the original's, read out of its screen; the pictures are the original's when its jar is there and ours,
 * painted to the same layout, when it is not.
 */
public class ResearchTableScreen extends AbstractContainerScreen<ResearchTableMenu> {
    private static final ResourceLocation PANEL = Alchemia.id("textures/gui/research_table.png");
    private static final ResourceLocation PARCHMENT = Alchemia.id("textures/gui/research_parchment.png");
    private static final ResourceLocation HEX = Alchemia.id("textures/gui/research_hex.png");
    private static final ResourceLocation HEX_LIT = Alchemia.id("textures/gui/research_hex_lit.png");
    private static final ResourceLocation ORB = Alchemia.id("textures/particle/mote.png");
    private static final ResourceLocation SCRIPT = Alchemia.id("textures/misc/script.png");

    private static final int SHEET = 256;
    /** The panel proper, and under it the narrower plate the inventory sits in. */
    private static final int PANEL_W = 255;
    private static final int PANEL_H = 167;
    private static final int PLATE_X = 40;
    private static final int PLATE_FROM_Y = 166;
    private static final int PLATE_W = 184;
    private static final int PLATE_H = 88;

    /** The parchment the note is spread on, and the middle of it, where the note's centre cell sits. */
    private static final int BOARD_X = 94;
    private static final int BOARD_Y = 8;
    private static final int BOARD_SIZE = 150;
    private static final int CENTRE_X = BOARD_X + BOARD_SIZE / 2;
    private static final int CENTRE_Y = BOARD_Y + BOARD_SIZE / 2;
    /** How big a cell is: the distance from its middle to a corner. */
    private static final double CELL = 9.0;
    /** A cell's plate is drawn this wide, overlapping its neighbours a little, as the original's were. */
    private static final int PLATE = 16;
    private static final float EMPTY_ALPHA = 0.25F;
    /** A written aspect that does not yet join up with anything the subject pinned is drawn this faintly. */
    private static final float LOOSE_ALPHA = 0.66F;

    /** The rack: five down and five across, filled a column at a time, and turned a column at a time. */
    private static final int POOL_X = 10;
    private static final int POOL_Y = 40;
    private static final int POOL_SIDE = 5;
    private static final int POOL_STEP = 16;
    private static final int POOL_PAGE = POOL_SIDE * POOL_SIDE;

    // The page arrows under the rack. They are only drawn while there is a column that way to turn to.
    private static final int ARROW_W = 24;
    private static final int ARROW_H = 8;
    private static final int PREV_X = 27;
    private static final int NEXT_X = 51;
    private static final int ARROW_Y = 121;
    private static final int PREV_FROM_X = 184;
    private static final int NEXT_FROM_X = 208;
    private static final int ARROW_FROM_Y = 208;

    // The two dishes, and the button between them that mixes what is in them.
    private static final int DISH_A_X = 13;
    private static final int DISH_B_X = 71;
    private static final int DISH_Y = 139;
    private static final int MIX_X = 35;
    private static final int MIX_Y = 139;
    private static final int MIX_W = 32;
    private static final int MIX_H = 16;
    private static final int MIX_FROM_X = 184;
    private static final int MIX_FROM_Y = 184;
    private static final int MIX_PRESSED_FROM_Y = 168;

    private static final int ICON = 16;

    // The script that settles on the parchment. It says nothing: it is there so the sheet looks written on rather
    // than blank. Sixteen letters, as many as the original's strip has, each drawn small and turned on its side.
    private static final int LETTERS = 16;
    private static final int LETTER = 16;
    private static final int LETTER_DRAWN = 10;
    /** How often another letter is tried for, and how far out one may land. */
    private static final long SETTLES = 250L;
    private static final int SPAN = 60;
    /** A letter lasts somewhere in here, then is gone. */
    private static final long LASTS = 15_000L;
    private static final long LASTS_UP_TO = 10_000L;
    /** How dark a letter ever gets. Faint enough to read the sheet straight through it. */
    private static final float FAINTEST = 0.33F;

    /** What the reader has picked up and is about to write down. */
    private Holder<Aspect> held;
    private Holder<Aspect> dishA;
    private Holder<Aspect> dishB;
    /** How many columns the rack has been turned. Clamped every time it is read, as the sheet's stock changes. */
    private int page;

    /** A letter on the parchment, and when it settled and when it will have gone. */
    private record Rune(long born, long gone, int letter) {
    }

    private final Map<HexGrid.Hex, Rune> runes = new HashMap<>();
    private final RandomSource random = RandomSource.create();
    private long nextRune;
    private ResourceLocation written;

    public ResearchTableScreen(ResearchTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = PANEL_W;
        imageHeight = PANEL_H + PLATE_H;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // the original's panels have soft, half-clear edges, which come out solid without blending
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(PANEL, leftPos, topPos, 0, 0, PANEL_W, PANEL_H, SHEET, SHEET);
        graphics.blit(PANEL, leftPos + PLATE_X, topPos + PANEL_H, 0, PLATE_FROM_Y, PLATE_W, PLATE_H, SHEET, SHEET);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        drawBoard(graphics, mouseX, mouseY, partialTick);
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

    /** Where the middle of a cell lands on the screen, worked out the way the original worked it out. */
    private double cellX(HexGrid.Hex at) {
        return leftPos + CENTRE_X + CELL * 1.5 * at.q();
    }

    private double cellY(HexGrid.Hex at) {
        return topPos + CENTRE_Y + CELL * Math.sqrt(3.0) * (at.r() + at.q() / 2.0);
    }

    private void drawBoard(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ResearchNote note = menu.note();
        if (note == null) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(PARCHMENT, leftPos + BOARD_X, topPos + BOARD_Y, 0, 0, BOARD_SIZE, BOARD_SIZE, SHEET, SHEET);
        // clipped to the sheet, so nothing can ever creep onto the wood around it
        graphics.enableScissor(leftPos + BOARD_X, topPos + BOARD_Y,
                leftPos + BOARD_X + BOARD_SIZE, topPos + BOARD_Y + BOARD_SIZE);
        drawScript(graphics, note);

        Set<HexGrid.Hex> joined = joinedToPinned(note);
        float ticks = minecraft != null && minecraft.player != null ? minecraft.player.tickCount + partialTick : 0F;
        drawLinks(graphics, note, joined, ticks);

        HexGrid.Hex over = cellUnder(mouseX, mouseY);
        if (!note.complete()) {
            for (ResearchNote.Cell cell : note.cells()) {
                if (cell.pinned()) {
                    drawOrb(graphics, cell.at(), ticks);
                } else if (cell.at().equals(over)) {
                    drawPlate(graphics, cell.at(), HEX_LIT, 1F, true);
                } else {
                    drawPlate(graphics, cell.at(), HEX, EMPTY_ALPHA, false);
                }
            }
        }
        for (ResearchNote.Cell cell : note.cells()) {
            float alpha = cell.pinned() || joined.contains(cell.at()) ? 1F : LOOSE_ALPHA;
            cell.aspect().ifPresent(aspect -> drawAspect(graphics, aspect,
                    cellX(cell.at()) - ICON / 2.0, cellY(cell.at()) - ICON / 2.0, alpha));
        }
        graphics.disableScissor();
    }

    /** Every cell that hangs off something the subject pinned, by a chain of aspects that hold together. */
    private static Set<HexGrid.Hex> joinedToPinned(ResearchNote note) {
        Set<HexGrid.Hex> joined = new HashSet<>();
        for (ResearchNote.Cell pinned : note.pinnedCells()) {
            joined.addAll(NoteSolving.reach(note, pinned.at()));
        }
        return joined;
    }

    /** One cell's plate, centred on the cell. A lit one is added on rather than laid over, so it glows. */
    private void drawPlate(GuiGraphics graphics, HexGrid.Hex at, ResourceLocation plate, float alpha, boolean glow) {
        RenderSystem.enableBlend();
        if (glow) {
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        } else {
            RenderSystem.defaultBlendFunc();
        }
        graphics.setColor(1F, 1F, 1F, alpha);
        graphics.pose().pushPose();
        graphics.pose().translate(cellX(at), cellY(at), 0);
        graphics.blit(plate, -PLATE / 2, -PLATE / 2, PLATE, PLATE, 0, 0, 1, 1, 1, 1);
        graphics.pose().popPose();
        graphics.setColor(1F, 1F, 1F, 1F);
        RenderSystem.defaultBlendFunc();
    }

    /** A pinned cell sits on a soft light that breathes, each channel at its own pace. */
    private void drawOrb(GuiGraphics graphics, HexGrid.Hex at, float ticks) {
        float red = 0.7F + Mth.sin((float) (ticks / 10.0)) * 0.15F;
        float green = 0.7F + Mth.sin((float) (ticks / 11.0)) * 0.15F;
        float blue = 0.7F + Mth.sin((float) (ticks / 12.0)) * 0.15F;
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        graphics.setColor(red, green, blue, 1F);
        graphics.pose().pushPose();
        graphics.pose().translate(cellX(at), cellY(at), 0);
        graphics.blit(ORB, -PLATE, -PLATE, 2 * PLATE, 2 * PLATE, 0, 0, 1, 1, 1, 1);
        graphics.pose().popPose();
        graphics.setColor(1F, 1F, 1F, 1F);
        RenderSystem.defaultBlendFunc();
    }

    /** A line between every two neighbouring cells of a chain off a pinned aspect, glowing a little in and out. */
    private void drawLinks(GuiGraphics graphics, ResearchNote note, Set<HexGrid.Hex> joined, float ticks) {
        float bright = 0.3F + Mth.sin(ticks * 0.3F) * 0.3F + 0.3F;
        int colour = 0xCC000000 | Math.round(bright * 255) << 16 | Math.round(0.6F * 255) << 8 | Math.round(0.8F * 255);
        Set<HexGrid.Hex> done = new HashSet<>();
        for (HexGrid.Hex from : joined) {
            Holder<Aspect> here = note.cellAt(from).flatMap(ResearchNote.Cell::aspect).orElse(null);
            if (here == null) {
                continue;
            }
            done.add(from);
            for (HexGrid.Hex to : from.neighbours()) {
                Holder<Aspect> there = note.cellAt(to).flatMap(ResearchNote.Cell::aspect).orElse(null);
                if (there != null && !done.contains(to) && joined.contains(to) && NoteSolving.linked(here, there)) {
                    line(graphics, cellX(from), cellY(from), cellX(to), cellY(to), colour);
                }
            }
        }
    }

    private static void line(GuiGraphics graphics, double x1, double y1, double x2, double y2, int colour) {
        double length = Math.hypot(x2 - x1, y2 - y1);
        graphics.pose().pushPose();
        graphics.pose().translate(x1, y1, 0);
        graphics.pose().mulPose(Axis.ZP.rotation((float) Math.atan2(y2 - y1, x2 - x1)));
        graphics.fill(0, -1, (int) Math.round(length), 1, colour);
        graphics.pose().popPose();
    }

    // --- the script on the parchment ---------------------------------------

    /**
     * Letters settling on the empty parts of the sheet and fading off again.
     * <p>
     * They spell nothing and mean nothing. They land on the board's own grid, so they stay out from under the cells
     * and line up with what is written; a place is picked anywhere within reach of the middle, as the original did.
     */
    private void drawScript(GuiGraphics graphics, ResearchNote note) {
        if (!note.research().equals(written)) {
            // a different note is a different page; what settled on the last one does not carry over
            runes.clear();
            written = note.research();
        }
        long now = System.currentTimeMillis();
        settle(note, now);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        runes.values().removeIf(rune -> rune.gone() <= now);
        for (Map.Entry<HexGrid.Hex, Rune> entry : runes.entrySet()) {
            Rune rune = entry.getValue();
            float through = (float) (now - rune.born()) / (rune.gone() - rune.born());
            // up over the first quarter, held, then away over the last half
            float alpha = through < 0.25F ? through * 2F : through > 0.5F ? 1F - through : 0.5F;

            graphics.setColor(0F, 0F, 0F, alpha * FAINTEST);
            graphics.pose().pushPose();
            graphics.pose().translate(cellX(entry.getKey()), cellY(entry.getKey()), 0);
            graphics.pose().mulPose(Axis.ZN.rotationDegrees(90F));
            graphics.blit(SCRIPT, -LETTER_DRAWN / 2, -LETTER_DRAWN / 2, LETTER_DRAWN, LETTER_DRAWN,
                    rune.letter() * LETTER, 0, LETTER, LETTER, LETTERS * LETTER, LETTER);
            graphics.pose().popPose();
        }
        graphics.setColor(1F, 1F, 1F, 1F);
    }

    /** Tries to put one more letter down. Nothing happens most of the time, which is what makes them drift in. */
    private void settle(ResearchNote note, long now) {
        if (now < nextRune) {
            return;
        }
        nextRune = now + SETTLES;
        HexGrid.Hex at = toHex(random.nextInt(2 * SPAN) - SPAN, random.nextInt(2 * SPAN) - SPAN);
        if (runes.containsKey(at) || note.cellAt(at).isPresent()) {
            return;
        }
        runes.put(at, new Rune(now, now + LASTS + random.nextInt((int) LASTS_UP_TO), random.nextInt(LETTERS)));
    }

    /** The cell a point lies in, measured from the middle of the sheet. */
    private static HexGrid.Hex toHex(double x, double y) {
        double q = 2.0 / 3.0 * x / CELL;
        double r = (Math.sqrt(3.0) / 3.0 * y - x / 3.0) / CELL;
        // round in cube coordinates, so a point near an edge goes to the nearer cell rather than a far one
        double s = -q - r;
        long rq = Math.round(q);
        long rr = Math.round(r);
        long rs = Math.round(s);
        double dq = Math.abs(rq - q);
        double dr = Math.abs(rr - r);
        double ds = Math.abs(rs - s);
        if (dq > dr && dq > ds) {
            rq = -rr - rs;
        } else if (dr > ds) {
            rr = -rq - rs;
        }
        return new HexGrid.Hex((int) rq, (int) rr);
    }

    /** The cell the pointer is over, if the note has one there. */
    private HexGrid.Hex cellUnder(int mouseX, int mouseY) {
        ResearchNote note = menu.note();
        if (note == null || !within(mouseX, mouseY, BOARD_X, BOARD_Y, BOARD_SIZE, BOARD_SIZE)) {
            return null;
        }
        HexGrid.Hex at = toHex(mouseX - leftPos - CENTRE_X, mouseY - topPos - CENTRE_Y);
        return note.cellAt(at).isPresent() ? at : null;
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

    /** How many columns the rack can be turned by before its last column is in view. */
    private int lastPage() {
        int over = pool().size() - POOL_PAGE;
        return over <= 0 ? 0 : (over + POOL_SIDE - 1) / POOL_SIDE;
    }

    /**
     * The turn being shown, brought back within range first.
     * <p>
     * Mixing takes two aspects off the sheet and puts one back, so the stock can shrink out from under a turn that
     * was made, and the last column can stop existing while the reader is looking at it.
     */
    private int page() {
        page = Math.max(0, Math.min(page, lastPage()));
        return page;
    }

    /** What is in view. */
    private List<Holder<Aspect>> shown() {
        List<Holder<Aspect>> stock = pool();
        int from = page() * POOL_SIDE;
        return stock.subList(Math.min(from, stock.size()), Math.min(from + POOL_PAGE, stock.size()));
    }

    /** Where the nth aspect in view is drawn: down a column first, then on to the next. */
    private int poolX(int place) {
        return leftPos + POOL_X + (place / POOL_SIDE) * POOL_STEP;
    }

    private int poolY(int place) {
        return topPos + POOL_Y + (place % POOL_SIDE) * POOL_STEP;
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
            drawAspect(graphics, aspect, x, y, 1F);
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 200);
            graphics.drawString(font, String.valueOf(note.budget().get(aspect)),
                    x + ICON - 6, y + ICON - 6, 0xFFFFE9C0, true);
            graphics.pose().popPose();
        }
    }

    // --- the three buttons -------------------------------------------------

    /** The arrows and the mixing button, drawn only while they would do something. */
    private void drawButtons(GuiGraphics graphics, int mouseX, int mouseY) {
        ResearchNote note = menu.note();
        if (note == null || note.complete()) {
            return;
        }
        if (page() > 0) {
            graphics.blit(PANEL, leftPos + PREV_X, topPos + ARROW_Y, PREV_FROM_X, ARROW_FROM_Y, ARROW_W, ARROW_H,
                    SHEET, SHEET);
        }
        if (page() < lastPage()) {
            graphics.blit(PANEL, leftPos + NEXT_X, topPos + ARROW_Y, NEXT_FROM_X, ARROW_FROM_Y, ARROW_W, ARROW_H,
                    SHEET, SHEET);
        }
        if (mixResult().isPresent()) {
            boolean pressing = within(mouseX, mouseY, MIX_X, MIX_Y, MIX_W, MIX_H);
            graphics.blit(PANEL, leftPos + MIX_X, topPos + MIX_Y, MIX_FROM_X,
                    pressing ? MIX_PRESSED_FROM_Y : MIX_FROM_Y, MIX_W, MIX_H, SHEET, SHEET);
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
            drawAspect(graphics, dishA, leftPos + DISH_A_X, topPos + DISH_Y, 1F);
        }
        if (dishB != null) {
            drawAspect(graphics, dishB, leftPos + DISH_B_X, topPos + DISH_Y, 1F);
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

    /**
     * One aspect, painted in its own colour.
     * <p>
     * Blending is turned on every single time rather than once for the lot. Drawing a string ends the font's batch,
     * and ending a batch puts back the state that batch wanted, which for text means blending off. So the first
     * aspect in a row would come out soft and every one after it hard.
     */
    private void drawAspect(GuiGraphics graphics, Holder<Aspect> aspect, double x, double y, float alpha) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        int colour = aspect.value().color();
        graphics.setColor(((colour >> 16) & 0xFF) / 255F, ((colour >> 8) & 0xFF) / 255F, (colour & 0xFF) / 255F,
                alpha);
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.blit(aspect.value().icon(), 0, 0, 0, 0, ICON, ICON, ICON, ICON);
        graphics.pose().popPose();
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

        if (within(mouseX, mouseY, PREV_X, ARROW_Y, ARROW_W, ARROW_H) && page() > 0) {
            page--;
            click();
            return true;
        }
        if (within(mouseX, mouseY, NEXT_X, ARROW_Y, ARROW_W, ARROW_H) && page() < lastPage()) {
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
