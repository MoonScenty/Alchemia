package me.moonscenty.alchemia.wand;

import java.util.HashMap;
import java.util.Map;

import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModWandParts;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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
            rods = new HashMap<>();
            rods.put(Items.STICK, ModWandParts.WOOD);
            rods.put(ModItems.WAND_RODS.get("greatwood").get(), ModWandParts.GREATWOOD);
            rods.put(ModItems.WAND_RODS.get("silverwood").get(), ModWandParts.SILVERWOOD);
            rods.put(ModItems.WAND_RODS.get("reed").get(), ModWandParts.REED);
            rods.put(ModItems.WAND_RODS.get("obsidian").get(), ModWandParts.OBSIDIAN);
            rods.put(ModItems.WAND_RODS.get("blaze").get(), ModWandParts.BLAZE);
            rods.put(ModItems.WAND_RODS.get("ice").get(), ModWandParts.ICE);
            rods.put(ModItems.WAND_RODS.get("quartz").get(), ModWandParts.QUARTZ);
            rods.put(ModItems.WAND_RODS.get("bone").get(), ModWandParts.BONE);
            rods.put(ModItems.WAND_RODS.get("greatwood_staff").get(), ModWandParts.GREATWOOD_STAFF);
            rods.put(ModItems.WAND_RODS.get("silverwood_staff").get(), ModWandParts.SILVERWOOD_STAFF);
            rods.put(ModItems.WAND_RODS.get("reed_staff").get(), ModWandParts.REED_STAFF);
            rods.put(ModItems.WAND_RODS.get("obsidian_staff").get(), ModWandParts.OBSIDIAN_STAFF);
            rods.put(ModItems.WAND_RODS.get("blaze_staff").get(), ModWandParts.BLAZE_STAFF);
            rods.put(ModItems.WAND_RODS.get("ice_staff").get(), ModWandParts.ICE_STAFF);
            rods.put(ModItems.WAND_RODS.get("quartz_staff").get(), ModWandParts.QUARTZ_STAFF);
            rods.put(ModItems.WAND_RODS.get("bone_staff").get(), ModWandParts.BONE_STAFF);
            rods.put(ModItems.WAND_RODS.get("primal_staff").get(), ModWandParts.PRIMAL_STAFF);
        }
        return rods;
    }

    private static Map<Item, Holder<WandCap>> capItems() {
        if (caps == null) {
            caps = new HashMap<>();
            caps.put(ModItems.WAND_CAPS.get("iron").get(), ModWandParts.IRON);
            caps.put(ModItems.WAND_CAPS.get("gold").get(), ModWandParts.GOLD);
            caps.put(ModItems.WAND_CAPS.get("brass").get(), ModWandParts.BRASS);
            caps.put(ModItems.WAND_CAPS.get("alchemium").get(), ModWandParts.ALCHEMIUM);
            caps.put(ModItems.WAND_CAPS.get("void").get(), ModWandParts.VOID);
        }
        return caps;
    }
}
