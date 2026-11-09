package me.moonscenty.alchemia.client.legacy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aura.node.NodeType;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.wand.FocusUpgrade;

public final class LegacyAssets {
    // ColorizerFoliage.getFoliageColorBasic()
    private static final int FOLIAGE = 0x48B518;
    private static final int EFFECT_CELL = 18;
    private static final int EFFECT_TOP = 198;
    private static final int NODE_CELL = 64;

    public static final String FORTRESS_ARMOUR = "fortress_armour";

    public static final String ROBE = "robe";
    public static final String ROBE_SKIRT = "robe_skirt";
    public static final String KNIGHT_ARMOUR = "knight_armour";
    public static final String KNIGHT_ARMOUR_LOWER = "knight_armour_lower";
    public static final String ELDRITCH_CRAB = "eldritch_crab";
    public static final String BANNER = "banner";
    public static final String CUBE = "cube";
    public static final String ELDRITCH_GOLEM = "eldritch_golem";
    public static final String LEADER_ARMOUR = "leader_armour";
    public static final String LEADER_ARMOUR_LOWER = "leader_armour_lower";
    public static final String TAINTACLE_GIANT = "taintacle_giant";
    public static final String OBELISK_CAP_MESH = "models/legacy/obelisk_cap.obj";
    public static final String CRAB_VENT_MESH = "models/legacy/crabvent.obj";
    public static final String HEMIS_MESH = "models/legacy/hemis.obj";
    public static final String HARNESS_MESH = "models/legacy/hoverharness.obj";
    public static final String ELDRITCH_GUARDIAN = "eldritch_guardian";

    public static final String INFUSER = "infuser";
    public static final String INFUSER_GLOW = "infuser_glow";

    public static final String BELLOWS = "bellows";

    public static final String RESEARCH_TABLE = "research_table";

    public static final String PILLAR_MESH = "models/legacy/pillar.obj";

    public static final String CRYSTAL_MESH = "models/legacy/crystal.obj";

    public static final String STABILIZER_MESH = "models/legacy/node_stabilizer.obj";

    public static final String RELAY_MESH = "models/legacy/vis_relay.obj";
    public static final String CRYSTALLIZER_MESH = "models/legacy/crystallizer.obj";
    public static final String THAUMATORIUM_MESH = "models/legacy/thaumatorium.obj";
    public static final String CENTRIFUGE = "centrifuge";
    public static final String BRAIN = "brain";
    public static final String BORE = "bore";
    public static final String BORE_EMITTER = "bore_emitter";
    public static final String BORE_BASE = "bore_base";
    public static final String JAR = "jar";

    public static final String WAND_MESH = "models/legacy/wand.obj";
    public static final String STAFF_MESH = "models/legacy/staff.obj";

    public static final String TURRET_MESH = "models/legacy/turret.obj";
    public static final String CROSSBOW_ADVANCED_MESH = "models/legacy/crossbow_advanced.obj";
    public static final String HEMISPHERE_MESH = "models/legacy/hemis.obj";

    public static final String ALEMBIC_MESH = "models/legacy/alembic.obj";

    public static final String TUBE_MESH = "models/legacy/tube.obj";

    public static final String VALVE = "valve";
    public static final String FIREBAT = "firebat";
    public static final String CROSSBOW = "crossbow";
    public static final String NODE_MAGNET = "node_magnet";
    public static final String PECH = "pech";
    public static final String TAINTACLE = "taintacle";
    public static final String TAINTACLE_TINY = "taintacle_tiny";

    public static final String SCANNER_MESH = "models/legacy/scanner.obj";

    private static final Map<FocusUpgrade, String> ORIGINAL_UPGRADE = originalUpgrades();

    public static final List<LegacyAsset> ALL = build();

    public static final Map<String, List<LegacyModelSource>> MODELS = Map.ofEntries(
            Map.entry(FORTRESS_ARMOUR, List.of(
                    new LegacyModelSource(LegacyEdition.FIVE,
                            "thaumcraft/client/renderers/models/gear/ModelFortressArmor", "(F)V", 1.0F),
                    new LegacyModelSource(LegacyEdition.FOUR,
                            "thaumcraft/client/renderers/models/gear/ModelFortressArmor", "(F)V", 1.0F))),
            Map.entry(ROBE, List.of(
                    new LegacyModelSource(LegacyEdition.FIVE,
                            "thaumcraft/client/renderers/models/gear/ModelRobe", "(F)V", 1.0F),
                    new LegacyModelSource(LegacyEdition.FOUR,
                            "thaumcraft/client/renderers/models/gear/ModelRobe", "(F)V", 1.0F))),
            Map.entry(ROBE_SKIRT, List.of(
                    new LegacyModelSource(LegacyEdition.FIVE,
                            "thaumcraft/client/renderers/models/gear/ModelRobe", "(F)V", 0.5F),
                    new LegacyModelSource(LegacyEdition.FOUR,
                            "thaumcraft/client/renderers/models/gear/ModelRobe", "(F)V", 0.5F))),
            Map.entry(VALVE, List.of(new LegacyModelSource(LegacyEdition.FIVE,
                    "thaumcraft/client/renderers/models/block/ModelTubeValve", "()V"))),
            Map.entry(FIREBAT, List.of(new LegacyModelSource(LegacyEdition.FIVE,
                    "thaumcraft/client/renderers/models/entity/ModelFireBat", "()V"))),
            Map.entry(CROSSBOW, List.of(new LegacyModelSource(LegacyEdition.FIVE,
                    "thaumcraft/client/renderers/models/entity/ModelCrossbow", "()V"))),
            Map.entry(NODE_MAGNET, List.of(new LegacyModelSource(LegacyEdition.FIVE,
                    "thaumcraft/client/renderers/models/entity/ModelNodeMagnet", "()V"))),
            Map.entry(PECH, List.of(new LegacyModelSource(LegacyEdition.FIVE,
                    "thaumcraft/client/renderers/models/entity/ModelPech", "()V"))),
            Map.entry(TAINTACLE, List.of(new LegacyModelSource(LegacyEdition.FIVE,
                    "thaumcraft/client/renderers/models/entity/ModelTaintacle", "(I)V", 10))),
            Map.entry(ELDRITCH_GUARDIAN, List.of(new LegacyModelSource(LegacyEdition.FIVE,
                    "thaumcraft/client/renderers/models/entity/ModelEldritchGuardian", "()V"))),
            Map.entry(ELDRITCH_GOLEM, List.of(new LegacyModelSource(LegacyEdition.FIVE,
                    "thaumcraft/client/renderers/models/entity/ModelEldritchGolem", "()V"))),
            Map.entry(LEADER_ARMOUR, List.of(new LegacyModelSource(LegacyEdition.FIVE,
                    "thaumcraft/client/renderers/models/gear/ModelLeaderArmor", "(F)V", 1.0F))),
            Map.entry(LEADER_ARMOUR_LOWER, List.of(new LegacyModelSource(LegacyEdition.FIVE,
                    "thaumcraft/client/renderers/models/gear/ModelLeaderArmor", "(F)V", 0.5F))),
            Map.entry(TAINTACLE_GIANT, List.of(new LegacyModelSource(LegacyEdition.FIVE,
                    "thaumcraft/client/renderers/models/entity/ModelTaintacle", "(I)V", 14))),
            Map.entry(CUBE, List.of(new LegacyModelSource(LegacyEdition.FIVE,
                    "thaumcraft/client/renderers/models/ModelCube", "(I)V", 0))),
            Map.entry(BANNER, List.of(new LegacyModelSource(LegacyEdition.FIVE,
                    "thaumcraft/client/renderers/models/block/ModelBanner", "()V"))),
            Map.entry(ELDRITCH_CRAB, List.of(new LegacyModelSource(LegacyEdition.FIVE,
                    "thaumcraft/client/renderers/models/entity/ModelEldritchCrab", "()V"))),
            Map.entry(KNIGHT_ARMOUR, List.of(new LegacyModelSource(LegacyEdition.FIVE,
                    "thaumcraft/client/renderers/models/gear/ModelKnightArmor", "(F)V", 1.0F))),
            Map.entry(KNIGHT_ARMOUR_LOWER, List.of(new LegacyModelSource(LegacyEdition.FIVE,
                    "thaumcraft/client/renderers/models/gear/ModelKnightArmor", "(F)V", 0.5F))),
            Map.entry(TAINTACLE_TINY, List.of(new LegacyModelSource(LegacyEdition.FIVE,
                    "thaumcraft/client/renderers/models/entity/ModelTaintacle", "(I)V", 6))),
            Map.entry(CENTRIFUGE, List.of(new LegacyModelSource(LegacyEdition.FIVE,
                    "thaumcraft/client/renderers/models/block/ModelCentrifuge", "()V"))),
            Map.entry(BORE, List.of(new LegacyModelSource(LegacyEdition.FIVE,
                    "thaumcraft/client/renderers/models/block/ModelBore", "()V"))),
            Map.entry(BORE_EMITTER, List.of(new LegacyModelSource(LegacyEdition.FIVE,
                    "thaumcraft/client/renderers/models/block/ModelBoreEmit", "()V"))),
            Map.entry(BORE_BASE, List.of(new LegacyModelSource(LegacyEdition.FIVE,
                    "thaumcraft/client/renderers/models/block/ModelBoreBase", "()V"))),
            Map.entry(BRAIN, List.of(new LegacyModelSource(LegacyEdition.FIVE,
                    "thaumcraft/client/renderers/models/block/ModelBrain", "()V"))),
            Map.entry(JAR, List.of(new LegacyModelSource(LegacyEdition.FIVE,
                    "thaumcraft/client/renderers/models/block/ModelJar", "()V"))),
            Map.entry(BELLOWS, List.of(new LegacyModelSource(LegacyEdition.FIVE,
                    "thaumcraft/client/renderers/models/block/ModelBellows", "()V"))),
            Map.entry(RESEARCH_TABLE, List.of(new LegacyModelSource(LegacyEdition.FIVE,
                    "thaumcraft/client/renderers/models/block/ModelResearchTable", "()V"))),
            Map.entry(INFUSER, List.of(new LegacyModelSource(LegacyEdition.FIVE,
                    "thaumcraft/client/renderers/models/ModelCube", "(I)V", 0))),
            Map.entry(INFUSER_GLOW, List.of(new LegacyModelSource(LegacyEdition.FIVE,
                    "thaumcraft/client/renderers/models/ModelCube", "(I)V", 32))));

