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

public class ResearchPageScreen extends Screen {
    private static final ResourceLocation BOOK = Alchemia.id("textures/gui/research_book.png");
    private static final ResourceLocation PLATES = Alchemia.id("textures/gui/research_book_overlay.png");
    private static final int SHEET = 512;

    private static final int PANE_W = 256;
    private static final int PANE_H = 181;
    private static final float BOOK_SCALE = 1.3F;
    private static final int SIDE_STEP = 152;
    private static final int TEXT_W = 139;
    private static final int LINE = 9;
    private static final int ICON = 16;
    private static final int CYCLE_TICKS = 20;
    private static final int TITLE_COLOUR = 0xFF303030;
    private static final int HEADING_COLOUR = 0xFF505050;
    private static final int TEXT_COLOUR = 0xFF000000;

    private static final int TURN_W = 12;
    private static final int TURN_H = 8;

    private final ThaumonomiconScreen parent;
    private final ResourceLocation id;
    private final List<ResearchPage> pages;

    private int spread;
    private long ticks;
    private int mouseX;
    private int mouseY;
    private List<Component> tip;
    private Holder<Aspect> pointedAspect;
    private int pointedX;
    private int pointedY;
    private static java.util.Map<Aspect, List<ItemStack>> carriers;

    public ResearchPageScreen(ThaumonomiconScreen parent, ResourceLocation id, ResearchEntry entry) {
        super(ResearchEntry.displayName(id));
        this.parent = parent;
        this.id = id;
        PlayerKnowledge knowledge = PlayerKnowledge.of(net.minecraft.client.Minecraft.getInstance().player);
        List<ResearchPage> shown = new java.util.ArrayList<>(entry.pagesFor(knowledge::hasResearch));
        // original: the aspects entry ends with every aspect, four to a page
        if (id.equals(Alchemia.id("aspects"))) {
            List<ResourceLocation> all = me.moonscenty.alchemia.registry.ModAspects.REGISTRY.holders()
                    .sorted(java.util.Comparator.comparing(holder -> holder.value().tag()))
                    .map(holder -> holder.key().location()).toList();
            for (int from = 0; from < all.size(); from += 4) {
                shown.add(new ResearchPage.Aspects(all.subList(from, Math.min(all.size(), from + 4)), Optional.empty()));
            }
        }
        this.pages = List.copyOf(shown);
    }

    @Override
    public void tick() {
        ticks++;
    }

    private int paneX() {
        return (width - PANE_W) / 2;
    }

    private int paneY() {
        return (height - PANE_H) / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        tip = null;
        pointedAspect = null;
        super.render(graphics, mouseX, mouseY, partialTick);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.pose().pushPose();
        graphics.pose().translate((width - PANE_W * BOOK_SCALE) / 2F, (height - PANE_H * BOOK_SCALE) / 2F, 0);
        graphics.pose().scale(BOOK_SCALE, BOOK_SCALE, 1F);
        // sheet read as 256 wide to take in the whole book
        graphics.blit(BOOK, 0, 0, PANE_W, PANE_H, 0, 0, SHEET, PANE_H * 2, SHEET, SHEET);
        graphics.pose().popPose();

        drawSheet(graphics, 0, spread);
        drawSheet(graphics, 1, spread + 1);
        drawTurns(graphics);
        if (pointedAspect != null) {
            drawCarriers(graphics, pointedAspect.value());
        }
        if (tip != null) {
            graphics.renderComponentTooltip(font, tip, mouseX, mouseY);
        }
    }

