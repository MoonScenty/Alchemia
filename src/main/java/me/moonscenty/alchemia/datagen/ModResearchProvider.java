package me.moonscenty.alchemia.datagen;

import net.minecraft.world.level.block.Blocks;
import java.util.List;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.block.CrystalType;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.research.Clues;
import me.moonscenty.alchemia.research.ModResearch;
import me.moonscenty.alchemia.research.NodeShape;
import me.moonscenty.alchemia.research.ResearchPage;
import me.moonscenty.alchemia.research.ResearchCategory;
import me.moonscenty.alchemia.research.ResearchEntry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.DyeColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class ModResearchProvider {
    public static void categories(BootstrapContext<ResearchCategory> context) {
        category(context, ModResearch.BASICS, "basics", 0);
        category(context, ModResearch.THAUMATURGY, "thaumaturgy", 1);
        category(context, ModResearch.ALCHEMY, "alchemy", 2);
        category(context, ModResearch.ARTIFICE, "artifice", 3);
        category(context, ModResearch.GOLEMANCY, "golemancy", 4, "basic_golemancy");
        category(context, ModResearch.ELDRITCH, "eldritch", 5, "eldritch_minor");
    }

    public static void entries(BootstrapContext<ResearchEntry> context) {
        entry(context, "aspects", ModResearch.BASICS, ModItems.THAUMOMETER, 0, 0,
                AspectList.EMPTY, List.of(), true, NodeShape.MAJOR, 1, "thaumometer", "wooden_table",
                "scribing_tools", "scribing_tools_from_bottle", "scribing_tools_refill");

        entry(context, "crystal_farmer", ModResearch.BASICS, ModBlocks.CRYSTALS.get(CrystalType.AIR), 1, 1,
                AspectList.of(ModAspects.CRYSTAL, 3).add(ModAspects.FLUX, 3).add(ModAspects.AIR, 3)
                        .add(ModAspects.FIRE, 3).add(ModAspects.WATER, 3).add(ModAspects.EARTH, 3)
                        .add(ModAspects.ORDER, 3).add(ModAspects.ENTROPY, 3),
                List.of("infusion"), false, NodeShape.PLAIN, 2, "air_crystal_from_shard", "fire_crystal_from_shard",
                "water_crystal_from_shard", "earth_crystal_from_shard", "order_crystal_from_shard",
                "entropy_crystal_from_shard", "flux_crystal_from_shard");
        entry(context, "researcher1", ModResearch.BASICS, ModItems.SCRIBING_TOOLS, 4, 1,
                AspectList.of(ModAspects.MIND, 3).add(ModAspects.SENSES, 3).add(ModAspects.ORDER, 3),
                List.of("aspects"), false, NodeShape.PLAIN, 1);
        entry(context, "researcher2", ModResearch.BASICS, ModItems.KNOWLEDGE_FRAGMENT, 3, 3,
                AspectList.of(ModAspects.MIND, 6).add(ModAspects.ORDER, 3).add(ModAspects.SENSES, 3)
                        .add(ModAspects.ENERGY, 3),
                List.of("researcher1"), false, NodeShape.SPECIAL, 2);
        entry(context, "enchantments", ModResearch.BASICS, net.minecraft.world.item.Items.ENCHANTED_BOOK, -2, -1,
                AspectList.EMPTY, List.of(), true, NodeShape.PLAIN, 1);
        entry(context, "amber", ModResearch.BASICS, ModItems.AMBER, -2, 1,
                AspectList.of(ModAspects.CRYSTAL, 2).add(ModAspects.TRAP, 2), List.of("aspects"), false, NodeShape.PLAIN, 1, "amber_from_ore_smelting", "amber_block_from_amber");
        entry(context, "quicksilver", ModResearch.ALCHEMY, ModItems.QUICKSILVER, 0, 1,
                AspectList.of(ModAspects.METAL, 3).add(ModAspects.EXCHANGE, 2), List.of("aspects"), false, NodeShape.PLAIN, 2, "quicksilver_from_raw_cinnabar_smelting", "quicksilver_from_shimmerleaf");
        entry(context, "vis_crystals", ModResearch.THAUMATURGY, ModItems.BALANCED_SHARD, 2, 1,
                AspectList.of(ModAspects.CRYSTAL, 4).add(ModAspects.AURA, 2), List.of("aspects"), false, NodeShape.SPECIAL, 2);
        entry(context, "greatwood", ModResearch.BASICS, ModBlocks.GREATWOOD.sapling(), -1, 2,
                AspectList.of(ModAspects.PLANT, 4).add(ModAspects.LIFE, 2), List.of("aspects"), false, NodeShape.PLAIN, 1, "greatwood_planks");
        entry(context, "silverwood", ModResearch.THAUMATURGY, ModBlocks.SILVERWOOD.sapling(), 1, 2,
                AspectList.of(ModAspects.PLANT, 4).add(ModAspects.AURA, 4), List.of("greatwood"), false, NodeShape.SPECIAL, 2, "silverwood_planks");
        entry(context, "warp", ModResearch.BASICS, Items.ENDER_PEARL, 0, 2, AspectList.EMPTY, List.of(), true,
                NodeShape.MAJOR, 1);

        entry(context, "wand", ModResearch.THAUMATURGY, ModItems.WAND, 3, 0,
                AspectList.EMPTY, List.of(), true, NodeShape.MAJOR, 1, "wand_cap_iron", "wand");
        entry(context, "arcane_workbench", ModResearch.THAUMATURGY, ModBlocks.ARCANE_WORKBENCH, 4, 1,
                AspectList.of(ModAspects.CRAFT, 4).add(ModAspects.AURA, 2), List.of("wand"), false, NodeShape.MAJOR, 2);
        entry(context, "charger", ModResearch.THAUMATURGY, ModBlocks.ARCANE_WORKBENCH_CHARGER, 5, 2,
                AspectList.of(ModAspects.AURA, 3).add(ModAspects.MECHANISM, 3).add(ModAspects.ENERGY, 6),
                List.of("recharge_pedestal"), false, NodeShape.PLAIN, 2, "arcane_workbench_charger");

        entry(context, "wand_cap_gold", ModResearch.THAUMATURGY, ModItems.WAND_CAPS.get("gold"), 4, 3,
                AspectList.of(ModAspects.METAL, 3).add(ModAspects.DESIRE, 3).add(ModAspects.TOOL, 3),
                List.of("arcane_workbench"), false, NodeShape.PLAIN, 1, "wand_cap_gold");
        entry(context, "wand_cap_brass", ModResearch.THAUMATURGY, ModItems.WAND_CAPS.get("brass"), 5, 4,
                AspectList.of(ModAspects.METAL, 3).add(ModAspects.ENERGY, 3).add(ModAspects.TOOL, 3),
                List.of("wand_cap_gold", "metallurgy"), false, NodeShape.PLAIN, 1, "wand_cap_brass");
        entry(context, "wand_cap_thaumium", ModResearch.THAUMATURGY, ModItems.WAND_CAPS.get("thaumium"), 6, 5,
                AspectList.of(ModAspects.METAL, 6).add(ModAspects.ENERGY, 6).add(ModAspects.TOOL, 3)
                        .add(ModAspects.AURA, 3),
                List.of("wand_cap_brass", "metallurgy", "infusion"), false, NodeShape.SPECIAL, 2,
                "wand_cap_thaumium_inert", "wand_cap_thaumium");

        entry(context, "wand_rod_greatwood", ModResearch.THAUMATURGY, ModItems.WAND_RODS.get("greatwood"), 2, 3,
                AspectList.of(ModAspects.TOOL, 3).add(ModAspects.PLANT, 6).add(ModAspects.ENERGY, 3),
                List.of("arcane_workbench", "greatwood"), false, NodeShape.PLAIN, 1, "wand_rod_greatwood");

        entry(context, "crucible", ModResearch.ALCHEMY, ModBlocks.CRUCIBLE, 0, 2,
                AspectList.EMPTY, List.of(), true, NodeShape.MAJOR, 1, "morphic_resonator", "balanced_shard_from_air",
                "balanced_shard_from_fire", "balanced_shard_from_water", "balanced_shard_from_earth",
                "balanced_shard_from_order", "balanced_shard_from_entropy");
        entry(context, "metallurgy", ModResearch.ALCHEMY, ModItems.THAUMIUM_INGOT, -1, 3,
                AspectList.of(ModAspects.METAL, 3).add(ModAspects.MECHANISM, 3),
                List.of("crucible"), false, NodeShape.PLAIN, 1,
                "thaumium_ingot_from_iron", "brass_ingot_from_iron");

        entry(context, "alumentum", ModResearch.ALCHEMY, ModItems.ALUMENTUM, -3, 2,
                AspectList.of(ModAspects.ENERGY, 3).add(ModAspects.FIRE, 1),
                List.of("crucible"), false, NodeShape.PLAIN, 1, "alumentum");
        entry(context, "nitor", ModResearch.ALCHEMY, ModBlocks.NITOR.get(DyeColor.WHITE), -3, 4,
                AspectList.of(ModAspects.LIGHT, 3).add(ModAspects.FIRE, 1),
                List.of("crucible"), false, NodeShape.PLAIN, 1, "nitor");

        entry(context, "distillation", ModResearch.ALCHEMY, ModBlocks.ESSENTIA_SMELTER, 1, 4,
                AspectList.of(ModAspects.FIRE, 3).add(ModAspects.WATER, 3).add(ModAspects.CRAFT, 3),
                List.of("nitor", "alumentum"), false, NodeShape.MAJOR, 1, "filter", "essentia_smelter", "alembic");
        entry(context, "alchemical_duplication", ModResearch.ALCHEMY, net.minecraft.world.item.Items.GUNPOWDER, -3, 1,
                AspectList.of(ModAspects.WATER, 3).add(ModAspects.DESIRE, 6).add(ModAspects.ENERGY, 3),
                List.of("tallow"), false, NodeShape.PLAIN, 1, "gunpowder_duplication", "slime_ball_duplication", "glowstone_dust_duplication", "ink_sac_duplication", "prismarine_crystals_duplication", "prismarine_shard_duplication");
        entry(context, "alchemical_manufacture", ModResearch.ALCHEMY, net.minecraft.world.item.Items.CLAY_BALL, -3, 3,
                AspectList.of(ModAspects.WATER, 3).add(ModAspects.CRAFT, 6).add(ModAspects.ENERGY, 3),
                List.of("tallow"), false, NodeShape.PLAIN, 1, "clay_ball_from_dirt", "cobweb_from_string", "mossy_cobblestone_from_cobblestone", "ice_from_snow", "lava_bucket_from_bucket", "grass_block_from_dirt");
        entry(context, "entropic_processing", ModResearch.ALCHEMY, net.minecraft.world.item.Items.BONE_MEAL, -3, 0,
                AspectList.of(ModAspects.WATER, 3).add(ModAspects.ENTROPY, 6).add(ModAspects.ENERGY, 3),
                List.of("tallow"), false, NodeShape.PLAIN, 1, "bone_meal_from_bone", "blaze_powder_from_blaze_rod", "leather_from_rotten_flesh", "string_from_wool", "mycelium_from_dirt");
        entry(context, "taint_shard", ModResearch.ALCHEMY, ModItems.SHARDS.get(CrystalType.FLUX), -4, 0,
                AspectList.of(ModAspects.FLUX, 5).add(ModAspects.AURA, 3).add(ModAspects.ENTROPY, 5),
                List.of("entropic_processing"), false, NodeShape.PLAIN, 1, "flux_shard_from_shard");
        entry(context, "pure_metal", ModResearch.ALCHEMY, ModItems.IRON_CLUSTER, -2, 3,
                AspectList.of(ModAspects.METAL, 3).add(ModAspects.ORDER, 3),
                List.of("metallurgy"), false, NodeShape.PLAIN, 1, "iron_cluster_from_ore", "gold_cluster_from_ore", "copper_cluster_from_ore", "cinnabar_cluster_from_ore");
        entry(context, "trans_metal", ModResearch.ALCHEMY, net.minecraft.world.item.Items.GOLD_NUGGET, -1, 4,
                AspectList.of(ModAspects.METAL, 3).add(ModAspects.EXCHANGE, 3).add(ModAspects.FLUX, 3),
                List.of("metallurgy"), false, NodeShape.PLAIN, 1, "iron_nugget_transmutation", "gold_nugget_transmutation");
        entry(context, "ethereal_bloom", ModResearch.ALCHEMY, ModBlocks.ETHEREAL_BLOOM, -4, 2,
                AspectList.of(ModAspects.LIGHT, 1).add(ModAspects.PLANT, 6).add(ModAspects.LIFE, 3)
                        .add(ModAspects.FLUX, 6), List.of("alchemical_manufacture"), false, NodeShape.PLAIN, 2,
                "ethereal_bloom");
        entry(context, "jar_void", ModResearch.ALCHEMY, ModBlocks.VOID_JAR, 0, 6,
                AspectList.of(ModAspects.WATER, 3).add(ModAspects.VOID, 6), List.of("jar_label"), false,
                NodeShape.PLAIN, 1, "void_jar");
        entry(context, "jar_label", ModResearch.ALCHEMY, ModBlocks.JAR, 0, 5,
                AspectList.EMPTY, List.of("distillation"), false, NodeShape.PLAIN, 1,
                "jar", "phial", "jar_label", "jar_brace");
        entry(context, "tubes", ModResearch.ALCHEMY, ModBlocks.TUBE, 2, 5,
                AspectList.of(ModAspects.WATER, 3).add(ModAspects.EXCHANGE, 6),
                List.of("distillation"), false, NodeShape.PLAIN, 1, "tube", "tube_valve", "essentia_resonator");
        entry(context, "tube_filter", ModResearch.ALCHEMY, ModBlocks.TUBE_FILTER, 3, 6,
                AspectList.of(ModAspects.WATER, 3).add(ModAspects.EXCHANGE, 6).add(ModAspects.ORDER, 3),
                List.of("tubes"), false, NodeShape.PLAIN, 1,
                "tube_filter", "tube_restrict", "tube_oneway", "tube_buffer", "alchemical_construct");

        entry(context, "mirror", ModResearch.ARTIFICE, ModBlocks.MIRROR, -1, 9,
                AspectList.of(ModAspects.MOTION, 6).add(ModAspects.ELDRITCH, 3).add(ModAspects.DARKNESS, 3)
                        .add(ModAspects.CRYSTAL, 3),
                List.of("infusion"), false, NodeShape.PLAIN, 2, "mirror");
        entry(context, "mirror_hand", ModResearch.ARTIFICE, ModItems.HAND_MIRROR, -2, 10,
                AspectList.of(ModAspects.TOOL, 6).add(ModAspects.ELDRITCH, 3).add(ModAspects.CRYSTAL, 3)
                        .add(ModAspects.MOTION, 3),
                List.of("mirror"), false, NodeShape.PLAIN, 2, "hand_mirror");
        entry(context, "mirror_essentia", ModResearch.ARTIFICE, ModBlocks.MIRROR_ESSENTIA, 0, 10,
                AspectList.of(ModAspects.MOTION, 6).add(ModAspects.ELDRITCH, 3).add(ModAspects.WATER, 3)
                        .add(ModAspects.ENERGY, 3),
                List.of("mirror"), false, NodeShape.PLAIN, 2, "mirror_essentia");

        entry(context, "infernal_furnace", ModResearch.ARTIFICE, Blocks.NETHER_BRICKS, 4, -1,
                AspectList.of(ModAspects.FIRE, 6).add(ModAspects.METAL, 3).add(ModAspects.CRAFT, 3)
                        .add(ModAspects.AURA, 3),
                List.of("primal_charm", "nitor", "alumentum"), false, NodeShape.PLAIN, 2);

        entry(context, "arcane_bore", ModResearch.GOLEMANCY, ModBlocks.ARCANE_BORE, 5, 5,
                AspectList.of(ModAspects.EARTH, 6).add(ModAspects.MOTION, 3).add(ModAspects.MECHANISM, 3)
                        .add(ModAspects.TOOL, 3),
                List.of("infusion", "basic_turret", "focus_excavation"), false, NodeShape.PLAIN, 2, "arcane_bore",
                "arcane_bore_base");

        entry(context, "brain_jar", ModResearch.ARTIFICE, ModBlocks.BRAIN_JAR, -2, 2,
                AspectList.of(ModAspects.DESIRE, 6).add(ModAspects.MIND, 3).add(ModAspects.UNDEAD, 3),
                List.of("infusion"), false, NodeShape.PLAIN, 2, "brain_jar");

        entry(context, "thaumatorium", ModResearch.ALCHEMY, ModBlocks.ALCHEMICAL_CONSTRUCT, 6, 8,
                AspectList.of(ModAspects.WATER, 3).add(ModAspects.ENERGY, 6).add(ModAspects.EXCHANGE, 3)
                        .add(ModAspects.CRAFT, 3),
                List.of("improved_distillation"), false, NodeShape.PLAIN, 3, "mnemonic_matrix");

        entry(context, "centrifuge", ModResearch.ALCHEMY, ModBlocks.CENTRIFUGE, 5, 5,
                AspectList.of(ModAspects.FLUX, 3).add(ModAspects.ENERGY, 3).add(ModAspects.EXCHANGE, 3)
                        .add(ModAspects.CRAFT, 3),
                List.of("tube_filter"), false, NodeShape.PLAIN, 2, "centrifuge");
        entry(context, "crystallizer", ModResearch.ALCHEMY, ModBlocks.CRYSTALLIZER, 5, 6,
                AspectList.of(ModAspects.WATER, 5).add(ModAspects.CRYSTAL, 5).add(ModAspects.EXCHANGE, 3)
                        .add(ModAspects.ENERGY, 5),
                List.of("tube_filter"), false, NodeShape.PLAIN, 1, "crystallizer");

        entry(context, "improved_distillation", ModResearch.ALCHEMY, ModBlocks.SMELTER_AUX, 4, 7,
                AspectList.of(ModAspects.FIRE, 5).add(ModAspects.TOOL, 3).add(ModAspects.WATER, 5)
                        .add(ModAspects.CRAFT, 5),
                List.of("tube_filter"), false, NodeShape.PLAIN, 2, "smelter_aux", "smelter_vent");
        entry(context, "thaumium_smelter", ModResearch.ALCHEMY, ModBlocks.THAUMIUM_SMELTER, 2, 7,
                AspectList.of(ModAspects.FIRE, 3).add(ModAspects.WATER, 3).add(ModAspects.CRAFT, 3),
                List.of("distillation", "improved_distillation"), false, NodeShape.PLAIN, 1, "thaumium_smelter");
        entry(context, "void_smelter", ModResearch.ALCHEMY, ModBlocks.VOID_SMELTER, 2, 8,
                AspectList.of(ModAspects.FIRE, 3).add(ModAspects.WATER, 3).add(ModAspects.CRAFT, 3),
                List.of("thaumium_smelter", "primordial_pearl"), false, NodeShape.PLAIN, 1,
                "advanced_alchemical_construct",
                "void_smelter");

        entry(context, "void_metal", ModResearch.ALCHEMY, ModItems.VOID_INGOT, -2, 4,
                AspectList.of(ModAspects.METAL, 3).add(ModAspects.ELDRITCH, 3).add(ModAspects.VOID, 5)
                        .add(ModAspects.FLUX, 3),
                List.of("metallurgy", "eldritch_minor"), false, NodeShape.SPECIAL, 2,
                "void_seed", "void_ingot", "void_plate", "wand_cap_void_inert", "wand_cap_void");

        entry(context, "thaumium_gear", ModResearch.ARTIFICE, ModItems.METAL_TOOLS.get("thaumium_pickaxe"),
                -1, 5, AspectList.of(ModAspects.METAL, 6).add(ModAspects.TOOL, 6).add(ModAspects.PROTECT, 4),
                List.of("metallurgy"), false, NodeShape.PLAIN, 2,
                "thaumium_pickaxe", "thaumium_axe", "thaumium_shovel", "thaumium_sword", "thaumium_hoe",
                "thaumium_helmet", "thaumium_chestplate", "thaumium_leggings", "thaumium_boots");
        entry(context, "void_gear", ModResearch.ARTIFICE, ModItems.METAL_TOOLS.get("void_pickaxe"),
                -3, 5, AspectList.of(ModAspects.VOID, 6).add(ModAspects.TOOL, 6).add(ModAspects.PROTECT, 4)
                        .add(ModAspects.ELDRITCH, 2),
                List.of("void_metal", "thaumium_gear"), false, NodeShape.PLAIN, 3,
                "void_pickaxe", "void_axe", "void_shovel", "void_sword", "void_hoe",
                "void_helmet", "void_chestplate", "void_leggings", "void_boots");

        entry(context, "infusion", ModResearch.THAUMATURGY, ModBlocks.INFUSION_MATRIX, 7, 2,
                AspectList.of(ModAspects.ENERGY, 6).add(ModAspects.MECHANISM, 3).add(ModAspects.CRAFT, 6),
                List.of("primal_charm", "distillation"), false, NodeShape.MAJOR, 2,
                "infusion_matrix", "arcane_pedestal");
        entry(context, "infusion_boost", ModResearch.ARTIFICE, ModBlocks.INFUSION_SPEED_STONE, 5, 4,
                AspectList.of(ModAspects.ENERGY, 6).add(ModAspects.AIR, 3).add(ModAspects.WATER, 3)
                        .add(ModAspects.EXCHANGE, 3),
                List.of("infusion", "alumentum", "nitor"), false, NodeShape.PLAIN, 3,
                "infusion_speed_stone", "infusion_cost_stone");

        entry(context, "goggles", ModResearch.ARTIFICE, ModItems.GOGGLES, 5, 2,
                AspectList.of(ModAspects.SENSES, 3).add(ModAspects.AURA, 3).add(ModAspects.ENERGY, 3),
                List.of("arcane_workbench"), false, NodeShape.PLAIN, 1, "goggles_of_revealing");

        entry(context, "enchanted_fabric", ModResearch.ARTIFICE, ModItems.ENCHANTED_FABRIC, 6, 3,
                AspectList.EMPTY, List.of(), true, NodeShape.PLAIN, 1, "enchanted_fabric");
        entry(context, "robes", ModResearch.ARTIFICE, ModItems.ROBES.get("cloth_chest"), 7, 2,
                AspectList.of(ModAspects.AURA, 4).add(ModAspects.PROTECT, 4).add(ModAspects.CRAFT, 3)
                        .add(ModAspects.ENERGY, 2),
                List.of("enchanted_fabric"), false, NodeShape.PLAIN, 2,
                "cloth_chest", "cloth_legs", "cloth_boots");
        entry(context, "void_robes", ModResearch.ARTIFICE, ModItems.ROBES.get("void_robe_chest"), 8, 3,
                AspectList.of(ModAspects.PROTECT, 3).add(ModAspects.VOID, 6).add(ModAspects.ELDRITCH, 9)
                        .add(ModAspects.SOUL, 6),
                List.of("void_metal", "enchanted_fabric", "eldritch_major"), false, NodeShape.SPECIAL, 2,
                "void_robe_helm", "void_robe_chest", "void_robe_legs");

        entry(context, "fortress_armour", ModResearch.ARTIFICE, ModItems.FORTRESS.get("fortress_chest"), 6, 5,
                AspectList.of(ModAspects.METAL, 3).add(ModAspects.PROTECT, 5).add(ModAspects.CRAFT, 5),
                List.of("metallurgy", "traveller_boots"), false, NodeShape.SPECIAL, 2,
                "fortress_helm", "fortress_chest", "fortress_legs", "helm_goggles",
                "mask_grinning_devil", "mask_angry_ghost", "mask_sipping_fiend");

        entry(context, "traveller_boots", ModResearch.ARTIFICE, ModItems.TRAVELLER_BOOTS, 7, 4,
                AspectList.of(ModAspects.MOTION, 3).add(ModAspects.EARTH, 3).add(ModAspects.FLIGHT, 3)
                        .add(ModAspects.WATER, 3),
                List.of("infusion"), false, NodeShape.PLAIN, 2, "traveller_boots");

        entry(context, "infusion_enchantment", ModResearch.THAUMATURGY, Items.ENCHANTED_BOOK, 9, 2,
                AspectList.of(ModAspects.ENERGY, 6).add(ModAspects.MIND, 3).add(ModAspects.AVERSION, 3)
                        .add(ModAspects.PROTECT, 3).add(ModAspects.TOOL, 3),
                List.of("elemental_tools"), false, NodeShape.PLAIN, 3,
                "infusion_enchantment_collector", "infusion_enchantment_destructive",
                "infusion_enchantment_burrowing", "infusion_enchantment_refining", "infusion_enchantment_sounding",
                "infusion_enchantment_arcing", "infusion_enchantment_essence");

        entry(context, "sceptre", ModResearch.THAUMATURGY, ModItems.PRIMAL_CHARM, 7, 5,
                AspectList.of(ModAspects.TOOL, 6).add(ModAspects.CRAFT, 6).add(ModAspects.PLANT, 6)
                        .add(ModAspects.ENERGY, 9),
                List.of("wand_rods"), false, NodeShape.PLAIN, 3, "arcane_sceptre");

        entry(context, "staff_greatwood", ModResearch.THAUMATURGY, ModItems.WAND_RODS.get("greatwood_staff"), 9, 5,
                AspectList.of(ModAspects.TOOL, 3).add(ModAspects.PLANT, 6).add(ModAspects.ENERGY, 3),
                List.of("wand_rods"), false, NodeShape.PLAIN, 1, "wand_rod_greatwood_staff");
        entry(context, "staves_primal", ModResearch.THAUMATURGY, ModItems.WAND_RODS.get("blaze_staff"), 10, 4,
                AspectList.of(ModAspects.TOOL, 3).add(ModAspects.AIR, 6).add(ModAspects.FIRE, 6)
                        .add(ModAspects.EARTH, 6).add(ModAspects.WATER, 6).add(ModAspects.ORDER, 6)
                        .add(ModAspects.ENTROPY, 6).add(ModAspects.ENERGY, 3),
                List.of("wand_rods", "staff_greatwood", "infusion"), false, NodeShape.PLAIN, 3,
                "wand_rod_reed_staff", "wand_rod_blaze_staff", "wand_rod_obsidian_staff", "wand_rod_ice_staff",
                "wand_rod_quartz_staff", "wand_rod_bone_staff");
        entry(context, "staff_silverwood", ModResearch.THAUMATURGY, ModItems.WAND_RODS.get("silverwood_staff"), 9, 6,
                AspectList.of(ModAspects.TOOL, 6).add(ModAspects.PLANT, 6).add(ModAspects.ENERGY, 9),
                List.of("wand_rods", "staff_greatwood"), false, NodeShape.PLAIN, 3, "wand_rod_silverwood_staff");

        entry(context, "wand_rods", ModResearch.THAUMATURGY, ModItems.WAND_RODS.get("silverwood"), 8, 4,
                AspectList.eachPrimal(6).add(ModAspects.TOOL, 3).add(ModAspects.ENERGY, 3),
                List.of("wand_rod_greatwood", "infusion", "silverwood"), false, NodeShape.PLAIN, 3,
                "wand_rod_obsidian", "wand_rod_ice", "wand_rod_quartz", "wand_rod_reed",
                "wand_rod_blaze", "wand_rod_bone", "wand_rod_silverwood");

        entry(context, "focal_manipulator", ModResearch.THAUMATURGY, ModBlocks.FOCAL_MANIPULATOR, 2, 5,
                AspectList.of(ModAspects.ENERGY, 5).add(ModAspects.TOOL, 8).add(ModAspects.CRAFT, 5)
                        .add(ModAspects.CRYSTAL, 5),
                List.of("focus_fire"), false, NodeShape.MAJOR, 2, "focal_manipulator");

        entry(context, "elemental_tools", ModResearch.ARTIFICE, ModItems.ELEMENTAL_TOOLS.get("elemental_axe"), 4, 6,
                AspectList.of(ModAspects.TOOL, 6).add(ModAspects.AVERSION, 6).add(ModAspects.AIR, 3)
                        .add(ModAspects.FIRE, 3).add(ModAspects.WATER, 3).add(ModAspects.EARTH, 3)
                        .add(ModAspects.ORDER, 3).add(ModAspects.ENTROPY, 3),
                List.of("thaumium_gear", "infusion", "vis_crystals"), false, NodeShape.PLAIN, 2,
                "elemental_axe", "elemental_pickaxe", "elemental_sword", "elemental_shovel", "elemental_hoe");
        hidden(context, "eldritch_minor", ModResearch.ELDRITCH, ModItems.VOID_SEED, 1, 0, NodeShape.SPECIAL);
        hidden(context, "eldritch_major", ModResearch.ELDRITCH, net.minecraft.world.item.Items.ENDER_EYE, -1, 0,
                NodeShape.SPECIAL);
        entry(context, "oculus", ModResearch.ELDRITCH, ModItems.ELDRITCH_EYE, -2, 3,
                AspectList.of(ModAspects.MIND, 3).add(ModAspects.DARKNESS, 3).add(ModAspects.EXCHANGE, 3)
                        .add(ModAspects.MOTION, 6).add(ModAspects.ELDRITCH, 6),
                List.of("crimson", "eldritch_major"), false, NodeShape.SPECIAL, 1, "eldritch_eye");
        entry(context, "banners", ModResearch.ARTIFICE, ModItems.BANNER, 0, -5, AspectList.EMPTY, List.of(), true,
                NodeShape.PLAIN, 1);

        hidden(context, "crimson", ModResearch.ELDRITCH, ModItems.CRIMSON_RITES, 0, 2, NodeShape.SPECIAL);
        hidden(context, "enter_outer", ModResearch.ELDRITCH, ModItems.RUNED_TABLET, -3, 5, NodeShape.SPECIAL);
        entry(context, "outer_revelations", ModResearch.ELDRITCH, ModItems.RUNED_TABLET, -4, 5, AspectList.EMPTY,
                List.of("enter_outer"), false, NodeShape.SPECIAL, 1);
        entry(context, "primordial_pearl", ModResearch.ELDRITCH, ModItems.PRIMORDIAL_PEARL, 2, 3,
                AspectList.of(ModAspects.ENERGY, 9).add(ModAspects.ELDRITCH, 9).add(ModAspects.ORDER, 9),
                List.of("eldritch_minor"), false, NodeShape.PLAIN, 2);

        entry(context, "primal_crusher", ModResearch.ELDRITCH, ModItems.PRIMAL_CRUSHER, 1, 5,
                AspectList.of(ModAspects.EARTH, 6).add(ModAspects.TOOL, 6).add(ModAspects.ENTROPY, 6)
                        .add(ModAspects.VOID, 6).add(ModAspects.AVERSION, 6).add(ModAspects.ELDRITCH, 6)
                        .add(ModAspects.DESIRE, 6),
                List.of("primordial_pearl"), false, NodeShape.SPECIAL, 2, "primal_crusher");

        entry(context, "autocaster", ModResearch.GOLEMANCY, ModItems.AUTOCASTER, 1, 6,
                AspectList.of(ModAspects.MECHANISM, 3).add(ModAspects.AVERSION, 3).add(ModAspects.SENSES, 3)
                        .add(ModAspects.MIND, 3).add(ModAspects.AURA, 3),
                List.of("biothaumic_mind", "arcane_bore"), false, NodeShape.MAJOR, 2, "autocaster");

        entry(context, "basic_turret", ModResearch.GOLEMANCY, ModItems.AUTOMATED_CROSSBOW, 3, 3,
                AspectList.of(ModAspects.MECHANISM, 3).add(ModAspects.AVERSION, 3).add(ModAspects.SENSES, 3)
                        .add(ModAspects.MIND, 3),
                List.of("clockwork_mind"), false, NodeShape.PLAIN, 1, "automated_crossbow");
        entry(context, "advanced_turret", ModResearch.GOLEMANCY, ModItems.ADVANCED_AUTOMATED_CROSSBOW, 3, 5,
                AspectList.of(ModAspects.MECHANISM, 3).add(ModAspects.AVERSION, 3).add(ModAspects.SENSES, 3)
                        .add(ModAspects.MIND, 3),
                List.of("biothaumic_mind", "basic_turret"), false, NodeShape.PLAIN, 2,
                "advanced_automated_crossbow");

        entry(context, "clockwork_mind", ModResearch.GOLEMANCY, ModItems.CLOCKWORK_MIND, 0, 3,
                AspectList.of(ModAspects.MECHANISM, 3).add(ModAspects.MIND, 3).add(ModAspects.SENSES, 3)
                        .add(ModAspects.ENERGY, 6).add(ModAspects.LIFE, 3),
                List.of("golem_wood", "distillation", "tallow", "nitor"), false, NodeShape.PLAIN, 1, "clockwork_mind",
                "stone_table");
        hidden(context, "basic_golemancy", ModResearch.GOLEMANCY, ModItems.GOLEM_BELL, 0, 0, NodeShape.MAJOR);
        entry(context, "golem_wood", ModResearch.GOLEMANCY, ModBlocks.GREATWOOD.planks().get(), -2, -2,
                AspectList.EMPTY, List.of(), false, NodeShape.MAJOR, 1);
        entry(context, "golem_iron", ModResearch.GOLEMANCY, ModItems.IRON_PLATE, -2, -4,
                AspectList.of(ModAspects.METAL, 5).add(ModAspects.CRAFT, 3),
                List.of("golem_wood"), false, NodeShape.PLAIN, 1, "iron_plate");
        entry(context, "golem_clay", ModResearch.GOLEMANCY, net.minecraft.world.level.block.Blocks.TERRACOTTA, -3, -3,
                AspectList.of(ModAspects.EARTH, 5).add(ModAspects.CRAFT, 3),
                List.of("golem_iron"), false, NodeShape.PLAIN, 1);
        entry(context, "golem_brass", ModResearch.GOLEMANCY, ModItems.BRASS_PLATE, -1, -5,
                AspectList.of(ModAspects.METAL, 3).add(ModAspects.CRAFT, 3).add(ModAspects.ENERGY, 5),
                List.of("golem_iron", "metallurgy"), false, NodeShape.PLAIN, 2, "brass_gear", "brass_plate");
        entry(context, "golem_thaumium", ModResearch.GOLEMANCY, ModItems.THAUMIUM_PLATE, -1, -7,
                AspectList.of(ModAspects.METAL, 3).add(ModAspects.CRAFT, 3).add(ModAspects.ORDER, 5),
                List.of("golem_brass", "metallurgy"), false, NodeShape.PLAIN, 2, "thaumium_gear", "thaumium_plate");
        entry(context, "golem_void", ModResearch.GOLEMANCY, ModItems.VOID_PLATE, -2, -6,
                AspectList.of(ModAspects.METAL, 5).add(ModAspects.CRAFT, 5).add(ModAspects.ELDRITCH, 8),
                List.of("golem_brass", "golem_thaumium", "void_metal"), false, NodeShape.PLAIN, 3, "void_gear",
                "void_plate");
        entry(context, "golem_direct", ModResearch.GOLEMANCY, ModItems.GOLEM_BELL, 1, 4,
                AspectList.of(ModAspects.SENSES, 5).add(ModAspects.MIND, 5).add(ModAspects.SOUL, 8),
                List.of("clockwork_mind"), false, NodeShape.MAJOR, 3);
        entry(context, "control_seals", ModResearch.GOLEMANCY, ModItems.SEAL_BLANK, -2, 5, AspectList.EMPTY,
                List.of("distillation", "tallow", "nitor"), false, NodeShape.MAJOR, 1, "seal_blank", "golem_bell");
        entry(context, "seal_collect", ModResearch.GOLEMANCY, ModItems.SEALS.get("pickup").get(), -4, 4,
                AspectList.EMPTY, List.of("control_seals"), false, NodeShape.PLAIN, 1, "seal_pickup",
                "seal_pickup_advanced");
        entry(context, "seal_store", ModResearch.GOLEMANCY, ModItems.SEALS.get("fill").get(), -6, 4,
                AspectList.of(ModAspects.VOID, 3).add(ModAspects.MIND, 3).add(ModAspects.EXCHANGE, 5),
                List.of("seal_collect"), false, NodeShape.PLAIN, 1, "seal_fill", "seal_fill_advanced");
        entry(context, "seal_empty", ModResearch.GOLEMANCY, ModItems.SEALS.get("empty").get(), -8, 4,
                AspectList.of(ModAspects.VOID, 3).add(ModAspects.MIND, 3).add(ModAspects.EXCHANGE, 5),
                List.of("seal_store"), false, NodeShape.PLAIN, 1, "seal_empty", "seal_empty_advanced");
        entry(context, "seal_provide", ModResearch.GOLEMANCY, ModItems.SEALS.get("provider").get(), -7, 6,
                AspectList.of(ModAspects.VOID, 3).add(ModAspects.MIND, 5).add(ModAspects.EXCHANGE, 4),
                List.of("seal_empty", "biothaumic_mind"), false, NodeShape.PLAIN, 1, "seal_provider");
        entry(context, "seal_guard", ModResearch.GOLEMANCY, ModItems.SEALS.get("guard").get(), -12, 4,
                AspectList.of(ModAspects.MAN, 3).add(ModAspects.MIND, 3).add(ModAspects.PROTECT, 5)
                        .add(ModAspects.AVERSION, 5),
                List.of("control_seals"), false, NodeShape.PLAIN, 2, "aggression_module", "seal_guard",
                "seal_guard_advanced");
        entry(context, "golem_combat", ModResearch.GOLEMANCY, ModItems.AGGRESSION_MODULE, -12, 2,
                AspectList.of(ModAspects.MECHANISM, 5).add(ModAspects.AVERSION, 5).add(ModAspects.PROTECT, 5)
                        .add(ModAspects.MIND, 5),
                List.of("seal_guard"), false, NodeShape.PLAIN, 2);
        entry(context, "seal_butcher", ModResearch.GOLEMANCY, ModItems.SEALS.get("butcher").get(), -11, 6,
                AspectList.of(ModAspects.MAN, 3).add(ModAspects.MIND, 3).add(ModAspects.BEAST, 5),
                List.of("biothaumic_mind", "infusion", "seal_guard"), false, NodeShape.PLAIN, 2, "seal_butcher");
        entry(context, "seal_use", ModResearch.GOLEMANCY, ModItems.SEALS.get("use").get(), -3, 6,
                AspectList.of(ModAspects.MAN, 3).add(ModAspects.MIND, 3).add(ModAspects.CRAFT, 5),
                List.of("control_seals", "biothaumic_mind", "infusion"), false, NodeShape.PLAIN, 1, "seal_use");
        entry(context, "seal_harvest", ModResearch.GOLEMANCY, ModItems.SEALS.get("harvest").get(), -5, 6,
                AspectList.of(ModAspects.MAN, 3).add(ModAspects.MIND, 3).add(ModAspects.PLANT, 5),
                List.of("seal_use", "infusion"), false, NodeShape.PLAIN, 2, "seal_harvest");
        entry(context, "seal_break", ModResearch.GOLEMANCY, ModItems.SEALS.get("breaker").get(), -10, 4,
                AspectList.of(ModAspects.MAN, 3).add(ModAspects.MIND, 3).add(ModAspects.ENTROPY, 5)
                        .add(ModAspects.TOOL, 5),
                List.of("control_seals", "infusion", "golem_breaker"), false, NodeShape.PLAIN, 2, "seal_breaker");
        entry(context, "seal_lumber", ModResearch.GOLEMANCY, ModItems.SEALS.get("lumber").get(), -9, 6,
                AspectList.of(ModAspects.MAN, 3).add(ModAspects.MIND, 3).add(ModAspects.PLANT, 5)
                        .add(ModAspects.TOOL, 5),
                List.of("seal_break", "biothaumic_mind"), false, NodeShape.PLAIN, 2, "seal_lumber");
        entry(context, "golem_vision", ModResearch.GOLEMANCY, ModItems.VISION_MODULE, -2, 2,
                AspectList.of(ModAspects.MECHANISM, 3).add(ModAspects.SENSES, 5).add(ModAspects.CRYSTAL, 3)
                        .add(ModAspects.MIND, 3),
                List.of("clockwork_mind"), false, NodeShape.PLAIN, 2, "vision_module");
        entry(context, "golem_climber", ModResearch.GOLEMANCY, net.minecraft.world.item.Items.FLINT, -4, 2,
                AspectList.of(ModAspects.MECHANISM, 3).add(ModAspects.BEAST, 3).add(ModAspects.MOTION, 5)
                        .add(ModAspects.MIND, 3),
                List.of("clockwork_mind"), false, NodeShape.PLAIN, 2);
        entry(context, "golem_flyer", ModResearch.GOLEMANCY, ModBlocks.LEVITATOR, -6, 2,
                AspectList.of(ModAspects.MECHANISM, 3).add(ModAspects.FLIGHT, 5).add(ModAspects.MOTION, 3)
                        .add(ModAspects.MIND, 3),
                List.of("golem_climber", "levitator"), false, NodeShape.PLAIN, 3);
        entry(context, "golem_breaker", ModResearch.GOLEMANCY, net.minecraft.world.level.block.Blocks.PISTON, -8, 2,
                AspectList.of(ModAspects.MECHANISM, 3).add(ModAspects.ENTROPY, 3).add(ModAspects.TOOL, 5)
                        .add(ModAspects.MIND, 3),
                List.of("clockwork_mind"), false, NodeShape.PLAIN, 2);
        entry(context, "biothaumic_mind", ModResearch.GOLEMANCY, ModItems.BIOTHAUMIC_MIND, 0, 5,
                AspectList.of(ModAspects.MECHANISM, 3).add(ModAspects.MIND, 3).add(ModAspects.SENSES, 3)
                        .add(ModAspects.UNDEAD, 3),
                List.of("clockwork_mind", "infusion"), false, NodeShape.PLAIN, 2, "biothaumic_mind");

        entry(context, "primal_charm", ModResearch.ARTIFICE, ModItems.PRIMAL_CHARM, 0, 1,
                AspectList.EMPTY, List.of(), true, NodeShape.MAJOR, 1,
                "primal_charm", "mirrored_glass", "mundane_amulet", "mundane_ring", "mundane_belt", "fancy_amulet",
                "fancy_ring", "fancy_belt");
        entry(context, "runic_shielding", ModResearch.ARTIFICE, ModItems.RUNIC_AMULET, -3, 6,
                AspectList.of(ModAspects.PROTECT, 6).add(ModAspects.AIR, 3).add(ModAspects.ENERGY, 3)
                        .add(ModAspects.MIND, 3),
                List.of("infusion"), false, NodeShape.PLAIN, 3, "runic_ring", "runic_amulet", "runic_girdle");
        entry(context, "runic_special", ModResearch.ARTIFICE, ModItems.CHARGED_RUNIC_RING, -2, 7,
                AspectList.of(ModAspects.ORDER, 3).add(ModAspects.PROTECT, 3).add(ModAspects.ENERGY, 6),
                List.of("runic_shielding"), false, NodeShape.PLAIN, 2, "charged_runic_ring", "revitalizing_runic_ring",
                "kinetic_runic_girdle", "emergency_runic_amulet");
        entry(context, "runic_augmentation", ModResearch.ARTIFICE, ModItems.SALIS_MUNDUS, -4, 7,
                AspectList.of(ModAspects.ORDER, 3).add(ModAspects.PROTECT, 3).add(ModAspects.EXCHANGE, 4)
                        .add(ModAspects.DESIRE, 4),
                List.of("runic_shielding"), false, NodeShape.PLAIN, 1, "runic_augmentation");
        entry(context, "verdant_heart", ModResearch.ARTIFICE, ModItems.VERDANT_HEART, 2, 8,
                AspectList.of(ModAspects.LIFE, 8).add(ModAspects.ORDER, 6).add(ModAspects.WATER, 6)
                        .add(ModAspects.PLANT, 6),
                List.of("infusion", "recharge_pedestal"), false, NodeShape.PLAIN, 3, "verdant_heart");
        entry(context, "verdant_heart_lifegiver", ModResearch.ARTIFICE, Items.GOLDEN_APPLE, 4, 8,
                AspectList.of(ModAspects.LIFE, 8).add(ModAspects.MAN, 6).add(ModAspects.DESIRE, 6)
                        .add(ModAspects.AIR, 6).add(ModAspects.PLANT, 6),
                List.of("infusion_enchantment", "verdant_heart"), false, NodeShape.PLAIN, 3,
                "verdant_heart_lifegiver", "verdant_heart_sustainer", "triple_meat_treat");
        entry(context, "thaumostatic_girdle", ModResearch.ARTIFICE, ModItems.THAUMOSTATIC_GIRDLE, -2, 0,
                AspectList.of(ModAspects.FLIGHT, 6).add(ModAspects.AIR, 3).add(ModAspects.MOTION, 6),
                List.of("thaumostatic_harness"), false, NodeShape.PLAIN, 3, "thaumostatic_girdle");
        entry(context, "vis_amulet", ModResearch.THAUMATURGY, ModItems.VIS_AMULET, 7, 0,
                AspectList.of(ModAspects.AURA, 3).add(ModAspects.ENERGY, 9).add(ModAspects.VOID, 3),
                List.of("recharge_pedestal", "infusion"), false, NodeShape.PLAIN, 2, "vis_amulet");
        entry(context, "arcane_lamp", ModResearch.ARTIFICE, ModBlocks.ARCANE_LAMP, 0, -1,
                AspectList.of(ModAspects.LIGHT, 3).add(ModAspects.SENSES, 3).add(ModAspects.DARKNESS, 3),
                List.of("primal_charm", "nitor"), false, NodeShape.PLAIN, 1, "arcane_lamp");
        entry(context, "growth_lamp", ModResearch.ARTIFICE, ModBlocks.GROWTH_LAMP, -1, -2,
                AspectList.of(ModAspects.LIGHT, 3).add(ModAspects.PLANT, 6).add(ModAspects.LIFE, 3)
                        .add(ModAspects.TOOL, 3),
                List.of("arcane_lamp"), false, NodeShape.PLAIN, 2, "growth_lamp");
        entry(context, "fertility_lamp", ModResearch.ARTIFICE, ModBlocks.FERTILITY_LAMP, 1, -2,
                AspectList.of(ModAspects.BEAST, 6).add(ModAspects.LIFE, 6).add(ModAspects.LIGHT, 3)
                        .add(ModAspects.DESIRE, 6),
                List.of("arcane_lamp"), false, NodeShape.PLAIN, 2, "fertility_lamp");
        entry(context, "hungry_chest", ModResearch.ARTIFICE, ModBlocks.HUNGRY_CHEST, 3, -1,
                AspectList.of(ModAspects.DESIRE, 3).add(ModAspects.VOID, 3),
                List.of("golem_wood"), false, NodeShape.PLAIN, 1, "hungry_chest");
        entry(context, "water_jug", ModResearch.ALCHEMY, ModBlocks.WATER_JUG, 1, 2,
                AspectList.of(ModAspects.WATER, 6).add(ModAspects.EARTH, 3).add(ModAspects.CRAFT, 3),
                List.of("crucible", "infusion"), false, NodeShape.PLAIN, 1, "water_jug");

        entry(context, "node_magnet", ModResearch.THAUMATURGY, ModItems.NODE_MAGNET, 10, 0,
                AspectList.of(ModAspects.AURA, 6).add(ModAspects.MOTION, 6).add(ModAspects.MECHANISM, 3)
                        .add(ModAspects.DESIRE, 8),
                List.of("node_stabilizer", "aura_totem_pole_pure", "biothaumic_mind", "crystal_farmer"), false,
                NodeShape.SPECIAL, 3,
                "node_magnet");

        entry(context, "node_stabilizer", ModResearch.THAUMATURGY, ModBlocks.NODE_STABILIZER, 9, 0,
                AspectList.of(ModAspects.AURA, 6).add(ModAspects.MOTION, 6).add(ModAspects.MECHANISM, 3)
                        .add(ModAspects.TRAP, 8),
                List.of("aura_totem"), false, NodeShape.PLAIN, 3, "node_stabilizer");
        entry(context, "focus_pouch", ModResearch.THAUMATURGY, ModItems.FOCUS_POUCH, 2, 0,
                AspectList.of(ModAspects.VOID, 6).add(ModAspects.TOOL, 3),
                List.of("wand"), false, NodeShape.PLAIN, 1, "focus_pouch");
        entry(context, "node_tapper1", ModResearch.THAUMATURGY, ModItems.WAND, 4, 0,
                AspectList.of(ModAspects.AURA, 6).add(ModAspects.ENERGY, 3).add(ModAspects.MOTION, 3)
                        .add(ModAspects.EXCHANGE, 3), List.of("wand"), false, NodeShape.MAJOR, 2);
        entry(context, "node_tapper2", ModResearch.THAUMATURGY, ModItems.WAND_RODS.get("greatwood"), 6, -1,
                AspectList.of(ModAspects.AURA, 6).add(ModAspects.ENERGY, 3).add(ModAspects.MOTION, 3)
                        .add(ModAspects.EXCHANGE, 3), List.of("node_tapper1"), false, NodeShape.MAJOR, 2);
        entry(context, "aura_preserve", ModResearch.THAUMATURGY, ModItems.BALANCED_SHARD, 4, -2,
                AspectList.of(ModAspects.AURA, 6).add(ModAspects.ENERGY, 3).add(ModAspects.PROTECT, 3)
                        .add(ModAspects.EXCHANGE, 3), List.of("node_tapper1"), false, NodeShape.MAJOR, 2);
        entry(context, "aura_totem", ModResearch.THAUMATURGY, ModBlocks.AURA_TOTEM_PUSH, 8, 1,
                AspectList.of(ModAspects.AURA, 6).add(ModAspects.EXCHANGE, 3).add(ModAspects.WATER, 3)
                        .add(ModAspects.AVERSION, 3),
                List.of("node_tapper2", "infusion"), false, NodeShape.SPECIAL, 3,
                "aura_totem_push", "aura_totem_pole_inner", "aura_totem_pole_outer");
        entry(context, "aura_totem_pull", ModResearch.THAUMATURGY, ModBlocks.AURA_TOTEM_PULL, 8, 0,
                AspectList.of(ModAspects.AURA, 6).add(ModAspects.EXCHANGE, 3).add(ModAspects.WATER, 3)
                        .add(ModAspects.DESIRE, 3),
                List.of("aura_totem"), false, NodeShape.PLAIN, 3, "aura_totem_pull");
        entry(context, "aura_totem_pole_pure", ModResearch.THAUMATURGY, ModBlocks.AURA_TOTEM_POLE_PURE, 9, 1,
                AspectList.of(ModAspects.AURA, 6).add(ModAspects.ORDER, 3).add(ModAspects.MOTION, 3),
                List.of("aura_totem"), false, NodeShape.PLAIN, 2, "aura_totem_pole_pure");

        entry(context, "recharge_pedestal", ModResearch.THAUMATURGY, ModBlocks.RECHARGE_PEDESTAL, 6, 1,
                AspectList.of(ModAspects.AURA, 6).add(ModAspects.ENERGY, 3).add(ModAspects.EXCHANGE, 3),
                List.of("infusion"), false, NodeShape.PLAIN, 2, "recharge_pedestal");
        entry(context, "dioptra", ModResearch.ARTIFICE, ModBlocks.DIOPTRA, 6, 1,
                AspectList.of(ModAspects.SENSES, 3).add(ModAspects.AURA, 3).add(ModAspects.ENERGY, 3)
                        .add(ModAspects.EARTH, 3),
                List.of("goggles"), false, NodeShape.PLAIN, 2, "dioptra");

        entry(context, "bath_salts", ModResearch.ALCHEMY, ModItems.BATH_SALTS, -1, 1,
                AspectList.of(ModAspects.MIND, 3).add(ModAspects.AURA, 3).add(ModAspects.ORDER, 3)
                        .add(ModAspects.LIFE, 3),
                List.of("alchemical_manufacture"), false, NodeShape.PLAIN, 2, "bath_salts");
        entry(context, "tallow", ModResearch.ALCHEMY, ModItems.TALLOW, -2, 2,
                AspectList.of(ModAspects.BEAST, 3).add(ModAspects.FIRE, 1), List.of("crucible"), false,
                NodeShape.PLAIN, 1, "tallow", "tallow_candle");
        entry(context, "essentia_transport", ModResearch.ALCHEMY, ModBlocks.ESSENTIA_INPUT, 7, 9,
                AspectList.of(ModAspects.WATER, 3).add(ModAspects.MOTION, 6).add(ModAspects.EXCHANGE, 3)
                        .add(ModAspects.DESIRE, 3).add(ModAspects.AVERSION, 3),
                List.of("thaumatorium", "infusion"), false, NodeShape.PLAIN, 2, "essentia_input", "essentia_output");
        entry(context, "thaumostatic_harness", ModResearch.ARTIFICE, ModItems.THAUMOSTATIC_HARNESS, -3, -1,
                AspectList.of(ModAspects.FLIGHT, 6).add(ModAspects.AIR, 6).add(ModAspects.MECHANISM, 3),
                List.of("levitator", "recharge_pedestal", "infusion"), false, NodeShape.SPECIAL, 3,
                "thaumostatic_harness");
        entry(context, "bone_bow", ModResearch.ARTIFICE, ModItems.BONE_BOW, -4, -1,
                AspectList.of(ModAspects.AVERSION, 3).add(ModAspects.FLIGHT, 3).add(ModAspects.UNDEAD, 3),
                List.of("arcane_workbench"), false, NodeShape.PLAIN, 1, "bone_bow");
        entry(context, "primal_arrows", ModResearch.ARTIFICE,
                ModItems.PRIMAL_ARROWS.get(CrystalType.FIRE), -4, 0,
                AspectList.of(ModAspects.AVERSION, 3).add(ModAspects.AIR, 3).add(ModAspects.FIRE, 3)
                        .add(ModAspects.WATER, 3).add(ModAspects.EARTH, 3).add(ModAspects.ORDER, 3)
                        .add(ModAspects.ENTROPY, 3),
                List.of("arcane_workbench"), false, NodeShape.PLAIN, 2, "primal_arrow_air", "primal_arrow_fire",
                "primal_arrow_water", "primal_arrow_earth", "primal_arrow_order", "primal_arrow_entropy");
        entry(context, "bottle_taint", ModResearch.ALCHEMY, ModItems.BOTTLE_TAINT, -4, 1,
                AspectList.of(ModAspects.FLUX, 5).add(ModAspects.ENERGY, 3).add(ModAspects.ENTROPY, 3)
                        .add(ModAspects.WATER, 1),
                List.of("entropic_processing"), false, NodeShape.PLAIN, 2, "bottle_taint");
        entry(context, "liquid_death", ModResearch.ALCHEMY, ModItems.LIQUID_DEATH_BUCKET, -4, 3,
                AspectList.of(ModAspects.DEATH, 5).add(ModAspects.ENTROPY, 5).add(ModAspects.WATER, 1),
                List.of("entropic_processing"), false, NodeShape.PLAIN, 2, "liquid_death");
        entry(context, "sanity_soap", ModResearch.ALCHEMY, ModItems.SANITY_SOAP, -1, -1,
                AspectList.of(ModAspects.MIND, 5).add(ModAspects.ORDER, 5).add(ModAspects.LIFE, 5)
                        .add(ModAspects.ELDRITCH, 5),
                List.of("bath_salts"), false, NodeShape.PLAIN, 1, "sanity_soap");
        entry(context, "pattern_crafter", ModResearch.GOLEMANCY, ModBlocks.PATTERN_CRAFTER, 2, 2,
                AspectList.of(ModAspects.EXCHANGE, 3).add(ModAspects.VOID, 3).add(ModAspects.CRAFT, 3)
                        .add(ModAspects.MAN, 3),
                List.of("clockwork_mind"), false, NodeShape.PLAIN, 1, "pattern_crafter");
        entry(context, "sanity_check", ModResearch.ELDRITCH, ModItems.SANITY_CHECKER, 3, -1,
                AspectList.of(ModAspects.MIND, 5).add(ModAspects.ELDRITCH, 3).add(ModAspects.SENSES, 5),
                List.of("eldritch_minor", "infusion"), false, NodeShape.PLAIN, 1, "sanity_checker");
        entry(context, "sinister_stone", ModResearch.ELDRITCH, ModItems.SINISTER_STONE, -1, 4,
                AspectList.of(ModAspects.SENSES, 3).add(ModAspects.DARKNESS, 3).add(ModAspects.ELDRITCH, 3)
                        .add(ModAspects.AURA, 3),
                List.of("goggles", "infusion", "oculus"), false, NodeShape.PLAIN, 1, "sinister_stone");
        foci(context);
        entry(context, "spa", ModResearch.ALCHEMY, ModBlocks.SPA, -2, 0,
                AspectList.of(ModAspects.WATER, 3).add(ModAspects.MECHANISM, 3).add(ModAspects.ORDER, 3),
                List.of("bath_salts"), false, NodeShape.PLAIN, 1, "spa");

        entry(context, "levitator", ModResearch.ARTIFICE, ModBlocks.LEVITATOR, -1, 0,
                AspectList.of(ModAspects.MOTION, 3).add(ModAspects.FLIGHT, 3).add(ModAspects.AIR, 3),
                List.of("primal_charm", "nitor"), false, NodeShape.PLAIN, 1, "levitator");
        entry(context, "redstone_relay", ModResearch.ARTIFICE, ModBlocks.REDSTONE_RELAY, 2, -2,
                AspectList.of(ModAspects.ENERGY, 6).add(ModAspects.AVERSION, 3).add(ModAspects.ORDER, 3),
                List.of("primal_charm"), false, NodeShape.PLAIN, 1, "redstone_relay");
        entry(context, "arcane_ear", ModResearch.ARTIFICE, ModBlocks.ARCANE_EAR, 6, 0,
                AspectList.of(ModAspects.SENSES, 3).add(ModAspects.MECHANISM, 3).add(ModAspects.AIR, 3),
                List.of("goggles"), false, NodeShape.PLAIN, 1, "arcane_ear");

        entry(context, "bellows", ModResearch.ARTIFICE, ModBlocks.BELLOWS, 2, 0,
                AspectList.of(ModAspects.AIR, 6).add(ModAspects.MECHANISM, 3).add(ModAspects.MOTION, 3),
                List.of("primal_charm"), false, NodeShape.PLAIN, 1, "bellows");

        entry(context, "barrier_stone", ModResearch.ARTIFICE, ModBlocks.BARRIER_STONE, 3, 3,
                AspectList.of(ModAspects.EARTH, 3).add(ModAspects.ENTROPY, 3).add(ModAspects.MOTION, 3),
                List.of("arcane_stone"), false, NodeShape.PLAIN, 1, "barrier_stone");
        entry(context, "travel_stone", ModResearch.ARTIFICE, ModBlocks.TRAVEL_STONE, 4, 3,
                AspectList.of(ModAspects.EARTH, 3).add(ModAspects.AIR, 3).add(ModAspects.MOTION, 3),
                List.of("arcane_stone"), false, NodeShape.PLAIN, 1, "travel_stone");
        entry(context, "arcane_stone", ModResearch.ARTIFICE, ModBlocks.ARCANE_STONE.block(), 3, 2,
                AspectList.EMPTY, List.of(), true, NodeShape.PLAIN, 1, "arcane_stone",
                "arcane_stone_bricks_from_arcane_stone");
    }

    private static void category(BootstrapContext<ResearchCategory> context, ResourceKey<ResearchCategory> key,
            String name, int order) {
        category(context, key, name, order, null);
    }

    private static void category(BootstrapContext<ResearchCategory> context, ResourceKey<ResearchCategory> key,
            String name, int order, String unlockedBy) {
        context.register(key, new ResearchCategory(Alchemia.id("textures/gui/research_category/" + name + ".png"),
                Alchemia.id("textures/gui/research_background/" + name + ".png"), order,
                java.util.Optional.ofNullable(unlockedBy).map(Alchemia::id)));
    }

    private static void foci(BootstrapContext<ResearchEntry> context) {
        focus(context, "fire", 3, 7, AspectList.of(ModAspects.FIRE, 6).add(ModAspects.AIR, 3)
                .add(ModAspects.ENERGY, 3), List.of("wand"), 1);
        focus(context, "shock", 4, 6, AspectList.of(ModAspects.AIR, 6).add(ModAspects.FIRE, 3)
                .add(ModAspects.ENERGY, 3), List.of("focus_fire"), 1);
        focus(context, "grapple", 5, 6, AspectList.of(ModAspects.MOTION, 6).add(ModAspects.AIR, 3)
                .add(ModAspects.WATER, 3), List.of("focus_shock"), 1);
        focus(context, "frost", 5, 7, AspectList.of(ModAspects.WATER, 6).add(ModAspects.COLD, 3)
                .add(ModAspects.ENERGY, 3), List.of("focus_fire"), 1);
        focus(context, "excavation", 5, 8, AspectList.of(ModAspects.EARTH, 6).add(ModAspects.ENTROPY, 3)
                .add(ModAspects.ENERGY, 3), List.of("focus_fire"), 2);
        focus(context, "trade", 4, 9, AspectList.of(ModAspects.EXCHANGE, 6).add(ModAspects.EARTH, 3)
                .add(ModAspects.ENERGY, 3), List.of("focus_fire"), 2);
        focus(context, "builder", 3, 10, AspectList.of(ModAspects.TOOL, 6).add(ModAspects.EARTH, 3)
                .add(ModAspects.ORDER, 3).add(ModAspects.MOTION, 3), List.of("focus_trade"), 2);
        focus(context, "hole", 5, 10, AspectList.of(ModAspects.MOTION, 3).add(ModAspects.ENTROPY, 3)
                .add(ModAspects.ELDRITCH, 6).add(ModAspects.AIR, 3),
                List.of("focus_trade", "focus_excavation", "infusion"), 2);
        focus(context, "hellbat", 1, 7, AspectList.of(ModAspects.MOTION, 3).add(ModAspects.BEAST, 6)
                .add(ModAspects.FIRE, 3).add(ModAspects.ENERGY, 3), List.of("focus_fire", "infusion"), 2);
        entry(context, "vampire_bats", ModResearch.THAUMATURGY, ModItems.FOCI.get("hellbat"), 0, 8,
                AspectList.of(ModAspects.DESIRE, 5).add(ModAspects.LIFE, 5).add(ModAspects.ENERGY, 5),
                List.of("focus_hellbat", "focal_manipulator"), false, NodeShape.PLAIN, 1);
        entry(context, "staff_primal", ModResearch.ELDRITCH, ModItems.WAND_RODS.get("primal_staff"), 6, 2,
                AspectList.of(ModAspects.AIR, 9).add(ModAspects.EARTH, 9).add(ModAspects.FIRE, 9)
                        .add(ModAspects.WATER, 9).add(ModAspects.ORDER, 9).add(ModAspects.ENTROPY, 9)
                        .add(ModAspects.TOOL, 9).add(ModAspects.ENERGY, 12),
                List.of("focus_primal"), false, NodeShape.PLAIN, 3, "wand_rod_primal_staff");
        entry(context, "focus_primal", ModResearch.ELDRITCH, ModItems.FOCI.get("primal"), 3, 1,
                AspectList.of(ModAspects.AIR, 6).add(ModAspects.WATER, 6).add(ModAspects.FIRE, 6)
                        .add(ModAspects.EARTH, 6).add(ModAspects.ORDER, 6).add(ModAspects.ENTROPY, 6)
                        .add(ModAspects.ENERGY, 6),
                List.of("eldritch_minor"), false, NodeShape.PLAIN, 2, "focus_primal");
    }

    private static void focus(BootstrapContext<ResearchEntry> context, String name, int column, int row,
            AspectList requirements, List<String> parents, int complexity) {
        entry(context, "focus_" + name, ModResearch.THAUMATURGY, ModItems.FOCI.get(name), column, row, requirements,
                parents, false, name.equals("fire") ? NodeShape.MAJOR : NodeShape.PLAIN, complexity, "focus_" + name);
    }

    private static ResearchPage construct(AspectList cost, int wide, int tall, int deep, String gate,
            ItemLike... blocks) {
        return new ResearchPage.Construct(cost, wide, tall, deep,
                java.util.Arrays.stream(blocks).map(block -> BuiltInRegistries.ITEM.getKey(block.asItem())).toList(),
                java.util.Optional.ofNullable(gate).map(Alchemia::id));
    }

    private static List<ResearchPage> altar(ItemLike stone, ItemLike pedestal, AspectList cost, String gate) {
        ItemLike air = Items.AIR;
        return List.of(construct(cost, 3, 3, 3, gate,
                air, air, air, air, ModBlocks.INFUSION_MATRIX.get(), air, air, air, air,
                stone, air, stone, air, air, air, stone, air, stone,
                stone, air, stone, air, pedestal, air, stone, air, stone));
    }

    // original's compound recipes, with the vis a wand spends to finish them
    private static java.util.Map<String, List<ResearchPage>> constructs() {
        ItemLike air = Items.AIR;
        ItemLike brick = Blocks.NETHER_BRICKS;
        ItemLike rock = Blocks.OBSIDIAN;
        AspectList altarCost = AspectList.eachPrimal(75);
        List<ResearchPage> altars = new java.util.ArrayList<>();
        altars.addAll(altar(ModBlocks.ARCANE_STONE.block().get(), ModBlocks.ARCANE_PEDESTAL.get(), altarCost, null));
        altars.addAll(altar(ModBlocks.ANCIENT_STONE.block().get(), ModBlocks.ANCIENT_PEDESTAL.get(), altarCost,
                Clues.ANCIENT_STONE.getPath()));
        altars.addAll(altar(ModBlocks.ELDRITCH_STONE.block().get(), ModBlocks.ELDRITCH_PEDESTAL.get(),
                AspectList.eachPrimal(75).withAmount(ModAspects.ORDER, 125), Clues.ELDRITCH_STONE.getPath()));
        return java.util.Map.of(
                "aspects", List.of(
                        construct(AspectList.EMPTY, 1, 2, 1, null, ModItems.WAND.get(), Blocks.BOOKSHELF),
                        construct(AspectList.EMPTY, 1, 2, 1, null, ModItems.SCRIBING_TOOLS.get(),
                                ModBlocks.WOODEN_TABLE.get())),
                "arcane_workbench", List.of(construct(AspectList.EMPTY, 1, 2, 1, null, ModItems.WAND.get(),
                        ModBlocks.WOODEN_TABLE.get())),
                "crucible", List.of(construct(AspectList.EMPTY, 1, 2, 1, null, ModItems.WAND.get(), Blocks.CAULDRON)),
                "infernal_furnace", List.of(construct(AspectList.of(ModAspects.FIRE, 50).add(ModAspects.EARTH, 50),
                        3, 3, 3, null,
                        brick, rock, brick, rock, air, rock, brick, rock, brick,
                        brick, rock, brick, rock, Items.LAVA_BUCKET, Blocks.IRON_BARS, brick, rock, brick,
                        brick, rock, brick, rock, rock, rock, brick, rock, brick)),
                "infusion", altars,
                "thaumatorium", List.of(construct(AspectList.of(ModAspects.FIRE, 50).add(ModAspects.ORDER, 100)
                        .add(ModAspects.WATER, 100), 1, 3, 1, null, ModBlocks.ALCHEMICAL_CONSTRUCT.get(),
                        ModBlocks.ALCHEMICAL_CONSTRUCT.get(), ModBlocks.CRUCIBLE.get())),
                "clockwork_mind", List.of(construct(AspectList.of(ModAspects.FIRE, 50).add(ModAspects.ORDER, 50)
                        .add(ModAspects.AIR, 50), 2, 2, 2, null,
                        air, Blocks.IRON_BARS, air, air, Blocks.CAULDRON, Blocks.PISTON, Blocks.ANVIL,
                        ModBlocks.STONE_TABLE.get())));
    }

    private static List<ResearchPage> pages(String name, String... recipes) {
        List<ResearchPage> pages = new java.util.ArrayList<>();
        pages.add(new ResearchPage.Text("research." + Alchemia.MODID + "." + name + ".page"));
        pages.addAll(constructs().getOrDefault(name, List.of()));
        for (String recipe : recipes) {
            pages.add(new ResearchPage.Recipe(Alchemia.id(recipe)));
        }
        pages.addAll(LATER_PAGES.getOrDefault(name, List.of()));
        return List.copyOf(pages);
    }

    private static void entry(BootstrapContext<ResearchEntry> context, String name, ResourceKey<ResearchCategory> category,
            ItemLike icon, int column, int row, AspectList requirements, List<String> parents, boolean autoUnlock,
            NodeShape shape, int complexity, String... recipes) {
        register(context, name, category, icon, column, row, requirements, parents, autoUnlock, shape, complexity,
                false, recipes);
    }

    private static void hidden(BootstrapContext<ResearchEntry> context, String name,
            ResourceKey<ResearchCategory> category, ItemLike icon, int column, int row, NodeShape shape,
            String... recipes) {
        register(context, name, category, icon, column, row, AspectList.EMPTY, List.of(), false, shape, 1, true,
                recipes);
    }

    private static final java.util.Map<String, Integer> WARP = java.util.Map.ofEntries(
            java.util.Map.entry("focus_hellbat", 2), java.util.Map.entry("vampire_bats", 2),
            java.util.Map.entry("infernal_furnace", 2), java.util.Map.entry("brain_jar", 3),
            java.util.Map.entry("biothaumic_mind", 3), java.util.Map.entry("seal_guard", 1),
            java.util.Map.entry("seal_butcher", 1), java.util.Map.entry("crimson", 3),
            java.util.Map.entry("oculus", 6), java.util.Map.entry("sinister_stone", 2),
            java.util.Map.entry("focus_primal", 2), java.util.Map.entry("liquid_death", 3),
            // original's value
            java.util.Map.entry("bottle_taint", 1), java.util.Map.entry("researcher2", 1),
            java.util.Map.entry("node_magnet", 3), java.util.Map.entry("staff_primal", 3));
    private static final java.util.Set<String> CONCEALED = java.util.Set.of("focus_hole", "focus_hellbat",
            "thaumostatic_harness", "infusion_boost", "mirror", "growth_lamp", "fertility_lamp", "bone_bow",
            "primal_arrows", "brain_jar", "liquid_death", "bottle_taint", "taint_shard", "ethereal_bloom",
            "bath_salts", "jar_label", "clockwork_mind", "biothaumic_mind", "control_seals", "golem_flyer",
            "autocaster", "crystal_farmer", "oculus", "sinister_stone", "primal_crusher", "void_robes",
            "focus_primal", "basic_turret", "node_magnet", "staff_primal", "primordial_pearl", "outer_revelations",
            "void_smelter");

    // original's secondary research, bought with levels
    private static final java.util.Set<String> SECONDARY = java.util.Set.of("aura_totem_pull", "aura_totem_pole_pure",
            "vampire_bats", "focus_pouch", "staves_primal", "staff_silverwood", "charger", "redstone_relay",
            "thaumostatic_girdle", "bellows", "infusion_boost", "runic_special", "mirror_hand", "mirror_essentia",
            "arcane_lamp", "verdant_heart_lifegiver", "alchemical_duplication", "alchemical_manufacture",
            "entropic_processing", "taint_shard", "spa", "jar_void", "tubes", "tube_filter", "hungry_chest",
            "golem_iron", "golem_clay", "golem_brass", "golem_thaumium", "golem_void", "golem_direct", "seal_store",
            "seal_empty", "seal_provide", "seal_butcher", "seal_break", "seal_lumber", "golem_combat",
            "advanced_turret", "outer_revelations");

    private static final java.util.Map<String, List<String>> CLUES = java.util.Map.ofEntries(
            java.util.Map.entry("focus_hellbat", List.of(Clues.BAT.getPath())),
            java.util.Map.entry("thaumostatic_harness", List.of("volatus")),
            java.util.Map.entry("mirror", List.of(Clues.TELEPORT.getPath())),
            java.util.Map.entry("growth_lamp", List.of("herba")),
            java.util.Map.entry("fertility_lamp", List.of("victus", "desiderium")),
            java.util.Map.entry("bone_bow", List.of(Clues.BONE.getPath(), Clues.BOW.getPath())),
            java.util.Map.entry("primal_arrows", List.of(Clues.ARROW.getPath())),
            java.util.Map.entry("brain_jar", List.of(Clues.BRAIN.getPath())),
            java.util.Map.entry("liquid_death", List.of("mortuus")),
            java.util.Map.entry("bottle_taint", List.of("vitium")),
            java.util.Map.entry("taint_shard", List.of("vitium", Clues.TAINT_SHARD.getPath())),
            java.util.Map.entry("ethereal_bloom", List.of("vitium")),
            java.util.Map.entry("bath_salts", List.of(Clues.BATH_SALTS.getPath())),
            java.util.Map.entry("biothaumic_mind", List.of(Clues.BRAIN.getPath())),
            java.util.Map.entry("autocaster", List.of(Clues.DISPENSER.getPath())),
            java.util.Map.entry("basic_turret", List.of(Clues.DISPENSER.getPath())),
            java.util.Map.entry("staff_primal", List.of("staff_silverwood", "staves_primal")),
            java.util.Map.entry("crystal_farmer", List.of("vitreus")),
            java.util.Map.entry("levitator", List.of("volatus")),
            java.util.Map.entry("arcane_lamp", List.of("lux")),
            java.util.Map.entry("fortress_armour", List.of("praemunio")),
            java.util.Map.entry("pattern_crafter", List.of("fabrico")),
            java.util.Map.entry("primordial_pearl", List.of(Clues.PRIMORDIAL_PEARL.getPath())),
            java.util.Map.entry("outer_revelations", List.of(Clues.OUTER_BOSS.getPath(), Clues.OUTER_MOB.getPath(),
                    Clues.ANCIENT_STONE.getPath(), Clues.ELDRITCH_STONE.getPath(), Clues.GLYPHED_STONE.getPath())));

    private static final java.util.Map<String, List<String>> HIDDEN_PARENTS = java.util.Map.of(
            "focal_manipulator", List.of("focus_frost", "focus_shock", "focus_trade", "focus_excavation"),
            "charger", List.of("wand_rod_greatwood"),
            "growth_lamp", List.of("infusion"),
            "fertility_lamp", List.of("infusion"),
            "primal_crusher", List.of("void_metal", "elemental_tools"),
            "void_smelter", List.of("void_metal"));

    private static final java.util.Map<String, List<ResearchPage>> LATER_PAGES = java.util.Map.of(
            "node_magnet", List.of(new ResearchPage.Text("research.alchemia.node_magnet.danger",
                    java.util.Optional.of(Clues.NODE_MAGNET_DANGER))));

    private static final java.util.Map<String, List<String>> SIBLINGS = java.util.Map.of(
            "crimson", List.of("eldritch_minor"),
            "clockwork_mind", List.of("control_seals"),
            "control_seals", List.of("seal_collect"),
            "distillation", List.of("jar_label"),
            "basic_golemancy", List.of("golem_wood"));

    private static void register(BootstrapContext<ResearchEntry> context, String name,
            ResourceKey<ResearchCategory> category, ItemLike icon, int column, int row, AspectList requirements,
            List<String> parents, boolean autoUnlock, NodeShape shape, int complexity, boolean hidden,
            String... recipes) {
        context.register(ResourceKey.create(ModResearch.ENTRY_KEY, Alchemia.id(name)),
                new ResearchEntry(category, requirements, column, row,
                        BuiltInRegistries.ITEM.wrapAsHolder(icon.asItem()),
                        parents.stream().map(Alchemia::id).toList(),
                        pages(name, recipes),
                        autoUnlock, shape, complexity, hidden || CONCEALED.contains(name), WARP.getOrDefault(name, 0),
                        SIBLINGS.getOrDefault(name, List.of()).stream().map(Alchemia::id).toList(),
                        CLUES.getOrDefault(name, List.of()).stream().map(Alchemia::id).toList(),
                        HIDDEN_PARENTS.getOrDefault(name, List.of()).stream().map(Alchemia::id).toList(),
                        SECONDARY.contains(name)));
    }
}
