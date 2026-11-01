package me.moonscenty.alchemia.crafting;

import me.moonscenty.alchemia.block.JarBlock;
import me.moonscenty.alchemia.item.PhialItem;
import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

// The phial's essentia isn't spent: a label records a name, not an amount. A written label on its own is wiped.
public class LabelRecipe extends CustomRecipe {
    public LabelRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return !assembled(input).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return assembled(input);
    }

    private static ItemStack assembled(CraftingInput input) {
        ItemStack label = ItemStack.EMPTY;
        ItemStack phial = ItemStack.EMPTY;
        for (int square = 0; square < input.size(); square++) {
            ItemStack found = input.getItem(square);
            if (found.isEmpty()) {
                continue;
            }
            if (found.is(ModItems.JAR_LABEL.get()) && label.isEmpty()) {
                label = found;
            } else if (found.is(ModItems.PHIAL.get()) && phial.isEmpty()) {
                phial = found;
            } else {
                return ItemStack.EMPTY;
            }
        }
        if (label.isEmpty()) {
            return ItemStack.EMPTY;
        }
        boolean written = label.has(ModDataComponents.ESSENTIA.get());
        if (phial.isEmpty()) {
            return written ? new ItemStack(ModItems.JAR_LABEL.get()) : ItemStack.EMPTY;
        }
        return written ? ItemStack.EMPTY
                : PhialItem.inside(phial).map(JarBlock::labelFor).orElse(ItemStack.EMPTY);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> left = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int square = 0; square < input.size(); square++) {
            ItemStack found = input.getItem(square);
            if (found.is(ModItems.PHIAL.get())) {
                left.set(square, PhialItem.emptied(found));
            }
        }
        return left;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.JAR_LABEL.get();
    }
}
