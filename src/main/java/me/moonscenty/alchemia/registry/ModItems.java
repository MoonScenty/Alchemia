package me.moonscenty.alchemia.registry;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import me.moonscenty.alchemia.Alchemia;
import java.util.function.Supplier;

import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.item.AlumentumItem;
import me.moonscenty.alchemia.item.FocusItem;
import me.moonscenty.alchemia.item.FocusPouchItem;
import me.moonscenty.alchemia.item.FortressArmorItem;
import me.moonscenty.alchemia.item.RobeItem;
import me.moonscenty.alchemia.wand.Focus;
import me.moonscenty.alchemia.wand.spell.Excavation;
import me.moonscenty.alchemia.wand.spell.Shock;
import me.moonscenty.alchemia.wand.spell.Hellbat;
import me.moonscenty.alchemia.wand.spell.Hole;
import me.moonscenty.alchemia.wand.spell.Swapping;
import me.moonscenty.alchemia.wand.spell.Thrown;
import me.moonscenty.alchemia.item.GogglesItem;
import me.moonscenty.alchemia.item.TravellerBootsItem;
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

    /**
     * The twelve foci, which are what a wand is pointed at something for.
     * <p>
     * What each one does when it goes off is not here yet. What is here is the part that is the same for all
     * twelve and that the rest will be built on: the price, the wait, and whether an autocaster will take it.
     */
    public static final Map<String, DeferredItem<FocusItem>> FOCI = registerFoci();

    /** Eighteen squares for foci, carried as one item. A wand changing its focus looks in here too. */
    public static final DeferredItem<FocusPouchItem> FOCUS_POUCH = ITEMS.register("focus_pouch",
            () -> new FocusPouchItem(new Item.Properties()));

    /** A turret folded up: a focus, a bracket and enough mind to point one at something. */
    public static final DeferredItem<Item> AUTOCASTER = ITEMS.register("autocaster",
            () -> new me.moonscenty.alchemia.item.AutocasterItem(new Item.Properties().stacksTo(1)));

    public static final Map<CrystalType, DeferredItem<Item>> SHARDS = registerShards();
    /** Taint in a bottle, to be thrown. */
    public static final DeferredItem<me.moonscenty.alchemia.item.TaintBottleItem> BOTTLE_TAINT = ITEMS.register(
            "bottle_taint", () -> new me.moonscenty.alchemia.item.TaintBottleItem(new Item.Properties().stacksTo(8)));
    /** The bow of bone: quick to draw, and its arrows fly faster and hit harder. */
    public static final DeferredItem<me.moonscenty.alchemia.item.BoneBowItem> BONE_BOW = ITEMS.register("bone_bow",
            () -> new me.moonscenty.alchemia.item.BoneBowItem(new Item.Properties().durability(512)));
    /** The six primal arrows, one for each primal. */
    public static final Map<CrystalType, DeferredItem<me.moonscenty.alchemia.item.PrimalArrowItem>> PRIMAL_ARROWS =
            registerPrimalArrows();
    public static final DeferredItem<Item> BALANCED_SHARD = ITEMS.registerSimpleItem("balanced_shard");

    /**
     * Tools and armour of our two metals.
     * <p>
     * Ten shapes and eight pieces, all of them the vanilla item with another metal behind it. The original did
     * the same -- a thaumium pickaxe is a pickaxe -- and anything either of them does beyond that is put on by
     * an altar afterwards rather than built into the item.
     */
    public static final Map<String, DeferredItem<Item>> METAL_TOOLS = registerMetalTools();

    /**
     * The five elemental tools: alchemium with a primal bound into each, and the workings that element is in a
     * tool already on them. Water carries, fire refines, air sweeps, earth moves earth; order and entropy
     * together make things grow.
     */
    public static final Map<String, DeferredItem<Item>> ELEMENTAL_TOOLS = registerElementalTools();

    /** Both elemental diggers and both void ones, beaten into one tool that only breaks things. */
    public static final DeferredItem<Item> PRIMAL_CRUSHER = ITEMS.register("primal_crusher",
            () -> new me.moonscenty.alchemia.item.tool.PrimalCrusherItem(ModTiers.PRIMAL,
                    me.moonscenty.alchemia.item.tool.ElementalTools.carrying(new Item.Properties().attributes(
                                    net.minecraft.world.item.DiggerItem.createAttributes(ModTiers.PRIMAL, 3.5F, -2.8F)),
                            me.moonscenty.alchemia.enchantment.InfusionEnchantment.DESTRUCTIVE, 1,
                            me.moonscenty.alchemia.enchantment.InfusionEnchantment.REFINING, 1)));
    public static final Map<String, DeferredItem<Item>> METAL_ARMOUR = registerMetalArmour();

    /**
     * The two robes.
     * <p>
     * Not a suit each: the cloth robe has no hood and the void robe has no boots, exactly as the original had
     * them. Between them they wear every mesh that was drawn, which is why neither is short of anything.
     * <p>
     * What they are worth is the discount. Cloth takes two parts in a hundred off a wand's bill at the chest and
     * the legs and one at the feet; the void robe takes five at each of its three.
     */
    public static final Map<String, DeferredItem<RobeItem>> ROBES = registerRobes();

    /**
     * Fortress armour: a helm, a cuirass and greaves, as the original had. There are no boots.
     */
    public static final Map<String, DeferredItem<FortressArmorItem>> FORTRESS = registerFortress();

    /** Brass and glass on the face, drawn as a model. Wearing them is what makes an aura node visible. */
    public static final DeferredItem<GogglesItem> GOGGLES = ITEMS.register("goggles_of_revealing",
            () -> new GogglesItem(ModArmorMaterials.GOGGLES, new Item.Properties()));

    /** Leather boots with a band round them, drawn as a model rather than stretched over the leg. */
    public static final DeferredItem<TravellerBootsItem> TRAVELLER_BOOTS = ITEMS.register("traveller_boots",
            () -> new TravellerBootsItem(ModArmorMaterials.TRAVELLER, new Item.Properties()));

    /** One point of any essentia, set hard enough to carry. Drawn once in grey and painted by what is in it. */
    public static final DeferredItem<CrystallizedEssenceItem> CRYSTALLIZED_ESSENCE =
            ITEMS.register("crystallized_essence", () -> new CrystallizedEssenceItem(new Item.Properties()));

    // Purified ore clusters, each smelts into two of its metal
    public static final DeferredItem<Item> IRON_CLUSTER = ITEMS.registerSimpleItem("iron_cluster");
    public static final DeferredItem<Item> GOLD_CLUSTER = ITEMS.registerSimpleItem("gold_cluster");
    public static final DeferredItem<Item> COPPER_CLUSTER = ITEMS.registerSimpleItem("copper_cluster");
    public static final DeferredItem<Item> CINNABAR_CLUSTER = ITEMS.registerSimpleItem("cinnabar_cluster");

    // --- parts the devices are built from ------------------------------------------------------------------

    /** All six primals and the balance between them, worked round a ring of gold. The root of artifice. */
    public static final DeferredItem<Item> PRIMAL_CHARM = ITEMS.register("primal_charm",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
    /** What an angry zombie still had. Food of a sort, and warp. */
    public static final DeferredItem<me.moonscenty.alchemia.item.ZombieBrainItem> ZOMBIE_BRAIN =
            ITEMS.register("zombie_brain", () -> new me.moonscenty.alchemia.item.ZombieBrainItem(new Item.Properties()));
    /** The original's eggs, all pink with the mob's own colour for the spots. */
    public static final DeferredItem<net.neoforged.neoforge.common.DeferredSpawnEggItem> ANGRY_ZOMBIE_SPAWN_EGG =
            ITEMS.register("angry_zombie_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    ModEntities.ANGRY_ZOMBIE, 0xFFC0FF, 0x008000, new Item.Properties()));
    public static final DeferredItem<net.neoforged.neoforge.common.DeferredSpawnEggItem> FURIOUS_ZOMBIE_SPAWN_EGG =
            ITEMS.register("furious_zombie_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    ModEntities.FURIOUS_ZOMBIE, 0xFFC0FF, 0x004000, new Item.Properties()));
    public static final DeferredItem<net.neoforged.neoforge.common.DeferredSpawnEggItem> FIREBAT_SPAWN_EGG =
            ITEMS.register("firebat_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    ModEntities.FIRE_BAT, 0xFFC0FF, 0xF00000, new Item.Properties()));
    public static final DeferredItem<net.neoforged.neoforge.common.DeferredSpawnEggItem> WISP_SPAWN_EGG =
            ITEMS.register("wisp_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    ModEntities.WISP, 0xFFC0FF, 0xFFFFFF, new Item.Properties()));
    public static final DeferredItem<net.neoforged.neoforge.common.DeferredSpawnEggItem> PECH_SPAWN_EGG =
            ITEMS.register("pech_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    ModEntities.PECH, 0xFFC0FF, 0x400040, new Item.Properties()));
    public static final DeferredItem<net.neoforged.neoforge.common.DeferredSpawnEggItem> TAINT_CRAWLER_SPAWN_EGG =
            ITEMS.register("taint_crawler_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    ModEntities.TAINT_CRAWLER, 0xA206A2, 0x303030, new Item.Properties()));
    public static final DeferredItem<net.neoforged.neoforge.common.DeferredSpawnEggItem> TAINT_SWARM_SPAWN_EGG =
            ITEMS.register("taint_swarm_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    ModEntities.TAINT_SWARM, 0xA206A2, 0xFF8080, new Item.Properties()));
    public static final DeferredItem<net.neoforged.neoforge.common.DeferredSpawnEggItem> TAINTACLE_SPAWN_EGG =
            ITEMS.register("taintacle_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    ModEntities.TAINTACLE, 0xA206A2, 0x443344, new Item.Properties()));
    public static final DeferredItem<net.neoforged.neoforge.common.DeferredSpawnEggItem> TAINTED_COW_SPAWN_EGG =
            ITEMS.register("tainted_cow_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    ModEntities.TAINTED_COW, 0xA206A2, 0x7E3B3B, new Item.Properties()));
    public static final DeferredItem<net.neoforged.neoforge.common.DeferredSpawnEggItem> TAINTED_PIG_SPAWN_EGG =
            ITEMS.register("tainted_pig_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    ModEntities.TAINTED_PIG, 0xA206A2, 0xEF98AF, new Item.Properties()));
    public static final DeferredItem<net.neoforged.neoforge.common.DeferredSpawnEggItem> TAINTED_CHICKEN_SPAWN_EGG =
            ITEMS.register("tainted_chicken_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    ModEntities.TAINTED_CHICKEN, 0xA206A2, 0xC0C0C0, new Item.Properties()));
    public static final DeferredItem<net.neoforged.neoforge.common.DeferredSpawnEggItem> TAINTED_SHEEP_SPAWN_EGG =
            ITEMS.register("tainted_sheep_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    ModEntities.TAINTED_SHEEP, 0xA206A2, 0x808080, new Item.Properties()));
    public static final DeferredItem<net.neoforged.neoforge.common.DeferredSpawnEggItem> TAINTED_CREEPER_SPAWN_EGG =
            ITEMS.register("tainted_creeper_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    ModEntities.TAINTED_CREEPER, 0xA206A2, 0x00FF00, new Item.Properties()));
    public static final DeferredItem<net.neoforged.neoforge.common.DeferredSpawnEggItem> TAINTED_VILLAGER_SPAWN_EGG =
            ITEMS.register("tainted_villager_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    ModEntities.TAINTED_VILLAGER, 0xA206A2, 0x00FFFF, new Item.Properties()));
    public static final DeferredItem<net.neoforged.neoforge.common.DeferredSpawnEggItem> TAINTED_RABBIT_SPAWN_EGG =
            ITEMS.register("tainted_rabbit_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    ModEntities.TAINTED_RABBIT, 0xA206A2, 0xF0C0C0, new Item.Properties()));
    public static final DeferredItem<net.neoforged.neoforge.common.DeferredSpawnEggItem> ALCHEMIC_SLIME_SPAWN_EGG =
            ITEMS.register("alchemic_slime_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    ModEntities.ALCHEMIC_SLIME, 0xA206A2, 0xFF80FF, new Item.Properties()));
    /** What the creatures of the taint are made of: its goo, and a tendril of it. */
    public static final DeferredItem<Item> TAINTED_GOO = ITEMS.registerSimpleItem("tainted_goo");
    public static final DeferredItem<Item> TAINT_TENDRIL = ITEMS.registerSimpleItem("taint_tendril");
    /** A gold coin, of the kind pechs hoard. */
    public static final DeferredItem<Item> COIN = ITEMS.registerSimpleItem("coin");
    /** A scrap of learning, worth a little experience. */
    public static final DeferredItem<me.moonscenty.alchemia.item.KnowledgeFragmentItem> KNOWLEDGE_FRAGMENT =
            ITEMS.register("knowledge_fragment",
                    () -> new me.moonscenty.alchemia.item.KnowledgeFragmentItem(new Item.Properties()));
    /** What is left of a wisp: two points of its aspect. */
    public static final DeferredItem<me.moonscenty.alchemia.item.WispyEssenceItem> WISPY_ESSENCE = ITEMS.register(
            "wispy_essence", () -> new me.moonscenty.alchemia.item.WispyEssenceItem(new Item.Properties()));
    /** The Crimson Cult's robes, plate and boots, in the original's names. Never made, only taken off a cultist. */
    public static final Map<String, DeferredItem<me.moonscenty.alchemia.item.CultistArmorItem>> CRIMSON =
            registerCrimson();
    /** The cult's own sword, which saps whatever it cuts. */
    public static final DeferredItem<me.moonscenty.alchemia.item.CrimsonBladeItem> CRIMSON_BLADE =
            ITEMS.register("crimson_blade", () -> new me.moonscenty.alchemia.item.CrimsonBladeItem(ModTiers.CRIMSON,
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC).attributes(
                            SwordItem.createAttributes(ModTiers.CRIMSON, 3, -2.4F))));
    /** A book of the cult's, which teaches what it is about and leaves its mark. */
    public static final DeferredItem<me.moonscenty.alchemia.item.CrimsonRitesItem> CRIMSON_RITES =
            ITEMS.register("crimson_rites", () -> new me.moonscenty.alchemia.item.CrimsonRitesItem(
                    new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.RARE)));
    public static final DeferredItem<net.neoforged.neoforge.common.DeferredSpawnEggItem> CULTIST_KNIGHT_SPAWN_EGG =
            ITEMS.register("cultist_knight_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    ModEntities.CULTIST_KNIGHT, 0x900608, 0x000080, new Item.Properties()));
    public static final DeferredItem<net.neoforged.neoforge.common.DeferredSpawnEggItem> CULTIST_CLERIC_SPAWN_EGG =
            ITEMS.register("cultist_cleric_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    ModEntities.CULTIST_CLERIC, 0x900608, 0x800000, new Item.Properties()));
    public static final DeferredItem<net.neoforged.neoforge.common.DeferredSpawnEggItem> CULTIST_PORTAL_SPAWN_EGG =
            ITEMS.register("cultist_portal_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    ModEntities.CULTIST_PORTAL, 0x900608, 0x6060D2, new Item.Properties()));
    public static final DeferredItem<net.neoforged.neoforge.common.DeferredSpawnEggItem> ELDRITCH_CRAB_SPAWN_EGG =
            ITEMS.register("eldritch_crab_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    ModEntities.ELDRITCH_CRAB, 0x808080, 0x550000, new Item.Properties()));
    public static final DeferredItem<net.neoforged.neoforge.common.DeferredSpawnEggItem> INHABITED_ZOMBIE_SPAWN_EGG =
            ITEMS.register("inhabited_zombie_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    ModEntities.INHABITED_ZOMBIE, 0x808080, 0x550000, new Item.Properties()));
    /** A banner of any of sixteen colours, and the Crimson Cult's. */
    public static final DeferredItem<me.moonscenty.alchemia.block.BannerItem> BANNER = ITEMS.register("banner",
            () -> new me.moonscenty.alchemia.block.BannerItem(new Item.Properties().stacksTo(16), false));
    public static final DeferredItem<me.moonscenty.alchemia.block.BannerItem> CRIMSON_BANNER = ITEMS.register(
            "crimson_banner", () -> new me.moonscenty.alchemia.block.BannerItem(new Item.Properties().stacksTo(16),
                    true));
    /** A pearl of every primal at once, which only the Outer Lands' greatest things and the rarest bags give up. */
    public static final DeferredItem<Item> PRIMORDIAL_PEARL = ITEMS.register("primordial_pearl",
            () -> new Item(new Item.Properties().fireResistant()));
    /** Bags of loot, common, uncommon and rare. */
    public static final DeferredItem<me.moonscenty.alchemia.item.LootBagItem> LOOT_BAG_COMMON = ITEMS.register(
            "loot_bag_common", () -> new me.moonscenty.alchemia.item.LootBagItem(new Item.Properties(), 0));
    public static final DeferredItem<me.moonscenty.alchemia.item.LootBagItem> LOOT_BAG_UNCOMMON = ITEMS.register(
            "loot_bag_uncommon", () -> new me.moonscenty.alchemia.item.LootBagItem(
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON), 1));
    public static final DeferredItem<me.moonscenty.alchemia.item.LootBagItem> LOOT_BAG_RARE = ITEMS.register(
            "loot_bag_rare", () -> new me.moonscenty.alchemia.item.LootBagItem(
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE), 2));
    /** The key to a great hall of the Outer Lands. */
    public static final DeferredItem<me.moonscenty.alchemia.item.RunedTabletItem> RUNED_TABLET = ITEMS.register(
            "runed_tablet", () -> new me.moonscenty.alchemia.item.RunedTabletItem(new Item.Properties().stacksTo(1)
                    .rarity(net.minecraft.world.item.Rarity.RARE)));
    public static final DeferredItem<net.minecraft.world.item.BucketItem> LIQUID_DEATH_BUCKET = ITEMS.register(
            "liquid_death_bucket", () -> new net.minecraft.world.item.BucketItem(ModFluids.DEATH.get(),
                    new Item.Properties().craftRemainder(net.minecraft.world.item.Items.BUCKET).stacksTo(1)
                            .rarity(net.minecraft.world.item.Rarity.RARE)));
    // --- baubles, worn in Curios' slots ------------------------------------------------------------------------
    /** The plain amulet, ring and belt the rest are worked from, and their finer kin. */
    public static final DeferredItem<me.moonscenty.alchemia.item.bauble.BaubleItem> MUNDANE_AMULET = bauble("mundane_amulet");
    public static final DeferredItem<me.moonscenty.alchemia.item.bauble.BaubleItem> MUNDANE_RING = bauble("mundane_ring");
    public static final DeferredItem<me.moonscenty.alchemia.item.bauble.BaubleItem> MUNDANE_BELT = bauble("mundane_belt");
    public static final DeferredItem<me.moonscenty.alchemia.item.bauble.BaubleItem> FANCY_AMULET = bauble("fancy_amulet");
    public static final DeferredItem<me.moonscenty.alchemia.item.bauble.BaubleItem> FANCY_RING = bauble("fancy_ring");
    public static final DeferredItem<me.moonscenty.alchemia.item.bauble.BaubleItem> FANCY_BELT = bauble("fancy_belt");
    /** An apprentice's ring of each primal: found, never made. */
    public static final Map<String, DeferredItem<me.moonscenty.alchemia.item.bauble.ApprenticeRingItem>> APPRENTICE_RINGS = registerApprenticeRings();
    /** Runic shielding: amulets, rings and girdles, and the specialised ones. */
    public static final DeferredItem<me.moonscenty.alchemia.item.bauble.BaubleItem> RUNIC_AMULET = runic("runic_amulet", 8,
            me.moonscenty.alchemia.item.bauble.RunicShield.Upgrade.NONE, Rarity.RARE);
    public static final DeferredItem<me.moonscenty.alchemia.item.bauble.BaubleItem> EMERGENCY_RUNIC_AMULET = runic("emergency_runic_amulet", 7,
            me.moonscenty.alchemia.item.bauble.RunicShield.Upgrade.EMERGENCY, Rarity.RARE);
    public static final DeferredItem<me.moonscenty.alchemia.item.bauble.BaubleItem> PROTECTION_RING = runic("protection_ring", 1,
            me.moonscenty.alchemia.item.bauble.RunicShield.Upgrade.NONE, Rarity.UNCOMMON);
    public static final DeferredItem<me.moonscenty.alchemia.item.bauble.BaubleItem> RUNIC_RING = runic("runic_ring", 5,
            me.moonscenty.alchemia.item.bauble.RunicShield.Upgrade.NONE, Rarity.RARE);
    public static final DeferredItem<me.moonscenty.alchemia.item.bauble.BaubleItem> CHARGED_RUNIC_RING = runic("charged_runic_ring", 4,
            me.moonscenty.alchemia.item.bauble.RunicShield.Upgrade.CHARGED, Rarity.RARE);
    public static final DeferredItem<me.moonscenty.alchemia.item.bauble.BaubleItem> REVITALIZING_RUNIC_RING = runic("revitalizing_runic_ring", 4,
            me.moonscenty.alchemia.item.bauble.RunicShield.Upgrade.HEALING, Rarity.RARE);
    public static final DeferredItem<me.moonscenty.alchemia.item.bauble.BaubleItem> RUNIC_GIRDLE = runic("runic_girdle", 10,
            me.moonscenty.alchemia.item.bauble.RunicShield.Upgrade.NONE, Rarity.RARE);
    public static final DeferredItem<me.moonscenty.alchemia.item.bauble.BaubleItem> KINETIC_RUNIC_GIRDLE = runic("kinetic_runic_girdle", 9,
            me.moonscenty.alchemia.item.bauble.RunicShield.Upgrade.KINETIC, Rarity.RARE);
    /** The vis stone that is found, and the amulet of vis that is made: they charge what is carried. */
    public static final DeferredItem<me.moonscenty.alchemia.item.bauble.VisAmuletItem> VIS_STONE = ITEMS.register("vis_stone",
            () -> new me.moonscenty.alchemia.item.bauble.VisAmuletItem(new Item.Properties().rarity(Rarity.UNCOMMON), 40, true));
    public static final DeferredItem<me.moonscenty.alchemia.item.bauble.VisAmuletItem> VIS_AMULET = ITEMS.register("vis_amulet",
            () -> new me.moonscenty.alchemia.item.bauble.VisAmuletItem(new Item.Properties().rarity(Rarity.RARE), 5, false));
    /** The thaumostatic girdle, that softens every fall. */
    public static final DeferredItem<me.moonscenty.alchemia.item.bauble.HoverGirdleItem> THAUMOSTATIC_GIRDLE = ITEMS.register("thaumostatic_girdle",
            () -> new me.moonscenty.alchemia.item.bauble.HoverGirdleItem(new Item.Properties().rarity(Rarity.RARE)));
    /** The verdant heart band, and what it may be worked into. */
    public static final DeferredItem<me.moonscenty.alchemia.item.bauble.VerdantHeartItem> VERDANT_HEART = ITEMS.register("verdant_heart",
            () -> new me.moonscenty.alchemia.item.bauble.VerdantHeartItem(new Item.Properties().rarity(Rarity.RARE)));
    /** Three meats and sugar: food, and the sustainer's heart. */
    public static final DeferredItem<Item> TRIPLE_MEAT_TREAT = ITEMS.register("triple_meat_treat",
            () -> new Item(new Item.Properties().food(new net.minecraft.world.food.FoodProperties.Builder()
                    .nutrition(6).saturationModifier(0.8F).alwaysEdible()
                    .effect(() -> new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.REGENERATION, 100, 0), 0.66F)
                    .build())));

    private static DeferredItem<me.moonscenty.alchemia.item.bauble.BaubleItem> bauble(String name) {
        return ITEMS.register(name, () -> new me.moonscenty.alchemia.item.bauble.BaubleItem(new Item.Properties()));
    }

    private static DeferredItem<me.moonscenty.alchemia.item.bauble.BaubleItem> runic(String name, int charge, me.moonscenty.alchemia.item.bauble.RunicShield.Upgrade upgrade,
            Rarity rarity) {
        return ITEMS.register(name, () -> new me.moonscenty.alchemia.item.bauble.BaubleItem(new Item.Properties().rarity(rarity), charge, upgrade));
    }

    private static Map<String, DeferredItem<me.moonscenty.alchemia.item.bauble.ApprenticeRingItem>> registerApprenticeRings() {
        Map<String, DeferredItem<me.moonscenty.alchemia.item.bauble.ApprenticeRingItem>> made = new java.util.LinkedHashMap<>();
        Map<String, java.util.function.Supplier<net.minecraft.core.Holder<me.moonscenty.alchemia.aspect.Aspect>>> primals =
                new java.util.LinkedHashMap<>();
        primals.put("air", () -> ModAspects.AIR);
        primals.put("earth", () -> ModAspects.EARTH);
        primals.put("fire", () -> ModAspects.FIRE);
        primals.put("water", () -> ModAspects.WATER);
        primals.put("order", () -> ModAspects.ORDER);
        primals.put("entropy", () -> ModAspects.ENTROPY);
        primals.forEach((name, primal) -> made.put(name, ITEMS.register("apprentice_ring_" + name,
                () -> new me.moonscenty.alchemia.item.bauble.ApprenticeRingItem(new Item.Properties().rarity(Rarity.UNCOMMON), primal))));
        // unmodifiableMap rather than Map.copyOf, for the reason written above registerMetalTools
        return Collections.unmodifiableMap(made);
    }

    /** A guardian's eye, that does not close. */
    public static final DeferredItem<Item> ELDRITCH_EYE = ITEMS.register("eldritch_eye",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
    public static final DeferredItem<net.neoforged.neoforge.common.DeferredSpawnEggItem> ELDRITCH_GUARDIAN_SPAWN_EGG =
            ITEMS.register("eldritch_guardian_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    ModEntities.ELDRITCH_GUARDIAN, 0x808080, 0x000000, new Item.Properties()));
    public static final DeferredItem<net.neoforged.neoforge.common.DeferredSpawnEggItem> MIND_SPIDER_SPAWN_EGG =
            ITEMS.register("mind_spider_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    ModEntities.MIND_SPIDER, 0x4C3E30, 0x444444, new Item.Properties()));
    /** A pane of glass silvered with quicksilver. */
    public static final DeferredItem<Item> MIRRORED_GLASS = ITEMS.registerSimpleItem("mirrored_glass");
    /** A mirror on a handle, that sends things through to a magic mirror wherever it is. */
    public static final DeferredItem<me.moonscenty.alchemia.item.HandMirrorItem> HAND_MIRROR =
            ITEMS.register("hand_mirror", () -> new me.moonscenty.alchemia.item.HandMirrorItem(
                    new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
    /** Glass and brass round a shard of flux, which takes whatever shape is pressed on it. */
    public static final DeferredItem<Item> MORPHIC_RESONATOR = ITEMS.registerSimpleItem("morphic_resonator");
    /** A mind of gears and a comparator under glass. Enough to follow one rule without being told twice. */
    public static final DeferredItem<Item> CLOCKWORK_MIND = ITEMS.registerSimpleItem("clockwork_mind");
    /** The clockwork mind with a brain grown into it, which can be told to choose. */
    public static final DeferredItem<Item> BIOALCHEMIC_MIND = ITEMS.registerSimpleItem("bioalchemic_mind");

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
    /** Soap that washes the mind: temporary warp, and sometimes a point of the warp that sticks. */
    public static final DeferredItem<me.moonscenty.alchemia.item.SanitySoapItem> SANITY_SOAP = ITEMS.register(
            "sanity_soap", () -> new me.moonscenty.alchemia.item.SanitySoapItem(new Item.Properties()));
    /** Flint that lights when it is looked through towards an eldritch portal. */
    public static final DeferredItem<me.moonscenty.alchemia.item.SinisterStoneItem> SINISTER_STONE = ITEMS.register(
            "sinister_stone", () -> new me.moonscenty.alchemia.item.SinisterStoneItem(new Item.Properties()
                    .stacksTo(1).rarity(net.minecraft.world.item.Rarity.RARE)));
    /** Salts that turn still water they dissolve in to purifying fluid. */
    public static final DeferredItem<me.moonscenty.alchemia.item.BathSaltsItem> BATH_SALTS = ITEMS.register(
            "bath_salts", () -> new me.moonscenty.alchemia.item.BathSaltsItem(new Item.Properties()));
    public static final DeferredItem<net.minecraft.world.item.BucketItem> PURIFYING_FLUID_BUCKET = ITEMS.register(
            "purifying_fluid_bucket", () -> new net.minecraft.world.item.BucketItem(ModFluids.PURIFYING.get(),
                    new Item.Properties().craftRemainder(net.minecraft.world.item.Items.BUCKET).stacksTo(1)
                            .rarity(net.minecraft.world.item.Rarity.RARE)));

    /**
     * Cloth that has been through a workbench rather than a loom. Everything soft in this craft is cut from it.
     * <p>
     * Registered as {@code enchanted_fabric} where the original said {@code fabric}, which is a word this game's
     * world has since given to something else entirely. What it is called on the tooltip is the original's.
     */
    public static final DeferredItem<Item> ENCHANTED_FABRIC = ITEMS.registerSimpleItem("enchanted_fabric");
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

    /** Void metal beaten flat. What the void robes are sewn onto. */
    public static final DeferredItem<Item> VOID_PLATE = ITEMS.registerSimpleItem("void_plate");
    /** The void metal gear, as brass and alchemium have theirs: what a void golem works on. */
    public static final DeferredItem<Item> VOID_GEAR = ITEMS.registerSimpleItem("void_gear");

    /** A golem off the press, or taken back up with a wand: what it is rides on the item. */
    public static final DeferredItem<me.moonscenty.alchemia.item.GolemItem> GOLEM = ITEMS.register("golem",
            () -> new me.moonscenty.alchemia.item.GolemItem(new Item.Properties()));
    /** The golemancer's bell, for telling golems to follow or to stay. */
    public static final DeferredItem<me.moonscenty.alchemia.item.GolemBellItem> GOLEM_BELL = ITEMS.register(
            "golem_bell", () -> new me.moonscenty.alchemia.item.GolemBellItem(new Item.Properties().stacksTo(1)));
    /** Eyes for a golem's head, that see further. */
    public static final DeferredItem<Item> VISION_MODULE = ITEMS.registerSimpleItem("vision_module");
    /** What gives a golem the will to fight. */
    public static final DeferredItem<Item> AGGRESSION_MODULE = ITEMS.registerSimpleItem("aggression_module");

    /** Tallow rendered from flesh in a crucible: what seals are stuck on with. */
    public static final DeferredItem<Item> TALLOW = ITEMS.registerSimpleItem("tallow");
    /** A seal not yet made into anything. */
    public static final DeferredItem<me.moonscenty.alchemia.item.SealItem> SEAL_BLANK = ITEMS.register("seal_blank",
            () -> new me.moonscenty.alchemia.item.SealItem(null, new Item.Properties()));
    /** Every kind of control seal, by its name. */
    public static final Map<String, DeferredItem<me.moonscenty.alchemia.item.SealItem>> SEALS = registerSeals();

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
    /**
     * The twelve foci, each with the numbers the original gave it.
     * <p>
     * The kinds are written as suppliers rather than made here and now: the aspects a price is quoted in are
     * registry entries, and this class is read before the registries are filled.
     */
    private static Map<String, DeferredItem<FocusItem>> registerFoci() {
        Map<String, Supplier<Focus>> kinds = new java.util.LinkedHashMap<>();
        // held down rather than let off, and charged for every tick of it
        kinds.put("fire", () -> Focus.of(0xE55104, AspectList.of(ModAspects.FIRE, 2))
                .heldDown().inTurrets(12.0F, 0.0F).casts(Thrown::fire));
        kinds.put("frost", () -> Focus.of(0x4F69CC, AspectList.of(ModAspects.WATER, 2)
                .add(ModAspects.FIRE, 1).add(ModAspects.ENTROPY, 2))
                .cooldown(200).inTurrets(32.0F, 15.0F).casts(Thrown::frost));
        kinds.put("shock", () -> Focus.of(0x9FB3BF, AspectList.of(ModAspects.AIR, 8))
                .cooldown(250).inTurrets(20.0F, 0.0F).casts(Shock::cast));
        // a penny a block, for as long as the button is down
        kinds.put("excavation", () -> Focus.of(0x064006, AspectList.of(ModAspects.EARTH, 1))
                .heldDown().casts(Excavation::cast));
        kinds.put("grapple", () -> Focus.of(0x1515FF, AspectList.of(ModAspects.WATER, 10)
                .add(ModAspects.AIR, 10)).cooldown(250).casts(Thrown::grapple));
        kinds.put("hellbat", () -> Focus.of(0xDC3602, AspectList.of(ModAspects.FIRE, 10)
                .add(ModAspects.ENTROPY, 5).add(ModAspects.AIR, 5)).cooldown(1000).inTurrets().casts(Hellbat::cast));
        kinds.put("pech", () -> Focus.of(0x229944, AspectList.of(ModAspects.EARTH, 2)
                .add(ModAspects.ENTROPY, 2).add(ModAspects.WATER, 2))
                .cooldown(250).inTurrets(32.0F, 12.0F).casts(Thrown::pech));
        // the dearest of them by a long way, and the only one that takes a wall out of the world
        kinds.put("hole", () -> Focus.of(0x091429, AspectList.of(ModAspects.ENTROPY, 25)
                .add(ModAspects.AIR, 25).add(ModAspects.EARTH, 25)).casts(Hole::cast));
        kinds.put("primal", () -> Focus.of(0xA5A1C1, primalCost())
                .cooldown(500).inTurrets().casts(Thrown::primal));
        kinds.put("shard", () -> Focus.of(0x9929BD, AspectList.of(ModAspects.FIRE, 1)
                .add(ModAspects.ENTROPY, 1).add(ModAspects.AIR, 1))
                .cooldown(300).inTurrets().casts(Thrown::visShard));
        kinds.put("trade", () -> Focus.of(0x857B93, AspectList.of(ModAspects.ENTROPY, 1)
                .add(ModAspects.EARTH, 1).add(ModAspects.ORDER, 1)).casts(Swapping::trade));
        kinds.put("builder", () -> Focus.of(0x85EB93, AspectList.of(ModAspects.AIR, 1)
                .add(ModAspects.EARTH, 1).add(ModAspects.ORDER, 1)).casts(Swapping::builder));

        Map<String, DeferredItem<FocusItem>> made = new java.util.LinkedHashMap<>();
        kinds.forEach((name, kind) -> made.put(name, ITEMS.register("focus_" + name,
                () -> new FocusItem(kind.get(), new Item.Properties()))));
        // unmodifiableMap rather than Map.copyOf, for the reason written above registerMetalTools
        return Collections.unmodifiableMap(made);
    }

    /**
     * Ten of each of the six.
     * <p>
     * The original asked ten plus a random even number up to eight, redrawn every fifth of a second, so the price
     * shimmered on the tooltip and the wand paid a different bill each time. That is put on when the focus is asked
     * for its price ({@code FocusItem.primalCost}); this is what it stands at for anything that reads it unasked.
     * <p>
     * The six are named one by one rather than asked for with {@code ModAspects.primals()}: that reads the aspect
     * registry, and this runs while the item registry is being filled, when the aspect registry is still empty.
     * A holder can be pointed at before it holds anything; a list of them cannot be counted.
     */
    private static AspectList primalCost() {
        return AspectList.of(ModAspects.AIR, 10)
                .add(ModAspects.EARTH, 10)
                .add(ModAspects.FIRE, 10)
                .add(ModAspects.WATER, 10)
                .add(ModAspects.ORDER, 10)
                .add(ModAspects.ENTROPY, 10);
    }

    /**
     * The six robe pieces, in the shape the original gave each robe.
     * <p>
     * The key is the item's own name, so that what is registered and what is asked for cannot drift apart.
     */
    private static Map<String, DeferredItem<RobeItem>> registerRobes() {
        Map<String, DeferredItem<RobeItem>> made = new java.util.LinkedHashMap<>();
        robe(made, "cloth_chest", ArmorItem.Type.CHESTPLATE, ModArmorMaterials.CLOTH,
                ModArmorMaterials.CLOTH_LASTS, 2, false);
        robe(made, "cloth_legs", ArmorItem.Type.LEGGINGS, ModArmorMaterials.CLOTH,
                ModArmorMaterials.CLOTH_LASTS, 2, false);
        robe(made, "cloth_boots", ArmorItem.Type.BOOTS, ModArmorMaterials.CLOTH,
                ModArmorMaterials.CLOTH_LASTS, 1, false);
        robe(made, "void_robe_helm", ArmorItem.Type.HELMET, ModArmorMaterials.VOID_ROBE,
                ModArmorMaterials.VOID_ROBE_LASTS, 5, true);
        robe(made, "void_robe_chest", ArmorItem.Type.CHESTPLATE, ModArmorMaterials.VOID_ROBE,
                ModArmorMaterials.VOID_ROBE_LASTS, 5, true);
        robe(made, "void_robe_legs", ArmorItem.Type.LEGGINGS, ModArmorMaterials.VOID_ROBE,
                ModArmorMaterials.VOID_ROBE_LASTS, 5, true);
        // unmodifiableMap rather than Map.copyOf, for the reason written above registerMetalTools
        return Collections.unmodifiableMap(made);
    }

    private static void robe(Map<String, DeferredItem<RobeItem>> into, String name, ArmorItem.Type type,
            DeferredHolder<ArmorMaterial, ArmorMaterial> material, int lasts, int discount, boolean drab) {
        into.put(name, ITEMS.register(name, () -> new RobeItem(material, type, discount, drab,
                new Item.Properties().durability(type.getDurability(lasts)))));
    }

    private static Map<String, DeferredItem<me.moonscenty.alchemia.item.CultistArmorItem>> registerCrimson() {
        Map<String, DeferredItem<me.moonscenty.alchemia.item.CultistArmorItem>> made = new java.util.LinkedHashMap<>();
        var robe = me.moonscenty.alchemia.item.CultistArmorItem.Kind.ROBE;
        var plate = me.moonscenty.alchemia.item.CultistArmorItem.Kind.PLATE;
        crimson(made, "crimson_robe_helm", ArmorItem.Type.HELMET, robe, ModArmorMaterials.CRIMSON_ROBE);
        crimson(made, "crimson_robe_chest", ArmorItem.Type.CHESTPLATE, robe, ModArmorMaterials.CRIMSON_ROBE);
        crimson(made, "crimson_robe_legs", ArmorItem.Type.LEGGINGS, robe, ModArmorMaterials.CRIMSON_ROBE);
        crimson(made, "crimson_plate_helm", ArmorItem.Type.HELMET, plate, ModArmorMaterials.CRIMSON_PLATE);
        crimson(made, "crimson_plate_chest", ArmorItem.Type.CHESTPLATE, plate, ModArmorMaterials.CRIMSON_PLATE);
        crimson(made, "crimson_plate_legs", ArmorItem.Type.LEGGINGS, plate, ModArmorMaterials.CRIMSON_PLATE);
        var praetor = me.moonscenty.alchemia.item.CultistArmorItem.Kind.PRAETOR;
        crimson(made, "crimson_praetor_helm", ArmorItem.Type.HELMET, praetor, ModArmorMaterials.CRIMSON_PRAETOR);
        crimson(made, "crimson_praetor_chest", ArmorItem.Type.CHESTPLATE, praetor, ModArmorMaterials.CRIMSON_PRAETOR);
        crimson(made, "crimson_praetor_legs", ArmorItem.Type.LEGGINGS, praetor, ModArmorMaterials.CRIMSON_PRAETOR);
        crimson(made, "crimson_boots", ArmorItem.Type.BOOTS, me.moonscenty.alchemia.item.CultistArmorItem.Kind.BOOTS,
                ModArmorMaterials.CRIMSON_BOOTS);
        // unmodifiableMap rather than Map.copyOf, for the reason written above registerMetalTools
        return Collections.unmodifiableMap(made);
    }

    private static void crimson(Map<String, DeferredItem<me.moonscenty.alchemia.item.CultistArmorItem>> into,
            String name, ArmorItem.Type type, me.moonscenty.alchemia.item.CultistArmorItem.Kind kind,
            DeferredHolder<ArmorMaterial, ArmorMaterial> material) {
        into.put(name, ITEMS.register(name, () -> new me.moonscenty.alchemia.item.CultistArmorItem(material, type,
                kind, new Item.Properties().durability(type.getDurability(ModArmorMaterials.CRIMSON_LASTS)))));
    }

    private static Map<String, DeferredItem<FortressArmorItem>> registerFortress() {
        Map<String, DeferredItem<FortressArmorItem>> made = new java.util.LinkedHashMap<>();
        Map<String, ArmorItem.Type> pieces = new java.util.LinkedHashMap<>();
        pieces.put("fortress_helm", ArmorItem.Type.HELMET);
        pieces.put("fortress_chest", ArmorItem.Type.CHESTPLATE);
        pieces.put("fortress_legs", ArmorItem.Type.LEGGINGS);
        pieces.forEach((name, type) -> made.put(name, ITEMS.register(name,
                () -> new FortressArmorItem(ModArmorMaterials.FORTRESS, type,
                        ModArmorMaterials.FORTRESS_LASTS, new Item.Properties()))));
        // unmodifiableMap rather than Map.copyOf, for the reason written above registerMetalTools
        return Collections.unmodifiableMap(made);
    }

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
        // unmodifiableMap rather than Map.copyOf: a copied map hands its entries back in an order the
        // running machine picks for itself, and everything written out of this one -- tag files above all
        // -- would come out shuffled from one run to the next
        return Collections.unmodifiableMap(made);
    }

    private static Map<String, DeferredItem<Item>> registerElementalTools() {
        Tier tier = ModTiers.ELEMENTAL;
        Map<String, DeferredItem<Item>> made = new java.util.LinkedHashMap<>();
        made.put("elemental_axe", ITEMS.register("elemental_axe",
                () -> new me.moonscenty.alchemia.item.tool.ElementalAxeItem(tier,
                        me.moonscenty.alchemia.item.tool.ElementalTools.carrying(new Item.Properties()
                                        .attributes(AxeItem.createAttributes(tier, 6.0F, -3.1F)),
                                me.moonscenty.alchemia.enchantment.InfusionEnchantment.BURROWING, 1,
                                me.moonscenty.alchemia.enchantment.InfusionEnchantment.COLLECTOR, 1))));
        made.put("elemental_pickaxe", ITEMS.register("elemental_pickaxe",
                () -> new me.moonscenty.alchemia.item.tool.ElementalPickaxeItem(tier,
                        me.moonscenty.alchemia.item.tool.ElementalTools.carrying(new Item.Properties()
                                        .attributes(PickaxeItem.createAttributes(tier, 1.0F, -2.8F)),
                                me.moonscenty.alchemia.enchantment.InfusionEnchantment.REFINING, 1,
                                me.moonscenty.alchemia.enchantment.InfusionEnchantment.SOUNDING, 2))));
        made.put("elemental_sword", ITEMS.register("elemental_sword",
                () -> new me.moonscenty.alchemia.item.tool.ElementalSwordItem(tier,
                        me.moonscenty.alchemia.item.tool.ElementalTools.carrying(new Item.Properties()
                                        .attributes(SwordItem.createAttributes(tier, 3, -2.4F)),
                                me.moonscenty.alchemia.enchantment.InfusionEnchantment.ARCING, 2))));
        made.put("elemental_shovel", ITEMS.register("elemental_shovel",
                () -> new me.moonscenty.alchemia.item.tool.ElementalShovelItem(tier,
                        me.moonscenty.alchemia.item.tool.ElementalTools.carrying(new Item.Properties()
                                        .attributes(ShovelItem.createAttributes(tier, 1.5F, -3.0F)),
                                me.moonscenty.alchemia.enchantment.InfusionEnchantment.DESTRUCTIVE, 1))));
        made.put("elemental_hoe", ITEMS.register("elemental_hoe",
                () -> new me.moonscenty.alchemia.item.tool.ElementalHoeItem(tier,
                        me.moonscenty.alchemia.item.tool.ElementalTools.carrying(new Item.Properties()
                                .attributes(HoeItem.createAttributes(tier, -2.0F, -1.0F))))));
        // unmodifiableMap rather than Map.copyOf, for the reason written above registerMetalTools
        return Collections.unmodifiableMap(made);
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
        // unmodifiableMap rather than Map.copyOf: a copied map hands its entries back in an order the
        // running machine picks for itself, and everything written out of this one -- tag files above all
        // -- would come out shuffled from one run to the next
        return Collections.unmodifiableMap(made);
    }

    /** Alchemium and void alone. Iron, gold and brass are finished when they leave the workbench. */
    private static Map<String, DeferredItem<Item>> registerInertCaps() {
        Map<String, DeferredItem<Item>> caps = new java.util.LinkedHashMap<>();
        for (String metal : new String[] {"alchemium", "void"}) {
            caps.put(metal, ITEMS.registerSimpleItem("wand_cap_" + metal + "_inert"));
        }
        return caps;
    }

    private static Map<CrystalType, DeferredItem<me.moonscenty.alchemia.item.PrimalArrowItem>> registerPrimalArrows() {
        Map<CrystalType, DeferredItem<me.moonscenty.alchemia.item.PrimalArrowItem>> arrows =
                new EnumMap<>(CrystalType.class);
        for (CrystalType type : CrystalType.values()) {
            if (type != CrystalType.FLUX) {
                arrows.put(type, ITEMS.register("primal_arrow_" + type.getName(),
                        () -> new me.moonscenty.alchemia.item.PrimalArrowItem(type, new Item.Properties())));
            }
        }
        return Collections.unmodifiableMap(arrows);
    }

    private static Map<CrystalType, DeferredItem<Item>> registerShards() {
        Map<CrystalType, DeferredItem<Item>> shards = new EnumMap<>(CrystalType.class);
        for (CrystalType type : CrystalType.values()) {
            shards.put(type, ITEMS.registerSimpleItem(type.getName() + "_shard"));
        }
        return Collections.unmodifiableMap(shards);
    }

    private static Map<String, DeferredItem<me.moonscenty.alchemia.item.SealItem>> registerSeals() {
        Map<String, DeferredItem<me.moonscenty.alchemia.item.SealItem>> seals = new java.util.LinkedHashMap<>();
        for (String kind : me.moonscenty.alchemia.golem.seal.SealTypes.keys()) {
            seals.put(kind, ITEMS.register("seal_" + kind,
                    () -> new me.moonscenty.alchemia.item.SealItem(kind, new Item.Properties())));
        }
        return java.util.Collections.unmodifiableMap(seals);
    }
}
