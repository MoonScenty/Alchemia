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

    /** The fortress armour, which the original wrote as code rather than as a model file. */
    public static final String FORTRESS_ARMOUR = "fortress_armour";

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
                            "thaumcraft/client/renderers/models/gear/ModelFortressArmor", "(F)V", 1.0F)));

    private LegacyAssets() {
    }

    private static List<LegacyAsset> build() {
        List<LegacyAsset> all = new ArrayList<>();
        baseResources(all);
        gear(all);
        return List.copyOf(all);
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
