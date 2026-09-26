package me.moonscenty.alchemia.crafting;

import java.util.Optional;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * Something boiled out of a crucible: a thing dropped in, and what has to already be dissolved for it to take.
 * <p>
 * The pot is not a bench, so nothing is laid out in a shape. What matters is what is in the water and what falls
 * into it, and the one thing that falls in is used up whether or not it is the last ingredient needed.
 *
 * @param catalyst what is thrown in to finish the work
 * @param aspects what has to be dissolved first, and what is taken out of the pot when the work is done
 */
public record CrucibleRecipe(Ingredient catalyst, AspectList aspects, ItemStack result,
                             Optional<ResourceLocation> research) implements Recipe<CrucibleInput> {

    @Override
    public boolean matches(CrucibleInput input, Level level) {
        if (!catalyst.test(input.catalyst())) {
            return false;
        }
        for (Holder<Aspect> aspect : aspects.sortedByName()) {
            if (input.dissolved().get(aspect) < aspects.get(aspect)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(CrucibleInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    /** What is left in the water once this has been boiled out of it. */
    public AspectList taken(AspectList dissolved) {
        AspectList left = dissolved;
        for (Holder<Aspect> aspect : aspects.sortedByName()) {
            left = left.reduce(aspect, aspects.get(aspect));
        }
        return left;
    }

    /** The one thing thrown in. Naming it here is what lets the book draw the recipe like any other. */
    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, catalyst);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.CRUCIBLE.get();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CRUCIBLE_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<CrucibleRecipe> {
        private static final MapCodec<CrucibleRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                        Ingredient.CODEC_NONEMPTY.fieldOf("catalyst").forGetter(CrucibleRecipe::catalyst),
                        AspectList.CODEC.fieldOf("aspects").forGetter(CrucibleRecipe::aspects),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(CrucibleRecipe::result),
                        ResourceLocation.CODEC.optionalFieldOf("research").forGetter(CrucibleRecipe::research))
                .apply(instance, CrucibleRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, CrucibleRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC, CrucibleRecipe::catalyst,
                        AspectList.STREAM_CODEC, CrucibleRecipe::aspects,
                        ItemStack.STREAM_CODEC, CrucibleRecipe::result,
                        ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), CrucibleRecipe::research,
                        CrucibleRecipe::new);

        @Override
        public MapCodec<CrucibleRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CrucibleRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
