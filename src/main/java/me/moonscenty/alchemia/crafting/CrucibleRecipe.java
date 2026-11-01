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

// The catalyst is used up whether or not it completes the recipe.
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

    public AspectList taken(AspectList dissolved) {
        AspectList left = dissolved;
        for (Holder<Aspect> aspect : aspects.sortedByName()) {
            left = left.reduce(aspect, aspects.get(aspect));
        }
        return left;
    }

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
                        Codecs.RESEARCH.forGetter(CrucibleRecipe::research))
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
