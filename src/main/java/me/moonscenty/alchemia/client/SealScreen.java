package me.moonscenty.alchemia.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.mojang.blaze3d.systems.RenderSystem;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.golem.GolemTrait;
import me.moonscenty.alchemia.golem.seal.Seal;
import me.moonscenty.alchemia.golem.seal.SealEntity;
import me.moonscenty.alchemia.menu.SealMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;

/**
 * A seal's panel as the original drew it: a round dial with the seal's pages as little buttons round its rim, the
 * redstone switch after them, and whichever page is open in the middle, with the player's pack below.
 *
 * <p>The pages are priority and locking (with the colour of golem it is set for), filter, area, options, and what
 * golems it wants or will not have. Only the seal's owner can lock it or set it to mind redstone.
 */
public class SealScreen extends AbstractContainerScreen<SealMenu> {
    private static final ResourceLocation BASE = Alchemia.id("textures/gui/base.png");
    private static final int SHEET = 256;
    private static final int PANEL_W = 176;
    private static final int PANEL_H = 232;
    private static final int MX = SealMenu.MIDDLE_X;
    private static final int MY = SealMenu.MIDDLE_Y;
    private static final int RIM = 86;
    private static final int WRAP = 150;

    /** One thing that can be clicked or hovered on the panel. */
    private record Spot(int x, int y, int wide, int tall, int id, Component tip) {
        boolean over(double mx, double my) {
            return mx >= x && mx < x + wide && my >= y && my < y + tall;
        }
    }

    private final List<Spot> spots = new ArrayList<>();

    public SealScreen(SealMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = PANEL_W;
        imageHeight = PANEL_H;
    }

    private SealEntity seal() {
        return menu.seal();
    }

    private boolean mine(SealEntity seal) {
        return Objects.equals(seal.owner(), minecraft.player.getUUID());
    }

    /** Where the n-th thing round the rim goes, as the original spread them over a sixth of the circle. */
    private int[] rim(int index, int count) {
        float slice = 60.0F / count;
        float start = -180.0F + (count - 1) * slice / 2.0F;
        slice = Mth.clamp(slice, 12.0F, 24.0F);
        float angle = (start - index * slice) / 180.0F * Mth.PI;
        return new int[] {(int) (Mth.cos(angle) * RIM), (int) (Mth.sin(angle) * RIM)};
    }

    /** Everything clickable on the open page, worked out afresh each frame. */
    private void layOut(SealEntity seal) {
        spots.clear();
        Seal kind = seal.seal();
        int[] categories = kind.categories();
        if (categories.length > 1) {
            for (int c = 0; c < categories.length; c++) {
                int[] at = rim(c, categories.length);
                spots.add(new Spot(MX + at[0] - 8, MY + at[1] - 8, 16, 16, c,
                        Component.translatable("gui.alchemia.seal.category." + categories[c])));
            }
        }
        int[] red = rim(categories.length, categories.length);
        spots.add(new Spot(MX + red[0] - 8, MY + red[1] - 8, 16, 16,
                seal.redstone() ? SealMenu.REDSTONE_OFF : SealMenu.REDSTONE_ON,
                Component.translatable(seal.redstone() ? "golem.alchemia.prop.redon" : "golem.alchemia.prop.redoff")));
        switch (menu.category()) {
            case Seal.PRIORITY -> {
                spots.add(new Spot(MX - 19, MY - 17, 10, 10, SealMenu.PRIORITY_DOWN, Component.empty()));
                spots.add(new Spot(MX + 9, MY - 17, 10, 10, SealMenu.PRIORITY_UP, Component.empty()));
                spots.add(new Spot(MX + 6, MY + 4, 10, 10, SealMenu.COLOUR_DOWN, Component.empty()));
                spots.add(new Spot(MX + 29, MY + 4, 10, 10, SealMenu.COLOUR_UP, Component.empty()));
                spots.add(new Spot(MX - 32, MY, 16, 16, seal.locked() ? SealMenu.UNLOCK : SealMenu.LOCK,
                        Component.translatable(seal.locked() ? "golem.alchemia.prop.lock"
                                : "golem.alchemia.prop.unlock")));
            }
            case Seal.FILTER -> {
                int size = kind.filterSize();
                int down = 16 + (size - 1) / 3 * 12;
                spots.add(new Spot(MX - 8, MY + (size - 1) / 3 * 24 - down + 27, 16, 16,
                        kind.blacklist() ? SealMenu.WHITELIST : SealMenu.BLACKLIST,
                        Component.translatable(kind.blacklist() ? "gui.alchemia.seal.blacklist"
                                : "gui.alchemia.seal.whitelist")));
            }
            case Seal.AREA -> {
                for (int row = 0; row < 3; row++) {
                    int y = MY - 25 + row * 25;
                    spots.add(new Spot(MX - 19, y, 10, 10, SealMenu.AREA + row * 2, Component.empty()));
                    spots.add(new Spot(MX + 9, y, 10, 10, SealMenu.AREA + row * 2 + 1, Component.empty()));
                }
            }
            case Seal.OPTIONS -> {
                List<Seal.Toggle> toggles = kind.toggles();
                int gap = toggles.size() < 4 ? 8 : toggles.size() < 6 ? 7 : toggles.size() < 9 ? 6 : 5;
                int height = (toggles.size() - 1) * gap;
                int half = halfWidest(toggles);
                for (int p = 0; p < toggles.size(); p++) {
                    Seal.Toggle toggle = toggles.get(p);
                    spots.add(new Spot(MX - half, MY - 5 - height + p * gap * 2, 8, 8,
                            toggle.value() ? SealMenu.TOGGLE_OFF + p : SealMenu.TOGGLE_ON + p, Component.empty()));
                }
            }
            default -> {
            }
        }
    }

