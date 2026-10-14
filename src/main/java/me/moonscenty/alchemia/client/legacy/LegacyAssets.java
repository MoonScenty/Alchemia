package me.moonscenty.alchemia.client.legacy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aura.node.NodeType;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.wand.FocusUpgrade;

/**
 * What we take from the original, step by step. Each entry is one of our files; a jar that has it replaces our own
 * picture, and without the jar ours stays. Paths are written without {@code .png}: targets under our
 * {@code textures/}, sources under the original's {@code textures/}.
 *
 * <p>Steps follow {@code docs/PORTING_PLAN.md}. Something we drew because the original had nothing there (the
 * deepslate ores, raw cinnabar) is simply not listed.
 */
public final class LegacyAssets {
    /**
     * The grey the original gave a greatwood leaf in a hand or a slot: {@code ColorizerFoliage.getFoliageColorBasic()}.
     * In the world it took the biome's foliage colour; ours is baked, so every greatwood is the plains green.
     */
    private static final int FOLIAGE = 0x48B518;
    /** A cell of the original's effect sheet, and where its cells start: the game's own inventory layout. */
    private static final int EFFECT_CELL = 18;
    private static final int EFFECT_TOP = 198;
    /** A cell of the original's sheet of node frames. */
    private static final int NODE_CELL = 64;

    /** The fortress armour, which the original wrote as code rather than as a model file. */
    public static final String FORTRESS_ARMOUR = "fortress_armour";

    /**
     * The void robe's model as the original built it for the robe itself, and as it built it for the hood and the
     * leggings. The constructor hangs different things on the body depending on the size it is given, so the same
     * class is read twice.
     */
    public static final String ROBE = "robe";
    public static final String ROBE_SKIRT = "robe_skirt";
    /** The Crimson Cult's plate, which the original built at full size for the chest and at half for the rest. */
    public static final String KNIGHT_ARMOUR = "knight_armour";
    public static final String KNIGHT_ARMOUR_LOWER = "knight_armour_lower";
    /** The eldritch crab, which the original built in code. */
    public static final String ELDRITCH_CRAB = "eldritch_crab";
    /** The banner, which the original built in code. */
    public static final String BANNER = "banner";
    public static final String CUBE = "cube";
    public static final String ELDRITCH_GOLEM = "eldritch_golem";
    public static final String LEADER_ARMOUR = "leader_armour";
    public static final String LEADER_ARMOUR_LOWER = "leader_armour_lower";
    public static final String TAINTACLE_GIANT = "taintacle_giant";
    /** The capstone the eldritch ring and its obelisk wear. */
    public static final String OBELISK_CAP_MESH = "models/legacy/obelisk_cap.obj";
    public static final String CRAB_VENT_MESH = "models/legacy/crabvent.obj";
    /** The eldritch guardian, which the original built in code. */
    public static final String ELDRITCH_GUARDIAN = "eldritch_guardian";

    /**
     * The stone a matrix is eight of, and the glow laid over it while it works: one class, a sixteen-block cube, read
     * once for each half of its sheet.
     */
    public static final String INFUSER = "infuser";
    public static final String INFUSER_GLOW = "infuser_glow";

    /** The two plates, the bag between them and the nozzle: the original's bellows, built in code. */
    public static final String BELLOWS = "bellows";

    /** The inkwell, scroll and ribbon the original stood on its research table. */
    public static final String RESEARCH_TABLE = "research_table";

    /** The pillar the original kept as an OBJ file, served as it is for the matrix to read and draw. */
    public static final String PILLAR_MESH = "models/legacy/pillar.obj";

    /** The eight shards a crystal is grown from, which the original kept as an OBJ file. */
    public static final String CRYSTAL_MESH = "models/legacy/crystal.obj";

    /** The stabiliser's body and its arms, which the original kept as an OBJ file and drew in its renderer. */
    public static final String STABILIZER_MESH = "models/legacy/node_stabilizer.obj";

    /** The vis relay the original drew as its workbench charger, kept as an OBJ file. */
    public static final String RELAY_MESH = "models/legacy/vis_relay.obj";
    /** The crystallizer's body, which the original kept as an OBJ. */
    public static final String CRYSTALLIZER_MESH = "models/legacy/crystallizer.obj";
    /** The thaumatorium, two blocks tall, which the original kept as an OBJ. */
    public static final String THAUMATORIUM_MESH = "models/legacy/thaumatorium.obj";
    /** The centrifuge, which the original built in code. */
    public static final String CENTRIFUGE = "centrifuge";
    /** The brain in a jar, and the jar whose brine it floats in, both built in code. */
    public static final String BRAIN = "brain";
    /** The arcane bore, its emitter, and its base, all built in code. */
    public static final String BORE = "bore";
    public static final String BORE_EMITTER = "bore_emitter";
    public static final String BORE_BASE = "bore_base";
    public static final String JAR = "jar";

    /** The wand: a rod, a cap at either end, and room for a sceptre's crossbar and a focus. */
    public static final String WAND_MESH = "models/legacy/wand.obj";

    /** The autocaster: its bracket, the ball that aims and the focus inside, which the original kept as one OBJ. */
    public static final String TURRET_MESH = "models/legacy/turret.obj";

    /** The alembic, from Thaumcraft 4: its pot, panel, legs and the two tubes it can stand on. */
    public static final String ALEMBIC_MESH = "models/legacy/alembic.obj";

    /** The tube: a middle box, a pipe to each side and a larger box for a buffer. */
    public static final String TUBE_MESH = "models/legacy/tube.obj";