    private boolean over(int x, int y, int size) {
        return mouseX >= x && mouseY >= y && mouseX < x + size && mouseY < y + size;
    }

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
            case CONSTRUCT -> drawConstruct(graphics, (ResearchPage.Construct) page, x - 4 + side * SIDE_STEP, y - 8);
            case ASPECTS -> drawAspects(graphics, (ResearchPage.Aspects) page, x - 4 + side * SIDE_STEP, y - 8);
        }
    }

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
        int floor = paneY() + 180;
        for (FormattedCharSequence line : font.split(Component.translatable(page.key()), TEXT_W)) {
            if (y > floor) {
                break;
            }
            graphics.drawString(font, line, x, y, TEXT_COLOUR, false);
            y += LINE;
        }
    }

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

    private static void plate(GuiGraphics graphics, int x, int y, int fromX, int fromY, int wide, int tall) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(PLATES, x, y, wide, tall, fromX, fromY, wide, tall, SHEET, SHEET);
    }

    private void drawCrafting(GuiGraphics graphics, Recipe<?> recipe, ItemStack result, int x, int y) {
        heading(graphics, "research.alchemia.crafting", x, y);
        plate(graphics, x + 4, y + 64, 120, 30, 104, 104);
        plate(graphics, x + 40, y + 24, 40, 6, 32, 32);
        drawStack(graphics, result, x + 48, y + 32);
        drawGrid(graphics, recipe, x + 16, y + 76);
    }

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

    private void drawSmelting(GuiGraphics graphics, Recipe<?> recipe, ItemStack result, int x, int y) {
        heading(graphics, "research.alchemia.smelting", x, y);
        plate(graphics, x, y + 28, 0, 384, 112, 128);
        List<Ingredient> ingredients = recipe.getIngredients();
        if (!ingredients.isEmpty()) {
            drawIngredient(graphics, ingredients.getFirst(), x + 48, y + 64);
        }
        drawStack(graphics, result, x + 48, y + 144);
    }

    private void drawCrucible(GuiGraphics graphics, CrucibleRecipe crucible, ItemStack result, int x, int y) {
        heading(graphics, "research.alchemia.in_crucible", x, y);
        plate(graphics, x, y + 28, 0, 6, 112, 34);
        plate(graphics, x, y + 92, 0, 40, 112, 96);
        plate(graphics, x + 42, y + 76, 200, 168, 22, 26);
        drawStack(graphics, result, x + 48, y + 36);
        drawIngredient(graphics, crucible.catalyst(), x + 26, y + 72);
        drawAspectRows(graphics, crucible.aspects(), 3, x + 28, y + 128);
    }

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
        ItemStack[] centrals = infusion.central().getItems();
        int unstable = Math.min(5, infusion.instabilityFor(centrals.length > 0 ? centrals[0] : ItemStack.EMPTY) / 2);
        Component instability = Component.translatable("research.alchemia.instability",
                Component.translatable("research.alchemia.instability." + unstable));
        graphics.drawString(font, instability, x + 56 - font.width(instability) / 2, y + 194, HEADING_COLOUR, false);
    }

    private void drawConstruct(GuiGraphics graphics, ResearchPage.Construct page, int x, int y) {
        heading(graphics, "research.alchemia.construct", x, y);
        List<Holder<Aspect>> asked = page.cost().sortedByAmount();
        for (int index = 0; index < asked.size(); index++) {
            drawAspect(graphics, asked.get(index), page.cost().get(asked.get(index)),
                    x + 14 + 18 * index + (5 - asked.size()) * 8, y + 182);
        }
        int across = 64 - (page.wide() * 16 + page.deep() * 16) / 2;
        int lift = -page.tall() * 25;
        int index = 0;
        for (int layer = 0; layer < page.tall(); layer++) {
            for (int depth = page.deep() - 1; depth >= 0; depth--) {
                for (int width = page.wide() - 1; width >= 0; width--) {
                    if (index < page.blocks().size()) {
                        var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(page.blocks().get(index));
                        if (item != net.minecraft.world.item.Items.AIR) {
                            drawStack(graphics, new ItemStack(item), x + across + width * 16 + depth * 16,
                                    y + 100 + lift - width * 8 + depth * 8 + layer * 50);
                        }
                    }
                    index++;
                }
            }
        }
    }

    private void drawAspects(GuiGraphics graphics, ResearchPage.Aspects page, int x, int y) {
        for (int index = 0; index < page.aspects().size(); index++) {
            var found = me.moonscenty.alchemia.registry.ModAspects.REGISTRY.getHolder(page.aspects().get(index));
            if (found.isEmpty()) {
                continue;
            }
            Holder<Aspect> aspect = found.get();
            int top = y + 8 + index * 50;
            graphics.pose().pushPose();
            graphics.pose().translate(x, top, 0);
            graphics.pose().scale(2F, 2F, 1F);
            drawIcon(graphics, aspect, 0, 0);
            graphics.pose().popPose();
            Component name = aspect.value().displayName();
            graphics.drawString(font, name, x + 16 - font.width(name) / 2, top + 33, HEADING_COLOUR, false);
            var parts = aspect.value().components();
            if (parts.isPresent() && parts.get().size() == 2) {
                for (int part = 0; part < 2; part++) {
                    Holder<Aspect> from = parts.get().get(part);
                    graphics.pose().pushPose();
                    graphics.pose().translate(x + 54 + part * 42, top + 4, 0);
                    graphics.pose().scale(1.5F, 1.5F, 1F);
                    drawIcon(graphics, from, 0, 0);
                    graphics.pose().popPose();
                    Component partName = from.value().displayName().copy().withStyle(net.minecraft.ChatFormatting.ITALIC);
                    graphics.drawString(font, partName, x + 66 + part * 42 - font.width(partName) / 2, top + 30,
                            HEADING_COLOUR, false);
                }
                graphics.drawString(font, "=", x + 39, top + 12, 0xFF999999, false);
                graphics.drawString(font, "+", x + 83, top + 12, 0xFF999999, false);
            } else {
                graphics.drawString(font, Component.translatable("research.alchemia.primal_aspect"), x + 48, top + 12,
                        0xFF444444, false);
            }
            if (over(x, top, 40)) {
                pointedAspect = aspect;
                pointedX = mouseX;
                pointedY = mouseY;
            }
        }
    }

    private void drawIcon(GuiGraphics graphics, Holder<Aspect> aspect, int x, int y) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        int colour = aspect.value().color();
        graphics.setColor(((colour >> 16) & 0xFF) / 255F, ((colour >> 8) & 0xFF) / 255F, (colour & 0xFF) / 255F, 1F);
        graphics.blit(aspect.value().icon(), x, y, 0, 0, ICON, ICON, ICON, ICON);
        graphics.setColor(1F, 1F, 1F, 1F);
    }

    // original: pointing at an aspect shows up to sixteen things that carry it
    private void drawCarriers(GuiGraphics graphics, Aspect aspect) {
        List<ItemStack> things = carriers().getOrDefault(aspect, List.of());
        if (things.isEmpty()) {
            return;
        }
        int shown = Math.min(16, things.size());
        int first = things.size() > 16 ? (int) (ticks / 5 % things.size()) : 0;
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 300);
        for (int index = 0; index < shown; index++) {
            ItemStack thing = things.get((first + index) % things.size());
            int at = pointedX + 8 + (index % 8) * 17;
            int down = pointedY - (4 + shown / 8 * 8) + (index / 8) * 17;
            graphics.renderItem(thing, at, down);
            graphics.renderItemDecorations(font, thing, at, down);
        }
        graphics.pose().popPose();
    }

    private static java.util.Map<Aspect, List<ItemStack>> carriers() {
        if (carriers == null) {
            java.util.Map<Aspect, List<ItemStack>> found = new java.util.HashMap<>();
            for (var item : net.minecraft.core.registries.BuiltInRegistries.ITEM) {
                ItemStack stack = new ItemStack(item);
                AspectList carried = me.moonscenty.alchemia.aspect.Aspects.of(stack);
                for (Holder<Aspect> aspect : carried.sortedByName()) {
                    found.computeIfAbsent(aspect.value(), key -> new java.util.ArrayList<>())
                            .add(stack.copyWithCount(Math.max(1, carried.get(aspect))));
                }
            }
            carriers = found;
        }
        return carriers;
    }

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
            ItemStack shown = options[(int) (ticks / CYCLE_TICKS % options.length)];
            graphics.renderItem(shown, x, y);
            pointAt(shown, x, y);
        }
    }

    private void drawStack(GuiGraphics graphics, ItemStack stack, int x, int y) {
        graphics.renderItem(stack, x, y);
        graphics.renderItemDecorations(font, stack, x, y);
        pointAt(stack, x, y);
    }

    private void pointAt(ItemStack stack, int x, int y) {
        if (!stack.isEmpty() && over(x, y, ICON)) {
            tip = getTooltipFromItem(minecraft, stack);
        }
    }

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
        if (over(x, y, ICON)) {
            tip = List.of(aspect.value().displayName(),
                    aspect.value().description().copy().withStyle(net.minecraft.ChatFormatting.GRAY));
        }
    }

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
            turned();
            return true;
        }
        if (spread + 2 < pages.size() && overTurn(mouseX, mouseY, true)) {
            spread += 2;
            turned();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    // original's keys: left, up and page up go back; right, down and page down go on
    @Override
    public boolean keyPressed(int key, int scan, int modifiers) {
        if (minecraft.options.keyInventory.matches(key, scan)) {
            onClose();
            return true;
        }
        if ((key == org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT || key == org.lwjgl.glfw.GLFW.GLFW_KEY_UP
                || key == org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_UP) && spread > 0) {
            spread -= 2;
            turned();
            return true;
        }
        if ((key == org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT || key == org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN
                || key == org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_DOWN) && spread + 2 < pages.size()) {
            spread += 2;
            turned();
            return true;
        }
        return super.keyPressed(key, scan, modifiers);
    }

    private void turned() {
        if (minecraft.player != null) {
            minecraft.player.playSound(me.moonscenty.alchemia.registry.ModSounds.PAGE.get(), 0.66F, 1.0F);
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
