package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.BellowsBlock;
import me.moonscenty.alchemia.block.entity.BellowsBlockEntity;
import me.moonscenty.alchemia.crafting.InfusionRecipe;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The devices of step ten, and the parts they are built from.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class DeviceTests {
    private static final String TEMPLATE = "empty_32x40x32";

    private static void bellows(GameTestHelper helper, BlockPos at, Direction facing) {
        helper.setBlock(at, ModBlocks.BELLOWS.get().defaultBlockState().setValue(BellowsBlock.FACING, facing));
    }

    /** Bellows blowing on a furnace add a tick of cooking every second tick, so it cooks half again as fast. */
    @GameTest(template = TEMPLATE)
    public static void bellowsHurryAFurnace(GameTestHelper helper) {
        BlockPos furnaceAt = new BlockPos(2, 2, 2);
        helper.setBlock(furnaceAt, Blocks.FURNACE);
        BlockPos bellowsAt = furnaceAt.east();
        bellows(helper, bellowsAt, Direction.WEST);

        AbstractFurnaceBlockEntity furnace = (AbstractFurnaceBlockEntity) helper.getBlockEntity(furnaceAt);
        furnace.cookingTotalTime = 200;
        furnace.cookingProgress = 10;
        BellowsBlockEntity pumping = (BellowsBlockEntity) helper.getBlockEntity(bellowsAt);
        BlockState state = helper.getBlockState(bellowsAt);
        for (int tick = 0; tick < 10; tick++) {
            BellowsBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(bellowsAt), state, pumping);
        }
        helper.assertValueEqual(furnace.cookingProgress, 15, "ten ticks of blowing is five ticks of cooking");
        helper.succeed();
    }

    /** A furnace with nothing cooking in it is not started by being blown on, nor is the last tick taken from it. */
    @GameTest(template = TEMPLATE)
    public static void bellowsDoNotStartOrFinishTheWork(GameTestHelper helper) {
        BlockPos furnaceAt = new BlockPos(2, 2, 2);
        helper.setBlock(furnaceAt, Blocks.FURNACE);
        BlockPos bellowsAt = furnaceAt.east();
        bellows(helper, bellowsAt, Direction.WEST);
        AbstractFurnaceBlockEntity furnace = (AbstractFurnaceBlockEntity) helper.getBlockEntity(furnaceAt);
        BellowsBlockEntity pumping = (BellowsBlockEntity) helper.getBlockEntity(bellowsAt);
        BlockState state = helper.getBlockState(bellowsAt);

        furnace.cookingTotalTime = 200;
        furnace.cookingProgress = 0;
        for (int tick = 0; tick < 4; tick++) {
            BellowsBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(bellowsAt), state, pumping);
        }
        helper.assertValueEqual(furnace.cookingProgress, 0, "an idle furnace stays idle");

        furnace.cookingProgress = 199;
        for (int tick = 0; tick < 4; tick++) {
            BellowsBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(bellowsAt), state, pumping);
        }
        helper.assertValueEqual(furnace.cookingProgress, 199, "and the furnace finishes its own work");
        helper.succeed();
    }

    /** A signal holds them still. */
    @GameTest(template = TEMPLATE)
    public static void redstoneHoldsTheBellowsStill(GameTestHelper helper) {
        BlockPos bellowsAt = new BlockPos(2, 2, 2);
        bellows(helper, bellowsAt, Direction.WEST);
        helper.setBlock(bellowsAt.above(), Blocks.REDSTONE_BLOCK);
        helper.runAfterDelay(2, () -> {
            helper.assertBlockProperty(bellowsAt, BellowsBlock.ENABLED, false);
            helper.setBlock(bellowsAt.above(), Blocks.AIR);
            helper.runAfterDelay(2, () -> {
                helper.assertBlockProperty(bellowsAt, BellowsBlock.ENABLED, true);
                helper.succeed();
            });
        });
    }

    /** Only bellows facing the block, and pumping, are counted as blowing on it. */
    @GameTest(template = TEMPLATE)
    public static void onlyBellowsFacingInAreCounted(GameTestHelper helper) {
        BlockPos middle = new BlockPos(4, 2, 4);
        helper.setBlock(middle, Blocks.STONE);
        bellows(helper, middle.east(), Direction.WEST);
        bellows(helper, middle.west(), Direction.EAST);
        bellows(helper, middle.north(), Direction.NORTH);
        BlockPos at = helper.absolutePos(middle);
        helper.assertValueEqual(BellowsBlock.blowingOn(helper.getLevel(), at,
                Direction.EAST, Direction.WEST, Direction.NORTH), 2, "the one facing away does not count");
        helper.succeed();
    }

    /** The autocaster is built round the original's made mind again, with the resonator in its ring. */
    @GameTest(template = TEMPLATE)
    public static void theAutocasterIsBuiltOfItsOwnParts(GameTestHelper helper) {
        var recipe = helper.getLevel().getRecipeManager().byKey(Alchemia.id("autocaster")).orElseThrow();
        InfusionRecipe infusion = (InfusionRecipe) recipe.value();
        helper.assertTrue(infusion.central().test(new ItemStack(ModItems.BIOTHAUMIC_MIND.get())),
                "a biothaumic mind at the middle");
        helper.assertTrue(infusion.ring().stream().anyMatch(part ->
                part.test(new ItemStack(ModItems.MORPHIC_RESONATOR.get()))), "and a resonator round it");

        var crusher = (InfusionRecipe) helper.getLevel().getRecipeManager()
                .byKey(Alchemia.id("primal_crusher")).orElseThrow().value();
        helper.assertValueEqual(crusher.ring().stream().filter(part ->
                part.test(new ItemStack(ModItems.PRIMAL_CHARM.get()))).count(), 2L, "two charms in the crusher's");
        helper.succeed();
    }
}
