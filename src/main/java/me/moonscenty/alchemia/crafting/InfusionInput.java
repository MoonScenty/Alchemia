package me.moonscenty.alchemia.crafting;

import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * What a matrix has to offer a recipe: the thing on the pedestal under it, and what is standing on the ring.
 * <p>
 * The ring has no order to it. Pedestals are found by walking outwards from the matrix, so the same ring gathered
 * twice can come back in a different order, and anything that cared about that order would work on a Tuesday and
 * not on a Wednesday.
 *
 * @param central what is being worked on, which stays where it is until the work is done
 * @param ring what is being worked into it, one thing to a pedestal
 */
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