    /** The valve's wheel and stem, which the original built in code and also used for the one-way tube's mark. */
    public static final String VALVE = "valve";
    /** The firebat, which the original built in code: the old bat's shape. */
    public static final String FIREBAT = "firebat";
    /** The pech, which the original built in code on a biped: jaw, pack and all. */
    public static final String PECH = "pech";
    /** The taintacle, which the original built in code as a chain of segments: ten long, and six for a tendril. */
    public static final String TAINTACLE = "taintacle";
    public static final String TAINTACLE_TINY = "taintacle_tiny";

    /** The alchemometer as Thaumcraft 4's thaumometer: a six-sided brass frame. */
    public static final String SCANNER_MESH = "models/legacy/scanner.obj";

    /**
     * What the original called each of the twenty-three: the same word with the underscores taken out, save for
     * the one it spelled wrong.
     */
    private static final Map<FocusUpgrade, String> ORIGINAL_UPGRADE = originalUpgrades();

    public static final List<LegacyAsset> ALL = build();

    /**
     * Models the original wrote as code. Each is read out of a constructor by
     * {@link me.moonscenty.alchemia.client.legacy.model.LegacyModelReader}; the first release that has the class
     * wins, as with files.
     */
    public static final Map<String, List<LegacyModelSource>> MODELS = Map.ofEntries(
            Map.entry(FORTRESS_ARMOUR, List.of(
                    // the original made it at 1.0 for the helm and cuirass and at 0.5 for the greaves; that only
                    // grows a biped's own boxes, and this model clears every one of them
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
        return List.copyOf(all);
    }

    /**
     * Step 2: an icon for every aspect, and the plate and question mark drawn under and instead of one. All grey;
     * the colour is put on in code, ours as the original's. 1.7.10 lacks a few that 1.8.9 added.
     */
    private static void aspects(List<LegacyAsset> all) {
        // the holders know their names before the registry is filled, which is all this needs
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

    /**
     * Step 3: the ailments warp brings on. The original kept every icon in one sheet laid out as the game's own
     * inventory was, eighteen pixels a cell from y 198, and each effect named its cell. Ours are one file each, so
     * each cell is cut out. The cells are the original's; which of our effects is which of its is in the name.
     */
    private static void effects(List<LegacyAsset> all) {
        effect(all, "flux_flu", 3, 1); // flux taint
        effect(all, "flux_phage", 6, 1); // infectious vis exhaust
        effect(all, "unnatural_hunger", 7, 1);
        effect(all, "sun_scorned", 6, 2);
        effect(all, "blurred_vision", 5, 2);
        effect(all, "deadly_gaze", 4, 2); // death gaze
        effect(all, "alchediarrhea", 7, 2); // thaumarhia
        effect(all, "warp_ward", 3, 2);
    }

    private static void effect(List<LegacyAsset> all, String name, int column, int row) {
        all.add(LegacyAsset.of("textures/mob_effect/" + name + ".png")
                .five("textures/misc/potions.png")
                .cropped(column * EFFECT_CELL, EFFECT_TOP + row * EFFECT_CELL, EFFECT_CELL, EFFECT_CELL));
    }

    /**
     * Step 4: what research is done with, and the sky the alchemonomicon's tree hangs in. Only what fits our screens
     * as they are: the panels of the book and the table are laid out differently from the original's and are not
     * taken until the screens are.
     */
    private static void research(List<LegacyAsset> all) {
        // the scanner's lens turns, so its picture is a strip of frames, and the strip's timing comes with it
        all.add(item("alchemometer", "thaumometer", null));
        // and its shape, from Thaumcraft 4: the frame, its sheet, and the glass screen set in it
        all.add(LegacyAsset.of(SCANNER_MESH).four("textures/models/scanner.obj"));
        all.add(LegacyAsset.of("textures/item/legacy/scanner.png").four("textures/models/scanner.png"));
        all.add(LegacyAsset.of("textures/item/legacy/scanscreen.png").four("textures/models/scanscreen.png"));
        all.add(item("alchemonomicon", "thaumonomicon", "thaumonomicon"));
        all.add(item("research_notes", "researchnotes", "researchnotes"));
        all.add(item("scribing_tools", "scribing_tools", null));
        // the letters that drift onto the research table's leather
        all.add(LegacyAsset.of("textures/misc/script.png").five("textures/misc/script.png"));

        // each branch's sky, in the original's order of branches, and the field of stars drawn over every one
        String[] branches = {"basics", "arcana", "alchemy", "artifice", "golemancy", "eldritch"};
        for (int i = 0; i < branches.length; i++) {
            all.add(LegacyAsset.of("textures/gui/research_background/" + branches[i] + ".png")
                    .five("textures/gui/gui_research_back_" + (i + 1) + ".jpg").fromJpeg());
        }
        all.add(LegacyAsset.of("textures/gui/research_overlay.png").five("textures/gui/gui_research_back_over.png"));

        // the research table: the screen is laid out as the original's was, so its panel goes straight in, with the
        // parchment the note is spread on and the two plates its cells are drawn with
        all.add(LegacyAsset.of("textures/gui/research_table.png").five("textures/gui/gui_research_table.png"));
        all.add(LegacyAsset.of("textures/gui/research_parchment.png").five("textures/research/parchment3.png"));
        all.add(LegacyAsset.of("textures/gui/research_hex.png").five("textures/gui/hex1.png"));
        all.add(LegacyAsset.of("textures/gui/research_hex_lit.png").five("textures/gui/hex2.png"));

        // the research table itself: the original's desk is a model file, served as it is with its pictures; the
        // inkwell and the scroll on it it built in code, and the quill is a flat picture stood in the inkwell
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

        // the alchemonomicon is laid out as the original's was: its frame pieces, plates, arrow heads and line pieces
        // all come off the one sheet, and each branch's tab has the original's picture on it
        all.add(LegacyAsset.of("textures/gui/research_browser.png").five("textures/gui/gui_research_browser.png"));
        // a research page: the open book, and the second sheet of pictures each kind of recipe is drawn over
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

    /**
     * Step 5: nodes, and the taint and goo that flux leaves behind.
     * <p>
     * A node is drawn off the original's one great sheet of frames, sixty-four pixels a cell and thirty-two cells a
     * row. Ours are two strips cut from it: the aspect-coloured glow is its first row, the beating halo the first
     * sixteen cells of its twenty-sixth. They were laid out that way to begin with, so the cut is all there is to do.
     */
    private static void aura(List<LegacyAsset> all) {
        // the crystals: the original's mesh of eight shards and the grey it was coloured from
        all.add(LegacyAsset.of(CRYSTAL_MESH).five("models/obj/crystal.obj"));
        all.add(LegacyAsset.of("textures/block/legacy/crystal.png").five("textures/blocks/crystal.png"));
        // a crystal in the hand is the original's grey cluster, coloured by its aspect
        all.add(LegacyAsset.of("textures/item/legacy/crystal_planter.png").five("textures/items/crystal_planter.png"));
        // the stabiliser: its body and arms, their sheet, and the glow drawn over the arms as they work
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
        // what kind of node it is, as a white knot drawn small over the core: the rows under the first, one a kind
        all.add(LegacyAsset.of("textures/entity/node_types.png").five("textures/misc/nodes.png")
                .cropped(0, NODE_CELL, 32 * NODE_CELL, NodeType.values().length * NODE_CELL));
        all.add(LegacyAsset.of("textures/entity/node_bubble.png").five("textures/misc/node_bubble.png"));

        // taint, each with the original's own variations in the same order
        for (int variant = 0; variant < 3; variant++) {
            String ours = variant == 0 ? "" : "_" + variant;
            all.add(block("taint_crust" + ours, "taint_crust_" + variant, null));
            all.add(block("taint_soil" + ours, "taint_soil_" + variant, null));
            all.add(block("taint_log" + ours, "log_taintwood_" + variant, null));
        }
        all.add(block("taint_log_top", "log_taintwood_top", null));
        all.add(block("taint_rock", "taint_rock", null));
        // the fibre is the original's model files: a crust turned onto each side it clings to, and four growths
        for (String part : List.of("taint_fibre", "taint_growth_1", "taint_growth_2", "taint_growth_3",
                "taint_growth_4")) {
            all.add(LegacyAsset.of("models/block/" + part + ".json").five("models/block/" + part + ".json")
                    .asModel("cutout"));
        }
        for (String picture : List.of("taint_fibres", "taint_growth_1", "taint_growth_2", "taint_growth_3",
                "taint_growth_4")) {
            all.add(LegacyAsset.of("textures/block/legacy/" + picture + ".png")
                    .five("textures/blocks/" + picture + ".png"));
        }
        // the original's goo is one strip, still or running; ours asks for two names and gets the same picture
        all.add(block("flux_goo_still", "flux_goo", null));
        all.add(block("flux_goo_flow", "flux_goo", null));
    }

    /**
     * Step 6: wands, their parts, the foci, and the arcane workbench.
     * <p>
     * The original and ours name a part's two pictures the other way round. Its {@code cap_gold} is the picture of a
     * loose cap and {@code cap_gold_mat} the stuff a wand's cap is made of; ours are {@code cap_gold_mat} for the
     * loose cap and {@code cap_gold} for the stuff. So each is taken from the other's name. Our wands keep their own
     * shape and are now dressed in the original's materials.
     */
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
        // the plain wooden rod has no loose picture, only the stuff
        all.add(wandPart("rod_wood", "rod_wood_mat"));

        for (String focus : List.of("builder", "excavation", "fire", "frost", "grapple", "hellbat", "pech", "primal",
                "shard", "shock", "trade")) {
            all.add(wandPart("focus_" + focus, "focus_" + focus));
        }
        all.add(wandPart("focus_hole", "focus_portablehole"));

        // the bench's screen is laid out as the original's was, so its panel goes straight in
        all.add(LegacyAsset.of("textures/gui/arcane_workbench.png").five("textures/gui/gui_arcaneworkbench.png"));
        // the bench itself is the original's model file, which names its pictures where the original kept them
        all.add(LegacyAsset.of("models/block/arcane_workbench.json").five("models/block/arcane_workbench.json")
                .asModel());
        for (String face : List.of("arcane_workbench_top", "arcane_workbench_side")) {
            all.add(LegacyAsset.of("textures/block/legacy/" + face + ".png").five("textures/blocks/" + face + ".png"));
        }
        // the bench a focus is worked on: the original drew it as the workbench with another skin on it,
        // so its model file names ours as a parent once the namespaces are rewritten
        all.add(LegacyAsset.of("models/block/focal_manipulator.json")
                .five("models/block/wand_workbench.json").asModel());
        for (String face : List.of("wand_workbench_top", "wand_workbench_side", "base_stone")) {
            all.add(LegacyAsset.of("textures/block/legacy/" + face + ".png").five("textures/blocks/" + face + ".png"));
        }
        all.add(LegacyAsset.of("textures/gui/focal_manipulator.png").five("textures/gui/gui_wandtable.png"));
        // the two rings the wheel turns behind the foci, one each way
        all.add(LegacyAsset.of("textures/gui/focus_wheel.png").five("textures/misc/radial.png"));
        all.add(LegacyAsset.of("textures/gui/focus_wheel_back.png").five("textures/misc/radial2.png"));
        // one small picture for each of the twenty-three, as the original kept them
        for (FocusUpgrade upgrade : FocusUpgrade.values()) {
            all.add(LegacyAsset.of("textures/gui/focus_upgrade/" + upgrade.getSerializedName() + ".png")
                    .five("textures/foci/" + ORIGINAL_UPGRADE.get(upgrade) + ".png"));
        }

        // the charger above it: the vis relay the original drew it as, and its sheet
        all.add(LegacyAsset.of(RELAY_MESH).five("models/obj/vis_relay.obj"));
        all.add(LegacyAsset.of("textures/entity/vis_relay.png").five("textures/models/vis_relay.png"));
        all.add(LegacyAsset.of("textures/item/legacy/vis_relay.png").five("textures/models/vis_relay.png"));
        // the centrifuge's sheet, and the crystallizer's body and sheet; its crystals are the vis relay's above
        all.add(LegacyAsset.of("textures/entity/centrifuge.png").five("textures/models/centrifuge.png")
                .forModel(CENTRIFUGE));
        all.add(LegacyAsset.of(CRYSTALLIZER_MESH).five("models/obj/crystallizer.obj"));
        // the thaumatorium: its mesh, its sheet and its panel; and the matrix, the original's model file
        all.add(LegacyAsset.of(THAUMATORIUM_MESH).five("models/obj/thaumatorium.obj"));
        // the mirrors: their frames and panes, the hand mirror, its panel, and the two pictures the swirl is made of
        for (String picture : List.of("mirrorframe", "mirrorframe2", "mirrorpane", "mirrorpaneopen", "mirrorpanetrans")) {
            all.add(LegacyAsset.of("textures/block/legacy/" + picture + ".png")
                    .five("textures/blocks/" + picture + ".png"));
        }
        all.add(LegacyAsset.of("textures/item/legacy/hand_mirror.png").five("textures/items/hand_mirror.png"));
        all.add(LegacyAsset.of("textures/gui/hand_mirror.png").five("textures/gui/gui_handmirror.png"));
        all.add(LegacyAsset.of("textures/misc/mirror_tunnel.png").five("textures/misc/tunnel.png"));
        all.add(LegacyAsset.of("textures/misc/mirror_field.png").five("textures/misc/particlefield.png"));
        // the infernal furnace: the original's model of the whole furnace, and its sheet
        all.add(LegacyAsset.of("models/block/infernal_furnace.json").five("models/block/infernal_furnace.json")
                .asModel("cutout"));
        all.add(LegacyAsset.of("textures/block/legacy/infernal_furnace.png")
                .five("textures/blocks/infernal_furnace.png"));
        // the arcane bore: its sheet (the base's too), the jar it wears round its middle, its panel, its two beams
        all.add(LegacyAsset.of("textures/entity/bore.png").five("textures/models/bore.png").forModel(BORE));
        all.add(LegacyAsset.of("textures/entity/jar.png").five("textures/models/jar.png").forModel(JAR));
        all.add(LegacyAsset.of("textures/gui/arcane_bore.png").five("textures/gui/gui_arcanebore.png"));
        all.add(LegacyAsset.of("textures/misc/bore_beam_1.png").five("textures/misc/beam1.png"));
        all.add(LegacyAsset.of("textures/misc/bore_beam_2.png").five("textures/misc/beam2.png"));
        // the brain in a jar: the brain's sheet and the brine's, and its still picture in a hand
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
        // the wand itself, built on the original's mesh from the rod and cap pictures above
        all.add(LegacyAsset.of(WAND_MESH).five("models/obj/wand.obj"));

        // the autocaster: the original's mesh, its sheet, the panel it opens as, and its picture in a hand
        all.add(LegacyAsset.of(TURRET_MESH).five("models/obj/turret.obj"));
        all.add(LegacyAsset.of("textures/entity/autocaster.png").five("textures/models/focus_turret.png"));
        all.add(LegacyAsset.of("textures/entity/eldritch_autocaster.png")
                .five("textures/models/focus_turret_eldritch.png"));
        all.add(LegacyAsset.of("textures/gui/autocaster.png").five("textures/gui/gui_turret_focus.png"));
        all.add(LegacyAsset.of("textures/item/legacy/autocaster.png").five("textures/items/turret_focus.png"));

        // the firebat Nine Hells lets off, in its two skins, both laid out for the original's model
        for (String skin : List.of("firebat", "vampirebat")) {
            all.add(LegacyAsset.of("textures/entity/" + skin + ".png")
                    .five("textures/models/creature/" + skin + ".png").forModel(FIREBAT));
        }

        // the angry zombie's skin, which the furious one wears as well; a zombie's sheet, so the game's model fits it
        all.add(LegacyAsset.of("textures/entity/angry_zombie.png").five("textures/models/creature/bzombie.png")
                .four("textures/models/bzombie.png"));
    }

    /**
     * Step 7: the crucible, the smelter, nitor, and the odds and ends of essentia work, with the jar, the alembic and
     * the tubes on the original's own shapes.
     */
    private static void essentia(List<LegacyAsset> all) {
        // the alembic is Thaumcraft 4's: its mesh and sheet sat with its other models, not under blocks
        all.add(LegacyAsset.of(ALEMBIC_MESH).four("textures/models/alembic.obj"));
        all.add(LegacyAsset.of("textures/entity/alembic.png").four("textures/models/alembic.png"));
        all.add(LegacyAsset.of("textures/item/legacy/alembic.png").four("textures/models/alembic.png"));

        // the jar: the original's glass and lid under each of our jar models, which hold only the liquid and the
        // label the original drew in its renderer
        for (String fill : List.of("", "_1", "_2", "_3", "_4")) {
            for (String kind : List.of("jar", "jar_labelled")) {
                String path = "models/block/jar/" + kind + fill + ".json";
                all.add(LegacyAsset.of(path).five("models/block/jar_normal.json").asModelOver(path));
            }
        }
        // the tubes: the original's mesh, and the one sheet every kind is cut from
        all.add(LegacyAsset.of(TUBE_MESH).five("models/obj/tube.obj"));
        // the valve's wheel and the one-way's mark are drawn off this sheet
        all.add(LegacyAsset.of("textures/entity/valve.png").five("textures/models/valve.png").forModel(VALVE));
        all.add(LegacyAsset.of("textures/block/legacy/tube.png").five("textures/blocks/tube.png"));
        // a tube in the hand is the original's flat picture of it, one for each kind; our item models name them
        for (String tube : List.of("tube_normal", "tube_valve", "tube_oneway", "tube_restrict", "tube_filter",
                "tube_buffer")) {
            all.add(LegacyAsset.of("textures/item/legacy/" + tube + ".png").five("textures/items/" + tube + ".png"));
        }
        for (String face : List.of("jar_side", "jar_top", "jar_bottom", "animatedglow")) {
            all.add(LegacyAsset.of("textures/block/legacy/" + face + ".png").five("textures/blocks/" + face + ".png"));
        }
        // the crucible is the original's model file, which names its pictures where the original kept them
        all.add(LegacyAsset.of("models/block/crucible.json").five("models/block/crucible.json").asModel());
        for (String face : List.of("bottom", "inner", "side", "top")) {
            all.add(LegacyAsset.of("textures/block/legacy/crucible_" + face + ".png")
                    .five("textures/blocks/crucible_" + face + ".png"));
        }
        all.add(block("smelter_front", "smelter_basic_front_off", null));
        all.add(block("smelter_front_on", "smelter_basic_front_on", null));
        all.add(block("smelter_side", "smelter_basic_side", null));
        all.add(block("smelter_top", "smelter_top", null));
        // the two better smelters' faces, under our own names; the alchemium one is the original's thaumium one
        for (String[] tier : new String[][] {{"alchemium_smelter", "thaumium"}, {"void_smelter", "void"}}) {
            all.add(block(tier[0] + "_front", "smelter_" + tier[1] + "_front_off", null));
            all.add(block(tier[0] + "_front_on", "smelter_" + tier[1] + "_front_on", null));
            all.add(block(tier[0] + "_side", "smelter_" + tier[1] + "_side", null));
        }
        all.add(LegacyAsset.of("textures/block/legacy/void_metal.png").five("textures/blocks/void_metal.png"));
        // the pump and the vent are the original's model files, which name their pictures where it kept them
        for (String part : List.of("smelter_aux", "smelter_vent")) {
            all.add(LegacyAsset.of("models/block/" + part + ".json").five("models/block/" + part + ".json")
                    .asModel());
        }
        for (String picture : List.of("smelter_aux", "smelter_aux_pump", "smelter_vent")) {
            all.add(LegacyAsset.of("textures/block/legacy/" + picture + ".png")
                    .five("textures/blocks/" + picture + ".png"));
        }
        // the smelter's screen is laid out as the original's was, so its panel goes straight in
        all.add(LegacyAsset.of("textures/gui/essentia_smelter.png").five("textures/gui/gui_smelter.png"));
        // the flame is grey and dyed in code, the bead is not, as in the original
        all.add(block("nitor", "nitor", null));
        all.add(block("nitor_core", "nitor_core", null));
        // the original's wisp, one cell in the bottom corner of its particle sheet, for the research screen's orbs
        all.add(LegacyAsset.of("textures/particle/mote.png").five("textures/misc/particles.png")
                .cropped(0, 224, 32, 32));
        // the original's two particle sheets whole, for the effects drawn straight off them as it drew them
        all.add(LegacyAsset.of("textures/misc/legacy_particles.png").five("textures/misc/particles.png"));
        all.add(LegacyAsset.of("textures/misc/legacy_particles2.png").five("textures/misc/particles2.png"));

        all.add(item("alumentum", "alumentum", null));
        // grey, and painted by whatever it holds
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

    /**
     * Step 8: the altar. The two stones laid under its corners, and the pedestal, whose model now asks for a side and
     * a top as the original's did. The matrix and the pillars come with the original's model and mesh they are drawn
     * on, since their pictures are laid out for those and not for ours.
     */
    private static void infusion(List<LegacyAsset> all) {
        all.add(block("infusion_cost_stone", "matrix_cost", null));
        all.add(block("infusion_speed_stone", "matrix_speed", null));
        // the pedestal is the original's model file, which names its pictures where the original kept them
        all.add(LegacyAsset.of("models/block/pedestal.json").five("models/block/pedestal_normal.json").asModel());
        for (String face : List.of("pedestal_side", "pedestal_top")) {
            all.add(LegacyAsset.of("textures/block/legacy/" + face + ".png").five("textures/blocks/" + face + ".png"));
        }

        // the matrix's stones and the pillars, drawn by the matrix on the original's own model and mesh
        all.add(LegacyAsset.of("textures/item/legacy/infuser.png").five("textures/blocks/infuser_normal.png"));
        all.add(LegacyAsset.of("textures/entity/infusion_matrix.png").five("textures/blocks/infuser_normal.png")
                .forModel(INFUSER));
        all.add(LegacyAsset.of(PILLAR_MESH).five("models/obj/pillar.obj"));
        all.add(LegacyAsset.of("textures/entity/arcane_pillar.png").five("textures/blocks/pillar_normal.png"));

        // 8C: the altar built of ancient or eldritch stone, its pillars, its matrix, its pedestals
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

        // the eldritch ring: its capstone mesh, its pictures, the way to the Outer Lands, the cult's thread
        all.add(LegacyAsset.of(OBELISK_CAP_MESH).five("models/obj/obelisk_cap.obj"));
        for (String picture : List.of("obelisk_cap", "obelisk_cap_2", "obelisk_side", "obelisk_side_2")) {
            all.add(LegacyAsset.of("textures/entity/" + picture + ".png").five("textures/models/" + picture + ".png"));
        }
        all.add(LegacyAsset.of("textures/entity/eldritch_portal.png").five("textures/misc/eldritch_portal.png"));
        all.add(LegacyAsset.of("textures/misc/wispy.png").five("textures/misc/wispy.png"));
        // banners: the cloth on the original's model, and the three layers of the one in the hand
        all.add(LegacyAsset.of("textures/entity/banner_cultist.png").five("textures/models/banner_cultist.png")
                .forModel(BANNER));
        all.add(LegacyAsset.of("textures/entity/banner_blank.png").five("textures/models/banner_blank.png")
                .forModel(BANNER));
        for (String layer : List.of("banner_stand", "banner", "banner_symbol", "banner_cultist")) {
            all.add(item(layer, layer, null));
        }

        // the primordial pearl, which turns through sixteen frames, and the three bags of loot
        all.add(item("primordial_pearl", "primordial_pearl", null));
        all.add(item("loot_bag_common", "loot_bag_common", "lootbag"));
        all.add(item("loot_bag_uncommon", "loot_bag_uncommon", "lootbagunc"));
        all.add(item("loot_bag_rare", "loot_bag_rare", "lootbagrare"));

        // the Outer Lands' stone
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
        // the mound's urns and crates, a side for each rarity, and the urn breaking
        for (String kind : List.of("loot_urn", "loot_crate")) {
            all.add(block(kind + "_top", kind + "_top", null));
            for (int rarity = 0; rarity <= 2; rarity++) {
                all.add(block(kind + "_side_" + rarity, kind + "_side_" + rarity, null));
            }
        }
        all.add(LegacyAsset.of("sounds/legacy/urnbreak.ogg").five("sounds/urnbreak.ogg"));
        // the Outer Lands: their bedrock with the stars in it, the lock and its arms, the crab vent, the tablet,
        // and their sounds
        all.add(LegacyAsset.of("textures/block/legacy/ancient_bedrock.png").five("textures/misc/particlefield32.png"));
        all.add(LegacyAsset.of("textures/entity/eldritch_lock.png").five("textures/models/eldritch_lock.png"));
        all.add(LegacyAsset.of("textures/entity/eldritch_cube.png").five("textures/models/eldritch_cube.png"));
        all.add(LegacyAsset.of("textures/entity/crabvent.png").five("textures/models/crabvent.png"));
        all.add(LegacyAsset.of(CRAB_VENT_MESH).five("models/obj/crabvent.obj"));
        all.add(item("runed_tablet", "runed_tablet", null));
        all.add(LegacyAsset.of("textures/block/legacy/liquid_death.png").five("textures/blocks/animatedglow.png"));
        all.add(LegacyAsset.of("textures/item/legacy/liquid_death_bucket.png").five("textures/items/bucket_death.png"));
        for (String sound : List.of("wind1", "wind2", "pump1", "pump2", "pump3", "evilportal")) {
            all.add(LegacyAsset.of("sounds/legacy/" + sound + ".ogg").five("sounds/" + sound + ".ogg"));
        }
        all.add(LegacyAsset.of("sounds/legacy/runicshieldcharge.ogg").five("sounds/runicShieldCharge.ogg"));
        // the great halls' bosses: the Warden in the guardian's model, the Construct in its own, the Praetor's plate
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

    /** Step 9: armour and its sheets. */
    private static void gear(List<LegacyAsset> all) {
        // the two metals' tools and plain armour: the original called alchemium thaumium
        metalGear(all, "alchemium", "thaumium");
        metalGear(all, "void", "void");
        // the five elemental tools and the crusher, whose picture is animated and brings its timing with it
        for (String[] tool : new String[][] {{"elemental_axe", "elemental_axe"},
                {"elemental_pickaxe", "elemental_pick"}, {"elemental_sword", "elemental_sword"},
                {"elemental_shovel", "elemental_shovel"}, {"elemental_hoe", "elemental_hoe"},
                {"primal_crusher", "primal_crusher"}}) {
            all.add(LegacyAsset.of("textures/item/legacy/" + tool[0] + ".png")
                    .five("textures/items/" + tool[1] + ".png"));
        }
        all.add(item("enchanted_fabric", "fabric", null));
        // the parts devices are built from, and the bellows' sheet
        for (String[] part : new String[][] {{"primal_charm", "primal_charm"}, {"mirrored_glass", "mirrored_glass"},
                {"morphic_resonator", "morphic_resonator"}, {"clockwork_mind", "mind_clockwork"},
                {"bioalchemic_mind", "mind_biothaumic"}}) {
            all.add(LegacyAsset.of("textures/item/legacy/" + part[0] + ".png").five("textures/items/" + part[1] + ".png"));
        }
        all.add(LegacyAsset.of("textures/entity/bellows.png").five("textures/models/bellows.png"));
        // the lamps and the jug are the original's model files; the chest is the original's sheet on the old chest
        for (String model : List.of("lamp", "lamp_stand", "water_jug")) {
            all.add(LegacyAsset.of("models/block/legacy/" + model + ".json")
                    .five("models/block/" + model + ".json").asModel());
        }
        for (String face : List.of("lamp_on", "lamp_off", "lamp_grow_on", "lamp_grow_off", "lamp_fert_on",
                "lamp_fert_off", "base_metal", "water_jug")) {
            all.add(LegacyAsset.of("textures/block/legacy/" + face + ".png").five("textures/blocks/" + face + ".png"));
        }
        all.add(LegacyAsset.of("textures/entity/hungry_chest.png").five("textures/models/chesthungry.png"));
        // the levitator and the ear are the original's model files, the dark one built over the lit one, so they keep
        // the original's names; the relay's names vanilla pictures by their old paths, so ours is built over it
        for (String model : List.of("levitator_on", "levitator_off", "arcane_ear_on", "arcane_ear_off")) {
            all.add(LegacyAsset.of("models/block/" + model + ".json")
                    .five("models/block/" + model + ".json").asModel("cutout"));
        }
        for (String model : List.of("redstone_relay_on", "redstone_relay_off")) {
            all.add(LegacyAsset.of("models/block/legacy/" + model + ".json")
                    .five("models/block/" + model + ".json").asModel("cutout"));
        }
        // the recharge pedestal and the dioptra are the original's model files, the dark dioptra built over the lit
        // one, so it keeps the original's name; the dioptra's foot names arcane stone where the original kept it
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
        // the totem and its pole are the original's model files; each kind is ours, built over them with its own
        // face, as the original's block states did it
        for (String model : List.of("aura_totem", "aura_totem_pole")) {
            all.add(LegacyAsset.of("models/block/legacy/" + model + ".json").five("models/block/" + model + ".json")
                    .asModel());
        }
        for (String face : List.of("aura_totem_push", "aura_totem_pull", "aura_totem_top", "aura_totem_pole_inner",
                "aura_totem_pole_outer", "aura_totem_pole_pure")) {
            all.add(LegacyAsset.of("textures/block/legacy/" + face + ".png").five("textures/blocks/" + face + ".png"));
        }
        all.add(LegacyAsset.of("textures/gui/aura_totem.png").five("textures/gui/gui_auratotem.png"));
        // the pech's three skins, laid out for the original's model, its trading panel, and what it hoards
        for (String skin : List.of("pech_forage", "pech_thaum", "pech_stalker")) {
            all.add(LegacyAsset.of("textures/entity/" + skin + ".png")
                    .five("textures/models/creature/" + skin + ".png").forModel(PECH));
        }
        all.add(LegacyAsset.of("textures/gui/pech.png").five("textures/gui/gui_pech.png"));
        all.add(LegacyAsset.of("textures/item/legacy/coin.png").five("textures/items/coin.png"));
        all.add(LegacyAsset.of("textures/item/legacy/knowledge_fragment.png")
                .five("textures/items/knowledge_fragment.png"));
        // the creatures of the taint: the crawler's skin for the game's silverfish, the taintacle's for the original's
        // model, what they leave, and the geyser that lets up swarms
        all.add(LegacyAsset.of("textures/entity/taint_crawler.png").five("textures/models/creature/crawler.png"));
        all.add(LegacyAsset.of("textures/entity/taintacle.png").five("textures/models/creature/taintacle.png")
                .forModel(TAINTACLE));
        all.add(LegacyAsset.of("textures/item/legacy/tainted_goo.png").five("textures/items/taint_slime.png"));
        // the animals it makes over, in the original's skins for the game's models, and its slime
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
        // the wisp's essence, and the original's recordings, which sounds.json names
        all.add(LegacyAsset.of("textures/item/legacy/wispy_essence.png").five("textures/items/wispy_essence.png"));
        for (String sound : List.of("wisplive1", "wisplive2", "wisplive3", "wispdead", "zap1", "zap2", "ice1", "ice2",
                "ice3", "pech_idle1", "pech_idle2", "pech_idle3", "pech_hit1", "pech_hit2", "pech_death",
                "pech_charge1", "pech_charge2", "pech_trade", "pech_dice", "coins")) {
            all.add(LegacyAsset.of("sounds/legacy/" + sound + ".ogg").five("sounds/" + sound + ".ogg"));
        }
        // the spa's faces and panel; the salts (which shimmer) and the bucket of what they make
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
        // the pouch opened is the original's panel; its screen and slots are laid out to match
        all.add(LegacyAsset.of("textures/gui/focus_pouch.png").five("textures/gui/gui_focuspouch.png")
                .four("textures/gui/gui_focuspouch.png"));

        // the boots and the goggles were plain armour in the original, one sheet each; the goggles' is twice the size
        all.add(item("traveller_boots", "traveller_boots", "bootstraveler"));
        all.add(item("goggles_of_revealing", "goggles_revealing", "gogglesrevealing"));
        all.add(LegacyAsset.of("textures/models/armor/traveller_boots_layer_1.png")
                .five("textures/models/armor/bootstraveler.png").four("textures/models/bootstraveler.png"));
        all.add(LegacyAsset.of("textures/models/armor/goggles_layer_1.png")
                .five("textures/models/armor/goggles.png").four("textures/models/goggles.png"));

        // fortress armour is one sheet for every piece; the game asks for it twice, as the outer and inner layer
        for (String layer : List.of("fortress_layer_1", "fortress_layer_2")) {
            all.add(LegacyAsset.of("textures/models/armor/" + layer + ".png")
                    .five("textures/models/armor/fortress_armor.png")
                    .four("textures/models/fortress_armor.png")
                    .forModel(FORTRESS_ARMOUR));
        }
        all.add(item("fortress_helm", "fortress_helm", "thaumiumfortresshelm"));
        all.add(item("fortress_chest", "fortress_chest", "thaumiumfortresschest"));
        all.add(item("fortress_legs", "fortress_legs", "thaumiumfortresslegs"));

        // the cloth robe is plain armour: a sheet that takes the dye and a trim over it that does not
        robeSheets(all, "cloth", 1, "robes_1", "robes_1_overlay", null);
        robeSheets(all, "cloth", 2, "robes_2", "robes_2_overlay", null);
        // the void robe's sheet is the same for both layers. The original named its two halves the other way
        // round: the file called "overlay" is the cloth that takes the dye, and the plain one is the trim
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

    /**
     * A metal's five tools, four pieces of armour and the two sheets it is worn with. 1.8.9 wrote the names apart
     * ({@code thaumium_pick}), 1.7.10 ran them together ({@code thaumiumpick}).
     */
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

    /**
     * One layer of a robe: the dyed cloth and its trim. 1.8.9 kept armour sheets under {@code models/armor/},
     * 1.7.10 straight under {@code models/}.
     */
    private static void robeSheets(List<LegacyAsset> all, String robe, int layer, String cloth, String trim,
            String model) {
        LegacyAsset dyed = LegacyAsset.of("textures/models/armor/" + robe + "_layer_" + layer + ".png")
                .five("textures/models/armor/" + cloth + ".png").four("textures/models/" + cloth + ".png");
        LegacyAsset over = LegacyAsset.of("textures/models/armor/" + robe + "_layer_" + layer + "_overlay.png")
                .five("textures/models/armor/" + trim + ".png").four("textures/models/" + trim + ".png");
        all.add(model == null ? dyed : dyed.forModel(model));
        all.add(model == null ? over : over.forModel(model));
    }

    /** Step 1: ores, metals, the two woods, the three plants, stone. */
    /**
     * Golems and their press: the meshes a golem is put together from and the pictures they are drawn in, the press
     * and its panel, the little pictures of every part and trait on that panel, the items, and the stone table.
     */
    /** Step 13B: the Crimson Cult, what it wears and carries, its portal and its sounds. */
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
        // the husk's own stained plate, and the crab that lives in it
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
        // each material's picture; alchemium is the original's thaumium
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
        // the stone table is the original's wooden table model in stone
        all.add(LegacyAsset.of("models/block/table_wood.json").five("models/block/table_wood.json").asModel());
        all.add(LegacyAsset.of("models/block/table_stone.json").five("models/block/table_stone.json").asModel());
        for (String face : List.of("table_stone_top", "table_stone_side", "table_wood_top", "table_wood_side")) {
            all.add(LegacyAsset.of("textures/block/legacy/" + face + ".png").five("textures/blocks/" + face + ".png"));
        }
        // seals, which shimmer and bring their timing with them, the tallow they are made with, and the ring and
        // corners drawn round one that is stuck up
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
        // ores and what comes out of them
        all.add(block("amber_ore", "ore_amber", "amberore"));
        all.add(block("cinnabar_ore", "ore_cinnabar", "cinnibar"));
        all.add(item("amber", "amber", "amber"));
        all.add(item("quicksilver", "quicksilver", "quicksilver"));
        all.add(item("quicksilver_drop", "nugget_quicksilver", "nuggetquicksilver"));
        all.add(item("zombie_brain", "brain", "brain"));
        all.add(item("iron_cluster", "cluster_iron", "clusteriron"));
        all.add(item("gold_cluster", "cluster_gold", "clustergold"));
        all.add(item("copper_cluster", "cluster_copper", "clustercopper"));
        all.add(item("cinnabar_cluster", "cluster_cinnabar", "clustercinnabar"));

        // the shards are one grey picture the original coloured with the shard's aspect
        all.add(shard("air", ModAspects.AIR));
        all.add(shard("fire", ModAspects.FIRE));
        all.add(shard("water", ModAspects.WATER));
        all.add(shard("earth", ModAspects.EARTH));
        all.add(shard("order", ModAspects.ORDER));
        all.add(shard("entropy", ModAspects.ENTROPY));
        // flux is the balanced shard's picture in flux's colour, not the plain shard's
        all.add(item("flux_shard", "shard_balanced", "shard_balanced").tinted(() -> ModAspects.FLUX.get().color()));
        all.add(item("balanced_shard", "shard_balanced", "shard_balanced"));

        // metals
        all.add(block("alchemium_block", "thaumium_metal", "thaumiumblock"));
        // the original called its brass block alchemical metal
        all.add(block("brass_block", "alchemical_metal", null));
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

        // the two woods
        all.add(block("greatwood_log", "log_greatwood", "greatwoodside"));
        all.add(block("greatwood_log_top", "log_greatwood_top", "greatwoodtop"));
        all.add(block("greatwood_planks", "greatwood_plank", "planks_greatwood"));
        all.add(block("greatwood_sapling", "greatwood_sapling", "greatwoodsapling"));
        all.add(block("greatwood_leaves", "greatwood_leaves", "greatwoodleaves").tinted(FOLIAGE));
        all.add(block("silverwood_log", "log_silverwood", "silverwoodside"));
        all.add(block("silverwood_log_top", "log_silverwood_top", "silverwoodtop"));
        all.add(block("silverwood_planks", "silverwood_plank", "planks_silverwood"));
        all.add(block("silverwood_sapling", "silverwood_sapling", "silverwoodsapling"));
        // silverwood leaves were never tinted (the original returned white for them)
        all.add(block("silverwood_leaves", "silverwood_leaves", "silverwoodleaves"));

        // the three plants
        all.add(block("shimmerleaf", "shimmerleaf", "shimmerleaf"));
        all.add(block("cinderpearl", "cinderpearl", "cinderpearl"));
        all.add(block("vishroom", "vishroom", "manashroom"));

        // stone. The amber block has its own top, and the arcane stone three faces, laid as the original laid them
        all.add(block("amber_block", "amber_side", "amberblock"));
        all.add(block("amber_block_top", "amber_top", "amberblock"));
        all.add(block("amber_bricks", "amber_brick", "amberbrick"));
        all.add(block("arcane_stone", "arcane_stone_1", null));
        all.add(block("arcane_stone_2", "arcane_stone_2", null));
        all.add(block("arcane_stone_3", "arcane_stone_3", null));
        all.add(block("arcane_stone_bricks", "arcane_brick_stone", "arcane_stone"));
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

    /** {@code four} may be null: the 1.7.10 release did not have everything 1.8.9 did. */
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
