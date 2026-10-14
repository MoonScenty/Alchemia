package me.moonscenty.alchemia.compat.jei;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * A row of aspects in their colours, each with how much is wanted in its corner, as the research book draws them;
 * and which of them is under the mouse, for its name.
 */
final class AspectRow {
    static final int ICON = 16;
    static final int STEP = 18;

    private AspectRow() {
    }

    /** Draws the aspects from here along, as many to a row as fit in this width. */
    static void draw(GuiGraphics graphics, AspectList aspects, int x, int y, int across) {
        List<Holder<Aspect>> order = aspects.sortedByAmount();
        Font font = Minecraft.getInstance().font;
        for (int i = 0; i < order.size(); i++) {
            int ax = x + (i % across) * STEP;
            int ay = y + (i / across) * STEP;
            Holder<Aspect> aspect = order.get(i);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            int colour = aspect.value().color();
            graphics.setColor(((colour >> 16) & 0xFF) / 255F, ((colour >> 8) & 0xFF) / 255F, (colour & 0xFF) / 255F,
                    1F);
            graphics.blit(aspect.value().icon(), ax, ay, 0, 0, ICON, ICON, ICON, ICON);
            graphics.setColor(1F, 1F, 1F, 1F);
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 200);
            String count = String.valueOf(aspects.get(aspect));
            graphics.drawString(font, count, ax + ICON - font.width(count) + 1, ay + ICON - 7, 0xFFFFFFFF, true);
            graphics.pose().popPose();
        }
    }

    /** The name of the aspect under the mouse, if one is. */
    @Nullable
    static Component under(AspectList aspects, int x, int y, int across, double mouseX, double mouseY) {
        List<Holder<Aspect>> order = aspects.sortedByAmount();
        for (int i = 0; i < order.size(); i++) {
            int ax = x + (i % across) * STEP;
            int ay = y + (i / across) * STEP;
            if (mouseX >= ax && mouseX < ax + ICON && mouseY >= ay && mouseY < ay + ICON) {
                return order.get(i).value().displayName();
            }
        }
        return null;
    }
}
