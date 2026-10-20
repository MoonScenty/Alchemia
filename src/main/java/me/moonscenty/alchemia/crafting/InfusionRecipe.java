package me.moonscenty.alchemia.crafting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.enchantment.InfusionEnchantment;
import me.moonscenty.alchemia.enchantment.InfusionEnchantments;
import me.moonscenty.alchemia.item.FortressArmorItem;
import me.moonscenty.alchemia.item.HelmFitting;
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
 * @param enchants what this puts on the thing worked, if it makes nothing new at all. A recipe with this set
 *                 hands back the same tool with one more thing on it, so its result is whatever went in
 * @param fits what this works into a helm, on the same terms: the helm that went in comes back wearing it.
 *             A helm takes one and the altar will not sell a second
 * @param augments whether this is runic augmentation: one more hardening on whatever runic gear is worked, its
 *                 ring a diamond and a salis mundus and one salis mundus more for each point of shield it already
 *                 holds, and its essentia doubling with each
 */
public record InfusionRecipe(Ingredient central, List<Ingredient> ring, ItemStack result, AspectList essentia,
                             int instability, Optional<ResourceLocation> research,
                             Optional<InfusionEnchantment> enchants,
                             Optional<HelmFitting> fits, boolean augments) implements Recipe<InfusionInput> {

    public InfusionRecipe(Ingredient central, List<Ingredient> ring, ItemStack result, AspectList essentia,
            int instability, Optional<ResourceLocation> research, Optional<InfusionEnchantment> enchants,
            Optional<HelmFitting> fits) {
        this(central, ring, result, essentia, instability, research, enchants, fits, false);
    }

    /** The old shape, for the recipes that make a thing rather than work on one. */
    public InfusionRecipe(Ingredient central, List<Ingredient> ring, ItemStack result, AspectList essentia,
            int instability, Optional<ResourceLocation> research) {
        this(central, ring, result, essentia, instability, research, Optional.empty(), Optional.empty(), false);
    }

    /** For the recipes that put a working on a tool. */
    public InfusionRecipe(Ingredient central, List<Ingredient> ring, ItemStack result, AspectList essentia,
            int instability, Optional<ResourceLocation> research, Optional<InfusionEnchantment> enchants) {
        this(central, ring, result, essentia, instability, research, enchants, Optional.empty(), false);
    }

    /** The shield a piece of runic gear holds now, which is what augmenting it once more costs by. */
    private static int held(ItemStack central) {
        return me.moonscenty.alchemia.item.bauble.RunicGear.charge(central);
    }

    /** The essentia this asks to work this thing: augmentation's doubles with every point already there. */
    public AspectList essentiaFor(ItemStack central) {
        if (enchants.isPresent()) {
            // the original's: the level being worked, and a third again for every other enchantment already on it
            var on = me.moonscenty.alchemia.enchantment.InfusionEnchantments.on(central);
            int others = on.size() - (on.containsKey(enchants.get()) ? 1 : 0);
            float times = me.moonscenty.alchemia.enchantment.InfusionEnchantments.level(central, enchants.get()) + 1
                    + others * 0.33F;
            AspectList scaled = AspectList.EMPTY;
            for (var aspect : essentia.sortedByName()) {
                int amount = (int) (essentia.get(aspect) * times);
                if (amount > 0) {
                    scaled = scaled.add(aspect, amount);
                }
            }
            return scaled;
        }
        if (!augments) {
            return essentia;
        }
        int vis = (int) (32 * Math.pow(2, held(central)));
        return AspectList.of(me.moonscenty.alchemia.registry.ModAspects.PROTECT, vis / 2)
                .add(me.moonscenty.alchemia.registry.ModAspects.CRYSTAL, vis / 2)
                .add(me.moonscenty.alchemia.registry.ModAspects.ENERGY, vis);
    }

    /** How unsteady working this thing is: augmentation's grows by one with every second point. */
    public int instabilityFor(ItemStack central) {
        return augments ? 5 + held(central) / 2 : instability;
    }

    @Override
    public boolean matches(InfusionInput input, Level level) {
        if (augments) {
            if (!me.moonscenty.alchemia.item.bauble.RunicGear.augmentable(input.central())) {
                return false;
            }
            int salis = 1 + held(input.central());
            long diamonds = input.ring().stream().filter(stack -> stack.is(net.minecraft.world.item.Items.DIAMOND))
                    .count();
            long salt = input.ring().stream()
                    .filter(stack -> stack.is(me.moonscenty.alchemia.registry.ModItems.SALIS_MUNDUS.get())).count();
            return diamonds == 1 && salt == salis && input.ring().size() == 1 + salis;
        }
        if (!central.test(input.central()) || input.ring().size() != ring.size()) {
            return false;
        }
        // an altar will not sell the same step twice: a tool already worked this far is not a match
        if (enchants.isPresent() && !InfusionEnchantments.roomFor(input.central(), enchants.get())) {
            return false;
        }
        // nor will it work a second thing into a helm that is already wearing one
        if (fits.isPresent() && FortressArmorItem.fitting(input.central()).isPresent()) {
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
        if (augments) {
            ItemStack hardened = input.central().copy();
            hardened.set(me.moonscenty.alchemia.registry.ModDataComponents.RUNIC_HARDENING.get(),
                    me.moonscenty.alchemia.item.bauble.RunicGear.hardening(hardened) + 1);
            return hardened;
        }
        if (fits.isPresent()) {
            return FortressArmorItem.wearing(input.central(), fits.get());
        }
        return enchants.map(which -> InfusionEnchantments.raised(input.central(), which))
                .orElseGet(result::copy);
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
                        // a working that enchants makes nothing new, so it writes down no result at all
                        ItemStack.OPTIONAL_CODEC.optionalFieldOf("result", ItemStack.EMPTY)
                                .forGetter(InfusionRecipe::result),
                        AspectList.CODEC.fieldOf("essentia").forGetter(InfusionRecipe::essentia),
                        com.mojang.serialization.Codec.INT.optionalFieldOf("instability", 0)
                                .forGetter(InfusionRecipe::instability),
                        ResourceLocation.CODEC.optionalFieldOf("research").forGetter(InfusionRecipe::research),
                        InfusionEnchantment.CODEC.optionalFieldOf("enchants").forGetter(InfusionRecipe::enchants),
                        HelmFitting.CODEC.optionalFieldOf("fits").forGetter(InfusionRecipe::fits),
                        com.mojang.serialization.Codec.BOOL.optionalFieldOf("augments", false)
                                .forGetter(InfusionRecipe::augments))
                .apply(instance, InfusionRecipe::new));

        /**
         * Written out by hand rather than composed.
         * <p>
         * The helper that stitches stream codecs together stops at six pieces and this has nine. Writing the
         * nine out is duller than one more line of composition would have been and no less plain.
         */
        private static final StreamCodec<RegistryFriendlyByteBuf, InfusionRecipe> STREAM_CODEC =
                new StreamCodec<>() {
                    @Override
                    public InfusionRecipe decode(RegistryFriendlyByteBuf buffer) {
                        Ingredient central = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
                        List<Ingredient> ring =
                                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer);
                        ItemStack result = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
                        AspectList essentia = AspectList.STREAM_CODEC.decode(buffer);
                        int instability = ByteBufCodecs.VAR_INT.decode(buffer);
                        Optional<ResourceLocation> research =
                                ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC).decode(buffer);
                        Optional<InfusionEnchantment> enchants =
                                ByteBufCodecs.optional(InfusionEnchantment.STREAM_CODEC).decode(buffer);
                        Optional<HelmFitting> fits =
                                ByteBufCodecs.optional(HelmFitting.STREAM_CODEC).decode(buffer);
                        boolean augments = ByteBufCodecs.BOOL.decode(buffer);
                        return new InfusionRecipe(central, ring, result, essentia, instability, research,
                                enchants, fits, augments);
                    }

                    @Override
                    public void encode(RegistryFriendlyByteBuf buffer, InfusionRecipe recipe) {
                        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.central());
                        Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, recipe.ring());
                        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, recipe.result());
                        AspectList.STREAM_CODEC.encode(buffer, recipe.essentia());
                        ByteBufCodecs.VAR_INT.encode(buffer, recipe.instability());
                        ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC).encode(buffer, recipe.research());
                        ByteBufCodecs.optional(InfusionEnchantment.STREAM_CODEC)
                                .encode(buffer, recipe.enchants());
                        ByteBufCodecs.optional(HelmFitting.STREAM_CODEC).encode(buffer, recipe.fits());
                        ByteBufCodecs.BOOL.encode(buffer, recipe.augments());
                    }
                };

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
