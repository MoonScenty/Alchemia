package me.moonscenty.alchemia.wand;

import java.util.HashMap;
import java.util.Map;

import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModWandParts;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Which item is which piece of a wand.
 * <p>
 * The plain wooden rod is a vanilla stick, as it was in the original. Every other piece has an item of its own,
 * wearing the same picture it wears on a finished wand.
 */
public final class WandParts {
    private static Map<Item, Holder<WandRod>> rods;
    private static Map<Item, Holder<WandCap>> caps;

    private WandParts() {
    }

    /** The rod an item would make, or nothing if it would make none. */
    public static Holder<WandRod> rodOf(ItemStack stack) {
        return rodItems().get(stack.getItem());
    }

    public static Holder<WandCap> capOf(ItemStack stack) {
        return capItems().get(stack.getItem());
    }

    private static Map<Item, Holder<WandRod>> rodItems() {
        if (rods == null) {
            rods = byItem(ModWandParts.ROD_ENTRIES, ModItems.WAND_RODS);
            rods.put(Items.STICK, ModWandParts.WOOD);
        }
        return rods;
    }

    private static Map<Item, Holder<WandCap>> capItems() {
        if (caps == null) {
            caps = byItem(ModWandParts.CAP_ENTRIES, ModItems.WAND_CAPS);
        }
        return caps;
    }

    /** Each part matched to the item registered under the same name. */
    private static <T> Map<Item, Holder<T>> byItem(DeferredRegister<T> parts, Map<String, DeferredItem<Item>> items) {
        Map<Item, Holder<T>> map = new HashMap<>();
        for (DeferredHolder<T, ? extends T> part : parts.getEntries()) {
            DeferredItem<Item> item = items.get(part.getId().getPath());
            if (item != null) {
                map.put(item.get(), part);
            }
        }
        return map;
    }
}
