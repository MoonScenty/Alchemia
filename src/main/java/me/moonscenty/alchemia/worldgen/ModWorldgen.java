package me.moonscenty.alchemia.worldgen;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.CrystalType;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModFeatures;
import me.moonscenty.alchemia.registry.ModTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
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

    public static final ResourceKey<PlacedFeature> GREATWOOD_TREE_PLACED = placed("greatwood_tree");
    public static final ResourceKey<PlacedFeature> SILVERWOOD_TREE_PLACED = placed("silverwood_tree");
    public static final ResourceKey<PlacedFeature> ORE_AMBER_PLACED = placed("ore_amber");
    public static final ResourceKey<PlacedFeature> ORE_CINNABAR_PLACED = placed("ore_cinnabar");
    public static final Map<CrystalType, ResourceKey<PlacedFeature>> CRYSTAL_PATCHES_PLACED = new EnumMap<>(CrystalType.class);

    public static final ResourceKey<BiomeModifier> ADD_ORES = biomeModifier("add_ores");
    public static final ResourceKey<BiomeModifier> ADD_CRYSTALS = biomeModifier("add_crystals");
    public static final ResourceKey<BiomeModifier> ADD_GREATWOOD = biomeModifier("add_greatwood");
    public static final ResourceKey<BiomeModifier> ADD_SILVERWOOD = biomeModifier("add_silverwood");

    static {
        for (CrystalType type : CrystalType.values()) {
            if (type.generatesNaturally()) {
                CRYSTAL_PATCHES.put(type, configured(type.getName() + "_crystal_patch"));
                CRYSTAL_PATCHES_PLACED.put(type, placed(type.getName() + "_crystal_patch"));
            }
        }
    }

    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, ModWorldgen::bootstrapConfigured)
            .add(Registries.PLACED_FEATURE, ModWorldgen::bootstrapPlaced)
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModWorldgen::bootstrapBiomeModifiers);

    private static void bootstrapConfigured(BootstrapContext<ConfiguredFeature<?, ?>> context) {
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
                // shimmerleaf carpets the ground under a silverwood, with vishrooms here and there
                .decorators(List.of(
                        new UndergrowthDecorator(BlockStateProvider.simple(ModBlocks.SHIMMERLEAF.get()), BlockTags.DIRT, 18, 8),
                        new UndergrowthDecorator(BlockStateProvider.simple(ModBlocks.VISHROOM.get()), BlockTags.DIRT, 10, 6)))
                .ignoreVines().build()));

        context.register(CINDERPEARL_PATCH, new ConfiguredFeature<>(Feature.FLOWER, FeatureUtils.simpleRandomPatchConfiguration(18,
                PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK,
                        new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.CINDERPEARL.get()))))));

        CRYSTAL_PATCHES.forEach((type, key) -> context.register(key, new ConfiguredFeature<>(ModFeatures.CRYSTAL_PATCH.get(),
                new BlockStateConfiguration(ModBlocks.CRYSTALS.get(type).get().defaultBlockState()))));
    }

    private static void bootstrapPlaced(BootstrapContext<PlacedFeature> context) {
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

        CRYSTAL_PATCHES_PLACED.forEach((type, key) -> context.register(key, new PlacedFeature(configured.getOrThrow(CRYSTAL_PATCHES.get(type)),
                ore(2, HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(8), VerticalAnchor.absolute(64))))));
    }

    private static void bootstrapBiomeModifiers(BootstrapContext<BiomeModifier> context) {
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

        List<Holder<PlacedFeature>> crystals = new ArrayList<>();
        CRYSTAL_PATCHES_PLACED.values().forEach(key -> crystals.add(placed.getOrThrow(key)));
        context.register(ADD_CRYSTALS, new BiomeModifiers.AddFeaturesBiomeModifier(overworld,
                HolderSet.direct(crystals), GenerationStep.Decoration.UNDERGROUND_DECORATION));
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
