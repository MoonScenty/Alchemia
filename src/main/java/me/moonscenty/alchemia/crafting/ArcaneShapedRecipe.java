package me.moonscenty.alchemia.crafting;

import java.util.Optional;

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
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;

/** An arcane recipe whose ingredients have to be laid out in a particular shape. */
public record ArcaneShapedRecipe(String group, ShapedRecipePattern pattern, ItemStack result,
                                 AspectList cost, Optional<ResourceLocation> research) implements ArcaneRecipe {

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return pattern.matches(input);
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= pattern.width() && height >= pattern.height();
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return pattern.ingredients();
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ARCANE_SHAPED.get();
    }

    public static class Serializer implements RecipeSerializer<ArcaneShapedRecipe> {
        private static final MapCodec<ArcaneShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                        Codecs.GROUP.forGetter(ArcaneShapedRecipe::group),
                        ShapedRecipePattern.MAP_CODEC.forGetter(ArcaneShapedRecipe::pattern),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(ArcaneShapedRecipe::result),
                        Codecs.COST.forGetter(ArcaneShapedRecipe::cost),
                        Codecs.RESEARCH.forGetter(ArcaneShapedRecipe::research))
                .apply(instance, ArcaneShapedRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, ArcaneShapedRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8, ArcaneShapedRecipe::group,
                        ShapedRecipePattern.STREAM_CODEC, ArcaneShapedRecipe::pattern,
                        ItemStack.STREAM_CODEC, ArcaneShapedRecipe::result,
                        AspectList.STREAM_CODEC, ArcaneShapedRecipe::cost,
                        ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), ArcaneShapedRecipe::research,
                        ArcaneShapedRecipe::new);

        @Override
        public MapCodec<ArcaneShapedRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ArcaneShapedRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
