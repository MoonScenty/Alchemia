package me.moonscenty.alchemia.gametest;

import java.util.List;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.crafting.ArcaneSceptreRecipe;
import me.moonscenty.alchemia.entity.QuickMeleeGoal;
import me.moonscenty.alchemia.item.VisHolder;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModWandParts;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Sceptres, which hold half as much again and make everything a tenth cheaper, and the half-second swing the
 * original gave its own fighters.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class SceptreTests {
    private static final String TEMPLATE = "empty_32x40x32";

    /** The sceptre's layout: a cap above, beside and below the rod, and a primal charm in the top corner. */
    private static CraftingInput bench(ItemStack cap, ItemStack rod) {
        ItemStack charm = new ItemStack(ModItems.PRIMAL_CHARM.get());
        return CraftingInput.of(3, 3, List.of(ItemStack.EMPTY, cap, charm, ItemStack.EMPTY, rod, cap, cap,
                ItemStack.EMPTY, ItemStack.EMPTY));
    }

    /** Gold caps on a greatwood rod make a gold greatwood sceptre, at ten times three times three of every primal. */
    @GameTest(template = TEMPLATE)
    public static void aSceptreIsMadeAndPricedByWhatWentIn(GameTestHelper helper) {
        ItemStack gold = new ItemStack(ModItems.WAND_CAPS.get("gold").get());
        CraftingInput grid = bench(gold, new ItemStack(ModItems.WAND_RODS.get("greatwood").get()));
        ArcaneSceptreRecipe recipe = ArcaneSceptreRecipe.INSTANCE;
        helper.assertTrue(recipe.matches(grid, helper.getLevel()), "the grid should spell a sceptre");
        ItemStack made = recipe.assemble(grid, helper.getLevel().registryAccess());
        helper.assertTrue(WandItem.isSceptre(made), "what comes out should be a sceptre");
        helper.assertValueEqual(((TranslatableContents) made.getHoverName().getContents()).getKey(),
                "item.alchemia.sceptre.named", "and called one");
        helper.assertValueEqual(WandItem.capacity(made), 375, "holding half as much again as a greatwood wand");
        helper.assertValueEqual(recipe.cost(grid).get(ModAspects.FIRE), 3 * 3 * 10, "at ninety of every primal");

        CraftingInput staff = bench(gold, new ItemStack(ModItems.WAND_RODS.get("greatwood_staff").get()));
        helper.assertFalse(recipe.matches(staff, helper.getLevel()), "a staff core takes no crossbar");
        CraftingInput odd = CraftingInput.of(3, 3, List.of(ItemStack.EMPTY, gold,
                new ItemStack(ModItems.PRIMAL_CHARM.get()), ItemStack.EMPTY,
                new ItemStack(ModItems.WAND_RODS.get("greatwood").get()),
                new ItemStack(ModItems.WAND_CAPS.get("iron").get()), gold, ItemStack.EMPTY, ItemStack.EMPTY));
        helper.assertFalse(recipe.matches(odd, helper.getLevel()), "nor will odd caps do");
        helper.succeed();
    }

    /** A sceptre pays a tenth less for everything, at a bench as through a focus. */
    @GameTest(template = TEMPLATE)
    public static void aSceptrePaysLess(GameTestHelper helper) {
        Player bearer = helper.makeMockPlayer(GameType.SURVIVAL);
        WandItem item = (WandItem) ModItems.WAND.get();
        AspectList price = AspectList.of(ModAspects.ORDER, 10);
        ItemStack sceptre = WandItem.brimming(WandItem.sceptre(ModWandParts.GREATWOOD, ModWandParts.GOLD));
        item.take(sceptre, price, bearer, VisHolder.Spending.CRAFTING);
        helper.assertValueEqual(375 - item.held(sceptre, ModAspects.ORDER), 9, "nine at the bench, not ten");
        helper.succeed();
    }

    /** The original's own fighters swing every half second; the taint crawler alone at the game's pace. */
    @GameTest(template = TEMPLATE)
    public static void theOriginalsFightersSwingQuickly(GameTestHelper helper) {
        for (EntityType<? extends Mob> type : List.<EntityType<? extends Mob>>of(ModEntities.TAINTED_COW.get(),
                ModEntities.TAINTED_CREEPER.get(), ModEntities.TAINTED_RABBIT.get(), ModEntities.TAINTACLE.get(),
                ModEntities.CULTIST_KNIGHT.get(), ModEntities.ELDRITCH_GUARDIAN.get())) {
            Mob mob = type.create(helper.getLevel());
            helper.assertTrue(quick(mob), type.getDescriptionId() + " should swing every half second");
        }
        Mob crawler = ModEntities.TAINT_CRAWLER.get().create(helper.getLevel());
        helper.assertFalse(quick(crawler), "the crawler swings at the game's pace");
        helper.succeed();
    }

    private static boolean quick(Mob mob) {
        return mob.goalSelector.getAvailableGoals().stream().anyMatch(goal -> goal.getGoal() instanceof QuickMeleeGoal);
    }
}
