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

public final class WandParts {
    private static Map<Item, Holder<WandRod>> rods;
    private static Map<Item, Holder<WandCap>> caps;

    private WandParts() {
    }

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
