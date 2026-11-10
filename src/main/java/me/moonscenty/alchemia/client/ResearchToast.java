package me.moonscenty.alchemia.client;

import me.moonscenty.alchemia.research.ModResearch;
import me.moonscenty.alchemia.research.ResearchEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ResearchToast implements Toast {
    private static final ResourceLocation BACKGROUND = ResourceLocation.withDefaultNamespace("toast/advancement");
    private static final long SHOWN_FOR = 5000L;

    private final ResourceLocation research;
    private final ItemStack icon;

    private ResearchToast(ResourceLocation research, ItemStack icon) {
        this.research = research;
        this.icon = icon;
    }

    public static void show(ResourceLocation research) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        ResearchEntry entry = minecraft.level.registryAccess().registryOrThrow(ModResearch.ENTRY_KEY).get(research);
        minecraft.getToasts().addToast(new ResearchToast(research, entry == null ? ItemStack.EMPTY : entry.iconStack()));
    }

    @Override
    public Visibility render(GuiGraphics graphics, ToastComponent toasts, long shownFor) {
        graphics.blitSprite(BACKGROUND, 0, 0, width(), height());
        var font = toasts.getMinecraft().font;
        graphics.drawString(font, Component.translatable("research.alchemia.complete"), 30, 7, 0xFFC060FF, false);
        graphics.drawString(font, ResearchEntry.displayName(research), 30, 18, 0xFFFFFFFF, false);
        graphics.renderFakeItem(icon, 8, 8);
        return shownFor >= SHOWN_FOR * toasts.getNotificationDisplayTimeMultiplier() ? Visibility.HIDE : Visibility.SHOW;
    }
}
