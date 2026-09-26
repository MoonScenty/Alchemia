package me.moonscenty.alchemia.crafting;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.moonscenty.alchemia.aspect.AspectList;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** An arcane recipe whose ingredients may be laid anywhere on the grid. */
public record ArcaneShapelessRecipe(String group, NonNullList<Ingredient> ingredients, ItemStack result,
                                    AspectList cost, Optional<ResourceLocation> research) implements ArcaneRecipe {

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.ingredientCount() != ingredients.size()) {
            return false;
        }
        return input.size() == 1 && ingredients.size() == 1
                ? ingredients.getFirst().test(input.getItem(0))
                : input.stackedContents().canCraft(this, null);
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= ingredients.size();
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ARCANE_SHAPELESS.get();
    }

    public static class Serializer implements RecipeSerializer<ArcaneShapelessRecipe> {
        /** At least one ingredient and at most nine, since nine is the whole grid. */
        private static final Codec<NonNullList<Ingredient>> INGREDIENTS = Ingredient.CODEC_NONEMPTY
                .listOf()
                .flatXmap(list -> list.isEmpty() ? com.mojang.serialization.DataResult.error(() -> "no ingredients")
                                : list.size() > 9 ? com.mojang.serialization.DataResult.error(() -> "too many ingredients")
                                : com.mojang.serialization.DataResult.success(
                                        NonNullList.copyOf(list)),
                        com.mojang.serialization.DataResult::success);

        private static final MapCodec<ArcaneShapelessRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                        Codecs.GROUP.forGetter(ArcaneShapelessRecipe::group),
                        INGREDIENTS.fieldOf("ingredients").forGetter(ArcaneShapelessRecipe::ingredients),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(ArcaneShapelessRecipe::result),
                        Codecs.COST.forGetter(ArcaneShapelessRecipe::cost),
                        Codecs.RESEARCH.forGetter(ArcaneShapelessRecipe::research))
                .apply(instance, ArcaneShapelessRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, ArcaneShapelessRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8, ArcaneShapelessRecipe::group,
                        Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list())
                                .map(NonNullList::copyOf, list -> list),
                        ArcaneShapelessRecipe::ingredients,
                        ItemStack.STREAM_CODEC, ArcaneShapelessRecipe::result,
                        AspectList.STREAM_CODEC, ArcaneShapelessRecipe::cost,
                        ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), ArcaneShapelessRecipe::research,
                        ArcaneShapelessRecipe::new);

        @Override
        public MapCodec<ArcaneShapelessRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ArcaneShapelessRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
