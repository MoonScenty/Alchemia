package me.moonscenty.alchemia.datagen;

import java.util.concurrent.CompletableFuture;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.crafting.ArcaneWandRecipe;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModTags;
import me.moonscenty.alchemia.registry.StoneSet;
import me.moonscenty.alchemia.registry.WoodSet;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderLookup;
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
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
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

        // Arcane stone itself is shaped on the arcane workbench, which does not exist yet
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

        SpecialRecipeBuilder.special(category -> new ArcaneWandRecipe())
                .save(output, Alchemia.id("arcane_wand").toString());
    }
}
