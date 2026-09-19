package me.moonscenty.alchemia.datagen;

import java.util.concurrent.CompletableFuture;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Alchemia.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ModTags.Blocks.ORES_AMBER).add(ModBlocks.AMBER_ORE.get(), ModBlocks.DEEPSLATE_AMBER_ORE.get());
        tag(ModTags.Blocks.ORES_CINNABAR).add(ModBlocks.CINNABAR_ORE.get(), ModBlocks.DEEPSLATE_CINNABAR_ORE.get());
        tag(Tags.Blocks.ORES).addTag(ModTags.Blocks.ORES_AMBER).addTag(ModTags.Blocks.ORES_CINNABAR);
        tag(Tags.Blocks.ORES_IN_GROUND_STONE).add(ModBlocks.AMBER_ORE.get(), ModBlocks.CINNABAR_ORE.get());
        tag(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE).add(ModBlocks.DEEPSLATE_AMBER_ORE.get(), ModBlocks.DEEPSLATE_CINNABAR_ORE.get());
        tag(Tags.Blocks.ORE_RATES_SINGULAR).add(ModBlocks.CINNABAR_ORE.get(), ModBlocks.DEEPSLATE_CINNABAR_ORE.get());
        tag(Tags.Blocks.ORE_RATES_DENSE).add(ModBlocks.AMBER_ORE.get(), ModBlocks.DEEPSLATE_AMBER_ORE.get());

        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .addTag(ModTags.Blocks.ORES_AMBER)
                .addTag(ModTags.Blocks.ORES_CINNABAR)
                .addTag(ModTags.Blocks.CRYSTALS);
        tag(BlockTags.NEEDS_STONE_TOOL).addTag(ModTags.Blocks.ORES_AMBER);
        tag(BlockTags.NEEDS_IRON_TOOL).addTag(ModTags.Blocks.ORES_CINNABAR);

        ModBlocks.CRYSTALS.values().forEach(crystal -> tag(ModTags.Blocks.CRYSTALS).add(crystal.get()));
        tag(ModTags.Blocks.CRYSTAL_GROWABLE)
                .addTag(BlockTags.BASE_STONE_OVERWORLD)
                .addTag(BlockTags.BASE_STONE_NETHER);
    }
}