    private LegacyAssets() {
    }

    private static List<LegacyAsset> build() {
        List<LegacyAsset> all = new ArrayList<>();
        baseResources(all);
        aspects(all);
        effects(all);
        research(all);
        aura(all);
        wands(all);
        essentia(all);
        infusion(all);
        gear(all);
        golems(all);
        cult(all);
        leftovers(all);
        return List.copyOf(all);
    }

    private static void aspects(List<LegacyAsset> all) {
        ModAspects.ASPECTS.getEntries().forEach(aspect -> {
            String tag = aspect.getId().getPath();
            all.add(LegacyAsset.of("textures/aspect/" + tag + ".png")
                    .five("textures/aspects/" + tag + ".png")
                    .four("textures/aspects/" + tag + ".png"));
        });
        all.add(LegacyAsset.of("textures/aspect/background.png")
                .five("textures/aspects/_back.png").four("textures/aspects/_back.png"));
        all.add(LegacyAsset.of("textures/aspect/unknown.png")
                .five("textures/aspects/_unknown.png").four("textures/aspects/_unknown.png"));
    }

    private static void effects(List<LegacyAsset> all) {
        effect(all, "taint_poison", 3, 1);
        effect(all, "flux_flu", 5, 1);
        effect(all, "flux_phage", 6, 1);
        effect(all, "unnatural_hunger", 7, 1);
        effect(all, "sun_scorned", 6, 2);
        effect(all, "blurred_vision", 5, 2);
        effect(all, "deadly_gaze", 4, 2);
        effect(all, "alchediarrhea", 7, 2);
        effect(all, "warp_ward", 3, 2);
    }

    private static void effect(List<LegacyAsset> all, String name, int column, int row) {
        all.add(LegacyAsset.of("textures/mob_effect/" + name + ".png")
                .five("textures/misc/potions.png")
                .cropped(column * EFFECT_CELL, EFFECT_TOP + row * EFFECT_CELL, EFFECT_CELL, EFFECT_CELL));
    }

    private static void research(List<LegacyAsset> all) {
        all.add(item("alchemometer", "thaumometer", null));
        all.add(LegacyAsset.of(SCANNER_MESH).four("textures/models/scanner.obj"));
        all.add(LegacyAsset.of("textures/item/legacy/scanner.png").four("textures/models/scanner.png"));
        all.add(LegacyAsset.of("textures/item/legacy/scanscreen.png").four("textures/models/scanscreen.png"));
        all.add(item("alchemonomicon", "thaumonomicon", "thaumonomicon"));
        all.add(item("research_notes", "researchnotes", "researchnotes"));
        all.add(item("scribing_tools", "scribing_tools", null));
        all.add(LegacyAsset.of("textures/misc/script.png").five("textures/misc/script.png"));

        String[] branches = {"basics", "arcana", "alchemy", "artifice", "golemancy", "eldritch"};
        for (int i = 0; i < branches.length; i++) {
            all.add(LegacyAsset.of("textures/gui/research_background/" + branches[i] + ".png")
                    .five("textures/gui/gui_research_back_" + (i + 1) + ".jpg").fromJpeg());
        }
        all.add(LegacyAsset.of("textures/gui/research_overlay.png").five("textures/gui/gui_research_back_over.png"));

        all.add(LegacyAsset.of("textures/gui/research_table.png").five("textures/gui/gui_research_table.png"));
        all.add(LegacyAsset.of("textures/gui/research_parchment.png").five("textures/research/parchment3.png"));
        all.add(LegacyAsset.of("textures/gui/research_hex.png").five("textures/gui/hex1.png"));
        all.add(LegacyAsset.of("textures/gui/research_hex_lit.png").five("textures/gui/hex2.png"));

        all.add(LegacyAsset.of("models/block/research_table.json").five("models/block/research_table.json").asModel()
                .forModel(RESEARCH_TABLE));
        all.add(LegacyAsset.of("models/item/research_table.json").five("models/item/research_table.json").asModel()
                .forModel(RESEARCH_TABLE));
        for (String face : List.of("research_table_top", "research_table_side", "research_table_front",
                "base_wood_grain")) {
            all.add(LegacyAsset.of("textures/block/legacy/" + face + ".png").five("textures/blocks/" + face + ".png"));
        }
        all.add(LegacyAsset.of("textures/entity/research_table.png").five("textures/blocks/research_table_model.png")
                .forModel(RESEARCH_TABLE));
        all.add(block("research_table_quill", "tablequill", null));

        all.add(LegacyAsset.of("textures/gui/research_browser.png").five("textures/gui/gui_research_browser.png"));
        all.add(LegacyAsset.of("textures/gui/research_book.png").five("textures/gui/gui_researchbook.png"));
        all.add(LegacyAsset.of("textures/gui/research_book_overlay.png")
                .five("textures/gui/gui_researchbook_overlay.png"));
        all.add(LegacyAsset.of("textures/gui/research_category/basics.png")
                .five("textures/items/thaumonomicon_cheat.png").four("textures/items/thaumonomiconcheat.png"));
        String[][] tabs = {{"arcana", "r_thaumaturgy"}, {"alchemy", "r_crucible"}, {"artifice", "r_artifice"},
                {"golemancy", "r_golemancy"}, {"eldritch", "r_eldritch"}};
        for (String[] tab : tabs) {
            all.add(LegacyAsset.of("textures/gui/research_category/" + tab[0] + ".png")
                    .five("textures/research/" + tab[1] + ".png"));
        }
    }

    private static void aura(List<LegacyAsset> all) {
        all.add(LegacyAsset.of(CRYSTAL_MESH).five("models/obj/crystal.obj"));
        all.add(LegacyAsset.of("textures/block/legacy/crystal.png").five("textures/blocks/crystal.png"));
        all.add(LegacyAsset.of("textures/item/legacy/crystal_planter.png").five("textures/items/crystal_planter.png"));
        all.add(LegacyAsset.of(STABILIZER_MESH).five("models/obj/node_stabilizer.obj"));
        all.add(LegacyAsset.of("textures/entity/node_stabilizer.png").five("textures/models/node_stabilizer.png"));
        all.add(LegacyAsset.of("textures/item/legacy/node_stabilizer.png")
                .five("textures/models/node_stabilizer.png"));
        all.add(LegacyAsset.of("textures/entity/node_stabilizer_over.png")
                .five("textures/models/node_stabilizer_over.png"));

        all.add(LegacyAsset.of("textures/entity/node_core.png").five("textures/misc/nodes.png")
                .cropped(0, 0, 32 * NODE_CELL, NODE_CELL));
        all.add(LegacyAsset.of("textures/entity/node_halo.png").five("textures/misc/nodes.png")
                .cropped(0, 25 * NODE_CELL, 16 * NODE_CELL, NODE_CELL));
        all.add(LegacyAsset.of("textures/entity/node_types.png").five("textures/misc/nodes.png")
                .cropped(0, NODE_CELL, 32 * NODE_CELL, NodeType.values().length * NODE_CELL));
        all.add(LegacyAsset.of("textures/entity/node_bubble.png").five("textures/misc/node_bubble.png"));

        for (int variant = 0; variant < 3; variant++) {
            String ours = variant == 0 ? "" : "_" + variant;
            all.add(block("taint_crust" + ours, "taint_crust_" + variant, null));
            all.add(block("taint_soil" + ours, "taint_soil_" + variant, null));
            all.add(block("taint_log" + ours, "log_taintwood_" + variant, null));
        }
        all.add(block("taint_log_top", "log_taintwood_top", null));
        all.add(block("taint_rock", "taint_rock", null));
        for (String part : List.of("taint_fibre", "taint_growth_1", "taint_growth_2", "taint_growth_3",
                "taint_growth_4", "taint_orb_0", "taint_orb_1", "taint_orb_2")) {
            all.add(LegacyAsset.of("models/block/" + part + ".json").five("models/block/" + part + ".json")
                    .asModel("cutout"));
        }
        for (String picture : List.of("taint_fibres", "taint_growth_1", "taint_growth_2", "taint_growth_3",
                "taint_growth_4", "taint_growth_5")) {
            all.add(LegacyAsset.of("textures/block/legacy/" + picture + ".png")
                    .five("textures/blocks/" + picture + ".png"));
        }
        all.add(block("flux_goo_still", "flux_goo", null));
        all.add(block("flux_goo_flow", "flux_goo", null));
    }

