package me.moonscenty.alchemia.enchantment;

import java.util.LinkedHashMap;
import java.util.Map;

import me.moonscenty.alchemia.registry.ModDataComponents;
import net.minecraft.world.item.ItemStack;

public final class InfusionEnchantments {
    private InfusionEnchantments() {
    }

    public static Map<InfusionEnchantment, Integer> on(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.INFUSION_ENCHANTMENTS.get(), Map.of());
    }

    public static int level(ItemStack stack, InfusionEnchantment which) {
        return on(stack).getOrDefault(which, 0);
    }

    public static boolean has(ItemStack stack, InfusionEnchantment which) {
        return level(stack, which) > 0;
    }

    // working the same one again raises its level; past the max the stack comes back unchanged
    public static ItemStack raised(ItemStack stack, InfusionEnchantment which) {
        int now = level(stack, which);
        if (now >= which.most()) {
            return stack.copy();
        }
        Map<InfusionEnchantment, Integer> all = new LinkedHashMap<>(on(stack));
        all.put(which, now + 1);
        ItemStack worked = stack.copy();
        // not Map.copyOf: insertion order matters
        worked.set(ModDataComponents.INFUSION_ENCHANTMENTS.get(), java.util.Collections.unmodifiableMap(all));
        return worked;
    }

    public static ItemStack atLeast(ItemStack stack, InfusionEnchantment which, int level) {
        if (level(stack, which) >= level) {
            return stack;
        }
        Map<InfusionEnchantment, Integer> all = new LinkedHashMap<>(on(stack));
        all.put(which, level);
        stack.set(ModDataComponents.INFUSION_ENCHANTMENTS.get(), java.util.Collections.unmodifiableMap(all));
        return stack;
    }

    public static boolean roomFor(ItemStack stack, InfusionEnchantment which) {
        return which.fits(stack) && level(stack, which) < which.most();
    }
}
