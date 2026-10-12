package me.moonscenty.alchemia.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.mojang.blaze3d.systems.RenderSystem;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.block.entity.GolemPressBlockEntity;
import me.moonscenty.alchemia.golem.GolemDesign;
import me.moonscenty.alchemia.golem.GolemParts;
import me.moonscenty.alchemia.golem.GolemTrait;
import me.moonscenty.alchemia.menu.GolemPressMenu;
import me.moonscenty.alchemia.network.StartGolem;
import me.moonscenty.alchemia.registry.ModAspects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * The golem press as the original laid it out: the material at top left and the fitting below it, the head, arms
 * and legs down the right, what the golem would be like in the middle, and what it takes along the far right.
 *
 * <p>Only the parts the player has the research for are offered, and the arrows step through them. Underneath are
 * how many hearts, how much armour and how hard a hit the golem would have. The button to press it lights only when
 * everything it takes is in the player's pack; what is missing is marked. While the press is at work nothing can be
 * changed, and a bar fills as it goes.
 */
public class GolemPressScreen extends AbstractContainerScreen<GolemPressMenu> {
    private static final ResourceLocation PANEL = Alchemia.id("textures/gui/golem_press.png");
    private static final ResourceLocation ARROWS = Alchemia.id("textures/gui/base.png");
    private static final ResourceLocation GOLEM = Alchemia.id("textures/item/legacy/golem.png");
    private static final int SHEET = 256;
    private static final int PANEL_W = 208;
    private static final int PANEL_H = 224;
    private static final int ICON = 16;
    private static final int ARROW = 10;
    private static final int WRAP = 150;

    /** What was picked last time, kept between openings as the original kept it. */
    private static int material;
    private static int head;
    private static int arms;
    private static int legs;
    private static int addon;

    private final List<GolemParts.Material> materials = new ArrayList<>();
    private final List<GolemParts.Head> heads = new ArrayList<>();
    private final List<GolemParts.Arms> armsKnown = new ArrayList<>();
    private final List<GolemParts.Legs> legsKnown = new ArrayList<>();
    private final List<GolemParts.Addon> addons = new ArrayList<>();
    private GolemDesign design = GolemDesign.BASIC;
    private List<ItemStack> components = List.of();
    private boolean[] owned = new boolean[0];
    private boolean everything;
    /** Set when the press is asked to start, until it has. */
    private boolean asked;

    public GolemPressScreen(GolemPressMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = PANEL_W;
        imageHeight = PANEL_H;
    }

    @Override
    protected void init() {
        super.init();
        known(materials, GolemParts.Material.values());
        known(heads, GolemParts.Head.values());
        known(armsKnown, GolemParts.Arms.values());
        known(legsKnown, GolemParts.Legs.values());
        known(addons, GolemParts.Addon.values());
        material = within(material, materials);
        head = within(head, heads);
        arms = within(arms, armsKnown);
        legs = within(legs, legsKnown);
        addon = within(addon, addons);
        choose();
    }

    private <T extends GolemParts.Part> void known(List<T> into, T[] all) {
        into.clear();
        Arrays.stream(all).filter(part -> part.known(minecraft.player)).forEach(into::add);
    }

    private static int within(int index, List<?> of) {
        return index >= of.size() || index < 0 ? 0 : index;
    }

    /** The golem as picked. Anything with nothing known stays as the plainest. */
    private void choose() {
        GolemDesign picked = GolemDesign.BASIC;
        if (!materials.isEmpty()) {
            picked = picked.withMaterial(materials.get(material));
        }
        if (!heads.isEmpty()) {
            picked = picked.withHead(heads.get(head));
        }
        if (!armsKnown.isEmpty()) {
            picked = picked.withArms(armsKnown.get(arms));
        }
        if (!legsKnown.isEmpty()) {
            picked = picked.withLegs(legsKnown.get(legs));
        }
        if (!addons.isEmpty()) {
            picked = picked.withAddon(addons.get(addon));
        }
        design = picked;
        components = design.components();
        count();
    }