    // the original names a part's loose picture and its material the other way round from ours
    private static void wands(List<LegacyAsset> all) {
        String[][] caps = {{"iron", "iron"}, {"gold", "gold"}, {"brass", "brass"}, {"alchemium", "thaumium"},
                {"void", "void"}};
        for (String[] cap : caps) {
            all.add(wandPart("cap_" + cap[0] + "_mat", "cap_" + cap[1]));
            all.add(wandPart("cap_" + cap[0], "cap_" + cap[1] + "_mat"));
        }
        all.add(wandPart("cap_alchemium_inert_mat", "cap_thaumium_inert"));
        all.add(wandPart("cap_void_inert_mat", "cap_void_inert"));

        for (String rod : List.of("greatwood", "silverwood", "obsidian", "ice", "quartz", "reed", "blaze", "bone")) {
            all.add(wandPart("rod_" + rod + "_mat", "rod_" + rod));
            all.add(wandPart("rod_" + rod, "rod_" + rod + "_mat"));
        }
        all.add(wandPart("rod_wood", "rod_wood_mat"));
        for (String rod : List.of("greatwood", "silverwood", "obsidian", "ice", "quartz", "reed", "blaze", "bone",
                "primal")) {
            all.add(wandPart("staff_rod_" + rod, "staff_rod_" + rod));
        }
        all.add(wandPart("rod_primal", "rod_primal_mat"));
        all.add(LegacyAsset.of(STAFF_MESH).five("models/obj/staff.obj"));

        for (String focus : List.of("builder", "excavation", "fire", "frost", "grapple", "hellbat", "pech", "primal",
                "shard", "shock", "trade")) {
            all.add(wandPart("focus_" + focus, "focus_" + focus));
        }
        all.add(wandPart("focus_hole", "focus_portablehole"));

        all.add(LegacyAsset.of("textures/gui/arcane_workbench.png").five("textures/gui/gui_arcaneworkbench.png"));
        all.add(LegacyAsset.of("models/block/arcane_workbench.json").five("models/block/arcane_workbench.json")
                .asModel());
        for (String face : List.of("arcane_workbench_top", "arcane_workbench_side")) {
            all.add(LegacyAsset.of("textures/block/legacy/" + face + ".png").five("textures/blocks/" + face + ".png"));
        }
        all.add(LegacyAsset.of("models/block/focal_manipulator.json")
                .five("models/block/wand_workbench.json").asModel());
        for (String face : List.of("wand_workbench_top", "wand_workbench_side", "base_stone")) {
            all.add(LegacyAsset.of("textures/block/legacy/" + face + ".png").five("textures/blocks/" + face + ".png"));
        }
        all.add(LegacyAsset.of("textures/gui/focal_manipulator.png").five("textures/gui/gui_wandtable.png"));
        all.add(LegacyAsset.of("textures/gui/focus_wheel.png").five("textures/misc/radial.png"));
        all.add(LegacyAsset.of("textures/gui/focus_wheel_back.png").five("textures/misc/radial2.png"));
        for (FocusUpgrade upgrade : FocusUpgrade.values()) {
            all.add(LegacyAsset.of("textures/gui/focus_upgrade/" + upgrade.getSerializedName() + ".png")
                    .five("textures/foci/" + ORIGINAL_UPGRADE.get(upgrade) + ".png"));
        }

        all.add(LegacyAsset.of(RELAY_MESH).five("models/obj/vis_relay.obj"));
        all.add(LegacyAsset.of("textures/entity/vis_relay.png").five("textures/models/vis_relay.png"));
        all.add(LegacyAsset.of("textures/item/legacy/vis_relay.png").five("textures/models/vis_relay.png"));
        all.add(LegacyAsset.of("textures/entity/centrifuge.png").five("textures/models/centrifuge.png")
                .forModel(CENTRIFUGE));
        all.add(LegacyAsset.of(CRYSTALLIZER_MESH).five("models/obj/crystallizer.obj"));
        all.add(LegacyAsset.of(THAUMATORIUM_MESH).five("models/obj/thaumatorium.obj"));
        for (String picture : List.of("mirrorframe", "mirrorframe2", "mirrorpane", "mirrorpaneopen", "mirrorpanetrans")) {
            all.add(LegacyAsset.of("textures/block/legacy/" + picture + ".png")
                    .five("textures/blocks/" + picture + ".png"));
        }
        all.add(LegacyAsset.of("textures/item/legacy/hand_mirror.png").five("textures/items/hand_mirror.png"));
        all.add(LegacyAsset.of("textures/gui/hand_mirror.png").five("textures/gui/gui_handmirror.png"));
        all.add(LegacyAsset.of("textures/misc/mirror_tunnel.png").five("textures/misc/tunnel.png"));
        all.add(LegacyAsset.of("textures/misc/mirror_field.png").five("textures/misc/particlefield.png"));
        all.add(LegacyAsset.of("models/block/infernal_furnace.json").five("models/block/infernal_furnace.json")
                .asModel("cutout"));
        all.add(LegacyAsset.of("textures/block/legacy/infernal_furnace.png")
                .five("textures/blocks/infernal_furnace.png"));
        all.add(LegacyAsset.of("textures/entity/bore.png").five("textures/models/bore.png").forModel(BORE));
        all.add(LegacyAsset.of("textures/entity/jar.png").five("textures/models/jar.png").forModel(JAR));
        all.add(LegacyAsset.of("textures/entity/label.png").five("textures/models/label.png"));
        all.add(LegacyAsset.of("textures/gui/arcane_bore.png").five("textures/gui/gui_arcanebore.png"));
        all.add(LegacyAsset.of("textures/misc/bore_beam_1.png").five("textures/misc/beam1.png"));
        all.add(LegacyAsset.of("textures/misc/bore_beam_2.png").five("textures/misc/beam2.png"));
        all.add(LegacyAsset.of("textures/entity/brain.png").five("textures/models/brain2.png").forModel(BRAIN));
        all.add(LegacyAsset.of("textures/entity/jar_brine.png").five("textures/models/jarbrine.png")
                .forModel(JAR));
        all.add(LegacyAsset.of("models/block/brain_jar_item.json").five("models/block/jar_brain_item.json")
                .asModel("translucent"));
        for (String picture : List.of("flesh", "jar_brine")) {
            all.add(LegacyAsset.of("textures/block/legacy/" + picture + ".png")
                    .five("textures/blocks/" + picture + ".png"));
        }
        all.add(LegacyAsset.of("textures/entity/thaumatorium.png").five("textures/models/thaumatorium.png"));
        all.add(LegacyAsset.of("textures/gui/thaumatorium.png").five("textures/gui/gui_thaumatorium.png"));
        all.add(LegacyAsset.of("models/block/mnemonic_matrix.json").five("models/block/brain_box.json").asModel());
        all.add(LegacyAsset.of("textures/block/legacy/brain_box.png").five("textures/blocks/brain_box.png"));
        all.add(LegacyAsset.of("textures/entity/crystallizer.png").five("textures/models/crystallizer.png"));
        all.add(LegacyAsset.of(WAND_MESH).five("models/obj/wand.obj"));

        all.add(LegacyAsset.of(TURRET_MESH).five("models/obj/turret.obj"));
        all.add(LegacyAsset.of("textures/entity/autocaster.png").five("textures/models/focus_turret.png"));
        all.add(LegacyAsset.of("textures/entity/eldritch_autocaster.png")
                .five("textures/models/focus_turret_eldritch.png"));
        all.add(LegacyAsset.of("textures/gui/autocaster.png").five("textures/gui/gui_turret_focus.png"));
        all.add(LegacyAsset.of("textures/item/legacy/autocaster.png").five("textures/items/turret_focus.png"));
        all.add(LegacyAsset.of("textures/entity/automated_crossbow.png").five("textures/models/crossbow.png")
                .forModel(CROSSBOW));
        all.add(LegacyAsset.of(CROSSBOW_ADVANCED_MESH).five("models/obj/crossbow_advanced.obj"));
        all.add(LegacyAsset.of("textures/entity/advanced_automated_crossbow.png")
                .five("textures/models/crossbow_advanced.png"));
        all.add(LegacyAsset.of("textures/entity/node_magnet.png").five("textures/models/nodemagnet.png")
                .forModel(NODE_MAGNET));
        all.add(LegacyAsset.of(HEMISPHERE_MESH).five("models/obj/hemis.obj"));
        for (int frame = 1; frame <= 15; frame++) {
            all.add(LegacyAsset.of("textures/entity/ripple" + frame + ".png")
                    .five("textures/models/ripple" + frame + ".png"));
        }
        all.add(LegacyAsset.of("textures/gui/automated_crossbow.png").five("textures/gui/gui_turret_basic.png"));
        all.add(LegacyAsset.of("textures/gui/advanced_automated_crossbow.png")
                .five("textures/gui/gui_turret_advanced.png"));
        for (String[] placer : new String[][] {{"automated_crossbow", "turret_basic"},
                {"advanced_automated_crossbow", "turret_advanced"}, {"node_magnet", "turret_magnet"}}) {
            all.add(LegacyAsset.of("textures/item/legacy/" + placer[0] + ".png")
                    .five("textures/items/" + placer[1] + ".png"));
        }

        for (String skin : List.of("firebat", "vampirebat")) {
            all.add(LegacyAsset.of("textures/entity/" + skin + ".png")
                    .five("textures/models/creature/" + skin + ".png").forModel(FIREBAT));
        }

        all.add(LegacyAsset.of("textures/entity/angry_zombie.png").five("textures/models/creature/bzombie.png")
                .four("textures/models/bzombie.png"));
    }

