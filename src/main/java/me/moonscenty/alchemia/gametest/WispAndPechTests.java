package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspects;
import me.moonscenty.alchemia.entity.Wisp;
import me.moonscenty.alchemia.item.WispyEssenceItem;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The wisp and the pech of step eleven.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class WispAndPechTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final BlockPos MIDDLE = new BlockPos(8, 6, 8);

    /** A wisp that has drifted a tick is a wisp of something. */
    @GameTest(template = TEMPLATE)
    public static void aWispTakesAnAspect(GameTestHelper helper) {
        Wisp wisp = helper.spawn(ModEntities.WISP.get(), MIDDLE);
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(wisp.aspect() != null, "it has an aspect");
            helper.succeed();
        });
    }

    /** A wisp leaves two points of its aspect as wispy essence, and is its aspect twice over to a scan. */
    @GameTest(template = TEMPLATE)
    public static void aWispLeavesItsEssence(GameTestHelper helper) {
        Wisp wisp = helper.spawn(ModEntities.WISP.get(), MIDDLE);
        wisp.setAspect(ModAspects.FIRE);
        var scanned = Aspects.of(wisp);
        helper.assertValueEqual(scanned.get(ModAspects.FIRE), 2, "two of fire to a scan");
        helper.assertValueEqual(scanned.get(ModAspects.AURA), 1, "and a little auram");
        wisp.kill();
        var left = helper.getLevel().getEntitiesOfClass(ItemEntity.class, helper.getBounds().inflate(4)).stream()
                .map(ItemEntity::getItem).filter(stack -> stack.is(ModItems.WISPY_ESSENCE.get())).findFirst();
        helper.assertTrue(left.isPresent(), "it left its essence");
        helper.assertTrue(WispyEssenceItem.inside(left.get()).filter(aspect -> aspect.is(ModAspects.FIRE.getKey()))
                .isPresent(), "of fire");
        var worth = Aspects.of(left.get());
        helper.assertValueEqual(worth.get(ModAspects.FIRE), 2, "worth two of fire");
        helper.assertValueEqual(worth.get(ModAspects.AURA), 1, "and a breath of auram");
        helper.succeed();
    }

    /** Taken against something standing still, a wisp zaps it before long. */
    @GameTest(template = TEMPLATE, timeoutTicks = 300)
    public static void aWispZapsWhatItTakesAgainst(GameTestHelper helper) {
        Wisp wisp = helper.spawn(ModEntities.WISP.get(), MIDDLE);
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, MIDDLE.offset(3, -4, 0));
        wisp.hurt(helper.getLevel().damageSources().mobAttack(pig), 0.0F);
        float full = pig.getHealth();
        helper.runAfterDelay(200, () -> {
            helper.assertTrue(pig.getHealth() < full, "the pig has been zapped");
            helper.succeed();
        });
    }

    /** A chunk thick with flux can let out a wisp, five blocks over the ground. */
    @GameTest(template = TEMPLATE)
    public static void fluxLetsOutAWisp(GameTestHelper helper) {
        BlockPos ground = helper.absolutePos(new BlockPos(4, 1, 4));
        helper.assertTrue(Wisp.fromFlux(helper.getLevel(), ground, RandomSource.create(1L)), "one came");
        var found = helper.getLevel().getEntitiesOfClass(Wisp.class, helper.getBounds().inflate(4));
        helper.assertTrue(found.size() == 1 && Math.abs(found.getFirst().getY() - (ground.getY() + 5)) < 0.01,
                "five blocks up");
        helper.succeed();
    }

    private static me.moonscenty.alchemia.entity.Pech pech(GameTestHelper helper, BlockPos at) {
        helper.setBlock(at.below(), net.minecraft.world.level.block.Blocks.STONE);
        return helper.spawnWithNoFreeWill(ModEntities.PECH.get(), at);
    }

    /** Offered what it prizes, a wild pech eats it and may take to you; what else it finds goes in its pack. */
    @GameTest(template = TEMPLATE)
    public static void aPechIsWonOverWithEmeralds(GameTestHelper helper) {
        var pech = pech(helper, new BlockPos(4, 2, 4));
        helper.assertTrue(pech.wants(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.EMERALD)),
                "a wild pech wants an emerald");
        int offered = 0;
        while (!pech.isTamed() && offered < 60) {
            var emeralds = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.EMERALD, 3);
            var left = pech.take(emeralds);
            helper.assertValueEqual(left.getCount(), 2, "it eats one at a time");
            offered++;
        }
        helper.assertTrue(pech.isTamed(), "and in the end takes to whoever keeps feeding it");
        var dirt = pech.take(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIRT, 10));
        helper.assertTrue(dirt.isEmpty(), "dirt goes in its pack");
        helper.assertTrue(pech.pack().stream().anyMatch(stack -> stack.is(net.minecraft.world.item.Items.DIRT)
                && stack.getCount() == 10), "all ten");
        helper.succeed();
    }

    /** Hit one pech, and the others about it come for you too. */
    @GameTest(template = TEMPLATE)
    public static void hittingAPechAngersTheOthers(GameTestHelper helper) {
        var struck = pech(helper, new BlockPos(4, 2, 4));
        var fellow = pech(helper, new BlockPos(8, 2, 8));
        fellow.setTamed(true);
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        struck.hurt(helper.getLevel().damageSources().playerAttack(player), 1.0F);
        helper.assertTrue(struck.getAnger() > 0 && fellow.getAnger() > 0, "both are angry");
        helper.assertFalse(fellow.isTamed(), "and no friend of anybody's");
        helper.succeed();
    }

    /** A tamed pech pays out for an emerald from its own list. */
    @GameTest(template = TEMPLATE)
    public static void aPechTradesForAnEmerald(GameTestHelper helper) {
        var pech = pech(helper, new BlockPos(4, 2, 4));
        pech.setTamed(true);
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.moveTo(pech.position());
        var menu = new me.moonscenty.alchemia.menu.PechMenu(1, player.getInventory(), pech);
        menu.getSlot(me.moonscenty.alchemia.menu.PechMenu.OFFER).set(
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.EMERALD, 2));
        helper.assertTrue(menu.canRoll(), "an emerald can be rolled for");
        menu.clickMenuButton(player, me.moonscenty.alchemia.menu.PechMenu.ROLL);
        helper.assertValueEqual(menu.getSlot(me.moonscenty.alchemia.menu.PechMenu.OFFER).getItem().getCount(), 1,
                "one emerald was spent");
        boolean paid = false;
        for (int slot = 1; slot <= me.moonscenty.alchemia.menu.PechMenu.GIVEN; slot++) {
            paid |= menu.getSlot(slot).hasItem();
        }
        helper.assertTrue(paid, "and something came back");
        helper.assertFalse(menu.canRoll(), "and nothing more until that is taken");
        helper.succeed();
    }

    /** Every kind of pech has something for every worth, from its own list or the nearest worth on it. */
    @GameTest(template = TEMPLATE)
    public static void everyLotHasSomething(GameTestHelper helper) {
        RandomSource random = RandomSource.create(4L);
        for (int kind = 0; kind < 3; kind++) {
            for (int worth = 1; worth <= 5; worth++) {
                helper.assertFalse(me.moonscenty.alchemia.entity.PechTrades.lot(helper.getLevel(), kind, worth, random)
                        .isEmpty(), "kind " + kind + " worth " + worth);
            }
        }
        helper.succeed();
    }

    /** A pech dies leaving coins where its pack was empty, and is its kind to a scan. */
    @GameTest(template = TEMPLATE)
    public static void aPechLeavesCoins(GameTestHelper helper) {
        var pech = pech(helper, new BlockPos(4, 2, 4));
        pech.setKind(me.moonscenty.alchemia.entity.Pech.MAGE);
        helper.assertValueEqual(Aspects.of(pech).get(ModAspects.AVERSION), 2, "a mage wards off");
        helper.assertValueEqual(Aspects.of(pech).get(ModAspects.MAN), 2, "as well as being a pech");
        pech.kill();
        long coins = helper.getLevel().getEntitiesOfClass(ItemEntity.class, helper.getBounds().inflate(4)).stream()
                .filter(item -> item.getItem().is(ModItems.COIN.get())).count();
        helper.assertTrue(coins > 0, "it left coins");
        helper.succeed();
    }
}
