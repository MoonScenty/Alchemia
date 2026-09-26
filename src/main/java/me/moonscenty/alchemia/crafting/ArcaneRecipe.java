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

    /** The two kinds share a type, so the workbench only ever looks one recipe type up. */
    @Override
    RecipeSerializer<?> getSerializer();
}
