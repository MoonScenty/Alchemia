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

public class AlchemonomiconScreen extends Screen {
    private static final ResourceLocation SHEET = Alchemia.id("textures/gui/research_browser.png");
    private static final ResourceLocation OVERLAY = Alchemia.id("textures/gui/research_overlay.png");
    private static final int SHEET_SIZE = 256;

    private static final int MARGIN = 16;
    private static final int BOX_MAX_W = 352;
    private static final int BOX_MAX_H = 240;
    private static final int GRID = 24;
    private static final int PLATE = 32;
    private static final int ICON = 16;
    private static final int SKY_REPEAT = 256;
    private static final double BACK_DRIFT = 2.0;
    private static final double OVER_DRIFT = 1.5;
    private static final float ZOOM_MIN = 1F;
    private static final float ZOOM_MAX = 2F;
    private static final float ZOOM_STEP = 0.25F;

    private static final int FRAME_CORNER = 22;
    private static final int FRAME_RUN = 64;
    private static final int FRAME_FROM = 13;
    private static final int FRAME_RUN_FROM = 48;

    private static final int PLATE_SQUARE = 80;
    private static final int PLATE_HEX = 112;
    private static final int PLATE_ROUND = 144;
    private static final int PLATE_BRACKETS = 176;
    private static final int PLATE_ROW = 48;

    private static final int TAB_X = 1;
    private static final int TAB_Y = 10;
    private static final int TAB_STEP = 24;

    private final List<ResourceKey<ResearchCategory>> categories = new ArrayList<>();
    private ResourceKey<ResearchCategory> openCategory;

    private double viewX;
    private double viewY;
    private float zoom = ZOOM_MIN;
    private boolean dragging;

    private ResearchEntry hovered;
    private ResourceLocation hoveredId;

    public AlchemonomiconScreen() {
        super(Component.translatable("item.alchemia.alchemonomicon"));
    }

    @Override
    protected void init() {
        categories.clear();
        PlayerKnowledge knowledge = PlayerKnowledge.of(minecraft.player);
        categoryRegistry().entrySet().stream()
                .filter(entry -> entry.getValue().isOpenTo(knowledge))
                .sorted(Comparator.comparingInt(entry -> entry.getValue().sortOrder()))
                .forEach(entry -> categories.add(entry.getKey()));
        if ((openCategory == null || !categories.contains(openCategory)) && !categories.isEmpty()) {
            openCategory = categories.getFirst();
        }
    }

    private Registry<ResearchCategory> categoryRegistry() {
        return minecraft.level.registryAccess().registryOrThrow(ModResearch.CATEGORY_KEY);
    }

    private Registry<ResearchEntry> entries() {
        return minecraft.level.registryAccess().registryOrThrow(ModResearch.ENTRY_KEY);
    }

    private int boxWidth() {
        return Math.min(width, BOX_MAX_W);
    }

    private int boxHeight() {
        return Math.min(height, BOX_MAX_H);
    }

    private int boxLeft() {
        return (width - boxWidth()) / 2;
    }

    private int boxTop() {
        return (height - boxHeight()) / 2;
    }

    private int pageLeft() {
        return boxLeft() + MARGIN;
    }

    private int pageTop() {
        return boxTop() + MARGIN;
    }

    private int pageWidth() {
        return boxWidth() - 2 * MARGIN;
    }

    private int pageHeight() {
        return boxHeight() - 2 * MARGIN;
    }

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
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillGradient(0, 0, width, height, 0xB0101018, 0xC0101018);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        drawPage(graphics, mouseX, mouseY);
        drawFrame(graphics);
        drawTabs(graphics);

