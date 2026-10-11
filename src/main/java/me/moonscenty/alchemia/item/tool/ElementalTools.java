package me.moonscenty.alchemia.item.tool;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import me.moonscenty.alchemia.enchantment.InfusionEnchantment;
import me.moonscenty.alchemia.registry.ModDataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

/**
 * What the elemental tools have in common: they are rare, and they come with an altar's workings already on them.
 *
 * <p>The workings are what each element <em>is</em> in a tool -- water carries things along, fire refines, air
 * sweeps, earth moves earth -- so they are put on as the item's own defaults rather than written onto a stack
 * somewhere after it is made. Every one of them, from the altar, the creative menu or anywhere else, has them.
 * An altar can still take one further; it can never take one away.
 */
public final class ElementalTools {
    /** As long as a button can be held down, which is what a tool used by holding is held for. */
    public static final int HELD_FOREVER = 72000;

    private ElementalTools() {
    }

    /** A rare tool carrying these workings from the moment it exists. */
    public static Item.Properties carrying(Item.Properties properties, Object... workings) {
        Map<InfusionEnchantment, Integer> built = new LinkedHashMap<>();
        for (int at = 0; at < workings.length; at += 2) {
            built.put((InfusionEnchantment) workings[at], (Integer) workings[at + 1]);
        }
        // unmodifiableMap rather than Map.copyOf, so the workings read back in the order they are written here
        return properties.rarity(Rarity.RARE)
                .component(ModDataComponents.INFUSION_ENCHANTMENTS.get(), Collections.unmodifiableMap(built));
    }
}
