package me.moonscenty.alchemia.worldgen;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.CrystalType;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModFeatures;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModWorldgen {
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_AMBER = configured("ore_amber");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_CINNABAR = configured("ore_cinnabar");
    public static final Map<CrystalType, ResourceKey<ConfiguredFeature<?, ?>>> CRYSTAL_PATCHES = new EnumMap<>(CrystalType.class);

    public static final ResourceKey<PlacedFeature> ORE_AMBER_PLACED = placed("ore_amber");
    public static final ResourceKey<PlacedFeature> ORE_CINNABAR_PLACED = placed("ore_cinnabar");
    public static final Map<CrystalType, ResourceKey<PlacedFeature>> CRYSTAL_PATCHES_PLACED = new EnumMap<>(CrystalType.class);

    public static final ResourceKey<BiomeModifier> ADD_ORES = biomeModifier("add_ores");
    public static final ResourceKey<BiomeModifier> ADD_CRYSTALS = biomeModifier("add_crystals");

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

        CRYSTAL_PATCHES_PLACED.forEach((type, key) -> context.register(key, new PlacedFeature(configured.getOrThrow(CRYSTAL_PATCHES.get(type)),
                ore(2, HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(8), VerticalAnchor.absolute(64))))));
    }

    private static void bootstrapBiomeModifiers(BootstrapContext<BiomeModifier> context) {
        HolderGetter<PlacedFeature> placed = context.lookup(Registries.PLACED_FEATURE);
        HolderSet<Biome> overworld = context.lookup(Registries.BIOME).getOrThrow(BiomeTags.IS_OVERWORLD);

        context.register(ADD_ORES, new BiomeModifiers.AddFeaturesBiomeModifier(overworld,
                HolderSet.direct(placed.getOrThrow(ORE_AMBER_PLACED), placed.getOrThrow(ORE_CINNABAR_PLACED)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        List<Holder<PlacedFeature>> crystals = new ArrayList<>();
        CRYSTAL_PATCHES_PLACED.values().forEach(key -> crystals.add(placed.getOrThrow(key)));
        context.register(ADD_CRYSTALS, new BiomeModifiers.AddFeaturesBiomeModifier(overworld,
                HolderSet.direct(crystals), GenerationStep.Decoration.UNDERGROUND_DECORATION));
    }

    private static List<PlacementModifier> ore(int count, PlacementModifier height) {
        return List.of(CountPlacement.of(count), InSquarePlacement.spread(), height, BiomeFilter.biome());
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
