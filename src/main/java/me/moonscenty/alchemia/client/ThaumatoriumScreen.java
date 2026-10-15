package me.moonscenty.alchemia.client;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.block.entity.ThaumatoriumBlockEntity;
import me.moonscenty.alchemia.crafting.CrucibleRecipe;
import me.moonscenty.alchemia.menu.ThaumatoriumMenu;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * The thaumatorium's panel, laid out as the original's: the catalyst slot top left, and beside it the recipe on
 * offer, with arrows to step through the others, and under them what it needs and how much of each is in.
 * <p>
 * Clicking the recipe chooses it, or unchooses it if it was chosen. A ring pulses round the recipe square while
 * anything is chosen; a recipe shown dim cannot be chosen until something else is let go of.
 */
public class ThaumatoriumScreen extends AbstractContainerScreen<ThaumatoriumMenu> {
    private static final ResourceLocation PANEL = Alchemia.id("textures/gui/thaumatorium.png");
    private static final int SHEET = 256;
    private static final int ICON = 16;
    private static final int SHOWN = 6;

    /** Which recipe on offer is being looked at, and how far along its aspects the row is scrolled. */
    private int index;
    private int firstAspect;
    private int offeredLast = -1;

    public ThaumatoriumScreen(ThaumatoriumMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    private List<RecipeHolder<CrucibleRecipe>> offered() {
        return menu.offered();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partial, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(PANEL, leftPos, topPos, 0, 0, imageWidth, imageHeight, SHEET, SHEET);
        List<RecipeHolder<CrucibleRecipe>> offered = offered();
        refresh(offered);
        if (offered.isEmpty()) {
            return;
        }
        ThaumatoriumBlockEntity it = menu.thaumatorium();
        List<ResourceLocation> chosen = it == null ? List.of() : it.recipes();
        RecipeHolder<CrucibleRecipe> shown = offered.get(index);
        boolean isChosen = chosen.contains(shown.id());

        if (offered.size() > 1) {
            graphics.blit(PANEL, leftPos + 128, topPos + 16, index > 0 ? 192 : 176, 16, 16, 8, SHEET, SHEET);
            graphics.blit(PANEL, leftPos + 128, topPos + 24, index < offered.size() - 1 ? 192 : 176, 24, 16, 8,
                    SHEET, SHEET);
        }
        List<Holder<Aspect>> aspects = shown.value().aspects().sortedByName();
        if (aspects.size() > SHOWN) {
            graphics.blit(PANEL, leftPos + 32, topPos + 40, firstAspect > 0 ? 192 : 176, 32, 8, 16, SHEET, SHEET);
            graphics.blit(PANEL, leftPos + 136, topPos + 40, firstAspect < aspects.size() - 1 ? 200 : 184, 32, 8, 16,
                    SHEET, SHEET);
        } else {
            firstAspect = 0;
        }
        if (!chosen.isEmpty()) {
            if (isChosen || over(mouseX, mouseY, 112, 16, 16, 16)) {
                graphics.blit(PANEL, leftPos + 104, topPos + 8, 176, 96, 48, 48, SHEET, SHEET);
            }
            float ticks = minecraft.player == null ? 0.0F : minecraft.player.tickCount;
            graphics.setColor(1.0F, 1.0F, 1.0F, Mth.clamp(0.6F + Mth.sin(ticks / 5.0F) * 0.4F + 0.4F, 0.0F, 1.0F));
            graphics.blit(PANEL, leftPos + 88, topPos + 16, 176, 56, 24, 24, SHEET, SHEET);
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        AspectList in = it == null ? AspectList.EMPTY : it.essentia();
        AspectList wants = shown.value().aspects();
        for (int place = 0; place < SHOWN && firstAspect + place < aspects.size(); place++) {
            Holder<Aspect> aspect = aspects.get(firstAspect + place);
            int x = leftPos + 40 + 16 * place;
            drawAspect(graphics, aspect, x, topPos + 40);
            graphics.drawString(font, String.valueOf(wants.get(aspect)), x + ICON - font.width(
                    String.valueOf(wants.get(aspect))), topPos + 40 + ICON - font.lineHeight + 1, 0xFFFFFF);
            if (isChosen) {
                RenderSystem.enableBlend();
                graphics.blit(PANEL, leftPos + 41 + 16 * place, topPos + 57, 176, 8, 14, 6, SHEET, SHEET);
                int width = (int) (Math.min(1.0F, in.get(aspect) / (float) wants.get(aspect)) * 12);
                int colour = aspect.value().color();
                graphics.setColor(((colour >> 16) & 0xFF) / 255F, ((colour >> 8) & 0xFF) / 255F,
                        (colour & 0xFF) / 255F, 1.0F);
                graphics.blit(PANEL, leftPos + 42 + 16 * place, topPos + 58, 176, 0, width, 4, SHEET, SHEET);
                graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }

        ItemStack output = shown.value().result();
        boolean full = it != null && chosen.size() >= it.remembers() && !isChosen;
        if (full) {
            graphics.setColor(1.0F, 0.5F, 0.5F, 0.5F);
        }
        graphics.renderItem(output, leftPos + 112, topPos + 16);
        graphics.renderItemDecorations(font, output, leftPos + 112, topPos + 16);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        if (it != null && it.remembers() > 1) {
            graphics.pose().pushPose();
            graphics.pose().translate(leftPos + 136, topPos + 33, 0);
            graphics.pose().scale(0.5F, 0.5F, 1.0F);
            String count = chosen.size() + "/" + it.remembers();
            graphics.drawString(font, count, -font.width(count) / 2, 0, 0xFFFFFF);
            graphics.pose().popPose();
        }
    }

    private void drawAspect(GuiGraphics graphics, Holder<Aspect> aspect, int x, int y) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        int colour = aspect.value().color();
        graphics.setColor(((colour >> 16) & 0xFF) / 255F, ((colour >> 8) & 0xFF) / 255F, (colour & 0xFF) / 255F, 1F);
        graphics.blit(aspect.value().icon(), x, y, 0, 0, ICON, ICON, ICON, ICON);
        graphics.setColor(1F, 1F, 1F, 1F);
    }

    /** When what is on offer changes, look at the first one already chosen, if any. */
    private void refresh(List<RecipeHolder<CrucibleRecipe>> offered) {
        if (offered.size() == offeredLast) {
            index = Mth.clamp(index, 0, Math.max(0, offered.size() - 1));
            return;
        }
        offeredLast = offered.size();
        index = 0;
        firstAspect = 0;
        ThaumatoriumBlockEntity it = menu.thaumatorium();
        if (it != null) {
            for (int i = 0; i < offered.size(); i++) {
                if (it.recipes().contains(offered.get(i).id())) {
                    index = i;
                    break;
                }
            }
        }
    }

    /** No words; the panel says what each part is. */
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        super.render(graphics, mouseX, mouseY, partial);
        List<RecipeHolder<CrucibleRecipe>> offered = offered();
        if (!offered.isEmpty() && index < offered.size() && over(mouseX, mouseY, 112, 16, 16, 16)) {
            graphics.renderTooltip(font, offered.get(index).value().result(), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        List<RecipeHolder<CrucibleRecipe>> offered = offered();
        if (!offered.isEmpty() && index < offered.size()) {
            ThaumatoriumBlockEntity it = menu.thaumatorium();
            RecipeHolder<CrucibleRecipe> shown = offered.get(index);
            List<Holder<Aspect>> aspects = shown.value().aspects().sortedByName();
            if (over(mouseX, mouseY, 112, 16, 16, 16) && it != null
                    && (it.recipes().size() < it.remembers() || it.recipes().contains(shown.id()))) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, index);
                click(ModSounds.HH_ON.get(), 0.3F);
                return true;
            }
            if (over(mouseX, mouseY, 128, 16, 16, 8) && index > 0) {
                index--;
                firstAspect = 0;
                click(ModSounds.CAMERA_CLACK.get(), 0.4F);
                return true;
            }
            if (over(mouseX, mouseY, 128, 24, 16, 8) && index < offered.size() - 1) {
                index++;
                firstAspect = 0;
                click(ModSounds.CAMERA_CLACK.get(), 0.4F);
                return true;
            }
            if (aspects.size() > SHOWN && over(mouseX, mouseY, 32, 40, 8, 16) && firstAspect > 0) {
                firstAspect--;
                click(ModSounds.CAMERA_CLACK.get(), 0.4F);
                return true;
            }
            if (aspects.size() > SHOWN && over(mouseX, mouseY, 136, 40, 8, 16)
                    && firstAspect < aspects.size() - 1) {
                firstAspect++;
                click(ModSounds.CAMERA_CLACK.get(), 0.4F);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void click(SoundEvent sound, float volume) {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(sound, 1.0F, volume));
    }

    private boolean over(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= leftPos + x && mouseX < leftPos + x + width
                && mouseY >= topPos + y && mouseY < topPos + y + height;
    }
}