    private static void essentia(List<LegacyAsset> all) {
        all.add(LegacyAsset.of(ALEMBIC_MESH).four("textures/models/alembic.obj"));
        all.add(LegacyAsset.of("textures/entity/alembic.png").four("textures/models/alembic.png"));
        all.add(LegacyAsset.of("textures/item/legacy/alembic.png").four("textures/models/alembic.png"));

        for (String fill : List.of("", "_1", "_2", "_3", "_4")) {
            for (String kind : List.of("jar", "jar_labelled")) {
                String path = "models/block/jar/" + kind + fill + ".json";
                all.add(LegacyAsset.of(path).five("models/block/jar_normal.json").asModelOver(path));
                all.add(LegacyAsset.of("models/block/void_jar/" + kind + fill + ".json")
                        .five("models/block/jar_void.json").asModelOver(path));
            }
        }
        all.add(LegacyAsset.of(TUBE_MESH).five("models/obj/tube.obj"));
        all.add(LegacyAsset.of("textures/entity/valve.png").five("textures/models/valve.png").forModel(VALVE));
        all.add(LegacyAsset.of("textures/block/legacy/tube.png").five("textures/blocks/tube.png"));
        for (String tube : List.of("tube_normal", "tube_valve", "tube_oneway", "tube_restrict", "tube_filter",
                "tube_buffer")) {
            all.add(LegacyAsset.of("textures/item/legacy/" + tube + ".png").five("textures/items/" + tube + ".png"));
        }
        for (String face : List.of("jar_side", "jar_top", "jar_bottom", "jar_side_void", "jar_top_void",
                "animatedglow")) {
            all.add(LegacyAsset.of("textures/block/legacy/" + face + ".png").five("textures/blocks/" + face + ".png"));
        }
        all.add(LegacyAsset.of("models/block/crucible.json").five("models/block/crucible.json").asModel());
        for (String face : List.of("bottom", "inner", "side", "top")) {
            all.add(LegacyAsset.of("textures/block/legacy/crucible_" + face + ".png")
                    .five("textures/blocks/crucible_" + face + ".png"));
        }
        all.add(block("smelter_front", "smelter_basic_front_off", null));
        all.add(block("smelter_front_on", "smelter_basic_front_on", null));
        all.add(block("smelter_side", "smelter_basic_side", null));
        all.add(block("smelter_top", "smelter_top", null));
        for (String[] tier : new String[][] {{"alchemium_smelter", "thaumium"}, {"void_smelter", "void"}}) {
            all.add(block(tier[0] + "_front", "smelter_" + tier[1] + "_front_off", null));
            all.add(block(tier[0] + "_front_on", "smelter_" + tier[1] + "_front_on", null));
            all.add(block(tier[0] + "_side", "smelter_" + tier[1] + "_side", null));
        }
        all.add(LegacyAsset.of("textures/block/legacy/void_metal.png").five("textures/blocks/void_metal.png"));
        for (String part : List.of("smelter_aux", "smelter_vent")) {
            all.add(LegacyAsset.of("models/block/" + part + ".json").five("models/block/" + part + ".json")
                    .asModel());
        }
        for (String picture : List.of("smelter_aux", "smelter_aux_pump", "smelter_vent")) {
            all.add(LegacyAsset.of("textures/block/legacy/" + picture + ".png")
                    .five("textures/blocks/" + picture + ".png"));
        }
        all.add(LegacyAsset.of("textures/gui/essentia_smelter.png").five("textures/gui/gui_smelter.png"));
        all.add(block("nitor", "nitor", null));
        all.add(block("nitor_core", "nitor_core", null));
        all.add(LegacyAsset.of("textures/particle/mote.png").five("textures/misc/particles.png")
                .cropped(0, 224, 32, 32));
        all.add(LegacyAsset.of("textures/misc/legacy_particles.png").five("textures/misc/particles.png"));
        all.add(LegacyAsset.of("textures/misc/legacy_particles2.png").five("textures/misc/particles2.png"));

        all.add(item("alumentum", "alumentum", null));
        all.add(item("crystallized_essence", "crystal_essence", null));
        all.add(item("filter", "filter", null));
        all.add(item("jar_brace", "jar_brace", null));
        all.add(item("jar_label", "label", null));
        all.add(item("jar_label_overlay", "label_overlay", null));
        all.add(item("phial", "phial", null));
        all.add(item("phial_contents", "phial_overlay", null));
        all.add(item("void_seed", "void_seed", null));
        all.add(item("void_ingot", "ingot_void", "voidingot"));
        all.add(item("void_nugget", "nugget_void", "nuggetvoid"));
        all.add(item("void_plate", "void_plate", null));
    }

