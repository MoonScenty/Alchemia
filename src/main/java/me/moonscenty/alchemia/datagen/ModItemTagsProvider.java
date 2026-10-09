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
        ModBlocks.NITOR.values().forEach(flame -> tag(ModTags.Items.NITOR).add(flame.get().asItem()));
        ModItems.FOCI.values().forEach(focus -> tag(ModTags.Items.FOCI).add(focus.get()));
        robes();
        metalGear();
        tag(net.minecraft.tags.ItemTags.HEAD_ARMOR).add(ModItems.GOGGLES.get());
        tag(net.minecraft.tags.ItemTags.DURABILITY_ENCHANTABLE).add(ModItems.GOGGLES.get());
        tag(net.minecraft.tags.ItemTags.ARMOR_ENCHANTABLE).add(ModItems.GOGGLES.get());
        tag(net.minecraft.tags.ItemTags.EQUIPPABLE_ENCHANTABLE).add(ModItems.GOGGLES.get());
        tag(net.minecraft.tags.ItemTags.FOOT_ARMOR).add(ModItems.TRAVELLER_BOOTS.get());
        tag(net.minecraft.tags.ItemTags.DURABILITY_ENCHANTABLE).add(ModItems.TRAVELLER_BOOTS.get());
        tag(net.minecraft.tags.ItemTags.ARMOR_ENCHANTABLE).add(ModItems.TRAVELLER_BOOTS.get());
        tag(net.minecraft.tags.ItemTags.EQUIPPABLE_ENCHANTABLE).add(ModItems.TRAVELLER_BOOTS.get());

        copy(ModTags.Blocks.ORES_AMBER, ModTags.Items.ORES_AMBER);
        copy(ModTags.Blocks.ORES_CINNABAR, ModTags.Items.ORES_CINNABAR);
        copy(Tags.Blocks.ORES, Tags.Items.ORES);
        copy(Tags.Blocks.ORES_IN_GROUND_STONE, Tags.Items.ORES_IN_GROUND_STONE);
        copy(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE, Tags.Items.ORES_IN_GROUND_DEEPSLATE);
        copy(Tags.Blocks.ORE_RATES_SINGULAR, Tags.Items.ORE_RATES_SINGULAR);
        copy(Tags.Blocks.ORE_RATES_DENSE, Tags.Items.ORE_RATES_DENSE);

        // what an aura node can be seen through. The sight reads this and nothing else
        tag(ModTags.Items.REVEALS).add(ModItems.GOGGLES.get());

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
        tag(ModTags.Items.INGOTS_VOID).add(ModItems.VOID_INGOT.get());
        tag(Tags.Items.INGOTS).addTag(ModTags.Items.INGOTS_ALCHEMIUM).addTag(ModTags.Items.INGOTS_BRASS)
                .addTag(ModTags.Items.INGOTS_VOID);
        tag(ModTags.Items.NUGGETS_ALCHEMIUM).add(ModItems.ALCHEMIUM_NUGGET.get());
        tag(ModTags.Items.NUGGETS_BRASS).add(ModItems.BRASS_NUGGET.get());
        tag(ModTags.Items.NUGGETS_VOID).add(ModItems.VOID_NUGGET.get());
        tag(ModTags.Items.NUGGETS_QUICKSILVER).add(ModItems.QUICKSILVER_DROP.get());
        tag(Tags.Items.NUGGETS).addTag(ModTags.Items.NUGGETS_ALCHEMIUM).addTag(ModTags.Items.NUGGETS_BRASS).addTag(ModTags.Items.NUGGETS_QUICKSILVER)
                .addTag(ModTags.Items.NUGGETS_VOID);
        tag(ModTags.Items.GEARS_ALCHEMIUM).add(ModItems.ALCHEMIUM_GEAR.get());
        tag(ModTags.Items.GEARS_BRASS).add(ModItems.BRASS_GEAR.get());
        tag(ModTags.Items.GEARS).addTag(ModTags.Items.GEARS_ALCHEMIUM).addTag(ModTags.Items.GEARS_BRASS);
        tag(ModTags.Items.PLATES_ALCHEMIUM).add(ModItems.ALCHEMIUM_PLATE.get());
        tag(ModTags.Items.PLATES_BRASS).add(ModItems.BRASS_PLATE.get());
        tag(ModTags.Items.PLATES_IRON).add(ModItems.IRON_PLATE.get());
        tag(ModTags.Items.PLATES_VOID).add(ModItems.VOID_PLATE.get());
        tag(ModTags.Items.PLATES).addTag(ModTags.Items.PLATES_ALCHEMIUM).addTag(ModTags.Items.PLATES_BRASS)
                .addTag(ModTags.Items.PLATES_IRON).addTag(ModTags.Items.PLATES_VOID);
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

    /** Each robe piece where the game looks for armour of its shape, and where an enchanting table looks. */
    private void robes() {
        java.util.Map<net.minecraft.world.item.ArmorItem.Type, net.minecraft.tags.TagKey<net.minecraft.world.item.Item>> shapes =
                java.util.Map.of(
                        net.minecraft.world.item.ArmorItem.Type.HELMET, ItemTags.HEAD_ARMOR,
                        net.minecraft.world.item.ArmorItem.Type.CHESTPLATE, ItemTags.CHEST_ARMOR,
                        net.minecraft.world.item.ArmorItem.Type.LEGGINGS, ItemTags.LEG_ARMOR,
                        net.minecraft.world.item.ArmorItem.Type.BOOTS, ItemTags.FOOT_ARMOR);
        ModItems.FORTRESS.values().forEach(held -> {
            me.moonscenty.alchemia.item.FortressArmorItem piece = held.get();
            tag(shapes.get(piece.getType())).add(piece);
            tag(ItemTags.DURABILITY_ENCHANTABLE).add(piece);
            tag(ItemTags.ARMOR_ENCHANTABLE).add(piece);
            tag(ItemTags.EQUIPPABLE_ENCHANTABLE).add(piece);
        });
        ModItems.ROBES.values().forEach(held -> {
            me.moonscenty.alchemia.item.RobeItem robe = held.get();
            tag(shapes.get(robe.getType())).add(robe);
            tag(ItemTags.DURABILITY_ENCHANTABLE).add(robe);
            tag(ItemTags.ARMOR_ENCHANTABLE).add(robe);
            tag(ItemTags.EQUIPPABLE_ENCHANTABLE).add(robe);
            // both robes take dye and wash out in a cauldron, as the original's both did
            tag(ItemTags.DYEABLE).add(robe);
        });
    }

    /**
     * The tools and armour of our metals, put where the game and the mod both look for them.
     * <p>
     * A pickaxe that is not in the pickaxes tag is not a pickaxe to anything that asks: not to vanilla, and not
     * to the workings an altar puts on tools. Extending the class is not the same as saying so.
     */
    private void metalGear() {
        java.util.Map<String, net.minecraft.tags.TagKey<net.minecraft.world.item.Item>> shapes =
                java.util.Map.of(
                        "pickaxe", net.minecraft.tags.ItemTags.PICKAXES,
                        "axe", net.minecraft.tags.ItemTags.AXES,
                        "shovel", net.minecraft.tags.ItemTags.SHOVELS,
                        "sword", net.minecraft.tags.ItemTags.SWORDS,
                        "hoe", net.minecraft.tags.ItemTags.HOES,
                        "helmet", net.minecraft.tags.ItemTags.HEAD_ARMOR,
                        "chestplate", net.minecraft.tags.ItemTags.CHEST_ARMOR,
                        "leggings", net.minecraft.tags.ItemTags.LEG_ARMOR,
                        "boots", net.minecraft.tags.ItemTags.FOOT_ARMOR);

        java.util.stream.Stream.concat(ModItems.METAL_TOOLS.entrySet().stream(),
                        ModItems.METAL_ARMOUR.entrySet().stream())
                .forEach(made -> {
                    String shape = made.getKey().substring(made.getKey().indexOf('_') + 1);
                    tag(shapes.get(shape)).add(made.getValue().get());
                });

        // and what may be taken to an enchanting table, which is a separate list from what a thing is
        ModItems.METAL_TOOLS.forEach((name, tool) -> {
            tag(net.minecraft.tags.ItemTags.DURABILITY_ENCHANTABLE).add(tool.get());
            if (name.endsWith("_sword")) {
                tag(net.minecraft.tags.ItemTags.SWORD_ENCHANTABLE).add(tool.get());
                tag(net.minecraft.tags.ItemTags.WEAPON_ENCHANTABLE).add(tool.get());
            } else {
                tag(net.minecraft.tags.ItemTags.MINING_ENCHANTABLE).add(tool.get());
                tag(net.minecraft.tags.ItemTags.MINING_LOOT_ENCHANTABLE).add(tool.get());
            }
        });
        ModItems.METAL_ARMOUR.values().forEach(piece -> {
            tag(net.minecraft.tags.ItemTags.DURABILITY_ENCHANTABLE).add(piece.get());
            tag(net.minecraft.tags.ItemTags.ARMOR_ENCHANTABLE).add(piece.get());
            tag(net.minecraft.tags.ItemTags.EQUIPPABLE_ENCHANTABLE).add(piece.get());
        });
    }
}
