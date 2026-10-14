package me.moonscenty.alchemia.datagen;

import me.moonscenty.alchemia.research.ModResearch;
import me.moonscenty.alchemia.worldgen.ModWorldgen;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Everything this mod ships as datapack content, gathered in one place for the generator.
 */
public class ModDatapackEntries {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, ModWorldgen::bootstrapConfigured)
            .add(Registries.PLACED_FEATURE, ModWorldgen::bootstrapPlaced)
            .add(Registries.BIOME, me.moonscenty.alchemia.worldgen.ModBiomes::bootstrap)
            .add(Registries.DIMENSION_TYPE, me.moonscenty.alchemia.outer.OuterLandsData::dimensionType)
            .add(Registries.LEVEL_STEM, me.moonscenty.alchemia.outer.OuterLandsData::levelStem)
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModWorldgen::bootstrapBiomeModifiers)
            .add(ModResearch.CATEGORY_KEY, ModResearchProvider::categories)
            .add(ModResearch.ENTRY_KEY, ModResearchProvider::entries);

    private ModDatapackEntries() {
    }
}
