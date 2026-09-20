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
                AspectList.EMPTY, List.of(), true, NodeShape.MAJOR);

        entry(context, "amber", ModResearch.BASICS, ModItems.AMBER, -2, 1,
                AspectList.of(ModAspects.CRYSTAL, 2).add(ModAspects.TRAP, 2), List.of("aspects"), false, NodeShape.PLAIN, "amber_from_ore_smelting", "amber_block_from_amber");
        entry(context, "quicksilver", ModResearch.ALCHEMY, ModItems.QUICKSILVER, 0, 1,
                AspectList.of(ModAspects.METAL, 3).add(ModAspects.EXCHANGE, 2), List.of("aspects"), false, NodeShape.PLAIN, "quicksilver_from_raw_cinnabar_smelting", "quicksilver_from_shimmerleaf");
        entry(context, "vis_crystals", ModResearch.ARCANA, ModItems.BALANCED_SHARD, 2, 1,
                AspectList.of(ModAspects.CRYSTAL, 4).add(ModAspects.AURA, 2), List.of("aspects"), false, NodeShape.SPECIAL);
        entry(context, "greatwood", ModResearch.BASICS, ModBlocks.GREATWOOD.sapling(), -1, 2,
                AspectList.of(ModAspects.PLANT, 4).add(ModAspects.LIFE, 2), List.of("aspects"), false, NodeShape.PLAIN, "greatwood_planks");
        entry(context, "silverwood", ModResearch.ARCANA, ModBlocks.SILVERWOOD.sapling(), 1, 2,
                AspectList.of(ModAspects.PLANT, 4).add(ModAspects.AURA, 4), List.of("greatwood"), false, NodeShape.SPECIAL, "silverwood_planks");
        entry(context, "warp", ModResearch.ELDRITCH, Items.ENDER_PEARL, 0, 3,
                AspectList.of(ModAspects.ELDRITCH, 4).add(ModAspects.FLUX, 2), List.of("vis_crystals"), false, NodeShape.MAJOR);
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
            NodeShape shape, String... recipes) {
        context.register(ResourceKey.create(ModResearch.ENTRY_KEY, Alchemia.id(name)),
                new ResearchEntry(category, requirements, column, row,
                        BuiltInRegistries.ITEM.wrapAsHolder(icon.asItem()),
                        parents.stream().map(Alchemia::id).toList(),
                        pages(name, recipes),
                        autoUnlock, shape));
    }
}