    private static void infusion(List<LegacyAsset> all) {
        all.add(block("infusion_cost_stone", "matrix_cost", null));
        all.add(block("infusion_speed_stone", "matrix_speed", null));
        all.add(LegacyAsset.of("models/block/pedestal.json").five("models/block/pedestal_normal.json").asModel());
        for (String face : List.of("pedestal_side", "pedestal_top")) {
            all.add(LegacyAsset.of("textures/block/legacy/" + face + ".png").five("textures/blocks/" + face + ".png"));
        }

        all.add(LegacyAsset.of("textures/item/legacy/infuser.png").five("textures/blocks/infuser_normal.png"));
        all.add(LegacyAsset.of("textures/entity/infusion_matrix.png").five("textures/blocks/infuser_normal.png")
                .forModel(INFUSER));
        all.add(LegacyAsset.of(PILLAR_MESH).five("models/obj/pillar.obj"));
        all.add(LegacyAsset.of("textures/entity/arcane_pillar.png").five("textures/blocks/pillar_normal.png"));

        all.add(LegacyAsset.of("textures/entity/ancient_pillar.png").five("textures/blocks/pillar_ancient.png"));
        all.add(LegacyAsset.of("textures/entity/eldritch_pillar.png").five("textures/blocks/pillar_eldritch.png"));
        all.add(LegacyAsset.of("textures/entity/infusion_matrix_ancient.png").five("textures/blocks/infuser_ancient.png")
                .forModel(INFUSER));
        all.add(LegacyAsset.of("textures/entity/infusion_matrix_eldritch.png")
                .five("textures/blocks/infuser_eldritch.png").forModel(INFUSER));
        all.add(LegacyAsset.of("models/block/eldritch_pedestal.json").five("models/block/pedestal_eldritch.json")
                .asModel());
        for (String face : List.of("pedestal_ancient_side", "pedestal_eldritch_side", "ancient_tile",
                "eldritch_stone_1")) {
            all.add(LegacyAsset.of("textures/block/legacy/" + face + ".png").five("textures/blocks/" + face + ".png"));
        }

        all.add(LegacyAsset.of(OBELISK_CAP_MESH).five("models/obj/obelisk_cap.obj"));
        for (String picture : List.of("obelisk_cap", "obelisk_cap_2", "obelisk_side", "obelisk_side_2")) {
            all.add(LegacyAsset.of("textures/entity/" + picture + ".png").five("textures/models/" + picture + ".png"));
        }
        all.add(LegacyAsset.of("textures/entity/eldritch_portal.png").five("textures/misc/eldritch_portal.png"));
        all.add(LegacyAsset.of("textures/misc/wispy.png").five("textures/misc/wispy.png"));
        all.add(LegacyAsset.of("textures/entity/banner_cultist.png").five("textures/models/banner_cultist.png")
                .forModel(BANNER));
        all.add(LegacyAsset.of("textures/entity/banner_blank.png").five("textures/models/banner_blank.png")
                .forModel(BANNER));
        for (String layer : List.of("banner_stand", "banner", "banner_symbol", "banner_cultist")) {
            all.add(item(layer, layer, null));
        }

        all.add(item("primordial_pearl", "primordial_pearl", null));
        all.add(item("loot_bag_common", "loot_bag_common", "lootbag"));
        all.add(item("loot_bag_uncommon", "loot_bag_uncommon", "lootbagunc"));
        all.add(item("loot_bag_rare", "loot_bag_rare", "lootbagrare"));

        all.add(block("ancient_stone", "ancient_stone_1", null));
        for (int i = 2; i <= 5; i++) {
            all.add(block("ancient_stone_" + i, "ancient_stone_" + i, null));
        }
        all.add(block("ancient_tile", "ancient_tile", null));
        for (int i = 1; i <= 4; i++) {
            all.add(block("ancient_rock_" + i, "ancient_rock_stone_" + i, null));
        }
        all.add(block("eldritch_stone", "eldritch_stone_1", null));
        all.add(block("eldritch_stone_2", "eldritch_stone_2", null));
        all.add(block("eldritch_stone_3", "eldritch_stone_3", null));
        all.add(block("glyphed_stone", "ancient_glyph", null));
        all.add(block("ancient_doorway", "eldritch_door", null));
        all.add(block("crusted_stone", "crust", null));
        all.add(block("glowing_crusted_stone", "crust_glow", null));
        all.add(block("porous_stone", "porous_stone", null));
        for (String kind : List.of("loot_urn", "loot_crate")) {
            all.add(block(kind + "_top", kind + "_top", null));
            for (int rarity = 0; rarity <= 2; rarity++) {
                all.add(block(kind + "_side_" + rarity, kind + "_side_" + rarity, null));
            }
        }
        all.add(LegacyAsset.of("sounds/legacy/urnbreak.ogg").five("sounds/urnbreak.ogg"));
        all.add(LegacyAsset.of("textures/block/legacy/ancient_bedrock.png").five("textures/misc/particlefield32.png"));
        all.add(LegacyAsset.of("textures/entity/eldritch_lock.png").five("textures/models/eldritch_lock.png"));
        all.add(LegacyAsset.of("textures/entity/eldritch_cube.png").five("textures/models/eldritch_cube.png"));
        all.add(LegacyAsset.of("textures/entity/crabvent.png").five("textures/models/crabvent.png"));
        all.add(LegacyAsset.of(CRAB_VENT_MESH).five("models/obj/crabvent.obj"));
        all.add(item("runed_tablet", "runed_tablet", null));
        String[][] baubles = {{"mundane_amulet", "amulet_mundane"}, {"mundane_ring", "ring_mundane"},
                {"mundane_belt", "girdle_mundane"}, {"fancy_amulet", "amulet_fancy"}, {"fancy_ring", "ring_fancy"},
                {"fancy_belt", "girdle_fancy"}, {"apprentice_ring", "ring_apprentice"},
                {"runic_amulet", "amulet_runic"}, {"emergency_runic_amulet", "amulet_runic_emergency"},
                {"protection_ring", "ring_protection"}, {"runic_ring", "ring_runic"},
                {"charged_runic_ring", "ring_runic_charge"}, {"revitalizing_runic_ring", "ring_runic_regen"},
                {"runic_girdle", "girdle_runic"}, {"kinetic_runic_girdle", "girdle_runic_kinetic"},
                {"vis_stone", "amulet_vis_stone"}, {"vis_amulet", "amulet_vis"},
                {"thaumostatic_girdle", "girdle_hover"}, {"verdant_heart", "ring_verdant"},
                {"triple_meat_treat", "tripletreat"}};
        for (String[] bauble : baubles) {
            all.add(item(bauble[0], bauble[1], null));
        }
        all.add(LegacyAsset.of(HEMIS_MESH).five("models/obj/hemis.obj"));
        for (int frame = 1; frame <= 15; frame++) {
            all.add(LegacyAsset.of("textures/entity/hemis" + frame + ".png").five("textures/models/hemis" + frame + ".png"));
            all.add(LegacyAsset.of("textures/entity/ripple" + frame + ".png").five("textures/models/ripple" + frame + ".png"));
        }
        all.add(LegacyAsset.of("sounds/legacy/runicshieldeffect.ogg").five("sounds/runicShieldEffect.ogg"));
        all.add(LegacyAsset.of("textures/block/legacy/liquid_death.png").five("textures/blocks/animatedglow.png"));
        all.add(LegacyAsset.of("textures/item/legacy/liquid_death_bucket.png").five("textures/items/bucket_death.png"));
        for (String sound : List.of("wind1", "wind2", "pump1", "pump2", "pump3", "evilportal")) {
            all.add(LegacyAsset.of("sounds/legacy/" + sound + ".ogg").five("sounds/" + sound + ".ogg"));
        }
        all.add(LegacyAsset.of("sounds/legacy/runicshieldcharge.ogg").five("sounds/runicShieldCharge.ogg"));
        all.add(LegacyAsset.of("textures/entity/eldritch_warden.png").five("textures/models/creature/eldritch_warden.png")
                .forModel(ELDRITCH_GUARDIAN));
        all.add(LegacyAsset.of("textures/entity/eldritch_golem.png").five("textures/models/creature/eldritch_golem.png")
                .forModel(ELDRITCH_GOLEM));
        for (int layer = 1; layer <= 2; layer++) {
            all.add(LegacyAsset.of("textures/models/armor/crimson_praetor_layer_" + layer + ".png")
                    .five("textures/models/armor/cultist_leader_armor.png").forModel(LEADER_ARMOUR));
        }
        for (String name : List.of("crimson_praetor_helm", "crimson_praetor_chest", "crimson_praetor_legs")) {
            all.add(item(name, name, null));
        }
        all.add(LegacyAsset.of("sounds/legacy/jacobs.ogg").five("sounds/jacobs.ogg"));
    }

    private static Map<FocusUpgrade, String> originalUpgrades() {
        Map<FocusUpgrade, String> named = new java.util.EnumMap<>(FocusUpgrade.class);
        for (FocusUpgrade upgrade : FocusUpgrade.values()) {
            named.put(upgrade, upgrade.getSerializedName().replace("_", ""));
        }
        named.put(FocusUpgrade.PERSISTENT, "persistant");
        return java.util.Collections.unmodifiableMap(named);
    }

    private static LegacyAsset wandPart(String ours, String original) {
        return LegacyAsset.of("textures/item/wand/" + ours + ".png").five("textures/items/wand/" + original + ".png");
    }

