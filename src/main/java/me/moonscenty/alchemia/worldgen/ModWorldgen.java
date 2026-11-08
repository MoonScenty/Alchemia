package me.moonscenty.alchemia.worldgen;

import java.util.List;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aura.node.NodeType;
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
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceBlockConfiguration;
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
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.HeightmapPlacement;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModWorldgen {
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_AMBER = configured("ore_amber");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_CINNABAR = configured("ore_cinnabar");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CRYSTALS = configured("crystals");

    public static final ResourceKey<ConfiguredFeature<?, ?>> CINDERPEARL_PATCH = configured("cinderpearl_patch");
    public static final ResourceKey<PlacedFeature> CINDERPEARL_PATCH_PLACED = placed("cinderpearl_patch");
    public static final ResourceKey<BiomeModifier> ADD_CINDERPEARL = biomeModifier("add_cinderpearl");

    public static final ResourceKey<ConfiguredFeature<?, ?>> GREATWOOD_TREE = configured("greatwood_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> WILD_GREATWOOD_TREE = configured("wild_greatwood_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SILVERWOOD_TREE = configured("silverwood_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TALL_SILVERWOOD_TREE = configured("tall_silverwood_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> BIG_MAGIC_TREE = configured("big_magic_tree");

    public static final ResourceKey<PlacedFeature> GREATWOOD_TREE_PLACED = placed("greatwood_tree");
    public static final ResourceKey<PlacedFeature> SOME_GREATWOOD_TREE_PLACED = placed("some_greatwood_tree");
    public static final ResourceKey<PlacedFeature> LITTLE_GREATWOOD_TREE_PLACED = placed("little_greatwood_tree");
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
    public static final ResourceKey<PlacedFeature> CRYSTALS_PLACED = placed("crystals");

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
    public static final ResourceKey<BiomeModifier> ADD_SOME_GREATWOOD = biomeModifier("add_some_greatwood");
    public static final ResourceKey<BiomeModifier> ADD_LITTLE_GREATWOOD = biomeModifier("add_little_greatwood");
    public static final ResourceKey<BiomeModifier> ADD_SILVERWOOD = biomeModifier("add_silverwood");
    public static final ResourceKey<BiomeModifier> SPAWN_ANGRY_ZOMBIES = biomeModifier("spawn_angry_zombies");
    public static final ResourceKey<BiomeModifier> SPAWN_FIREBATS = biomeModifier("spawn_firebats");
    public static final ResourceKey<BiomeModifier> SPAWN_HALLOWEEN_FIREBATS = biomeModifier("spawn_halloween_firebats");
    public static final ResourceKey<BiomeModifier> SPAWN_WISPS = biomeModifier("spawn_wisps");
    public static final ResourceKey<BiomeModifier> SPAWN_PECHS = biomeModifier("spawn_pechs");

    public static void bootstrapConfigured(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        // original's ores are single blocks, and only in plain stone
        BlockMatchTest stone = new BlockMatchTest(Blocks.STONE);
        BlockMatchTest deepslate = new BlockMatchTest(Blocks.DEEPSLATE);
        context.register(ORE_AMBER, new ConfiguredFeature<>(Feature.REPLACE_SINGLE_BLOCK, new ReplaceBlockConfiguration(List.of(
                OreConfiguration.target(stone, ModBlocks.AMBER_ORE.get().defaultBlockState()),
                OreConfiguration.target(deepslate, ModBlocks.DEEPSLATE_AMBER_ORE.get().defaultBlockState())))));
        context.register(ORE_CINNABAR, new ConfiguredFeature<>(Feature.REPLACE_SINGLE_BLOCK, new ReplaceBlockConfiguration(List.of(
                OreConfiguration.target(stone, ModBlocks.CINNABAR_ORE.get().defaultBlockState()),
                OreConfiguration.target(deepslate, ModBlocks.DEEPSLATE_CINNABAR_ORE.get().defaultBlockState())))));

        // greatwood 12-22 tall with several leaf balls; silverwood 7-10 tall with one tall canopy
        context.register(GREATWOOD_TREE, new ConfiguredFeature<>(Feature.TREE, greatwood().build()));
        // original: one wild greatwood in 8 shelters a spider nest, never one grown from a sapling
        context.register(WILD_GREATWOOD_TREE, new ConfiguredFeature<>(Feature.TREE, greatwood()
                .decorators(List.of(new SpiderNestDecorator(1.0F / 8.0F))).build()));
        // original's silverwood is 7-10 tall, 8-12 inside the magical forest
        context.register(SILVERWOOD_TREE, silverwood(new SilverwoodTrunkPlacer(7, 3, 0)));
        context.register(TALL_SILVERWOOD_TREE, silverwood(new SilverwoodTrunkPlacer(8, 4, 0)));

        // original's big magic tree: fancy-oak growth, 11-22 to the crown, leaf balls three layers deep
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

        // mostly big magic trees; 1 in 10 of the rest greatwood, 1 in 14 overall silverwood
        HolderGetter<ConfiguredFeature<?, ?>> features = context.lookup(Registries.CONFIGURED_FEATURE);
        context.register(MAGICAL_FOREST_TREES, new ConfiguredFeature<>(Feature.RANDOM_SELECTOR,
                new RandomFeatureConfiguration(List.of(
                        new WeightedPlacedFeature(PlacementUtils.inlinePlaced(
                                features.getOrThrow(TALL_SILVERWOOD_TREE),
                                PlacementUtils.filteredByBlockSurvival(ModBlocks.SILVERWOOD.sapling().get())),
                                1.0F / 14.0F),
                        new WeightedPlacedFeature(PlacementUtils.inlinePlaced(
                                features.getOrThrow(WILD_GREATWOOD_TREE),
                                PlacementUtils.filteredByBlockSurvival(ModBlocks.GREATWOOD.sapling().get())),
                                0.1F)),
                        PlacementUtils.inlinePlaced(
                                features.getOrThrow(BIG_MAGIC_TREE),
                                PlacementUtils.filteredByBlockSurvival(Blocks.OAK_SAPLING)))));
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

        context.register(CRYSTALS, new ConfiguredFeature<>(ModFeatures.CRYSTAL_CLUSTERS.get(),
                NoneFeatureConfiguration.INSTANCE));
    }

    private static TreeConfiguration.TreeConfigurationBuilder greatwood() {
        return new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(ModBlocks.GREATWOOD.log().get()),
                new GreatwoodTrunkPlacer(12, 6, 4),
                BlockStateProvider.simple(ModBlocks.GREATWOOD.leaves().get()),
                new SphereFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 1),
                new TwoLayersFeatureSize(1, 1, 2)).ignoreVines();
    }

    private static ConfiguredFeature<?, ?> silverwood(SilverwoodTrunkPlacer trunk) {
        return new ConfiguredFeature<>(Feature.TREE, new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(ModBlocks.SILVERWOOD.log().get()),
                trunk,
                BlockStateProvider.simple(ModBlocks.SILVERWOOD.leaves().get()),
                new SphereFoliagePlacer(ConstantInt.of(3), ConstantInt.of(0), 3),
                new TwoLayersFeatureSize(1, 1, 2))
                // about one silverwood in three gets a small pure node in its trunk
                .decorators(List.of(
                        new UndergrowthDecorator(BlockStateProvider.simple(ModBlocks.SHIMMERLEAF.get()), BlockTags.DIRT, 18, 8),
                        new UndergrowthDecorator(BlockStateProvider.simple(ModBlocks.VISHROOM.get()), BlockTags.DIRT, 10, 6),
                        new NodeInTreeDecorator(0.3F, NodeType.PURE, 0.33F)))
                .ignoreVines().build());
    }

    public static void bootstrapPlaced(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configured = context.lookup(Registries.CONFIGURED_FEATURE);

        // original's ores: 20 amber up to 24 under the surface, 18 cinnabar at Y 0-50
        context.register(ORE_AMBER_PLACED, new PlacedFeature(configured.getOrThrow(ORE_AMBER),
                List.of(CountPlacement.of(20), InSquarePlacement.spread(),
                        new BelowSurfacePlacement(24))));
        context.register(ORE_CINNABAR_PLACED, new PlacedFeature(configured.getOrThrow(ORE_CINNABAR),
                List.of(CountPlacement.of(18), InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(50)))));

        context.register(CINDERPEARL_PATCH_PLACED, new PlacedFeature(configured.getOrThrow(CINDERPEARL_PATCH),
                List.of(RarityFilter.onAverageOnceEvery(30), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome())));

        // original's 1 in 25 chunks times the biome's greatwood chance (1, 0.5 or 0.2), silverwood 1 in 60
        context.register(GREATWOOD_TREE_PLACED, new PlacedFeature(configured.getOrThrow(WILD_GREATWOOD_TREE),
                tree(25, ModBlocks.GREATWOOD.sapling().get())));
        context.register(SOME_GREATWOOD_TREE_PLACED, new PlacedFeature(configured.getOrThrow(WILD_GREATWOOD_TREE),
                tree(50, ModBlocks.GREATWOOD.sapling().get())));
        context.register(LITTLE_GREATWOOD_TREE_PLACED, new PlacedFeature(configured.getOrThrow(WILD_GREATWOOD_TREE),
                tree(125, ModBlocks.GREATWOOD.sapling().get())));
        context.register(SILVERWOOD_TREE_PLACED, new PlacedFeature(configured.getOrThrow(SILVERWOOD_TREE),
                tree(60, ModBlocks.SILVERWOOD.sapling().get())));

        // original's odds: ring ~1 in 66 chunks, portal 1 in 500, mound 1 in 150
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

        context.register(AURA_NODE_PLACED, new PlacedFeature(configured.getOrThrow(AURA_NODE), List.of()));

        // original's density
        context.register(MAGICAL_FOREST_TREES_PLACED, new PlacedFeature(configured.getOrThrow(MAGICAL_FOREST_TREES),
                VegetationPlacements.treePlacement(
                        PlacementUtils.countExtra(2, 0.1F, 1))));
        context.register(MAGICAL_FOREST_ROCKS_PLACED, new PlacedFeature(configured.getOrThrow(
                MiscOverworldFeatures.FOREST_ROCK),
                List.of(CountPlacement.of(UniformInt.of(0, 2)),
                        InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome())));
        // original's 16 tries a chunk at 1 in 40
        context.register(MAGICAL_FOREST_MUSHROOMS_PLACED, new PlacedFeature(configured.getOrThrow(
                MAGICAL_FOREST_MUSHROOMS), List.of(CountPlacement.of(16), RarityFilter.onAverageOnceEvery(40),
                        InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome())));
        context.register(VISHROOMS_BY_TREES_PLACED, new PlacedFeature(configured.getOrThrow(VISHROOMS_BY_TREES),
                List.of(BiomeFilter.biome())));
        context.register(AMBIENT_GRASS_PLACED, new PlacedFeature(configured.getOrThrow(AMBIENT_GRASS),
                List.of(BiomeFilter.biome())));

        context.register(CRYSTALS_PLACED, new PlacedFeature(configured.getOrThrow(CRYSTALS), List.of()));
    }

    public static void bootstrapBiomeModifiers(BootstrapContext<BiomeModifier> context) {
        HolderGetter<PlacedFeature> placed = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderSet<Biome> overworld = biomes.getOrThrow(BiomeTags.IS_OVERWORLD);
        HolderSet<Biome> withOres = biomes.getOrThrow(ModTags.Biomes.HAS_ORES);

        context.register(ADD_ORES, new BiomeModifiers.AddFeaturesBiomeModifier(withOres,
                HolderSet.direct(placed.getOrThrow(ORE_AMBER_PLACED), placed.getOrThrow(ORE_CINNABAR_PLACED)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        context.register(ADD_GREATWOOD, new BiomeModifiers.AddFeaturesBiomeModifier(biomes.getOrThrow(ModTags.Biomes.HAS_GREATWOOD),
                HolderSet.direct(placed.getOrThrow(GREATWOOD_TREE_PLACED)), GenerationStep.Decoration.VEGETAL_DECORATION));
        context.register(ADD_SOME_GREATWOOD, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(ModTags.Biomes.HAS_SOME_GREATWOOD),
                HolderSet.direct(placed.getOrThrow(SOME_GREATWOOD_TREE_PLACED)), GenerationStep.Decoration.VEGETAL_DECORATION));
        context.register(ADD_LITTLE_GREATWOOD, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(ModTags.Biomes.HAS_LITTLE_GREATWOOD),
                HolderSet.direct(placed.getOrThrow(LITTLE_GREATWOOD_TREE_PLACED)), GenerationStep.Decoration.VEGETAL_DECORATION));
        context.register(ADD_SILVERWOOD, new BiomeModifiers.AddFeaturesBiomeModifier(biomes.getOrThrow(ModTags.Biomes.HAS_SILVERWOOD),
                HolderSet.direct(placed.getOrThrow(SILVERWOOD_TREE_PLACED)), GenerationStep.Decoration.VEGETAL_DECORATION));

        context.register(ADD_CINDERPEARL, new BiomeModifiers.AddFeaturesBiomeModifier(biomes.getOrThrow(ModTags.Biomes.HAS_CINDERPEARL),
                HolderSet.direct(placed.getOrThrow(CINDERPEARL_PATCH_PLACED)), GenerationStep.Decoration.VEGETAL_DECORATION));

        context.register(ADD_ELDRITCH, new BiomeModifiers.AddFeaturesBiomeModifier(overworld,
                HolderSet.direct(placed.getOrThrow(ELDRITCH_RING_PLACED), placed.getOrThrow(CULTIST_PORTAL_PLACED)),
                GenerationStep.Decoration.SURFACE_STRUCTURES));

        context.register(ADD_MOUND, new BiomeModifiers.AddFeaturesBiomeModifier(overworld,
                HolderSet.direct(placed.getOrThrow(MOUND_PLACED)), GenerationStep.Decoration.SURFACE_STRUCTURES));

        context.register(ADD_NODES, new BiomeModifiers.AddFeaturesBiomeModifier(biomes.getOrThrow(ModTags.Biomes.HAS_NODES),
                HolderSet.direct(placed.getOrThrow(AURA_NODE_PLACED)), GenerationStep.Decoration.TOP_LAYER_MODIFICATION));

        context.register(ADD_CRYSTALS, new BiomeModifiers.AddFeaturesBiomeModifier(withOres,
                HolderSet.direct(placed.getOrThrow(CRYSTALS_PLACED)), GenerationStep.Decoration.UNDERGROUND_DECORATION));

        // original's spawns
        context.register(SPAWN_ANGRY_ZOMBIES, BiomeModifiers.AddSpawnsBiomeModifier.singleSpawn(
                biomes.getOrThrow(ModTags.Biomes.HAS_ANGRY_ZOMBIES),
                new net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData(
                        me.moonscenty.alchemia.registry.ModEntities.ANGRY_ZOMBIE.get(), 10, 1, 1)));
        context.register(SPAWN_FIREBATS, BiomeModifiers.AddSpawnsBiomeModifier.singleSpawn(
                biomes.getOrThrow(BiomeTags.IS_NETHER),
                new net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData(
                        me.moonscenty.alchemia.registry.ModEntities.FIRE_BAT.get(), 10, 1, 2)));
        // original: on 31 October fire bats join the angry zombies' lands
        context.register(SPAWN_HALLOWEEN_FIREBATS, new HalloweenSpawnsModifier(
                biomes.getOrThrow(ModTags.Biomes.HAS_ANGRY_ZOMBIES),
                new net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData(
                        me.moonscenty.alchemia.registry.ModEntities.FIRE_BAT.get(), 5, 1, 2)));
        context.register(SPAWN_WISPS, BiomeModifiers.AddSpawnsBiomeModifier.singleSpawn(
                biomes.getOrThrow(BiomeTags.IS_NETHER),
                new net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData(
                        me.moonscenty.alchemia.registry.ModEntities.WISP.get(), 5, 1, 1)));
        context.register(SPAWN_PECHS, BiomeModifiers.AddSpawnsBiomeModifier.singleSpawn(
                biomes.getOrThrow(ModTags.Biomes.HAS_PECHS),
                new net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData(
                        me.moonscenty.alchemia.registry.ModEntities.PECH.get(), 10, 1, 1)));
    }

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
