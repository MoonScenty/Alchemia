package me.moonscenty.alchemia.gametest;

import java.util.List;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.entity.InfernalFurnaceBlockEntity;
import me.moonscenty.alchemia.crafting.TripleMeatTreatRecipe;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Meat chunks: what the infernal furnace throws out besides off raw meat, and the treat three kinds make. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class MeatChunkTests {
    private static final String TEMPLATE = "empty_32x40x32";

    @GameTest(template = TEMPLATE)
    public static void theFurnaceThrowsOutChunksOffMeat(GameTestHelper helper) {
        helper.assertTrue(InfernalFurnaceBlockEntity.bonus(new ItemStack(Items.BEEF))
                .is(ModItems.MEAT_CHUNKS.get("beef").get()), "beef");
        helper.assertTrue(InfernalFurnaceBlockEntity.bonus(new ItemStack(Items.SALMON))
                .is(ModItems.MEAT_CHUNKS.get("fish").get()), "any raw fish");
        helper.assertTrue(InfernalFurnaceBlockEntity.bonus(new ItemStack(Items.MUTTON))
                .is(ModItems.MEAT_CHUNKS.get("mutton").get()), "mutton");
        helper.assertTrue(InfernalFurnaceBlockEntity.bonus(new ItemStack(Items.COOKED_BEEF)).isEmpty(),
                "nothing off cooked meat");
        helper.assertValueEqual(new ItemStack(ModItems.MEAT_CHUNKS.get("pork").get())
                .getUseDuration(helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL)), 10,
                "eaten in half a second");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void threeDifferentChunksAndSugarMakeATreat(GameTestHelper helper) {
        TripleMeatTreatRecipe recipe = new TripleMeatTreatRecipe(CraftingBookCategory.MISC);
        helper.assertTrue(recipe.matches(grid(Items.SUGAR, chunk("beef"), chunk("fish"), chunk("rabbit")),
                helper.getLevel()), "sugar and three kinds");
        helper.assertFalse(recipe.matches(grid(Items.SUGAR, chunk("beef"), chunk("beef"), chunk("rabbit")),
                helper.getLevel()), "not two of one kind");
        helper.assertFalse(recipe.matches(grid(Items.SUGAR, chunk("beef"), chunk("fish")), helper.getLevel()),
                "not two kinds");
        helper.assertFalse(recipe.matches(grid(chunk("beef"), chunk("fish"), chunk("rabbit"), chunk("pork")),
                helper.getLevel()), "not without sugar");
        helper.assertTrue(helper.getLevel().getRecipeManager()
                .byKey(Alchemia.id("triple_meat_treat")).isPresent(), "the recipe is loaded");
        helper.succeed();
    }

    private static Item chunk(String meat) {
        return ModItems.MEAT_CHUNKS.get(meat).get();
    }

    private static CraftingInput grid(Item... items) {
        List<ItemStack> stacks = new java.util.ArrayList<>();
        for (Item item : items) {
            stacks.add(new ItemStack(item));
        }
        while (stacks.size() < 9) {
            stacks.add(ItemStack.EMPTY);
        }
        return CraftingInput.of(3, 3, stacks);
    }
}
