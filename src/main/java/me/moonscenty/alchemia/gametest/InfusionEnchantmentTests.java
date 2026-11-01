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

@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class InfusionEnchantmentTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final BlockPos HERE = new BlockPos(8, 2, 8);

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

    @GameTest(template = TEMPLATE)
    public static void aBurrowingPickaxeTakesTheFarEndOfASeam(GameTestHelper helper) {
        helper.setBlock(HERE, Blocks.IRON_ORE);
        helper.setBlock(HERE.east(), Blocks.IRON_ORE);
        helper.setBlock(HERE.east(2), Blocks.STONE);

        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                InfusionEnchantments.raised(new ItemStack(Items.NETHERITE_PICKAXE), InfusionEnchantment.BURROWING));
        player.setPos(helper.absoluteVec(HERE.above(2).getCenter()));

        // fire the BreakEvent instead of breaking: burrowing cancels the event and spends the swing elsewhere
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

    private static net.minecraft.world.entity.monster.Zombie plainZombie(GameTestHelper helper, BlockPos at) {
        var zombie = net.minecraft.world.entity.EntityType.ZOMBIE.create(helper.getLevel());
        zombie.setData(me.moonscenty.alchemia.player.ModAttachments.CHAMPION,
                me.moonscenty.alchemia.entity.Champions.ORDINARY);
        net.minecraft.world.phys.Vec3 where = helper.absoluteVec(at.getBottomCenter());
        zombie.moveTo(where.x, where.y, where.z, 0.0F, 0.0F);
        helper.getLevel().addFreshEntity(zombie);
        return zombie;
    }

    @GameTest(template = TEMPLATE)
    public static void anArcingSwordReachesPastWhatItStruck(GameTestHelper helper) {
        var struck = plainZombie(helper, HERE);
        var beside = plainZombie(helper, HERE.east());
        var far = plainZombie(helper, HERE.east(8));
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