    private static void gear(List<LegacyAsset> all) {
        metalGear(all, "alchemium", "thaumium");
        metalGear(all, "void", "void");
        for (String[] tool : new String[][] {{"elemental_axe", "elemental_axe"},
                {"elemental_pickaxe", "elemental_pick"}, {"elemental_sword", "elemental_sword"},
                {"elemental_shovel", "elemental_shovel"}, {"elemental_hoe", "elemental_hoe"},
                {"primal_crusher", "primal_crusher"}}) {
            all.add(LegacyAsset.of("textures/item/legacy/" + tool[0] + ".png")
                    .five("textures/items/" + tool[1] + ".png"));
        }
        all.add(item("enchanted_fabric", "fabric", null));
        for (String[] part : new String[][] {{"primal_charm", "primal_charm"}, {"mirrored_glass", "mirrored_glass"},
                {"morphic_resonator", "morphic_resonator"}, {"clockwork_mind", "mind_clockwork"},
                {"bioalchemic_mind", "mind_biothaumic"}}) {
            all.add(LegacyAsset.of("textures/item/legacy/" + part[0] + ".png").five("textures/items/" + part[1] + ".png"));
        }
        all.add(LegacyAsset.of("textures/entity/bellows.png").five("textures/models/bellows.png"));
        for (String model : List.of("lamp", "lamp_stand", "water_jug")) {
            all.add(LegacyAsset.of("models/block/legacy/" + model + ".json")
                    .five("models/block/" + model + ".json").asModel());
        }
        for (String face : List.of("lamp_on", "lamp_off", "lamp_grow_on", "lamp_grow_off", "lamp_fert_on",
                "lamp_fert_off", "base_metal", "water_jug")) {
            all.add(LegacyAsset.of("textures/block/legacy/" + face + ".png").five("textures/blocks/" + face + ".png"));
        }
        all.add(LegacyAsset.of("textures/entity/hungry_chest.png").five("textures/models/chesthungry.png"));
        for (String model : List.of("levitator_on", "levitator_off", "arcane_ear_on", "arcane_ear_off")) {
            all.add(LegacyAsset.of("models/block/" + model + ".json")
                    .five("models/block/" + model + ".json").asModel("cutout"));
        }
        for (String model : List.of("redstone_relay_on", "redstone_relay_off")) {
            all.add(LegacyAsset.of("models/block/legacy/" + model + ".json")
                    .five("models/block/" + model + ".json").asModel("cutout"));
        }
        all.add(LegacyAsset.of("models/block/legacy/recharge_pedestal.json")
                .five("models/block/recharge_pedestal.json").asModel());
        for (String model : List.of("dioptra_on", "dioptra_off")) {
            all.add(LegacyAsset.of("models/block/" + model + ".json").five("models/block/" + model + ".json")
                    .asModel());
        }
        for (String face : List.of("recharge_pedestal_side", "recharge_pedestal_top", "recharge_pedestal_gem",
                "dioptra_side_on", "dioptra_side_off", "dioptra_top", "arcane_stone_1")) {
            all.add(LegacyAsset.of("textures/block/legacy/" + face + ".png").five("textures/blocks/" + face + ".png"));
        }
        all.add(LegacyAsset.of("textures/entity/dioptra_grid.png").five("textures/misc/gridblock.png"));
        all.add(LegacyAsset.of("textures/entity/dioptra_side.png").five("textures/models/dioptra_side.png"));
        for (String model : List.of("aura_totem", "aura_totem_pole")) {
            all.add(LegacyAsset.of("models/block/legacy/" + model + ".json").five("models/block/" + model + ".json")
                    .asModel());
        }
        for (String face : List.of("aura_totem_push", "aura_totem_pull", "aura_totem_top", "aura_totem_pole_inner",
                "aura_totem_pole_outer", "aura_totem_pole_pure")) {
            all.add(LegacyAsset.of("textures/block/legacy/" + face + ".png").five("textures/blocks/" + face + ".png"));
        }
        all.add(LegacyAsset.of("textures/gui/aura_totem.png").five("textures/gui/gui_auratotem.png"));
        for (String skin : List.of("pech_forage", "pech_thaum", "pech_stalker")) {
            all.add(LegacyAsset.of("textures/entity/" + skin + ".png")
                    .five("textures/models/creature/" + skin + ".png").forModel(PECH));
        }
        all.add(LegacyAsset.of("textures/gui/pech.png").five("textures/gui/gui_pech.png"));
        all.add(LegacyAsset.of("textures/item/legacy/coin.png").five("textures/items/coin.png"));
        all.add(LegacyAsset.of("textures/item/legacy/knowledge_fragment.png")
                .five("textures/items/knowledge_fragment.png"));
        all.add(LegacyAsset.of("textures/entity/taint_crawler.png").five("textures/models/creature/crawler.png"));
        all.add(LegacyAsset.of("textures/entity/taintacle.png").five("textures/models/creature/taintacle.png")
                .forModel(TAINTACLE));
        all.add(LegacyAsset.of("textures/item/legacy/tainted_goo.png").five("textures/items/taint_slime.png"));
        for (String[] animal : new String[][] {{"tainted_cow", "cow"}, {"tainted_pig", "pig"},
                {"tainted_chicken", "chicken"}, {"tainted_sheep", "sheep"}, {"tainted_sheep_fur", "sheep_fur"},
                {"tainted_creeper", "creeper"}, {"tainted_villager", "villager"}, {"tainted_rabbit", "taintrabbit"},
                {"alchemic_slime", "tslime"}}) {
            all.add(LegacyAsset.of("textures/entity/" + animal[0] + ".png")
                    .five("textures/models/creature/" + animal[1] + ".png"));
        }
        all.add(LegacyAsset.of("textures/item/legacy/taint_tendril.png").five("textures/items/taint_tendril.png"));
        all.add(block("taint_geyser_side", "taint_geyser_side", null));
        all.add(block("taint_geyser_top", "taint_geyser_top", null));
        for (String sound : List.of("swarm1", "swarm2", "swarm3", "swarmattack", "fly1", "fly2", "roots1", "roots2",
                "roots3", "tentacle1", "tentacle2", "tentacle3", "gore1", "gore2")) {
            all.add(LegacyAsset.of("sounds/legacy/" + sound + ".ogg").five("sounds/" + sound + ".ogg"));
        }
        all.add(LegacyAsset.of("textures/item/legacy/wispy_essence.png").five("textures/items/wispy_essence.png"));
        for (String sound : List.of("wisplive1", "wisplive2", "wisplive3", "wispdead", "zap1", "zap2", "ice1", "ice2",
                "ice3", "pech_idle1", "pech_idle2", "pech_idle3", "pech_hit1", "pech_hit2", "pech_death",
                "pech_charge1", "pech_charge2", "pech_trade", "pech_dice", "coins")) {
            all.add(LegacyAsset.of("sounds/legacy/" + sound + ".ogg").five("sounds/" + sound + ".ogg"));
        }
        for (String face : List.of("spa_side", "spa_top")) {
            all.add(LegacyAsset.of("textures/block/legacy/" + face + ".png").five("textures/blocks/" + face + ".png"));
        }
        all.add(LegacyAsset.of("textures/gui/spa.png").five("textures/gui/gui_spa.png"));
        all.add(LegacyAsset.of("textures/item/legacy/bath_salts.png").five("textures/items/bath_salts.png"));
        all.add(LegacyAsset.of("textures/item/legacy/purifying_fluid_bucket.png")
                .five("textures/items/bucket_pure.png"));
        for (String face : List.of("levitator_bottom", "levitator_side_on", "levitator_side_off", "levitator_top_on",
                "levitator_top_off", "redstone_relay_on", "redstone_relay_off", "arcane_ear_on", "arcane_ear_off",
                "base_wood")) {
            all.add(LegacyAsset.of("textures/block/legacy/" + face + ".png").five("textures/blocks/" + face + ".png"));
        }
        all.add(item("focus_pouch", "focus_pouch", "focuspouch"));
        all.add(LegacyAsset.of("textures/gui/focus_pouch.png").five("textures/gui/gui_focuspouch.png")
                .four("textures/gui/gui_focuspouch.png"));

        all.add(item("traveller_boots", "traveller_boots", "bootstraveler"));
        all.add(item("goggles_of_revealing", "goggles_revealing", "gogglesrevealing"));
        all.add(LegacyAsset.of("textures/models/armor/traveller_boots_layer_1.png")
                .five("textures/models/armor/bootstraveler.png").four("textures/models/bootstraveler.png"));
        all.add(LegacyAsset.of("textures/models/armor/goggles_layer_1.png")
                .five("textures/models/armor/goggles.png").four("textures/models/goggles.png"));

        for (String layer : List.of("fortress_layer_1", "fortress_layer_2")) {
            all.add(LegacyAsset.of("textures/models/armor/" + layer + ".png")
                    .five("textures/models/armor/fortress_armor.png")
                    .four("textures/models/fortress_armor.png")
                    .forModel(FORTRESS_ARMOUR));
        }
        all.add(item("fortress_helm", "fortress_helm", "thaumiumfortresshelm"));
        all.add(item("fortress_chest", "fortress_chest", "thaumiumfortresschest"));
        all.add(item("fortress_legs", "fortress_legs", "thaumiumfortresslegs"));

        robeSheets(all, "cloth", 1, "robes_1", "robes_1_overlay", null);
        robeSheets(all, "cloth", 2, "robes_2", "robes_2_overlay", null);
        // the original named these two halves the other way round
        robeSheets(all, "void_robe", 1, "void_robe_armor_overlay", "void_robe_armor", ROBE);
        robeSheets(all, "void_robe", 2, "void_robe_armor_overlay", "void_robe_armor", ROBE);

        all.add(item("cloth_chest", "cloth_chest", "clothchest"));
        all.add(item("cloth_chest_overlay", "cloth_chest_over", "clothchestover"));
        all.add(item("cloth_legs", "cloth_legs", "clothlegs"));
        all.add(item("cloth_legs_overlay", "cloth_legs_over", "clothlegsover"));
        all.add(item("cloth_boots", "cloth_boots", "clothboots"));
        all.add(item("cloth_boots_overlay", "cloth_boots_over", "clothbootsover"));
        all.add(item("void_robe_helm", "void_robe_helm", "voidrobehelm"));
        all.add(item("void_robe_chest", "void_robe_chest", "voidrobechest"));
        all.add(item("void_robe_chest_overlay", "void_robe_chest_over", "voidrobechestover"));
        all.add(item("void_robe_legs", "void_robe_legs", "voidrobelegs"));
        all.add(item("void_robe_legs_overlay", "void_robe_legs_over", "voidrobelegsover"));
    }

    private static void metalGear(List<LegacyAsset> all, String ours, String original) {
        String[][] pieces = {{"sword", "sword"}, {"pickaxe", "pick"}, {"axe", "axe"}, {"shovel", "shovel"},
                {"hoe", "hoe"}, {"helmet", "helm"}, {"chestplate", "chest"}, {"leggings", "legs"},
                {"boots", "boots"}};
        for (String[] piece : pieces) {
            all.add(item(ours + "_" + piece[0], original + "_" + piece[1], original + piece[1]));
        }
        for (int layer = 1; layer <= 2; layer++) {
            all.add(LegacyAsset.of("textures/models/armor/" + ours + "_layer_" + layer + ".png")
                    .five("textures/models/armor/" + original + "_" + layer + ".png")
                    .four("textures/models/" + original + "_" + layer + ".png"));
        }
    }

