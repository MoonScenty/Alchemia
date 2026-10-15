package me.moonscenty.alchemia.client;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import me.moonscenty.alchemia.client.legacy.LegacyEdition;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * What the game shows instead of its title when a jar of the original is not in the {@code legacy} folder: which
 * one is wanted, a way to its download page on CurseForge, the folder to put it in, and the way out. The mod draws
 * with the original's pictures and models, so there is nothing to play until both are there.
 * <p>
 * The jars are not fetched for the player. The original's licence lets only its author hand it out, so the player
 * takes it from the author's own page.
 */
public class MissingJarsScreen extends Screen {
    private static final int BUTTON_WIDTH = 260;
    private static final int BUTTON_HEIGHT = 20;
    private static final int GAP = 4;

    private final Set<LegacyEdition> missing;
    private final Path folder;
    private MultiLineLabel message = MultiLineLabel.EMPTY;

    public MissingJarsScreen(Set<LegacyEdition> missing, Path folder) {
        super(Component.translatable("screen.alchemia.missing_jars.title"));
        this.missing = missing;
        this.folder = folder;
    }

    @Override
    protected void init() {
        message = MultiLineLabel.create(font, Component.translatable("screen.alchemia.missing_jars.message",
                folder.toAbsolutePath().toString()), width - 40);
        List<LegacyEdition> wanted = missing.stream().sorted().toList();
        int buttons = wanted.size() + 2;
        int y = height - 24 - buttons * (BUTTON_HEIGHT + GAP);
        int x = (width - BUTTON_WIDTH) / 2;
        for (LegacyEdition edition : wanted) {
            addRenderableWidget(Button.builder(
                            Component.translatable("screen.alchemia.missing_jars.download", edition.fileName()),
                            ConfirmLinkScreen.confirmLink(this, edition.downloadPage()))
                    .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
            y += BUTTON_HEIGHT + GAP;
        }
        addRenderableWidget(Button.builder(Component.translatable("screen.alchemia.missing_jars.folder"),
                        button -> Util.getPlatform().openPath(folder))
                .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        y += BUTTON_HEIGHT + GAP;
        addRenderableWidget(Button.builder(Component.translatable("menu.quit"),
                        button -> minecraft.stop())
                .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        super.render(graphics, mouseX, mouseY, partial);
        graphics.drawCenteredString(font, title.copy().withStyle(ChatFormatting.BOLD), width / 2, 20, 0xFFFFFF);
        message.renderCentered(graphics, width / 2, 40, font.lineHeight + 2, 0xD0D0D0);
        int y = 40 + message.getLineCount() * (font.lineHeight + 2) + 8;
        for (LegacyEdition edition : missing.stream().sorted().toList()) {
            graphics.drawCenteredString(font, Component.literal("- " + edition.fileName())
                    .withStyle(ChatFormatting.GOLD), width / 2, y, 0xFFFFFF);
            y += font.lineHeight + 2;
        }
    }

    /** There is nowhere to go back to: the game has nothing to show without the jars. */
    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
