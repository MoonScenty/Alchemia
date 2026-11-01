package me.moonscenty.alchemia.crafting;

import java.util.Optional;

import me.moonscenty.alchemia.aspect.AspectList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * Something made on an arcane workbench rather than a plain one.
 * <p>
 * The grid is the same three by three, so these are read and matched exactly like vanilla's. What they add is a
 * price in vis, taken from the wand lying on the bench, and the research the maker has to have done first.
 */
public interface ArcaneRecipe extends Recipe<CraftingInput> {
    /** What the work costs, in primal aspects. An empty list is a recipe anyone can make with an empty wand. */
    AspectList cost();

    /** What this particular grid costs. The same as {@link #cost()} unless what is on the grid sets the price. */
    default AspectList cost(net.minecraft.world.item.crafting.CraftingInput input) {
        return cost();
    }

    /** The research that has to be finished first, if any. */
    Optional<ResourceLocation> research();

    @Override
    default RecipeType<?> getType() {
        return ModRecipes.ARCANE.get();
    }

    /** Never true: these are laid out on a bench, not carried around in a crafting book. */
    @Override
    default boolean isSpecial() {
        return true;
    }

    /** Whether the grid is three by three with exactly these squares filled, counted across the rows from 0. */
    static boolean onlyFilled(CraftingInput input, int... squares) {
        if (input.width() != 3 || input.height() != 3) {
            return false;
        }
        for (int square = 0; square < 9; square++) {
            boolean used = false;
            for (int wanted : squares) {
                used |= wanted == square;
            }
            if (used == input.getItem(square).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /** The two kinds share a type, so the workbench only ever looks one recipe type up. */
    @Override
    RecipeSerializer<?> getSerializer();
}
