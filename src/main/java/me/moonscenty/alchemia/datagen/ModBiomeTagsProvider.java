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
                .addTag(BiomeTags.IS_FOREST)
                .add(Biomes.PLAINS, Biomes.MEADOW, Biomes.SWAMP);
        tag(ModTags.Biomes.HAS_SILVERWOOD)
                .addTag(Tags.Biomes.IS_MAGICAL)
                .add(Biomes.BIRCH_FOREST, Biomes.OLD_GROWTH_BIRCH_FOREST, Biomes.FLOWER_FOREST);

        tag(ModTags.Biomes.HAS_CINDERPEARL)
                .add(Biomes.DESERT)
                .addTag(BiomeTags.IS_BADLANDS);

        tag(ModTags.Biomes.HAS_ANGRY_ZOMBIES)
                .addTag(BiomeTags.IS_FOREST)
                .addTag(BiomeTags.IS_TAIGA)
                .addTag(BiomeTags.IS_HILL)
                .addTag(BiomeTags.IS_SAVANNA)
                .add(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS, Biomes.SWAMP, Biomes.DESERT, Biomes.SNOWY_PLAINS,
                        Biomes.ICE_SPIKES);

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
