package me.moonscenty.alchemia.crafting;

import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

// Ring order is arbitrary (pedestals are found by walking outwards); nothing may depend on it.
public record InfusionInput(ItemStack central, List<ItemStack> ring) implements RecipeInput {
    @Override
    public ItemStack getItem(int slot) {
        return slot == 0 ? central : ring.get(slot - 1);
    }

    @Override
    public int size() {
        return 1 + ring.size();
    }
}