    /** Whether the player has each thing it takes, and so whether it can be pressed. */
    private void count() {
        owned = new boolean[components.size()];
        everything = true;
        for (int at = 0; at < components.size(); at++) {
            ItemStack need = components.get(at);
            owned[at] = GolemPressBlockEntity.carried(minecraft.player, need) >= need.getCount();
            everything &= owned[at];
        }
    }

    private boolean busy() {
        return menu.cost() > 0;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        count();
        if (busy()) {
            asked = false;
        }
    }

    // --- drawing ------------------------------------------------------------------------------------------

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(PANEL, leftPos, topPos, 0, 0, PANEL_W, PANEL_H, SHEET, SHEET);
        if (!components.isEmpty()) {
            graphics.setColor(1.0F, 1.0F, 1.0F, 0.5F);
            int row = 1;
            int column = 0;
            for (int at = 0; at < components.size(); at++) {
                if (!owned[at]) {
                    graphics.blit(PANEL, leftPos + 144 + column * 16, topPos + 16 + 16 * row, 240, 0, 16, 16, SHEET,
                            SHEET);
                }
                if (++row > 3) {
                    row = 0;
                    column++;
                }
            }
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
        if (menu.cost() > 0 && menu.maxCost() > 0) {
            int done = (int) (46.0F * (1.0F - menu.cost() / (float) menu.maxCost()));
            graphics.blit(PANEL, leftPos + 145, topPos + 89, 209, 89, done, 6, SHEET, SHEET);
        }
        for (int[] frame : new int[][] {{12, 12}, {12, 60}, {108, 12}, {108, 36}, {108, 60}}) {
            graphics.blit(PANEL, leftPos + frame[0], topPos + frame[1], 228, 124, 24, 24, SHEET, SHEET);
        }
        arrows(graphics, mouseX, mouseY);
        icons(graphics);
        traits(graphics);
        wants(graphics);
        craftButton(graphics, mouseX, mouseY);
        stats(graphics);
        RenderSystem.disableBlend();
    }

