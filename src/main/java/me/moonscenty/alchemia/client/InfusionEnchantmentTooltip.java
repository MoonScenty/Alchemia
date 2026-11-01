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

@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public final class InfusionEnchantmentTooltip {
    private InfusionEnchantmentTooltip() {
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        Map<InfusionEnchantment, Integer> worked = InfusionEnchantments.on(event.getItemStack());
        worked.forEach((which, level) -> {
            net.minecraft.network.chat.MutableComponent line = Component.translatable(which.key());
            if (which.most() > 1) {
                line = line.append(" ").append(Component.translatable("enchantment.level." + level));
            }
            event.getToolTip().add(line.withStyle(ChatFormatting.AQUA));
        });
    }
}
