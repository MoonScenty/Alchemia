package me.moonscenty.alchemia.datagen;

import java.util.List;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.block.CrystalType;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.research.ModResearch;
import me.moonscenty.alchemia.research.NodeShape;
import me.moonscenty.alchemia.research.ResearchPage;
import me.moonscenty.alchemia.research.ResearchCategory;
import me.moonscenty.alchemia.research.ResearchEntry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.DyeColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

/**
 * The branches of study, and the research that exists so far.
 * <p>
 * Only entries whose subject is already built are written here. The rest arrive with the systems they describe, so the
 * book never points at something that does not exist.
 */
public class ModResearchProvider {
    public static void categories(BootstrapContext<ResearchCategory> context) {
        // Thaumaturgy is called arcana here, following the rule on names carried over from the original
        category(context, ModResearch.BASICS, "basics", 0);
        category(context, ModResearch.ARCANA, "arcana", 1);
        category(context, ModResearch.ALCHEMY, "alchemy", 2);
        category(context, ModResearch.ARTIFICE, "artifice", 3);
        category(context, ModResearch.GOLEMANCY, "golemancy", 4);
        category(context, ModResearch.ELDRITCH, "eldritch", 5);
    }

    public static void entries(BootstrapContext<ResearchEntry> context) {
        // where everything starts: the six primals are plain enough to see without help
        entry(context, "aspects", ModResearch.BASICS, ModItems.ALCHEMOMETER, 0, 0,
                AspectList.EMPTY, List.of(), true, NodeShape.MAJOR, 1);

        entry(context, "amber", ModResearch.BASICS, ModItems.AMBER, -2, 1,
                AspectList.of(ModAspects.CRYSTAL, 2).add(ModAspects.TRAP, 2), List.of("aspects"), false, NodeShape.PLAIN, 1, "amber_from_ore_smelting", "amber_block_from_amber");
        entry(context, "quicksilver", ModResearch.ALCHEMY, ModItems.QUICKSILVER, 0, 1,
                AspectList.of(ModAspects.METAL, 3).add(ModAspects.EXCHANGE, 2), List.of("aspects"), false, NodeShape.PLAIN, 2, "quicksilver_from_raw_cinnabar_smelting", "quicksilver_from_shimmerleaf");
        entry(context, "vis_crystals", ModResearch.ARCANA, ModItems.BALANCED_SHARD, 2, 1,
                AspectList.of(ModAspects.CRYSTAL, 4).add(ModAspects.AURA, 2), List.of("aspects"), false, NodeShape.SPECIAL, 2);
        entry(context, "greatwood", ModResearch.BASICS, ModBlocks.GREATWOOD.sapling(), -1, 2,
                AspectList.of(ModAspects.PLANT, 4).add(ModAspects.LIFE, 2), List.of("aspects"), false, NodeShape.PLAIN, 1, "greatwood_planks");
        entry(context, "silverwood", ModResearch.ARCANA, ModBlocks.SILVERWOOD.sapling(), 1, 2,
                AspectList.of(ModAspects.PLANT, 4).add(ModAspects.AURA, 4), List.of("greatwood"), false, NodeShape.SPECIAL, 2, "silverwood_planks");
        entry(context, "warp", ModResearch.ELDRITCH, Items.ENDER_PEARL, 0, 3,
                AspectList.of(ModAspects.ELDRITCH, 4).add(ModAspects.FLUX, 2), List.of("vis_crystals"), false, NodeShape.MAJOR, 3);

        // --- the wand and the bench it is worked at -------------------------------------------------------

        // given for nothing: a wand is what the whole branch is worked with, so there is no first wand to earn
        entry(context, "wand", ModResearch.ARCANA, ModItems.WAND, 3, 0,
                AspectList.EMPTY, List.of(), true, NodeShape.MAJOR, 1, "wand_cap_iron", "wand");
        entry(context, "arcane_workbench", ModResearch.ARCANA, ModBlocks.ARCANE_WORKBENCH, 4, 1,
                AspectList.of(ModAspects.CRAFT, 4).add(ModAspects.AURA, 2), List.of("wand"), false, NodeShape.MAJOR, 2);
        entry(context, "charger", ModResearch.ARCANA, ModBlocks.ARCANE_WORKBENCH_CHARGER, 5, 2,
                AspectList.of(ModAspects.AURA, 4).add(ModAspects.ENERGY, 4).add(ModAspects.CRYSTAL, 2),
                List.of("arcane_workbench"), false, NodeShape.PLAIN, 2);

        entry(context, "wand_cap_gold", ModResearch.ARCANA, ModItems.WAND_CAPS.get("gold"), 4, 3,
                AspectList.of(ModAspects.METAL, 3).add(ModAspects.DESIRE, 3).add(ModAspects.TOOL, 3),
                List.of("arcane_workbench"), false, NodeShape.PLAIN, 2, "wand_cap_gold");
        entry(context, "wand_cap_brass", ModResearch.ARCANA, ModItems.WAND_CAPS.get("brass"), 5, 4,
                AspectList.of(ModAspects.METAL, 3).add(ModAspects.ENERGY, 3).add(ModAspects.TOOL, 3),
                List.of("wand_cap_gold"), false, NodeShape.PLAIN, 2, "wand_cap_brass");
        entry(context, "wand_cap_alchemium", ModResearch.ARCANA, ModItems.WAND_CAPS.get("alchemium"), 6, 5,
                AspectList.of(ModAspects.METAL, 6).add(ModAspects.ENERGY, 6).add(ModAspects.TOOL, 3)
                        .add(ModAspects.AURA, 3),
                // cast at a workbench and finished on an altar, so the altar has to come first
                List.of("wand_cap_brass", "infusion"), false, NodeShape.SPECIAL, 3,
                "wand_cap_alchemium_inert", "wand_cap_alchemium");

        entry(context, "wand_rod_greatwood", ModResearch.ARCANA, ModItems.WAND_RODS.get("greatwood"), 2, 3,
                AspectList.of(ModAspects.TOOL, 3).add(ModAspects.PLANT, 6).add(ModAspects.ENERGY, 3),
                List.of("arcane_workbench", "greatwood"), false, NodeShape.PLAIN, 2, "wand_rod_greatwood");

        // --- the crucible ---------------------------------------------------------------------------------

        entry(context, "crucible", ModResearch.ALCHEMY, ModBlocks.CRUCIBLE, 0, 2,
                AspectList.of(ModAspects.WATER, 4).add(ModAspects.FIRE, 4).add(ModAspects.EXCHANGE, 2),
                List.of("wand"), false, NodeShape.MAJOR, 2, "morphic_resonator");
        entry(context, "metallurgy", ModResearch.ALCHEMY, ModItems.ALCHEMIUM_INGOT, -1, 3,
                AspectList.of(ModAspects.METAL, 6).add(ModAspects.EXCHANGE, 4).add(ModAspects.FIRE, 2),
                List.of("crucible"), false, NodeShape.PLAIN, 3,
                "alchemium_ingot_from_iron", "brass_ingot_from_iron");

        // both are the crucible turned on a fire rather than on a metal: one holds the heat, the other the light
        entry(context, "alumentum", ModResearch.ALCHEMY, ModItems.ALUMENTUM, -3, 2,
                AspectList.of(ModAspects.FIRE, 6).add(ModAspects.ENERGY, 4).add(ModAspects.ENTROPY, 2),
                List.of("crucible"), false, NodeShape.PLAIN, 2, "alumentum");
        entry(context, "nitor", ModResearch.ALCHEMY, ModBlocks.NITOR.get(DyeColor.WHITE), -3, 4,
                AspectList.of(ModAspects.LIGHT, 6).add(ModAspects.FIRE, 4).add(ModAspects.ENERGY, 4),
                List.of("crucible"), false, NodeShape.PLAIN, 2, "nitor");

        // --- distilling, and the plumbing that follows from it ---------------------------------------------

        entry(context, "distillation", ModResearch.ALCHEMY, ModBlocks.ESSENTIA_SMELTER, 1, 4,
                AspectList.of(ModAspects.FIRE, 4).add(ModAspects.WATER, 4).add(ModAspects.CRAFT, 4),
                List.of("crucible"), false, NodeShape.MAJOR, 3, "filter", "essentia_smelter", "alembic");
        entry(context, "jar_label", ModResearch.ALCHEMY, ModBlocks.JAR, 0, 5,
                AspectList.of(ModAspects.CRYSTAL, 4).add(ModAspects.VOID, 4).add(ModAspects.TRAP, 2),
                List.of("distillation"), false, NodeShape.PLAIN, 2,
                "jar", "phial", "jar_label", "jar_brace");
        entry(context, "tubes", ModResearch.ALCHEMY, ModBlocks.TUBE, 2, 5,
                AspectList.of(ModAspects.WATER, 3).add(ModAspects.EXCHANGE, 6),
                List.of("distillation"), false, NodeShape.PLAIN, 2, "tube", "tube_valve");
        entry(context, "tube_filter", ModResearch.ALCHEMY, ModBlocks.TUBE_FILTER, 3, 6,
                AspectList.of(ModAspects.WATER, 3).add(ModAspects.EXCHANGE, 6).add(ModAspects.ORDER, 3),
                List.of("tubes"), false, NodeShape.PLAIN, 3,
                "tube_filter", "tube_restrict", "tube_oneway", "tube_buffer");

        // the two things a run of pipe can be finished with: one that takes essentia apart, one that sets it hard
        entry(context, "centrifuge", ModResearch.ALCHEMY, ModBlocks.CENTRIFUGE, 5, 5,
                AspectList.of(ModAspects.FLUX, 3).add(ModAspects.ENERGY, 3).add(ModAspects.EXCHANGE, 3)
                        .add(ModAspects.CRAFT, 3),
                List.of("tube_filter"), false, NodeShape.PLAIN, 2, "centrifuge");
        entry(context, "crystallizer", ModResearch.ALCHEMY, ModBlocks.CRYSTALLIZER, 5, 6,
                AspectList.of(ModAspects.WATER, 5).add(ModAspects.CRYSTAL, 5).add(ModAspects.EXCHANGE, 3)
                        .add(ModAspects.ENERGY, 5),
                List.of("tube_filter"), false, NodeShape.PLAIN, 1, "crystallizer");

        // what a smelter can be made better with: pumps and vents against it, and two better smelters
        entry(context, "improved_distillation", ModResearch.ALCHEMY, ModBlocks.SMELTER_AUX, 4, 7,
                AspectList.of(ModAspects.FIRE, 5).add(ModAspects.TOOL, 3).add(ModAspects.WATER, 5)
                        .add(ModAspects.CRAFT, 5),
                List.of("tube_filter"), false, NodeShape.PLAIN, 2, "smelter_aux", "smelter_vent");
        entry(context, "alchemium_smelter", ModResearch.ALCHEMY, ModBlocks.ALCHEMIUM_SMELTER, 2, 7,
                AspectList.of(ModAspects.FIRE, 3).add(ModAspects.WATER, 3).add(ModAspects.CRAFT, 3),
                List.of("distillation", "improved_distillation"), false, NodeShape.PLAIN, 1, "alchemium_smelter");
        entry(context, "void_smelter", ModResearch.ALCHEMY, ModBlocks.VOID_SMELTER, 2, 8,
                AspectList.of(ModAspects.FIRE, 3).add(ModAspects.WATER, 3).add(ModAspects.CRAFT, 3),
                List.of("alchemium_smelter", "void_metal"), false, NodeShape.PLAIN, 1, "void_smelter");

        // the last of the caps hangs off this one, since void metal is the only thing it can be drawn from
        entry(context, "void_metal", ModResearch.ALCHEMY, ModItems.VOID_INGOT, -2, 4,
                AspectList.of(ModAspects.VOID, 6).add(ModAspects.DARKNESS, 6).add(ModAspects.METAL, 4)
                        .add(ModAspects.ELDRITCH, 2),
                List.of("metallurgy"), false, NodeShape.SPECIAL, 3,
                "void_seed", "void_ingot", "void_plate", "wand_cap_void_inert", "wand_cap_void");

        // what either metal is good for once there is enough of it to forge with
        entry(context, "alchemium_gear", ModResearch.ARTIFICE, ModItems.METAL_TOOLS.get("alchemium_pickaxe"),
                -1, 5, AspectList.of(ModAspects.METAL, 6).add(ModAspects.TOOL, 6).add(ModAspects.PROTECT, 4),
                List.of("metallurgy"), false, NodeShape.PLAIN, 2,
                "alchemium_pickaxe", "alchemium_axe", "alchemium_shovel", "alchemium_sword", "alchemium_hoe",
                "alchemium_helmet", "alchemium_chestplate", "alchemium_leggings", "alchemium_boots");
        entry(context, "void_gear", ModResearch.ARTIFICE, ModItems.METAL_TOOLS.get("void_pickaxe"),
                -3, 5, AspectList.of(ModAspects.VOID, 6).add(ModAspects.TOOL, 6).add(ModAspects.PROTECT, 4)
                        .add(ModAspects.ELDRITCH, 2),
                List.of("void_metal", "alchemium_gear"), false, NodeShape.PLAIN, 3,
                "void_pickaxe", "void_axe", "void_shovel", "void_sword", "void_hoe",
                "void_helmet", "void_chestplate", "void_leggings", "void_boots");

        // --- the altar, and what can only be made on one --------------------------------------------------

        entry(context, "infusion", ModResearch.ARCANA, ModBlocks.INFUSION_MATRIX, 7, 2,
                AspectList.of(ModAspects.AURA, 6).add(ModAspects.CRAFT, 6).add(ModAspects.ORDER, 4)
                        .add(ModAspects.ENERGY, 4),
                List.of("arcane_workbench", "vis_crystals"), false, NodeShape.MAJOR, 3,
                "infusion_matrix", "arcane_pedestal");
        // what an altar is built on rather than what it makes: two stones, and a choice between them
        entry(context, "infusion_boost", ModResearch.ARTIFICE, ModBlocks.INFUSION_SPEED_STONE, 5, 4,
                AspectList.of(ModAspects.ENERGY, 6).add(ModAspects.AIR, 3).add(ModAspects.WATER, 3)
                        .add(ModAspects.EXCHANGE, 3),
                List.of("infusion", "alumentum", "nitor"), false, NodeShape.PLAIN, 3,
                "infusion_speed_stone", "infusion_cost_stone");

        entry(context, "goggles", ModResearch.ARTIFICE, ModItems.GOGGLES, 5, 2,
                AspectList.of(ModAspects.SENSES, 3).add(ModAspects.AURA, 3).add(ModAspects.ENERGY, 3),
                List.of("arcane_workbench"), false, NodeShape.PLAIN, 2, "goggles_of_revealing");

        // cloth with the six primals worked into it, and what is cut from it. A robe is not armour: what it is
        // worth is that a wand spends less while it is on
        entry(context, "enchanted_fabric", ModResearch.ARTIFICE, ModItems.ENCHANTED_FABRIC, 6, 3,
                AspectList.of(ModAspects.CRAFT, 4).add(ModAspects.AURA, 3).add(ModAspects.BEAST, 2),
                List.of("arcane_workbench"), false, NodeShape.PLAIN, 2, "enchanted_fabric");
        entry(context, "robes", ModResearch.ARTIFICE, ModItems.ROBES.get("cloth_chest"), 7, 2,
                AspectList.of(ModAspects.AURA, 4).add(ModAspects.PROTECT, 4).add(ModAspects.CRAFT, 3)
                        .add(ModAspects.ENERGY, 2),
                List.of("enchanted_fabric"), false, NodeShape.PLAIN, 2,
                "cloth_chest", "cloth_legs", "cloth_boots");
        entry(context, "void_robes", ModResearch.ARTIFICE, ModItems.ROBES.get("void_robe_chest"), 8, 3,
                AspectList.of(ModAspects.VOID, 6).add(ModAspects.AURA, 4).add(ModAspects.PROTECT, 4)
                        .add(ModAspects.ELDRITCH, 3),
                List.of("robes", "void_gear", "infusion"), false, NodeShape.SPECIAL, 3,
                "void_robe_helm", "void_robe_chest", "void_robe_legs");

        // a finished suit of alchemium taken apart again and built up into plate. The most an altar will do
        // for armour, and the only armour in the mod that is better than the metal it is made of
        entry(context, "fortress_armour", ModResearch.ARTIFICE, ModItems.FORTRESS.get("fortress_chest"), 6, 5,
                AspectList.of(ModAspects.METAL, 6).add(ModAspects.PROTECT, 6).add(ModAspects.TOOL, 4)
                        .add(ModAspects.ENERGY, 4),
                List.of("alchemium_gear", "infusion"), false, NodeShape.SPECIAL, 3,
                "fortress_helm", "fortress_chest", "fortress_legs", "helm_goggles",
                "mask_grinning_devil", "mask_angry_ghost", "mask_sipping_fiend");

        entry(context, "traveller_boots", ModResearch.ARTIFICE, ModItems.TRAVELLER_BOOTS, 7, 4,
                AspectList.of(ModAspects.MOTION, 3).add(ModAspects.EARTH, 3).add(ModAspects.FLIGHT, 3)
                        .add(ModAspects.WATER, 3),
                List.of("infusion"), false, NodeShape.PLAIN, 2, "traveller_boots");

        // what an altar can put on somebody else's tools, which is the only enchanting in the mod
        entry(context, "infusion_enchantment", ModResearch.ARCANA, Items.ENCHANTED_BOOK, 9, 2,
                AspectList.of(ModAspects.AURA, 6).add(ModAspects.TOOL, 6).add(ModAspects.CRAFT, 4)
                        .add(ModAspects.MIND, 4),
                List.of("infusion"), false, NodeShape.PLAIN, 3,
                "infusion_enchantment_collector", "infusion_enchantment_destructive",
                "infusion_enchantment_burrowing", "infusion_enchantment_refining", "infusion_enchantment_sounding",
                "infusion_enchantment_arcing", "infusion_enchantment_essence");

        entry(context, "wand_rods", ModResearch.ARCANA, ModItems.WAND_RODS.get("silverwood"), 8, 4,
                AspectList.of(ModAspects.TOOL, 6).add(ModAspects.AURA, 6).add(ModAspects.ENERGY, 6),
                List.of("infusion", "silverwood"), false, NodeShape.PLAIN, 3,
                "wand_rod_obsidian", "wand_rod_ice", "wand_rod_quartz", "wand_rod_reed",
                "wand_rod_blaze", "wand_rod_bone", "wand_rod_silverwood");

        // where a focus is worked on. It wants an altar's worth of study and a wand to have a focus on in the
        // first place, and from there every one of the twenty-three is reached through this one entry
        entry(context, "focal_manipulator", ModResearch.ARCANA, ModBlocks.FOCAL_MANIPULATOR, 2, 5,
                AspectList.of(ModAspects.ENERGY, 8).add(ModAspects.TOOL, 8).add(ModAspects.CRAFT, 5)
                        .add(ModAspects.CRYSTAL, 5),
                List.of("arcane_workbench", "arcane_stone"), false, NodeShape.MAJOR, 2, "focal_manipulator");

        // a primal bound into each of the alchemium tools, with what that element is already worked in
        entry(context, "elemental_tools", ModResearch.ARTIFICE, ModItems.ELEMENTAL_TOOLS.get("elemental_axe"), 4, 6,
                AspectList.of(ModAspects.TOOL, 6).add(ModAspects.AVERSION, 6).add(ModAspects.AIR, 3)
                        .add(ModAspects.FIRE, 3).add(ModAspects.WATER, 3).add(ModAspects.EARTH, 3)
                        .add(ModAspects.ORDER, 3).add(ModAspects.ENTROPY, 3),
                List.of("alchemium_gear", "infusion", "vis_crystals"), false, NodeShape.PLAIN, 2,
                "elemental_axe", "elemental_pickaxe", "elemental_sword", "elemental_shovel", "elemental_hoe");
        // filed under eldritch, as the original filed it: it is the first thing beaten round something that
        // should not hold it
        entry(context, "primal_crusher", ModResearch.ELDRITCH, ModItems.PRIMAL_CRUSHER, 1, 5,
                AspectList.of(ModAspects.EARTH, 6).add(ModAspects.TOOL, 6).add(ModAspects.ENTROPY, 6)
                        .add(ModAspects.VOID, 6).add(ModAspects.AVERSION, 6).add(ModAspects.ELDRITCH, 6)
                        .add(ModAspects.DESIRE, 6),
                List.of("elemental_tools", "void_gear", "warp"), false, NodeShape.SPECIAL, 2, "primal_crusher");

        // a focus with a bracket and just enough mind to aim itself. The original filed it under golemancy,
        // which is where everything that works while nobody is holding it belongs
        entry(context, "autocaster", ModResearch.GOLEMANCY, ModItems.AUTOCASTER, 1, 6,
                AspectList.of(ModAspects.MECHANISM, 3).add(ModAspects.AVERSION, 3).add(ModAspects.SENSES, 3)
                        .add(ModAspects.MIND, 3).add(ModAspects.AURA, 3),
                List.of("biothaumic_mind", "infusion", "wand"), false, NodeShape.MAJOR, 2, "autocaster");

        // the made minds golemancy is built on: gears under glass, and the same with a brain grown into it
        entry(context, "clockwork_mind", ModResearch.GOLEMANCY, ModItems.CLOCKWORK_MIND, 0, 3,
                AspectList.of(ModAspects.MECHANISM, 3).add(ModAspects.MIND, 3).add(ModAspects.SENSES, 3)
                        .add(ModAspects.ENERGY, 6).add(ModAspects.LIFE, 3),
                List.of("arcane_workbench", "metallurgy"), false, NodeShape.PLAIN, 1, "clockwork_mind");
        entry(context, "biothaumic_mind", ModResearch.GOLEMANCY, ModItems.BIOTHAUMIC_MIND, 0, 5,
                AspectList.of(ModAspects.MECHANISM, 3).add(ModAspects.MIND, 3).add(ModAspects.SENSES, 3)
                        .add(ModAspects.UNDEAD, 3),
                List.of("clockwork_mind", "infusion"), false, NodeShape.PLAIN, 2, "biothaumic_mind");

        // where artifice starts: the charm of all six, and glass silvered to make a mirror of
        entry(context, "primal_charm", ModResearch.ARTIFICE, ModItems.PRIMAL_CHARM, 1, 1,
                AspectList.EMPTY, List.of("vis_crystals"), false, NodeShape.MAJOR, 1,
                "primal_charm", "mirrored_glass");
        // the lamps: one that lights the dark, and the two worked from it that tend what grows and what breeds
        entry(context, "arcane_lamp", ModResearch.ARTIFICE, ModBlocks.ARCANE_LAMP, 0, -1,
                AspectList.of(ModAspects.LIGHT, 6).add(ModAspects.FIRE, 3).add(ModAspects.AIR, 3),
                List.of("primal_charm", "nitor"), false, NodeShape.PLAIN, 1, "arcane_lamp");
        entry(context, "growth_lamp", ModResearch.ARTIFICE, ModBlocks.GROWTH_LAMP, -1, -2,
                AspectList.of(ModAspects.PLANT, 6).add(ModAspects.LIGHT, 3).add(ModAspects.LIFE, 3),
                List.of("arcane_lamp", "infusion"), false, NodeShape.PLAIN, 2, "growth_lamp");
        entry(context, "fertility_lamp", ModResearch.ARTIFICE, ModBlocks.FERTILITY_LAMP, 1, -2,
                AspectList.of(ModAspects.BEAST, 6).add(ModAspects.LIGHT, 3).add(ModAspects.LIFE, 3),
                List.of("arcane_lamp", "infusion"), false, NodeShape.PLAIN, 2, "fertility_lamp");
        entry(context, "hungry_chest", ModResearch.ARTIFICE, ModBlocks.HUNGRY_CHEST, 3, -1,
                AspectList.of(ModAspects.VOID, 3).add(ModAspects.DESIRE, 3).add(ModAspects.TRAP, 3),
                List.of("primal_charm", "greatwood"), false, NodeShape.PLAIN, 1, "hungry_chest");
        entry(context, "water_jug", ModResearch.ALCHEMY, ModBlocks.WATER_JUG, 1, 2,
                AspectList.of(ModAspects.WATER, 6).add(ModAspects.EARTH, 3).add(ModAspects.CRAFT, 3),
                List.of("crucible", "infusion"), false, NodeShape.PLAIN, 1, "water_jug");

        // totems that move the aura about, and the pole that steadies them
        entry(context, "aura_totem", ModResearch.ARCANA, ModBlocks.AURA_TOTEM_PUSH, 8, 1,
                AspectList.of(ModAspects.AURA, 6).add(ModAspects.EXCHANGE, 3).add(ModAspects.WATER, 3)
                        .add(ModAspects.AVERSION, 3),
                List.of("infusion"), false, NodeShape.SPECIAL, 3,
                "aura_totem_push", "aura_totem_pole_inner", "aura_totem_pole_outer");
        entry(context, "aura_totem_pull", ModResearch.ARCANA, ModBlocks.AURA_TOTEM_PULL, 8, 0,
                AspectList.of(ModAspects.AURA, 6).add(ModAspects.EXCHANGE, 3).add(ModAspects.WATER, 3)
                        .add(ModAspects.DESIRE, 3),
                List.of("aura_totem"), false, NodeShape.PLAIN, 3, "aura_totem_pull");
        entry(context, "aura_totem_pole_pure", ModResearch.ARCANA, ModBlocks.AURA_TOTEM_POLE_PURE, 9, 1,
                AspectList.of(ModAspects.AURA, 6).add(ModAspects.ORDER, 3).add(ModAspects.MOTION, 3),
                List.of("aura_totem"), false, NodeShape.PLAIN, 2, "aura_totem_pole_pure");

        // a pedestal that fills a wand, and a device that shows the aura
        entry(context, "recharge_pedestal", ModResearch.ARCANA, ModBlocks.RECHARGE_PEDESTAL, 6, 1,
                AspectList.of(ModAspects.AURA, 6).add(ModAspects.ENERGY, 6).add(ModAspects.EXCHANGE, 3),
                List.of("infusion", "charger"), false, NodeShape.PLAIN, 2, "recharge_pedestal");
        entry(context, "dioptra", ModResearch.ARTIFICE, ModBlocks.DIOPTRA, 6, 1,
                AspectList.of(ModAspects.SENSES, 3).add(ModAspects.AURA, 3).add(ModAspects.ENERGY, 3)
                        .add(ModAspects.EARTH, 3),
                List.of("goggles"), false, NodeShape.PLAIN, 2, "dioptra");

        // salts against warp, and a machine to make a bath of them
        entry(context, "bath_salts", ModResearch.ALCHEMY, ModItems.BATH_SALTS, -1, 1,
                AspectList.of(ModAspects.MIND, 3).add(ModAspects.AURA, 3).add(ModAspects.ORDER, 3)
                        .add(ModAspects.LIFE, 3),
                List.of("crucible"), false, NodeShape.PLAIN, 2, "bath_salts");
        entry(context, "spa", ModResearch.ALCHEMY, ModBlocks.SPA, -2, 0,
                AspectList.of(ModAspects.WATER, 3).add(ModAspects.MECHANISM, 3).add(ModAspects.ORDER, 3),
                List.of("bath_salts"), false, NodeShape.PLAIN, 1, "spa");

        // things that answer to redstone or move what is in front of them
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

        entry(context, "arcane_stone", ModResearch.ARTIFICE, ModBlocks.ARCANE_STONE.block(), 3, 2,
                AspectList.of(ModAspects.EARTH, 4).add(ModAspects.AURA, 2).add(ModAspects.CRYSTAL, 2),
                List.of("arcane_workbench"), false, NodeShape.PLAIN, 2, "arcane_stone",
                "arcane_stone_bricks_from_arcane_stone");
    }