    private static void robeSheets(List<LegacyAsset> all, String robe, int layer, String cloth, String trim,
            String model) {
        LegacyAsset dyed = LegacyAsset.of("textures/models/armor/" + robe + "_layer_" + layer + ".png")
                .five("textures/models/armor/" + cloth + ".png").four("textures/models/" + cloth + ".png");
        LegacyAsset over = LegacyAsset.of("textures/models/armor/" + robe + "_layer_" + layer + "_overlay.png")
                .five("textures/models/armor/" + trim + ".png").four("textures/models/" + trim + ".png");
        all.add(model == null ? dyed : dyed.forModel(model));
        all.add(model == null ? over : over.forModel(model));
    }

    private static void leftovers(List<LegacyAsset> all) {
        all.add(item("sanity_soap", "sanity_soap", null));
        all.add(item("essentia_resonator", "resonator", null));
        all.add(LegacyAsset.of("sounds/legacy/alembicknock.ogg").five("sounds/alembicknock.ogg"));
        all.add(item("sanity_checker", "sanity_checker", null));
        all.add(LegacyAsset.of("textures/gui/hud.png").five("textures/gui/hud.png"));
        all.add(block("void_metal_block", "void_metal", null));
        for (String element : List.of("air", "fire", "water", "earth", "order", "entropy")) {
            all.add(item("primal_arrow_" + element, "primal_arrow_" + element, null));
        }
        all.add(item("bottle_taint", "bottle_taint", null));
        all.add(LegacyAsset.of("textures/block/legacy/taint_dust.png").five("textures/blocks/taint_dust.png"));
        for (String transfuser : List.of("essentia_input", "essentia_output")) {
            all.add(LegacyAsset.of("models/block/" + transfuser + ".json").five("models/block/" + transfuser + ".json")
                    .asModel("cutout"));
        }
        all.add(LegacyAsset.of("textures/block/legacy/essentia_input.png").five("textures/blocks/essentia_input.png"));
        all.add(item("thaumostatic_harness", "thaumostatic_harness", null));
        all.add(LegacyAsset.of("textures/models/armor/hover_harness_layer_1.png")
                .five("textures/models/armor/hoverharness.png"));
        all.add(LegacyAsset.of(HARNESS_MESH).five("models/obj/hoverharness.obj"));
        all.add(LegacyAsset.of("textures/entity/hover_harness_pack.png").five("textures/models/armor/hoverharness2.png"));
        all.add(LegacyAsset.of("textures/entity/lightning_ring.png").five("textures/models/armor/lightningring.png"));
        for (String drawn : List.of("bone_bow", "bone_bow_0", "bone_bow_1", "bone_bow_2")) {
            all.add(item(drawn, drawn, null));
        }
        all.add(item("sinister_stone", "sinister_stone", null));
        all.add(item("sinister_stone_on", "sinister_stone_active", null));
        all.add(LegacyAsset.of("models/block/candle.json").five("models/block/candle.json").asModel("cutout"));
        for (String picture : List.of("candle", "candlestub", "pattern_crafter", "pattern_crafter_modes")) {
            all.add(LegacyAsset.of("textures/block/legacy/" + picture + ".png")
                    .five("textures/blocks/" + picture + ".png"));
        }
        all.add(LegacyAsset.of("models/block/pattern_crafter.json").five("models/block/pattern_crafter.json")
                .asModel("cutout"));
        all.add(LegacyAsset.of("models/block/pattern_crafter_inv.json").five("models/block/pattern_crafter_inv.json")
                .asModel("cutout"));
        all.add(LegacyAsset.of("sounds/legacy/key.ogg").five("sounds/key.ogg"));
        for (String sound : List.of("poof1", "poof2", "spill", "bubble1", "bubble2", "bubble3", "bubble4", "jar1", "jar2", "jar3", "jar4", "page1", "page2", "crystal", "craftfail", "cameraticks", "squeek1", "squeek2", "creak1", "creak2", "rumble", "fireloop", "brain1", "brain2", "brain3", "brain4", "infuserstart", "infuser", "hhon", "hhoff", "write1", "write2", "erase", "swing1", "swing2", "heartbeat", "whispers")) {
            all.add(LegacyAsset.of("sounds/legacy/" + sound + ".ogg").five("sounds/" + sound + ".ogg"));
        }
    }

    private static void cult(List<LegacyAsset> all) {
        all.add(LegacyAsset.of("textures/entity/cultist.png").five("textures/models/creature/cultist.png"));
        all.add(LegacyAsset.of("textures/entity/cultist_portal.png").five("textures/misc/cultist_portal.png"));
        for (int layer = 1; layer <= 2; layer++) {
            all.add(LegacyAsset.of("textures/models/armor/crimson_robe_layer_" + layer + ".png")
                    .five("textures/models/armor/cultist_robe_armor.png").forModel(ROBE));
            all.add(LegacyAsset.of("textures/models/armor/crimson_plate_layer_" + layer + ".png")
                    .five("textures/models/armor/cultist_plate_armor.png").forModel(KNIGHT_ARMOUR));
        }
        all.add(LegacyAsset.of("textures/models/armor/crimson_boots_layer_1.png")
                .five("textures/models/armor/cultistboots.png"));
        for (String name : List.of("crimson_robe_helm", "crimson_robe_chest", "crimson_robe_legs",
                "crimson_plate_helm", "crimson_plate_chest", "crimson_plate_legs", "crimson_boots", "crimson_blade",
                "crimson_rites")) {
            all.add(item(name, name, null));
        }
        all.add(LegacyAsset.of("textures/models/armor/crimson_plate_husk.png")
                .five("textures/models/armor/zombie_plate_armor.png").forModel(KNIGHT_ARMOUR));
        all.add(LegacyAsset.of("textures/entity/inhabited_zombie.png").five("textures/models/creature/czombie.png"));
        all.add(LegacyAsset.of("textures/entity/eldritch_guardian.png")
                .five("textures/models/creature/eldritch_guardian.png").forModel(ELDRITCH_GUARDIAN));
        all.add(item("eldritch_eye", "eldritch_eye", null));
        all.add(LegacyAsset.of("textures/entity/eldritch_crab.png").five("textures/models/creature/crab.png")
                .forModel(ELDRITCH_CRAB));
        for (String sound : List.of("chant1", "chant2", "chant3", "monolith", "wandfail", "egattack", "shock1",
                "shock2", "learn", "crabtalk1", "crabtalk2", "crabtalk3", "crabclaw", "crabdeath", "egidle1", "egidle2",
                "egscreech", "egdeath")) {
            all.add(LegacyAsset.of("sounds/legacy/" + sound + ".ogg").five("sounds/" + sound + ".ogg"));
        }
    }

