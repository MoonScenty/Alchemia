package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.crafting.InfusionRecipe;
import me.moonscenty.alchemia.entity.PechTrades;
import me.moonscenty.alchemia.item.FocusItem;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Step 14D-3: what was left off because it did not exist then, put back. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class FillInTests {
    private static final String TEMPLATE = "empty_32x40x32";

    /** The primal focus asks ten to eighteen of each primal, even, the same within a fifth of a second. */
    @GameTest(template = TEMPLATE)
    public static void primalPriceShimmers(GameTestHelper helper) {
        long now = 1_000_000L;
        AspectList first = FocusItem.primalCost(now);
        helper.assertTrue(first.equals(FocusItem.primalCost(now + 199 - now % 200)), "the same within the window");
        boolean moved = false;
        for (int window = 1; window < 50 && !moved; window++) {
            moved = !FocusItem.primalCost(now + window * 200L).equals(first);
        }
        helper.assertTrue(moved, "and something else soon after");
        for (var primal : java.util.List.of(ModAspects.AIR, ModAspects.EARTH, ModAspects.FIRE, ModAspects.WATER,
                ModAspects.ORDER, ModAspects.ENTROPY)) {
            int asked = first.get(primal);
            helper.assertTrue(asked >= 10 && asked <= 18 && asked % 2 == 0, "ten to eighteen, even: " + asked);
        }
        helper.succeed();
    }

    /** The stalker has its candles back, fifteen colours, at the lowest worth. */
    @GameTest(template = TEMPLATE)
    public static void stalkerSellsCandles(GameTestHelper helper) {
        long candles = PechTrades.list(2).stream()
                .filter(lot -> lot.worth() == 1)
                .map(lot -> lot.makes().apply(helper.getLevel()))
                .filter(stack -> ModBlocks.TALLOW_CANDLES.values().stream()
                        .anyMatch(candle -> stack.is(candle.get().asItem())))
                .count();
        helper.assertValueEqual(candles, 15L, "every colour but black");
        helper.succeed();
    }

    /** Liquid death is made, the boots take any fish, the rings take a fragment. */
    @GameTest(template = TEMPLATE)
    public static void recipesUseWhatNowExists(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager();
        helper.assertTrue(recipes.byKey(Alchemia.id("liquid_death")).isPresent(), "liquid death is made");
        InfusionRecipe boots = (InfusionRecipe) recipes.byKey(Alchemia.id("traveller_boots")).orElseThrow().value();
        helper.assertTrue(boots.ring().stream().anyMatch(part -> part.test(new ItemStack(Items.SALMON))),
                "any raw fish will do");
        boolean fragment = recipes.getAllRecipesFor(me.moonscenty.alchemia.crafting.ModRecipes.INFUSION.get())
                .stream().filter(holder -> holder.value().enchants().isPresent())
                .allMatch(holder -> holder.value().ring().stream().map(Ingredient::getItems)
                        .anyMatch(items -> items.length > 0 && items[0].is(ModItems.KNOWLEDGE_FRAGMENT.get())));
        helper.assertTrue(fragment, "every enchanting ring has a knowledge fragment");
        helper.assertTrue(me.moonscenty.alchemia.item.bauble.Recharging.chargeable(
                new ItemStack(ModItems.VERDANT_HEART.get())), "a verdant heart goes on the pedestal");
        helper.succeed();
    }
}
