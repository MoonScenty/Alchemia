package me.moonscenty.alchemia.worldgen;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.registry.ModEntities;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * The original's three lands of the overworld.
 *
 * <p>The magical forest is the only one the world makes of itself: a bright blue-green forest of great oaks with the
 * odd greatwood and silverwood, mushrooms large and small, mossy boulders and vishrooms at the feet of the trees,
 * where pechs and wisps live. The eerie land and the tainted land are never made; they are painted on the world,
 * the one by dark nodes and the other by taint as it spreads, and each is taken back when what made it is gone.
 *
 * <p>Colours, climates and what lives there are the original's.
 */
public final class ModBiomes {
    public static final ResourceKey<Biome> MAGICAL_FOREST = key("magical_forest");
    public static final ResourceKey<Biome> EERIE = key("eerie");
    public static final ResourceKey<Biome> TAINTED_LAND = key("tainted_land");
    /** The Outer Lands' only land. */
    public static final ResourceKey<Biome> ELDRITCH = key("eldritch");

    /** The sky every land the original left to its default had, at the forest's warmth. */
    private static final int FOG = 0xC0D8FF;
    private static final int WATER_FOG = 0x050533;

    private ModBiomes() {
    }

    private static ResourceKey<Biome> key(String name) {
        return ResourceKey.create(Registries.BIOME, Alchemia.id(name));
    }

    public static void bootstrap(BootstrapContext<Biome> context) {
        HolderGetter<PlacedFeature> placed = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> carvers = context.lookup(Registries.CONFIGURED_CARVER);
        context.register(MAGICAL_FOREST, magicalForest(placed, carvers));
        context.register(EERIE, eerie(placed, carvers));
        context.register(TAINTED_LAND, taintedLand(placed, carvers));
        context.register(ELDRITCH, eldritch(placed, carvers));
    }

    /** What every overworld land has under and around it: caves, lakes, geodes, springs, dungeons, ice. */
    private static void everywhere(BiomeGenerationSettings.Builder generation) {
        BiomeDefaultFeatures.addDefaultCarversAndLakes(generation);
        BiomeDefaultFeatures.addDefaultCrystalFormations(generation);
        BiomeDefaultFeatures.addDefaultMonsterRoom(generation);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(generation);
        BiomeDefaultFeatures.addDefaultSprings(generation);
        BiomeDefaultFeatures.addSurfaceFreezing(generation);
        BiomeDefaultFeatures.addDefaultOres(generation);
        BiomeDefaultFeatures.addDefaultSoftDisks(generation);
    }

