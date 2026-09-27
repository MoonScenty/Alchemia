package me.moonscenty.alchemia.crafting;

import java.util.ArrayList;
import java.util.List;
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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * Something worked at an infusion matrix: one thing held under it, a ring of things around it, and a long drink
 * of essentia drawn out of whatever jars are standing nearby.
 * <p>
 * The ring is a bag, not a shape. Where each thing stands makes no difference to what comes out -- only that the
 * ring holds exactly these things and nothing else, which is what lets a works be laid out to fit the room rather
 * than to fit the recipe.
 *
 * @param central what is worked on, and what the result takes the place of when it is done
 * @param ring what is worked into it, one thing to a pedestal
 * @param essentia what has to be drunk, a point at a time, out of the jars within reach
 * @param instability how badly the work wants to go wrong, before the surroundings have their say
 */
public record InfusionRecipe(Ingredient central, List<Ingredient> ring, ItemStack result, AspectList essentia,
                             int instability, Optional<ResourceLocation> research) implements Recipe<InfusionInput> {

    @Override
    public boolean matches(InfusionInput input, Level level) {
        if (!central.test(input.central()) || input.ring().size() != ring.size()) {
            return false;
        }
        // every thing on the ring has to answer for exactly one of the ingredients, and none may be left over
        List<Ingredient> wanted = new ArrayList<>(ring);
        for (ItemStack standing : input.ring()) {
            int found = -1;
            for (int which = 0; which < wanted.size(); which++) {
                if (wanted.get(which).test(standing)) {
                    found = which;
                    break;
                }
            }
            if (found < 0) {
                return false;
            }
            wanted.remove(found);
        }
        return wanted.isEmpty();
    }

    @Override
    public ItemStack assemble(InfusionInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    /** What the book draws: the thing worked on first, then the ring around it. */
    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> all = NonNullList.create();
        all.add(central);
        all.addAll(ring);
        return all;
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
        return ModRecipes.INFUSION.get();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.INFUSION_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<InfusionRecipe> {
        private static final MapCodec<InfusionRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                        Ingredient.CODEC_NONEMPTY.fieldOf("central").forGetter(InfusionRecipe::central),
                        Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ring").forGetter(InfusionRecipe::ring),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(InfusionRecipe::result),
                        AspectList.CODEC.fieldOf("essentia").forGetter(InfusionRecipe::essentia),
                        com.mojang.serialization.Codec.INT.optionalFieldOf("instability", 0)
                                .forGetter(InfusionRecipe::instability),
                        ResourceLocation.CODEC.optionalFieldOf("research").forGetter(InfusionRecipe::research))
                .apply(instance, InfusionRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, InfusionRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC, InfusionRecipe::central,
                        Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), InfusionRecipe::ring,
                        ItemStack.STREAM_CODEC, InfusionRecipe::result,
                        AspectList.STREAM_CODEC, InfusionRecipe::essentia,
                        ByteBufCodecs.VAR_INT, InfusionRecipe::instability,
                        ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), InfusionRecipe::research,
                        InfusionRecipe::new);

        @Override
        public MapCodec<InfusionRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, InfusionRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
