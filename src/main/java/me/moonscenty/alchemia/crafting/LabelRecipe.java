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

/**
 * Writing an aspect onto a label, and rubbing it out again.
 * <p>
 * A blank label held up to a full phial takes the name of what is in the phial. The essentia is not used up -- the
 * phial comes back empty but whole, because what a label carries is a word rather than a measure, and one draught
 * of fire names as many labels as you care to write. Laying a written label down on its own rubs it out.
 * <p>
 * It is written in code rather than as a recipe file because both halves depend on what went in: thirty-five
 * aspects would otherwise want thirty-five files apiece, and none of them worth one.
 */
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

    /** What these squares spell out, which is nothing at all unless they spell one of the two. */
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
                // a second label, a second phial, or anything else at all
                return ItemStack.EMPTY;
            }
        }
        if (label.isEmpty()) {
            return ItemStack.EMPTY;
        }
        boolean written = label.has(ModDataComponents.ESSENTIA.get());
        if (phial.isEmpty()) {
            // a written label on its own is rubbed out; a blank one on its own is nothing to work on
            return written ? new ItemStack(ModItems.JAR_LABEL.get()) : ItemStack.EMPTY;
        }
        return written ? ItemStack.EMPTY
                : PhialItem.inside(phial).map(JarBlock::labelFor).orElse(ItemStack.EMPTY);
    }

    /** The phial is handed back rather than drunk: its essentia is what was read, not what was spent. */
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