        if (hovered != null) {
            graphics.renderComponentTooltip(font,
                    describe(hovered, hoveredId, PlayerKnowledge.of(minecraft.player)), mouseX, mouseY);
        } else {
            tabTooltip(graphics, mouseX, mouseY);
        }
    }

    private void drawPage(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.enableScissor(pageLeft(), pageTop(), pageLeft() + pageWidth(), pageTop() + pageHeight());
        drawSky(graphics);

        graphics.pose().pushPose();
        graphics.pose().translate(toWindowX(0), toWindowY(0), 0);
        graphics.pose().scale(1F / zoom, 1F / zoom, 1F);

        Registry<ResearchEntry> entries = entries();
        PlayerKnowledge knowledge = PlayerKnowledge.of(minecraft.player);
        for (Map.Entry<ResourceKey<ResearchEntry>, ResearchEntry> entry : entries.entrySet()) {
            ResearchEntry research = entry.getValue();
            if (!research.category().equals(openCategory)) {
                continue;
            }
            if (!research.isShownTo(knowledge, entry.getKey().location())) {
                continue;
            }
            float[] colour = lineColour(research, entry.getKey().location(), knowledge);
            for (ResourceLocation parentId : research.parents()) {
                ResearchEntry parent = entries.get(parentId);
                if (parent != null && parent.category().equals(openCategory)
                        && parent.isShownTo(knowledge, parentId)) {
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
            if (!research.isShownTo(knowledge, id)) {
                continue;
            }
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
        // sheet is declared at a quarter of its size so it tiles
        graphics.blit(texture, pageLeft(), pageTop(), w, h, u, v, Math.round(w * zoom), Math.round(h * zoom),
                SKY_REPEAT, SKY_REPEAT);
    }

    private static float[] lineColour(ResearchEntry research, ResourceLocation id, PlayerKnowledge knowledge) {
        if (knowledge.hasResearch(id)) {
            return new float[] {0.6F, 0.6F, 0.7F};
        }
        if (research.isAvailableTo(knowledge)) {
            return new float[] {0.25F, 0.25F, 0.3F};
        }
        return new float[] {0.1F, 0.1F, 0.15F};
    }

    private void drawNode(GuiGraphics graphics, ResearchEntry research, ResourceLocation id,
            PlayerKnowledge knowledge, int x, int y) {
        float bright;
        if (knowledge.hasResearch(id)) {
            bright = 1F;
        } else if (research.isAvailableTo(knowledge)) {
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
            // items cannot be tinted, so unknown nodes are shaded over instead
            int shade = Math.round((1F - bright) * 0xC0);
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 200);
            graphics.fill(x, y, x + ICON, y + ICON, shade << 24);
            graphics.pose().popPose();
        }
    }

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

    private void drawFrame(GuiGraphics graphics) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        int left = boxLeft();
        int top = boxTop();
        int right = left + boxWidth();
        int bottom = top + boxHeight();
        for (int x = left + 16; x < right - 16; x += FRAME_RUN) {
            int run = Math.min(FRAME_RUN, right - 16 - x);
            graphics.blit(SHEET, x, top - 2, FRAME_RUN_FROM, FRAME_FROM, run, FRAME_CORNER, SHEET_SIZE, SHEET_SIZE);
            graphics.blit(SHEET, x, bottom - 20, FRAME_RUN_FROM, FRAME_FROM, run, FRAME_CORNER,
                    SHEET_SIZE, SHEET_SIZE);
        }
        for (int y = top + 16; y < bottom - 16; y += FRAME_RUN) {
            int run = Math.min(FRAME_RUN, bottom - 16 - y);
            graphics.blit(SHEET, left - 2, y, FRAME_FROM, FRAME_RUN_FROM, FRAME_CORNER, run, SHEET_SIZE, SHEET_SIZE);
            graphics.blit(SHEET, right - 20, y, FRAME_FROM, FRAME_RUN_FROM, FRAME_CORNER, run,
                    SHEET_SIZE, SHEET_SIZE);
        }
        corner(graphics, left - 2, top - 2);
        corner(graphics, left - 2, bottom - 20);
        corner(graphics, right - 20, top - 2);
        corner(graphics, right - 20, bottom - 20);
    }

    private static void corner(GuiGraphics graphics, int x, int y) {
        graphics.blit(SHEET, x, y, FRAME_FROM, FRAME_FROM, FRAME_CORNER, FRAME_CORNER, SHEET_SIZE, SHEET_SIZE);
    }

    private int tabX() {
        return boxLeft() + TAB_X;
    }

    private int tabY(int index) {
        return boxTop() + TAB_Y + index * TAB_STEP;
    }

    private void drawTabs(GuiGraphics graphics) {
        for (int index = 0; index < categories.size(); index++) {
            ResourceKey<ResearchCategory> key = categories.get(index);
            int y = tabY(index);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            corner(graphics, tabX() - 3, y - 3);
            float strength = key.equals(openCategory) ? 1F : 0.66F;
            graphics.setColor(strength, strength, strength, key.equals(openCategory) ? 1F : 0.8F);
            graphics.blit(categoryRegistry().get(key).icon(), tabX(), y, 0, 0, ICON, ICON, ICON, ICON);
            graphics.setColor(1F, 1F, 1F, 1F);
        }
    }

    private void tabTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        for (int index = 0; index < categories.size(); index++) {
            if (overTab(index, mouseX, mouseY)) {
                graphics.renderTooltip(font, Component.empty().append(ResearchCategory.displayName(categories.get(index)))
                        .append(" (" + completion(categories.get(index)) + "%)"), mouseX, mouseY);
                return;
            }
        }
    }

    // original's: the share of a tab's research that is known, leaving out what everyone starts with
    private int completion(ResourceKey<ResearchCategory> category) {
        PlayerKnowledge knowledge = PlayerKnowledge.of(minecraft.player);
        int all = 0;
        int known = 0;
        for (var entry : entries().entrySet()) {
            if (entry.getValue().category().equals(category) && !entry.getValue().autoUnlock()) {
                all++;
                if (knowledge.hasResearch(entry.getKey().location())) {
                    known++;
                }
            }
        }
        return all == 0 ? 0 : known * 100 / all;
    }

    private boolean overTab(int index, double mouseX, double mouseY) {
        int y = tabY(index);
        return mouseX >= tabX() && mouseX < tabX() + ICON && mouseY >= y && mouseY < y + ICON;
    }

    private List<Component> describe(ResearchEntry research, ResourceLocation id, PlayerKnowledge knowledge) {
        List<Component> lines = new ArrayList<>();
        lines.add(ResearchEntry.displayName(id).copy().withStyle(ChatFormatting.GOLD));
        lines.add(ResearchEntry.description(id));
        if (research.warp() > 0) {
            int level = Math.min(5, research.warp());
            lines.add(Component.translatable("research.alchemia.forbidden",
                    Component.translatable("research.alchemia.forbidden." + level))
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        }

        if (knowledge.hasResearch(id)) {
            lines.add(Component.translatable("research.alchemia.known").withStyle(ChatFormatting.GREEN));
            lines.add(Component.translatable("research.alchemia.open").withStyle(ChatFormatting.DARK_GRAY));
        } else if (!research.parentsKnown(knowledge)) {
            lines.add(Component.translatable("research.alchemia.locked").withStyle(ChatFormatting.DARK_GRAY));
        } else if (!research.cluesMet(knowledge)) {
            List<Component> aspects = research.clues().stream()
                    .filter(clue -> !me.moonscenty.alchemia.research.Clues.met(knowledge, clue))
                    .flatMap(clue -> me.moonscenty.alchemia.registry.ModAspects.REGISTRY.getOptional(clue).stream())
                    .map(aspect -> (Component) aspect.displayName()).toList();
            if (!aspects.isEmpty()) {
                lines.add(Component.translatable("research.alchemia.needs_knowledge",
                        aspects.stream().reduce((a, b) -> Component.empty().append(a).append(", ").append(b))
                                .orElse(Component.empty())).withStyle(ChatFormatting.DARK_PURPLE));
            } else {
                lines.add(Component.translatable("research.alchemia.needs_clue").withStyle(ChatFormatting.DARK_PURPLE));
            }
        } else if (research.secondary()) {
            boolean affords = me.moonscenty.alchemia.research.ResearchPurchase.affords(minecraft.player, research);
            lines.add(Component.translatable(affords ? "research.alchemia.purchase" : "research.alchemia.short",
                    research.experience()).withStyle(affords ? ChatFormatting.DARK_GREEN : ChatFormatting.RED,
                    ChatFormatting.ITALIC));
        } else {
            research.missingFor(knowledge::knows).ifPresentOrElse(
                    missing -> lines.add(Component.translatable("research.alchemia.needs", names(missing))
                            .withStyle(ChatFormatting.DARK_PURPLE)),
                    () -> {
                        lines.add(Component.translatable("research.alchemia.ready").withStyle(ChatFormatting.AQUA));
                        if (carriesNote(id)) {
                            lines.add(Component.translatable("research.alchemia.has_note")
                                    .withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
                        } else if (canScribe()) {
                            lines.add(Component.translatable("research.alchemia.take_note")
                                    .withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC));
                        } else {
                            lines.add(Component.translatable("research.alchemia.need_scribing")
                                    .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
                        }
                    });
        }
        return lines;
    }

    private boolean carriesNote(ResourceLocation id) {
        for (var stack : minecraft.player.getInventory().items) {
            var note = stack.is(me.moonscenty.alchemia.registry.ModItems.RESEARCH_NOTES.get())
                    ? me.moonscenty.alchemia.item.ResearchNoteItem.noteOn(stack) : null;
            if (note != null && note.research().equals(id)) {
                return true;
            }
        }
        return false;
    }

    private boolean canScribe() {
        boolean paper = false;
        boolean ink = false;
        for (var stack : minecraft.player.getInventory().items) {
            paper |= stack.is(net.minecraft.world.item.Items.PAPER);
            ink |= stack.is(me.moonscenty.alchemia.registry.ModItems.SCRIBING_TOOLS.get())
                    && stack.getDamageValue() < stack.getMaxDamage();
        }
        return paper && ink;
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (int index = 0; index < categories.size(); index++) {
            if (overTab(index, mouseX, mouseY)) {
                openCategory = categories.get(index);
                viewX = 0;
                viewY = 0;
                minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance
                        .forUI(me.moonscenty.alchemia.registry.ModSounds.CAMERA_CLACK.get(), 1.0F, 0.4F));
                return true;
            }
        }
        if (hovered != null) {
            PlayerKnowledge knowledge = PlayerKnowledge.of(minecraft.player);
            if (knowledge.hasResearch(hoveredId)) {
                minecraft.setScreen(new ResearchPageScreen(this, hoveredId, hovered));
                return true;
            }
            if (me.moonscenty.alchemia.research.ResearchPurchase.canBuy(knowledge, hoveredId, hovered)) {
                if (me.moonscenty.alchemia.research.ResearchPurchase.affords(minecraft.player, hovered)) {
                    PacketDistributor.sendToServer(new me.moonscenty.alchemia.network.BuyResearch(hoveredId));
                }
                return true;
            }
            if (NoteRequests.isReady(knowledge, hoveredId, hovered)) {
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

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY != 0) {
            zoom = Mth.clamp(zoom + (scrollY < 0 ? ZOOM_STEP : -ZOOM_STEP), ZOOM_MIN, ZOOM_MAX);
            keepInBounds();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

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