    /** A branch: its tab picture and its sky are both named after it. */
    private static void category(BootstrapContext<ResearchCategory> context, ResourceKey<ResearchCategory> key,
            String name, int order) {
        context.register(key, new ResearchCategory(Alchemia.id("textures/gui/research_category/" + name + ".png"),
                Alchemia.id("textures/gui/research_background/" + name + ".png"), order));
    }

    /**
     * The write-up always opens with a passage, and any recipes named follow it one to a page. Splitting the passage
     * into several is a matter of adding keys here; nothing else has to change.
     */
    private static List<ResearchPage> pages(String name, String... recipes) {
        List<ResearchPage> pages = new java.util.ArrayList<>();
        pages.add(new ResearchPage.Text("research." + Alchemia.MODID + "." + name + ".page"));
        for (String recipe : recipes) {
            pages.add(new ResearchPage.Recipe(Alchemia.id(recipe)));
        }
        return List.copyOf(pages);
    }

    private static void entry(BootstrapContext<ResearchEntry> context, String name, ResourceKey<ResearchCategory> category,
            ItemLike icon, int column, int row, AspectList requirements, List<String> parents, boolean autoUnlock,
            NodeShape shape, int complexity, String... recipes) {
        context.register(ResourceKey.create(ModResearch.ENTRY_KEY, Alchemia.id(name)),
                new ResearchEntry(category, requirements, column, row,
                        BuiltInRegistries.ITEM.wrapAsHolder(icon.asItem()),
                        parents.stream().map(Alchemia::id).toList(),
                        pages(name, recipes),
                        autoUnlock, shape, complexity));
    }
}
