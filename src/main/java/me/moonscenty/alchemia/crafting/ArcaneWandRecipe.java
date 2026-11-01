package me.moonscenty.alchemia.crafting;

import java.util.Optional;

import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.wand.WandParts;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import me.moonscenty.alchemia.wand.WandCap;
import me.moonscenty.alchemia.wand.WandRod;

// free on purpose: the bench takes its price from a wand, and this makes the first one
public class ArcaneWandRecipe extends UnitArcaneRecipe {
    public static final ArcaneWandRecipe INSTANCE = new ArcaneWandRecipe();

    // the rod runs corner to corner; caps sit at the two free corners
    private static final int LOWER_CAP = 6;
    private static final int ROD = 4;
    private static final int UPPER_CAP = 2;

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return assembled(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack made = assembled(input);
        return made == null ? ItemStack.EMPTY : made;
    }

    private static ItemStack assembled(CraftingInput input) {
        if (!ArcaneRecipe.onlyFilled(input, LOWER_CAP, ROD, UPPER_CAP)) {
            return null;
        }

        Holder<WandCap> lower = WandParts.capOf(input.getItem(LOWER_CAP));
        Holder<WandCap> upper = WandParts.capOf(input.getItem(UPPER_CAP));
        Holder<WandRod> rod = WandParts.rodOf(input.getItem(ROD));
        if (lower == null || upper == null || rod == null || !lower.equals(upper)) {
            return null;
        }
        return WandItem.of(rod, lower);
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return WandItem.of(me.moonscenty.alchemia.registry.ModWandParts.WOOD,
                me.moonscenty.alchemia.registry.ModWandParts.IRON);
    }

    @Override
    public AspectList cost() {
        return AspectList.EMPTY;
    }

    @Override
    public Optional<ResourceLocation> research() {
        return Optional.empty();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ARCANE_WAND.get();
    }
}
