package me.moonscenty.alchemia.datagen;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.Direction;
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

    /** The rods and caps, for their loose items. */
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
        matrix();
        // a tube in the hand is the original's flat picture of it, one for each kind
        String[][] tubes = {{"tube", "tube_normal"}, {"tube_valve", "tube_valve"}, {"tube_oneway", "tube_oneway"},
                {"tube_restrict", "tube_restrict"}, {"tube_filter", "tube_filter"}, {"tube_buffer", "tube_buffer"}};
        for (String[] tube : tubes) {
            withExistingParent(tube[0], mcLoc("item/generated")).texture("layer0", modLoc("item/legacy/" + tube[1]));
        }
        withExistingParent("essentia_smelter", modLoc("block/essentia_smelter"));
        withExistingParent("arcane_workbench", modLoc("block/arcane_workbench"));
        // drawn in the hand on the original's meshes by LegacyMeshItemModel; these only say how they are held
        heldAsBlock("alembic", "item/legacy/alembic");
        heldAsBlock("arcane_workbench_charger", "item/legacy/vis_relay");
        heldAsBlock("node_stabilizer", "item/legacy/node_stabilizer");
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
        // the boots and the goggles are pictures in the slot too, as the original drew them
        basicItem(ModItems.TRAVELLER_BOOTS.get());
        basicItem(ModItems.GOGGLES.get());
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

        // a crystal in the hand is the original's grey cluster, coloured by its aspect
        ModBlocks.CRYSTALS.values().forEach(crystal -> withExistingParent(crystal.getId().getPath(),
                mcLoc("item/generated")).texture("layer0", modLoc("item/legacy/crystal_planter")));
    }

    private static void held(ItemModelBuilder model, ItemDisplayContext context, float rx, float ry, float rz,
            float tx, float ty, float tz, float scale) {
        model.transforms().transform(context).rotation(rx, ry, rz).translation(tx, ty, tz).scale(scale).end().end();
    }

    /** An item held and shown as a block is, with nothing in its model but a picture to break into. */
    private void heldAsBlock(String name, String particle) {
        withExistingParent(name, mcLoc("block/block")).texture("particle", modLoc(particle));
    }

    /**
     * The matrix in the hand: its eight stones as the original's cube, each wearing the original's sheet folded the
     * way a sixteen-pixel box folds it, gathered into one block as they hang in the altar before it wakes.
     */
    private void matrix() {
        var matrix = withExistingParent("infusion_matrix", mcLoc("block/block"))
                .texture("stone", modLoc("item/legacy/infuser"))
                .texture("particle", modLoc("item/legacy/infuser"));
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    var stone = matrix.element()
                            .from(0.4F + 8 * x, 0.4F + 8 * y, 0.4F + 8 * z)
                            .to(7.6F + 8 * x, 7.6F + 8 * y, 7.6F + 8 * z);
                    // the box's six faces on the top half of the sheet, a sixteen-pixel box at the sheet's corner
                    stone.face(Direction.UP).uvs(4, 0, 8, 4).texture("#stone").end();
                    stone.face(Direction.DOWN).uvs(8, 0, 12, 4).texture("#stone").end();
                    stone.face(Direction.WEST).uvs(0, 4, 4, 8).texture("#stone").end();
                    stone.face(Direction.NORTH).uvs(4, 4, 8, 8).texture("#stone").end();
                    stone.face(Direction.EAST).uvs(8, 4, 12, 8).texture("#stone").end();
                    stone.face(Direction.SOUTH).uvs(12, 4, 16, 8).texture("#stone").end();
                    stone.end();
                }
            }
        }
    }

    /**
     * The wand itself, which says only how a wand is held and shown. What it looks like is built from the
     * original's mesh by LegacyWandModel, out of whatever the wand in hand is made of.
     */
    private void wands() {
        var wand = getBuilder("wand").texture("particle", modLoc("item/wand/rod_greatwood"));
        held(wand, ItemDisplayContext.GUI, 0, 45, -45, 0, 0, 0, 0.72F);
        held(wand, ItemDisplayContext.GROUND, 0, 0, 0, 0, 2, 0, 0.5F);
        held(wand, ItemDisplayContext.FIXED, 0, 0, -45, 0, 0, 0, 0.8F);
        held(wand, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, 0, -90, 10, 0, 3.5F, 1.5F, 0.85F);
        held(wand, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, 0, 90, -10, 0, 3.5F, 1.5F, 0.85F);
        held(wand, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, 0, -90, -20, 1.13F, 3.2F, 1.13F, 0.68F);
        held(wand, ItemDisplayContext.FIRST_PERSON_LEFT_HAND, 0, 90, 20, 1.13F, 3.2F, 1.13F, 0.68F);
        held(wand, ItemDisplayContext.HEAD, 0, 0, 0, 0, 13, 7, 1.0F);

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
