package me.moonscenty.alchemia.item.tool;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import me.moonscenty.alchemia.enchantment.InfusionEnchantment;
import me.moonscenty.alchemia.registry.ModDataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

// built-in infusion enchantments are item defaults, so every copy has them
public final class ElementalTools {
    public static final int HELD_FOREVER = 72000;

    private ElementalTools() {
    }

    public static Item.Properties carrying(Item.Properties properties, Object... workings) {
        Map<InfusionEnchantment, Integer> built = new LinkedHashMap<>();
        for (int at = 0; at < workings.length; at += 2) {
            built.put((InfusionEnchantment) workings[at], (Integer) workings[at + 1]);
        }
        // not Map.copyOf: insertion order matters
        return properties.rarity(Rarity.RARE)
                .component(ModDataComponents.INFUSION_ENCHANTMENTS.get(), Collections.unmodifiableMap(built));
    }
}
