package me.moonscenty.alchemia.datagen;

import java.util.concurrent.CompletableFuture;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aspect.ModDataMaps;
import me.moonscenty.alchemia.aura.AuraTrait;
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
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
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
        biomeAura();
    }

    /**
     * What the land does to the magic over it, carried over from the original's table of biome types.
     * <p>
     * Written against biome tags rather than named biomes, so anything a pack adds is covered by whatever tags it
     * already carries.
     * <p>
     * A biome carrying several of these tags ends up with whichever entry the generated file happens to list last,
     * and that file is sorted by tag name, so which one wins is arbitrary. It is close enough while every entry is
     * within half a step of the others; a subject that needs it settled should be given its biome an entry of its
     * own, which beats any tag.
     */
    private void biomeAura() {
        var aura = builder(AuraTrait.MAP);

        // wet land holds its magic well
        land(aura, Tags.Biomes.IS_AQUATIC, 1.0F, ModAspects.WATER);
        land(aura, BiomeTags.IS_OCEAN, 1.0F, ModAspects.WATER);
        land(aura, BiomeTags.IS_RIVER, 1.0F, ModAspects.WATER);
        land(aura, Tags.Biomes.IS_WET, 1.0F, ModAspects.WATER);
        land(aura, Tags.Biomes.IS_LUSH, 1.2F, ModAspects.WATER);

        land(aura, Tags.Biomes.IS_HOT, 1.0F, ModAspects.FIRE);
        land(aura, Tags.Biomes.IS_DESERT, 0.75F, ModAspects.FIRE);
        land(aura, BiomeTags.IS_NETHER, 0.5F, ModAspects.FIRE);
        land(aura, BiomeTags.IS_BADLANDS, 1.0F, ModAspects.FIRE);
        land(aura, Tags.Biomes.IS_SPOOKY, 0.75F, ModAspects.FIRE);

        land(aura, Tags.Biomes.IS_DENSE_VEGETATION, 1.0F, ModAspects.ORDER);
        land(aura, Tags.Biomes.IS_SNOWY, 0.75F, ModAspects.ORDER);
        land(aura, Tags.Biomes.IS_COLD, 0.75F, ModAspects.ORDER);
        land(aura, Tags.Biomes.IS_ICY, 1.0F, ModAspects.ORDER);
        land(aura, Tags.Biomes.IS_MUSHROOM, 1.3F, ModAspects.ORDER);
        land(aura, Tags.Biomes.IS_MAGICAL, 1.0F, ModAspects.ORDER);

        land(aura, Tags.Biomes.IS_CONIFEROUS_TREE, 1.0F, ModAspects.EARTH);
        land(aura, BiomeTags.IS_FOREST, 1.2F, ModAspects.EARTH);
        land(aura, Tags.Biomes.IS_SANDY, 0.75F, ModAspects.EARTH);
        land(aura, BiomeTags.IS_BEACH, 0.75F, ModAspects.EARTH);
        land(aura, BiomeTags.IS_JUNGLE, 1.2F, ModAspects.EARTH);

        land(aura, BiomeTags.IS_SAVANNA, 0.75F, ModAspects.AIR);
        land(aura, BiomeTags.IS_MOUNTAIN, 0.75F, ModAspects.AIR);
        land(aura, BiomeTags.IS_HILL, 1.0F, ModAspects.AIR);
        land(aura, Tags.Biomes.IS_PLAINS, 0.75F, ModAspects.AIR);
        land(aura, BiomeTags.IS_END, 0.5F, ModAspects.AIR);

        // land that has little left in it, and holds little
        land(aura, Tags.Biomes.IS_DRY, 0.5F, ModAspects.ENTROPY);
        land(aura, Tags.Biomes.IS_SPARSE_VEGETATION, 0.75F, ModAspects.ENTROPY);
        land(aura, Tags.Biomes.IS_SWAMP, 1.2F, ModAspects.ENTROPY);
        land(aura, Tags.Biomes.IS_WASTELAND, 0.5F, ModAspects.ENTROPY);
        land(aura, Tags.Biomes.IS_DEAD, 0.25F, ModAspects.ENTROPY);
    }

    private void land(Builder<AuraTrait, Biome> aura, TagKey<Biome> where, float level, Holder<Aspect> leaning) {
        aura.add(where, new AuraTrait(level, leaning), true);
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
        item(me.moonscenty.alchemia.registry.ModItems.ZOMBIE_BRAIN.get(),
                a(ModAspects.LIFE, 2).add(ModAspects.MIND, 4).add(ModAspects.UNDEAD, 2));
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

        item(ModItems.GOLEM, a(ModAspects.MOTION, 3).add(ModAspects.MAN, 3).add(ModAspects.MECHANISM, 3));
        item(ModItems.ALCHEMIUM_NUGGET, a(ModAspects.METAL, 1));
        item(ModItems.BRASS_NUGGET, a(ModAspects.METAL, 1));
        item(ModItems.QUICKSILVER_DROP, a(ModAspects.METAL, 1));
        item(ModItems.SALIS_MUNDUS, a(ModAspects.FLUX, 2).add(ModAspects.ENERGY, 2));
        // a breath of auram, besides the two points of whatever the wisp was
        item(ModItems.WISPY_ESSENCE, a(ModAspects.AURA, 1));
        item(ModItems.COIN, a(ModAspects.DESIRE, 1));
        item(ModItems.TAINTED_GOO, a(ModAspects.FLUX, 2).add(ModAspects.WATER, 1).add(ModAspects.AURA, 1));
        item(ModItems.TAINT_TENDRIL, a(ModAspects.FLUX, 2).add(ModAspects.DESIRE, 1).add(ModAspects.AURA, 1));
        item(ModItems.KNOWLEDGE_FRAGMENT, a(ModAspects.MIND, 8));

        // our trees are livelier than ordinary ones, and the silverwood hums with aura
        item(ModBlocks.GREATWOOD.log(), a(ModAspects.PLANT, 3).add(ModAspects.LIFE, 1));
        item(ModBlocks.GREATWOOD.sapling(), a(ModAspects.PLANT, 2).add(ModAspects.LIFE, 2));
        item(ModBlocks.SILVERWOOD.log(), a(ModAspects.PLANT, 3).add(ModAspects.AURA, 1));
        item(ModBlocks.SILVERWOOD.sapling(), a(ModAspects.PLANT, 2).add(ModAspects.AURA, 2));

        item(ModBlocks.SHIMMERLEAF, a(ModAspects.PLANT, 2).add(ModAspects.AURA, 2).add(ModAspects.ENERGY, 1));
        item(ModBlocks.CINDERPEARL, a(ModAspects.PLANT, 2).add(ModAspects.AURA, 1).add(ModAspects.FIRE, 2));
        item(ModBlocks.VISHROOM, a(ModAspects.PLANT, 2).add(ModAspects.DEATH, 1).add(ModAspects.AURA, 1).add(ModAspects.ENTROPY, 1));

        for (StoneSet set : java.util.List.of(ModBlocks.ARCANE_STONE, ModBlocks.ARCANE_STONE_BRICKS)) {
            item(set.block(), a(ModAspects.EARTH, 1).add(ModAspects.ENERGY, 1));
        }
        for (var old : java.util.List.of(ModBlocks.ANCIENT_STONE.block(), ModBlocks.ELDRITCH_STONE.block(),
                ModBlocks.ANCIENT_ROCK, ModBlocks.ANCIENT_TILE)) {
            item(old, a(ModAspects.EARTH, 1).add(ModAspects.ELDRITCH, 1));
        }
        // a candle is a light and a little of what it was rendered from
        // an arrow, and the primal in it
        ModItems.PRIMAL_ARROWS.forEach((type, arrow) -> item(arrow, a(ModAspects.AVERSION, 1).add(type.aspect(), 1)));
        ModBlocks.TALLOW_CANDLES.values().forEach(candle ->
                item(candle, a(ModAspects.LIGHT, 1).add(ModAspects.LIFE, 1)));
        item(ModBlocks.CRUSTED_STONE, a(ModAspects.EARTH, 1).add(ModAspects.ELDRITCH, 1).add(ModAspects.LIFE, 1));
        item(ModBlocks.GLOWING_CRUSTED_STONE,
                a(ModAspects.EARTH, 1).add(ModAspects.ELDRITCH, 1).add(ModAspects.LIFE, 1).add(ModAspects.LIGHT, 1));
        item(ModBlocks.ANCIENT_DOORWAY, a(ModAspects.METAL, 1).add(ModAspects.ELDRITCH, 1).add(ModAspects.TRAP, 1));
        item(ModBlocks.GLYPHED_STONE, a(ModAspects.METAL, 1).add(ModAspects.ELDRITCH, 1).add(ModAspects.MIND, 1));
        item(ModBlocks.POROUS_STONE, a(ModAspects.EARTH, 1).add(ModAspects.VOID, 1));
        // the baubles found rather than made
        for (var ring : me.moonscenty.alchemia.registry.ModItems.APPRENTICE_RINGS.values()) {
            item(ring, a(ModAspects.AURA, 2).add(ModAspects.METAL, 1).add(ring.get().primal(), 5));
        }
        item(me.moonscenty.alchemia.registry.ModItems.VIS_STONE, a(ModAspects.AURA, 2).add(ModAspects.METAL, 1)
                .add(ModAspects.ENERGY, 5));
        item(me.moonscenty.alchemia.registry.ModItems.PROTECTION_RING, a(ModAspects.METAL, 1)
                .add(ModAspects.PROTECT, 3).add(ModAspects.ENERGY, 3));
        item(me.moonscenty.alchemia.registry.ModItems.PRIMORDIAL_PEARL, a(ModAspects.AIR, 16).add(ModAspects.EARTH, 16)
                .add(ModAspects.FIRE, 16).add(ModAspects.WATER, 16).add(ModAspects.ORDER, 16)
                .add(ModAspects.ENTROPY, 16).add(ModAspects.FLUX, 16));
        item(me.moonscenty.alchemia.registry.ModItems.ELDRITCH_EYE, a(ModAspects.ELDRITCH, 5).add(ModAspects.AURA, 3)
                .add(ModAspects.SENSES, 3).add(ModAspects.SOUL, 3));
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
        for (EntityType<?> cultist : new EntityType<?>[] {me.moonscenty.alchemia.registry.ModEntities.CULTIST_KNIGHT.get(),
                me.moonscenty.alchemia.registry.ModEntities.CULTIST_CLERIC.get()}) {
            mob(cultist, a(ModAspects.ELDRITCH, 1).add(ModAspects.MAN, 2).add(ModAspects.AVERSION, 1));
        }
        mob(me.moonscenty.alchemia.registry.ModEntities.ELDRITCH_GUARDIAN.get(),
                a(ModAspects.ELDRITCH, 4).add(ModAspects.DEATH, 2).add(ModAspects.UNDEAD, 4));
        // the original's, which it copied by mistake from the tainted creeper's
        mob(me.moonscenty.alchemia.registry.ModEntities.MIND_SPIDER.get(), a(ModAspects.FLUX, 2).add(ModAspects.FIRE, 2));
        mob(me.moonscenty.alchemia.registry.ModEntities.ELDRITCH_CRAB.get(),
                a(ModAspects.ELDRITCH, 1).add(ModAspects.BEAST, 1).add(ModAspects.TRAP, 1));
        mob(me.moonscenty.alchemia.registry.ModEntities.INHABITED_ZOMBIE.get(),
                a(ModAspects.ELDRITCH, 1).add(ModAspects.UNDEAD, 1).add(ModAspects.MAN, 1));
        for (String plate : new String[] {"crimson_plate_helm", "crimson_plate_chest", "crimson_plate_legs"}) {
            item(me.moonscenty.alchemia.registry.ModItems.CRIMSON.get(plate).get(),
                    a(ModAspects.METAL, 5).add(ModAspects.ELDRITCH, 1));
        }
        for (String robe : new String[] {"crimson_robe_helm", "crimson_robe_chest", "crimson_robe_legs"}) {
            item(me.moonscenty.alchemia.registry.ModItems.CRIMSON.get(robe).get(),
                    a(ModAspects.METAL, 3).add(ModAspects.BEAST, 1).add(ModAspects.ELDRITCH, 1));
        }
        item(me.moonscenty.alchemia.registry.ModItems.CRIMSON.get("crimson_boots").get(),
                a(ModAspects.METAL, 4).add(ModAspects.ELDRITCH, 1));
        item(me.moonscenty.alchemia.registry.ModItems.CRIMSON_BLADE.get(),
                a(ModAspects.METAL, 4).add(ModAspects.ELDRITCH, 1).add(ModAspects.DEATH, 1));
        item(me.moonscenty.alchemia.registry.ModItems.CRIMSON_RITES.get(),
                a(ModAspects.MIND, 5).add(ModAspects.ELDRITCH, 3).add(ModAspects.SOUL, 3));
        // any wisp is a little auram and air, and two of its own aspect besides
        mob(me.moonscenty.alchemia.registry.ModEntities.WISP.get(), a(ModAspects.AURA, 1).add(ModAspects.AIR, 1));
        // a golem, as the original had it
        mob(me.moonscenty.alchemia.registry.ModEntities.GOLEM.get(),
                a(ModAspects.AIR, 2).add(ModAspects.EARTH, 2).add(ModAspects.MOTION, 2));
        // the creatures of the taint, as the original had them; it gave the crawler nothing
        mob(me.moonscenty.alchemia.registry.ModEntities.TAINT_SWARM.get(),
                a(ModAspects.FLUX, 3).add(ModAspects.AIR, 3));
        mob(me.moonscenty.alchemia.registry.ModEntities.TAINTACLE.get(),
                a(ModAspects.FLUX, 3).add(ModAspects.WATER, 2));
        mob(me.moonscenty.alchemia.registry.ModEntities.TAINTACLE_TINY.get(),
                a(ModAspects.FLUX, 1).add(ModAspects.WATER, 1));
        // the animals it makes over, and its slime, as the original had them
        mob(me.moonscenty.alchemia.registry.ModEntities.TAINTED_COW.get(),
                a(ModAspects.FLUX, 3).add(ModAspects.EARTH, 3));
        mob(me.moonscenty.alchemia.registry.ModEntities.TAINTED_PIG.get(),
                a(ModAspects.FLUX, 2).add(ModAspects.EARTH, 2));
        mob(me.moonscenty.alchemia.registry.ModEntities.TAINTED_SHEEP.get(),
                a(ModAspects.FLUX, 2).add(ModAspects.EARTH, 2));
        mob(me.moonscenty.alchemia.registry.ModEntities.TAINTED_CHICKEN.get(),
                a(ModAspects.FLUX, 2).add(ModAspects.FLIGHT, 2).add(ModAspects.AIR, 1));
        mob(me.moonscenty.alchemia.registry.ModEntities.TAINTED_RABBIT.get(),
                a(ModAspects.FLUX, 2).add(ModAspects.MOTION, 2));
        mob(me.moonscenty.alchemia.registry.ModEntities.TAINTED_VILLAGER.get(),
                a(ModAspects.FLUX, 3).add(ModAspects.AIR, 2));
        mob(me.moonscenty.alchemia.registry.ModEntities.TAINTED_CREEPER.get(),
                a(ModAspects.FLUX, 2).add(ModAspects.FIRE, 2));
        mob(me.moonscenty.alchemia.registry.ModEntities.ALCHEMIC_SLIME.get(),
                a(ModAspects.LIFE, 2).add(ModAspects.WATER, 2).add(ModAspects.FLUX, 1));
        // what every pech is; each kind adds its own
        mob(me.moonscenty.alchemia.registry.ModEntities.PECH.get(),
                a(ModAspects.MAN, 2).add(ModAspects.AURA, 2).add(ModAspects.EXCHANGE, 2));
        mob(me.moonscenty.alchemia.registry.ModEntities.ANGRY_ZOMBIE.get(),
                a(ModAspects.UNDEAD, 3).add(ModAspects.MAN, 1).add(ModAspects.MIND, 1).add(ModAspects.EARTH, 1));
        mob(me.moonscenty.alchemia.registry.ModEntities.FURIOUS_ZOMBIE.get(),
                a(ModAspects.UNDEAD, 4).add(ModAspects.MAN, 2).add(ModAspects.MIND, 1).add(ModAspects.EARTH, 2));
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