    private static Biome magicalForest(HolderGetter<PlacedFeature> placed,
            HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(placed, carvers);
        everywhere(generation);
        BiomeDefaultFeatures.addForestFlowers(generation);
        generation.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS,
                placed.getOrThrow(ModWorldgen.MAGICAL_FOREST_ROCKS_PLACED));
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION,
                placed.getOrThrow(ModWorldgen.MAGICAL_FOREST_MUSHROOMS_PLACED));
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION,
                placed.getOrThrow(ModWorldgen.MAGICAL_FOREST_TREES_PLACED));
        BiomeDefaultFeatures.addDefaultFlowers(generation);
        BiomeDefaultFeatures.addForestGrass(generation);
        BiomeDefaultFeatures.addDefaultMushrooms(generation);
        BiomeDefaultFeatures.addDefaultExtraVegetation(generation);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_WATERLILY);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION,
                placed.getOrThrow(ModWorldgen.VISHROOMS_BY_TREES_PLACED));
        generation.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION,
                placed.getOrThrow(ModWorldgen.AMBIENT_GRASS_PLACED));

        MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(spawns);
        BiomeDefaultFeatures.commonSpawns(spawns);
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 2, 1, 3));
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.HORSE, 2, 1, 3));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.WITCH, 3, 1, 1));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 3, 1, 1));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.WISP.get(), 10, 1, 2));

        return land(0.7F, 0.6F, 0x55FF81, 0x66FFC5, sky(0.7F), 0x0077EE, spawns, generation,
                net.minecraft.sounds.Musics.createGameMusic(SoundEvents.MUSIC_BIOME_FOREST));
    }

    private static Biome eerie(HolderGetter<PlacedFeature> placed, HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(placed, carvers);
        everywhere(generation);
        MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(spawns);
        BiomeDefaultFeatures.monsters(spawns, 95, 5, 100, false);
        spawns.addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.BAT, 3, 1, 1));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.WITCH, 8, 1, 1));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 4, 1, 1));
        spawns.addSpawn(MobCategory.MONSTER,
                new MobSpawnSettings.SpawnerData(ModEntities.ANGRY_ZOMBIE.get(), 32, 1, 1));
        spawns.addSpawn(MobCategory.MONSTER,
                new MobSpawnSettings.SpawnerData(ModEntities.FURIOUS_ZOMBIE.get(), 8, 1, 1));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.WISP.get(), 3, 1, 1));
        spawns.addSpawn(MobCategory.MONSTER,
                new MobSpawnSettings.SpawnerData(ModEntities.ELDRITCH_GUARDIAN.get(), 1, 1, 1));
        return land(0.5F, 0.5F, 0x404840, 0x404840, 0x222299, 0x2E535F, spawns, generation, null);
    }

    private static Biome taintedLand(HolderGetter<PlacedFeature> placed,
            HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(placed, carvers);
        everywhere(generation);
        MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.TAINTACLE.get(), 1, 1, 1));
        return land(0.8F, 0.4F, 0x6D4189, 0x7C6D87, 0x7C44FF, 0xCC1188, spawns, generation, null);
    }

    /** Nothing grows; the inhabited dead and the guardians walk the mazes, one at a time. The sky is black. */
    private static Biome eldritch(HolderGetter<PlacedFeature> placed, HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        spawns.addSpawn(MobCategory.MONSTER,
                new MobSpawnSettings.SpawnerData(ModEntities.INHABITED_ZOMBIE.get(), 1, 1, 1));
        spawns.addSpawn(MobCategory.MONSTER,
                new MobSpawnSettings.SpawnerData(ModEntities.ELDRITCH_GUARDIAN.get(), 1, 1, 1));
        BiomeSpecialEffects effects = new BiomeSpecialEffects.Builder()
                .waterColor(0x3F76E4)
                .waterFogColor(WATER_FOG)
                .fogColor(0x181318)
                .skyColor(0)
                .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                .build();
        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(0.5F)
                .downfall(0.0F)
                .specialEffects(effects)
                .mobSpawnSettings(spawns.build())
                .generationSettings(new BiomeGenerationSettings.Builder(placed, carvers).build())
                .build();
    }

    private static Biome land(float temperature, float downfall, int grass, int foliage, int sky, int water,
            MobSpawnSettings.Builder spawns, BiomeGenerationSettings.Builder generation, Music music) {
        BiomeSpecialEffects.Builder effects = new BiomeSpecialEffects.Builder()
                .waterColor(water)
                .waterFogColor(WATER_FOG)
                .fogColor(FOG)
                .skyColor(sky)
                .grassColorOverride(grass)
                .foliageColorOverride(foliage)
                .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS);
        if (music != null) {
            effects.backgroundMusic(music);
        }
        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(temperature)
                .downfall(downfall)
                .specialEffects(effects.build())
                .mobSpawnSettings(spawns.build())
                .generationSettings(generation.build())
                .build();
    }

    /** The sky the game gives a land of this warmth. */
    private static int sky(float temperature) {
        float cool = Mth.clamp(temperature / 3.0F, -1.0F, 1.0F);
        return Mth.hsvToRgb(0.62222224F - cool * 0.05F, 0.5F + cool * 0.1F, 1.0F);
    }
}
