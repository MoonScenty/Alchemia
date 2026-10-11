package me.moonscenty.alchemia.enchantment;

import java.util.LinkedHashMap;
import java.util.Map;

import me.moonscenty.alchemia.registry.ModDataComponents;
import net.minecraft.world.item.ItemStack;

/** Reading and writing what an altar has put on a tool. */
public final class InfusionEnchantments {
    private InfusionEnchantments() {
    }

    /** Everything on it, in the order it was put there. Empty for anything that has never been under a matrix. */
    public static Map<InfusionEnchantment, Integer> on(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.INFUSION_ENCHANTMENTS.get(), Map.of());
    }

    /** How far this one has been taken on that, or nothing at all. */
    public static int level(ItemStack stack, InfusionEnchantment which) {
        return on(stack).getOrDefault(which, 0);
    }

    public static boolean has(ItemStack stack, InfusionEnchantment which) {
        return level(stack, which) > 0;
    }

    /**
     * The same tool with one of these raised by a step.
     * <p>
     * Raised rather than added: a tool worked twice for the same thing carries it once, deeper. Past what the
     * thing can be taken to, the tool comes back as it went in -- the altar will not sell the same step twice.
     */
    public static ItemStack raised(ItemStack stack, InfusionEnchantment which) {
        int now = level(stack, which);
        if (now >= which.most()) {
            return stack.copy();
        }
        Map<InfusionEnchantment, Integer> all = new LinkedHashMap<>(on(stack));
        all.put(which, now + 1);
        ItemStack worked = stack.copy();
        // unmodifiableMap rather than Map.copyOf, which would hand the workings back in an order of its own
        worked.set(ModDataComponents.INFUSION_ENCHANTMENTS.get(), java.util.Collections.unmodifiableMap(all));
        return worked;
    }

    /**
     * The same tool with at least this much of one of these on it.
     * <p>
     * For the tools that come with their workings built in: the original put them back every few seconds if
     * anything had taken them off, and never lowered one that had been worked higher.
     */
    public static ItemStack atLeast(ItemStack stack, InfusionEnchantment which, int level) {
        if (level(stack, which) >= level) {
            return stack;
        }
        Map<InfusionEnchantment, Integer> all = new LinkedHashMap<>(on(stack));
        all.put(which, level);
        stack.set(ModDataComponents.INFUSION_ENCHANTMENTS.get(), java.util.Collections.unmodifiableMap(all));
        return stack;
    }

    /** Whether an altar has anything left to do for this one on that tool. */
    public static boolean roomFor(ItemStack stack, InfusionEnchantment which) {
        return which.fits(stack) && level(stack, which) < which.most();
    }
}
