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
        category(context, ModResearch.BASICS, ModItems.ALCHEMOMETER, "basics", 0);
        category(context, ModResearch.ARCANA, ModItems.BALANCED_SHARD, "arcana", 1);
        category(context, ModResearch.ALCHEMY, ModItems.QUICKSILVER, "alchemy", 2);
        category(context, ModResearch.ARTIFICE, ModItems.ALCHEMIUM_GEAR, "artifice", 3);
        category(context, ModResearch.GOLEMANCY, ModItems.BRASS_INGOT, "golemancy", 4);
        category(context, ModResearch.ELDRITCH, ModItems.SHARDS.get(CrystalType.FLUX), "eldritch", 5);
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
                List.of("wand_cap_brass"), false, NodeShape.SPECIAL, 3, "wand_cap_alchemium");

        entry(context, "wand_rod_greatwood", ModResearch.ARCANA, ModItems.WAND_RODS.get("greatwood"), 2, 3,
                AspectList.of(ModAspects.TOOL, 3).add(ModAspects.PLANT, 6).add(ModAspects.ENERGY, 3),
                List.of("arcane_workbench", "greatwood"), false, NodeShape.PLAIN, 2, "wand_rod_greatwood");

        // --- the crucible ---------------------------------------------------------------------------------

        entry(context, "crucible", ModResearch.ALCHEMY, ModBlocks.CRUCIBLE, 0, 2,
                AspectList.of(ModAspects.WATER, 4).add(ModAspects.FIRE, 4).add(ModAspects.EXCHANGE, 2),
                List.of("wand"), false, NodeShape.MAJOR, 2);
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

        // the last of the caps hangs off this one, since void metal is the only thing it can be drawn from
        entry(context, "void_metal", ModResearch.ALCHEMY, ModItems.VOID_INGOT, -2, 4,
                AspectList.of(ModAspects.VOID, 6).add(ModAspects.DARKNESS, 6).add(ModAspects.METAL, 4)
                        .add(ModAspects.ELDRITCH, 2),
                List.of("metallurgy"), false, NodeShape.SPECIAL, 3,
                "void_seed", "void_ingot", "wand_cap_void");

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

        entry(context, "wand_rods", ModResearch.ARCANA, ModItems.WAND_RODS.get("silverwood"), 8, 4,
                AspectList.of(ModAspects.TOOL, 6).add(ModAspects.AURA, 6).add(ModAspects.ENERGY, 6),
                List.of("infusion", "silverwood"), false, NodeShape.PLAIN, 3,
                "wand_rod_obsidian", "wand_rod_ice", "wand_rod_quartz", "wand_rod_reed",
                "wand_rod_blaze", "wand_rod_bone", "wand_rod_silverwood");

        entry(context, "arcane_stone", ModResearch.ARTIFICE, ModBlocks.ARCANE_STONE.block(), 3, 2,
                AspectList.of(ModAspects.EARTH, 4).add(ModAspects.AURA, 2).add(ModAspects.CRYSTAL, 2),
                List.of("arcane_workbench"), false, NodeShape.PLAIN, 2, "arcane_stone",
                "arcane_stone_bricks_from_arcane_stone");
    }

    private static void category(BootstrapContext<ResearchCategory> context, ResourceKey<ResearchCategory> key,
            ItemLike icon, String sky, int order) {
        ResourceLocation iconPath = BuiltInRegistries.ITEM.getKey(icon.asItem());
        context.register(key, new ResearchCategory(iconPath,
                Alchemia.id("textures/gui/research_background/" + sky + ".png"), order));
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
