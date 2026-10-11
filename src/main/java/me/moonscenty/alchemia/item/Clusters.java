package me.moonscenty.alchemia.item;

import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.Tags;

/**
 * What a piece of ore comes up as when it is mined well enough: a cluster, which smelts to twice the metal.
 *
 * <p>Three things in the mod turn ore into clusters -- a pickaxe worked for refining, the pickaxe of the core and
 * the crusher that carry it, and an excavation focus that has learned to dowse. All three ask here.
 *
 * <p>What counts as ore is read from the common tags rather than listed, so raw iron dug up by anybody's pickaxe
 * is raw iron, and an ore block taken up whole with silk touch counts as much as what it would have broken into.
 * The original kept the same table keyed by item, for the same reason.
 */
public final class Clusters {
    private Clusters() {
    }

    /**
     * The cluster this drop could have been, or nothing when it is not ore.
     * <p>
     * As many clusters as there were pieces: a stack of two raw copper is two copper clusters, not one.
     */
    public static ItemStack of(ItemStack drop) {
        Item cluster = clusterFor(drop);
        return cluster == null ? ItemStack.EMPTY : new ItemStack(cluster, drop.getCount());
    }

    /**
     * The drop, or the cluster in its place if the chance comes up.
     * <p>
     * A drop that is not ore is handed back untouched whatever the chance; nothing is spent on it.
     */
    public static ItemStack maybe(ItemStack drop, float chance, RandomSource random) {
        ItemStack cluster = of(drop);
        return cluster.isEmpty() || random.nextFloat() > chance ? drop : cluster;
    }

    private static Item clusterFor(ItemStack drop) {
        if (either(drop, Tags.Items.RAW_MATERIALS_IRON, Tags.Items.ORES_IRON)) {
            return ModItems.IRON_CLUSTER.get();
        }
        if (either(drop, Tags.Items.RAW_MATERIALS_GOLD, Tags.Items.ORES_GOLD)) {
            return ModItems.GOLD_CLUSTER.get();
        }
        if (either(drop, Tags.Items.RAW_MATERIALS_COPPER, Tags.Items.ORES_COPPER)) {
            return ModItems.COPPER_CLUSTER.get();
        }
        if (either(drop, ModTags.Items.RAW_MATERIALS_CINNABAR, ModTags.Items.ORES_CINNABAR)) {
            return ModItems.CINNABAR_CLUSTER.get();
        }
        return null;
    }

    private static boolean either(ItemStack drop, TagKey<Item> raw, TagKey<Item> ore) {
        return drop.is(raw) || drop.is(ore);
    }
}
