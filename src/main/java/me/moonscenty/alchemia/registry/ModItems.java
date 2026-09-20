package me.moonscenty.alchemia.registry;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.CrystalType;
import me.moonscenty.alchemia.item.AlchemonomiconItem;
import me.moonscenty.alchemia.item.ResearchNoteItem;
import me.moonscenty.alchemia.item.AlchemometerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Alchemia.MODID);

    public static final DeferredItem<Item> AMBER = ITEMS.registerSimpleItem("amber");
    public static final DeferredItem<Item> QUICKSILVER = ITEMS.registerSimpleItem("quicksilver");
    public static final DeferredItem<Item> RAW_CINNABAR = ITEMS.registerSimpleItem("raw_cinnabar");

    public static final DeferredItem<Item> ALCHEMOMETER = ITEMS.register("alchemometer",
            () -> new AlchemometerItem(new Item.Properties().stacksTo(1)));

    /** Quill and ink. Its damage is how much ink is left, spent a point at a time while working on a note. */
    public static final DeferredItem<Item> SCRIBING_TOOLS = ITEMS.register("scribing_tools",
            () -> new Item(new Item.Properties().stacksTo(1).durability(64)));
    /** A sheet of vellum with a research puzzle part-drawn on it. */
    public static final DeferredItem<Item> RESEARCH_NOTES = ITEMS.register("research_notes",
            () -> new ResearchNoteItem(new Item.Properties().stacksTo(1)));

    /** The book everything worked out so far is written into. */
    public static final DeferredItem<Item> ALCHEMONOMICON = ITEMS.register("alchemonomicon",
            () -> new AlchemonomiconItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final Map<CrystalType, DeferredItem<Item>> SHARDS = registerShards();
    public static final DeferredItem<Item> BALANCED_SHARD = ITEMS.registerSimpleItem("balanced_shard");

    // Purified ore clusters, each smelts into two of its metal
    public static final DeferredItem<Item> IRON_CLUSTER = ITEMS.registerSimpleItem("iron_cluster");
    public static final DeferredItem<Item> GOLD_CLUSTER = ITEMS.registerSimpleItem("gold_cluster");
    public static final DeferredItem<Item> COPPER_CLUSTER = ITEMS.registerSimpleItem("copper_cluster");
    public static final DeferredItem<Item> CINNABAR_CLUSTER = ITEMS.registerSimpleItem("cinnabar_cluster");

    public static final DeferredItem<Item> ALCHEMIUM_INGOT = ITEMS.registerSimpleItem("alchemium_ingot");
    public static final DeferredItem<Item> BRASS_INGOT = ITEMS.registerSimpleItem("brass_ingot");
    public static final DeferredItem<Item> ALCHEMIUM_NUGGET = ITEMS.registerSimpleItem("alchemium_nugget");
    public static final DeferredItem<Item> BRASS_NUGGET = ITEMS.registerSimpleItem("brass_nugget");
    public static final DeferredItem<Item> QUICKSILVER_DROP = ITEMS.registerSimpleItem("quicksilver_drop");
    public static final DeferredItem<Item> ALCHEMIUM_GEAR = ITEMS.registerSimpleItem("alchemium_gear");
    public static final DeferredItem<Item> BRASS_GEAR = ITEMS.registerSimpleItem("brass_gear");
    public static final DeferredItem<Item> ALCHEMIUM_PLATE = ITEMS.registerSimpleItem("alchemium_plate");
    public static final DeferredItem<Item> BRASS_PLATE = ITEMS.registerSimpleItem("brass_plate");
    public static final DeferredItem<Item> IRON_PLATE = ITEMS.registerSimpleItem("iron_plate");
    public static final DeferredItem<Item> SALIS_MUNDUS = ITEMS.registerSimpleItem("salis_mundus");

    private static Map<CrystalType, DeferredItem<Item>> registerShards() {
        Map<CrystalType, DeferredItem<Item>> shards = new EnumMap<>(CrystalType.class);
        for (CrystalType type : CrystalType.values()) {
            shards.put(type, ITEMS.registerSimpleItem(type.getName() + "_shard"));
        }
        return Collections.unmodifiableMap(shards);
    }
}