    private static void golems(List<LegacyAsset> all) {
        for (String mesh : List.of("golem_base", "golem_head_basic", "golem_head_smart", "golem_head_smart_armor",
                "golem_head_scout", "golem_head_scout_smart", "golem_arms_basic", "golem_arms_fine",
                "golem_arms_claws", "golem_arms_breakers", "golem_arms_darter", "golem_legs_walker",
                "golem_legs_wheel", "golem_legs_climber", "golem_legs_floater", "golem_armor", "golem_hauler")) {
            all.add(LegacyAsset.of("models/legacy/golem/" + mesh + ".obj").five("models/obj/" + mesh + ".obj"));
        }
        all.add(LegacyAsset.of("models/legacy/golem/golem_press.obj").five("models/obj/golembuilder.obj"));
        for (String[] material : new String[][] {{"wood", "wood"}, {"iron", "iron"}, {"clay", "clay"},
                {"brass", "brass"}, {"alchemium", "thaumium"}, {"void", "void"}}) {
            all.add(LegacyAsset.of("textures/entity/golem/mat_" + material[0] + ".png")
                    .five("textures/models/golems/mat_" + material[1] + ".png"));
        }
        for (String sheet : List.of("golem_head_other", "golem_arms_claws", "golem_arms_breakers",
                "golem_arms_darter", "golem_hauler", "golem_legs_floater", "golem_legs_wheel")) {
            all.add(LegacyAsset.of("textures/entity/golem/" + sheet + ".png")
                    .five("textures/models/golems/" + sheet + ".png"));
        }
        all.add(LegacyAsset.of("textures/entity/golem/base_metal.png").five("textures/blocks/base_metal.png"));
        all.add(LegacyAsset.of("textures/entity/golem/golem_press.png")
                .five("textures/models/golems/golembuilder.png"));
        all.add(LegacyAsset.of("textures/gui/golem_press.png").five("textures/gui/gui_golembuilder.png"));
        all.add(LegacyAsset.of("textures/gui/base.png").five("textures/gui/gui_base.png"));
        for (me.moonscenty.alchemia.golem.GolemTrait trait : me.moonscenty.alchemia.golem.GolemTrait.values()) {
            all.add(LegacyAsset.of("textures/gui/golem/tag_" + trait.key() + ".png")
                    .five("textures/misc/golem/tag_" + trait.key() + ".png"));
        }
        for (String[] part : new String[][] {{"head_basic", "head_basic"}, {"head_smart", "head_smart"},
                {"head_smart_armored", "head_smartarmor"}, {"head_scout", "head_scout"},
                {"head_smart_scout", "head_smartscout"}, {"arm_basic", "arms_basic"}, {"arm_fine", "arms_fine"},
                {"arm_claws", "arms_claws"}, {"arm_breakers", "arms_breakers"}, {"arm_darts", "arms_darts"},
                {"leg_walker", "legs_walker"}, {"leg_roller", "legs_roller"}, {"leg_climber", "legs_climber"},
                {"leg_flyer", "legs_flyer"}, {"addon_armored", "addon_armored"}, {"addon_fighter", "addon_fighter"},
                {"addon_hauler", "addon_hauler"}}) {
            all.add(LegacyAsset.of("textures/gui/golem/" + part[0] + ".png")
                    .five("textures/misc/golem/" + part[1] + ".png"));
        }
        all.add(item("void_gear", "void_gear", null));
        for (String[] item : new String[][] {{"golem", "golem"}, {"golem_bell", "golem_bell"},
                {"vision_module", "module_vision"}, {"aggression_module", "module_aggression"}}) {
            all.add(LegacyAsset.of("textures/item/legacy/" + item[0] + ".png")
                    .five("textures/items/" + item[1] + ".png"));
        }
        all.add(LegacyAsset.of("models/block/table_wood.json").five("models/block/table_wood.json").asModel());
        all.add(LegacyAsset.of("models/block/table_stone.json").five("models/block/table_stone.json").asModel());
        for (String face : List.of("table_stone_top", "table_stone_side", "table_wood_top", "table_wood_side")) {
            all.add(LegacyAsset.of("textures/block/legacy/" + face + ".png").five("textures/blocks/" + face + ".png"));
        }
        for (String seal : List.of("blank", "pickup", "pickup_advanced", "fill", "fill_advanced", "empty",
                "empty_advanced", "provider", "guard", "guard_advanced", "butcher", "harvest", "lumber", "breaker",
                "use")) {
            all.add(LegacyAsset.of("textures/item/legacy/seal_" + seal + ".png")
                    .five("textures/items/seals/seal_" + seal + ".png"));
        }
        all.add(LegacyAsset.of("textures/item/legacy/tallow.png").five("textures/items/tallow.png"));
        all.add(LegacyAsset.of("textures/misc/seal_area.png").five("textures/misc/seal_area.png"));
        all.add(LegacyAsset.of("textures/misc/frame_corner.png").five("textures/misc/frame_corner.png"));
        for (String sound : List.of("cameraclack1", "cameraclack2", "cameraclack3", "tool1", "tool2", "scan",
                "wand1", "wand2", "wand3", "craftstart")) {
            all.add(LegacyAsset.of("sounds/legacy/" + sound + ".ogg").five("sounds/" + sound + ".ogg"));
        }
    }

    private static void baseResources(List<LegacyAsset> all) {
        all.add(block("amber_ore", "ore_amber", "amberore"));
        all.add(block("cinnabar_ore", "ore_cinnabar", "cinnibar"));
        all.add(item("amber", "amber", "amber"));
        all.add(item("quicksilver", "quicksilver", "quicksilver"));
        all.add(item("quicksilver_drop", "nugget_quicksilver", "nuggetquicksilver"));
        all.add(item("zombie_brain", "brain", "brain"));
        for (String meat : new String[] {"beef", "chicken", "pork", "fish", "rabbit", "mutton"}) {
            boolean old = !meat.equals("rabbit") && !meat.equals("mutton");
            all.add(item(meat + "_chunk", "chunk_" + meat, old ? "nugget" + meat : null));
        }
        all.add(item("iron_cluster", "cluster_iron", "clusteriron"));
        all.add(item("gold_cluster", "cluster_gold", "clustergold"));
        all.add(item("copper_cluster", "cluster_copper", "clustercopper"));
        all.add(item("cinnabar_cluster", "cluster_cinnabar", "clustercinnabar"));

        all.add(shard("air", ModAspects.AIR));
        all.add(shard("fire", ModAspects.FIRE));
        all.add(shard("water", ModAspects.WATER));
        all.add(shard("earth", ModAspects.EARTH));
        all.add(shard("order", ModAspects.ORDER));
        all.add(shard("entropy", ModAspects.ENTROPY));
        all.add(item("flux_shard", "shard_balanced", "shard_balanced").tinted(() -> ModAspects.FLUX.get().color()));
        all.add(item("balanced_shard", "shard_balanced", "shard_balanced"));

        all.add(block("alchemium_block", "thaumium_metal", "thaumiumblock"));
        all.add(block("brass_block", "base_brass", null));
        all.add(block("alchemical_construct", "alchemical_metal", null));
        all.add(block("advanced_alchemical_construct", "advanced_alchemical_metal", null));
        all.add(item("alchemium_ingot", "ingot_thaumium", "thaumiumingot"));
        all.add(item("brass_ingot", "ingot_brass", null));
        all.add(item("alchemium_nugget", "nugget_thaumium", "nuggetthaumium"));
        all.add(item("brass_nugget", "nugget_brass", null));
        all.add(item("alchemium_gear", "thaumium_gear", null));
        all.add(item("brass_gear", "brass_gear", null));
        all.add(item("alchemium_plate", "thaumium_plate", null));
        all.add(item("brass_plate", "brass_plate", null));
        all.add(item("iron_plate", "iron_plate", null));
        all.add(item("salis_mundus", "salis_mundus", "dust"));

        all.add(block("greatwood_log", "log_greatwood", "greatwoodside"));
        all.add(block("greatwood_log_top", "log_greatwood_top", "greatwoodtop"));
        all.add(block("greatwood_planks", "greatwood_plank", "planks_greatwood"));
        all.add(block("greatwood_sapling", "greatwood_sapling", "greatwoodsapling"));
        all.add(block("greatwood_leaves", "greatwood_leaves", "greatwoodleaves").tinted(FOLIAGE));
        all.add(block("silverwood_log", "log_silverwood", "silverwoodside"));
        all.add(block("silverwood_log_top", "log_silverwood_top", "silverwoodtop"));
        all.add(block("silverwood_planks", "silverwood_plank", "planks_silverwood"));
        all.add(block("silverwood_sapling", "silverwood_sapling", "silverwoodsapling"));
        all.add(block("silverwood_leaves", "silverwood_leaves", "silverwoodleaves"));

        all.add(block("shimmerleaf", "shimmerleaf", "shimmerleaf"));
        all.add(block("ethereal_bloom", "bloom", null));
        all.add(LegacyAsset.of("textures/entity/bloom_leaves.png").five("textures/models/bloom_leaves.png"));
        all.add(LegacyAsset.of("textures/entity/bloom_stalk.png").five("textures/models/bloom_stalk.png"));
        all.add(LegacyAsset.of("textures/entity/crystal_capacitor.png").five("textures/models/crystalcapacitor.png"));
        all.add(LegacyAsset.of("textures/entity/bloom_glow.png").five("textures/misc/nodes.png")
                .cropped(0, 6 * NODE_CELL, 32 * NODE_CELL, NODE_CELL));
        all.add(block("cinderpearl", "cinderpearl", "cinderpearl"));
        all.add(block("vishroom", "vishroom", "manashroom"));

        all.add(block("amber_block", "amber_side", "amberblock"));
        all.add(block("amber_block_top", "amber_top", "amberblock"));
        all.add(block("amber_bricks", "amber_brick", "amberbrick"));
        all.add(block("arcane_stone", "arcane_stone_1", null));
        all.add(block("arcane_stone_2", "arcane_stone_2", null));
        all.add(block("arcane_stone_3", "arcane_stone_3", null));
        all.add(block("arcane_stone_bricks", "arcane_brick_stone", "arcane_stone"));
        all.add(block("barrier_stone", "paving_stone_barrier", "paving_stone_warding"));
        all.add(block("travel_stone", "paving_stone_travel", "paving_stone_travel"));
    }

    private static LegacyAsset shard(String name, Supplier<Aspect> aspect) {
        return item(name + "_shard", "shard", "shard").tinted(() -> aspect.get().color());
    }

    private static LegacyAsset item(String target, String five, String four) {
        return texture("item/" + target, "items/", five, four);
    }

    private static LegacyAsset block(String target, String five, String four) {
        return texture("block/" + target, "blocks/", five, four);
    }

    private static LegacyAsset texture(String target, String folder, String five, String four) {
        LegacyAsset asset = LegacyAsset.of("textures/" + target + ".png");
        if (five != null) {
            asset = asset.five("textures/" + folder + five + ".png");
        }
        if (four != null) {
            asset = asset.four("textures/" + folder + four + ".png");
        }
        return asset;
    }
}
