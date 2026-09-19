package me.moonscenty.alchemia.datagen;

import java.util.concurrent.CompletableFuture;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModTags;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        cook(output, "amber_from_ore", Ingredient.of(ModTags.Items.ORES_AMBER), has(ModTags.Items.ORES_AMBER), new ItemStack(ModItems.AMBER.get()));
        cook(output, "quicksilver_from_ore", Ingredient.of(ModTags.Items.ORES_CINNABAR), has(ModTags.Items.ORES_CINNABAR), new ItemStack(ModItems.QUICKSILVER.get()));
        cook(output, "quicksilver_from_raw_cinnabar", Ingredient.of(ModItems.RAW_CINNABAR), has(ModItems.RAW_CINNABAR), new ItemStack(ModItems.QUICKSILVER.get()));

        cluster(output, "iron_ingot", ModItems.IRON_CLUSTER, Items.IRON_INGOT);
        cluster(output, "gold_ingot", ModItems.GOLD_CLUSTER, Items.GOLD_INGOT);
        cluster(output, "copper_ingot", ModItems.COPPER_CLUSTER, Items.COPPER_INGOT);
        cluster(output, "quicksilver", ModItems.CINNABAR_CLUSTER, ModItems.QUICKSILVER);
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
}
