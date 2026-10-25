package me.moonscenty.alchemia.gametest;

import java.util.List;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.entity.JarBlockEntity;
import me.moonscenty.alchemia.block.entity.TubeBlockEntity;
import me.moonscenty.alchemia.item.EssentiaResonatorItem;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The small instruments and odds and ends the first pass left out: the essentia resonator's reading, and what the
 * pechs' coins are worth.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class InstrumentTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final BlockPos HERE = new BlockPos(8, 2, 8);

    private static String key(Component line) {
        return line.getContents() instanceof TranslatableContents words ? words.getKey() : "";
    }

    private static Object argument(Component line, int index) {
        return ((TranslatableContents) line.getContents()).getArgs()[index];
    }

    /** Held to the lid of a jar, the resonator says what is in it and that it draws only more of the same. */
    @GameTest(template = TEMPLATE)
    public static void theResonatorReadsAJar(GameTestHelper helper) {
        helper.setBlock(HERE, ModBlocks.JAR.get());
        JarBlockEntity jar = (JarBlockEntity) helper.getBlockEntity(HERE);
        for (int i = 0; i < 3; i++) {
            jar.accept(ModAspects.FIRE);
        }

        List<Component> lines = EssentiaResonatorItem.report(helper.getLevel(), helper.absolutePos(HERE),
                Direction.UP);
        helper.assertValueEqual(lines.size(), 2, "one line for what it holds and one for what it draws");
        helper.assertValueEqual(key(lines.get(0)), "item.alchemia.essentia_resonator.contains", "the first line");
        helper.assertValueEqual(argument(lines.get(0), 0), 3, "it holds three");
        Component draws = (Component) argument(lines.get(1), 0);
        helper.assertTrue(draws.getString().equals(ModAspects.FIRE.value().displayName().getString()),
                "a jar with fire in it draws fire and nothing else, not " + draws.getString());

        // through its glass side there is nothing to be read or drawn
        List<Component> side = EssentiaResonatorItem.report(helper.getLevel(), helper.absolutePos(HERE),
                Direction.NORTH);
        helper.assertValueEqual(side.size(), 1, "only the line for what it draws");
        helper.assertValueEqual(key((Component) argument(side.get(0), 0)),
                "item.alchemia.essentia_resonator.nothing", "and through the glass it draws nothing");
        helper.succeed();
    }

    /** A plain tube draws anything through an open side, and nothing through a shut one. */
    @GameTest(template = TEMPLATE)
    public static void theResonatorReadsATube(GameTestHelper helper) {
        helper.setBlock(HERE, ModBlocks.TUBE.get());
        BlockPos at = helper.absolutePos(HERE);
        List<Component> open = EssentiaResonatorItem.report(helper.getLevel(), at, Direction.EAST);
        helper.assertValueEqual(key((Component) argument(open.get(0), 0)),
                "item.alchemia.essentia_resonator.anything", "an open tube draws anything");

        ((TubeBlockEntity) helper.getBlockEntity(HERE)).shut(Direction.EAST, true);
        List<Component> shut = EssentiaResonatorItem.report(helper.getLevel(), at, Direction.EAST);
        helper.assertValueEqual(key((Component) argument(shut.get(0), 0)),
                "item.alchemia.essentia_resonator.nothing", "a shut side draws nothing");

        helper.setBlock(HERE.above(), Blocks.STONE);
        helper.assertTrue(EssentiaResonatorItem.report(helper.getLevel(), helper.absolutePos(HERE.above()),
                Direction.UP).isEmpty(), "stone has nothing to say");
        helper.succeed();
    }

    /** Nine coins make a gold ingot, and one in a furnace melts to a nugget. */
    @GameTest(template = TEMPLATE)
    public static void coinsAreWorthGold(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager();
        ItemStack coin = new ItemStack(ModItems.COIN.get());
        CraftingInput nine = CraftingInput.of(3, 3, java.util.Collections.nCopies(9, coin));
        ItemStack ingot = recipes.getRecipeFor(RecipeType.CRAFTING, nine, helper.getLevel())
                .map(recipe -> recipe.value().assemble(nine, helper.getLevel().registryAccess()))
                .orElse(ItemStack.EMPTY);
        helper.assertTrue(ingot.is(Items.GOLD_INGOT), "nine coins should make a gold ingot, not " + ingot);

        SingleRecipeInput one = new SingleRecipeInput(coin);
        ItemStack nugget = recipes.getRecipeFor(RecipeType.SMELTING, one, helper.getLevel())
                .map(recipe -> recipe.value().assemble(one, helper.getLevel().registryAccess()))
                .orElse(ItemStack.EMPTY);
        helper.assertTrue(nugget.is(Items.GOLD_NUGGET), "a coin should melt to a gold nugget, not " + nugget);
        helper.succeed();
    }
}
