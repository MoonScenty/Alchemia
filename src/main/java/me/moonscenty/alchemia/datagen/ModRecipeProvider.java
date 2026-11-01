package me.moonscenty.alchemia.datagen;

import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.crafting.ArcaneShapedRecipe;
import me.moonscenty.alchemia.crafting.ArcaneShapelessRecipe;
import me.moonscenty.alchemia.crafting.InfusionRecipe;
import me.moonscenty.alchemia.enchantment.InfusionEnchantment;
import me.moonscenty.alchemia.crafting.LabelRecipe;
import me.moonscenty.alchemia.crafting.CrucibleRecipe;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.crafting.ArcaneWandRecipe;
import me.moonscenty.alchemia.block.CrystalType;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.item.HelmFitting;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModTags;
import me.moonscenty.alchemia.registry.StoneSet;
import me.moonscenty.alchemia.registry.WoodSet;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;

public class ModRecipeProvider extends RecipeProvider {
    private static final int VOID_ROBE_UNSTABLE = 6;
    private static final int FORTRESS_UNSTABLE = 3;
    private static final int HELM_GOGGLES_UNSTABLE = 5;
    private static final int MASK_UNSTABLE = 8;
    private static final int ELEMENTAL_UNSTABLE = 1;
    private static final int CRUSHER_UNSTABLE = 6;
    private static final int TOTEM_UNSTABLE = 5;

    private void pole(RecipeOutput output, String name, String research,
            net.neoforged.neoforge.registries.DeferredBlock<?> pole, Ingredient shard, AspectList cost) {
        arcane(output, name, research, new ItemStack(pole.get()), cost,
                Map.of('N', shard,
                        'W', Ingredient.of(ModBlocks.SILVERWOOD.planks()),
                        'G', Ingredient.of(ModBlocks.GREATWOOD.planks())),
                "WGW", "WNW", "WGW");
    }

    private static final int PEDESTAL_UNSTABLE = 3;
    private static final int LAMP_UNSTABLE = 4;
    private static final int JUG_UNSTABLE = 1;
    private static final int BIOALCHEMIC_UNSTABLE = 4;
    private static final int AUTOCASTER_UNSTABLE = 5;

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        baubles(output);
        cook(output, "amber_from_ore", Ingredient.of(ModTags.Items.ORES_AMBER), has(ModTags.Items.ORES_AMBER), new ItemStack(ModItems.AMBER.get()));
        cook(output, "quicksilver_from_ore", Ingredient.of(ModTags.Items.ORES_CINNABAR), has(ModTags.Items.ORES_CINNABAR), new ItemStack(ModItems.QUICKSILVER.get()));
        cook(output, "quicksilver_from_raw_cinnabar", Ingredient.of(ModItems.RAW_CINNABAR), has(ModItems.RAW_CINNABAR), new ItemStack(ModItems.QUICKSILVER.get()));

