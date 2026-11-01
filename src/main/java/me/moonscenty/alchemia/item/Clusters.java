package me.moonscenty.alchemia.item;

import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.Tags;

// ore is recognised by common tags, so raw ore and silk-touched ore blocks both count
public final class Clusters {
    private Clusters() {
    }

    public static ItemStack of(ItemStack drop) {
        Item cluster = clusterFor(drop);
        return cluster == null ? ItemStack.EMPTY : new ItemStack(cluster, drop.getCount());
    }

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
