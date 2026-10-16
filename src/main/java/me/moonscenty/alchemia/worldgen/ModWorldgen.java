package me.moonscenty.alchemia.worldgen;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aura.node.NodeType;
import me.moonscenty.alchemia.block.CrystalType;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModFeatures;
import me.moonscenty.alchemia.registry.ModTags;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.data.worldgen.features.MiscOverworldFeatures;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.placement.SurfaceWaterDepthFilter;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModWorldgen {
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_AMBER = configured("ore_amber");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_CINNABAR = configured("ore_cinnabar");
    public static final Map<CrystalType, ResourceKey<ConfiguredFeature<?, ?>>> CRYSTAL_PATCHES = new EnumMap<>(CrystalType.class);

    public static final ResourceKey<ConfiguredFeature<?, ?>> CINDERPEARL_PATCH = configured("cinderpearl_patch");
    public static final ResourceKey<PlacedFeature> CINDERPEARL_PATCH_PLACED = placed("cinderpearl_patch");
    public static final ResourceKey<BiomeModifier> ADD_CINDERPEARL = biomeModifier("add_cinderpearl");

    public static final ResourceKey<ConfiguredFeature<?, ?>> GREATWOOD_TREE = configured("greatwood_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SILVERWOOD_TREE = configured("silverwood_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> BIG_MAGIC_TREE = configured("big_magic_tree");

    public static final ResourceKey<PlacedFeature> GREATWOOD_TREE_PLACED = placed("greatwood_tree");
    public static final ResourceKey<PlacedFeature> SILVERWOOD_TREE_PLACED = placed("silverwood_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> AURA_NODE = configured("aura_node");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ELDRITCH_RING = configured("eldritch_ring");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CULTIST_PORTAL = configured("cultist_portal");
    public static final ResourceKey<PlacedFeature> ELDRITCH_RING_PLACED = placed("eldritch_ring");
    public static final ResourceKey<PlacedFeature> CULTIST_PORTAL_PLACED = placed("cultist_portal");
    public static final ResourceKey<BiomeModifier> ADD_ELDRITCH = biomeModifier("add_eldritch");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MOUND = configured("mound");
    public static final ResourceKey<PlacedFeature> MOUND_PLACED = placed("mound");
    public static final ResourceKey<BiomeModifier> ADD_MOUND = biomeModifier("add_mound");
    public static final ResourceKey<PlacedFeature> AURA_NODE_PLACED = placed("aura_node");
    public static final ResourceKey<PlacedFeature> ORE_AMBER_PLACED = placed("ore_amber");
    public static final ResourceKey<PlacedFeature> ORE_CINNABAR_PLACED = placed("ore_cinnabar");
    public static final Map<CrystalType, ResourceKey<PlacedFeature>> CRYSTAL_PATCHES_PLACED = new EnumMap<>(CrystalType.class);

    // the magical forest: its mix of trees, its boulders and huge mushrooms, its vishrooms and glimmering grass
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAGICAL_FOREST_TREES = configured("magical_forest_trees");
    public static final ResourceKey<PlacedFeature> MAGICAL_FOREST_TREES_PLACED = placed("magical_forest_trees");
    public static final ResourceKey<PlacedFeature> MAGICAL_FOREST_ROCKS_PLACED = placed("magical_forest_rocks");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAGICAL_FOREST_MUSHROOMS =
            configured("magical_forest_mushrooms");
    public static final ResourceKey<PlacedFeature> MAGICAL_FOREST_MUSHROOMS_PLACED = placed("magical_forest_mushrooms");
    public static final ResourceKey<ConfiguredFeature<?, ?>> VISHROOMS_BY_TREES = configured("vishrooms_by_trees");
    public static final ResourceKey<PlacedFeature> VISHROOMS_BY_TREES_PLACED = placed("vishrooms_by_trees");
    public static final ResourceKey<ConfiguredFeature<?, ?>> AMBIENT_GRASS = configured("ambient_grass");
    public static final ResourceKey<PlacedFeature> AMBIENT_GRASS_PLACED = placed("ambient_grass");

    public static final ResourceKey<BiomeModifier> ADD_ORES = biomeModifier("add_ores");
    public static final ResourceKey<BiomeModifier> ADD_CRYSTALS = biomeModifier("add_crystals");
    public static final ResourceKey<BiomeModifier> ADD_NODES = biomeModifier("add_nodes");
    public static final ResourceKey<BiomeModifier> ADD_GREATWOOD = biomeModifier("add_greatwood");
    public static final ResourceKey<BiomeModifier> ADD_SILVERWOOD = biomeModifier("add_silverwood");
    public static final ResourceKey<BiomeModifier> SPAWN_ANGRY_ZOMBIES = biomeModifier("spawn_angry_zombies");
    public static final ResourceKey<BiomeModifier> SPAWN_FIREBATS = biomeModifier("spawn_firebats");
    public static final ResourceKey<BiomeModifier> SPAWN_WISPS = biomeModifier("spawn_wisps");
    public static final ResourceKey<BiomeModifier> SPAWN_PECHS = biomeModifier("spawn_pechs");

    static {
        for (CrystalType type : CrystalType.values()) {
            if (type.generatesNaturally()) {
                CRYSTAL_PATCHES.put(type, configured(type.getName() + "_crystal_patch"));
                CRYSTAL_PATCHES_PLACED.put(type, placed(type.getName() + "_crystal_patch"));
            }
        }
    }

    public static void bootstrapConfigured(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        TagMatchTest stone = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        TagMatchTest deepslate = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);

        // The original scattered single ore blocks, so the veins are kept very small
        context.register(ORE_AMBER, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(List.of(
                OreConfiguration.target(stone, ModBlocks.AMBER_ORE.get().defaultBlockState()),
                OreConfiguration.target(deepslate, ModBlocks.DEEPSLATE_AMBER_ORE.get().defaultBlockState())), 3)));
        context.register(ORE_CINNABAR, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(List.of(
                OreConfiguration.target(stone, ModBlocks.CINNABAR_ORE.get().defaultBlockState()),
                OreConfiguration.target(deepslate, ModBlocks.DEEPSLATE_CINNABAR_ORE.get().defaultBlockState())), 3)));

        // 12-22 blocks tall with several leaf balls, 7-10 blocks tall with one tall canopy
        context.register(GREATWOOD_TREE, new ConfiguredFeature<>(Feature.TREE, new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(ModBlocks.GREATWOOD.log().get()),
                new GreatwoodTrunkPlacer(12, 6, 4),
                BlockStateProvider.simple(ModBlocks.GREATWOOD.leaves().get()),
                new SphereFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 1),
                new TwoLayersFeatureSize(1, 1, 2)).ignoreVines().build()));
        context.register(SILVERWOOD_TREE, new ConfiguredFeature<>(Feature.TREE, new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(ModBlocks.SILVERWOOD.log().get()),
                new SilverwoodTrunkPlacer(7, 3, 0),
                BlockStateProvider.simple(ModBlocks.SILVERWOOD.leaves().get()),
                new SphereFoliagePlacer(ConstantInt.of(3), ConstantInt.of(0), 3),
                new TwoLayersFeatureSize(1, 1, 2))
                // shimmerleaf carpets the ground under a silverwood, with vishrooms here and there, and about one
                // tree in three has a small pure node in the heart of its trunk
                .decorators(List.of(
                        new UndergrowthDecorator(BlockStateProvider.simple(ModBlocks.SHIMMERLEAF.get()), BlockTags.DIRT, 18, 8),
                        new UndergrowthDecorator(BlockStateProvider.simple(ModBlocks.VISHROOM.get()), BlockTags.DIRT, 10, 6),
                        new NodeInTreeDecorator(0.3F, NodeType.PURE, 0.33F)))
                .ignoreVines().build()));

        // the original's big magic tree, the magical forest's own oak: the great oak's way of growing, but 11 to 22
        // blocks to its crown where a great oak stands 5 to 16, and leaf balls three layers deep instead of four
        context.register(BIG_MAGIC_TREE, new ConfiguredFeature<>(Feature.TREE, new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(Blocks.OAK_LOG),
                new net.minecraft.world.level.levelgen.feature.trunkplacers.FancyTrunkPlacer(9, 11, 0),
                BlockStateProvider.simple(Blocks.OAK_LEAVES),
                new net.minecraft.world.level.levelgen.feature.foliageplacers.FancyFoliagePlacer(ConstantInt.of(2),
                        ConstantInt.of(3), 3),
                new TwoLayersFeatureSize(0, 0, 0, java.util.OptionalInt.of(4))).ignoreVines().build()));

        context.register(CINDERPEARL_PATCH, new ConfiguredFeature<>(Feature.FLOWER, FeatureUtils.simpleRandomPatchConfiguration(18,
                PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK,
                        new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.CINDERPEARL.get()))))));

        context.register(AURA_NODE, new ConfiguredFeature<>(ModFeatures.AURA_NODE.get(), NoneFeatureConfiguration.INSTANCE));
        context.register(ELDRITCH_RING, new ConfiguredFeature<>(ModFeatures.ELDRITCH_RING.get(),
                NoneFeatureConfiguration.INSTANCE));
        context.register(CULTIST_PORTAL, new ConfiguredFeature<>(ModFeatures.CULTIST_PORTAL.get(),
                NoneFeatureConfiguration.INSTANCE));
        context.register(MOUND, new ConfiguredFeature<>(ModFeatures.MOUND.get(), NoneFeatureConfiguration.INSTANCE));

        // the magical forest's trees: mostly big magic trees, one in ten of the rest a greatwood, one in fourteen of all a
        // silverwood, each only where its sapling could stand
        HolderGetter<ConfiguredFeature<?, ?>> features = context.lookup(Registries.CONFIGURED_FEATURE);
        context.register(MAGICAL_FOREST_TREES, new ConfiguredFeature<>(Feature.RANDOM_SELECTOR,
                new RandomFeatureConfiguration(List.of(
                        new WeightedPlacedFeature(PlacementUtils.inlinePlaced(
                                features.getOrThrow(SILVERWOOD_TREE),
                                PlacementUtils.filteredByBlockSurvival(ModBlocks.SILVERWOOD.sapling().get())),
                                1.0F / 14.0F),
                        new WeightedPlacedFeature(PlacementUtils.inlinePlaced(
                                features.getOrThrow(GREATWOOD_TREE),
                                PlacementUtils.filteredByBlockSurvival(ModBlocks.GREATWOOD.sapling().get())),
                                13.0F / 140.0F)),
                        PlacementUtils.inlinePlaced(
                                features.getOrThrow(BIG_MAGIC_TREE),
                                PlacementUtils.filteredByBlockSurvival(Blocks.OAK_SAPLING)))));
        // huge mushrooms, brown or red
        context.register(MAGICAL_FOREST_MUSHROOMS, new ConfiguredFeature<>(Feature.RANDOM_BOOLEAN_SELECTOR,
                new RandomBooleanFeatureConfiguration(
                        PlacementUtils.inlinePlaced(features.getOrThrow(
                                TreeFeatures.HUGE_BROWN_MUSHROOM)),
                        PlacementUtils.inlinePlaced(features.getOrThrow(
                                TreeFeatures.HUGE_RED_MUSHROOM)))));
        context.register(VISHROOMS_BY_TREES, new ConfiguredFeature<>(ModFeatures.VISHROOMS_BY_TREES.get(),
                NoneFeatureConfiguration.INSTANCE));
        context.register(AMBIENT_GRASS, new ConfiguredFeature<>(ModFeatures.AMBIENT_GRASS.get(),
                NoneFeatureConfiguration.INSTANCE));

        CRYSTAL_PATCHES.forEach((type, key) -> context.register(key, new ConfiguredFeature<>(ModFeatures.CRYSTAL_PATCH.get(),
                new BlockStateConfiguration(ModBlocks.CRYSTALS.get(type).get().defaultBlockState()))));
    }

    public static void bootstrapPlaced(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configured = context.lookup(Registries.CONFIGURED_FEATURE);

        // Amber sits close to the surface, cinnabar deep down
        context.register(ORE_AMBER_PLACED, new PlacedFeature(configured.getOrThrow(ORE_AMBER),
                ore(12, HeightRangePlacement.triangle(VerticalAnchor.absolute(32), VerticalAnchor.absolute(128)))));
        context.register(ORE_CINNABAR_PLACED, new PlacedFeature(configured.getOrThrow(ORE_CINNABAR),
                ore(10, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(40)))));

        context.register(CINDERPEARL_PATCH_PLACED, new PlacedFeature(configured.getOrThrow(CINDERPEARL_PATCH),
                List.of(RarityFilter.onAverageOnceEvery(30), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome())));

        context.register(GREATWOOD_TREE_PLACED, new PlacedFeature(configured.getOrThrow(GREATWOOD_TREE),
                tree(14, ModBlocks.GREATWOOD.sapling().get())));
        context.register(SILVERWOOD_TREE_PLACED, new PlacedFeature(configured.getOrThrow(SILVERWOOD_TREE),
                tree(40, ModBlocks.SILVERWOOD.sapling().get())));

        // a ring in about one chunk in sixty-six, a portal in one in five hundred: the original's odds
        // a mound one chunk in a hundred and fifty, as the original
        context.register(MOUND_PLACED, new PlacedFeature(configured.getOrThrow(MOUND),
                List.of(RarityFilter.onAverageOnceEvery(150), InSquarePlacement.spread(),
                        net.minecraft.world.level.levelgen.placement.HeightmapPlacement.onHeightmap(
                                net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG),
                        BiomeFilter.biome())));
        context.register(ELDRITCH_RING_PLACED, new PlacedFeature(configured.getOrThrow(ELDRITCH_RING),
                List.of(RarityFilter.onAverageOnceEvery(66), InSquarePlacement.spread(),
                        net.minecraft.world.level.levelgen.placement.HeightmapPlacement.onHeightmap(
                                net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG),
                        BiomeFilter.biome())));
        context.register(CULTIST_PORTAL_PLACED, new PlacedFeature(configured.getOrThrow(CULTIST_PORTAL),
                List.of(RarityFilter.onAverageOnceEvery(500), InSquarePlacement.spread(),
                        net.minecraft.world.level.levelgen.placement.HeightmapPlacement.onHeightmap(
                                net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG),
                        BiomeFilter.biome())));

        // one chunk in thirty or so gets a node, which is about what the original worked out to
        context.register(AURA_NODE_PLACED, new PlacedFeature(configured.getOrThrow(AURA_NODE),
                List.of(RarityFilter.onAverageOnceEvery(30), InSquarePlacement.spread(), BiomeFilter.biome())));

        // two trees a chunk and now and then a third, as the original's forest had
        context.register(MAGICAL_FOREST_TREES_PLACED, new PlacedFeature(configured.getOrThrow(MAGICAL_FOREST_TREES),
                VegetationPlacements.treePlacement(
                        PlacementUtils.countExtra(2, 0.1F, 1))));
        // up to two mossy boulders a chunk
        context.register(MAGICAL_FOREST_ROCKS_PLACED, new PlacedFeature(configured.getOrThrow(
                MiscOverworldFeatures.FOREST_ROCK),
                List.of(CountPlacement.of(UniformInt.of(0, 2)),
                        InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome())));
        // a huge mushroom in about two chunks of five
        context.register(MAGICAL_FOREST_MUSHROOMS_PLACED, new PlacedFeature(configured.getOrThrow(
                MAGICAL_FOREST_MUSHROOMS), List.of(RarityFilter.onAverageOnceEvery(3), InSquarePlacement.spread(),
                        PlacementUtils.HEIGHTMAP, BiomeFilter.biome())));
        context.register(VISHROOMS_BY_TREES_PLACED, new PlacedFeature(configured.getOrThrow(VISHROOMS_BY_TREES),
                List.of(BiomeFilter.biome())));
        context.register(AMBIENT_GRASS_PLACED, new PlacedFeature(configured.getOrThrow(AMBIENT_GRASS),
                List.of(BiomeFilter.biome())));

        CRYSTAL_PATCHES_PLACED.forEach((type, key) -> context.register(key, new PlacedFeature(configured.getOrThrow(CRYSTAL_PATCHES.get(type)),
                ore(2, HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(8), VerticalAnchor.absolute(64))))));
    }

    public static void bootstrapBiomeModifiers(BootstrapContext<BiomeModifier> context) {
        HolderGetter<PlacedFeature> placed = context.lookup(Registries.PLACED_FEATURE);
        HolderSet<Biome> overworld = context.lookup(Registries.BIOME).getOrThrow(BiomeTags.IS_OVERWORLD);

        context.register(ADD_ORES, new BiomeModifiers.AddFeaturesBiomeModifier(overworld,
                HolderSet.direct(placed.getOrThrow(ORE_AMBER_PLACED), placed.getOrThrow(ORE_CINNABAR_PLACED)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        context.register(ADD_GREATWOOD, new BiomeModifiers.AddFeaturesBiomeModifier(biomes.getOrThrow(ModTags.Biomes.HAS_GREATWOOD),
                HolderSet.direct(placed.getOrThrow(GREATWOOD_TREE_PLACED)), GenerationStep.Decoration.VEGETAL_DECORATION));
        context.register(ADD_SILVERWOOD, new BiomeModifiers.AddFeaturesBiomeModifier(biomes.getOrThrow(ModTags.Biomes.HAS_SILVERWOOD),
                HolderSet.direct(placed.getOrThrow(SILVERWOOD_TREE_PLACED)), GenerationStep.Decoration.VEGETAL_DECORATION));

        context.register(ADD_CINDERPEARL, new BiomeModifiers.AddFeaturesBiomeModifier(biomes.getOrThrow(ModTags.Biomes.HAS_CINDERPEARL),
                HolderSet.direct(placed.getOrThrow(CINDERPEARL_PATCH_PLACED)), GenerationStep.Decoration.VEGETAL_DECORATION));

        context.register(ADD_ELDRITCH, new BiomeModifiers.AddFeaturesBiomeModifier(overworld,
                HolderSet.direct(placed.getOrThrow(ELDRITCH_RING_PLACED), placed.getOrThrow(CULTIST_PORTAL_PLACED)),
                GenerationStep.Decoration.SURFACE_STRUCTURES));

        context.register(ADD_MOUND, new BiomeModifiers.AddFeaturesBiomeModifier(overworld,
                HolderSet.direct(placed.getOrThrow(MOUND_PLACED)), GenerationStep.Decoration.SURFACE_STRUCTURES));

        context.register(ADD_NODES, new BiomeModifiers.AddFeaturesBiomeModifier(overworld,
                HolderSet.direct(placed.getOrThrow(AURA_NODE_PLACED)), GenerationStep.Decoration.TOP_LAYER_MODIFICATION));

        List<Holder<PlacedFeature>> crystals = new ArrayList<>();
        CRYSTAL_PATCHES_PLACED.values().forEach(key -> crystals.add(placed.getOrThrow(key)));
        context.register(ADD_CRYSTALS, new BiomeModifiers.AddFeaturesBiomeModifier(overworld,
                HolderSet.direct(crystals), GenerationStep.Decoration.UNDERGROUND_DECORATION));

        // the original's spawns: an angry zombie now and then among the others, and firebats in the Nether
        context.register(SPAWN_ANGRY_ZOMBIES, BiomeModifiers.AddSpawnsBiomeModifier.singleSpawn(
                biomes.getOrThrow(ModTags.Biomes.HAS_ANGRY_ZOMBIES),
                new net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData(
                        me.moonscenty.alchemia.registry.ModEntities.ANGRY_ZOMBIE.get(), 10, 1, 1)));
        context.register(SPAWN_FIREBATS, BiomeModifiers.AddSpawnsBiomeModifier.singleSpawn(
                biomes.getOrThrow(BiomeTags.IS_NETHER),
                new net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData(
                        me.moonscenty.alchemia.registry.ModEntities.FIRE_BAT.get(), 10, 1, 2)));
        // and wisps, one at a time, in the Nether too
        context.register(SPAWN_WISPS, BiomeModifiers.AddSpawnsBiomeModifier.singleSpawn(
                biomes.getOrThrow(BiomeTags.IS_NETHER),
                new net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData(
                        me.moonscenty.alchemia.registry.ModEntities.WISP.get(), 5, 1, 1)));
        // and pechs in magical places, one at a time
        context.register(SPAWN_PECHS, BiomeModifiers.AddSpawnsBiomeModifier.singleSpawn(
                biomes.getOrThrow(net.neoforged.neoforge.common.Tags.Biomes.IS_MAGICAL),
                new net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData(
                        me.moonscenty.alchemia.registry.ModEntities.PECH.get(), 10, 1, 2)));
    }

    private static List<PlacementModifier> ore(int count, PlacementModifier height) {
        return List.of(CountPlacement.of(count), InSquarePlacement.spread(), height, BiomeFilter.biome());
    }

    /** A lone tree roughly once every {@code rarity} chunks, on dry ground where its sapling could stand. */
    private static List<PlacementModifier> tree(int rarity, Block sapling) {
        return List.of(RarityFilter.onAverageOnceEvery(rarity), InSquarePlacement.spread(), SurfaceWaterDepthFilter.forMaxDepth(0),
                PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, BiomeFilter.biome(), PlacementUtils.filteredByBlockSurvival(sapling));
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> configured(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, Alchemia.id(name));
    }

    private static ResourceKey<PlacedFeature> placed(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Alchemia.id(name));
    }

    private static ResourceKey<BiomeModifier> biomeModifier(String name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, Alchemia.id(name));
    }
}
