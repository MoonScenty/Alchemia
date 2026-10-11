package me.moonscenty.alchemia.gametest;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aura.AuraGeneration;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.block.entity.FocalManipulatorBlockEntity;
import me.moonscenty.alchemia.item.VisHolder;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModWandParts;
import me.moonscenty.alchemia.wand.Casting;
import me.moonscenty.alchemia.wand.Focus;
import me.moonscenty.alchemia.wand.FocusUpgrade;
import me.moonscenty.alchemia.wand.FocusUpgrades;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * What can be worked into a focus, what it costs, and what the focus becomes afterwards.
 *
 * <p>The table of which focus offers what at which rank is the original's, so most of what is worth checking is
 * that it has been copied straight and that nothing in it is unreachable. The rest is the arithmetic: a rank is
 * dearer than the one before it, a price is always quoted in primals, and thrift is a discount on a working and
 * on nothing else.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class FocusUpgradeTests {
    private static final String TEMPLATE = "empty_32x40x32";

    private static ItemStack focus(String kind) {
        return new ItemStack(ModItems.FOCI.get(kind).get());
    }

    /** Every one of the twenty-three is offered by some focus at some rank, or it may as well not exist. */
    @GameTest(template = TEMPLATE)
    public static void everyUpgradeIsReachable(GameTestHelper helper) {
        Set<FocusUpgrade> found = EnumSet.noneOf(FocusUpgrade.class);
        for (String kind : ModItems.FOCI.keySet()) {
            for (int rank = 1; rank <= FocusUpgrades.RANKS; rank++) {
                found.addAll(FocusUpgrades.offers(focus(kind), rank));
            }
        }
        for (FocusUpgrade upgrade : FocusUpgrade.values()) {
            helper.assertTrue(found.contains(upgrade), upgrade.getSerializedName() + " is offered somewhere");
        }
        helper.succeed();
    }

    /** The deep ranks are where a focus is turned into something else, as the original had it. */
    @GameTest(template = TEMPLATE)
    public static void theTableIsTheOriginals(GameTestHelper helper) {
        helper.assertTrue(FocusUpgrades.offers(focus("fire"), 3).contains(FocusUpgrade.FIREBALL),
                "fire offers a fireball at the third rank");
        helper.assertFalse(FocusUpgrades.offers(focus("fire"), 1).contains(FocusUpgrade.FIREBALL),
                "and nowhere shallower");
        helper.assertTrue(FocusUpgrades.offers(focus("primal"), 1).equals(List.of(FocusUpgrade.FRUGAL)),
                "the primal focus will only ever be made cheaper");
        helper.assertTrue(FocusUpgrades.offers(focus("hole"), 1).contains(FocusUpgrade.EXTEND),
                "a hole can be made to stay open longer from the first rank");
        helper.assertTrue(FocusUpgrades.offers(focus("shock"), 0).isEmpty(), "there is no nought-th rank");
        helper.assertTrue(FocusUpgrades.offers(focus("shock"), 6).isEmpty(), "nor a sixth");
        helper.succeed();
    }

    /** Ranks fill in order, and a full focus takes nothing more. */
    @GameTest(template = TEMPLATE)
    public static void fiveGoInAndNoMore(GameTestHelper helper) {
        ItemStack focus = focus("shock");
        helper.assertValueEqual(FocusUpgrades.nextRank(focus), 1, "an untouched focus starts at the first rank");

        for (int rank = 1; rank <= FocusUpgrades.RANKS; rank++) {
            helper.assertValueEqual(FocusUpgrades.nextRank(focus), rank, "the ranks fill in order");
            FocusUpgrades.apply(focus, FocusUpgrade.FRUGAL);
        }
        helper.assertValueEqual(FocusUpgrades.level(focus, FocusUpgrade.FRUGAL), 5, "five ranks of thrift");
        helper.assertValueEqual(FocusUpgrades.nextRank(focus), 0, "and no room for a sixth");
        helper.assertTrue(FocusUpgrades.offers(focus, 0).isEmpty(), "so nothing is offered");

        FocusUpgrades.apply(focus, FocusUpgrade.POTENCY);
        helper.assertValueEqual(FocusUpgrades.on(focus).size(), 5, "a full focus is left alone");
        helper.succeed();
    }

    /** Every rank is half as dear again as the one before, and the bill is always in primals. */
    @GameTest(template = TEMPLATE)
    public static void aDeeperRankCostsMore(GameTestHelper helper) {
        AspectList first = FocusUpgrades.price(FocusUpgrade.FRUGAL, 1);
        AspectList third = FocusUpgrades.price(FocusUpgrade.FRUGAL, 3);
        helper.assertTrue(third.total() > first.total(), "the third rank is dearer than the first");
        for (Holder<Aspect> aspect : third.sortedByName()) {
            helper.assertTrue(aspect.value().isPrimal(), "a bill is paid in primals: " + aspect.value().tag());
        }
        helper.assertValueEqual(FocusUpgrades.levels(1), 6, "the first rank is six levels");
        helper.assertValueEqual(FocusUpgrades.levels(5), 30, "and the fifth is thirty");
        helper.succeed();
    }

    /** A compound asks for as much of each of its two halves, all the way down to the six. */
    @GameTest(template = TEMPLATE)
    public static void aPriceBreaksDownIntoPrimals(GameTestHelper helper) {
        // desire is soul and void; soul is life and death; void is air and entropy
        AspectList broken = AspectList.of(ModAspects.DESIRE, 100).toPrimals();
        helper.assertValueEqual(broken.get(ModAspects.AIR), 100, "a hundred air out of the void in it");
        helper.assertValueEqual(broken.get(ModAspects.WATER), 200, "and water from both life and death");
        helper.assertTrue(broken.get(ModAspects.DESIRE) == 0, "and nothing of what was asked for");
        helper.succeed();
    }

    /** A fire focus with a fireball in it is a different focus, and the price and the wait say so. */
    @GameTest(template = TEMPLATE)
    public static void anUpgradeCanChangeTheFocusOutright(GameTestHelper helper) {
        ItemStack focus = focus("fire");
        Focus plain = FocusUpgrades.tuned(focus);
        helper.assertTrue(plain.perTick(), "a spray of fire is charged by the tick");
        helper.assertValueEqual(plain.turret().range(), 12.0F, "and reaches twelve");

        FocusUpgrades.apply(focus, FocusUpgrade.FIREBALL);
        Focus thrown = FocusUpgrades.tuned(focus);
        helper.assertFalse(thrown.perTick(), "a fireball is charged when it is thrown");
        helper.assertValueEqual(thrown.cooldown(), 1000, "and waits a second afterwards");
        helper.assertValueEqual(thrown.cost().get(ModAspects.FIRE), 8, "and costs eight fire");
        helper.assertValueEqual(thrown.cost().get(ModAspects.ENTROPY), 8, "and eight entropy");
        helper.assertValueEqual(thrown.turret().range(), 32.0F, "and reaches the whole way");
        helper.succeed();
    }

    /** Digging wider costs more earth, and taking the block up whole costs one of everything else. */
    @GameTest(template = TEMPLATE)
    public static void excavationPaysForWhatIsAskedOfIt(GameTestHelper helper) {
        ItemStack focus = focus("excavation");
        helper.assertValueEqual(FocusUpgrades.tuned(focus).cost().get(ModAspects.EARTH), 1, "a penny a block");

        FocusUpgrades.apply(focus, FocusUpgrade.POTENCY);
        FocusUpgrades.apply(focus, FocusUpgrade.ENLARGE);
        helper.assertValueEqual(FocusUpgrades.tuned(focus).cost().get(ModAspects.EARTH), 2, "wider costs more");

        FocusUpgrades.apply(focus, FocusUpgrade.TREASURE);
        FocusUpgrades.apply(focus, FocusUpgrade.ENLARGE);
        FocusUpgrades.apply(focus, FocusUpgrade.SILK_TOUCH);
        AspectList whole = FocusUpgrades.tuned(focus).cost();
        helper.assertValueEqual(whole.get(ModAspects.EARTH), 3, "wider again");
        helper.assertValueEqual(whole.get(ModAspects.ORDER), 1, "and one of each of the other five besides");
        helper.succeed();
    }

    /** Thrift pays for the focus's own working. A bench borrowing the wand's vis pays the full price. */
    @GameTest(template = TEMPLATE)
    public static void thriftOnlyPaysForTheWorking(GameTestHelper helper) {
        Player holder = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wand = WandItem.of(ModWandParts.SILVERWOOD, ModWandParts.GOLD);
        ItemStack focus = focus("shock");
        FocusUpgrades.apply(focus, FocusUpgrade.FRUGAL);
        FocusUpgrades.apply(focus, FocusUpgrade.FRUGAL);
        WandItem.setFocus(wand, focus);

        AspectList filled = AspectList.EMPTY;
        for (Holder<Aspect> primal : ModAspects.primals()) {
            filled = filled.add(primal, 100 * WandItem.FINE);
        }
        wand.set(ModDataComponents.VIS.get(), filled);
        WandItem item = (WandItem) wand.getItem();

        AspectList price = AspectList.of(ModAspects.AIR, 10);
        int before = item.held(wand, ModAspects.AIR);
        item.take(wand, price, holder, VisHolder.Spending.WORKING);
        int working = before - item.held(wand, ModAspects.AIR);

        wand.set(ModDataComponents.VIS.get(), filled);
        item.take(wand, price, holder, VisHolder.Spending.CRAFTING);
        int crafting = before - item.held(wand, ModAspects.AIR);

        helper.assertTrue(working < crafting, "two ranks of thrift take a fifth off a working");
        helper.succeed();
    }

    /** The whole of it: a focus on the bench, levels paid, vis drawn, and one more thing worked in. */
    @GameTest(template = TEMPLATE, timeoutTicks = 600)
    public static void theBenchWorksOneThingIn(GameTestHelper helper) {
        BlockPos at = new BlockPos(2, 2, 2);
        FocalManipulatorBlockEntity bench = bench(helper, at);
        bench.setItem(0, focus("shock"));
        Player smith = helper.makeMockPlayer(GameType.CREATIVE);

        helper.assertTrue(bench.start(FocusUpgrade.FRUGAL, smith), "the bench took the work on");
        helper.assertTrue(bench.bill() > 0, "and there is a bill to pay");
        helper.runAfterDelay(500, () -> {
            helper.assertValueEqual(FocusUpgrades.level(bench.getItem(0), FocusUpgrade.FRUGAL), 1,
                    "thrift was worked into it");
            helper.assertValueEqual(bench.bill(), 0, "and the bench is idle again");
            helper.succeed();
        });
    }

    /** It will not take on work the focus does not offer, nor a second piece while it is busy. */
    @GameTest(template = TEMPLATE)
    public static void theBenchRefusesWhatTheFocusWillNotTake(GameTestHelper helper) {
        FocalManipulatorBlockEntity bench = bench(helper, new BlockPos(2, 2, 2));
        Player smith = helper.makeMockPlayer(GameType.CREATIVE);

        helper.assertFalse(bench.start(FocusUpgrade.FRUGAL, smith), "an empty bench does nothing");
        bench.setItem(0, focus("primal"));
        helper.assertFalse(bench.start(FocusUpgrade.POTENCY, smith),
                "a primal focus is never offered more force");
        helper.assertTrue(bench.start(FocusUpgrade.FRUGAL, smith), "only thrift");
        helper.assertFalse(bench.start(FocusUpgrade.FRUGAL, smith), "and not twice at once");
        helper.succeed();
    }

    /** Taking the focus off the bench part-way loses the work, which is the only honest answer. */
    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void takingTheFocusOffLosesTheWork(GameTestHelper helper) {
        FocalManipulatorBlockEntity bench = bench(helper, new BlockPos(2, 2, 2));
        bench.setItem(0, focus("shock"));
        Player smith = helper.makeMockPlayer(GameType.CREATIVE);
        helper.assertTrue(bench.start(FocusUpgrade.FRUGAL, smith), "it started");

        ItemStack taken = bench.removeItemNoUpdate(0);
        helper.assertValueEqual(bench.bill(), 0, "and gave up when the focus went");
        helper.assertTrue(FocusUpgrades.on(taken).isEmpty(), "with nothing written on it");
        helper.succeed();
    }

    /** A bench with the air round it topped up, since a rank costs a good deal more than a chunk holds. */
    private static FocalManipulatorBlockEntity bench(GameTestHelper helper, BlockPos at) {
        helper.setBlock(at, ModBlocks.FOCAL_MANIPULATOR.get());
        BlockPos here = helper.absolutePos(at);
        AuraGeneration.ensure(helper.getLevel(), helper.getLevel().getChunkAt(here), RandomSource.create(11L));
        for (Holder<Aspect> primal : ModAspects.primals()) {
            AuraHandler.add(helper.getLevel(), here, primal, 2000);
        }
        return (FocalManipulatorBlockEntity) helper.getLevel().getBlockEntity(here);
    }

    /** A wand with an upgraded focus on it: what the spells themselves read when they go off. */
    private static ItemStack wand(String kind, FocusUpgrade... worked) {
        ItemStack wand = WandItem.of(ModWandParts.SILVERWOOD, ModWandParts.GOLD);
        ItemStack focus = focus(kind);
        for (FocusUpgrade upgrade : worked) {
            FocusUpgrades.apply(focus, upgrade);
        }
        WandItem.setFocus(wand, focus);
        AspectList filled = AspectList.EMPTY;
        for (Holder<Aspect> primal : ModAspects.primals()) {
            filled = filled.add(primal, 100 * WandItem.FINE);
        }
        wand.set(ModDataComponents.VIS.get(), filled);
        return wand;
    }

    /** Force makes a bolt hurt more, which is the plainest thing any of the twenty-three does. */
    @GameTest(template = TEMPLATE)
    public static void forceMakesABoltHurtMore(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.SURVIVAL);
        LivingEntity plain = helper.spawn(EntityType.PIG, new BlockPos(2, 2, 2));
        LivingEntity forced = helper.spawn(EntityType.PIG, new BlockPos(4, 2, 2));
        float whole = plain.getHealth();

        me.moonscenty.alchemia.wand.spell.Shock.cast(helper.getLevel(), caster, wand("shock"),
                new net.minecraft.world.phys.EntityHitResult(plain), 0);
        me.moonscenty.alchemia.wand.spell.Shock.cast(helper.getLevel(), caster,
                wand("shock", FocusUpgrade.POTENCY, FocusUpgrade.POTENCY),
                new net.minecraft.world.phys.EntityHitResult(forced), 0);

        helper.assertTrue(plain.getHealth() < whole, "the plain bolt landed");
        helper.assertTrue(forced.getHealth() < plain.getHealth(), "and the forced one landed harder");
        helper.succeed();
    }

    /** Chaining carries the bolt on to whatever is standing beside what it struck. */
    @GameTest(template = TEMPLATE)
    public static void chainingJumpsToWhatIsStandingNear(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.SURVIVAL);
        LivingEntity first = helper.spawn(EntityType.PIG, new BlockPos(3, 2, 3));
        LivingEntity beside = helper.spawn(EntityType.PIG, new BlockPos(4, 2, 3));
        float whole = beside.getHealth();

        me.moonscenty.alchemia.wand.spell.Shock.cast(helper.getLevel(), caster,
                wand("shock", FocusUpgrade.CHAIN_LIGHTNING),
                new net.minecraft.world.phys.EntityHitResult(first), 0);

        helper.assertTrue(beside.getHealth() < whole, "the bolt jumped to the one standing beside it");
        helper.succeed();
    }

    /** Earth shock throws a ball at the floor rather than a bolt at a face, so it needs no mark. */
    @GameTest(template = TEMPLATE)
    public static void earthShockNeedsNothingToAimAt(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertFalse(me.moonscenty.alchemia.wand.spell.Shock.cast(helper.getLevel(), caster,
                        wand("shock"), net.minecraft.world.phys.BlockHitResult.miss(
                                net.minecraft.world.phys.Vec3.ZERO, Direction.UP, BlockPos.ZERO), 0),
                "a plain bolt wants something to hit");
        helper.assertTrue(me.moonscenty.alchemia.wand.spell.Shock.cast(helper.getLevel(), caster,
                        wand("shock", FocusUpgrade.EARTH_SHOCK), net.minecraft.world.phys.BlockHitResult.miss(
                                net.minecraft.world.phys.Vec3.ZERO, Direction.UP, BlockPos.ZERO), 0),
                "a ball only wants a floor");
        helper.succeed();
    }

    /** A widened builder lays a longer row, and an architect's eye lets that be turned down again. */
    @GameTest(template = TEMPLATE)
    public static void anArchitectSetsHowFarItWorks(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.CREATIVE);
        ItemStack wand = wand("builder", FocusUpgrade.ARCHITECT);
        ItemStack focus = WandItem.focus(wand);
        helper.assertTrue(focus.get(ModDataComponents.FOCUS_REACH.get()) == null, "it starts at its widest");

        me.moonscenty.alchemia.wand.spell.Swapping.setReach(caster, wand);
        Integer first = WandItem.focus(wand).get(ModDataComponents.FOCUS_REACH.get());
        helper.assertTrue(first != null && first == 16, "the first press sets it to the most it can do");
        me.moonscenty.alchemia.wand.spell.Swapping.setReach(caster, wand);
        helper.assertValueEqual(WandItem.focus(wand).get(ModDataComponents.FOCUS_REACH.get()), 15,
                "and every press after that turns it down one");
        helper.succeed();
    }

    /** A focus with no architect's eye in it does not listen to the key at all. */
    @GameTest(template = TEMPLATE)
    public static void withoutAnArchitectTheKeyDoesNothing(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.CREATIVE);
        ItemStack wand = wand("builder");
        me.moonscenty.alchemia.wand.spell.Swapping.setReach(caster, wand);
        helper.assertTrue(WandItem.focus(wand).get(ModDataComponents.FOCUS_REACH.get()) == null,
                "nothing was set");
        helper.succeed();
    }

    /** Silk touch on a digging focus takes the block up as it stood. */
    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void silkTouchTakesTheBlockUpWhole(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos at = new BlockPos(2, 2, 2);
        helper.setBlock(at, net.minecraft.world.level.block.Blocks.STONE);
        ItemStack wand = wand("excavation", FocusUpgrade.POTENCY, FocusUpgrade.POTENCY,
                FocusUpgrade.POTENCY, FocusUpgrade.POTENCY, FocusUpgrade.SILK_TOUCH);

        net.minecraft.world.phys.BlockHitResult struck = new net.minecraft.world.phys.BlockHitResult(
                net.minecraft.world.phys.Vec3.ZERO, Direction.UP, helper.absolutePos(at), false);
        for (int tick = 0; tick < 20; tick++) {
            me.moonscenty.alchemia.wand.spell.Excavation.cast(helper.getLevel(), caster, wand, struck, tick);
        }
        helper.runAfterDelay(5, () -> {
            helper.assertBlockPresent(net.minecraft.world.level.block.Blocks.AIR, at);
            helper.assertItemEntityPresent(net.minecraft.world.level.block.Blocks.STONE.asItem(), at, 3.0);
            helper.succeed();
        });
    }

    /** A focus with nothing in it is still the focus it was, which everything else leans on. */
    @GameTest(template = TEMPLATE)
    public static void aPlainFocusIsUnchanged(GameTestHelper helper) {
        for (String kind : ModItems.FOCI.keySet()) {
            ItemStack focus = focus(kind);
            helper.assertTrue(FocusUpgrades.on(focus).isEmpty(), kind + " starts with nothing in it");
            helper.assertTrue(FocusUpgrades.tuned(focus) == ModItems.FOCI.get(kind).get().base(),
                    kind + " is itself until something is worked in");
        }
        ItemStack wand = WandItem.of(ModWandParts.GREATWOOD, ModWandParts.IRON);
        WandItem.setFocus(wand, focus("frost"));
        helper.assertTrue(Casting.fitted(wand) != null, "and a wand can read it off the end of itself");
        helper.succeed();
    }
}
