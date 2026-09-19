package me.moonscenty.alchemia.datagen;

import java.util.concurrent.CompletableFuture;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
            CompletableFuture<TagLookup<Block>> blockTags, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, Alchemia.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        copy(ModTags.Blocks.ORES_AMBER, ModTags.Items.ORES_AMBER);
        copy(ModTags.Blocks.ORES_CINNABAR, ModTags.Items.ORES_CINNABAR);
        copy(Tags.Blocks.ORES, Tags.Items.ORES);
        copy(Tags.Blocks.ORES_IN_GROUND_STONE, Tags.Items.ORES_IN_GROUND_STONE);
        copy(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE, Tags.Items.ORES_IN_GROUND_DEEPSLATE);
        copy(Tags.Blocks.ORE_RATES_SINGULAR, Tags.Items.ORE_RATES_SINGULAR);
        copy(Tags.Blocks.ORE_RATES_DENSE, Tags.Items.ORE_RATES_DENSE);

        tag(ModTags.Items.GEMS_AMBER).add(ModItems.AMBER.get());
        tag(ModTags.Items.GEMS_QUICKSILVER).add(ModItems.QUICKSILVER.get());
        tag(Tags.Items.GEMS).addTag(ModTags.Items.GEMS_AMBER).addTag(ModTags.Items.GEMS_QUICKSILVER);
        tag(ModTags.Items.RAW_MATERIALS_CINNABAR).add(ModItems.RAW_CINNABAR.get());
        tag(Tags.Items.RAW_MATERIALS).addTag(ModTags.Items.RAW_MATERIALS_CINNABAR);

        ModItems.SHARDS.values().forEach(shard -> tag(ModTags.Items.SHARDS).add(shard.get()));
        tag(ModTags.Items.CLUSTERS).add(ModItems.IRON_CLUSTER.get(), ModItems.GOLD_CLUSTER.get(),
                ModItems.COPPER_CLUSTER.get(), ModItems.CINNABAR_CLUSTER.get());
    }
}
