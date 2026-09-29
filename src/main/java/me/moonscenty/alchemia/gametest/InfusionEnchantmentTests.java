package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.enchantment.InfusionEnchantment;
import me.moonscenty.alchemia.enchantment.InfusionEnchantments;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * What an altar puts on a tool, and what the tool then does.
 * <p>
 * These are the only enchantments in the mod that a table cannot sell, so the rules about what they go on and how
 * far they go are the whole of what keeps them from being a better enchanting table.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class InfusionEnchantmentTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final BlockPos HERE = new BlockPos(8, 2, 8);

    /** A working goes on the tools it was meant for, and on nothing else. */
    @GameTest(template = TEMPLATE)
    public static void aWorkingGoesOnlyOnWhatItWasMeantFor(GameTestHelper helper) {
        helper.assertTrue(InfusionEnchantment.DESTRUCTIVE.fits(new ItemStack(Items.IRON_PICKAXE)),
                "a pickaxe takes destructive");
        helper.assertFalse(InfusionEnchantment.DESTRUCTIVE.fits(new ItemStack(Items.IRON_SWORD)),
                "a sword does not: there is nothing for it to break");
        helper.assertTrue(InfusionEnchantment.COLLECTOR.fits(new ItemStack(Items.IRON_SWORD)),
                "collector does go on a sword");
        helper.assertFalse(InfusionEnchantment.COLLECTOR.fits(new ItemStack(Items.BREAD)),
                "and on nothing that is not a tool at all");
        helper.succeed();
    }

    /** The same working twice is the same tool, not a deeper one, once it has been taken as far as it goes. */
    @GameTest(template = TEMPLATE)
    public static void anAltarWillNotSellTheSameStepTwice(GameTestHelper helper) {
        ItemStack plain = new ItemStack(Items.IRON_PICKAXE);
        helper.assertTrue(InfusionEnchantments.roomFor(plain, InfusionEnchantment.DESTRUCTIVE),
                "a plain pickaxe has room for it");

        ItemStack worked = InfusionEnchantments.raised(plain, InfusionEnchantment.DESTRUCTIVE);
        helper.assertValueEqual(InfusionEnchantments.level(worked, InfusionEnchantment.DESTRUCTIVE), 1,
                "one working puts it on");
        helper.assertFalse(InfusionEnchantments.roomFor(worked, InfusionEnchantment.DESTRUCTIVE),
                "and there is nothing left to do for it");
        helper.assertValueEqual(
                InfusionEnchantments.level(InfusionEnchantments.raised(worked, InfusionEnchantment.DESTRUCTIVE),
                        InfusionEnchantment.DESTRUCTIVE), 1,
                "working it again leaves it where it was");

        helper.assertValueEqual(InfusionEnchantments.level(plain, InfusionEnchantment.DESTRUCTIVE), 0,
                "and the tool it was worked from is untouched");
        helper.succeed();
    }

    /**
     * A destructive pickaxe takes the eight stones round the one struck, and leaves what it is no good for.
     * <p>
     * The second half is what keeps it from being a bomb: the log standing in the middle of the stone is still
     * standing afterwards, because a pickaxe was never going to fell it.
     */
    @GameTest(template = TEMPLATE)
    public static void aDestructivePickaxeTakesTheEightRoundIt(GameTestHelper helper) {
        for (int east = -1; east <= 1; east++) {
            for (int south = -1; south <= 1; south++) {
                helper.setBlock(HERE.offset(east, 0, south), Blocks.STONE);
            }
        }
        helper.setBlock(HERE.offset(1, 0, 1), Blocks.OAK_LOG);

        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                InfusionEnchantments.raised(new ItemStack(Items.NETHERITE_PICKAXE), InfusionEnchantment.DESTRUCTIVE));
        player.setPos(helper.absoluteVec(HERE.above(2).getCenter()));

        helper.getLevel().destroyBlock(helper.absolutePos(HERE), true, player);

        helper.assertBlockPresent(Blocks.AIR, HERE);
        helper.assertBlockPresent(Blocks.AIR, HERE.offset(-1, 0, 0));
        helper.assertBlockPresent(Blocks.AIR, HERE.offset(0, 0, -1));
        helper.assertBlockPresent(Blocks.OAK_LOG, HERE.offset(1, 0, 1));
        helper.succeed();
    }

    /** Crouching turns both of them off, which is the only say a player has over either. */
    @GameTest(template = TEMPLATE)
    public static void crouchingTurnsThemOff(GameTestHelper helper) {
        for (int east = -1; east <= 1; east++) {
            for (int south = -1; south <= 1; south++) {
                helper.setBlock(HERE.offset(east, 0, south), Blocks.STONE);
            }
        }

        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                InfusionEnchantments.raised(new ItemStack(Items.NETHERITE_PICKAXE), InfusionEnchantment.DESTRUCTIVE));
        player.setPos(helper.absoluteVec(HERE.above(2).getCenter()));
        player.setShiftKeyDown(true);

        helper.getLevel().destroyBlock(helper.absolutePos(HERE), true, player);

        helper.assertBlockPresent(Blocks.AIR, HERE);
        helper.assertBlockPresent(Blocks.STONE, HERE.offset(-1, 0, 0));
        helper.succeed();
    }
}
