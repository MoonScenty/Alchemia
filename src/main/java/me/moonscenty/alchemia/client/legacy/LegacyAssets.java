package me.moonscenty.alchemia.client.legacy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.registry.ModAspects;

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

    /** The fortress armour, which the original wrote as code rather than as a model file. */
    public static final String FORTRESS_ARMOUR = "fortress_armour";

    /**
     * The void robe's model as the original built it for the robe itself, and as it built it for the hood and the
     * leggings. The constructor hangs different things on the body depending on the size it is given, so the same
     * class is read twice.
     */
    public static final String ROBE = "robe";
    public static final String ROBE_SKIRT = "robe_skirt";

    public static final List<LegacyAsset> ALL = build();

    /**
     * Models the original wrote as code. Each is read out of a constructor by
     * {@link me.moonscenty.alchemia.client.legacy.model.LegacyModelReader}; the first release that has the class
     * wins, as with files.
     */
    public static final Map<String, List<LegacyModelSource>> MODELS = Map.of(
            FORTRESS_ARMOUR, List.of(
                    // the original made it at 1.0 for the helm and cuirass and at 0.5 for the greaves; that only
                    // grows a biped's own boxes, and this model clears every one of them
                    new LegacyModelSource(LegacyEdition.FIVE,
                            "thaumcraft/client/renderers/models/gear/ModelFortressArmor", "(F)V", 1.0F),
                    new LegacyModelSource(LegacyEdition.FOUR,
                            "thaumcraft/client/renderers/models/gear/ModelFortressArmor", "(F)V", 1.0F)),
            ROBE, List.of(
                    new LegacyModelSource(LegacyEdition.FIVE,
                            "thaumcraft/client/renderers/models/gear/ModelRobe", "(F)V", 1.0F),
                    new LegacyModelSource(LegacyEdition.FOUR,
                            "thaumcraft/client/renderers/models/gear/ModelRobe", "(F)V", 1.0F)),
            ROBE_SKIRT, List.of(
                    new LegacyModelSource(LegacyEdition.FIVE,
                            "thaumcraft/client/renderers/models/gear/ModelRobe", "(F)V", 0.5F),
                    new LegacyModelSource(LegacyEdition.FOUR,
                            "thaumcraft/client/renderers/models/gear/ModelRobe", "(F)V", 0.5F)));

    private LegacyAssets() {
    }

    private static List<LegacyAsset> build() {
        List<LegacyAsset> all = new ArrayList<>();
        baseResources(all);
        aspects(all);
        effects(all);
        research(all);
        gear(all);
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

    /** Step 9: armour and its sheets. */
    private static void gear(List<LegacyAsset> all) {
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
    private static void baseResources(List<LegacyAsset> all) {
        // ores and what comes out of them
        all.add(block("amber_ore", "ore_amber", "amberore"));
        all.add(block("cinnabar_ore", "ore_cinnabar", "cinnibar"));
        all.add(item("amber", "amber", "amber"));
        all.add(item("quicksilver", "quicksilver", "quicksilver"));
        all.add(item("quicksilver_drop", "nugget_quicksilver", "nuggetquicksilver"));
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

        // stone. The original's amber block had a separate top and its arcane stone three faces; ours are one
        // picture each, so they take the side and the first face until the models are split
        all.add(block("amber_block", "amber_side", "amberblock"));
        all.add(block("amber_bricks", "amber_brick", "amberbrick"));
        all.add(block("arcane_stone", "arcane_stone_1", null));
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
