package me.moonscenty.alchemia.datagen;

import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.crafting.ArcaneShapedRecipe;
import me.moonscenty.alchemia.crafting.ArcaneShapelessRecipe;
import me.moonscenty.alchemia.crafting.InfusionRecipe;
import me.moonscenty.alchemia.crafting.LabelRecipe;
import me.moonscenty.alchemia.crafting.CrucibleRecipe;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.crafting.ArcaneWandRecipe;
import me.moonscenty.alchemia.block.CrystalType;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModTags;
import me.moonscenty.alchemia.registry.StoneSet;
import me.moonscenty.alchemia.registry.WoodSet;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
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
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        cook(output, "amber_from_ore", Ingredient.of(ModTags.Items.ORES_AMBER), has(ModTags.Items.ORES_AMBER), new ItemStack(ModItems.AMBER.get()));
        cook(output, "quicksilver_from_ore", Ingredient.of(ModTags.Items.ORES_CINNABAR), has(ModTags.Items.ORES_CINNABAR), new ItemStack(ModItems.QUICKSILVER.get()));
        cook(output, "quicksilver_from_raw_cinnabar", Ingredient.of(ModItems.RAW_CINNABAR), has(ModItems.RAW_CINNABAR), new ItemStack(ModItems.QUICKSILVER.get()));

        compress(output, ModItems.ALCHEMIUM_NUGGET, ModTags.Items.NUGGETS_ALCHEMIUM, ModItems.ALCHEMIUM_INGOT, ModTags.Items.INGOTS_ALCHEMIUM);
        compress(output, ModItems.ALCHEMIUM_INGOT, ModTags.Items.INGOTS_ALCHEMIUM, ModBlocks.ALCHEMIUM_BLOCK, ModTags.Items.STORAGE_BLOCKS_ALCHEMIUM);
        compress(output, ModItems.BRASS_NUGGET, ModTags.Items.NUGGETS_BRASS, ModItems.BRASS_INGOT, ModTags.Items.INGOTS_BRASS);
        compress(output, ModItems.BRASS_INGOT, ModTags.Items.INGOTS_BRASS, ModBlocks.BRASS_BLOCK, ModTags.Items.STORAGE_BLOCKS_BRASS);
        compress(output, ModItems.QUICKSILVER_DROP, ModTags.Items.NUGGETS_QUICKSILVER, ModItems.QUICKSILVER, ModTags.Items.GEMS_QUICKSILVER);

        plate(output, ModItems.ALCHEMIUM_PLATE, ModTags.Items.INGOTS_ALCHEMIUM);
        plate(output, ModItems.BRASS_PLATE, ModTags.Items.INGOTS_BRASS);
        plate(output, ModItems.IRON_PLATE, Tags.Items.INGOTS_IRON);
        gear(output, ModItems.ALCHEMIUM_GEAR, ModTags.Items.NUGGETS_ALCHEMIUM);
        gear(output, ModItems.BRASS_GEAR, ModTags.Items.NUGGETS_BRASS);

        // a balanced shard put in a furnace comes out as salis mundus, exactly as in the original
        cook(output, "salis_mundus", Ingredient.of(ModItems.BALANCED_SHARD), has(ModItems.BALANCED_SHARD),
                new ItemStack(ModItems.SALIS_MUNDUS.get()));

        wand(output);
        arcane(output);
        crucible(output);
        distillery(output);
        voidMetal(output);
        infusion(output);

        // arcane stone itself is shaped on the arcane workbench; these are what it is worked into afterwards
        quadrupleFrom(output, ModBlocks.ARCANE_STONE_BRICKS.block(), ModBlocks.ARCANE_STONE.block());
        for (StoneSet set : ModBlocks.STONE_SETS) {
            stairBuilder(set.stairs(), Ingredient.of(set.block())).unlockedBy("has_input", has(set.block())).save(output);
            slab(output, RecipeCategory.BUILDING_BLOCKS, set.slab(), set.block());
            stonecutting(output, set.stairs(), set.block(), 1);
            stonecutting(output, set.slab(), set.block(), 2);
        }
        stonecutting(output, ModBlocks.ARCANE_STONE_BRICKS.block(), ModBlocks.ARCANE_STONE.block(), 1);

        nodeStabilizer(output);

        // Amber blocks and bricks are cut back and forth freely, as in the original
        compress(output, ModItems.AMBER, ModTags.Items.GEMS_AMBER, ModBlocks.AMBER_BLOCK, ModTags.Items.STORAGE_BLOCKS_AMBER);
        quadrupleFrom(output, ModBlocks.AMBER_BRICKS, ModBlocks.AMBER_BLOCK);
        quadrupleFrom(output, ModBlocks.AMBER_BLOCK, ModBlocks.AMBER_BRICKS);

        // Both blooms are steeped down into the essence they carry
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

    /**
     * The vanilla helper saves under the minecraft namespace, so this one spells the id out.
     */
    private static void stonecutting(RecipeOutput output, ItemLike result, ItemLike material, int count) {
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(material), RecipeCategory.BUILDING_BLOCKS, result, count)
                .unlockedBy("has_input", has(material))
                .save(output, Alchemia.id(getConversionRecipeName(result, material) + "_stonecutting"));
    }

    /** Four blocks in a square become four of another, so nothing is lost either way. */
    private static void quadrupleFrom(RecipeOutput output, ItemLike result, ItemLike input) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, result, 4)
                .pattern("##").pattern("##")
                .define('#', input)
                .unlockedBy("has_input", has(input))
                .save(output, Alchemia.id(BuiltInRegistries.ITEM.getKey(result.asItem()).getPath()
                        + "_from_" + BuiltInRegistries.ITEM.getKey(input.asItem()).getPath()));
    }

    /** One plant yields one of whatever it is steeped with. */
    private static void distill(RecipeOutput output, ItemLike plant, ItemLike result) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, result)
                .requires(plant)
                .unlockedBy("has_input", has(plant))
                .save(output, Alchemia.id(BuiltInRegistries.ITEM.getKey(result.asItem()).getPath()
                        + "_from_" + BuiltInRegistries.ITEM.getKey(plant.asItem()).getPath()));
    }

    /**
     * Arcane stone around a balanced shard, braced with alchemium. What holds a node still is mostly a matter of
     * having something steady to hold it against.
     */
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

    /** Nine small items pack into one big item and back. */
    private static void compress(RecipeOutput output, ItemLike small, TagKey<Item> smallTag, ItemLike big, TagKey<Item> bigTag) {
        String smallName = BuiltInRegistries.ITEM.getKey(small.asItem()).getPath();
        String bigName = BuiltInRegistries.ITEM.getKey(big.asItem()).getPath();
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, big)
                .pattern("###").pattern("###").pattern("###")
                .define('#', smallTag)
                .unlockedBy("has_input", has(smallTag))
                .save(output, Alchemia.id(bigName + "_from_" + smallName));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, small, 9)
                .requires(bigTag)
                .unlockedBy("has_input", has(bigTag))
                .save(output, Alchemia.id(smallName + "_from_" + bigName));
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

    /** Clusters are purified ore: they smelt into twice the usual amount. */
    private static void cluster(RecipeOutput output, String resultName, ItemLike cluster, ItemLike result) {
        cook(output, resultName + "_from_cluster", Ingredient.of(cluster), has(cluster), new ItemStack(result, 2));
    }

    /** Registers both the furnace and the blast furnace version of a recipe. */
    private static void cook(RecipeOutput output, String name, Ingredient input, Criterion<?> unlockedBy, ItemStack result) {
        SimpleCookingRecipeBuilder.smelting(input, RecipeCategory.MISC, result, 1.0F, 200)
                .unlockedBy("has_input", unlockedBy)
                .save(output, Alchemia.id(name + "_smelting"));
        SimpleCookingRecipeBuilder.blasting(input, RecipeCategory.MISC, result, 1.0F, 100)
                .unlockedBy("has_input", unlockedBy)
                .save(output, Alchemia.id(name + "_blasting"));
    }

    /**
     * The first wand, and the caps it is made with.
     * <p>
     * Both are worked at a plain bench, as they were in the original, and they have to be: an arcane workbench takes
     * its price out of a wand, so the first one cannot be made at one. Every other pairing of rod and cap is put
     * together at the arcane workbench by {@code alchemia:arcane_wand}, which is written in code because nine rods
     * against five caps is forty-five results and none of them is worth a file.
     * <p>
     * A wand with nothing written on it is a wooden rod with iron caps, so this recipe needs no components: leaving
     * them off says the same thing and keeps the item stacking with one built at the bench.
     */
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
    }

    /**
     * What is shaped on the arcane workbench, and what it costs in vis.
     * <p>
     * The prices are the original's: eight times what the piece is reckoned to be worth, split across the primals
     * that go into the work. A cap is order, fire and air; a rod is entropy alone, since cutting a wand out of a
     * log is mostly undoing what the tree made.
     */
    private void arcane(RecipeOutput output) {
        arcane(output, "wand_cap_gold", new ItemStack(ModItems.WAND_CAPS.get("gold").get()),
                AspectList.of(ModAspects.ORDER, 24).add(ModAspects.FIRE, 24).add(ModAspects.AIR, 24),
                Map.of('N', Ingredient.of(Tags.Items.NUGGETS_GOLD)), "NNN", "N N");

        arcane(output, "wand_cap_brass", new ItemStack(ModItems.WAND_CAPS.get("brass").get()),
                AspectList.of(ModAspects.ORDER, 24).add(ModAspects.FIRE, 24).add(ModAspects.AIR, 24),
                Map.of('N', Ingredient.of(ModTags.Items.NUGGETS_BRASS)), "NNN", "N N");

        // what the workbench makes of these two is a casting, not a cap; the altar finishes them
        arcane(output, "wand_cap_alchemium_inert", new ItemStack(ModItems.INERT_CAPS.get("alchemium").get()),
                AspectList.of(ModAspects.ORDER, 48).add(ModAspects.FIRE, 48).add(ModAspects.AIR, 48),
                Map.of('N', Ingredient.of(ModTags.Items.NUGGETS_ALCHEMIUM)), "NNN", "N N");

        arcane(output, "wand_rod_greatwood", new ItemStack(ModItems.WAND_RODS.get("greatwood").get()),
                AspectList.of(ModAspects.ENTROPY, 24),
                Map.of('G', Ingredient.of(ModBlocks.GREATWOOD.log())), " G", "G ");

        arcane(output, "arcane_stone", new ItemStack(ModBlocks.ARCANE_STONE.block(), 9),
                AspectList.of(ModAspects.EARTH, 5).add(ModAspects.FIRE, 5),
                Map.of('S', Ingredient.of(Tags.Items.STONES), 'C', Ingredient.of(ModTags.Items.SHARDS)),
                "SSS", "SCS", "SSS");

        // the two altar stones: the same eight stones round a block of gold, with light or with fuel packed in
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

    private void arcane(RecipeOutput output, String name, ItemStack result, AspectList cost,
            Map<Character, Ingredient> key, String... pattern) {
        arcane(output, name, null, result, cost, key, pattern);
    }

    /** The same, for the ones that are only shown once the research behind them is done. */
    private void arcane(RecipeOutput output, String name, String research, ItemStack result, AspectList cost,
            Map<Character, Ingredient> key, String... pattern) {
        output.accept(Alchemia.id(name), new ArcaneShapedRecipe("", ShapedRecipePattern.of(key, pattern), result,
                cost, Optional.ofNullable(research).map(Alchemia::id)), null);
    }

    /** Worked at the arcane workbench, but laid out anyhow: most of the tube fittings are one thing plus another. */
    private void shapeless(RecipeOutput output, String name, String research, ItemStack result, AspectList cost,
            Ingredient... ingredients) {
        NonNullList<Ingredient> laid = NonNullList.of(Ingredient.EMPTY, ingredients);
        output.accept(Alchemia.id(name), new ArcaneShapelessRecipe("", laid, result, cost,
                Optional.ofNullable(research).map(Alchemia::id)), null);
    }

    /**
     * The distillery: what catches essentia, what carries it, and what it is kept in.
     * <p>
     * The prices and the shapes are the original's. The one substitution is the tube's quicksilver nugget, which
     * the original spelled as the fifth of its nine nuggets.
     */
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

        arcane(output, "alembic", "distillation", new ItemStack(ModBlocks.ALEMBIC.get()),
                AspectList.of(ModAspects.AIR, 15).add(ModAspects.WATER, 25),
                Map.of('W', Ingredient.of(ModBlocks.GREATWOOD.planks()),
                        'F', Ingredient.of(ModItems.FILTER),
                        'S', Ingredient.of(ModItems.BRASS_PLATE),
                        'B', Ingredient.of(Items.BUCKET)),
                "WFW", "SBS", "WFW");

        // the only one of the lot worked at a plain bench: glass and a lump of clay for the stopper
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.PHIAL.get(), 8)
                .pattern(" C ").pattern("G G").pattern(" G ")
                .define('G', Tags.Items.GLASS_BLOCKS)
                .define('C', Items.CLAY_BALL)
                .unlockedBy("has_glass", has(Tags.Items.GLASS_BLOCKS))
                .save(output);

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

        // writing an aspect on a label and rubbing it out again are both in code: an aspect is not a shape
        SpecialRecipeBuilder.special(LabelRecipe::new).save(output, Alchemia.id("jar_label_writing").toString());

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.JAR_BRACE.get(), 2)
                .pattern("NSN").pattern("S S").pattern("NSN")
                .define('N', ModTags.Items.NUGGETS_BRASS)
                .define('S', Tags.Items.RODS_WOODEN)
                .unlockedBy("has_nugget", has(ModTags.Items.NUGGETS_BRASS))
                .save(output);

        tubes(output);
    }

    /** The pipework: one recipe for the plain length, and one apiece for what is done to it afterwards. */
    private void tubes(RecipeOutput output) {
        arcane(output, "tube", "tubes", new ItemStack(ModBlocks.TUBE.get(), 8),
                AspectList.of(ModAspects.WATER, 5).add(ModAspects.ORDER, 5),
                Map.of('I', Ingredient.of(Tags.Items.INGOTS_IRON),
                        'B', Ingredient.of(Tags.Items.NUGGETS_GOLD),
                        'G', Ingredient.of(Tags.Items.GLASS_BLOCKS),
                        'Q', Ingredient.of(ModTags.Items.NUGGETS_QUICKSILVER)),
                " Q ", "IGI", " B ");

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

    /**
     * The altar, and the seven rods that can only be made on one.
     * <p>
     * The rod prices are the original's: twice the rod is reckoned to be worth in whatever it is chiefly made of,
     * and once each in a couple of aspects that say what it is for. Silverwood pays once in everything instead,
     * which is the whole point of silverwood.
     */
    private void infusion(RecipeOutput output) {
        arcane(output, "infusion_matrix", "infusion", new ItemStack(ModBlocks.INFUSION_MATRIX.get()),
                AspectList.of(ModAspects.ORDER, 100),
                Map.of('S', Ingredient.of(ModBlocks.ARCANE_STONE_BRICKS.block()),
                        'B', Ingredient.of(ModTags.Items.SHARDS),
                        'N', Ingredient.of(ModItems.BALANCED_SHARD)),
                "SBS", "BNB", "SBS");

        arcane(output, "arcane_pedestal", "infusion", new ItemStack(ModBlocks.ARCANE_PEDESTAL.get()),
                AspectList.of(ModAspects.AIR, 5),
                Map.of('S', Ingredient.of(ModBlocks.ARCANE_STONE.slab()),
                        'B', Ingredient.of(ModBlocks.ARCANE_STONE.block())),
                "SSS", " B ", "SSS");

        // the rods: what it is made of in the middle, a balanced shard and its own element around it
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

        // silverwood is the exception: a little of everything, and every primal shard laid out around it
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

        // the two caps a workbench can only cast. Salis mundus round them, and the altar does the rest
        infusion(output, "wand_cap_alchemium", Ingredient.of(ModItems.INERT_CAPS.get("alchemium")),
                salis(3), new ItemStack(ModItems.WAND_CAPS.get("alchemium").get()),
                AspectList.of(ModAspects.ENERGY, 12).add(ModAspects.AURA, 6), 5, "wand_cap_alchemium");
        infusion(output, "wand_cap_void", Ingredient.of(ModItems.INERT_CAPS.get("void")),
                salis(4), new ItemStack(ModItems.WAND_CAPS.get("void").get()),
                AspectList.of(ModAspects.ENERGY, 18).add(ModAspects.VOID, 18)
                        .add(ModAspects.ELDRITCH, 18).add(ModAspects.AURA, 18), 8, "void_metal");
    }

    /** A ring of salis mundus, which is what every finishing of a cast thing is laid out with. */
    private static List<Ingredient> salis(int many) {
        return java.util.Collections.nCopies(many, Ingredient.of(ModItems.SALIS_MUNDUS));
    }

    /** One wand rod: the stuff it is cut from, with a balanced shard and a shard of its own element beside it. */
    private void rod(RecipeOutput output, String name, Ingredient from, int instability, AspectList cost,
            CrystalType element) {
        infusion(output, "wand_rod_" + name, from,
                List.of(Ingredient.of(ModItems.BALANCED_SHARD), Ingredient.of(ModItems.SHARDS.get(element))),
                new ItemStack(ModItems.WAND_RODS.get(name).get()), cost, instability, "wand_rods");
    }

    private void infusion(RecipeOutput output, String name, Ingredient central, List<Ingredient> ring,
            ItemStack result, AspectList essentia, int instability, String research) {
        output.accept(Alchemia.id(name), new InfusionRecipe(central, ring, result, essentia, instability,
                Optional.of(Alchemia.id(research))), null);
    }

    /**
     * What is boiled out of a crucible.
     * <p>
     * A crucible recipe is not a shape but a state: what has to be dissolved in the water already, and the one
     * thing thrown in afterwards to finish it. The thing thrown in is used up either way, so an iron ingot dropped
     * into a pot that is not ready simply comes apart into what iron is made of.
     */
    private void crucible(RecipeOutput output) {
        crucible(output, "alchemium_ingot_from_iron", Ingredient.of(Tags.Items.INGOTS_IRON),
                AspectList.of(ModAspects.EARTH, 2).add(ModAspects.ORDER, 2),
                new ItemStack(ModItems.ALCHEMIUM_INGOT.get()), "metallurgy");
        crucible(output, "brass_ingot_from_iron", Ingredient.of(Tags.Items.INGOTS_IRON),
                AspectList.of(ModAspects.ENERGY, 1).add(ModAspects.WATER, 1),
                new ItemStack(ModItems.BRASS_INGOT.get()), "metallurgy");

        // coal boiled until it is four coals, and glowstone boiled until it is light with nothing under it
        crucible(output, "alumentum", Ingredient.of(ItemTags.COALS),
                AspectList.of(ModAspects.ENERGY, 3).add(ModAspects.FIRE, 3).add(ModAspects.ENTROPY, 3),
                new ItemStack(ModItems.ALUMENTUM.get()), "alumentum");
        crucible(output, "nitor", Ingredient.of(Items.GLOWSTONE_DUST),
                AspectList.of(ModAspects.ENERGY, 3).add(ModAspects.FIRE, 3).add(ModAspects.LIGHT, 3),
                new ItemStack(ModBlocks.NITOR.get(DyeColor.WHITE).get(), NITOR_AT_A_TIME), "nitor");
        nitorDyes(output);
    }

    /** How many flames one boiling of glowstone makes. */
    private static final int NITOR_AT_A_TIME = 4;

    /**
     * A flame takes a dye like wool does.
     * <p>
     * Any of the sixteen goes in and the dyed one comes out, so a flame can be changed its mind about rather than
     * boiled again. The white one is no more the original than the rest -- it is only the one the crucible makes.
     */
    private void nitorDyes(RecipeOutput output) {
        for (DyeColor colour : DyeColor.values()) {
            ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, ModBlocks.NITOR.get(colour).get())
                    .requires(ModTags.Items.NITOR)
                    .requires(DyeItem.byColor(colour))
                    .unlockedBy("has_nitor", has(ModTags.Items.NITOR))
                    .save(output, Alchemia.id(ModBlocks.name(colour) + "_from_dye"));
        }
    }

    /**
     * Void metal, which is boiled rather than mined.
     * <p>
     * A wheat seed steeped in darkness stops being a seed; the thing it becomes, steeped again in metal, comes out
     * as an ingot. The prices are the original's, and so is the oddity that the second boiling wants a point of
     * flux in the water -- the metal will not set without something wrong in it.
     */
    private void voidMetal(RecipeOutput output) {
        crucible(output, "void_seed", Ingredient.of(Items.WHEAT_SEEDS),
                AspectList.of(ModAspects.DARKNESS, 8).add(ModAspects.VOID, 8).add(ModAspects.ELDRITCH, 2),
                new ItemStack(ModItems.VOID_SEED.get()), "void_metal");
        crucible(output, "void_ingot", Ingredient.of(ModItems.VOID_SEED),
                AspectList.of(ModAspects.METAL, 7).add(ModAspects.FLUX, 1),
                new ItemStack(ModItems.VOID_INGOT.get()), "void_metal");

        compress(output, ModItems.VOID_NUGGET, ModTags.Items.NUGGETS_VOID,
                ModItems.VOID_INGOT, ModTags.Items.INGOTS_VOID);

        // the last of the caps, and the dearest: nine times what an iron one costs, across all four of the primals
        arcane(output, "wand_cap_void_inert", "void_metal",
                new ItemStack(ModItems.INERT_CAPS.get("void").get()),
                AspectList.of(ModAspects.ENTROPY, 72).add(ModAspects.ORDER, 72)
                        .add(ModAspects.FIRE, 72).add(ModAspects.AIR, 72),
                Map.of('N', Ingredient.of(ModTags.Items.NUGGETS_VOID)), "NNN", "N N");
    }

    private void crucible(RecipeOutput output, String name, Ingredient catalyst, AspectList aspects,
            ItemStack result, String research) {
        output.accept(Alchemia.id(name),
                new CrucibleRecipe(catalyst, aspects, result, Optional.of(Alchemia.id(research))), null);
    }
}
