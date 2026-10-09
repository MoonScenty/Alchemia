package me.moonscenty.alchemia.datagen;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.CrystalBlock;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;
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
        held("crucible", modLoc("block/crucible"));
        // a jar in the hand shows what is in it, the same as one on a shelf does
        ModelFile fullJar = held("jar_filled", modLoc("block/jar/jar_4"));
        held("jar", modLoc("block/jar/jar"))
                .override().predicate(Alchemia.id("filled"), 1).model(fullJar).end();
        held("arcane_pedestal", modLoc("block/pedestal"));
        // all eight stones, turned the way the renderer turns them. A model cannot turn a box a quarter, so the
        // turning was done on paper and written out as which glyph lands on which side (tools/gen_matrix_item.py)
        held("infusion_matrix", modLoc("block/infusion_cluster"));
        // a straight length of pipe rather than the bare middle, and each kind wearing whatever tells it apart
        withExistingParent("tube", modLoc("block/tube/item_plain"));
        withExistingParent("tube_valve", modLoc("block/tube/item_valve"));
        withExistingParent("tube_oneway", modLoc("block/tube/item_oneway"));
        withExistingParent("tube_restrict", modLoc("block/tube/item_restrict"));
        withExistingParent("tube_filter", modLoc("block/tube/item_filter"));
        withExistingParent("tube_buffer", modLoc("block/tube/item_buffer"));
        withExistingParent("essentia_smelter", modLoc("block/essentia_smelter"));
        withExistingParent("alembic", modLoc("block/alembic/item"));
        withExistingParent("arcane_workbench", modLoc("block/arcane_workbench"));
        withExistingParent("arcane_workbench_charger", modLoc("block/charger/item"));
        withExistingParent("node_stabilizer", modLoc("block/node_stabilizer/item"));
        withExistingParent("taint_fibre", mcLoc("item/generated")).texture("layer0", modLoc("block/taint_fibres"));
        distillery();
        basicItem(ModItems.AMBER.get());
        basicItem(ModItems.QUICKSILVER.get());
        basicItem(ModItems.RAW_CINNABAR.get());
        ModItems.SHARDS.values().forEach(shard -> basicItem(shard.get()));
        basicItem(ModItems.BALANCED_SHARD.get());
        basicItem(ModItems.CRYSTALLIZED_ESSENCE.get());
        // a tool is held out in front; a piece of armour lies flat in the slot like any other picture
        ModItems.METAL_TOOLS.forEach((name, tool) -> withExistingParent(name, mcLoc("item/handheld"))
                .texture("layer0", modLoc("item/" + name)));
        ModItems.METAL_ARMOUR.forEach((name, piece) -> basicItem(piece.get()));
        // fortress armour is a picture in the slot, as the original drew it; the original's comes in with its jar
        ModItems.FORTRESS.values().forEach(piece -> basicItem(piece.get()));
        // a robe is its cloth, which takes the dye, and a trim over it; the hood has no trim
        ModItems.ROBES.forEach((name, robe) -> {
            if (name.equals("void_robe_helm")) {
                basicItem(robe.get());
            } else {
                withExistingParent(name, mcLoc("item/generated"))
                        .texture("layer0", modLoc("item/" + name))
                        .texture("layer1", modLoc("item/" + name + "_overlay"));
            }
        });
        // the traveller's boots have a model of their own, in models/item/, and datagen does not write over it
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
        basicItem(ModItems.VOID_PLATE.get());
        basicItem(ModItems.SALIS_MUNDUS.get());
        basicItem(ModItems.ENCHANTED_FABRIC.get());
        basicItem(ModItems.ALUMENTUM.get());
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

    /**
     * The wand itself, picking between the forty-five that are already written down.
     * <p>
     * A built wand is a model, not two pictures stacked: a rod standing between two ferrules, wearing the surface
     * of whatever it was made of. The forty-five are not built here because they all come from one drawing with
     * two textures swapped, which {@code tools/gen_wand3d.py} does once rather than this doing it every run.
     */
    private void wands() {
        var wand = withExistingParent("wand", modLoc("item/wand_" + RODS[0] + "_" + CAPS[0]));
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
            // the same ferrule with the life taken out of it, for the two that are cast before they are finished
            if (ModItems.INERT_CAPS.containsKey(cap)) {
                withExistingParent("wand_cap_" + cap + "_inert", mcLoc("item/generated"))
                        .texture("layer0", modLoc("item/wand/cap_" + cap + "_inert_mat"));
            }
        }
        basicItem(ModItems.FOCUS_POUCH.get());

        // one picture apiece, all drawn as one set by tools/gen_foci.py
        ModItems.FOCI.keySet().forEach(name -> withExistingParent("focus_" + name, mcLoc("item/generated"))
                .texture("layer0", modLoc("item/wand/focus_" + name)));

        // a loose rod has a picture of its own, drawn as one rather than as the strip laid under a wand's caps;
        // only the wooden one has no item, being a plain stick
        for (int rod = 1; rod < RODS.length; rod++) {
            withExistingParent("wand_rod_" + RODS[rod], mcLoc("item/handheld"))
                    .texture("layer0", modLoc("item/wand/rod_" + RODS[rod] + "_mat"));
        }
    }

    /**
     * The phial and the label, each of which is two pictures in one item.
     * <p>
     * An empty phial is glass and nothing else; a full one has the essentia drawn behind the glass, tinted by
     * whatever it is holding. A label is the same trick with paper and ink. Which of the two is shown comes from a
     * single number the item hands the renderer, since that is all an override can be asked to test.
     */
    private void distillery() {
        basicItem(ModItems.FILTER.get());
        basicItem(ModItems.JAR_BRACE.get());
        basicItem(ModItems.VOID_SEED.get());
        basicItem(ModItems.VOID_INGOT.get());
        basicItem(ModItems.VOID_NUGGET.get());

        withExistingParent("phial_filled", mcLoc("item/generated"))
                .texture("layer0", modLoc("item/phial_contents"))
                .texture("layer1", modLoc("item/phial"));
        withExistingParent("jar_label_written", mcLoc("item/generated"))
                .texture("layer0", modLoc("item/jar_label"))
                .texture("layer1", modLoc("item/jar_label_overlay"));

        filled("phial", "phial_filled");
        filled("jar_label", "jar_label_written");
    }

    /** An item that is drawn one way empty and another way full, with the full picture kept in its own file. */
    private void filled(String name, String full) {
        withExistingParent(name, mcLoc("item/generated"))
                .texture("layer0", modLoc("item/" + name))
                .override()
                        .predicate(Alchemia.id("filled"), 1)
                        .model(getExistingFile(modLoc("item/" + full)))
                        .end();
    }

    /**
     * A block shown as itself in a slot, in a hand and on the ground.
     * <p>
     * The poses would normally come from inheriting {@code minecraft:block/block}, but a model can only have one
     * parent and these need theirs for the shape. Vanilla's cauldron, which ours is built on, carries no poses of
     * its own — it never needed any, since Mojang draw the cauldron item flat — so they are written out here.
     */
    private ItemModelBuilder held(String name, ResourceLocation model) {
        return withExistingParent(name, model)
                .transforms()
                .transform(ItemDisplayContext.GUI)
                        .rotation(30, 225, 0).scale(0.625F).end()
                .transform(ItemDisplayContext.GROUND)
                        .translation(0, 3, 0).scale(0.25F).end()
                .transform(ItemDisplayContext.FIXED)
                        .scale(0.5F).end()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
                        .rotation(75, 45, 0).translation(0, 2.5F, 0).scale(0.375F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                        .rotation(0, 45, 0).scale(0.4F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
                        .rotation(0, 225, 0).scale(0.4F).end()
                .end();
    }
}