    private int halfWidest(List<Seal.Toggle> toggles) {
        int half = 12;
        for (Seal.Toggle toggle : toggles) {
            half = Math.max(half, (12 + Math.min(100, font.width(Component.translatable(toggle.translationKey()))))
                    / 2);
        }
        return half;
    }

    // --- drawing ------------------------------------------------------------------------------------------

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        SealEntity seal = seal();
        if (seal == null) {
            return;
        }
        layOut(seal);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        blit(graphics, MX - 80, MY - 80, 96, 0, 160, 160);
        blit(graphics, 0, 143, 0, 167, 176, 89);
        graphics.drawCenteredString(font, Component.translatable("gui.alchemia.seal.category." + menu.category()),
                leftPos + MX, topPos + MY - 64, 0xFFFFFF);
        drawSpots(graphics, seal, mouseX - leftPos, mouseY - topPos);
        switch (menu.category()) {
            case Seal.PRIORITY -> priority(graphics, seal, mouseX - leftPos, mouseY - topPos);
            case Seal.FILTER -> filter(graphics, seal);
            case Seal.AREA -> area(graphics, seal);
            case Seal.OPTIONS -> options(graphics, seal);
            case Seal.TRAITS -> traits(graphics, seal);
            default -> {
            }
        }
        RenderSystem.disableBlend();
    }

    private void blit(GuiGraphics graphics, int x, int y, int u, int v, int wide, int tall) {
        RenderSystem.enableBlend();
        graphics.blit(BASE, leftPos + x, topPos + y, u, v, wide, tall, SHEET, SHEET);
    }

    private void drawSpots(GuiGraphics graphics, SealEntity seal, int mx, int my) {
        int[] categories = seal.seal().categories();
        for (Spot spot : spots) {
            boolean over = spot.over(mx, my);
            if (spot.id() < categories.length) {
                float shade = categories[spot.id()] == menu.category() || over ? 1.0F : 0.7F;
                graphics.setColor(shade, shade, shade, shade);
                blit(graphics, spot.x(), spot.y(), categories[spot.id()] * 16, 120, 16, 16);
            } else if (spot.id() == SealMenu.REDSTONE_ON || spot.id() == SealMenu.REDSTONE_OFF) {
                shade(graphics, over);
                blit(graphics, spot.x(), spot.y(), seal.redstone() ? 64 : 80, 136, 16, 16);
            } else if (spot.id() == SealMenu.LOCK || spot.id() == SealMenu.UNLOCK) {
                shade(graphics, over);
                blit(graphics, spot.x(), spot.y(), seal.locked() ? 32 : 48, 136, 16, 16);
            } else if (spot.id() == SealMenu.BLACKLIST || spot.id() == SealMenu.WHITELIST) {
                shade(graphics, over);
                blit(graphics, spot.x(), spot.y(), seal.seal().blacklist() ? 0 : 16, 136, 16, 16);
            } else if (spot.id() >= SealMenu.TOGGLE_ON && spot.id() < SealMenu.PRIORITY_DOWN) {
                continue;
            } else {
                float shade = over ? 1.0F : 0.9F;
                graphics.setColor(shade, shade, shade, shade);
                boolean down = spot.id() >= SealMenu.AREA ? (spot.id() - SealMenu.AREA) % 2 == 0
                        : spot.id() % 2 == 0;
                blit(graphics, spot.x(), spot.y(), down ? 0 : 10, 0, 10, 10);
            }
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private static void shade(GuiGraphics graphics, boolean over) {
        float shade = over ? 1.0F : 0.9F;
        graphics.setColor(shade, shade, shade, shade);
    }

    private void priority(GuiGraphics graphics, SealEntity seal, int mx, int my) {
        blit(graphics, MX + 17, MY + 3, 2, 18, 12, 12);
        if (seal.colour() >= 1) {
            int colour = DyeColor.byId(seal.colour() - 1).getMapColor().col;
            graphics.setColor((colour >> 16 & 0xFF) / 255.0F, (colour >> 8 & 0xFF) / 255.0F, (colour & 0xFF) / 255.0F,
                    1.0F);
            blit(graphics, MX + 20, MY + 6, 74, 31, 6, 6);
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
        if (mx >= MX + 5 && mx <= MX + 41 && my >= MY + 3 && my <= MY + 15) {
            Component said = seal.colour() >= 1
                    ? Component.translatable("golem.alchemia.prop.color", Component.translatable(
                            "color.minecraft." + DyeColor.byId(seal.colour() - 1).getName()))
                    : Component.translatable("golem.alchemia.prop.colorall");
            graphics.drawCenteredString(font, said, leftPos + MX + 23, topPos + MY + 17, 0xFFFFFF);
        }
        graphics.drawCenteredString(font, Component.translatable("golem.alchemia.prop.priority"), leftPos + MX,
                topPos + MY - 28, 0xBBAAFF);
        graphics.drawCenteredString(font, Integer.toString(seal.priority()), leftPos + MX, topPos + MY - 16, 0xFFFFFF);
        if (mine(seal)) {
            graphics.drawCenteredString(font, Component.translatable("golem.alchemia.prop.owner"), leftPos + MX,
                    topPos + MY + 32, 0xBBAAFF);
        }
    }

    private void filter(GuiGraphics graphics, SealEntity seal) {
        int size = seal.seal().filterSize();
        int across = 16 + (size - 1) % 3 * 12;
        int down = 16 + (size - 1) / 3 * 12;
        for (int at = 0; at < size; at++) {
            blit(graphics, MX + at % 3 * 24 - across, MY + at / 3 * 24 - down, 0, 56, 32, 32);
        }
    }

    /** What each square counts, drawn over its picture, where the seal counts at all. */
    private void counts(GuiGraphics graphics, SealEntity seal) {
        if (menu.category() != Seal.FILTER || !seal.seal().hasStacksizeLimiters()) {
            return;
        }
        for (int at = 0; at < seal.seal().filterSize(); at++) {
            if (!seal.seal().filter().get(at).isEmpty()) {
                var slot = menu.slots.get(at);
                String count = Integer.toString(seal.seal().limit(at));
                graphics.pose().pushPose();
                graphics.pose().translate(0.0F, 0.0F, 200.0F);
                graphics.drawString(font, count, leftPos + slot.x + 17 - font.width(count), topPos + slot.y + 9,
                        0xFFFFFF);
                graphics.pose().popPose();
            }
        }
    }

    private void area(GuiGraphics graphics, SealEntity seal) {
        String[] captions = {"gui.alchemia.seal.up_down", "gui.alchemia.seal.east_west",
                "gui.alchemia.seal.north_south"};
        int[] values = {seal.area().getY(), seal.area().getX(), seal.area().getZ()};
        for (int row = 0; row < 3; row++) {
            graphics.drawCenteredString(font, Component.translatable(captions[row]), leftPos + MX,
                    topPos + MY - 33 + row * 24, 0xDDDDDD);
            graphics.drawCenteredString(font, Integer.toString(values[row]), leftPos + MX, topPos + MY - 24 + row * 24,
                    0xFFFFFF);
        }
    }

    private void options(GuiGraphics graphics, SealEntity seal) {
        List<Seal.Toggle> toggles = seal.seal().toggles();
        int p = 0;
        for (Spot spot : spots) {
            if (spot.id() < SealMenu.TOGGLE_ON || spot.id() >= SealMenu.PRIORITY_DOWN) {
                continue;
            }
            Seal.Toggle toggle = toggles.get(p++);
            blit(graphics, spot.x() - 2, spot.y() - 2, 2, 18, 12, 12);
            if (toggle.value()) {
                blit(graphics, spot.x() - 2, spot.y() - 2, 18, 18, 12, 12);
            }
            graphics.drawString(font, Component.translatable(toggle.translationKey()), leftPos + spot.x() + 12,
                    topPos + spot.y(), 0xFFFFFF);
        }
    }

    private void traits(GuiGraphics graphics, SealEntity seal) {
        graphics.drawCenteredString(font, Component.translatable("gui.alchemia.seal.required"), leftPos + MX,
                topPos + MY - 26, 0xDDDDDD);
        graphics.drawCenteredString(font, Component.translatable("gui.alchemia.seal.forbidden"), leftPos + MX,
                topPos + MY + 6, 0xDDDDDD);
        icons(graphics, seal.seal().required(), MY - 8);
        icons(graphics, seal.seal().forbidden(), MY + 24);
    }

    private void icons(GuiGraphics graphics, List<GolemTrait> traits, int y) {
        for (int p = 0; p < traits.size(); p++) {
            RenderSystem.enableBlend();
            graphics.blit(traits.get(p).icon(), leftPos + MX + p * 18 - (traits.size() - 1) * 9, topPos + y, 0, 0, 16,
                    16, 16, 16);
        }
    }

    /** The panel is a picture; it carries no title of its own. */
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        SealEntity seal = seal();
        if (seal != null) {
            counts(graphics, seal);
            tips(graphics, seal, mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    /** The name of a page beside its button, and what the other buttons and the trait pictures mean. */
    private void tips(GuiGraphics graphics, SealEntity seal, int mouseX, int mouseY) {
        int mx = mouseX - leftPos;
        int my = mouseY - topPos;
        int[] categories = seal.seal().categories();
        for (Spot spot : spots) {
            if (!spot.over(mx, my) || spot.tip().getString().isEmpty()) {
                continue;
            }
            if (spot.id() < categories.length) {
                graphics.drawString(font, spot.tip(), leftPos + spot.x() - 2 - font.width(spot.tip()),
                        topPos + spot.y() + 4, 0xFFFFFF);
            } else {
                graphics.renderTooltip(font, font.split(spot.tip(), WRAP), mouseX, mouseY);
            }
            return;
        }
        if (menu.category() == Seal.TRAITS) {
            hoverTraits(graphics, seal.seal().required(), MY - 8, mx, my, mouseX, mouseY);
            hoverTraits(graphics, seal.seal().forbidden(), MY + 24, mx, my, mouseX, mouseY);
        }
    }

    private void hoverTraits(GuiGraphics graphics, List<GolemTrait> traits, int y, int mx, int my, int mouseX,
            int mouseY) {
        for (int p = 0; p < traits.size(); p++) {
            int x = MX + p * 18 - (traits.size() - 1) * 9;
            if (mx >= x && mx < x + 16 && my >= y && my < y + 16) {
                List<FormattedCharSequence> lines = new ArrayList<>();
                lines.add(Component.translatable(traits.get(p).translationKey()).withStyle(ChatFormatting.WHITE)
                        .getVisualOrderText());
                lines.addAll(font.split(Component.translatable(traits.get(p).descriptionKey())
                        .withStyle(ChatFormatting.GRAY), WRAP));
                graphics.renderTooltip(font, lines, mouseX, mouseY);
            }
        }
    }

    // --- clicking -----------------------------------------------------------------------------------------

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        SealEntity seal = seal();
        if (seal != null) {
            int[] categories = seal.seal().categories();
            for (Spot spot : spots) {
                if (!spot.over(mouseX - leftPos, mouseY - topPos)) {
                    continue;
                }
                int id = spot.id();
                if (id < categories.length) {
                    if (categories[id] == menu.category()) {
                        return true;
                    }
                    menu.setCategory(categories[id]);
                } else if ((id == SealMenu.LOCK || id == SealMenu.UNLOCK || id == SealMenu.REDSTONE_ON
                        || id == SealMenu.REDSTONE_OFF) && !mine(seal)) {
                    return true;
                }
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /** The number keys do nothing here, as in the original: there is nothing on the panel to swap into. */
    @Override
    protected boolean checkHotbarKeyPressed(int keyCode, int scanCode) {
        return false;
    }
}
