package me.moonscenty.alchemia.client;

import java.util.Map;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.enchantment.InfusionEnchantment;
import me.moonscenty.alchemia.enchantment.InfusionEnchantments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * What an altar has put on a tool, written under its name.
 * <p>
 * Written here rather than by the item itself because these go on anything -- a vanilla pickaxe knows nothing
 * about us, and a tool that carries a working it cannot say it carries is a tool nobody will trust.
 */
@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public final class InfusionEnchantmentTooltip {
    private InfusionEnchantmentTooltip() {
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        Map<InfusionEnchantment, Integer> worked = InfusionEnchantments.on(event.getItemStack());
        worked.forEach((which, level) -> {
            // a working that goes no further than one says so by its name alone, as the game's own do
            net.minecraft.network.chat.MutableComponent line = Component.translatable(which.key());
            if (which.most() > 1) {
                line = line.append(" ").append(Component.translatable("enchantment.level." + level));
            }
            event.getToolTip().add(line.withStyle(ChatFormatting.AQUA));
        });
    }
}
