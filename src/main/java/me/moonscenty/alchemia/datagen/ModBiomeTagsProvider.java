package me.moonscenty.alchemia.datagen;

import java.util.concurrent.CompletableFuture;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.registry.ModTags;
import me.moonscenty.alchemia.worldgen.ModBiomes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBiomeTagsProvider extends BiomeTagsProvider {
    public ModBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Alchemia.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ModTags.Biomes.HAS_GREATWOOD)
                .addTag(Tags.Biomes.IS_MAGICAL)
                .add(Biomes.FOREST, Biomes.FLOWER_FOREST, Biomes.BIRCH_FOREST, Biomes.OLD_GROWTH_BIRCH_FOREST,
                        Biomes.DARK_FOREST, Biomes.WINDSWEPT_FOREST, Biomes.SPARSE_JUNGLE);
        tag(ModTags.Biomes.HAS_SOME_GREATWOOD)
                .addTag(Tags.Biomes.IS_LUSH);
        tag(ModTags.Biomes.HAS_LITTLE_GREATWOOD)
                .add(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS, Biomes.MEADOW, Biomes.SWAMP, Biomes.MANGROVE_SWAMP,
                        Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU, Biomes.WINDSWEPT_SAVANNA, Biomes.TAIGA,
                        Biomes.SNOWY_TAIGA, Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA, Biomes.GROVE);
        tag(ModTags.Biomes.HAS_SILVERWOOD)
                .addTag(Tags.Biomes.IS_MAGICAL)
                .add(Biomes.FOREST, Biomes.BIRCH_FOREST)
                .remove(ModBiomes.MAGICAL_FOREST);

        tag(ModTags.Biomes.HAS_ORES)
                .addTag(BiomeTags.IS_OVERWORLD)
                .addTag(BiomeTags.IS_END);
        tag(ModTags.Biomes.HAS_NODES)
                .addTag(BiomeTags.IS_OVERWORLD)
                .addTag(BiomeTags.IS_NETHER)
                .addTag(BiomeTags.IS_END);
        tag(ModTags.Biomes.HAS_PECHS)
                .addTag(Tags.Biomes.IS_MAGICAL)
                .remove(ModBiomes.TAINTED_LAND);

        tag(ModTags.Biomes.HAS_CINDERPEARL)
                .add(Biomes.DESERT)
                .addTag(BiomeTags.IS_BADLANDS);

        tag(ModTags.Biomes.HAS_ANGRY_ZOMBIES)
                .addTag(BiomeTags.IS_FOREST)
                .addTag(BiomeTags.IS_TAIGA)
                .addTag(BiomeTags.IS_HILL)
                .addTag(BiomeTags.IS_SAVANNA)
                .add(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS, Biomes.SWAMP, Biomes.DESERT, Biomes.SNOWY_PLAINS,
                        Biomes.ICE_SPIKES, ModBiomes.MAGICAL_FOREST);

        tag(ModTags.Biomes.NODE_DARKENS_INTO)
                .add(ModBiomes.EERIE);

        tag(Tags.Biomes.IS_MAGICAL).add(ModBiomes.MAGICAL_FOREST,
                ModBiomes.EERIE, ModBiomes.TAINTED_LAND);
        tag(Tags.Biomes.IS_FOREST).add(ModBiomes.MAGICAL_FOREST);
        tag(Tags.Biomes.IS_SPOOKY).add(ModBiomes.EERIE);
        tag(Tags.Biomes.IS_WASTELAND).add(ModBiomes.TAINTED_LAND);
        tag(BiomeTags.IS_OVERWORLD).add(ModBiomes.MAGICAL_FOREST,
                ModBiomes.EERIE, ModBiomes.TAINTED_LAND);
    }
}
