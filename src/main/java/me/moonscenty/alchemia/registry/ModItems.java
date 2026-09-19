package me.moonscenty.alchemia.registry;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.CrystalType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Alchemia.MODID);

    public static final DeferredItem<Item> AMBER = ITEMS.registerSimpleItem("amber");
    public static final DeferredItem<Item> QUICKSILVER = ITEMS.registerSimpleItem("quicksilver");
    public static final DeferredItem<Item> RAW_CINNABAR = ITEMS.registerSimpleItem("raw_cinnabar");

    public static final Map<CrystalType, DeferredItem<Item>> SHARDS = registerShards();
    public static final DeferredItem<Item> BALANCED_SHARD = ITEMS.registerSimpleItem("balanced_shard");

    // Purified ore clusters, each smelts into two of its metal
    public static final DeferredItem<Item> IRON_CLUSTER = ITEMS.registerSimpleItem("iron_cluster");
    public static final DeferredItem<Item> GOLD_CLUSTER = ITEMS.registerSimpleItem("gold_cluster");
    public static final DeferredItem<Item> COPPER_CLUSTER = ITEMS.registerSimpleItem("copper_cluster");
    public static final DeferredItem<Item> CINNABAR_CLUSTER = ITEMS.registerSimpleItem("cinnabar_cluster");

    private static Map<CrystalType, DeferredItem<Item>> registerShards() {
        Map<CrystalType, DeferredItem<Item>> shards = new EnumMap<>(CrystalType.class);
        for (CrystalType type : CrystalType.values()) {
            shards.put(type, ITEMS.registerSimpleItem(type.getName() + "_shard"));
        }
        return Collections.unmodifiableMap(shards);
    }
}
