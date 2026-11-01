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

// The ring matches as a bag, never by position. With enchants or fits set, the result is the central item itself
// with one more enchantment or helm fitting; augments adds one runic hardening, costs scaling with what it holds.
public record InfusionRecipe(Ingredient central, List<Ingredient> ring, ItemStack result, AspectList essentia,
                             int instability, Optional<ResourceLocation> research,
                             Optional<InfusionEnchantment> enchants,
                             Optional<HelmFitting> fits, boolean augments) implements Recipe<InfusionInput> {

    public InfusionRecipe(Ingredient central, List<Ingredient> ring, ItemStack result, AspectList essentia,
            int instability, Optional<ResourceLocation> research, Optional<InfusionEnchantment> enchants,
            Optional<HelmFitting> fits) {
        this(central, ring, result, essentia, instability, research, enchants, fits, false);
    }

    public InfusionRecipe(Ingredient central, List<Ingredient> ring, ItemStack result, AspectList essentia,
            int instability, Optional<ResourceLocation> research) {
        this(central, ring, result, essentia, instability, research, Optional.empty(), Optional.empty(), false);
    }

    public InfusionRecipe(Ingredient central, List<Ingredient> ring, ItemStack result, AspectList essentia,
            int instability, Optional<ResourceLocation> research, Optional<InfusionEnchantment> enchants) {
        this(central, ring, result, essentia, instability, research, enchants, Optional.empty(), false);
    }

    private static int held(ItemStack central) {
        return me.moonscenty.alchemia.item.bauble.RunicGear.charge(central);
    }

    // augmentation's essentia doubles with every shield point already held
    public AspectList essentiaFor(ItemStack central) {
        if (enchants.isPresent()) {
            // original's: the level being worked, plus a third for every other enchantment already on it
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

    // augmentation's grows by one with every second point
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
        // the same step can't be sold twice
        if (enchants.isPresent() && !InfusionEnchantments.roomFor(input.central(), enchants.get())) {
            return false;
        }
        // one fitting per helm
        if (fits.isPresent() && FortressArmorItem.fitting(input.central()).isPresent()) {
            return false;
        }
        // each ring item must match exactly one ingredient, with none left over
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
                        // enchanting recipes make nothing new, so no result is written
                        ItemStack.OPTIONAL_CODEC.optionalFieldOf("result", ItemStack.EMPTY)
                                .forGetter(InfusionRecipe::result),
                        AspectList.CODEC.fieldOf("essentia").forGetter(InfusionRecipe::essentia),
                        com.mojang.serialization.Codec.INT.optionalFieldOf("instability", 0)
                                .forGetter(InfusionRecipe::instability),
                        Codecs.RESEARCH.forGetter(InfusionRecipe::research),
                        InfusionEnchantment.CODEC.optionalFieldOf("enchants").forGetter(InfusionRecipe::enchants),
                        HelmFitting.CODEC.optionalFieldOf("fits").forGetter(InfusionRecipe::fits),
                        com.mojang.serialization.Codec.BOOL.optionalFieldOf("augments", false)
                                .forGetter(InfusionRecipe::augments))
                .apply(instance, InfusionRecipe::new));

        // written by hand: StreamCodec.composite stops at six fields and this has nine
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
