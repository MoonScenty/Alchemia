package me.moonscenty.alchemia.crafting;

import java.util.Optional;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.wand.WandParts;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import me.moonscenty.alchemia.wand.WandCap;
import me.moonscenty.alchemia.wand.WandRod;

/**
 * Putting a wand together: a rod across the middle of the bench with a matching cap at each end of it.
 * <p>
 * This one is written in code rather than as a recipe file because what comes out depends on what went in — nine
 * rods against five caps is forty-five wands, and none of them is worth a file of its own.
 * <p>
 * It costs nothing. It has to: the bench takes its price out of a wand, and this is where the first one comes from.
 */
public class ArcaneWandRecipe implements ArcaneRecipe {
    /** Where each piece has to lie. The rod runs corner to corner, so the caps sit at the two free corners. */
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

    /** The wand these nine squares spell out, or nothing if they do not spell one. */
    private static ItemStack assembled(CraftingInput input) {
        if (input.width() != 3 || input.height() != 3) {
            return null;
        }
        // everything but the three squares the wand lies on has to be bare
        for (int square = 0; square < 9; square++) {
            boolean used = square == LOWER_CAP || square == ROD || square == UPPER_CAP;
            if (used == input.getItem(square).isEmpty()) {
                return null;
            }
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
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
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

    /** Nothing to write down: the recipe is the same every time and reads what it needs off the bench. */
    public static class Serializer implements RecipeSerializer<ArcaneWandRecipe> {
        private static final MapCodec<ArcaneWandRecipe> CODEC = MapCodec.unit(ArcaneWandRecipe::new);
        private static final StreamCodec<RegistryFriendlyByteBuf, ArcaneWandRecipe> STREAM_CODEC =
                StreamCodec.unit(new ArcaneWandRecipe());

        @Override
        public MapCodec<ArcaneWandRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ArcaneWandRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
