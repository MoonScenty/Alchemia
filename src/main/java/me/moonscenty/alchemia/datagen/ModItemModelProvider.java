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
        ModelFile fullVoidJar = held("void_jar_filled", modLoc("block/void_jar/jar_4"));
        held("void_jar", modLoc("block/void_jar/jar"))
                .override().predicate(Alchemia.id("filled"), 1).model(fullVoidJar).end();
        held("arcane_pedestal", modLoc("block/pedestal"));
        withExistingParent("ethereal_bloom", mcLoc("item/generated")).texture("layer0", modLoc("block/ethereal_bloom"));
        // a mirror is its frame and its pane, the pane open once it remembers a partner
        for (String[] mirror : new String[][] {{"mirror", "mirrorframe"}, {"mirror_essentia", "mirrorframe2"}}) {
            ModelFile open = withExistingParent(mirror[0] + "_on", mcLoc("item/generated"))
                    .texture("layer0", modLoc("block/legacy/" + mirror[1]))
                    .texture("layer1", modLoc("block/legacy/mirrorpaneopen"));
            withExistingParent(mirror[0], mcLoc("item/generated"))
                    .texture("layer0", modLoc("block/legacy/" + mirror[1]))
                    .texture("layer1", modLoc("block/legacy/mirrorpane"))
                    .override().predicate(Alchemia.id("linked"), 1).model(open).end();
        }
        withExistingParent("hand_mirror", mcLoc("item/handheld")).texture("layer0", modLoc("item/legacy/hand_mirror"));
        matrix();
        // a tube in the hand is the original's flat picture of it, one for each kind
        String[][] tubes = {{"tube", "tube_normal"}, {"tube_valve", "tube_valve"}, {"tube_oneway", "tube_oneway"},
                {"tube_restrict", "tube_restrict"}, {"tube_filter", "tube_filter"}, {"tube_buffer", "tube_buffer"}};
        for (String[] tube : tubes) {
            withExistingParent(tube[0], mcLoc("item/generated")).texture("layer0", modLoc("item/legacy/" + tube[1]));
        }
        withExistingParent("essentia_smelter", modLoc("block/essentia_smelter"));
        withExistingParent("alchemium_smelter", modLoc("block/alchemium_smelter"));
        withExistingParent("void_smelter", modLoc("block/void_smelter"));
        withExistingParent("smelter_aux", modLoc("block/smelter_aux"));
        withExistingParent("mnemonic_matrix", modLoc("block/mnemonic_matrix"));
        // in the hand, the original's still picture of a jar with a brain in it
        withExistingParent("brain_jar", modLoc("block/brain_jar_item"));
        withExistingParent("smelter_vent", modLoc("block/smelter_vent"));
        withExistingParent("arcane_workbench", modLoc("block/arcane_workbench"));
        withExistingParent("focal_manipulator", modLoc("block/focal_manipulator"));
        // drawn in the hand on the original's meshes by LegacyMeshItemModel; these only say how they are held
        heldAsBlock("alembic", "item/legacy/alembic");
        heldAsBlock("arcane_workbench_charger", "item/legacy/vis_relay");
        heldAsBlock("node_stabilizer", "item/legacy/node_stabilizer");
        withExistingParent("taint_fibre", mcLoc("item/generated")).texture("layer0", modLoc("block/legacy/taint_fibres"));
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
        basicItem(ModItems.ZOMBIE_BRAIN.get());
        ModItems.CRIMSON.values().forEach(piece -> basicItem(piece.get()));
        handheldItem(ModItems.CRIMSON_BLADE.get());
        basicItem(ModItems.CRIMSON_RITES.get());
        basicItem(ModItems.ELDRITCH_EYE.get());
        basicItem(ModItems.PRIMORDIAL_PEARL.get());
        basicItem(ModItems.LOOT_BAG_COMMON.get());
        basicItem(ModItems.LOOT_BAG_UNCOMMON.get());
        basicItem(ModItems.LOOT_BAG_RARE.get());
        basicItem(ModItems.RUNED_TABLET.get());
        basicItem(ModItems.SANITY_SOAP.get());
        ModItems.PRIMAL_ARROWS.values().forEach(arrow -> basicItem(arrow.get()));
        basicItem(ModItems.BOTTLE_TAINT.get());
        basicItem(ModItems.THAUMOSTATIC_HARNESS.get());
        // the bow of bone, held as a bow is and drawn through the original's three pictures: begun, past seven
        // ticks, and from thirteen on
        for (int stage = 0; stage < 3; stage++) {
            withExistingParent("bone_bow_pulling_" + stage, mcLoc("item/bow"))
                    .texture("layer0", modLoc("item/bone_bow_" + stage));
        }
        var bow = withExistingParent("bone_bow", mcLoc("item/bow")).texture("layer0", modLoc("item/bone_bow"));
        float[] from = {0.0F, 0.8F, 1.3F};
        for (int stage = 0; stage < 3; stage++) {
            bow.override().predicate(modLoc("pulling"), 1.0F).predicate(modLoc("pull"), from[stage])
                    .model(new ModelFile.UncheckedModelFile(modLoc("item/bone_bow_pulling_" + stage))).end();
        }
        // the lodestone lights while it is looked through towards a portal
        getBuilder("sinister_stone_on").parent(new ModelFile.UncheckedModelFile(mcLoc("item/generated")))
                .texture("layer0", modLoc("item/sinister_stone_on"));
        basicItem(ModItems.SINISTER_STONE.get()).override().predicate(modLoc("active"), 1.0F)
                .model(new ModelFile.UncheckedModelFile(modLoc("item/sinister_stone_on"))).end();
        ModBlocks.TALLOW_CANDLES.values().forEach(candle ->
                withExistingParent(candle.getId().getPath(), modLoc("block/candle")));
        withExistingParent("pattern_crafter", modLoc("block/pattern_crafter_inv"));
        for (var bauble : java.util.List.of(ModItems.MUNDANE_AMULET, ModItems.MUNDANE_RING, ModItems.MUNDANE_BELT,
                ModItems.FANCY_AMULET, ModItems.FANCY_RING, ModItems.FANCY_BELT, ModItems.RUNIC_AMULET,
                ModItems.EMERGENCY_RUNIC_AMULET, ModItems.PROTECTION_RING, ModItems.RUNIC_RING,
                ModItems.CHARGED_RUNIC_RING, ModItems.REVITALIZING_RUNIC_RING, ModItems.RUNIC_GIRDLE,
                ModItems.KINETIC_RUNIC_GIRDLE)) {
            basicItem(bauble.get());
        }
        basicItem(ModItems.VIS_STONE.get());
        basicItem(ModItems.VIS_AMULET.get());
        basicItem(ModItems.THAUMOSTATIC_GIRDLE.get());
        basicItem(ModItems.VERDANT_HEART.get());
        basicItem(ModItems.TRIPLE_MEAT_TREAT.get());
        ModItems.MEAT_CHUNKS.values().forEach(chunk -> basicItem(chunk.get()));
        // the six apprentice's rings are the one band, coloured as their primal
        ModItems.APPRENTICE_RINGS.values().forEach(ring -> withExistingParent(ring.getId().getPath(),
                mcLoc("item/generated")).texture("layer0", modLoc("item/apprentice_ring")));
        withExistingParent("liquid_death_bucket", mcLoc("item/generated"))
                .texture("layer0", modLoc("item/legacy/liquid_death_bucket"));
        withExistingParent("banner", mcLoc("item/generated")).texture("layer0", modLoc("item/banner_stand"))
                .texture("layer1", modLoc("item/banner")).texture("layer2", modLoc("item/banner_symbol"));
        withExistingParent("crimson_banner", mcLoc("item/generated")).texture("layer0", modLoc("item/banner_stand"))
                .texture("layer1", modLoc("item/banner_cultist"));
        for (var egg : java.util.List.of(ModItems.CULTIST_KNIGHT_SPAWN_EGG, ModItems.CULTIST_CLERIC_SPAWN_EGG,
                ModItems.CULTIST_PORTAL_SPAWN_EGG, ModItems.ELDRITCH_CRAB_SPAWN_EGG, ModItems.INHABITED_ZOMBIE_SPAWN_EGG,
                ModItems.ELDRITCH_GUARDIAN_SPAWN_EGG, ModItems.MIND_SPIDER_SPAWN_EGG)) {
            withExistingParent(egg.getId().getPath(), mcLoc("item/template_spawn_egg"));
        }
        for (String hoard : new String[] {"coin", "knowledge_fragment", "tainted_goo", "taint_tendril"}) {
            withExistingParent(hoard, mcLoc("item/generated")).texture("layer0", modLoc("item/legacy/" + hoard));
        }
        withExistingParent("wispy_essence", mcLoc("item/generated"))
                .texture("layer0", modLoc("item/legacy/wispy_essence"));
        for (var egg : java.util.List.of(ModItems.ANGRY_ZOMBIE_SPAWN_EGG, ModItems.FURIOUS_ZOMBIE_SPAWN_EGG,
                ModItems.FIREBAT_SPAWN_EGG, ModItems.WISP_SPAWN_EGG, ModItems.PECH_SPAWN_EGG,
                ModItems.TAINT_CRAWLER_SPAWN_EGG, ModItems.TAINT_SWARM_SPAWN_EGG, ModItems.TAINTACLE_SPAWN_EGG,
                ModItems.TAINTED_COW_SPAWN_EGG, ModItems.TAINTED_PIG_SPAWN_EGG, ModItems.TAINTED_CHICKEN_SPAWN_EGG,
                ModItems.TAINTED_SHEEP_SPAWN_EGG, ModItems.TAINTED_CREEPER_SPAWN_EGG, ModItems.TAINTED_VILLAGER_SPAWN_EGG,
                ModItems.TAINTED_RABBIT_SPAWN_EGG, ModItems.ALCHEMIC_SLIME_SPAWN_EGG)) {
            withExistingParent(egg.getId().getPath(), mcLoc("item/template_spawn_egg"));
        }
        basicItem(ModItems.ALCHEMIUM_GEAR.get());
        basicItem(ModItems.BRASS_GEAR.get());
        basicItem(ModItems.ALCHEMIUM_PLATE.get());
        basicItem(ModItems.BRASS_PLATE.get());
        basicItem(ModItems.IRON_PLATE.get());
        basicItem(ModItems.VOID_PLATE.get());
        basicItem(ModItems.VOID_GEAR.get());
        // golems and what goes into them, on the original's pictures; a golem item is tinted by its material
        for (String golemancy : new String[] {"golem", "golem_bell", "vision_module", "aggression_module", "tallow",
                "seal_blank", "seal_pickup", "seal_pickup_advanced", "seal_fill", "seal_fill_advanced", "seal_empty",
                "seal_empty_advanced", "seal_provider", "seal_guard", "seal_guard_advanced", "seal_butcher",
                "seal_harvest", "seal_lumber", "seal_breaker", "seal_use"}) {
            withExistingParent(golemancy, mcLoc("item/generated"))
                    .texture("layer0", modLoc("item/legacy/" + golemancy));
        }
        withExistingParent("stone_table", modLoc("block/table_stone"));
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
        // held the way any tool is held, on the original's pictures
        java.util.stream.Stream.concat(ModItems.ELEMENTAL_TOOLS.keySet().stream(), java.util.stream.Stream.of(
                "primal_crusher")).forEach(tool -> withExistingParent(tool, mcLoc("item/handheld"))
                .texture("layer0", modLoc("item/legacy/" + tool)));
        basicItem(ModItems.FOCUS_POUCH.get());
        for (String part : new String[] {"primal_charm", "mirrored_glass", "morphic_resonator", "clockwork_mind",
                "bioalchemic_mind"}) {
            withExistingParent(part, mcLoc("item/generated")).texture("layer0", modLoc("item/legacy/" + part));
        }
        // a lamp in the hand is the lit lamp, as the original showed it; the jug is its block
        withExistingParent("arcane_lamp", modLoc("block/arcane_lamp_on"));
        withExistingParent("growth_lamp", modLoc("block/growth_lamp_on"));
        withExistingParent("fertility_lamp", modLoc("block/fertility_lamp_on"));
        getBuilder("water_jug").parent(new net.neoforged.neoforge.client.model.generators.ModelFile
                .UncheckedModelFile(modLoc("block/legacy/water_jug")));
        // the totems and poles in the hand are their blocks
        for (String totem : new String[] {"aura_totem_push", "aura_totem_pull", "aura_totem_pole_inner",
                "aura_totem_pole_outer", "aura_totem_pole_pure"}) {
            withExistingParent(totem, modLoc("block/" + totem));
        }
        // the recharge pedestal and the dioptra in the hand are their blocks, the dioptra lit
        getBuilder("recharge_pedestal").parent(new net.neoforged.neoforge.client.model.generators.ModelFile
                .UncheckedModelFile(modLoc("block/legacy/recharge_pedestal")));
        getBuilder("dioptra").parent(new net.neoforged.neoforge.client.model.generators.ModelFile
                .UncheckedModelFile(modLoc("block/dioptra_on")));
        // the salts and the bucket of what they make are the original's pictures; the spa is its block
        withExistingParent("bath_salts", mcLoc("item/generated")).texture("layer0", modLoc("item/legacy/bath_salts"));
        withExistingParent("purifying_fluid_bucket", mcLoc("item/generated"))
                .texture("layer0", modLoc("item/legacy/purifying_fluid_bucket"));
        withExistingParent("spa", modLoc("block/spa"));
        // the levitator and the ear in the hand are lit, as the original showed them; the relay is dark
        getBuilder("levitator").parent(new net.neoforged.neoforge.client.model.generators.ModelFile
                .UncheckedModelFile(modLoc("block/levitator_on")));
        getBuilder("arcane_ear").parent(new net.neoforged.neoforge.client.model.generators.ModelFile
                .UncheckedModelFile(modLoc("block/arcane_ear_on")));
        withExistingParent("redstone_relay", modLoc("block/redstone_relay_off"));
        // the chest is drawn by its own renderer, held the way the game holds its own chest
        getBuilder("hungry_chest").parent(new net.neoforged.neoforge.client.model.generators.ModelFile
                .UncheckedModelFile("minecraft:item/chest"));
        // drawn by the block's own renderer, breathing; only how it is held is said here
        getBuilder("bellows").parent(new net.neoforged.neoforge.client.model.generators.ModelFile.UncheckedModelFile(
                        "minecraft:builtin/entity"))
                .transforms()
                .transform(net.minecraft.world.item.ItemDisplayContext.GUI).rotation(30, 225, 0).scale(0.625F).end()
                .transform(net.minecraft.world.item.ItemDisplayContext.GROUND).translation(0, 3, 0).scale(0.25F).end()
                .transform(net.minecraft.world.item.ItemDisplayContext.FIXED).scale(0.5F).end()
                .transform(net.minecraft.world.item.ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
                .rotation(75, 45, 0).translation(0, 2.5F, 0).scale(0.375F).end()
                .transform(net.minecraft.world.item.ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                .rotation(0, 45, 0).scale(0.4F).end()
                .end();
        // drawn by the blocks' own renderers; only how they are held is said here, as the bellows
        for (String device : java.util.List.of("centrifuge", "crystallizer", "arcane_bore", "arcane_bore_base")) {
            getBuilder(device).parent(new net.neoforged.neoforge.client.model.generators.ModelFile
                            .UncheckedModelFile("minecraft:builtin/entity"))
                    .transforms()
                    .transform(net.minecraft.world.item.ItemDisplayContext.GUI).rotation(30, 225, 0).scale(0.625F)
                    .end()
                    .transform(net.minecraft.world.item.ItemDisplayContext.GROUND).translation(0, 3, 0).scale(0.25F)
                    .end()
                    .transform(net.minecraft.world.item.ItemDisplayContext.FIXED).scale(0.5F).end()
                    .transform(net.minecraft.world.item.ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
                    .rotation(75, 45, 0).translation(0, 2.5F, 0).scale(0.375F).end()
                    .transform(net.minecraft.world.item.ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                    .rotation(0, 45, 0).scale(0.4F).end()
                    .end();
        }
        withExistingParent("autocaster", mcLoc("item/generated"))
                .texture("layer0", modLoc("item/legacy/autocaster"));

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
