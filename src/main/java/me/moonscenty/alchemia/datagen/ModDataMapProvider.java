package me.moonscenty.alchemia.datagen;

import java.util.concurrent.CompletableFuture;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aspect.ModDataMaps;
import me.moonscenty.alchemia.block.CrystalType;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModTags;
import me.moonscenty.alchemia.registry.StoneSet;
import me.moonscenty.alchemia.registry.WoodSet;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.Compostable;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

/**
 * Writes down the aspects of the things at the root of the crafting tree, and the compostables.
 * <p>
 * Only raw materials and mobs are listed. Anything made from them has its aspects worked out from its recipe, so a
 * crafted item needs no entry here — and neither does an item from another mod.
 */
public class ModDataMapProvider extends DataMapProvider {
    public ModDataMapProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        compostables();
        vanillaAspects();
        modAspects();
        mobAspects();
    }

    private void compostables() {
        var compostables = builder(NeoForgeDataMaps.COMPOSTABLES);
        ModBlocks.PLANTS.forEach(plant -> compostables.add(plant.getId(), new Compostable(0.65F), false));
        for (WoodSet wood : ModBlocks.WOODS) {
            compostables.add(wood.leaves().getId(), new Compostable(0.3F), false);
            compostables.add(wood.sapling().getId(), new Compostable(0.3F), false);
        }
    }

    /** Raw vanilla materials, at the values the original gave them. */
    private void vanillaAspects() {
        // stone and soil
        tag(Tags.Items.STONES, a(ModAspects.EARTH, 2));
        tag(Tags.Items.COBBLESTONES, a(ModAspects.EARTH, 1).add(ModAspects.ENTROPY, 1));
        tag(ItemTags.DIRT, a(ModAspects.EARTH, 2));
        tag(ItemTags.SAND, a(ModAspects.EARTH, 1).add(ModAspects.ENTROPY, 1));
        tag(Tags.Items.GRAVELS, a(ModAspects.EARTH, 2));
        tag(Tags.Items.OBSIDIANS, a(ModAspects.EARTH, 2).add(ModAspects.FIRE, 2).add(ModAspects.DARKNESS, 1));
        tag(Tags.Items.NETHERRACKS, a(ModAspects.EARTH, 2).add(ModAspects.FIRE, 1));
        tag(Tags.Items.END_STONES, a(ModAspects.EARTH, 1).add(ModAspects.DARKNESS, 1));
        item(Items.SOUL_SAND, a(ModAspects.EARTH, 1).add(ModAspects.TRAP, 1).add(ModAspects.SOUL, 1));
        item(Items.SOUL_SOIL, a(ModAspects.EARTH, 1).add(ModAspects.TRAP, 1).add(ModAspects.SOUL, 1));
        item(Items.CLAY_BALL, a(ModAspects.WATER, 1).add(ModAspects.EARTH, 1));
        item(Items.FLINT, a(ModAspects.EARTH, 1).add(ModAspects.TOOL, 1));
        item(Items.GLASS, a(ModAspects.CRYSTAL, 1));
        // deepslate has no counterpart in the original, so it is stone with a touch of the dark
        item(Items.DEEPSLATE, a(ModAspects.EARTH, 2).add(ModAspects.DARKNESS, 1));
        item(Items.COBBLED_DEEPSLATE, a(ModAspects.EARTH, 1).add(ModAspects.ENTROPY, 1).add(ModAspects.DARKNESS, 1));

        // wood
        tag(ItemTags.LOGS, a(ModAspects.PLANT, 4));
        tag(ItemTags.PLANKS, a(ModAspects.PLANT, 1));
        tag(ItemTags.SAPLINGS, a(ModAspects.PLANT, 3));
        tag(ItemTags.LEAVES, a(ModAspects.PLANT, 1));
        item(Items.STICK, a(ModAspects.PLANT, 1));

        // metals and gems
        tag(Tags.Items.INGOTS_IRON, a(ModAspects.METAL, 4));
        tag(Tags.Items.INGOTS_GOLD, a(ModAspects.METAL, 3).add(ModAspects.DESIRE, 2));
        tag(Tags.Items.INGOTS_COPPER, a(ModAspects.METAL, 3).add(ModAspects.ENERGY, 1));
        tag(Tags.Items.GEMS_DIAMOND, a(ModAspects.CRYSTAL, 4).add(ModAspects.DESIRE, 4));
        tag(Tags.Items.GEMS_EMERALD, a(ModAspects.CRYSTAL, 4).add(ModAspects.DESIRE, 5));
        tag(Tags.Items.GEMS_QUARTZ, a(ModAspects.CRYSTAL, 1).add(ModAspects.ENERGY, 1));
        tag(Tags.Items.GEMS_LAPIS, a(ModAspects.SENSES, 3));
        tag(Tags.Items.DUSTS_REDSTONE, a(ModAspects.ENERGY, 2));
        tag(Tags.Items.DUSTS_GLOWSTONE, a(ModAspects.SENSES, 3).add(ModAspects.LIGHT, 10));
        item(Items.AMETHYST_SHARD, a(ModAspects.CRYSTAL, 3).add(ModAspects.SENSES, 1));
        item(Items.COAL, a(ModAspects.ENERGY, 2).add(ModAspects.FIRE, 2));
        item(Items.CHARCOAL, a(ModAspects.ENERGY, 2).add(ModAspects.FIRE, 2));

        // what drops off mobs
        item(Items.LEATHER, a(ModAspects.BEAST, 2).add(ModAspects.PROTECT, 1));
        item(Items.FEATHER, a(ModAspects.FLIGHT, 2).add(ModAspects.AIR, 1));
        item(Items.STRING, a(ModAspects.BEAST, 2));
        item(Items.BONE, a(ModAspects.DEATH, 2).add(ModAspects.LIFE, 1));
        item(Items.GUNPOWDER, a(ModAspects.FIRE, 4).add(ModAspects.ENTROPY, 4));
        item(Items.SLIME_BALL, a(ModAspects.WATER, 1).add(ModAspects.LIFE, 1));
        item(Items.ENDER_PEARL, a(ModAspects.ELDRITCH, 4).add(ModAspects.MOTION, 4));
        item(Items.BLAZE_ROD, a(ModAspects.FIRE, 4).add(ModAspects.ENERGY, 2));
        item(Items.GHAST_TEAR, a(ModAspects.WATER, 1).add(ModAspects.UNDEAD, 4).add(ModAspects.SOUL, 4));
        item(Items.SPIDER_EYE, a(ModAspects.SENSES, 2).add(ModAspects.BEAST, 2).add(ModAspects.DEATH, 1));
        item(Items.ROTTEN_FLESH, a(ModAspects.MAN, 1).add(ModAspects.LIFE, 2));
        item(Items.EGG, a(ModAspects.LIFE, 1).add(ModAspects.BEAST, 1));
        tag(ItemTags.WOOL, a(ModAspects.BEAST, 2).add(ModAspects.CRAFT, 1));

        // what grows
        item(Items.WHEAT, a(ModAspects.PLANT, 2).add(ModAspects.LIFE, 1));
        item(Items.APPLE, a(ModAspects.PLANT, 2).add(ModAspects.LIFE, 1));
        item(Items.CARROT, a(ModAspects.PLANT, 1).add(ModAspects.LIFE, 1).add(ModAspects.SENSES, 1));
        item(Items.POTATO, a(ModAspects.PLANT, 1).add(ModAspects.LIFE, 1).add(ModAspects.EARTH, 1));
        item(Items.SUGAR_CANE, a(ModAspects.PLANT, 1).add(ModAspects.WATER, 1).add(ModAspects.AIR, 1));
        item(Items.CACTUS, a(ModAspects.PLANT, 3).add(ModAspects.WATER, 1).add(ModAspects.ENTROPY, 1));
        item(Items.MELON_SLICE, a(ModAspects.LIFE, 1));
        item(Items.PUMPKIN, a(ModAspects.PLANT, 2));
        item(Items.NETHER_WART, a(ModAspects.PLANT, 1).add(ModAspects.FIRE, 1));
        item(Items.VINE, a(ModAspects.PLANT, 1));
        item(Items.SHORT_GRASS, a(ModAspects.PLANT, 1).add(ModAspects.AIR, 1));
        item(Items.DEAD_BUSH, a(ModAspects.PLANT, 1).add(ModAspects.ENTROPY, 1));
        tag(ItemTags.SMALL_FLOWERS, a(ModAspects.PLANT, 1).add(ModAspects.SENSES, 1));

        AspectList meat = a(ModAspects.BEAST, 2).add(ModAspects.LIFE, 1).add(ModAspects.EARTH, 1);
        for (ItemLike cut : new ItemLike[] {Items.BEEF, Items.PORKCHOP, Items.CHICKEN, Items.MUTTON, Items.RABBIT}) {
            item(cut, meat);
        }
        tag(ItemTags.FISHES, a(ModAspects.WATER, 1).add(ModAspects.BEAST, 2).add(ModAspects.LIFE, 1));

        // the cold and the bright
        item(Items.ICE, a(ModAspects.COLD, 4));
        item(Items.SNOWBALL, a(ModAspects.COLD, 1));
        item(Items.TORCH, a(ModAspects.LIGHT, 1));
    }

    private void modAspects() {
        item(ModItems.AMBER, a(ModAspects.TRAP, 2).add(ModAspects.CRYSTAL, 2));
        item(ModItems.QUICKSILVER, a(ModAspects.METAL, 3).add(ModAspects.DEATH, 1).add(ModAspects.EXCHANGE, 2));
        AspectList cinnabar = a(ModAspects.EARTH, 1).add(ModAspects.METAL, 2).add(ModAspects.EXCHANGE, 2).add(ModAspects.DEATH, 1);
        item(ModItems.RAW_CINNABAR, cinnabar);
        tag(ModTags.Items.ORES_AMBER, a(ModAspects.EARTH, 1).add(ModAspects.TRAP, 3).add(ModAspects.CRYSTAL, 2));
        tag(ModTags.Items.ORES_CINNABAR, cinnabar);

        // a shard and the crystal it broke off carry the aspect they grew from
        for (CrystalType type : CrystalType.values()) {
            Holder<Aspect> aspect = aspectOf(type);
            item(ModItems.SHARDS.get(type), a(aspect, 2).add(ModAspects.CRYSTAL, 1));
            item(ModBlocks.CRYSTALS.get(type), a(aspect, 4).add(ModAspects.CRYSTAL, 2));
        }
        // the balanced shard holds a little of all six at once
        AspectList balanced = a(ModAspects.CRYSTAL, 1);
        for (Holder<Aspect> primal : ModAspects.primals()) {
            balanced = balanced.add(primal, 1);
        }
        item(ModItems.BALANCED_SHARD, balanced);

        item(ModItems.IRON_CLUSTER, a(ModAspects.ORDER, 1).add(ModAspects.METAL, 6).add(ModAspects.EARTH, 1));
        item(ModItems.GOLD_CLUSTER, a(ModAspects.ORDER, 1).add(ModAspects.METAL, 4).add(ModAspects.EARTH, 1).add(ModAspects.DESIRE, 2));
        item(ModItems.COPPER_CLUSTER, a(ModAspects.ORDER, 1).add(ModAspects.METAL, 5).add(ModAspects.EARTH, 1));
        item(ModItems.CINNABAR_CLUSTER, a(ModAspects.ORDER, 1).add(ModAspects.METAL, 4).add(ModAspects.EARTH, 1)
                .add(ModAspects.EXCHANGE, 4).add(ModAspects.DEATH, 1));

        item(ModItems.ALCHEMIUM_NUGGET, a(ModAspects.METAL, 1));
        item(ModItems.BRASS_NUGGET, a(ModAspects.METAL, 1));
        item(ModItems.QUICKSILVER_DROP, a(ModAspects.METAL, 1));
        item(ModItems.SALIS_MUNDUS, a(ModAspects.FLUX, 2).add(ModAspects.ENERGY, 2));

        // our trees are livelier than ordinary ones, and the silverwood hums with aura
        item(ModBlocks.GREATWOOD.log(), a(ModAspects.PLANT, 3).add(ModAspects.LIFE, 1));
        item(ModBlocks.GREATWOOD.sapling(), a(ModAspects.PLANT, 2).add(ModAspects.LIFE, 2));
        item(ModBlocks.SILVERWOOD.log(), a(ModAspects.PLANT, 3).add(ModAspects.AURA, 1));
        item(ModBlocks.SILVERWOOD.sapling(), a(ModAspects.PLANT, 2).add(ModAspects.AURA, 2));

        item(ModBlocks.SHIMMERLEAF, a(ModAspects.PLANT, 2).add(ModAspects.AURA, 2).add(ModAspects.ENERGY, 1));
        item(ModBlocks.CINDERPEARL, a(ModAspects.PLANT, 2).add(ModAspects.AURA, 1).add(ModAspects.FIRE, 2));
        item(ModBlocks.VISHROOM, a(ModAspects.PLANT, 2).add(ModAspects.DEATH, 1).add(ModAspects.AURA, 1).add(ModAspects.ENTROPY, 1));

        for (StoneSet set : ModBlocks.STONE_SETS) {
            item(set.block(), a(ModAspects.EARTH, 1).add(ModAspects.ENERGY, 1));
        }
    }

    private void mobAspects() {
        AspectList beast = a(ModAspects.BEAST, 4).add(ModAspects.LIFE, 2);
        for (EntityType<?> animal : new EntityType<?>[] {EntityType.COW, EntityType.PIG, EntityType.SHEEP,
                EntityType.CHICKEN, EntityType.RABBIT, EntityType.HORSE, EntityType.WOLF, EntityType.CAT}) {
            mob(animal, beast);
        }

        AspectList undead = a(ModAspects.UNDEAD, 4).add(ModAspects.DEATH, 2).add(ModAspects.ENTROPY, 1);
        for (EntityType<?> risen : new EntityType<?>[] {EntityType.ZOMBIE, EntityType.SKELETON, EntityType.HUSK,
                EntityType.DROWNED, EntityType.STRAY, EntityType.WITHER_SKELETON, EntityType.ZOMBIE_VILLAGER}) {
            mob(risen, undead);
        }

        mob(EntityType.CREEPER, a(ModAspects.FIRE, 4).add(ModAspects.ENTROPY, 4).add(ModAspects.PLANT, 2));
        mob(EntityType.SPIDER, a(ModAspects.BEAST, 4).add(ModAspects.SENSES, 2).add(ModAspects.TRAP, 2));
        mob(EntityType.ENDERMAN, a(ModAspects.ELDRITCH, 6).add(ModAspects.MOTION, 4).add(ModAspects.DARKNESS, 2));
        mob(EntityType.BLAZE, a(ModAspects.FIRE, 8).add(ModAspects.ENERGY, 4));
        mob(EntityType.VILLAGER, a(ModAspects.MAN, 6).add(ModAspects.EXCHANGE, 2).add(ModAspects.MIND, 2));
        mob(EntityType.WITCH, a(ModAspects.MAN, 4).add(ModAspects.AURA, 4).add(ModAspects.MIND, 2));
    }

    private static Holder<Aspect> aspectOf(CrystalType type) {
        return switch (type) {
            case AIR -> ModAspects.AIR;
            case FIRE -> ModAspects.FIRE;
            case WATER -> ModAspects.WATER;
            case EARTH -> ModAspects.EARTH;
            case ORDER -> ModAspects.ORDER;
            case ENTROPY -> ModAspects.ENTROPY;
            case FLUX -> ModAspects.FLUX;
        };
    }

    private static AspectList a(Holder<Aspect> aspect, int amount) {
        return AspectList.of(aspect, amount);
    }

    private void item(ItemLike item, AspectList aspects) {
        builder(ModDataMaps.ITEM_ASPECTS).add(item.asItem().builtInRegistryHolder(), aspects, false);
    }

    private void tag(TagKey<Item> tag, AspectList aspects) {
        builder(ModDataMaps.ITEM_ASPECTS).add(tag, aspects, false);
    }

    private void mob(EntityType<?> type, AspectList aspects) {
        builder(ModDataMaps.ENTITY_ASPECTS).add(type.builtInRegistryHolder(), aspects, false);
    }
}