        compress(output, ModItems.ALCHEMIUM_NUGGET, ModTags.Items.NUGGETS_ALCHEMIUM, ModItems.ALCHEMIUM_INGOT, ModTags.Items.INGOTS_ALCHEMIUM);
        compress(output, ModItems.ALCHEMIUM_INGOT, ModTags.Items.INGOTS_ALCHEMIUM, ModBlocks.ALCHEMIUM_BLOCK, ModTags.Items.STORAGE_BLOCKS_ALCHEMIUM);
        compress(output, ModItems.BRASS_NUGGET, ModTags.Items.NUGGETS_BRASS, ModItems.BRASS_INGOT, ModTags.Items.INGOTS_BRASS);
        compress(output, ModItems.BRASS_INGOT, ModTags.Items.INGOTS_BRASS, ModBlocks.BRASS_BLOCK, ModTags.Items.STORAGE_BLOCKS_BRASS);
        compress(output, ModItems.QUICKSILVER_DROP, ModTags.Items.NUGGETS_QUICKSILVER, ModItems.QUICKSILVER, ModTags.Items.GEMS_QUICKSILVER);
        compress(output, ModItems.VOID_INGOT, ModTags.Items.INGOTS_VOID, ModBlocks.VOID_METAL_BLOCK,
                ModTags.Items.STORAGE_BLOCKS_VOID);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.GOLD_INGOT)
                .pattern("###").pattern("###").pattern("###")
                .define('#', ModItems.COIN)
                .unlockedBy("has_coin", has(ModItems.COIN))
                .save(output, Alchemia.id("gold_ingot_from_coins"));
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.COIN), RecipeCategory.MISC, Items.GOLD_NUGGET,
                        0.0F, 200)
                .unlockedBy("has_coin", has(ModItems.COIN))
                .save(output, Alchemia.id("gold_nugget_from_smelting_coin"));

        plate(output, ModItems.ALCHEMIUM_PLATE, ModTags.Items.INGOTS_ALCHEMIUM);
        plate(output, ModItems.BRASS_PLATE, ModTags.Items.INGOTS_BRASS);
        plate(output, ModItems.IRON_PLATE, Tags.Items.INGOTS_IRON);
        plate(output, ModItems.VOID_PLATE, ModTags.Items.INGOTS_VOID);
        gear(output, ModItems.ALCHEMIUM_GEAR, ModTags.Items.NUGGETS_ALCHEMIUM);
        gear(output, ModItems.BRASS_GEAR, ModTags.Items.NUGGETS_BRASS);
        gear(output, ModItems.VOID_GEAR, ModTags.Items.NUGGETS_VOID);
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.WOODEN_TABLE.get())
                .pattern("SSS").pattern("W W")
                .define('S', net.minecraft.tags.ItemTags.WOODEN_SLABS).define('W', net.minecraft.tags.ItemTags.PLANKS)
                .unlockedBy("has_planks", has(net.minecraft.tags.ItemTags.PLANKS)).save(output);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModItems.SCRIBING_TOOLS.get())
                .requires(net.neoforged.neoforge.common.crafting.DataComponentIngredient.of(true,
                        net.minecraft.core.component.DataComponentPredicate.EMPTY, ModItems.PHIAL.get()))
                .requires(Items.FEATHER).requires(Tags.Items.DYES_BLACK)
                .unlockedBy("has_feather", has(Items.FEATHER)).save(output);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModItems.SCRIBING_TOOLS.get())
                .requires(Items.GLASS_BOTTLE).requires(Items.FEATHER).requires(Tags.Items.DYES_BLACK)
                .unlockedBy("has_feather", has(Items.FEATHER)).save(output, Alchemia.id("scribing_tools_from_bottle"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModItems.SCRIBING_TOOLS.get())
                .requires(ModItems.SCRIBING_TOOLS.get()).requires(Tags.Items.DYES_BLACK)
                .unlockedBy("has_tools", has(ModItems.SCRIBING_TOOLS.get())).save(output, Alchemia.id("scribing_tools_refill"));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.ALCHEMOMETER.get())
                .pattern(" 1 ").pattern("IGI").pattern(" 1 ")
                .define('I', Tags.Items.INGOTS_GOLD).define('G', Tags.Items.GLASS_BLOCKS_COLORLESS)
                .define('1', ModTags.Items.SHARDS)
                .unlockedBy("has_shard", has(ModTags.Items.SHARDS)).save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.STONE_TABLE.get())
                .pattern("SSS").pattern("W W")
                .define('S', Items.STONE_SLAB)
                .define('W', Tags.Items.STONES)
                .unlockedBy("has_input", has(Items.STONE_SLAB))
                .save(output);

        cook(output, "salis_mundus", Ingredient.of(ModItems.BALANCED_SHARD), has(ModItems.BALANCED_SHARD),
                new ItemStack(ModItems.SALIS_MUNDUS.get()));

        wand(output);
        arcane(output);
        crucible(output);
        metalGear(output);
        distillery(output);
        voidMetal(output);
        infusion(output);
        enchantments(output);

        quadrupleFrom(output, ModBlocks.ARCANE_STONE_BRICKS.block(), ModBlocks.ARCANE_STONE.block());
        for (StoneSet set : ModBlocks.STONE_SETS) {
            stairBuilder(set.stairs(), Ingredient.of(set.block())).unlockedBy("has_input", has(set.block())).save(output);
            slab(output, RecipeCategory.BUILDING_BLOCKS, set.slab(), set.block());
            stonecutting(output, set.stairs(), set.block(), 1);
            stonecutting(output, set.slab(), set.block(), 2);
        }
        stonecutting(output, ModBlocks.ARCANE_STONE_BRICKS.block(), ModBlocks.ARCANE_STONE.block(), 1);

        nodeStabilizer(output);

        compress(output, ModItems.AMBER, ModTags.Items.GEMS_AMBER, ModBlocks.AMBER_BLOCK, ModTags.Items.STORAGE_BLOCKS_AMBER);
        quadrupleFrom(output, ModBlocks.AMBER_BRICKS, ModBlocks.AMBER_BLOCK);
        quadrupleFrom(output, ModBlocks.AMBER_BLOCK, ModBlocks.AMBER_BRICKS);

        distill(output, ModBlocks.SHIMMERLEAF, ModItems.QUICKSILVER);
        distill(output, ModBlocks.CINDERPEARL, Items.BLAZE_POWDER);

        for (WoodSet wood : ModBlocks.WOODS) {
            planksFromLogs(output, wood.planks(), wood.logsItemTag(), 4);
            stairBuilder(wood.stairs(), Ingredient.of(wood.planks())).group("wooden_stairs")
                    .unlockedBy("has_planks", has(wood.planks())).save(output);
            slab(output, RecipeCategory.BUILDING_BLOCKS, wood.slab(), wood.planks());
        }

        cluster(output, "iron_ingot", ModItems.IRON_CLUSTER, Items.IRON_INGOT);
        cluster(output, "gold_ingot", ModItems.GOLD_CLUSTER, Items.GOLD_INGOT);
        cluster(output, "copper_ingot", ModItems.COPPER_CLUSTER, Items.COPPER_INGOT);
        cluster(output, "quicksilver", ModItems.CINNABAR_CLUSTER, ModItems.QUICKSILVER);
    }

    // the vanilla helper saves under the minecraft namespace, so spell the id out
    private static void stonecutting(RecipeOutput output, ItemLike result, ItemLike material, int count) {
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(material), RecipeCategory.BUILDING_BLOCKS, result, count)
                .unlockedBy("has_input", has(material))
                .save(output, Alchemia.id(getConversionRecipeName(result, material) + "_stonecutting"));
    }

    private static void quadrupleFrom(RecipeOutput output, ItemLike result, ItemLike input) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, result, 4)
                .pattern("##").pattern("##")
                .define('#', input)
                .unlockedBy("has_input", has(input))
                .save(output, conversion(result, input));
    }

    private static void distill(RecipeOutput output, ItemLike plant, ItemLike result) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, result)
                .requires(plant)
                .unlockedBy("has_input", has(plant))
                .save(output, conversion(result, plant));
    }

    private static void nodeStabilizer(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.NODE_STABILIZER.get())
                .pattern(" P ")
                .pattern("PSP")
                .pattern("BBB")
                .define('P', ModItems.ALCHEMIUM_PLATE.get())
                .define('S', ModItems.BALANCED_SHARD.get())
                .define('B', ModBlocks.ARCANE_STONE_BRICKS.block().get())
                .unlockedBy("has_shard", has(ModItems.BALANCED_SHARD.get()))
                .save(output, Alchemia.id("node_stabilizer"));
    }

    private void staffCore(RecipeOutput output, String stuff, String research, AspectList cost) {
        arcane(output, "wand_rod_" + stuff + "_staff", research,
                new ItemStack(ModItems.WAND_RODS.get(stuff + "_staff").get()), cost,
                Map.of('S', Ingredient.of(ModItems.PRIMAL_CHARM), 'G', Ingredient.of(ModItems.WAND_RODS.get(stuff))),
                "  S", " G ", "G  ");
    }

    private static void compress(RecipeOutput output, ItemLike small, TagKey<Item> smallTag, ItemLike big, TagKey<Item> bigTag) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, big)
                .pattern("###").pattern("###").pattern("###")
                .define('#', smallTag)
                .unlockedBy("has_input", has(smallTag))
                .save(output, conversion(big, small));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, small, 9)
                .requires(bigTag)
                .unlockedBy("has_input", has(bigTag))
                .save(output, conversion(small, big));
    }

    private static ResourceLocation conversion(ItemLike result, ItemLike input) {
        return Alchemia.id(getConversionRecipeName(result, input));
    }

    private static void plate(RecipeOutput output, ItemLike plate, TagKey<Item> ingot) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, plate, 3)
                .pattern("###")
                .define('#', ingot)
                .unlockedBy("has_input", has(ingot))
                .save(output);
    }

    private static void gear(RecipeOutput output, ItemLike gear, TagKey<Item> nugget) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, gear)
                .pattern("###").pattern("#I#").pattern("###")
                .define('#', nugget)
                .define('I', Tags.Items.INGOTS_IRON)
                .unlockedBy("has_input", has(nugget))
                .save(output);
    }

    private static void cluster(RecipeOutput output, String resultName, ItemLike cluster, ItemLike result) {
        cook(output, resultName + "_from_cluster", Ingredient.of(cluster), has(cluster), new ItemStack(result, 2));
    }

    private static void cook(RecipeOutput output, String name, Ingredient input, Criterion<?> unlockedBy, ItemStack result) {
        SimpleCookingRecipeBuilder.smelting(input, RecipeCategory.MISC, result, 1.0F, 200)
                .unlockedBy("has_input", unlockedBy)
                .save(output, Alchemia.id(name + "_smelting"));
        SimpleCookingRecipeBuilder.blasting(input, RecipeCategory.MISC, result, 1.0F, 100)
                .unlockedBy("has_input", unlockedBy)
                .save(output, Alchemia.id(name + "_blasting"));
    }

    // no components: the result must stack with a wooden/iron wand built at the arcane workbench
    private void wand(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.WAND_CAPS.get("iron").get())
                .pattern("NNN")
                .pattern("N N")
                .define('N', Tags.Items.NUGGETS_IRON)
                .unlockedBy("has_nugget", has(Tags.Items.NUGGETS_IRON))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.WAND.get())
                .pattern("  C")
                .pattern(" S ")
                .pattern("C  ")
                .define('C', ModItems.WAND_CAPS.get("iron").get())
                .define('S', Tags.Items.RODS_WOODEN)
                .unlockedBy("has_cap", has(ModItems.WAND_CAPS.get("iron").get()))
                .save(output);

        SpecialRecipeBuilder.special(category -> ArcaneWandRecipe.INSTANCE)
                .save(output, Alchemia.id("arcane_wand").toString());
        SpecialRecipeBuilder.special(category -> me.moonscenty.alchemia.crafting.ArcaneSceptreRecipe.INSTANCE)
                .save(output, Alchemia.id("arcane_sceptre").toString());
    }

    private void arcane(RecipeOutput output) {
        arcane(output, "wand_cap_gold", new ItemStack(ModItems.WAND_CAPS.get("gold").get()),
                AspectList.of(ModAspects.ORDER, 24).add(ModAspects.FIRE, 24).add(ModAspects.AIR, 24),
                Map.of('N', Ingredient.of(Tags.Items.NUGGETS_GOLD)), "NNN", "N N");

        arcane(output, "wand_cap_brass", new ItemStack(ModItems.WAND_CAPS.get("brass").get()),
                AspectList.of(ModAspects.ORDER, 24).add(ModAspects.FIRE, 24).add(ModAspects.AIR, 24),
                Map.of('N', Ingredient.of(ModTags.Items.NUGGETS_BRASS)), "NNN", "N N");

        arcane(output, "wand_cap_alchemium_inert", new ItemStack(ModItems.INERT_CAPS.get("alchemium").get()),
                AspectList.of(ModAspects.ORDER, 48).add(ModAspects.FIRE, 48).add(ModAspects.AIR, 48),
                Map.of('N', Ingredient.of(ModTags.Items.NUGGETS_ALCHEMIUM)), "NNN", "N N");

        arcane(output, "wand_rod_greatwood", new ItemStack(ModItems.WAND_RODS.get("greatwood").get()),
                AspectList.of(ModAspects.ENTROPY, 24),
                Map.of('G', Ingredient.of(ModBlocks.GREATWOOD.log())), " G", "G ");

        staffCore(output, "greatwood", "staff_greatwood", AspectList.of(ModAspects.ORDER, 8 * 8));
        staffCore(output, "silverwood", "staff_silverwood", AspectList.of(ModAspects.ORDER, 24 * 8));
        for (String[] primal : new String[][] {{"reed", "air"}, {"obsidian", "earth"}, {"blaze", "fire"},
                {"ice", "water"}, {"quartz", "order"}, {"bone", "entropy"}}) {
            var element = me.moonscenty.alchemia.block.CrystalType.valueOf(primal[1].toUpperCase()).aspect();
            staffCore(output, primal[0], "staves_primal",
                    AspectList.of(ModAspects.ORDER, 14 * 4).add(element, 14 * 4));
        }

        arcane(output, "arcane_stone", new ItemStack(ModBlocks.ARCANE_STONE.block(), 9),
                AspectList.of(ModAspects.EARTH, 5).add(ModAspects.FIRE, 5),
                Map.of('S', Ingredient.of(Tags.Items.STONES), 'C', Ingredient.of(ModTags.Items.SHARDS)),
                "SSS", "SCS", "SSS");

        arcane(output, "enchanted_fabric", "enchanted_fabric", new ItemStack(ModItems.ENCHANTED_FABRIC.get()),
                AspectList.eachPrimal(5),
                Map.of('S', Ingredient.of(Items.STRING), 'C', Ingredient.of(ItemTags.WOOL)),
                " S ", "SCS", " S ");

        arcane(output, "cloth_chest", "robes", new ItemStack(ModItems.ROBES.get("cloth_chest").get()),
                AspectList.of(ModAspects.AIR, 30),
                Map.of('I', Ingredient.of(ModItems.ENCHANTED_FABRIC)), "I I", "III", "III");
        arcane(output, "cloth_legs", "robes", new ItemStack(ModItems.ROBES.get("cloth_legs").get()),
                AspectList.of(ModAspects.WATER, 25),
                Map.of('I', Ingredient.of(ModItems.ENCHANTED_FABRIC)), "III", "I I", "I I");
        arcane(output, "cloth_boots", "robes", new ItemStack(ModItems.ROBES.get("cloth_boots").get()),
                AspectList.of(ModAspects.EARTH, 20),
                Map.of('I', Ingredient.of(ModItems.ENCHANTED_FABRIC)), "I I", "I I");

        arcane(output, "focus_pouch", new ItemStack(ModItems.FOCUS_POUCH.get()),
                AspectList.of(ModAspects.EARTH, 25).add(ModAspects.ORDER, 25).add(ModAspects.ENTROPY, 25),
                Map.of('L', Ingredient.of(Items.LEATHER),
                        'G', Ingredient.of(Items.GOLD_INGOT),
                        'B', Ingredient.of(ModItems.MUNDANE_BELT)),
                "LGL", "LBL", "LLL");

        arcane(output, "goggles_of_revealing", "goggles",
                new ItemStack(ModItems.GOGGLES.get()),
                AspectList.eachPrimal(25),
                Map.of('T', Ingredient.of(ModItems.ALCHEMOMETER),
                        'G', Ingredient.of(ModTags.Items.INGOTS_BRASS),
                        'L', Ingredient.of(Items.LEATHER)),
                "LGL", "L L", "TGT");

        arcane(output, "focal_manipulator", "focal_manipulator",
                new ItemStack(ModBlocks.FOCAL_MANIPULATOR.get()),
                AspectList.of(ModAspects.FIRE, 150).add(ModAspects.AIR, 150).add(ModAspects.ENTROPY, 150)
                        .add(ModAspects.EARTH, 150).add(ModAspects.WATER, 150).add(ModAspects.ORDER, 150),
                Map.of('I', Ingredient.of(Items.IRON_INGOT),
                        'Q', Ingredient.of(ModBlocks.ARCANE_STONE.slab()),
                        'S', Ingredient.of(ModBlocks.ARCANE_STONE.block()),
                        'T', Ingredient.of(ModBlocks.STONE_TABLE),
                        'G', Ingredient.of(Items.GOLD_INGOT),
                        'P', Ingredient.of(ModItems.PRIMAL_CHARM)),
                "IQI", "SPS", "GTG");

        shapeless(output, "primal_charm", "primal_charm", new ItemStack(ModItems.PRIMAL_CHARM.get()),
                AspectList.of(ModAspects.EARTH, 25).add(ModAspects.FIRE, 25).add(ModAspects.AIR, 25)
                        .add(ModAspects.WATER, 25).add(ModAspects.ORDER, 25).add(ModAspects.ENTROPY, 25),
                Ingredient.of(Items.GOLD_INGOT), Ingredient.of(ModItems.BALANCED_SHARD),
                Ingredient.of(ModItems.SHARDS.get(CrystalType.AIR)), Ingredient.of(ModItems.SHARDS.get(CrystalType.FIRE)),
                Ingredient.of(ModItems.SHARDS.get(CrystalType.WATER)),
                Ingredient.of(ModItems.SHARDS.get(CrystalType.EARTH)),
                Ingredient.of(ModItems.SHARDS.get(CrystalType.ORDER)),
                Ingredient.of(ModItems.SHARDS.get(CrystalType.ENTROPY)));
        shapeless(output, "mirrored_glass", "primal_charm", new ItemStack(ModItems.MIRRORED_GLASS.get()),
                AspectList.of(ModAspects.FIRE, 25).add(ModAspects.EARTH, 25),
                Ingredient.of(ModItems.QUICKSILVER), Ingredient.of(net.neoforged.neoforge.common.Tags.Items.GLASS_PANES));
        arcane(output, "morphic_resonator", "crucible", new ItemStack(ModItems.MORPHIC_RESONATOR.get()),
                AspectList.of(ModAspects.ORDER, 50).add(ModAspects.ENTROPY, 50),
                Map.of('G', Ingredient.of(net.neoforged.neoforge.common.Tags.Items.GLASS_PANES),
                        'B', Ingredient.of(ModItems.BRASS_PLATE),
                        'F', Ingredient.of(ModItems.SHARDS.get(CrystalType.FLUX))),
                " G ", "BFB", " G ");
        arcane(output, "clockwork_mind", "clockwork_mind", new ItemStack(ModItems.CLOCKWORK_MIND.get()),
                AspectList.of(ModAspects.FIRE, 10).add(ModAspects.WATER, 10).add(ModAspects.ORDER, 10),
                Map.of('P', Ingredient.of(net.neoforged.neoforge.common.Tags.Items.GLASS_PANES),
                        'G', Ingredient.of(ModItems.BRASS_GEAR),
                        'B', Ingredient.of(ModItems.BRASS_PLATE),
                        'C', Ingredient.of(Items.COMPARATOR)),
                "PPP", "PGP", "BCB");
        arcane(output, "vision_module", "golem_vision", new ItemStack(ModItems.VISION_MODULE.get()),
                AspectList.of(ModAspects.WATER, 25).add(ModAspects.AIR, 25),
                Map.of('B', Ingredient.of(Items.GLASS_BOTTLE),
                        'E', Ingredient.of(Items.FERMENTED_SPIDER_EYE),
                        'P', Ingredient.of(ModItems.BRASS_PLATE),
                        'G', Ingredient.of(ModItems.BRASS_GEAR)),
                "B B", "E E", "PGP");
        arcane(output, "golem_bell", "control_seals", new ItemStack(ModItems.GOLEM_BELL.get()),
                AspectList.of(ModAspects.ORDER, 10).add(ModAspects.AIR, 10),
                Map.of('S', Ingredient.of(Tags.Items.RODS_WOODEN),
                        'Q', Ingredient.of(Tags.Items.GEMS_QUARTZ)),
                " QQ", " QQ", "S  ");
        arcane(output, "aggression_module", "seal_guard", new ItemStack(ModItems.AGGRESSION_MODULE.get()),
                AspectList.of(ModAspects.FIRE, 25).add(ModAspects.ENTROPY, 25),
                Map.of('R', Ingredient.of(net.neoforged.neoforge.common.Tags.Items.GLASS_PANES),
                        'T', Ingredient.of(Items.BLAZE_POWDER),
                        'P', Ingredient.of(ModItems.BRASS_PLATE),
                        'G', Ingredient.of(ModItems.BRASS_GEAR)),
                " R ", "RTR", "PGP");
        shapeless(output, "seal_blank", "control_seals", new ItemStack(ModItems.SEAL_BLANK.get(), 3),
                AspectList.of(ModAspects.FIRE, 10).add(ModAspects.ORDER, 10),
                Ingredient.of(Items.CLAY_BALL), Ingredient.of(ModItems.TALLOW),
                Ingredient.of(net.neoforged.neoforge.common.Tags.Items.DYES_RED), Ingredient.of(ModTags.Items.NITOR));
        arcane(output, "arcane_lamp", "arcane_lamp", new ItemStack(ModBlocks.ARCANE_LAMP.get()),
                AspectList.of(ModAspects.FIRE, 25).add(ModAspects.AIR, 25),
                Map.of('S', Ingredient.of(Items.DAYLIGHT_DETECTOR),
                        'I', Ingredient.of(Items.IRON_INGOT),
                        'A', Ingredient.of(ModBlocks.AMBER_BLOCK.get()),
                        'N', Ingredient.of(ModTags.Items.NITOR)),
                " S ", "IAI", " N ");
        arcane(output, "hungry_chest", "hungry_chest", new ItemStack(ModBlocks.HUNGRY_CHEST.get()),
                AspectList.of(ModAspects.AIR, 15).add(ModAspects.ORDER, 15).add(ModAspects.ENTROPY, 15),
                Map.of('W', Ingredient.of(ModBlocks.GREATWOOD.planks()),
                        'T', Ingredient.of(net.minecraft.tags.ItemTags.WOODEN_TRAPDOORS)),
                "WTW", "W W", "WWW");

        arcane(output, "levitator", "levitator", new ItemStack(ModBlocks.LEVITATOR.get()),
                AspectList.of(ModAspects.AIR, 20).add(ModAspects.EARTH, 15),
                Map.of('W', Ingredient.of(net.minecraft.tags.ItemTags.PLANKS),
                        'E', Ingredient.of(ModItems.SHARDS.get(CrystalType.AIR)),
                        'B', Ingredient.of(Items.IRON_INGOT),
                        'N', Ingredient.of(ModTags.Items.NITOR),
                        'G', Ingredient.of(ModItems.BRASS_GEAR)),
                "WEW", "BNB", "WGW");
        arcane(output, "redstone_relay", "redstone_relay", new ItemStack(ModBlocks.REDSTONE_RELAY.get()),
                AspectList.of(ModAspects.FIRE, 5).add(ModAspects.ORDER, 5),
                Map.of('T', Ingredient.of(Items.REDSTONE_TORCH),
                        'G', Ingredient.of(ModItems.BRASS_GEAR),
                        'C', Ingredient.of(ModItems.SHARDS.get(CrystalType.ORDER)),
                        'S', Ingredient.of(Items.SMOOTH_STONE_SLAB)),
                "TGC", "SSS");
        arcane(output, "barrier_stone", "barrier_stone", new ItemStack(ModBlocks.BARRIER_STONE.get(), 4),
                AspectList.of(ModAspects.FIRE, 50).add(ModAspects.ORDER, 50),
                Map.of('S', Ingredient.of(ModBlocks.ARCANE_STONE_BRICKS.block()),
                        'A', Ingredient.of(ModItems.SHARDS.get(CrystalType.FIRE)),
                        'B', Ingredient.of(ModItems.SHARDS.get(CrystalType.ORDER))),
                "SAS", "SBS");
        arcane(output, "travel_stone", "travel_stone", new ItemStack(ModBlocks.TRAVEL_STONE.get(), 4),
                AspectList.of(ModAspects.EARTH, 50).add(ModAspects.AIR, 50),
                Map.of('S', Ingredient.of(ModBlocks.ARCANE_STONE_BRICKS.block()),
                        'A', Ingredient.of(ModItems.SHARDS.get(CrystalType.AIR)),
                        'B', Ingredient.of(ModItems.SHARDS.get(CrystalType.EARTH))),
                "SAS", "SBS");
        arcane(output, "arcane_workbench_charger", "charger", new ItemStack(ModBlocks.ARCANE_WORKBENCH_CHARGER.get()),
                AspectList.of(ModAspects.FIRE, 100).add(ModAspects.ORDER, 100).add(ModAspects.AIR, 100),
                Map.of('I', Ingredient.of(Tags.Items.INGOTS_IRON),
                        'R', Ingredient.of(ModItems.BALANCED_SHARD),
                        'W', Ingredient.of(ModItems.WAND_RODS.get("greatwood"))),
                " R ", "W W", "I I");
        arcane(output, "arcane_ear", "arcane_ear", new ItemStack(ModBlocks.ARCANE_EAR.get()),
                AspectList.of(ModAspects.AIR, 10).add(ModAspects.ORDER, 5),
                Map.of('P', Ingredient.of(ModItems.BRASS_PLATE),
                        'B', Ingredient.of(ModItems.ZOMBIE_BRAIN),
                        'W', Ingredient.of(net.minecraft.tags.ItemTags.WOODEN_SLABS),
                        'R', Ingredient.of(Items.REDSTONE)),
                "PBP", " P ", "WRW");

        pole(output, "aura_totem_pole_inner", "aura_totem", ModBlocks.AURA_TOTEM_POLE_INNER,
                Ingredient.of(ModItems.SHARDS.get(CrystalType.EARTH)),
                AspectList.of(ModAspects.AIR, 50).add(ModAspects.EARTH, 125).add(ModAspects.ORDER, 75));
        pole(output, "aura_totem_pole_outer", "aura_totem", ModBlocks.AURA_TOTEM_POLE_OUTER,
                Ingredient.of(ModItems.SHARDS.get(CrystalType.AIR)),
                AspectList.of(ModAspects.AIR, 125).add(ModAspects.EARTH, 50).add(ModAspects.ORDER, 125));
        pole(output, "aura_totem_pole_pure", "aura_totem_pole_pure", ModBlocks.AURA_TOTEM_POLE_PURE,
                Ingredient.of(ModItems.BALANCED_SHARD),
                AspectList.of(ModAspects.AIR, 125).add(ModAspects.EARTH, 125).add(ModAspects.ORDER, 250));

        arcane(output, "dioptra", "dioptra", new ItemStack(ModBlocks.DIOPTRA.get()),
                AspectList.of(ModAspects.AIR, 25).add(ModAspects.EARTH, 25).add(ModAspects.ORDER, 25),
                Map.of('A', Ingredient.of(ModBlocks.ARCANE_STONE.block()),
                        'P', Ingredient.of(ModBlocks.ARCANE_STONE.slab()),
                        'I', Ingredient.of(Items.IRON_INGOT),
                        'G', Ingredient.of(ModItems.GOGGLES)),
                "APA", "IGI", "AAA");

        arcane(output, "spa", "spa", new ItemStack(ModBlocks.SPA.get()),
                AspectList.of(ModAspects.WATER, 25).add(ModAspects.ORDER, 15).add(ModAspects.EARTH, 15),
                Map.of('Q', Ingredient.of(Items.QUARTZ_BLOCK),
                        'I', Ingredient.of(Items.IRON_BARS),
                        'S', Ingredient.of(ModBlocks.ARCANE_STONE.block()),
                        'J', Ingredient.of(ModBlocks.JAR),
                        'P', Ingredient.of(ModItems.BRASS_GEAR)),
                "QIQ", "SJS", "SPS");

        arcane(output, "bellows", "bellows", new ItemStack(ModBlocks.BELLOWS.get()),
                AspectList.of(ModAspects.AIR, 25).add(ModAspects.ORDER, 10),
                Map.of('W', Ingredient.of(net.minecraft.tags.ItemTags.PLANKS),
                        'L', Ingredient.of(Items.LEATHER),
                        'C', Ingredient.of(ModItems.SHARDS.get(CrystalType.AIR)),
                        'I', Ingredient.of(Items.IRON_INGOT)),
                "WW ", "LCI", "WW ");

        arcane(output, "infusion_speed_stone", new ItemStack(ModBlocks.INFUSION_SPEED_STONE.get()),
                AspectList.of(ModAspects.AIR, 250).add(ModAspects.ORDER, 250).add(ModAspects.ENTROPY, 250),
                Map.of('S', Ingredient.of(ModBlocks.ARCANE_STONE.block()),
                        'N', Ingredient.of(ModTags.Items.NITOR),
                        'G', Ingredient.of(Blocks.GOLD_BLOCK)),
                "SNS", "NGN", "SNS");
        arcane(output, "infusion_cost_stone", new ItemStack(ModBlocks.INFUSION_COST_STONE.get()),
                AspectList.of(ModAspects.WATER, 250).add(ModAspects.ORDER, 250).add(ModAspects.ENTROPY, 250),
                Map.of('S', Ingredient.of(ModBlocks.ARCANE_STONE.block()),
                        'A', Ingredient.of(ModItems.ALUMENTUM),
                        'G', Ingredient.of(Blocks.GOLD_BLOCK)),
                "SAS", "AGA", "SAS");
    }

    private static Ingredient essence(net.minecraft.core.Holder<me.moonscenty.alchemia.aspect.Aspect> aspect) {
        return net.neoforged.neoforge.common.crafting.DataComponentIngredient.of(false,
                me.moonscenty.alchemia.item.CrystallizedEssenceItem.of(aspect));
    }

    private static Ingredient potion(net.minecraft.world.item.Item bottle,
            net.minecraft.core.Holder<net.minecraft.world.item.alchemy.Potion> potion) {
        return net.neoforged.neoforge.common.crafting.DataComponentIngredient.of(false,
                net.minecraft.world.item.alchemy.PotionContents.createItemStack(bottle, potion));
    }

    private void baubles(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MUNDANE_AMULET.get())
                .pattern(" S ").pattern("S S").pattern(" I ")
                .define('S', Items.STRING).define('I', ModTags.Items.INGOTS_BRASS)
                .unlockedBy("has_input", has(ModTags.Items.INGOTS_BRASS)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MUNDANE_RING.get())
                .pattern("NNN").pattern("N N").pattern("NNN")
                .define('N', ModTags.Items.NUGGETS_BRASS)
                .unlockedBy("has_input", has(ModTags.Items.NUGGETS_BRASS)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MUNDANE_BELT.get())
                .pattern(" L ").pattern("L L").pattern(" I ")
                .define('L', Items.LEATHER).define('I', ModTags.Items.INGOTS_BRASS)
                .unlockedBy("has_input", has(ModTags.Items.INGOTS_BRASS)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FANCY_AMULET.get())
                .pattern(" S ").pattern("SGS").pattern(" I ")
                .define('S', Items.STRING).define('G', Items.DIAMOND).define('I', Items.GOLD_INGOT)
                .unlockedBy("has_input", has(Items.DIAMOND)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FANCY_RING.get())
                .pattern("NGN").pattern("N N").pattern("NNN")
                .define('N', Items.GOLD_NUGGET).define('G', Items.DIAMOND)
                .unlockedBy("has_input", has(Items.DIAMOND)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FANCY_BELT.get())
                .pattern(" L ").pattern("LGL").pattern(" I ")
                .define('L', Items.LEATHER).define('G', Items.DIAMOND).define('I', Items.GOLD_INGOT)
                .unlockedBy("has_input", has(Items.DIAMOND)).save(output);
        SpecialRecipeBuilder.special(me.moonscenty.alchemia.crafting.TripleMeatTreatRecipe::new)
                .save(output, Alchemia.id("triple_meat_treat").toString());

        Ingredient charm = Ingredient.of(ModItems.PRIMAL_CHARM);
        Ingredient amber = Ingredient.of(ModItems.AMBER);
        Ingredient fabric = Ingredient.of(ModItems.ENCHANTED_FABRIC);
        Ingredient nitor = Ingredient.of(ModTags.Items.NITOR);
        Ingredient scribing = Ingredient.of(ModItems.SCRIBING_TOOLS);
        Ingredient balanced = Ingredient.of(ModItems.BALANCED_SHARD);
        Ingredient air = Ingredient.of(ModItems.SHARDS.get(me.moonscenty.alchemia.block.CrystalType.AIR).get());
        Ingredient earth = Ingredient.of(ModItems.SHARDS.get(me.moonscenty.alchemia.block.CrystalType.EARTH).get());
        Ingredient fire = Ingredient.of(ModItems.SHARDS.get(me.moonscenty.alchemia.block.CrystalType.FIRE).get());
        Ingredient water = Ingredient.of(ModItems.SHARDS.get(me.moonscenty.alchemia.block.CrystalType.WATER).get());

        infusion(output, "thaumostatic_girdle", Ingredient.of(ModItems.MUNDANE_BELT),
                List.of(air, Ingredient.of(Items.FEATHER), Ingredient.of(Items.GOLD_INGOT), earth,
                        Ingredient.of(Items.FEATHER), Ingredient.of(Items.GOLD_INGOT)),
                new ItemStack(ModItems.THAUMOSTATIC_GIRDLE.get()), AspectList.of(ModAspects.FLIGHT, 16)
                        .add(ModAspects.ENERGY, 32).add(ModAspects.AIR, 32).add(ModAspects.MOTION, 16),
                8, "thaumostatic_girdle");
        infusion(output, "vis_amulet", Ingredient.of(ModItems.MUNDANE_AMULET),
                List.of(charm, air, fire, charm, water, earth),
                new ItemStack(ModItems.VIS_AMULET.get()), AspectList.of(ModAspects.AURA, 24)
                        .add(ModAspects.ENERGY, 128).add(ModAspects.VOID, 24),
                6, "vis_amulet");
        infusion(output, "runic_amulet", Ingredient.of(ModItems.MUNDANE_AMULET),
                List.of(charm, amber, fabric, nitor, nitor, scribing),
                new ItemStack(ModItems.RUNIC_AMULET.get()), AspectList.of(ModAspects.PROTECT, 30)
                        .add(ModAspects.ENERGY, 50), 4, "runic_shielding");
        infusion(output, "runic_ring", Ingredient.of(ModItems.MUNDANE_RING),
                List.of(charm, amber, fabric, nitor, scribing),
                new ItemStack(ModItems.RUNIC_RING.get()), AspectList.of(ModAspects.PROTECT, 25)
                        .add(ModAspects.ENERGY, 45), 3, "runic_shielding");
        infusion(output, "runic_girdle", Ingredient.of(ModItems.MUNDANE_BELT),
                List.of(charm, amber, fabric, nitor, nitor, nitor, scribing),
                new ItemStack(ModItems.RUNIC_GIRDLE.get()), AspectList.of(ModAspects.PROTECT, 40)
                        .add(ModAspects.ENERGY, 64), 4, "runic_shielding");
        infusion(output, "emergency_runic_amulet", Ingredient.of(ModItems.RUNIC_AMULET),
                List.of(balanced, earth, earth, potion(Items.POTION, net.minecraft.world.item.alchemy.Potions.STRONG_STRENGTH),
                        earth, earth),
                new ItemStack(ModItems.EMERGENCY_RUNIC_AMULET.get()), AspectList.of(ModAspects.PROTECT, 30)
                        .add(ModAspects.EARTH, 50).add(ModAspects.VOID, 32), 7, "runic_special");
        infusion(output, "charged_runic_ring", Ingredient.of(ModItems.RUNIC_RING),
                List.of(balanced, fire, fire, potion(Items.POTION, net.minecraft.world.item.alchemy.Potions.STRONG_SWIFTNESS),
                        fire, fire),
                new ItemStack(ModItems.CHARGED_RUNIC_RING.get()), AspectList.of(ModAspects.PROTECT, 25)
                        .add(ModAspects.ENERGY, 64), 6, "runic_special");
        infusion(output, "revitalizing_runic_ring", Ingredient.of(ModItems.RUNIC_RING),
                List.of(balanced, water, water, potion(Items.POTION, net.minecraft.world.item.alchemy.Potions.LONG_REGENERATION),
                        water, water),
                new ItemStack(ModItems.REVITALIZING_RUNIC_RING.get()), AspectList.of(ModAspects.PROTECT, 25)
                        .add(ModAspects.WATER, 32).add(ModAspects.LIFE, 32), 6, "runic_special");
        infusion(output, "kinetic_runic_girdle", Ingredient.of(ModItems.RUNIC_GIRDLE),
                List.of(balanced, air, air, potion(Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.STRONG_HARMING),
                        air, air),
                new ItemStack(ModItems.KINETIC_RUNIC_GIRDLE.get()), AspectList.of(ModAspects.PROTECT, 40)
                        .add(ModAspects.AIR, 64), 7, "runic_special");
        infusion(output, "verdant_heart", Ingredient.of(ModItems.FANCY_RING),
                List.of(Ingredient.of(ModItems.PRIMORDIAL_PEARL), essence(ModAspects.LIFE),
                        Ingredient.of(Items.MILK_BUCKET), essence(ModAspects.PLANT)),
                new ItemStack(ModItems.VERDANT_HEART.get()), AspectList.of(ModAspects.LIFE, 48)
                        .add(ModAspects.ORDER, 24).add(ModAspects.PLANT, 48), 5, "verdant_heart");
        infusion(output, "verdant_heart_lifegiver", Ingredient.of(ModItems.VERDANT_HEART),
                List.of(Ingredient.of(Items.GOLDEN_APPLE), essence(ModAspects.LIFE),
                        potion(Items.POTION, net.minecraft.world.item.alchemy.Potions.REGENERATION), essence(ModAspects.MAN)),
                me.moonscenty.alchemia.item.bauble.VerdantHeartItem.of(new ItemStack(ModItems.VERDANT_HEART.get()),
                        me.moonscenty.alchemia.item.bauble.VerdantHeartItem.LIFEGIVER),
                AspectList.of(ModAspects.LIFE, 64).add(ModAspects.MAN, 64), 5, "verdant_heart_lifegiver");
        infusion(output, "verdant_heart_sustainer", Ingredient.of(ModItems.VERDANT_HEART),
                List.of(Ingredient.of(ModItems.TRIPLE_MEAT_TREAT), essence(ModAspects.DESIRE),
                        potion(Items.POTION, net.minecraft.world.item.alchemy.Potions.WATER_BREATHING), essence(ModAspects.AIR)),
                me.moonscenty.alchemia.item.bauble.VerdantHeartItem.of(new ItemStack(ModItems.VERDANT_HEART.get()),
                        me.moonscenty.alchemia.item.bauble.VerdantHeartItem.SUSTAINER),
                AspectList.of(ModAspects.DESIRE, 64).add(ModAspects.AIR, 64), 5, "verdant_heart_lifegiver");
        // the altar works the real inputs out from what is on it; this ring and essentia are for the book
        output.accept(Alchemia.id("runic_augmentation"), new InfusionRecipe(
                Ingredient.of(ModTags.Items.RUNIC_AUGMENTABLE),
                List.of(Ingredient.of(Items.DIAMOND), Ingredient.of(ModItems.SALIS_MUNDUS)), ItemStack.EMPTY,
                AspectList.of(ModAspects.PROTECT, 16).add(ModAspects.CRYSTAL, 16).add(ModAspects.ENERGY, 32), 5,
                Optional.of(Alchemia.id("runic_augmentation")), Optional.empty(), Optional.empty(), true), null);
    }

    private void arcane(RecipeOutput output, String name, ItemStack result, AspectList cost,
            Map<Character, Ingredient> key, String... pattern) {
        arcane(output, name, null, result, cost, key, pattern);
    }

    private void arcane(RecipeOutput output, String name, String research, ItemStack result, AspectList cost,
            Map<Character, Ingredient> key, String... pattern) {
        output.accept(Alchemia.id(name), new ArcaneShapedRecipe("", ShapedRecipePattern.of(key, pattern), result,
                cost, Optional.ofNullable(research).map(Alchemia::id)), null);
    }

    private void shapeless(RecipeOutput output, String name, String research, ItemStack result, AspectList cost,
            Ingredient... ingredients) {
        NonNullList<Ingredient> laid = NonNullList.of(Ingredient.EMPTY, ingredients);
        output.accept(Alchemia.id(name), new ArcaneShapelessRecipe("", laid, result, cost,
                Optional.ofNullable(research).map(Alchemia::id)), null);
    }

    private void distillery(RecipeOutput output) {
        arcane(output, "filter", "distillation", new ItemStack(ModItems.FILTER.get(), 2),
                AspectList.of(ModAspects.ORDER, 15).add(ModAspects.WATER, 15),
                Map.of('G', Ingredient.of(Tags.Items.INGOTS_GOLD),
                        'W', Ingredient.of(ModBlocks.SILVERWOOD.planks())), "GWG");

        arcane(output, "essentia_smelter", "distillation", new ItemStack(ModBlocks.ESSENTIA_SMELTER.get()),
                AspectList.of(ModAspects.FIRE, 25).add(ModAspects.WATER, 25),
                Map.of('B', Ingredient.of(ModItems.BRASS_PLATE),
                        'C', Ingredient.of(ModBlocks.CRUCIBLE),
                        'F', Ingredient.of(Items.FURNACE),
                        'S', Ingredient.of(Tags.Items.COBBLESTONES)),
                "BCB", "SFS", "SSS");

        arcane(output, "alchemical_construct", "distillation", new ItemStack(ModBlocks.ALCHEMICAL_CONSTRUCT.get()),
                AspectList.of(ModAspects.WATER, 50).add(ModAspects.ORDER, 50),
                Map.of('I', Ingredient.of(Tags.Items.INGOTS_IRON), 'V', Ingredient.of(ModBlocks.TUBE_VALVE),
                        'T', Ingredient.of(ModBlocks.TUBE), 'W', Ingredient.of(ModBlocks.GREATWOOD.planks())),
                "IVI", "TWT", "IVI");
        arcane(output, "advanced_alchemical_construct", "void_smelter",
                new ItemStack(ModBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get()),
                AspectList.of(ModAspects.WATER, 200).add(ModAspects.ORDER, 200).add(ModAspects.EARTH, 200),
                Map.of('V', Ingredient.of(ModItems.VOID_INGOT), 'A', Ingredient.of(ModBlocks.ALCHEMICAL_CONSTRUCT),
                        'P', Ingredient.of(ModItems.PRIMORDIAL_PEARL)),
                "VAV", "APA", "VAV");
        arcane(output, "alchemium_smelter", "alchemium_smelter", new ItemStack(ModBlocks.ALCHEMIUM_SMELTER.get()),
                AspectList.of(ModAspects.FIRE, 350).add(ModAspects.WATER, 350),
                Map.of('B', Ingredient.of(ModItems.BRASS_PLATE),
                        'F', Ingredient.of(ModBlocks.ESSENTIA_SMELTER),
                        'G', Ingredient.of(ModBlocks.ALCHEMICAL_CONSTRUCT),
                        'I', Ingredient.of(ModItems.ALCHEMIUM_INGOT)),
                "BFB", "IGI", "III");
        arcane(output, "void_smelter", "void_smelter", new ItemStack(ModBlocks.VOID_SMELTER.get()),
                AspectList.of(ModAspects.FIRE, 750).add(ModAspects.WATER, 750),
                Map.of('B', Ingredient.of(ModItems.BRASS_PLATE),
                        'F', Ingredient.of(ModBlocks.ALCHEMIUM_SMELTER),
                        'G', Ingredient.of(ModBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT),
                        'I', Ingredient.of(ModItems.VOID_INGOT)),
                "BFB", "IGI", "III");
        arcane(output, "arcane_bore_base", "arcane_bore", new ItemStack(ModBlocks.ARCANE_BORE_BASE.get()),
                AspectList.of(ModAspects.AIR, 100).add(ModAspects.ORDER, 100),
                Map.of('W', Ingredient.of(ModBlocks.GREATWOOD.planks()),
                        'I', Ingredient.of(Tags.Items.INGOTS_IRON),
                        'D', Ingredient.of(Items.DISPENSER),
                        'G', Ingredient.of(ModItems.BRASS_GEAR)),
                "WIW", "IDI", "WGW");
        arcane(output, "mnemonic_matrix", "thaumatorium", new ItemStack(ModBlocks.MNEMONIC_MATRIX.get()),
                AspectList.of(ModAspects.FIRE, 50).add(ModAspects.WATER, 50).add(ModAspects.ORDER, 50),
                Map.of('I', Ingredient.of(Tags.Items.INGOTS_IRON),
                        'A', Ingredient.of(ModItems.AMBER),
                        'B', Ingredient.of(ModItems.CLOCKWORK_MIND)),
                "IAI", "ABA", "IAI");
        arcane(output, "centrifuge", "centrifuge", new ItemStack(ModBlocks.CENTRIFUGE.get()),
                AspectList.of(ModAspects.WATER, 100).add(ModAspects.ORDER, 100).add(ModAspects.ENTROPY, 100),
                Map.of('T', Ingredient.of(ModBlocks.TUBE),
                        'R', Ingredient.of(ModItems.MORPHIC_RESONATOR),
                        'C', Ingredient.of(ModBlocks.ALCHEMICAL_CONSTRUCT),
                        'P', Ingredient.of(ModItems.BRASS_GEAR)),
                " T ", "RCP", " T ");
        arcane(output, "crystallizer", "crystallizer", new ItemStack(ModBlocks.CRYSTALLIZER.get()),
                AspectList.of(ModAspects.WATER, 100).add(ModAspects.EARTH, 250).add(ModAspects.ORDER, 100),
                Map.of('I', Ingredient.of(Tags.Items.INGOTS_IRON),
                        'D', Ingredient.of(ModItems.BRASS_GEAR),
                        'Q', Ingredient.of(ModItems.BALANCED_SHARD),
                        'C', Ingredient.of(ModBlocks.ALCHEMICAL_CONSTRUCT),
                        'W', Ingredient.of(net.minecraft.tags.ItemTags.PLANKS),
                        'T', Ingredient.of(ModBlocks.TUBE)),
                "IDI", "QCQ", "WTW");
        arcane(output, "smelter_aux", "improved_distillation", new ItemStack(ModBlocks.SMELTER_AUX.get()),
                AspectList.of(ModAspects.AIR, 50).add(ModAspects.WATER, 50).add(ModAspects.EARTH, 25),
                Map.of('W', Ingredient.of(ModBlocks.GREATWOOD.planks()),
                        'T', Ingredient.of(ModBlocks.TUBE_FILTER),
                        'R', Ingredient.of(ModItems.BRASS_PLATE),
                        'G', Ingredient.of(ModBlocks.ALCHEMICAL_CONSTRUCT),
                        'I', Ingredient.of(Tags.Items.INGOTS_IRON),
                        'B', Ingredient.of(ModBlocks.BELLOWS)),
                "WTW", "RGR", "IBI");
        arcane(output, "smelter_vent", "improved_distillation", new ItemStack(ModBlocks.SMELTER_VENT.get()),
                AspectList.of(ModAspects.AIR, 125).add(ModAspects.WATER, 125).add(ModAspects.ORDER, 100),
                Map.of('I', Ingredient.of(Tags.Items.INGOTS_IRON),
                        'B', Ingredient.of(ModItems.BRASS_INGOT),
                        'M', Ingredient.of(ModItems.MORPHIC_RESONATOR),
                        'G', Ingredient.of(ModBlocks.ALCHEMICAL_CONSTRUCT),
                        'F', Ingredient.of(ModItems.FILTER)),
                "IBI", "MGF", "IBI");

        arcane(output, "alembic", "distillation", new ItemStack(ModBlocks.ALEMBIC.get()),
                AspectList.of(ModAspects.AIR, 15).add(ModAspects.WATER, 25),
                Map.of('W', Ingredient.of(ModBlocks.GREATWOOD.planks()),
                        'F', Ingredient.of(ModItems.FILTER),
                        'S', Ingredient.of(ModItems.BRASS_PLATE),
                        'B', Ingredient.of(Items.BUCKET)),
                "WFW", "SBS", "WFW");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.PHIAL.get(), 8)
                .pattern(" C ").pattern("G G").pattern(" G ")
                .define('G', Tags.Items.GLASS_BLOCKS)
                .define('C', Items.CLAY_BALL)
                .unlockedBy("has_glass", has(Tags.Items.GLASS_BLOCKS))
                .save(output);

        arcane(output, "void_jar", "jar_void", new ItemStack(ModBlocks.VOID_JAR.get()),
                AspectList.of(ModAspects.WATER, 25).add(ModAspects.ENTROPY, 25),
                Map.of('O', Ingredient.of(Items.OBSIDIAN), 'J', Ingredient.of(ModBlocks.JAR),
                        'P', Ingredient.of(Items.BLAZE_POWDER)),
                "O", "J", "P");
        arcane(output, "jar", "jar_label", new ItemStack(ModBlocks.JAR.get()),
                AspectList.of(ModAspects.WATER, 5),
                Map.of('G', Ingredient.of(Tags.Items.GLASS_PANES),
                        'W', Ingredient.of(ItemTags.WOODEN_SLABS)),
                "GWG", "G G", "GGG");

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.JAR_LABEL.get(), 4)
                .requires(Tags.Items.DYES_BLACK)
                .requires(Tags.Items.SLIME_BALLS)
                .requires(Items.PAPER, 4)
                .unlockedBy("has_paper", has(Items.PAPER))
                .save(output);

        SpecialRecipeBuilder.special(LabelRecipe::new).save(output, Alchemia.id("jar_label_writing").toString());

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.JAR_BRACE.get(), 2)
                .pattern("NSN").pattern("S S").pattern("NSN")
                .define('N', ModTags.Items.NUGGETS_BRASS)
                .define('S', Tags.Items.RODS_WOODEN)
                .unlockedBy("has_nugget", has(ModTags.Items.NUGGETS_BRASS))
                .save(output);

        tubes(output);
    }

    private void tubes(RecipeOutput output) {
        arcane(output, "tube", "tubes", new ItemStack(ModBlocks.TUBE.get(), 8),
                AspectList.of(ModAspects.WATER, 5).add(ModAspects.ORDER, 5),
                Map.of('I', Ingredient.of(Tags.Items.INGOTS_IRON),
                        'B', Ingredient.of(Tags.Items.NUGGETS_GOLD),
                        'G', Ingredient.of(Tags.Items.GLASS_BLOCKS),
                        'Q', Ingredient.of(ModTags.Items.NUGGETS_QUICKSILVER)),
                " Q ", "IGI", " B ");

        arcane(output, "essentia_resonator", "tubes", new ItemStack(ModItems.ESSENTIA_RESONATOR.get()),
                AspectList.of(ModAspects.WATER, 5).add(ModAspects.AIR, 5),
                Map.of('I', Ingredient.of(Tags.Items.INGOTS_IRON),
                        'N', Ingredient.of(Tags.Items.GEMS_QUARTZ),
                        'S', Ingredient.of(Tags.Items.RODS_WOODEN)),
                "I I", "INI", " S ");

        shapeless(output, "tube_valve", "tubes", new ItemStack(ModBlocks.TUBE_VALVE.get()),
                AspectList.of(ModAspects.WATER, 5).add(ModAspects.ORDER, 5),
                Ingredient.of(ModBlocks.TUBE), Ingredient.of(Items.LEVER));

        shapeless(output, "tube_filter", "tube_filter", new ItemStack(ModBlocks.TUBE_FILTER.get()),
                AspectList.of(ModAspects.WATER, 5).add(ModAspects.ORDER, 10),
                Ingredient.of(ModBlocks.TUBE), Ingredient.of(ModItems.FILTER));

        shapeless(output, "tube_restrict", "tube_filter", new ItemStack(ModBlocks.TUBE_RESTRICT.get()),
                AspectList.of(ModAspects.WATER, 5).add(ModAspects.EARTH, 10),
                Ingredient.of(ModBlocks.TUBE), Ingredient.of(Tags.Items.STONES));

        shapeless(output, "tube_oneway", "tube_filter", new ItemStack(ModBlocks.TUBE_ONEWAY.get()),
                AspectList.of(ModAspects.WATER, 5).add(ModAspects.ORDER, 10).add(ModAspects.ENTROPY, 10),
                Ingredient.of(ModBlocks.TUBE), Ingredient.of(Tags.Items.DYES_BLUE));

        arcane(output, "tube_buffer", "tube_filter", new ItemStack(ModBlocks.TUBE_BUFFER.get()),
                AspectList.of(ModAspects.WATER, 25).add(ModAspects.ORDER, 25),
                Map.of('P', Ingredient.of(ModItems.PHIAL),
                        'V', Ingredient.of(ModBlocks.TUBE_VALVE),
                        'R', Ingredient.of(ModBlocks.TUBE_RESTRICT),
                        'T', Ingredient.of(ModBlocks.TUBE)),
                "PVP", "T T", "PRP");
    }

    private void infusion(RecipeOutput output) {
        arcane(output, "infusion_matrix", "infusion", new ItemStack(ModBlocks.INFUSION_MATRIX.get()),
                AspectList.of(ModAspects.ORDER, 100),
                Map.of('S', Ingredient.of(ModBlocks.ARCANE_STONE_BRICKS.block()),
                        'B', Ingredient.of(ModTags.Items.SHARDS),
                        'N', Ingredient.of(ModItems.BALANCED_SHARD)),
                "SBS", "BNB", "SBS");

        arcane(output, "ancient_pedestal", "infusion", new ItemStack(ModBlocks.ANCIENT_PEDESTAL.get()),
                AspectList.of(ModAspects.AIR, 25).add(ModAspects.ENTROPY, 25).add(ModAspects.ORDER, 150),
                Map.of('S', Ingredient.of(ModBlocks.ANCIENT_STONE.slab()),
                        'B', Ingredient.of(ModBlocks.ANCIENT_STONE.block())),
                "SSS", " B ", "SSS");
        arcane(output, "eldritch_pedestal", "infusion", new ItemStack(ModBlocks.ELDRITCH_PEDESTAL.get()),
                AspectList.of(ModAspects.AIR, 25).add(ModAspects.ENTROPY, 150).add(ModAspects.ORDER, 25),
                Map.of('S', Ingredient.of(ModBlocks.ELDRITCH_STONE.slab()),
                        'B', Ingredient.of(ModBlocks.ELDRITCH_STONE.block())),
                "SSS", " B ", "SSS");
        for (net.minecraft.world.item.DyeColor dye : net.minecraft.world.item.DyeColor.values()) {
            arcane(output, "banner_" + dye.getName(), "banners",
                    me.moonscenty.alchemia.block.BannerItem.of(dye, null),
                    AspectList.of(ModAspects.WATER, 5).add(ModAspects.EARTH, 5),
                    Map.of('W', Ingredient.of(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                                    net.minecraft.resources.ResourceLocation.withDefaultNamespace(dye.getName() + "_wool"))),
                            'S', Ingredient.of(Items.STICK),
                            'B', Ingredient.of(net.minecraft.tags.ItemTags.WOODEN_SLABS)),
                    "WS", "WS", "WB");
        }
        infusion(output, "eldritch_eye", Ingredient.of(Items.ENDER_EYE),
                List.of(Ingredient.of(ModItems.VOID_SEED), Ingredient.of(Items.GOLD_INGOT)),
                new ItemStack(ModItems.ELDRITCH_EYE.get()),
                AspectList.of(ModAspects.ELDRITCH, 64).add(ModAspects.VOID, 16).add(ModAspects.DARKNESS, 16)
                        .add(ModAspects.MOTION, 16), 5, "oculus");
        arcane(output, "arcane_pedestal", "infusion", new ItemStack(ModBlocks.ARCANE_PEDESTAL.get()),
                AspectList.of(ModAspects.AIR, 5),
                Map.of('S', Ingredient.of(ModBlocks.ARCANE_STONE.slab()),
                        'B', Ingredient.of(ModBlocks.ARCANE_STONE.block())),
                "SSS", " B ", "SSS");

        rod(output, "obsidian", Ingredient.of(Items.OBSIDIAN), 3,
                AspectList.of(ModAspects.EARTH, 12).add(ModAspects.ENERGY, 6).add(ModAspects.DARKNESS, 6),
                CrystalType.EARTH);
        rod(output, "ice", Ingredient.of(Items.ICE), 3,
                AspectList.of(ModAspects.WATER, 12).add(ModAspects.ENERGY, 6).add(ModAspects.COLD, 6),
                CrystalType.WATER);
        rod(output, "quartz", Ingredient.of(Items.QUARTZ_BLOCK), 3,
                AspectList.of(ModAspects.ORDER, 12).add(ModAspects.ENERGY, 6).add(ModAspects.CRYSTAL, 6),
                CrystalType.ORDER);
        rod(output, "reed", Ingredient.of(Items.SUGAR_CANE), 3,
                AspectList.of(ModAspects.AIR, 12).add(ModAspects.ENERGY, 6).add(ModAspects.MOTION, 6),
                CrystalType.AIR);
        rod(output, "blaze", Ingredient.of(Items.BLAZE_ROD), 3,
                AspectList.of(ModAspects.FIRE, 12).add(ModAspects.ENERGY, 6).add(ModAspects.BEAST, 6),
                CrystalType.FIRE);
        rod(output, "bone", Ingredient.of(Items.BONE), 3,
                AspectList.of(ModAspects.ENTROPY, 12).add(ModAspects.ENERGY, 6).add(ModAspects.UNDEAD, 6),
                CrystalType.ENTROPY);

        List<Ingredient> around = new java.util.ArrayList<>();
        around.add(Ingredient.of(ModItems.BALANCED_SHARD));
        AspectList evenly = AspectList.of(ModAspects.ENERGY, 9);
        for (CrystalType type : CrystalType.values()) {
            if (type.generatesNaturally()) {
                around.add(Ingredient.of(ModItems.SHARDS.get(type)));
                evenly = evenly.add(type.aspect(), 9);
            }
        }
        infusion(output, "wand_rod_silverwood", Ingredient.of(ModBlocks.SILVERWOOD.log()), around,
                new ItemStack(ModItems.WAND_RODS.get("silverwood").get()), evenly, 5, "wand_rods");

        AspectList eachPrimal = AspectList.EMPTY;
        for (var primal : ModAspects.primals()) {
            eachPrimal = eachPrimal.add(primal, 28);
        }
        infusion(output, "wand_rod_primal_staff", Ingredient.of(ModItems.WAND_RODS.get("silverwood")),
                List.of(Ingredient.of(ModItems.PRIMAL_CHARM), Ingredient.of(ModItems.WAND_RODS.get("obsidian")),
                        Ingredient.of(ModItems.WAND_RODS.get("blaze")), Ingredient.of(ModItems.WAND_RODS.get("ice")),
                        Ingredient.of(ModItems.PRIMAL_CHARM), Ingredient.of(ModItems.WAND_RODS.get("quartz")),
                        Ingredient.of(ModItems.WAND_RODS.get("bone")), Ingredient.of(ModItems.WAND_RODS.get("reed"))),
                new ItemStack(ModItems.WAND_RODS.get("primal_staff").get()),
                eachPrimal.add(ModAspects.ENERGY, 56), 8, "staff_primal");

        infusion(output, "traveller_boots", Ingredient.of(Items.LEATHER_BOOTS),
                List.of(Ingredient.of(ModItems.SHARDS.get(CrystalType.AIR)),
                        Ingredient.of(ModItems.SHARDS.get(CrystalType.AIR)),
                        Ingredient.of(ModItems.ENCHANTED_FABRIC), Ingredient.of(ModItems.ENCHANTED_FABRIC),
                        Ingredient.of(Items.FEATHER),
                        Ingredient.of(Items.COD, Items.SALMON, Items.TROPICAL_FISH, Items.PUFFERFISH)),
                new ItemStack(ModItems.TRAVELLER_BOOTS.get()),
                AspectList.of(ModAspects.FLIGHT, 25).add(ModAspects.MOTION, 25), 1, "traveller_boots");

        voidRobe(output, "void_robe_helm", "void_helmet",
                List.of(Ingredient.of(ModItems.GOGGLES), Ingredient.of(ModItems.ENCHANTED_FABRIC),
                        Ingredient.of(ModItems.ENCHANTED_FABRIC), Ingredient.of(ModItems.SALIS_MUNDUS),
                        Ingredient.of(ModItems.ENCHANTED_FABRIC), Ingredient.of(ModItems.ENCHANTED_FABRIC)),
                AspectList.of(ModAspects.METAL, 16).add(ModAspects.SENSES, 16).add(ModAspects.PROTECT, 16)
                        .add(ModAspects.ENERGY, 16).add(ModAspects.ELDRITCH, 16).add(ModAspects.VOID, 16));
        voidRobe(output, "void_robe_chest", "void_chestplate",
                List.of(Ingredient.of(ModItems.ROBES.get("cloth_chest")), Ingredient.of(ModItems.VOID_PLATE),
                        Ingredient.of(ModItems.VOID_PLATE), Ingredient.of(ModItems.SALIS_MUNDUS),
                        Ingredient.of(ModItems.ENCHANTED_FABRIC), Ingredient.of(Items.LEATHER)),
                AspectList.of(ModAspects.METAL, 24).add(ModAspects.PROTECT, 24).add(ModAspects.ENERGY, 16)
                        .add(ModAspects.ELDRITCH, 16).add(ModAspects.VOID, 24));
        voidRobe(output, "void_robe_legs", "void_leggings",
                List.of(Ingredient.of(ModItems.ROBES.get("cloth_legs")), Ingredient.of(ModItems.VOID_PLATE),
                        Ingredient.of(ModItems.VOID_PLATE), Ingredient.of(ModItems.SALIS_MUNDUS),
                        Ingredient.of(ModItems.ENCHANTED_FABRIC), Ingredient.of(Items.LEATHER)),
                AspectList.of(ModAspects.METAL, 20).add(ModAspects.PROTECT, 20).add(ModAspects.ENERGY, 16)
                        .add(ModAspects.ELDRITCH, 16).add(ModAspects.VOID, 20));

        fortress(output, "fortress_helm", "alchemium_helmet", 2,
                List.of(Ingredient.of(Items.GOLD_INGOT), Ingredient.of(Items.GOLD_INGOT),
                        Ingredient.of(Items.EMERALD)),
                AspectList.of(ModAspects.METAL, 24).add(ModAspects.PROTECT, 16).add(ModAspects.ENERGY, 16));
        fortress(output, "fortress_chest", "alchemium_chestplate", 4,
                List.of(Ingredient.of(Items.GOLD_INGOT), Ingredient.of(Items.LEATHER)),
                AspectList.of(ModAspects.METAL, 24).add(ModAspects.PROTECT, 24).add(ModAspects.ENERGY, 16));
        fortress(output, "fortress_legs", "alchemium_leggings", 3,
                List.of(Ingredient.of(Items.GOLD_INGOT), Ingredient.of(Items.LEATHER)),
                AspectList.of(ModAspects.METAL, 24).add(ModAspects.PROTECT, 20).add(ModAspects.ENERGY, 16));

        // the input helm itself comes back, keeping its enchantments and wear
        helmFitting(output, "helm_goggles", HelmFitting.GOGGLES,
                List.of(Ingredient.of(Items.SLIME_BALL), Ingredient.of(ModItems.GOGGLES)),
                AspectList.of(ModAspects.SENSES, 32).add(ModAspects.AURA, 16).add(ModAspects.PROTECT, 16),
                HELM_GOGGLES_UNSTABLE);

        mask(output, "mask_grinning_devil", HelmFitting.GRINNING_DEVIL,
                List.of(Ingredient.of(Items.INK_SAC), Ingredient.of(ModBlocks.SHIMMERLEAF.get()),
                        Ingredient.of(ModItems.ZOMBIE_BRAIN)),
                AspectList.of(ModAspects.MIND, 64).add(ModAspects.LIFE, 64).add(ModAspects.PROTECT, 16));
        mask(output, "mask_angry_ghost", HelmFitting.ANGRY_GHOST,
                List.of(Ingredient.of(Items.BONE_MEAL), Ingredient.of(Items.POISONOUS_POTATO),
                        Ingredient.of(Items.WITHER_SKELETON_SKULL)),
                AspectList.of(ModAspects.ENTROPY, 64).add(ModAspects.DEATH, 64).add(ModAspects.PROTECT, 16));
        mask(output, "mask_sipping_fiend", HelmFitting.SIPPING_FIEND,
                List.of(Ingredient.of(Items.RED_DYE), Ingredient.of(Items.GHAST_TEAR),
                        Ingredient.of(Items.MILK_BUCKET)),
                AspectList.of(ModAspects.UNDEAD, 64).add(ModAspects.LIFE, 64).add(ModAspects.PROTECT, 16));

        infusion(output, "seal_harvest", Ingredient.of(ModItems.SEAL_BLANK),
                List.of(Ingredient.of(Items.WHEAT_SEEDS), Ingredient.of(Items.PUMPKIN_SEEDS),
                        Ingredient.of(Items.MELON_SEEDS), Ingredient.of(Items.COCOA_BEANS),
                        Ingredient.of(Items.SUGAR_CANE), Ingredient.of(Items.CACTUS)),
                new ItemStack(ModItems.SEALS.get("harvest").get()),
                AspectList.of(ModAspects.PLANT, 4).add(ModAspects.SENSES, 4).add(ModAspects.MAN, 4), 0,
                "seal_harvest");
        infusion(output, "seal_butcher", Ingredient.of(ModItems.SEALS.get("guard").get()),
                List.of(Ingredient.of(Items.LEATHER), Ingredient.of(net.minecraft.tags.ItemTags.WOOL),
                        Ingredient.of(Items.RABBIT_HIDE), Ingredient.of(Items.PORKCHOP),
                        Ingredient.of(Items.MUTTON), Ingredient.of(Items.BEEF)),
                new ItemStack(ModItems.SEALS.get("butcher").get()),
                AspectList.of(ModAspects.BEAST, 4).add(ModAspects.SENSES, 4).add(ModAspects.MAN, 4), 0,
                "seal_butcher");
        infusion(output, "seal_breaker", Ingredient.of(ModItems.SEAL_BLANK),
                List.of(Ingredient.of(Items.GOLDEN_PICKAXE), Ingredient.of(Items.GOLDEN_AXE),
                        Ingredient.of(Items.GOLDEN_SHOVEL)),
                new ItemStack(ModItems.SEALS.get("breaker").get()),
                AspectList.of(ModAspects.TOOL, 4).add(ModAspects.ENTROPY, 4).add(ModAspects.MAN, 4), 1,
                "seal_break");
        infusion(output, "bioalchemic_mind", Ingredient.of(ModItems.CLOCKWORK_MIND),
                List.of(Ingredient.of(ModItems.ZOMBIE_BRAIN), Ingredient.of(Items.CLOCK)),
                new ItemStack(ModItems.BIOALCHEMIC_MIND.get()),
                AspectList.of(ModAspects.MIND, 16).add(ModAspects.MECHANISM, 16), BIOALCHEMIC_UNSTABLE,
                "bioalchemic_mind");

        infusion(output, "aura_totem_pull", Ingredient.of(ModBlocks.SILVERWOOD.planks()),
                List.of(Ingredient.of(ModBlocks.GREATWOOD.planks()), Ingredient.of(Items.CHEST),
                        Ingredient.of(ModItems.BALANCED_SHARD), Ingredient.of(ModItems.SALIS_MUNDUS),
                        Ingredient.of(ModItems.AMBER), Ingredient.of(Items.CARROT_ON_A_STICK),
                        Ingredient.of(ModItems.WISPY_ESSENCE)),
                new ItemStack(ModBlocks.AURA_TOTEM_PULL.get()),
                AspectList.of(ModAspects.ENERGY, 25).add(ModAspects.AURA, 25).add(ModAspects.MOTION, 25)
                        .add(ModAspects.DESIRE, 25), TOTEM_UNSTABLE, "aura_totem_pull");
        infusion(output, "aura_totem_push", Ingredient.of(ModBlocks.SILVERWOOD.planks()),
                List.of(Ingredient.of(ModBlocks.GREATWOOD.planks()), Ingredient.of(Items.CHEST),
                        Ingredient.of(ModItems.BALANCED_SHARD), Ingredient.of(ModItems.SALIS_MUNDUS),
                        Ingredient.of(ModItems.QUICKSILVER), Ingredient.of(Items.PISTON),
                        Ingredient.of(ModItems.WISPY_ESSENCE)),
                new ItemStack(ModBlocks.AURA_TOTEM_PUSH.get()),
                AspectList.of(ModAspects.ENERGY, 25).add(ModAspects.AURA, 25).add(ModAspects.MOTION, 25)
                        .add(ModAspects.AVERSION, 25), TOTEM_UNSTABLE, "aura_totem");

        infusion(output, "mirror", Ingredient.of(ModItems.MIRRORED_GLASS),
                List.of(Ingredient.of(Items.GOLD_INGOT), Ingredient.of(Items.GOLD_INGOT), Ingredient.of(Items.GOLD_INGOT),
                        Ingredient.of(Items.ENDER_PEARL)),
                new ItemStack(ModBlocks.MIRROR.get()),
                AspectList.of(ModAspects.MOTION, 8).add(ModAspects.DARKNESS, 8).add(ModAspects.EXCHANGE, 8), 1, "mirror");
        infusion(output, "hand_mirror", Ingredient.of(ModBlocks.MIRROR),
                List.of(Ingredient.of(Items.STICK), Ingredient.of(Items.COMPASS), Ingredient.of(Items.MAP)),
                new ItemStack(ModItems.HAND_MIRROR.get()),
                AspectList.of(ModAspects.TOOL, 16).add(ModAspects.MOTION, 16), 5, "mirror_hand");
        infusion(output, "mirror_essentia", Ingredient.of(ModItems.MIRRORED_GLASS),
                List.of(Ingredient.of(Tags.Items.INGOTS_IRON), Ingredient.of(Tags.Items.INGOTS_IRON),
                        Ingredient.of(Tags.Items.INGOTS_IRON), Ingredient.of(Items.ENDER_PEARL)),
                new ItemStack(ModBlocks.MIRROR_ESSENTIA.get()),
                AspectList.of(ModAspects.MOTION, 8).add(ModAspects.WATER, 8).add(ModAspects.EXCHANGE, 8), 2,
                "mirror_essentia");

        infusion(output, "arcane_bore", Ingredient.of(ModItems.CLOCKWORK_MIND),
                List.of(Ingredient.of(ModBlocks.GREATWOOD.planks()), Ingredient.of(ModBlocks.GREATWOOD.planks()),
                        Ingredient.of(ModItems.BRASS_GEAR), Ingredient.of(ModItems.BRASS_PLATE),
                        Ingredient.of(Items.DIAMOND_PICKAXE), Ingredient.of(Items.DIAMOND_SHOVEL),
                        Ingredient.of(ModItems.SHARDS.get(me.moonscenty.alchemia.block.CrystalType.AIR)),
                        Ingredient.of(ModItems.SHARDS.get(me.moonscenty.alchemia.block.CrystalType.EARTH))),
                new ItemStack(ModBlocks.ARCANE_BORE.get()),
                AspectList.of(ModAspects.ENERGY, 16).add(ModAspects.EARTH, 32).add(ModAspects.MECHANISM, 32)
                        .add(ModAspects.VOID, 16).add(ModAspects.MOTION, 16),
                4, "arcane_bore");

        for (CrystalType type : CrystalType.values()) {
            infusion(output, type.getName() + "_crystal_from_shard", Ingredient.of(ModItems.SHARDS.get(type)),
                    List.of(Ingredient.of(Items.WHEAT_SEEDS), Ingredient.of(ModItems.SALIS_MUNDUS)),
                    new ItemStack(ModBlocks.CRYSTALS.get(type).get()),
                    AspectList.of(type.aspect(), 8).add(ModAspects.CRYSTAL, 8).add(ModAspects.TRAP, 4),
                    type == CrystalType.FLUX ? 4 : 0, "crystal_farmer");
        }
        infusion(output, "brain_jar", Ingredient.of(ModBlocks.JAR),
                List.of(Ingredient.of(ModItems.ZOMBIE_BRAIN), Ingredient.of(Items.SPIDER_EYE),
                        Ingredient.of(Items.WATER_BUCKET), Ingredient.of(Items.SPIDER_EYE)),
                new ItemStack(ModBlocks.BRAIN_JAR.get()),
                AspectList.of(ModAspects.MIND, 10).add(ModAspects.SENSES, 10).add(ModAspects.UNDEAD, 20),
                4, "brain_jar");

        infusion(output, "recharge_pedestal", Ingredient.of(ModBlocks.ARCANE_PEDESTAL),
                List.of(Ingredient.of(Items.GOLD_INGOT), Ingredient.of(Items.DIAMOND),
                        Ingredient.of(ModItems.PRIMAL_CHARM), Ingredient.of(Items.DIAMOND)),
                new ItemStack(ModBlocks.RECHARGE_PEDESTAL.get()),
                AspectList.of(ModAspects.AURA, 10).add(ModAspects.ENERGY, 15).add(ModAspects.EXCHANGE, 15),
                PEDESTAL_UNSTABLE, "recharge_pedestal");

        infusion(output, "growth_lamp", Ingredient.of(ModBlocks.ARCANE_LAMP.get()),
                List.of(Ingredient.of(Items.GOLD_INGOT), Ingredient.of(Items.BONE_MEAL),
                        Ingredient.of(ModItems.SHARDS.get(CrystalType.EARTH)), Ingredient.of(Items.GOLD_INGOT),
                        Ingredient.of(Items.BONE_MEAL), Ingredient.of(ModItems.SHARDS.get(CrystalType.EARTH))),
                new ItemStack(ModBlocks.GROWTH_LAMP.get()),
                AspectList.of(ModAspects.PLANT, 16).add(ModAspects.LIGHT, 8).add(ModAspects.LIFE, 8)
                        .add(ModAspects.TOOL, 8), LAMP_UNSTABLE, "growth_lamp");
        infusion(output, "fertility_lamp", Ingredient.of(ModBlocks.ARCANE_LAMP.get()),
                List.of(Ingredient.of(Items.GOLD_INGOT), Ingredient.of(Items.WHEAT),
                        Ingredient.of(ModItems.SHARDS.get(CrystalType.FIRE)), Ingredient.of(Items.GOLD_INGOT),
                        Ingredient.of(Items.CARROT), Ingredient.of(ModItems.SHARDS.get(CrystalType.FIRE))),
                new ItemStack(ModBlocks.FERTILITY_LAMP.get()),
                AspectList.of(ModAspects.BEAST, 16).add(ModAspects.LIGHT, 8).add(ModAspects.LIFE, 8)
                        .add(ModAspects.DESIRE, 8), LAMP_UNSTABLE, "fertility_lamp");
        infusion(output, "water_jug", Ingredient.of(Items.TERRACOTTA),
                List.of(Ingredient.of(Items.WATER_BUCKET), Ingredient.of(Items.WATER_BUCKET),
                        Ingredient.of(ModItems.SHARDS.get(CrystalType.WATER)), Ingredient.of(ModItems.SALIS_MUNDUS)),
                new ItemStack(ModBlocks.WATER_JUG.get()),
                AspectList.of(ModAspects.WATER, 16).add(ModAspects.CRAFT, 8).add(ModAspects.EARTH, 8),
                JUG_UNSTABLE, "water_jug");

        arcane(output, "automated_crossbow", "basic_turret", new ItemStack(ModItems.AUTOMATED_CROSSBOW.get()),
                AspectList.of(ModAspects.FIRE, 50).add(ModAspects.AIR, 100).add(ModAspects.ORDER, 50),
                Map.of('B', Ingredient.of(Items.BOW), 'G', Ingredient.of(ModItems.BRASS_GEAR),
                        'I', Ingredient.of(Tags.Items.INGOTS_IRON), 'W', Ingredient.of(ModBlocks.GREATWOOD.planks()),
                        'M', Ingredient.of(ModItems.CLOCKWORK_MIND), 'S', Ingredient.of(Tags.Items.RODS_WOODEN)),
                "BGI", "WMW", "S S");
        arcane(output, "advanced_automated_crossbow", "advanced_turret",
                new ItemStack(ModItems.ADVANCED_AUTOMATED_CROSSBOW.get()),
                AspectList.of(ModAspects.FIRE, 50).add(ModAspects.AIR, 75).add(ModAspects.ORDER, 125),
                Map.of('P', Ingredient.of(ModItems.IRON_PLATE), 'M', Ingredient.of(ModItems.BIOALCHEMIC_MIND),
                        'T', Ingredient.of(ModItems.AUTOMATED_CROSSBOW), 'B', Ingredient.of(ModItems.BRASS_PLATE),
                        'G', Ingredient.of(ModItems.BRASS_GEAR)),
                "PMP", "PTP", "BGB");
        infusion(output, "node_magnet", Ingredient.of(ModItems.MORPHIC_RESONATOR),
                List.of(Ingredient.of(ModItems.BRASS_GEAR), Ingredient.of(ModItems.BRASS_PLATE),
                        Ingredient.of(Tags.Items.STORAGE_BLOCKS_IRON), Ingredient.of(ModItems.BIOALCHEMIC_MIND),
                        Ingredient.of(ModBlocks.GREATWOOD.planks()),
                        Ingredient.of(ModBlocks.CRYSTALS.get(me.moonscenty.alchemia.block.CrystalType.AIR)),
                        Ingredient.of(ModBlocks.CRYSTALS.get(me.moonscenty.alchemia.block.CrystalType.FIRE)),
                        Ingredient.of(ModBlocks.CRYSTALS.get(me.moonscenty.alchemia.block.CrystalType.WATER)),
                        Ingredient.of(ModBlocks.CRYSTALS.get(me.moonscenty.alchemia.block.CrystalType.EARTH)),
                        Ingredient.of(ModBlocks.CRYSTALS.get(me.moonscenty.alchemia.block.CrystalType.ORDER)),
                        Ingredient.of(ModBlocks.CRYSTALS.get(me.moonscenty.alchemia.block.CrystalType.ENTROPY))),
                new ItemStack(ModItems.NODE_MAGNET.get()),
                AspectList.of(ModAspects.MECHANISM, 16).add(ModAspects.MOTION, 32).add(ModAspects.AURA, 64), 4,
                "node_magnet");

        infusion(output, "autocaster", Ingredient.of(ModItems.BIOALCHEMIC_MIND),
                List.of(Ingredient.of(ModItems.BALANCED_SHARD), Ingredient.of(ModItems.MORPHIC_RESONATOR),
                        Ingredient.of(ModBlocks.GREATWOOD.planks()), Ingredient.of(ModBlocks.GREATWOOD.planks()),
                        Ingredient.of(ModItems.BRASS_GEAR), Ingredient.of(ModItems.BRASS_PLATE),
                        Ingredient.of(ModItems.WAND)),
                new ItemStack(ModItems.AUTOCASTER.get()),
                AspectList.of(ModAspects.AURA, 16).add(ModAspects.MECHANISM, 32).add(ModAspects.MIND, 16)
                        .add(ModAspects.SENSES, 16).add(ModAspects.AVERSION, 16),
                AUTOCASTER_UNSTABLE, "autocaster");

        elemental(output, "elemental_axe", "alchemium_axe", CrystalType.WATER, CrystalType.WATER,
                AspectList.of(ModAspects.WATER, 16).add(ModAspects.PLANT, 8));
        elemental(output, "elemental_pickaxe", "alchemium_pickaxe", CrystalType.FIRE, CrystalType.FIRE,
                AspectList.of(ModAspects.FIRE, 8).add(ModAspects.METAL, 8).add(ModAspects.SENSES, 8));
        elemental(output, "elemental_sword", "alchemium_sword", CrystalType.AIR, CrystalType.AIR,
                AspectList.of(ModAspects.AIR, 8).add(ModAspects.MOTION, 8).add(ModAspects.AVERSION, 8));
        elemental(output, "elemental_shovel", "alchemium_shovel", CrystalType.EARTH, CrystalType.EARTH,
                AspectList.of(ModAspects.EARTH, 16).add(ModAspects.CRAFT, 8));
        elemental(output, "elemental_hoe", "alchemium_hoe", CrystalType.ORDER, CrystalType.ENTROPY,
                AspectList.of(ModAspects.ORDER, 8).add(ModAspects.PLANT, 8).add(ModAspects.ENTROPY, 8));

        infusion(output, "primal_crusher", Ingredient.of(ModItems.PRIMORDIAL_PEARL),
                List.of(Ingredient.of(ModItems.PRIMAL_CHARM),
                        Ingredient.of(ModItems.METAL_TOOLS.get("void_pickaxe")),
                        Ingredient.of(ModItems.METAL_TOOLS.get("void_shovel")),
                        Ingredient.of(ModItems.PRIMAL_CHARM),
                        Ingredient.of(ModItems.ELEMENTAL_TOOLS.get("elemental_pickaxe")),
                        Ingredient.of(ModItems.ELEMENTAL_TOOLS.get("elemental_shovel"))),
                new ItemStack(ModItems.PRIMAL_CRUSHER.get()),
                AspectList.of(ModAspects.EARTH, 24).add(ModAspects.TOOL, 24).add(ModAspects.ENTROPY, 16)
                        .add(ModAspects.VOID, 16).add(ModAspects.AVERSION, 16).add(ModAspects.ELDRITCH, 16)
                        .add(ModAspects.DESIRE, 16),
                CRUSHER_UNSTABLE, "primal_crusher");

        infusion(output, "wand_cap_alchemium", Ingredient.of(ModItems.INERT_CAPS.get("alchemium")),
                salis(3), new ItemStack(ModItems.WAND_CAPS.get("alchemium").get()),
                AspectList.of(ModAspects.ENERGY, 12).add(ModAspects.AURA, 6), 5, "wand_cap_alchemium");
        infusion(output, "wand_cap_void", Ingredient.of(ModItems.INERT_CAPS.get("void")),
                salis(4), new ItemStack(ModItems.WAND_CAPS.get("void").get()),
                AspectList.of(ModAspects.ENERGY, 18).add(ModAspects.VOID, 18)
                        .add(ModAspects.ELDRITCH, 18).add(ModAspects.AURA, 18), 8, "void_metal");
    }

    private void elemental(RecipeOutput output, String made, String from, CrystalType one, CrystalType two,
            AspectList essentia) {
        infusion(output, made, Ingredient.of(ModItems.METAL_TOOLS.get(from)),
                List.of(Ingredient.of(ModItems.SHARDS.get(one)), Ingredient.of(ModItems.SHARDS.get(two)),
                        Ingredient.of(Items.DIAMOND), Ingredient.of(ModBlocks.GREATWOOD.planks())),
                new ItemStack(ModItems.ELEMENTAL_TOOLS.get(made).get()), essentia, ELEMENTAL_UNSTABLE,
                "elemental_tools");
    }

    private void enchantments(RecipeOutput output) {
        enchantment(output, InfusionEnchantment.COLLECTOR,
                AspectList.of(ModAspects.DESIRE, 16).add(ModAspects.WATER, 24),
                List.of(Ingredient.of(ModItems.KNOWLEDGE_FRAGMENT),
                        Ingredient.of(Items.ENCHANTED_BOOK), Ingredient.of(Items.LEAD)));
        enchantment(output, InfusionEnchantment.DESTRUCTIVE,
                AspectList.of(ModAspects.AVERSION, 48).add(ModAspects.ENTROPY, 64),
                List.of(Ingredient.of(ModItems.KNOWLEDGE_FRAGMENT),
                        Ingredient.of(Items.ENCHANTED_BOOK), Ingredient.of(Items.TNT)));
        enchantment(output, InfusionEnchantment.BURROWING,
                AspectList.of(ModAspects.SENSES, 24).add(ModAspects.EARTH, 32),
                List.of(Ingredient.of(ModItems.KNOWLEDGE_FRAGMENT),
                        Ingredient.of(Items.ENCHANTED_BOOK), Ingredient.of(Items.RABBIT_FOOT)));
        enchantment(output, InfusionEnchantment.REFINING,
                AspectList.of(ModAspects.ORDER, 16).add(ModAspects.EXCHANGE, 12),
                List.of(Ingredient.of(ModItems.KNOWLEDGE_FRAGMENT),
                        Ingredient.of(Items.ENCHANTED_BOOK), Ingredient.of(ModItems.SALIS_MUNDUS)));
        enchantment(output, InfusionEnchantment.SOUNDING,
                AspectList.of(ModAspects.SENSES, 8).add(ModAspects.FIRE, 12),
                List.of(Ingredient.of(ModItems.KNOWLEDGE_FRAGMENT),
                        Ingredient.of(Items.ENCHANTED_BOOK), Ingredient.of(Items.MAP)));
        enchantment(output, InfusionEnchantment.ARCING,
                AspectList.of(ModAspects.ENERGY, 8).add(ModAspects.AIR, 12),
                List.of(Ingredient.of(ModItems.KNOWLEDGE_FRAGMENT),
                        Ingredient.of(Items.ENCHANTED_BOOK), Ingredient.of(Items.REDSTONE_BLOCK)));
        enchantment(output, InfusionEnchantment.ESSENCE,
                AspectList.of(ModAspects.BEAST, 8).add(ModAspects.FLUX, 12),
                List.of(Ingredient.of(ModItems.KNOWLEDGE_FRAGMENT),
                        Ingredient.of(Items.ENCHANTED_BOOK), Ingredient.of(ModItems.CRYSTALLIZED_ESSENCE)));
    }

    private void enchantment(RecipeOutput output, InfusionEnchantment which, AspectList essentia,
            List<Ingredient> ring) {
        String name = "infusion_enchantment_" + which.getSerializedName();
        Ingredient takes = net.neoforged.neoforge.common.crafting.CompoundIngredient.of(
                which.goesOn().stream().map(Ingredient::of).toArray(Ingredient[]::new));
        output.accept(Alchemia.id(name), new InfusionRecipe(takes, ring, ItemStack.EMPTY, essentia,
                INFUSION_ENCHANTMENT_INSTABILITY, Optional.of(Alchemia.id("infusion_enchantment")),
                Optional.of(which)), null);
    }

    private static final int INFUSION_ENCHANTMENT_INSTABILITY = 4;

    private static List<Ingredient> salis(int many) {
        return java.util.Collections.nCopies(many, Ingredient.of(ModItems.SALIS_MUNDUS));
    }

    private void rod(RecipeOutput output, String name, Ingredient from, int instability, AspectList cost,
            CrystalType element) {
        infusion(output, "wand_rod_" + name, from,
                List.of(Ingredient.of(ModItems.BALANCED_SHARD), Ingredient.of(ModItems.SHARDS.get(element))),
                new ItemStack(ModItems.WAND_RODS.get(name).get()), cost, instability, "wand_rods");
    }

    private void infusion(RecipeOutput output, String name, Ingredient central, List<Ingredient> ring,
            ItemStack result, AspectList essentia, int instability, String research) {
        output.accept(Alchemia.id(name), new InfusionRecipe(central, ring, result, essentia, instability,
                Optional.ofNullable(research).map(Alchemia::id)), null);
    }

    private void infusion(RecipeOutput output, String name, Ingredient central, List<Ingredient> ring,
            ItemStack result, AspectList essentia, int instability) {
        infusion(output, name, central, ring, result, essentia, instability, null);
    }

    private void mask(RecipeOutput output, String name, HelmFitting face, List<Ingredient> own,
            AspectList essentia) {
        List<Ingredient> ring = new java.util.ArrayList<>(own);
        ring.add(Ingredient.of(Items.IRON_INGOT));
        ring.add(Ingredient.of(Items.IRON_INGOT));
        ring.add(Ingredient.of(Items.LEATHER));
        helmFitting(output, name, face, ring, essentia, MASK_UNSTABLE);
    }

    private void helmFitting(RecipeOutput output, String name, HelmFitting face, List<Ingredient> ring,
            AspectList essentia, int instability) {
        output.accept(Alchemia.id(name), new InfusionRecipe(
                Ingredient.of(ModItems.FORTRESS.get("fortress_helm")), ring, ItemStack.EMPTY, essentia,
                instability, Optional.of(Alchemia.id("fortress_armour")),
                Optional.empty(), Optional.of(face)), null);
    }

    private void fortress(RecipeOutput output, String name, String from, int plates, List<Ingredient> rest,
            AspectList essentia) {
        List<Ingredient> ring = new java.util.ArrayList<>();
        for (int beaten = 0; beaten < plates; beaten++) {
            ring.add(Ingredient.of(ModItems.ALCHEMIUM_PLATE));
        }
        ring.addAll(rest);
        infusion(output, name, Ingredient.of(ModItems.METAL_ARMOUR.get(from)), ring,
                new ItemStack(ModItems.FORTRESS.get(name).get()), essentia, FORTRESS_UNSTABLE, "fortress_armour");
    }

    private void voidRobe(RecipeOutput output, String name, String from, List<Ingredient> ring,
            AspectList essentia) {
        infusion(output, name, Ingredient.of(ModItems.METAL_ARMOUR.get(from)), ring,
                new ItemStack(ModItems.ROBES.get(name).get()), essentia, VOID_ROBE_UNSTABLE, "void_robes");
    }

    private void metalGear(RecipeOutput output) {
        forge(output, "alchemium", ModTags.Items.INGOTS_ALCHEMIUM);
        forge(output, "void", ModTags.Items.INGOTS_VOID);
    }

    private void forge(RecipeOutput output, String metal, net.minecraft.tags.TagKey<Item> of) {
        Ingredient bar = Ingredient.of(of);
        Ingredient haft = Ingredient.of(Items.STICK);
        shape(output, metal + "_pickaxe", bar, haft, "III", " S ", " S ");
        shape(output, metal + "_axe", bar, haft, "II", "SI", "S ");
        shape(output, metal + "_shovel", bar, haft, "I", "S", "S");
        shape(output, metal + "_sword", bar, haft, "I", "I", "S");
        shape(output, metal + "_hoe", bar, haft, "II", "S ", "S ");
        shape(output, metal + "_helmet", bar, haft, "III", "I I");
        shape(output, metal + "_chestplate", bar, haft, "I I", "III", "III");
        shape(output, metal + "_leggings", bar, haft, "III", "I I", "I I");
        shape(output, metal + "_boots", bar, haft, "I I", "I I");
    }

    private void shape(RecipeOutput output, String name, Ingredient bar, Ingredient haft, String... rows) {
        Item made = ModItems.METAL_TOOLS.containsKey(name)
                ? ModItems.METAL_TOOLS.get(name).get() : ModItems.METAL_ARMOUR.get(name).get();
        var builder = ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, made)
                .define('I', bar)
                .unlockedBy("has_ingot", has(bar.getItems()[0].getItem()));
        if (String.join("", rows).indexOf('S') >= 0) {
            builder.define('S', haft);
        }
        for (String row : rows) {
            builder.pattern(row);
        }
        builder.save(output, Alchemia.id(name));
    }

    private void crucible(RecipeOutput output) {
        crucible(output, "gunpowder_duplication", Ingredient.of(Items.GUNPOWDER), AspectList.of(ModAspects.FIRE, 4).add(ModAspects.ENTROPY, 4),
                new ItemStack(Items.GUNPOWDER, 2), "alchemical_duplication");
        crucible(output, "slime_ball_duplication", Ingredient.of(Items.SLIME_BALL), AspectList.of(ModAspects.WATER, 2).add(ModAspects.LIFE, 1),
                new ItemStack(Items.SLIME_BALL, 2), "alchemical_duplication");
        crucible(output, "glowstone_dust_duplication", Ingredient.of(Tags.Items.DUSTS_GLOWSTONE), AspectList.of(ModAspects.LIGHT, 3).add(ModAspects.SENSES, 1),
                new ItemStack(Items.GLOWSTONE_DUST, 2), "alchemical_duplication");
        crucible(output, "ink_sac_duplication", Ingredient.of(Items.INK_SAC), AspectList.of(ModAspects.WATER, 2).add(ModAspects.SENSES, 2),
                new ItemStack(Items.INK_SAC, 2), "alchemical_duplication");
        crucible(output, "prismarine_crystals_duplication", Ingredient.of(Items.PRISMARINE_CRYSTALS), AspectList.of(ModAspects.WATER, 1).add(ModAspects.CRYSTAL, 2).add(ModAspects.LIGHT, 1),
                new ItemStack(Items.PRISMARINE_CRYSTALS, 2), "alchemical_duplication");
        crucible(output, "prismarine_shard_duplication", Ingredient.of(Items.PRISMARINE_SHARD), AspectList.of(ModAspects.WATER, 2).add(ModAspects.EARTH, 1),
                new ItemStack(Items.PRISMARINE_SHARD, 2), "alchemical_duplication");
        crucible(output, "clay_ball_from_dirt", Ingredient.of(Items.DIRT), AspectList.of(ModAspects.WATER, 1).add(ModAspects.EARTH, 1),
                new ItemStack(Items.CLAY_BALL), "alchemical_manufacture");
        crucible(output, "cobweb_from_string", Ingredient.of(Items.STRING), AspectList.of(ModAspects.TRAP, 2),
                new ItemStack(Items.COBWEB), "alchemical_manufacture");
        crucible(output, "mossy_cobblestone_from_cobblestone", Ingredient.of(Items.COBBLESTONE), AspectList.of(ModAspects.PLANT, 2),
                new ItemStack(Items.MOSSY_COBBLESTONE), "alchemical_manufacture");
        crucible(output, "ice_from_snow", Ingredient.of(Items.SNOW_BLOCK), AspectList.of(ModAspects.ORDER, 1).add(ModAspects.COLD, 1),
                new ItemStack(Items.ICE), "alchemical_manufacture");
        crucible(output, "lava_bucket_from_bucket", Ingredient.of(Items.BUCKET), AspectList.of(ModAspects.EARTH, 2).add(ModAspects.FIRE, 4),
                new ItemStack(Items.LAVA_BUCKET), "alchemical_manufacture");
        crucible(output, "grass_block_from_dirt", Ingredient.of(Items.DIRT), AspectList.of(ModAspects.PLANT, 1),
                new ItemStack(Items.GRASS_BLOCK), "alchemical_manufacture");
        crucible(output, "bone_meal_from_bone", Ingredient.of(Items.BONE), AspectList.of(ModAspects.ENTROPY, 1),
                new ItemStack(Items.BONE_MEAL, 4), "entropic_processing");
        crucible(output, "blaze_powder_from_blaze_rod", Ingredient.of(Items.BLAZE_ROD), AspectList.of(ModAspects.ENTROPY, 1),
                new ItemStack(Items.BLAZE_POWDER, 3), "entropic_processing");
        crucible(output, "leather_from_rotten_flesh", Ingredient.of(Items.ROTTEN_FLESH), AspectList.of(ModAspects.ENTROPY, 1).add(ModAspects.FIRE, 1),
                new ItemStack(Items.LEATHER), "entropic_processing");
        crucible(output, "string_from_wool", Ingredient.of(net.minecraft.tags.ItemTags.WOOL), AspectList.of(ModAspects.ENTROPY, 1),
                new ItemStack(Items.STRING, 4), "entropic_processing");
        crucible(output, "mycelium_from_dirt", Ingredient.of(Items.DIRT), AspectList.of(ModAspects.ENTROPY, 1).add(ModAspects.PLANT, 1),
                new ItemStack(Items.MYCELIUM), "entropic_processing");
        crucible(output, "iron_cluster_from_ore", Ingredient.of(Tags.Items.ORES_IRON), AspectList.of(ModAspects.METAL, 1).add(ModAspects.ORDER, 1),
                new ItemStack(ModItems.IRON_CLUSTER.get()), "pure_metal");
        crucible(output, "gold_cluster_from_ore", Ingredient.of(Tags.Items.ORES_GOLD), AspectList.of(ModAspects.METAL, 1).add(ModAspects.ORDER, 1),
                new ItemStack(ModItems.GOLD_CLUSTER.get()), "pure_metal");
        crucible(output, "copper_cluster_from_ore", Ingredient.of(Tags.Items.ORES_COPPER), AspectList.of(ModAspects.METAL, 1).add(ModAspects.ORDER, 1),
                new ItemStack(ModItems.COPPER_CLUSTER.get()), "pure_metal");
        crucible(output, "cinnabar_cluster_from_ore", Ingredient.of(ModTags.Items.ORES_CINNABAR), AspectList.of(ModAspects.METAL, 1).add(ModAspects.ORDER, 1),
                new ItemStack(ModItems.CINNABAR_CLUSTER.get()), "pure_metal");
        crucible(output, "iron_nugget_transmutation", Ingredient.of(Tags.Items.NUGGETS_IRON), AspectList.of(ModAspects.METAL, 2).add(ModAspects.FLUX, 1),
                new ItemStack(Items.IRON_NUGGET, 3), "trans_metal");
        crucible(output, "gold_nugget_transmutation", Ingredient.of(Tags.Items.NUGGETS_GOLD), AspectList.of(ModAspects.METAL, 2).add(ModAspects.DESIRE, 1).add(ModAspects.FLUX, 1),
                new ItemStack(Items.GOLD_NUGGET, 3), "trans_metal");
        crucible(output, "flux_shard_from_shard", Ingredient.of(ModTags.Items.SHARDS), AspectList.of(ModAspects.FLUX, 4).add(ModAspects.ENTROPY, 4),
                new ItemStack(ModItems.SHARDS.get(CrystalType.FLUX).get()), "taint_shard");
        crucible(output, "ethereal_bloom", Ingredient.of(ModBlocks.SHIMMERLEAF),
                AspectList.of(ModAspects.LIGHT, 8).add(ModAspects.PLANT, 16).add(ModAspects.LIFE, 16)
                        .add(ModAspects.FLUX, 16),
                new ItemStack(ModBlocks.ETHEREAL_BLOOM.get()), "ethereal_bloom");
        for (CrystalType from : new CrystalType[] {CrystalType.AIR, CrystalType.FIRE, CrystalType.WATER,
                CrystalType.EARTH, CrystalType.ORDER, CrystalType.ENTROPY}) {
            AspectList others = AspectList.EMPTY;
            for (CrystalType other : new CrystalType[] {CrystalType.AIR, CrystalType.FIRE, CrystalType.WATER,
                    CrystalType.EARTH, CrystalType.ORDER, CrystalType.ENTROPY}) {
                if (other != from) {
                    others = others.add(other.aspect(), 2);
                }
            }
            crucible(output, "balanced_shard_from_" + from.getName(),
                    Ingredient.of(ModItems.SHARDS.get(from)), others, new ItemStack(ModItems.BALANCED_SHARD.get()),
                    "crucible");
        }
        crucible(output, "alchemium_ingot_from_iron", Ingredient.of(Tags.Items.INGOTS_IRON),
                AspectList.of(ModAspects.EARTH, 2).add(ModAspects.ORDER, 2),
                new ItemStack(ModItems.ALCHEMIUM_INGOT.get()), "metallurgy");
        crucible(output, "brass_ingot_from_iron", Ingredient.of(Tags.Items.INGOTS_IRON),
                AspectList.of(ModAspects.ENERGY, 1).add(ModAspects.WATER, 1),
                new ItemStack(ModItems.BRASS_INGOT.get()), "metallurgy");

        crucible(output, "bottle_taint", net.neoforged.neoforge.common.crafting.DataComponentIngredient.of(false,
                        me.moonscenty.alchemia.item.PhialItem.filled(new ItemStack(ModItems.PHIAL.get()),
                                ModAspects.FLUX)),
                AspectList.of(ModAspects.FLUX, 8).add(ModAspects.WATER, 8),
                new ItemStack(ModItems.BOTTLE_TAINT.get()), "bottle_taint");
        crucible(output, "liquid_death", Ingredient.of(Items.BUCKET),
                AspectList.of(ModAspects.DEATH, 32).add(ModAspects.CRYSTAL, 32).add(ModAspects.ENTROPY, 32),
                new ItemStack(ModItems.LIQUID_DEATH_BUCKET.get()), "liquid_death");
        crucible(output, "tallow", Ingredient.of(Items.ROTTEN_FLESH), AspectList.of(ModAspects.FIRE, 1),
                new ItemStack(ModItems.TALLOW.get()), "tallow");
        seal(output, "pickup", ModItems.SEAL_BLANK.get(), AspectList.of(ModAspects.DESIRE, 4), "seal_collect");
        seal(output, "pickup_advanced", ModItems.SEALS.get("pickup").get(),
                AspectList.of(ModAspects.SENSES, 4).add(ModAspects.MIND, 4), "seal_collect");
        seal(output, "fill", ModItems.SEAL_BLANK.get(), AspectList.of(ModAspects.AVERSION, 4), "seal_store");
        seal(output, "fill_advanced", ModItems.SEALS.get("fill").get(),
                AspectList.of(ModAspects.SENSES, 4).add(ModAspects.MIND, 4), "seal_store");
        seal(output, "empty", ModItems.SEAL_BLANK.get(), AspectList.of(ModAspects.VOID, 4), "seal_empty");
        seal(output, "empty_advanced", ModItems.SEALS.get("empty").get(),
                AspectList.of(ModAspects.SENSES, 4).add(ModAspects.MIND, 4), "seal_empty");
        seal(output, "provider", ModItems.SEALS.get("empty_advanced").get(),
                AspectList.of(ModAspects.EXCHANGE, 4).add(ModAspects.DESIRE, 4), "seal_provide");
        seal(output, "guard", ModItems.SEAL_BLANK.get(),
                AspectList.of(ModAspects.AVERSION, 8).add(ModAspects.PROTECT, 8), "seal_guard");
        seal(output, "guard_advanced", ModItems.SEALS.get("guard").get(),
                AspectList.of(ModAspects.SENSES, 8).add(ModAspects.MIND, 8), "seal_guard");
        seal(output, "lumber", ModItems.SEALS.get("breaker").get(),
                AspectList.of(ModAspects.PLANT, 16).add(ModAspects.SENSES, 8), "seal_lumber");
        seal(output, "use", ModItems.SEAL_BLANK.get(),
                AspectList.of(ModAspects.CRAFT, 8).add(ModAspects.SENSES, 4).add(ModAspects.MIND, 4), "seal_use");

        crucible(output, "bath_salts", Ingredient.of(ModItems.SALIS_MUNDUS),
                AspectList.of(ModAspects.MIND, 6).add(ModAspects.AIR, 6).add(ModAspects.ORDER, 6)
                        .add(ModAspects.LIFE, 6),
                new ItemStack(ModItems.BATH_SALTS.get()), "bath_salts");
        crucible(output, "alumentum", Ingredient.of(ItemTags.COALS),
                AspectList.of(ModAspects.ENERGY, 3).add(ModAspects.FIRE, 3).add(ModAspects.ENTROPY, 3),
                new ItemStack(ModItems.ALUMENTUM.get()), "alumentum");
        crucible(output, "nitor", Ingredient.of(Items.GLOWSTONE_DUST),
                AspectList.of(ModAspects.ENERGY, 3).add(ModAspects.FIRE, 3).add(ModAspects.LIGHT, 3),
                new ItemStack(ModBlocks.NITOR.get(DyeColor.WHITE).get(), NITOR_AT_A_TIME), "nitor");
        nitorDyes(output);
        leftovers(output);
        foci(output);
    }

    private static final int NITOR_AT_A_TIME = 4;

    private Ingredient shard(me.moonscenty.alchemia.block.CrystalType type) {
        return Ingredient.of(ModItems.SHARDS.get(type));
    }

    private void leftovers(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.FLESH_BLOCK.get())
                .define('F', Items.ROTTEN_FLESH).pattern("FFF").pattern("FFF").pattern("FFF")
                .unlockedBy("has_flesh", has(Items.ROTTEN_FLESH)).save(output, Alchemia.id("flesh_block"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.ROTTEN_FLESH, 9)
                .requires(ModBlocks.FLESH_BLOCK.get())
                .unlockedBy("has_flesh_block", has(ModBlocks.FLESH_BLOCK.get()))
                .save(output, Alchemia.id("rotten_flesh_from_block"));
        crucible(output, "sanity_soap", Ingredient.of(ModBlocks.FLESH_BLOCK.get()),
                AspectList.of(ModAspects.MIND, 16).add(ModAspects.ELDRITCH, 16).add(ModAspects.ORDER, 16)
                        .add(ModAspects.LIFE, 16),
                new ItemStack(ModItems.SANITY_SOAP.get()), "sanity_soap");

        var white = ModBlocks.TALLOW_CANDLES.get(DyeColor.WHITE).get();
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, white, 3)
                .define('S', Items.STRING).define('T', ModItems.TALLOW.get()).pattern("S").pattern("T").pattern("T")
                .unlockedBy("has_tallow", has(ModItems.TALLOW.get())).save(output, Alchemia.id("tallow_candle"));
        for (DyeColor colour : DyeColor.values()) {
            if (colour == DyeColor.WHITE) {
                continue;
            }
            ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, ModBlocks.TALLOW_CANDLES.get(colour).get())
                    .requires(white).requires(colour.getTag())
                    .unlockedBy("has_candle", has(white))
                    .save(output, Alchemia.id(colour.getSerializedName() + "_tallow_candle"));
        }
        ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, white)
                .requires(Ingredient.of(ModBlocks.TALLOW_CANDLES.values().stream()
                        .map(candle -> new ItemStack(candle.get())))).requires(Items.BONE_MEAL)
                .unlockedBy("has_candle", has(white)).save(output, Alchemia.id("white_tallow_candle_bleached"));

        arcane(output, "pattern_crafter", "pattern_crafter", new ItemStack(ModBlocks.PATTERN_CRAFTER.get()),
                AspectList.of(ModAspects.ORDER, 100).add(ModAspects.FIRE, 100).add(ModAspects.ENTROPY, 100),
                Map.of('H', Ingredient.of(Items.HOPPER), 'G', Ingredient.of(ModItems.BRASS_GEAR),
                        'C', Ingredient.of(Items.CRAFTING_TABLE), 'W', Ingredient.of(ModBlocks.GREATWOOD.planks())),
                " H ", "GCG", " W ");

        arcane(output, "bone_bow", "bone_bow", new ItemStack(ModItems.BONE_BOW.get()),
                AspectList.of(ModAspects.AIR, 50).add(ModAspects.ENTROPY, 100),
                Map.of('S', Ingredient.of(Items.STRING), 'B', Ingredient.of(Items.BONE),
                        'E', shard(me.moonscenty.alchemia.block.CrystalType.ENTROPY)),
                "SB ", "SEB", "SB ");

        ModItems.PRIMAL_ARROWS.forEach((type, arrow) -> arcane(output, "primal_arrow_" + type.getName(),
                "primal_arrows", new ItemStack(arrow.get(), 8), AspectList.of(type.aspect(), 25),
                Map.of('A', Ingredient.of(Items.ARROW), 'S', shard(type)), "AAA", "ASA", "AAA"));

        for (boolean filling : new boolean[] {true, false}) {
            var made = filling ? ModBlocks.ESSENTIA_INPUT : ModBlocks.ESSENTIA_OUTPUT;
            arcane(output, made.getId().getPath(), "essentia_transport", new ItemStack(made.get()),
                    AspectList.of(ModAspects.WATER, 75).add(ModAspects.AIR, 50).add(ModAspects.ORDER, 50),
                    Map.of('B', Ingredient.of(ModTags.Items.INGOTS_BRASS),
                            'Q', Ingredient.of(filling ? Items.DISPENSER : Items.HOPPER),
                            'I', Ingredient.of(Tags.Items.INGOTS_IRON), 'P', Ingredient.of(ModBlocks.TUBE),
                            'G', Ingredient.of(ModBlocks.ALCHEMICAL_CONSTRUCT)),
                    "BQB", "IGI", "IPI");
        }

        infusion(output, "thaumostatic_harness", Ingredient.of(Items.LEATHER_CHESTPLATE),
                List.of(shard(me.moonscenty.alchemia.block.CrystalType.AIR),
                        shard(me.moonscenty.alchemia.block.CrystalType.AIR),
                        Ingredient.of(ModBlocks.GREATWOOD.planks()), Ingredient.of(ModBlocks.GREATWOOD.planks()),
                        Ingredient.of(ModItems.MORPHIC_RESONATOR), Ingredient.of(ModTags.Items.PLATES_BRASS),
                        Ingredient.of(ModTags.Items.PLATES_BRASS), Ingredient.of(Tags.Items.INGOTS_IRON),
                        Ingredient.of(Tags.Items.INGOTS_IRON)),
                new ItemStack(ModItems.THAUMOSTATIC_HARNESS.get()),
                AspectList.of(ModAspects.FLIGHT, 32).add(ModAspects.ENERGY, 32).add(ModAspects.MECHANISM, 32)
                        .add(ModAspects.MOTION, 16), 6, "thaumostatic_harness");

        infusion(output, "sinister_stone", Ingredient.of(Items.FLINT),
                List.of(Ingredient.of(ModTags.Items.NITOR), shard(me.moonscenty.alchemia.block.CrystalType.ORDER),
                        Ingredient.of(ModItems.KNOWLEDGE_FRAGMENT),
                        shard(me.moonscenty.alchemia.block.CrystalType.ENTROPY)),
                new ItemStack(ModItems.SINISTER_STONE.get()),
                AspectList.of(ModAspects.SENSES, 8).add(ModAspects.DARKNESS, 8).add(ModAspects.ELDRITCH, 8)
                        .add(ModAspects.AURA, 8), 5, "sinister_stone");
        infusion(output, "sanity_checker", Ingredient.of(ModItems.ALCHEMOMETER),
                List.of(Ingredient.of(ModItems.MIRRORED_GLASS), Ingredient.of(ModItems.ZOMBIE_BRAIN),
                        Ingredient.of(ModItems.MORPHIC_RESONATOR)),
                new ItemStack(ModItems.SANITY_CHECKER.get()),
                AspectList.of(ModAspects.MIND, 24).add(ModAspects.SENSES, 24).add(ModAspects.ELDRITCH, 8), 4,
                "sanity_check");
    }

    private void foci(RecipeOutput output) {
        Ingredient quartz = Ingredient.of(Items.QUARTZ);
        var air = shard(me.moonscenty.alchemia.block.CrystalType.AIR);
        var earth = shard(me.moonscenty.alchemia.block.CrystalType.EARTH);
        var fire = shard(me.moonscenty.alchemia.block.CrystalType.FIRE);
        var water = shard(me.moonscenty.alchemia.block.CrystalType.WATER);
        var entropy = shard(me.moonscenty.alchemia.block.CrystalType.ENTROPY);
        focus(output, "fire", AspectList.of(ModAspects.FIRE, 100).add(ModAspects.ENTROPY, 50),
                Map.of('C', fire, 'Q', quartz, '#', Ingredient.of(Items.FIRE_CHARGE)), "CQC", "Q#Q", "CQC");
        focus(output, "shock", AspectList.of(ModAspects.AIR, 75).add(ModAspects.ORDER, 75).add(ModAspects.ENTROPY, 50),
                Map.of('C', air, 'Q', quartz, '#', Ingredient.of(Items.POTATO)), "CQC", "Q#Q", "CQC");
        focus(output, "frost", AspectList.of(ModAspects.WATER, 75).add(ModAspects.ORDER, 75)
                        .add(ModAspects.ENTROPY, 50),
                Map.of('C', water, 'Q', quartz, '#', Ingredient.of(Items.DIAMOND)), "CQC", "Q#Q", "CQC");
        focus(output, "excavation", AspectList.of(ModAspects.EARTH, 75).add(ModAspects.ENTROPY, 75)
                        .add(ModAspects.ORDER, 50),
                Map.of('C', earth, 'Q', quartz, '#', Ingredient.of(Tags.Items.GEMS_EMERALD)), "CQC", "Q#Q", "CQC");
        focus(output, "trade", AspectList.of(ModAspects.ORDER, 75).add(ModAspects.ENTROPY, 75).add(ModAspects.EARTH, 50),
                Map.of('C', Ingredient.of(ModItems.BALANCED_SHARD), 'Q', quartz,
                        '#', Ingredient.of(ModItems.QUICKSILVER)), "CQC", "Q#Q", "CQC");
        focus(output, "builder", AspectList.of(ModAspects.ORDER, 75).add(ModAspects.AIR, 50).add(ModAspects.EARTH, 75),
                Map.of('C', air, 'E', earth, 'Q', quartz, '#', Ingredient.of(ModItems.AMBER)), "CQE", "Q#Q", "EQC");
        focus(output, "grapple", AspectList.of(ModAspects.AIR, 75).add(ModAspects.WATER, 75).add(ModAspects.ORDER, 50),
                Map.of('A', air, 'W', water, 'Q', quartz, '#', Ingredient.of(Items.SLIME_BALL)), "AQW", "Q#Q", "WQA");
        AspectList primals = AspectList.of(ModAspects.EARTH, 150).add(ModAspects.ENTROPY, 150)
                .add(ModAspects.ORDER, 150).add(ModAspects.AIR, 150).add(ModAspects.FIRE, 150)
                .add(ModAspects.WATER, 150);
        focus(output, "primal", primals, Map.of('C', Ingredient.of(Items.DIAMOND), 'Q', quartz,
                '#', Ingredient.of(ModItems.PRIMAL_CHARM)), "CQC", "Q#Q", "CQC");
        infusion(output, "focus_hole", Ingredient.of(Items.ENDER_PEARL),
                List.of(quartz, earth, quartz, air, quartz, entropy), new ItemStack(ModItems.FOCI.get("hole").get()),
                AspectList.of(ModAspects.MOTION, 25).add(ModAspects.ELDRITCH, 10).add(ModAspects.EXCHANGE, 10)
                        .add(ModAspects.ENTROPY, 25), 3, "focus_hole");
        infusion(output, "focus_hellbat", Ingredient.of(Items.MAGMA_CREAM),
                List.of(quartz, fire, quartz, air, quartz, entropy),
                new ItemStack(ModItems.FOCI.get("hellbat").get()),
                AspectList.of(ModAspects.FIRE, 25).add(ModAspects.AIR, 15).add(ModAspects.BEAST, 15)
                        .add(ModAspects.ENTROPY, 25), 3, "focus_hellbat");
    }

    private void focus(RecipeOutput output, String name, AspectList cost, Map<Character, Ingredient> key,
            String... pattern) {
        arcane(output, "focus_" + name, "focus_" + name, new ItemStack(ModItems.FOCI.get(name).get()), cost, key,
                pattern);
    }

    private void nitorDyes(RecipeOutput output) {
        for (DyeColor colour : DyeColor.values()) {
            ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, ModBlocks.NITOR.get(colour).get())
                    .requires(ModTags.Items.NITOR)
                    .requires(DyeItem.byColor(colour))
                    .unlockedBy("has_nitor", has(ModTags.Items.NITOR))
                    .save(output, Alchemia.id(ModBlocks.name(colour) + "_from_dye"));
        }
    }

    private void voidMetal(RecipeOutput output) {
        crucible(output, "void_seed", Ingredient.of(Items.WHEAT_SEEDS),
                AspectList.of(ModAspects.DARKNESS, 8).add(ModAspects.VOID, 8).add(ModAspects.ELDRITCH, 2),
                new ItemStack(ModItems.VOID_SEED.get()), "void_metal");
        crucible(output, "void_ingot", Ingredient.of(ModItems.VOID_SEED),
                AspectList.of(ModAspects.METAL, 7).add(ModAspects.FLUX, 1),
                new ItemStack(ModItems.VOID_INGOT.get()), "void_metal");

        compress(output, ModItems.VOID_NUGGET, ModTags.Items.NUGGETS_VOID,
                ModItems.VOID_INGOT, ModTags.Items.INGOTS_VOID);

        arcane(output, "wand_cap_void_inert", "void_metal",
                new ItemStack(ModItems.INERT_CAPS.get("void").get()),
                AspectList.of(ModAspects.ENTROPY, 72).add(ModAspects.ORDER, 72)
                        .add(ModAspects.FIRE, 72).add(ModAspects.AIR, 72),
                Map.of('N', Ingredient.of(ModTags.Items.NUGGETS_VOID)), "NNN", "N N");
    }

    private void seal(RecipeOutput output, String kind, net.minecraft.world.level.ItemLike from, AspectList aspects,
            String research) {
        crucible(output, "seal_" + kind, Ingredient.of(from), aspects, new ItemStack(ModItems.SEALS.get(kind).get()),
                research);
    }

    private void crucible(RecipeOutput output, String name, Ingredient catalyst, AspectList aspects,
            ItemStack result, String research) {
        output.accept(Alchemia.id(name),
                new CrucibleRecipe(catalyst, aspects, result, Optional.of(Alchemia.id(research))), null);
    }
}
