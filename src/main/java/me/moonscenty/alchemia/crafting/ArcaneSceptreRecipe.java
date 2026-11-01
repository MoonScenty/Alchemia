package me.moonscenty.alchemia.crafting;

import java.util.Optional;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModWandParts;
import me.moonscenty.alchemia.wand.WandCap;
import me.moonscenty.alchemia.wand.WandParts;
import me.moonscenty.alchemia.wand.WandRod;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class ArcaneSceptreRecipe extends UnitArcaneRecipe {
    public static final ArcaneSceptreRecipe INSTANCE = new ArcaneSceptreRecipe();

    // grid slots, row by row
    private static final int TOP_CAP = 1;
    private static final int CHARM = 2;
    private static final int ROD = 4;
    private static final int SIDE_CAP = 5;
    private static final int LOW_CAP = 6;
    // original's value
    private static final int PRICE = 10;

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return parts(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        Parts parts = parts(input);
        return parts == null ? ItemStack.EMPTY : WandItem.sceptre(parts.rod(), parts.cap());
    }

    @Override
    public AspectList cost(CraftingInput input) {
        Parts parts = parts(input);
        return parts == null ? AspectList.EMPTY : price(parts.cap().value(), parts.rod().value());
    }

    public static AspectList price(WandCap cap, WandRod rod) {
        return AspectList.eachPrimal(cap.craftCost() * rod.craftCost() * PRICE);
    }

    private record Parts(Holder<WandRod> rod, Holder<WandCap> cap) {
    }

    private static Parts parts(CraftingInput input) {
        if (!ArcaneRecipe.onlyFilled(input, TOP_CAP, CHARM, ROD, SIDE_CAP, LOW_CAP)) {
            return null;
        }
        if (!input.getItem(CHARM).is(ModItems.PRIMAL_CHARM.get())) {
            return null;
        }
        Holder<WandCap> cap = WandParts.capOf(input.getItem(TOP_CAP));
        Holder<WandRod> rod = WandParts.rodOf(input.getItem(ROD));
        if (cap == null || rod == null || rod.value().staff() || !cap.equals(WandParts.capOf(input.getItem(SIDE_CAP)))
                || !cap.equals(WandParts.capOf(input.getItem(LOW_CAP)))) {
            return null;
        }
        return new Parts(rod, cap);
    }

    // iron on wood, for the book and recipe viewers
    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return WandItem.sceptre(ModWandParts.WOOD, ModWandParts.IRON);
    }

    @Override
    public AspectList cost() {
        return price(ModWandParts.IRON.get(), ModWandParts.WOOD.get());
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        Ingredient cap = Ingredient.of(ModItems.WAND_CAPS.get("iron").get());
        return NonNullList.of(Ingredient.EMPTY, Ingredient.EMPTY, cap,
                Ingredient.of(ModItems.PRIMAL_CHARM.get()), Ingredient.EMPTY,
                Ingredient.of(net.minecraft.world.item.Items.STICK), cap, cap, Ingredient.EMPTY, Ingredient.EMPTY);
    }

    @Override
    public Optional<ResourceLocation> research() {
        return Optional.of(Alchemia.id("sceptre"));
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ARCANE_SCEPTRE.get();
    }
}
