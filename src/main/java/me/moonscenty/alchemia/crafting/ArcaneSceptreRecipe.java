package me.moonscenty.alchemia.crafting;

import java.util.Optional;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModWandParts;
import me.moonscenty.alchemia.wand.WandCap;
import me.moonscenty.alchemia.wand.WandParts;
import me.moonscenty.alchemia.wand.WandRod;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Putting a sceptre together, the original's {@code ArcaneSceptreRecipe}: a wand's rod in the middle of the bench with
 * a cap above it, a cap to its right and a cap in the far corner below, and a primal charm in the top corner where the
 * crossbar meets the shaft. Any rod but a staff core, and the three caps all of a kind.
 * <p>
 * Unlike a wand it has a price, and the price is set by what went into it: the cap's working cost times the rod's,
 * times ten, of every primal. That is why the price is asked of the grid rather than of the recipe.
 */
public class ArcaneSceptreRecipe implements ArcaneRecipe {
    public static final ArcaneSceptreRecipe INSTANCE = new ArcaneSceptreRecipe();

    /** Where each piece lies, numbered across the rows of the grid. */
    private static final int TOP_CAP = 1;
    private static final int CHARM = 2;
    private static final int ROD = 4;
    private static final int SIDE_CAP = 5;
    private static final int LOW_CAP = 6;
    /** What a cap's cost times a rod's is multiplied by, for each primal. The original's. */
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

    /** What a sceptre of these makes the bench charge, of every primal. */
    public static AspectList price(WandCap cap, WandRod rod) {
        int each = cap.craftCost() * rod.craftCost() * PRICE;
        AspectList price = AspectList.EMPTY;
        for (Holder<me.moonscenty.alchemia.aspect.Aspect> primal : ModAspects.primals()) {
            price = price.add(primal, each);
        }
        return price;
    }

    private record Parts(Holder<WandRod> rod, Holder<WandCap> cap) {
    }

    /** The rod and cap these nine squares spell a sceptre of, or nothing. */
    private static Parts parts(CraftingInput input) {
        if (input.width() != 3 || input.height() != 3) {
            return null;
        }
        for (int square = 0; square < 9; square++) {
            boolean used = square == TOP_CAP || square == CHARM || square == ROD || square == SIDE_CAP
                    || square == LOW_CAP;
            if (used == input.getItem(square).isEmpty()) {
                return null;
            }
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

    /** The plainest sceptre, iron on wood, which is what the book and a recipe viewer show. */
    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return WandItem.sceptre(ModWandParts.WOOD, ModWandParts.IRON);
    }

    /** What the plainest sceptre costs, for showing; the bench asks the grid. */
    @Override
    public AspectList cost() {
        return price(ModWandParts.IRON.get(), ModWandParts.WOOD.get());
    }

    /** The plainest layout, for the book to draw. */
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
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ARCANE_SCEPTRE.get();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ArcaneSceptreRecipe;
    }

    @Override
    public int hashCode() {
        return ArcaneSceptreRecipe.class.hashCode();
    }

    /** Nothing to write down: the recipe is the same every time and reads what it needs off the bench. */
    public static class Serializer implements RecipeSerializer<ArcaneSceptreRecipe> {
        private static final MapCodec<ArcaneSceptreRecipe> CODEC = MapCodec.unit(INSTANCE);
        private static final StreamCodec<RegistryFriendlyByteBuf, ArcaneSceptreRecipe> STREAM_CODEC =
                StreamCodec.unit(INSTANCE);

        @Override
        public MapCodec<ArcaneSceptreRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ArcaneSceptreRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
