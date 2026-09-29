package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.enchantment.InfusionEnchantment;
import me.moonscenty.alchemia.enchantment.InfusionEnchantments;
import me.moonscenty.alchemia.registry.ModItems;
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

    /**
     * A burrowing pickaxe brings a seam apart from its far end.
     * <p>
     * The block struck is left standing and the furthest one joined to it goes instead, which is what makes a
     * trunk fall on its own rather than be climbed. The stone round the seam is not touched: this follows what
     * is joined, not what is near.
     */
    @GameTest(template = TEMPLATE)
    public static void aBurrowingPickaxeTakesTheFarEndOfASeam(GameTestHelper helper) {
        helper.setBlock(HERE, Blocks.IRON_ORE);
        helper.setBlock(HERE.east(), Blocks.IRON_ORE);
        helper.setBlock(HERE.east(2), Blocks.STONE);

        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                InfusionEnchantments.raised(new ItemStack(Items.NETHERITE_PICKAXE), InfusionEnchantment.BURROWING));
        player.setPos(helper.absoluteVec(HERE.above(2).getCenter()));

        // the break has to be announced rather than simply done: what a burrowing tool does is to call the
        // swing off and spend it elsewhere, and only the announcement can be called off
        BlockPos at = helper.absolutePos(HERE);
        var swing = new net.neoforged.neoforge.event.level.BlockEvent.BreakEvent(
                helper.getLevel(), at, helper.getLevel().getBlockState(at), player);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(swing);

        helper.assertTrue(swing.isCanceled(), "the swing was spent on the far end instead");
        helper.assertBlockPresent(Blocks.IRON_ORE, HERE);
        helper.assertBlockPresent(Blocks.AIR, HERE.east());
        helper.assertBlockPresent(Blocks.STONE, HERE.east(2));
        helper.succeed();
    }

    /** An arcing sword carries the blow to as many others as it has been taken steps, and no further. */
    @GameTest(template = TEMPLATE)
    public static void anArcingSwordReachesPastWhatItStruck(GameTestHelper helper) {
        var struck = helper.spawn(net.minecraft.world.entity.EntityType.ZOMBIE, HERE);
        var beside = helper.spawn(net.minecraft.world.entity.EntityType.ZOMBIE, HERE.east());
        var far = helper.spawn(net.minecraft.world.entity.EntityType.ZOMBIE, HERE.east(8));
        float whole = beside.getHealth();

        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                InfusionEnchantments.raised(new ItemStack(Items.NETHERITE_SWORD), InfusionEnchantment.ARCING));
        player.setPos(helper.absoluteVec(HERE.getCenter()));
        net.neoforged.neoforge.common.CommonHooks.onPlayerAttackTarget(player, struck);

        helper.assertTrue(beside.getHealth() < whole, "the one standing beside it took the blow too");
        helper.assertValueEqual(far.getHealth(), whole, "the one across the room did not");
        helper.succeed();
    }

    /**
     * An essence weapon takes a little of what it killed, and only what that thing was made of.
     * <p>
     * Taken to its fifth step it takes something every time, which is what makes this testable at all; at one
     * step it is one time in five and a test that asserted a drop would be wrong four times in five.
     */
    @GameTest(template = TEMPLATE)
    public static void anEssenceWeaponTakesWhatItKilled(GameTestHelper helper) {
        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
        for (int step = 0; step < InfusionEnchantment.ESSENCE.most(); step++) {
            sword = InfusionEnchantments.raised(sword, InfusionEnchantment.ESSENCE);
        }
        helper.assertValueEqual(InfusionEnchantments.level(sword, InfusionEnchantment.ESSENCE),
                InfusionEnchantment.ESSENCE.most(), "the sword is worked as far as it goes");

        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, sword);
        var zombie = helper.spawn(net.minecraft.world.entity.EntityType.ZOMBIE, HERE);
        player.setPos(zombie.position());

        var drops = new java.util.ArrayList<net.minecraft.world.entity.item.ItemEntity>();
        net.neoforged.neoforge.common.CommonHooks.onLivingDrops(zombie,
                helper.getLevel().damageSources().playerAttack(player), drops, true);

        helper.assertTrue(drops.stream().anyMatch(
                        dropped -> dropped.getItem().is(ModItems.CRYSTALLIZED_ESSENCE.get())),
                "it gave up a crystal of what it was made of");
        drops.stream().filter(dropped -> dropped.getItem().is(ModItems.CRYSTALLIZED_ESSENCE.get())).forEach(dropped ->
                helper.assertTrue(
                        me.moonscenty.alchemia.item.CrystallizedEssenceItem.inside(dropped.getItem()).isPresent(),
                        "and the crystal has something in it"));
        helper.succeed();
    }
}
