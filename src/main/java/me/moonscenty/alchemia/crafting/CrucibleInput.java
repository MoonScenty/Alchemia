package me.moonscenty.alchemia.crafting;

import me.moonscenty.alchemia.aspect.AspectList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * What a crucible has to offer a recipe: the thing that just fell in, and what was already dissolved in the water.
 */
public record CrucibleInput(ItemStack catalyst, AspectList dissolved) implements RecipeInput {
    @Override
    public ItemStack getItem(int slot) {
        return slot == 0 ? catalyst : ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 1;
    }
}