    private void arrows(GuiGraphics graphics, int mouseX, int mouseY) {
        for (Arrow arrow : Arrow.values()) {
            if (arrow.choices(this) > 1) {
                boolean over = over(mouseX, mouseY, arrow.x, arrow.y, ARROW, ARROW);
                float shade = busy() ? 0.5F : over ? 1.0F : 0.9F;
                graphics.setColor(shade, shade, shade, shade);
                graphics.blit(ARROWS, leftPos + arrow.x, topPos + arrow.y, arrow.back ? 20 : 30, 0, ARROW, ARROW,
                        SHEET, SHEET);
                graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
    }

    private void icons(GuiGraphics graphics) {
        int colour = design.material().colour();
        graphics.setColor((colour >> 16 & 0xFF) / 255.0F, (colour >> 8 & 0xFF) / 255.0F, (colour & 0xFF) / 255.0F,
                1.0F);
        graphics.blit(GOLEM, leftPos + 24, topPos + 24, 0, 0, ICON, ICON, ICON, ICON);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        icon(graphics, design.head().icon(), 120, 24);
        icon(graphics, design.arms().icon(), 120, 48);
        icon(graphics, design.legs().icon(), 120, 72);
        if (design.addon() != GolemParts.Addon.NONE) {
            icon(graphics, design.addon().icon(), 24, 72);
        }
    }

    private void icon(GuiGraphics graphics, ResourceLocation icon, int x, int y) {
        RenderSystem.enableBlend();
        graphics.blit(icon, leftPos + x, topPos + y, 0, 0, ICON, ICON, ICON, ICON);
    }

    /** Where the trait tags go, round the middle of the panel, four to a column. */
    private List<int[]> traitPlaces() {
        int count = design.traits().size();
        List<int[]> places = new ArrayList<>();
        if (count == 0) {
            return places;
        }
        int down = count <= 4 ? (count - 1) % 4 * 8 : 24;
        int across = (count - 1) / 4 % 4 * 8;
        int row = 0;
        int column = 0;
        for (int at = 0; at < count; at++) {
            places.add(new int[] {72 + column * 16 - across, 48 + 16 * row - down});
            if (++row > 3) {
                row = 0;
                column++;
            }
        }
        return places;
    }

    private void traits(GuiGraphics graphics) {
        List<GolemTrait> traits = new ArrayList<>(design.traits());
        List<int[]> places = traitPlaces();
        for (int at = 0; at < traits.size(); at++) {
            icon(graphics, traits.get(at).icon(), places.get(at)[0], places.get(at)[1]);
        }
    }

    /** Where each thing it takes goes: the machina first, then four to a column. */
    private List<int[]> wantPlaces() {
        List<int[]> places = new ArrayList<>();
        int row = 1;
        int column = 0;
        for (int at = 0; at < components.size(); at++) {
            places.add(new int[] {152 + column * 16, 24 + 16 * row});
            if (++row > 3) {
                row = 0;
                column++;
            }
        }
        return places;
    }

    private void wants(GuiGraphics graphics) {
        if (components.isEmpty()) {
            return;
        }
        Aspect machina = ModAspects.MECHANISM.value();
        int colour = machina.color();
        graphics.setColor((colour >> 16 & 0xFF) / 255.0F, (colour >> 8 & 0xFF) / 255.0F, (colour & 0xFF) / 255.0F,
                1.0F);
        icon(graphics, machina.icon(), 152, 24);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        String cost = Integer.toString(design.cost());
        graphics.drawString(font, cost, leftPos + 162 - font.width(cost), topPos + 24, 0xFFFFFF);
        List<int[]> places = wantPlaces();
        for (int at = 0; at < components.size(); at++) {
            int x = leftPos + places.get(at)[0];
            int y = topPos + places.get(at)[1];
            graphics.renderItem(components.get(at), x, y);
            graphics.renderItemDecorations(font, components.get(at), x, y);
        }
    }

    private void craftButton(GuiGraphics graphics, int mouseX, int mouseY) {
        boolean on = everything && !busy() && !asked;
        float shade = on && over(mouseX, mouseY, 120, 104, 24, 16) ? 1.0F : 0.9F;
        RenderSystem.enableBlend();
        graphics.setColor(shade, shade, shade, shade);
        graphics.blit(PANEL, leftPos + 120, topPos + 104, 216, 64, 24, 16, SHEET, SHEET);
        if (!on) {
            graphics.blit(PANEL, leftPos + 120, topPos + 104, 216, 40, 24, 16, SHEET, SHEET);
        }
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /** Hearts, armour and a hit, as the golem would come off the press. */
    private void stats(GuiGraphics graphics) {
        int health = 10 + design.material().health();
        if (design.has(GolemTrait.FRAGILE)) {
            health = (int) (health * 0.75);
        }
        graphics.drawCenteredString(font, Float.toString(health / 2.0F), leftPos + 48, topPos + 108, 0xFFFFFF);
        graphics.drawCenteredString(font, Float.toString(design.armour() / 2.0F), leftPos + 72, topPos + 108,
                0xFFFFFF);
        graphics.drawCenteredString(font, Float.toString((float) (design.damage() / 2.0)), leftPos + 97,
                topPos + 108, 0xFFFFFF);
    }

    /** The panel is a picture of the press, as the original's was; it carries no words. */
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    /** What every picture on the panel is, and for parts and traits, what they do. */
    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        for (int[] spot : new int[][] {{24, 24, 0}, {120, 24, 1}, {120, 48, 2}, {120, 72, 3}, {24, 72, 4}}) {
            if (over(mouseX, mouseY, spot[0], spot[1], ICON, ICON)) {
                GolemParts.Part part = design.parts().get(spot[2]);
                if (part != GolemParts.Addon.NONE) {
                    explain(graphics, Component.translatable(part.translationKey()),
                            Component.translatable(part.descriptionKey()), mouseX, mouseY);
                    return;
                }
            }
        }
        List<GolemTrait> traits = new ArrayList<>(design.traits());
        List<int[]> places = traitPlaces();
        for (int at = 0; at < traits.size(); at++) {
            if (over(mouseX, mouseY, places.get(at)[0], places.get(at)[1], ICON, ICON)) {
                explain(graphics, Component.translatable(traits.get(at).translationKey()),
                        Component.translatable(traits.get(at).descriptionKey()), mouseX, mouseY);
                return;
            }
        }
        if (!components.isEmpty() && over(mouseX, mouseY, 152, 24, ICON, ICON)) {
            Aspect machina = ModAspects.MECHANISM.value();
            explain(graphics, machina.displayName(), machina.description(), mouseX, mouseY);
            return;
        }
        List<int[]> wanted = wantPlaces();
        for (int at = 0; at < components.size(); at++) {
            if (over(mouseX, mouseY, wanted.get(at)[0], wanted.get(at)[1], ICON, ICON)) {
                graphics.renderTooltip(font, components.get(at), mouseX, mouseY);
                return;
            }
        }
        super.renderTooltip(graphics, mouseX, mouseY);
    }

    private void explain(GuiGraphics graphics, Component name, Component text, int mouseX, int mouseY) {
        List<FormattedCharSequence> lines = new ArrayList<>();
        lines.add(name.copy().withStyle(ChatFormatting.WHITE).getVisualOrderText());
        lines.addAll(font.split(text.copy().withStyle(ChatFormatting.GRAY), WRAP));
        graphics.renderTooltip(font, lines, mouseX, mouseY);
    }

    // --- clicking -----------------------------------------------------------------------------------------

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!busy() && !asked) {
            for (Arrow arrow : Arrow.values()) {
                if (arrow.choices(this) > 1 && over(mouseX, mouseY, arrow.x, arrow.y, ARROW, ARROW)) {
                    arrow.step(this);
                    choose();
                    click();
                    return true;
                }
            }
            if (everything && over(mouseX, mouseY, 120, 104, 24, 16)) {
                PacketDistributor.sendToServer(new StartGolem(design));
                asked = true;
                click();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void click() {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private boolean over(double mouseX, double mouseY, int x, int y, int wide, int tall) {
        return mouseX >= leftPos + x && mouseX < leftPos + x + wide && mouseY >= topPos + y
                && mouseY < topPos + y + tall;
    }

    /** The ten little arrows, a pair to each choice, where the original put them. */
    private enum Arrow {
        HEAD_BACK(101, 19, true, screen -> screen.heads.size(), screen -> head = wrap(head - 1, screen.heads)),
        HEAD_ON(129, 19, false, screen -> screen.heads.size(), screen -> head = wrap(head + 1, screen.heads)),
        MATERIAL_BACK(5, 19, true, screen -> screen.materials.size(),
                screen -> material = wrap(material - 1, screen.materials)),
        MATERIAL_ON(33, 19, false, screen -> screen.materials.size(),
                screen -> material = wrap(material + 1, screen.materials)),
        ARMS_BACK(101, 43, true, screen -> screen.armsKnown.size(),
                screen -> arms = wrap(arms - 1, screen.armsKnown)),
        ARMS_ON(129, 43, false, screen -> screen.armsKnown.size(), screen -> arms = wrap(arms + 1, screen.armsKnown)),
        LEGS_BACK(101, 67, true, screen -> screen.legsKnown.size(),
                screen -> legs = wrap(legs - 1, screen.legsKnown)),
        LEGS_ON(129, 67, false, screen -> screen.legsKnown.size(), screen -> legs = wrap(legs + 1, screen.legsKnown)),
        ADDON_BACK(5, 67, true, screen -> screen.addons.size(), screen -> addon = wrap(addon - 1, screen.addons)),
        ADDON_ON(33, 67, false, screen -> screen.addons.size(), screen -> addon = wrap(addon + 1, screen.addons));

        final int x;
        final int y;
        final boolean back;
        private final Function<GolemPressScreen, Integer> choices;
        private final java.util.function.Consumer<GolemPressScreen> step;

        Arrow(int x, int y, boolean back, Function<GolemPressScreen, Integer> choices,
                java.util.function.Consumer<GolemPressScreen> step) {
            this.x = x;
            this.y = y;
            this.back = back;
            this.choices = choices;
            this.step = step;
        }

        int choices(GolemPressScreen screen) {
            return choices.apply(screen);
        }

        void step(GolemPressScreen screen) {
            step.accept(screen);
        }

        private static int wrap(int index, List<?> of) {
            return Math.floorMod(index, Math.max(1, of.size()));
        }
    }
}
