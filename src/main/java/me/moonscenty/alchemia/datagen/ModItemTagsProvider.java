package me.moonscenty.alchemia.datagen;

import java.util.concurrent.CompletableFuture;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModTags;
import me.moonscenty.alchemia.registry.WoodSet;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
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

        // nothing reveals nodes on its own yet, but the tag has to exist for the sight to read it
        tag(ModTags.Items.REVEALS);

        tag(ModTags.Items.GEMS_AMBER).add(ModItems.AMBER.get());
        tag(ModTags.Items.GEMS_QUICKSILVER).add(ModItems.QUICKSILVER.get());
        tag(Tags.Items.GEMS).addTag(ModTags.Items.GEMS_AMBER).addTag(ModTags.Items.GEMS_QUICKSILVER);
        tag(ModTags.Items.RAW_MATERIALS_CINNABAR).add(ModItems.RAW_CINNABAR.get());
        tag(Tags.Items.RAW_MATERIALS).addTag(ModTags.Items.RAW_MATERIALS_CINNABAR);

        copy(ModTags.Blocks.STORAGE_BLOCKS_ALCHEMIUM, ModTags.Items.STORAGE_BLOCKS_ALCHEMIUM);
        copy(ModTags.Blocks.STORAGE_BLOCKS_BRASS, ModTags.Items.STORAGE_BLOCKS_BRASS);
        copy(Tags.Blocks.STORAGE_BLOCKS, Tags.Items.STORAGE_BLOCKS);

        tag(ModTags.Items.INGOTS_ALCHEMIUM).add(ModItems.ALCHEMIUM_INGOT.get());
        tag(ModTags.Items.INGOTS_BRASS).add(ModItems.BRASS_INGOT.get());
        tag(Tags.Items.INGOTS).addTag(ModTags.Items.INGOTS_ALCHEMIUM).addTag(ModTags.Items.INGOTS_BRASS);
        tag(ModTags.Items.NUGGETS_ALCHEMIUM).add(ModItems.ALCHEMIUM_NUGGET.get());
        tag(ModTags.Items.NUGGETS_BRASS).add(ModItems.BRASS_NUGGET.get());
        tag(ModTags.Items.NUGGETS_QUICKSILVER).add(ModItems.QUICKSILVER_DROP.get());
        tag(Tags.Items.NUGGETS).addTag(ModTags.Items.NUGGETS_ALCHEMIUM).addTag(ModTags.Items.NUGGETS_BRASS).addTag(ModTags.Items.NUGGETS_QUICKSILVER);
        tag(ModTags.Items.GEARS_ALCHEMIUM).add(ModItems.ALCHEMIUM_GEAR.get());
        tag(ModTags.Items.GEARS_BRASS).add(ModItems.BRASS_GEAR.get());
        tag(ModTags.Items.GEARS).addTag(ModTags.Items.GEARS_ALCHEMIUM).addTag(ModTags.Items.GEARS_BRASS);
        tag(ModTags.Items.PLATES_ALCHEMIUM).add(ModItems.ALCHEMIUM_PLATE.get());
        tag(ModTags.Items.PLATES_BRASS).add(ModItems.BRASS_PLATE.get());
        tag(ModTags.Items.PLATES_IRON).add(ModItems.IRON_PLATE.get());
        tag(ModTags.Items.PLATES).addTag(ModTags.Items.PLATES_ALCHEMIUM).addTag(ModTags.Items.PLATES_BRASS).addTag(ModTags.Items.PLATES_IRON);
        tag(ItemTags.BEACON_PAYMENT_ITEMS).add(ModItems.ALCHEMIUM_INGOT.get(), ModItems.BRASS_INGOT.get());

        for (WoodSet wood : ModBlocks.WOODS) {
            copy(wood.logsBlockTag(), wood.logsItemTag());
        }
        copy(ModTags.Blocks.STORAGE_BLOCKS_AMBER, ModTags.Items.STORAGE_BLOCKS_AMBER);
        copy(BlockTags.STAIRS, ItemTags.STAIRS);
        copy(BlockTags.SLABS, ItemTags.SLABS);

        copy(BlockTags.LOGS_THAT_BURN, ItemTags.LOGS_THAT_BURN);
        copy(BlockTags.PLANKS, ItemTags.PLANKS);
        copy(BlockTags.WOODEN_STAIRS, ItemTags.WOODEN_STAIRS);
        copy(BlockTags.WOODEN_SLABS, ItemTags.WOODEN_SLABS);
        copy(BlockTags.LEAVES, ItemTags.LEAVES);
        copy(BlockTags.SAPLINGS, ItemTags.SAPLINGS);

        // the balanced shard counts as one too, as it did in the original, so anything asking for "a shard" takes it
        ModItems.SHARDS.values().forEach(shard -> tag(ModTags.Items.SHARDS).add(shard.get()));
        tag(ModTags.Items.SHARDS).add(ModItems.BALANCED_SHARD.get());
        tag(ModTags.Items.CLUSTERS).add(ModItems.IRON_CLUSTER.get(), ModItems.GOLD_CLUSTER.get(),
                ModItems.COPPER_CLUSTER.get(), ModItems.CINNABAR_CLUSTER.get());
    }
}
