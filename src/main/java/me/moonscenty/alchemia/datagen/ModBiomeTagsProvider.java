package me.moonscenty.alchemia.datagen;

import java.util.concurrent.CompletableFuture;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.registry.ModTags;
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
        // Silverwood only takes root where the land is touched by magic, or among birches
        tag(ModTags.Biomes.HAS_SILVERWOOD)
                .addTag(Tags.Biomes.IS_MAGICAL)
                .add(Biomes.BIRCH_FOREST, Biomes.OLD_GROWTH_BIRCH_FOREST, Biomes.FLOWER_FOREST);

        // Hot, dry places where the original spawned cinderpearls on open sand
        tag(ModTags.Biomes.HAS_CINDERPEARL)
                .add(Biomes.DESERT)
                .addTag(BiomeTags.IS_BADLANDS);

        // stands in until there is somewhere properly eerie for a dark node to make
        tag(ModTags.Biomes.NODE_DARKENS_INTO)
                .add(Biomes.DESERT);
    }
}
