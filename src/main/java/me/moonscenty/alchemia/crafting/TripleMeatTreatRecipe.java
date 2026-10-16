package me.moonscenty.alchemia.crafting;

import java.util.HashSet;
import java.util.Set;

import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * The triple meat treat, the original's {@code RecipeTripleMeatTreat}: a sugar and three meat chunks, all of
 * different kinds, anywhere on the grid. Twenty ways to choose three of six, so it is worked out here rather than
 * written out twenty times. The book and recipe viewers are shown a sugar and three chunks of any kind.
 */
public class TripleMeatTreatRecipe extends CustomRecipe {
    public TripleMeatTreatRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        boolean sugar = false;
        Set<Item> meats = new HashSet<>();
        for (int square = 0; square < input.size(); square++) {
            ItemStack found = input.getItem(square);
            if (found.isEmpty()) {
                continue;
            }
            if (found.is(Items.SUGAR)) {
                if (sugar) {
                    return false;
                }
                sugar = true;
            } else if (!isChunk(found) || !meats.add(found.getItem())) {
                // anything else, or a second chunk of a kind already there
                return false;
            }
        }
        return sugar && meats.size() == 3;
    }

    private static boolean isChunk(ItemStack stack) {
        return ModItems.MEAT_CHUNKS.values().stream().anyMatch(chunk -> stack.is(chunk.get()));
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return new ItemStack(ModItems.TRIPLE_MEAT_TREAT.get());
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(ModItems.TRIPLE_MEAT_TREAT.get());
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        Ingredient chunk = Ingredient.of(ModItems.MEAT_CHUNKS.values().stream().map(held -> held.get())
                .toArray(Item[]::new));
        return NonNullList.of(Ingredient.EMPTY, Ingredient.of(Items.SUGAR), chunk, chunk, chunk);
    }

    /** Shown in recipe viewers like any other bench recipe. */
    @Override
    public boolean isSpecial() {
        return false;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 4;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.TRIPLE_MEAT_TREAT.get();
    }
}
