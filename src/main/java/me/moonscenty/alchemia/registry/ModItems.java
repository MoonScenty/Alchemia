package me.moonscenty.alchemia.registry;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.item.AlumentumItem;
import me.moonscenty.alchemia.item.CrystallizedEssenceItem;
import me.moonscenty.alchemia.item.JarLabelItem;
import me.moonscenty.alchemia.item.PhialItem;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.block.CrystalType;
import me.moonscenty.alchemia.item.AlchemonomiconItem;
import me.moonscenty.alchemia.item.NodePlacerItem;
import me.moonscenty.alchemia.item.ResearchNoteItem;
import me.moonscenty.alchemia.item.AlchemometerItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorItem;
import me.moonscenty.alchemia.item.ModTiers;
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

    /** Creative only: puts a node wherever it is pointed, since there is no other way to carry one yet. */
    public static final DeferredItem<Item> NODE_PLACER = ITEMS.register("node_placer",
            () -> new NodePlacerItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    /** The book everything worked out so far is written into. */
    public static final DeferredItem<Item> ALCHEMONOMICON = ITEMS.register("alchemonomicon",
            () -> new AlchemonomiconItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    /** A wand. What it is made of lives in its components, so there is only ever one item. */
    public static final DeferredItem<Item> WAND = ITEMS.register("wand",
            () -> new WandItem(new Item.Properties()));

    /** The metal ends, as they are before they are worked onto a rod. */
    public static final Map<String, DeferredItem<Item>> WAND_CAPS = registerWandCaps();

    /**
     * The two caps that are cast and then finished, in the state they leave the workbench in.
     * <p>
     * These are not caps. They cannot be put on a wand and there is no {@link me.moonscenty.alchemia.wand.WandCap}
     * for them; they are what goes on the pedestal when the finished cap is infused, and nothing else.
     */
    public static final Map<String, DeferredItem<Item>> INERT_CAPS = registerInertCaps();

    /** The shafts. A plain wooden one is a vanilla stick, so it is not in here. */
    public static final Map<String, DeferredItem<Item>> WAND_RODS = registerWandRods();

    public static final Map<CrystalType, DeferredItem<Item>> SHARDS = registerShards();
    public static final DeferredItem<Item> BALANCED_SHARD = ITEMS.registerSimpleItem("balanced_shard");

    /**
     * Tools and armour of our two metals.
     * <p>
     * Ten shapes and eight pieces, all of them the vanilla item with another metal behind it. The original did
     * the same -- a thaumium pickaxe is a pickaxe -- and anything either of them does beyond that is put on by
     * an altar afterwards rather than built into the item.
     */
    public static final Map<String, DeferredItem<Item>> METAL_TOOLS = registerMetalTools();
    public static final Map<String, DeferredItem<Item>> METAL_ARMOUR = registerMetalArmour();

    /** One point of any essentia, set hard enough to carry. Drawn once in grey and painted by what is in it. */
    public static final DeferredItem<CrystallizedEssenceItem> CRYSTALLIZED_ESSENCE =
            ITEMS.register("crystallized_essence", () -> new CrystallizedEssenceItem(new Item.Properties()));

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
    /** Coal that has been through the crucible: four times the fire in it, and it hurries a smelter along. */
    public static final DeferredItem<AlumentumItem> ALUMENTUM =
            ITEMS.register("alumentum", () -> new AlumentumItem(new Item.Properties()));

    // --- the fittings of the distillery ---------------------------------------------------------------------

    /** A gauze in a wooden frame. What an alembic catches its essentia in, and what a filter tube is sieved with. */
    public static final DeferredItem<Item> FILTER = ITEMS.registerSimpleItem("filter");

    /** Carries eight of one essentia by hand. */
    public static final DeferredItem<Item> PHIAL = ITEMS.register("phial",
            () -> new PhialItem(new Item.Properties()));

    /** Says what belongs in a jar, whether or not any of it is in there. */
    public static final DeferredItem<Item> JAR_LABEL = ITEMS.register("jar_label",
            () -> new JarLabelItem(new Item.Properties()));

    /** Fitted to a jar, it lets the jar be filled but not emptied. */
    public static final DeferredItem<Item> JAR_BRACE = ITEMS.registerSimpleItem("jar_brace");

    // --- void metal -----------------------------------------------------------------------------------------

    /** A wheat seed with the life boiled out of it and something else boiled in. */
    public static final DeferredItem<Item> VOID_SEED = ITEMS.registerSimpleItem("void_seed");

    /** Metal that eats light. What the last of the wand caps is drawn out of. */
    public static final DeferredItem<Item> VOID_INGOT = ITEMS.registerSimpleItem("void_ingot");

    public static final DeferredItem<Item> VOID_NUGGET = ITEMS.registerSimpleItem("void_nugget");

    /**
     * One item to a rod, wearing the same picture the wand wears. The rod sprites were drawn to serve as both, so
     * a rod in the hand looks like the rod on the wand rather than like a swatch of what it is made of.
     */
    private static Map<String, DeferredItem<Item>> registerWandRods() {
        Map<String, DeferredItem<Item>> rods = new java.util.LinkedHashMap<>();
        for (String stuff : new String[] {"greatwood", "silverwood", "reed", "obsidian",
                "blaze", "ice", "quartz", "bone"}) {
            rods.put(stuff, ITEMS.registerSimpleItem("wand_rod_" + stuff));
        }
        return rods;
    }

    /** One item to a cap. */
    private static Map<String, DeferredItem<Item>> registerWandCaps() {
        Map<String, DeferredItem<Item>> caps = new java.util.LinkedHashMap<>();
        for (String metal : new String[] {"iron", "gold", "brass", "alchemium", "void"}) {
            caps.put(metal, ITEMS.registerSimpleItem("wand_cap_" + metal));
        }
        return caps;
    }

    /**
     * The shapes, forged twice.
     * <p>
     * The metals are named here rather than in a field of their own. A field would have to be assigned before
     * this runs, and what order two static fields of one class are assigned in is the order they are written --
     * which is not a thing to hang a mod's startup on.
     */
    private static Map<String, DeferredItem<Item>> registerMetalTools() {
        Map<String, DeferredItem<Item>> made = new java.util.LinkedHashMap<>();
        for (Map.Entry<String, Tier> forging
                : List.of(Map.entry("alchemium", ModTiers.ALCHEMIUM), Map.entry("void", ModTiers.VOID))) {
            String metal = forging.getKey();
            Tier tier = forging.getValue();
            made.put(metal + "_pickaxe", ITEMS.register(metal + "_pickaxe",
                    () -> new PickaxeItem(tier, new Item.Properties().attributes(
                            PickaxeItem.createAttributes(tier, 1.0F, -2.8F)))));
            made.put(metal + "_axe", ITEMS.register(metal + "_axe",
                    () -> new AxeItem(tier, new Item.Properties().attributes(
                            AxeItem.createAttributes(tier, 6.0F, -3.1F)))));
            made.put(metal + "_shovel", ITEMS.register(metal + "_shovel",
                    () -> new ShovelItem(tier, new Item.Properties().attributes(
                            ShovelItem.createAttributes(tier, 1.5F, -3.0F)))));
            made.put(metal + "_sword", ITEMS.register(metal + "_sword",
                    () -> new SwordItem(tier, new Item.Properties().attributes(
                            SwordItem.createAttributes(tier, 3, -2.4F)))));
            made.put(metal + "_hoe", ITEMS.register(metal + "_hoe",
                    () -> new HoeItem(tier, new Item.Properties().attributes(
                            HoeItem.createAttributes(tier, -2.0F, -1.0F)))));
        }
        return Map.copyOf(made);
    }

    private static Map<String, DeferredItem<Item>> registerMetalArmour() {
        Map<String, DeferredItem<Item>> made = new java.util.LinkedHashMap<>();
        Map<String, DeferredHolder<ArmorMaterial, ArmorMaterial>> metals =
                Map.of("alchemium", ModArmorMaterials.ALCHEMIUM, "void", ModArmorMaterials.VOID);
        Map<String, Integer> lasts =
                Map.of("alchemium", ModArmorMaterials.ALCHEMIUM_LASTS, "void", ModArmorMaterials.VOID_LASTS);
        for (String metal : new String[] {"alchemium", "void"}) {
            for (ArmorItem.Type type : new ArmorItem.Type[] {ArmorItem.Type.HELMET, ArmorItem.Type.CHESTPLATE,
                    ArmorItem.Type.LEGGINGS, ArmorItem.Type.BOOTS}) {
                made.put(metal + "_" + type.getName(), ITEMS.register(metal + "_" + type.getName(),
                        () -> new ArmorItem(metals.get(metal), type,
                                new Item.Properties().durability(type.getDurability(lasts.get(metal))))));
            }
        }
        return Map.copyOf(made);
    }

    /** Alchemium and void alone. Iron, gold and brass are finished when they leave the workbench. */
    private static Map<String, DeferredItem<Item>> registerInertCaps() {
        Map<String, DeferredItem<Item>> caps = new java.util.LinkedHashMap<>();
        for (String metal : new String[] {"alchemium", "void"}) {
            caps.put(metal, ITEMS.registerSimpleItem("wand_cap_" + metal + "_inert"));
        }
        return caps;
    }

    private static Map<CrystalType, DeferredItem<Item>> registerShards() {
        Map<CrystalType, DeferredItem<Item>> shards = new EnumMap<>(CrystalType.class);
        for (CrystalType type : CrystalType.values()) {
            shards.put(type, ITEMS.registerSimpleItem(type.getName() + "_shard"));
        }
        return Collections.unmodifiableMap(shards);
    }
}
