package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.crafting.CrucibleInput;
import me.moonscenty.alchemia.crafting.CrucibleRecipe;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The two boilings that turn a handful of wheat seeds into metal.
 * <p>
 * These ask the recipes rather than the pot. What a crucible does with a thing dropped in it has tests of its own,
 * and a recipe with research behind it will not fire for a nameless item anyway; what is worth checking here is
 * that the water has to be right, and that being nearly right is not enough.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class VoidMetalTests {
    private static final String TEMPLATE = "empty_32x40x32";

    private static CrucibleRecipe recipe(GameTestHelper helper, String name) {
        RecipeHolder<?> found = helper.getLevel().getRecipeManager()
                .byKey(ResourceLocation.fromNamespaceAndPath(Alchemia.MODID, name)).orElse(null);
        helper.assertTrue(found != null && found.value() instanceof CrucibleRecipe, name + " is a crucible recipe");
        return (CrucibleRecipe) found.value();
    }

    /** A wheat seed steeped in darkness stops being a seed. */
    @GameTest(template = TEMPLATE)
    public static void seedsBoilIntoAVoidSeed(GameTestHelper helper) {
        CrucibleRecipe recipe = recipe(helper, "void_seed");
        AspectList water = AspectList.of(ModAspects.DARKNESS, 8)
                .add(ModAspects.VOID, 8).add(ModAspects.ELDRITCH, 2);

        CrucibleInput ready = new CrucibleInput(new ItemStack(Items.WHEAT_SEEDS), water);
        helper.assertTrue(recipe.matches(ready, helper.getLevel()), "the water was right");
        helper.assertTrue(recipe.assemble(ready, helper.getLevel().registryAccess())
                .is(ModItems.VOID_SEED.get()), "and a void seed came out");

        CrucibleInput thin = new CrucibleInput(new ItemStack(Items.WHEAT_SEEDS),
                water.reduce(ModAspects.ELDRITCH, 1));
        helper.assertTrue(!recipe.matches(thin, helper.getLevel()), "a point short is not nearly enough");

        CrucibleInput wrong = new CrucibleInput(new ItemStack(Items.PUMPKIN_SEEDS), water);
        helper.assertTrue(!recipe.matches(wrong, helper.getLevel()), "and it wants wheat seeds, not any seed");
        helper.succeed();
    }

    /** The seed, steeped again in metal, sets as an ingot -- but not without something wrong in the water. */
    @GameTest(template = TEMPLATE)
    public static void aVoidSeedSetsAsMetal(GameTestHelper helper) {
        CrucibleRecipe recipe = recipe(helper, "void_ingot");
        AspectList water = AspectList.of(ModAspects.METAL, 7).add(ModAspects.FLUX, 1);

        CrucibleInput ready = new CrucibleInput(new ItemStack(ModItems.VOID_SEED.get()), water);
        helper.assertTrue(recipe.matches(ready, helper.getLevel()), "the water was right");
        helper.assertTrue(recipe.assemble(ready, helper.getLevel().registryAccess())
                .is(ModItems.VOID_INGOT.get()), "and an ingot came out");

        CrucibleInput clean = new CrucibleInput(new ItemStack(ModItems.VOID_SEED.get()),
                AspectList.of(ModAspects.METAL, 7));
        helper.assertTrue(!recipe.matches(clean, helper.getLevel()), "clean water sets nothing");
        helper.succeed();
    }

    /** The last of the caps is drawn from it, which is the whole reason for going to the trouble. */
    @GameTest(template = TEMPLATE)
    public static void theVoidCapIsMadeOfIt(GameTestHelper helper) {
        RecipeHolder<?> found = helper.getLevel().getRecipeManager()
                .byKey(ResourceLocation.fromNamespaceAndPath(Alchemia.MODID, "wand_cap_void")).orElse(null);
        helper.assertTrue(found != null, "there is a recipe for the void cap");
        helper.assertTrue(found.value().getResultItem(helper.getLevel().registryAccess())
                .is(ModItems.WAND_CAPS.get("void").get()), "and it makes the cap");
        helper.succeed();
    }
}
