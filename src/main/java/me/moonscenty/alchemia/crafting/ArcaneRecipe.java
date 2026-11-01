package me.moonscenty.alchemia.crafting;

import java.util.Optional;

import me.moonscenty.alchemia.aspect.AspectList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.RecipeSerializer;

public interface ArcaneRecipe extends Recipe<CraftingInput> {
    AspectList cost();

    default AspectList cost(net.minecraft.world.item.crafting.CraftingInput input) {
        return cost();
    }

    Optional<ResourceLocation> research();

    @Override
    default RecipeType<?> getType() {
        return ModRecipes.ARCANE.get();
    }

    @Override
    default boolean isSpecial() {
        return true;
    }

    // squares are row-major indices 0-8
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

    // both kinds share one type, so the workbench looks up just one recipe type
    @Override
    RecipeSerializer<?> getSerializer();
}
