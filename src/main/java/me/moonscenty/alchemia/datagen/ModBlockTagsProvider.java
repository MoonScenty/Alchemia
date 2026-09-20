package me.moonscenty.alchemia.datagen;

import java.util.concurrent.CompletableFuture;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModTags;
import me.moonscenty.alchemia.registry.StoneSet;
import me.moonscenty.alchemia.registry.WoodSet;
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

        tag(ModTags.Blocks.STORAGE_BLOCKS_ALCHEMIUM).add(ModBlocks.ALCHEMIUM_BLOCK.get());
        tag(ModTags.Blocks.STORAGE_BLOCKS_BRASS).add(ModBlocks.BRASS_BLOCK.get());
        tag(Tags.Blocks.STORAGE_BLOCKS).addTag(ModTags.Blocks.STORAGE_BLOCKS_ALCHEMIUM).addTag(ModTags.Blocks.STORAGE_BLOCKS_BRASS);
        tag(BlockTags.BEACON_BASE_BLOCKS).add(ModBlocks.ALCHEMIUM_BLOCK.get(), ModBlocks.BRASS_BLOCK.get());

        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .addTag(ModTags.Blocks.STORAGE_BLOCKS_ALCHEMIUM)
                .addTag(ModTags.Blocks.STORAGE_BLOCKS_BRASS)
                .addTag(ModTags.Blocks.ORES_AMBER)
                .addTag(ModTags.Blocks.ORES_CINNABAR)
                .addTag(ModTags.Blocks.CRYSTALS);
        tag(BlockTags.NEEDS_STONE_TOOL)
                .addTag(ModTags.Blocks.ORES_AMBER)
                .addTag(ModTags.Blocks.STORAGE_BLOCKS_ALCHEMIUM)
                .addTag(ModTags.Blocks.STORAGE_BLOCKS_BRASS);
        tag(BlockTags.NEEDS_IRON_TOOL).addTag(ModTags.Blocks.ORES_CINNABAR);

        for (WoodSet wood : ModBlocks.WOODS) {
            tag(wood.logsBlockTag()).add(wood.log().get());
            tag(BlockTags.LOGS_THAT_BURN).addTag(wood.logsBlockTag());
            tag(BlockTags.OVERWORLD_NATURAL_LOGS).add(wood.log().get());
            tag(BlockTags.PLANKS).add(wood.planks().get());
            tag(BlockTags.WOODEN_STAIRS).add(wood.stairs().get());
            tag(BlockTags.WOODEN_SLABS).add(wood.slab().get());
            tag(BlockTags.LEAVES).add(wood.leaves().get());
            tag(BlockTags.SAPLINGS).add(wood.sapling().get());
        }

        tag(ModTags.Blocks.STORAGE_BLOCKS_AMBER).add(ModBlocks.AMBER_BLOCK.get());
        tag(Tags.Blocks.STORAGE_BLOCKS).addTag(ModTags.Blocks.STORAGE_BLOCKS_AMBER);
        tag(BlockTags.BEACON_BASE_BLOCKS).add(ModBlocks.AMBER_BLOCK.get(), ModBlocks.AMBER_BRICKS.get());
        // Amber gives way to bare hands, but a pickaxe is still the quicker route
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.AMBER_BLOCK.get(), ModBlocks.AMBER_BRICKS.get());

        for (StoneSet set : ModBlocks.STONE_SETS) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(set.block().get(), set.stairs().get(), set.slab().get());
            tag(BlockTags.NEEDS_STONE_TOOL).add(set.block().get(), set.stairs().get(), set.slab().get());
            tag(BlockTags.STAIRS).add(set.stairs().get());
            tag(BlockTags.SLABS).add(set.slab().get());
        }

        tag(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.RESEARCH_TABLE.get());

        tag(ModTags.Blocks.CINDERPEARL_PLACEABLE)
                .addTag(BlockTags.SAND)
                .addTag(BlockTags.DIRT)
                .addTag(BlockTags.TERRACOTTA);
        ModBlocks.PLANTS.forEach(plant -> tag(BlockTags.SWORD_EFFICIENT).add(plant.get()));

        ModBlocks.CRYSTALS.values().forEach(crystal -> tag(ModTags.Blocks.CRYSTALS).add(crystal.get()));
        tag(ModTags.Blocks.CRYSTAL_GROWABLE)
                .addTag(BlockTags.BASE_STONE_OVERWORLD)
                .addTag(BlockTags.BASE_STONE_NETHER);
    }
}
