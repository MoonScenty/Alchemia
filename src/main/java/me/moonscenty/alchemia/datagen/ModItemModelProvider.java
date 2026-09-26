package me.moonscenty.alchemia.datagen;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.CrystalBlock;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Alchemia.MODID, existingFileHelper);
    }

    /**
     * The rods and caps, in the order the wand model picks them by.
     * <p>
     * A wand is one item made of two pieces, so all forty-five pairings are written out and the model chosen by a
     * single number the item hands the renderer. Changing these lists changes that number, so they are kept in the
     * order the parts are registered in.
     */
    static final String[] RODS = {"wood", "greatwood", "silverwood", "reed", "obsidian", "blaze", "ice", "quartz", "bone"};
    static final String[] CAPS = {"iron", "gold", "brass", "alchemium", "void"};

    @Override
    protected void registerModels() {
        wands();
        // shown in hand and on the ground with its arms, which the placed block draws separately
        withExistingParent("arcane_workbench", modLoc("block/arcane_workbench"));
        withExistingParent("arcane_workbench_charger", modLoc("block/charger/item"));
        withExistingParent("node_stabilizer", modLoc("block/node_stabilizer/item"));
        withExistingParent("taint_fibre", mcLoc("item/generated")).texture("layer0", modLoc("block/taint_fibres"));
        basicItem(ModItems.AMBER.get());
        basicItem(ModItems.QUICKSILVER.get());
        basicItem(ModItems.RAW_CINNABAR.get());
        ModItems.SHARDS.values().forEach(shard -> basicItem(shard.get()));
        basicItem(ModItems.BALANCED_SHARD.get());
        basicItem(ModItems.IRON_CLUSTER.get());
        basicItem(ModItems.GOLD_CLUSTER.get());
        basicItem(ModItems.COPPER_CLUSTER.get());
        basicItem(ModItems.CINNABAR_CLUSTER.get());

        basicItem(ModItems.ALCHEMIUM_INGOT.get());
        basicItem(ModItems.BRASS_INGOT.get());
        basicItem(ModItems.ALCHEMIUM_NUGGET.get());
        basicItem(ModItems.BRASS_NUGGET.get());
        basicItem(ModItems.QUICKSILVER_DROP.get());
        basicItem(ModItems.ALCHEMIUM_GEAR.get());
        basicItem(ModItems.BRASS_GEAR.get());
        basicItem(ModItems.ALCHEMIUM_PLATE.get());
        basicItem(ModItems.BRASS_PLATE.get());
        basicItem(ModItems.IRON_PLATE.get());
        basicItem(ModItems.SALIS_MUNDUS.get());
        basicItem(ModItems.ALCHEMOMETER.get());
        basicItem(ModItems.NODE_PLACER.get());
        basicItem(ModItems.SCRIBING_TOOLS.get());
        basicItem(ModItems.RESEARCH_NOTES.get());
        basicItem(ModItems.ALCHEMONOMICON.get());

        ModBlocks.PLANTS.forEach(plant -> {
            String name = plant.getId().getPath();
            withExistingParent(name, mcLoc("item/generated")).texture("layer0", modLoc("block/" + name));
        });

        ModBlocks.WOODS.forEach(wood -> {
            String name = wood.sapling().getId().getPath();
            withExistingParent(name, mcLoc("item/generated")).texture("layer0", modLoc("block/" + name));
        });

        // Crystals show their fully grown texture in the inventory
        ModBlocks.CRYSTALS.values().forEach(crystal -> {
            String name = crystal.getId().getPath();
            withExistingParent(name, mcLoc("item/generated"))
                    .texture("layer0", modLoc("block/crystal/" + name + "_stage" + CrystalBlock.MAX_AGE));
        });
    }

    /** Every rod against every cap, plus the wand itself picking between them. */
    private void wands() {
        for (String rod : RODS) {
            for (String cap : CAPS) {
                withExistingParent("wand_" + rod + "_" + cap, mcLoc("item/handheld"))
                        .texture("layer0", modLoc("item/wand/rod_" + rod))
                        .texture("layer1", modLoc("item/wand/cap_" + cap));
            }
        }

        var wand = withExistingParent("wand", mcLoc("item/handheld"))
                .texture("layer0", modLoc("item/wand/rod_" + RODS[0]))
                .texture("layer1", modLoc("item/wand/cap_" + CAPS[0]));
        for (int rod = 0; rod < RODS.length; rod++) {
            for (int cap = 0; cap < CAPS.length; cap++) {
                wand.override()
                        .predicate(Alchemia.id("wand"), rod * CAPS.length + cap)
                        .model(getExistingFile(modLoc("item/wand_" + RODS[rod] + "_" + CAPS[cap])))
                        .end();
            }
        }

        for (String cap : CAPS) {
            withExistingParent("wand_cap_" + cap, mcLoc("item/generated"))
                    .texture("layer0", modLoc("item/wand/cap_" + cap + "_mat"));
        }
        // the rod sprite serves as its own item picture; only the wooden one has no item, being a plain stick
        for (int rod = 1; rod < RODS.length; rod++) {
            withExistingParent("wand_rod_" + RODS[rod], mcLoc("item/handheld"))
                    .texture("layer0", modLoc("item/wand/rod_" + RODS[rod]));
        }
    }
}
