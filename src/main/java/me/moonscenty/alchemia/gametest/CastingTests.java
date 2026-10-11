package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModWandParts;
import me.moonscenty.alchemia.wand.Casting;
import me.moonscenty.alchemia.wand.spell.Grapple;
import me.moonscenty.alchemia.wand.spell.Ember;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.wand.spell.Hole;
import me.moonscenty.alchemia.wand.spell.PechBlast;
import me.moonscenty.alchemia.wand.spell.Swapping;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import me.moonscenty.alchemia.wand.spell.PrimalOrb;
import me.moonscenty.alchemia.wand.spell.VisShard;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Letting a wand off: what has to be true before a spell runs, and what it costs when it does.
 * <p>
 * The order is the whole of it. A wand that cannot pay does not cast; a spell that finds nothing to do is not
 * paid for. Getting either backwards empties a wand into the sky.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class CastingTests {
    private static final String TEMPLATE = "empty_32x40x32";

    /**
     * A wand with a focus on it and as much of every primal in it as asked for.
     * <p>
     * Every primal rather than only air: half the foci are priced in things that are not air, and a wand filled
     * with one of the six is a wand that cannot afford most of what is being tested.
     */
    private static ItemStack wand(String focus, int each) {
        ItemStack wand = WandItem.of(ModWandParts.SILVERWOOD, ModWandParts.GOLD);
        WandItem.setFocus(wand, new ItemStack(ModItems.FOCI.get(focus).get()));
        AspectList filled = AspectList.EMPTY;
        for (var primal : ModAspects.primals()) {
            filled = filled.add(primal, each * WandItem.FINE);
        }
        wand.set(ModDataComponents.VIS.get(), filled);
        return wand;
    }

    /** A shock finds whatever is in the way, hurts it, and takes the price off the wand. */
    @GameTest(template = TEMPLATE)
    public static void aShockHurtsWhatItIsPointedAtAndIsPaidFor(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wand = wand("shock", 100);

        BlockPos where = new BlockPos(2, 2, 2);
        LivingEntity target = helper.spawn(EntityType.ZOMBIE, where);
        helper.assertTrue(target.isAlive(), "there is something to shoot at");

        float before = target.getHealth();
        int had = ((WandItem) wand.getItem()).held(wand, ModAspects.AIR);
        boolean went = me.moonscenty.alchemia.wand.spell.Shock.cast(helper.getLevel(), caster, wand,
                new net.minecraft.world.phys.EntityHitResult(target), 0);

        helper.assertTrue(went, "the bolt landed");
        helper.assertTrue(target.getHealth() < before, "and it hurt");
        helper.assertValueEqual(((WandItem) wand.getItem()).held(wand, ModAspects.AIR), had,
                "the spell itself takes nothing; paying is somebody else's job");
        helper.succeed();
    }

    /**
     * A shock aimed at nothing costs nothing.
     * <p>
     * Air is not a target. A wand that paid for a miss would be a wand emptied by waving it about.
     */
    @GameTest(template = TEMPLATE)
    public static void aShockAtNothingIsNotPaidFor(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wand = wand("shock", 100);
        int had = ((WandItem) wand.getItem()).held(wand, ModAspects.AIR);

        boolean went = Casting.cast(helper.getLevel(), caster, wand, 0);
        helper.assertFalse(went, "nothing was in the way");
        helper.assertValueEqual(((WandItem) wand.getItem()).held(wand, ModAspects.AIR), had,
                "so nothing was spent");
        helper.succeed();
    }

    /**
     * An empty wand does not cast at all.
     * <p>
     * Checked before the spell runs rather than after, so that a spell is never half done and then unpaid for.
     */
    @GameTest(template = TEMPLATE)
    public static void anEmptyWandDoesNotCast(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.SURVIVAL);
        LivingEntity target = helper.spawn(EntityType.ZOMBIE, new BlockPos(2, 2, 2));
        float before = target.getHealth();

        ItemStack dry = wand("shock", 0);
        helper.assertFalse(Casting.cast(helper.getLevel(), caster, dry, 0), "an empty wand will not go off");
        helper.assertTrue(target.getHealth() == before, "and nothing was hurt by it");
        helper.succeed();
    }

    /** A wand with nothing fitted is a stick with vis in it. */
    @GameTest(template = TEMPLATE)
    public static void aBareWandCastsNothing(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack bare = WandItem.of(ModWandParts.SILVERWOOD, ModWandParts.GOLD);
        WandItem.brimming(bare);
        helper.assertTrue(Casting.fittedTo(bare) == null, "nothing is fitted to it");
        helper.assertFalse(Casting.cast(helper.getLevel(), caster, bare, 0), "so nothing comes of using it");
        helper.succeed();
    }
    /**
     * A pech's curse bursts where it lands and leaves what was standing there the worse for it.
     * <p>
     * It is thrown, not aimed: whatever is within reach of the burst catches it, and whoever threw it does not.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void aPechsCurseAfflictsWhatItBurstsOn(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.SURVIVAL);
        // a pig rather than a zombie: two of the three afflictions are poison and weakness, and the dead feel
        // neither. A curse tested on something immune to two thirds of it is a curse tested on nothing
        LivingEntity victim = helper.spawn(EntityType.PIG, new BlockPos(2, 2, 2));

        PechBlast blast = new PechBlast(helper.getLevel(), caster);
        blast.setPos(victim.getX(), victim.getY() + 0.5, victim.getZ());
        helper.getLevel().addFreshEntity(blast);
        blast.burst(new net.minecraft.world.phys.BlockHitResult(blast.position(),
                net.minecraft.core.Direction.UP, helper.absolutePos(new BlockPos(2, 1, 2)), false));

        helper.assertTrue(!victim.getActiveEffects().isEmpty(), "something settled on it");
        helper.assertTrue(victim.getHealth() < victim.getMaxHealth(), "and the burst itself hurt");
        helper.assertTrue(caster.getActiveEffects().isEmpty(), "whoever threw it is spared");
        helper.succeed();
    }

    /**
     * A hook that has bitten hauls whoever threw it towards itself.
     * <p>
     * A hook that has not bitten does nothing at all, which is what stops one from towing a climber off the
     * ground before it has anything to pull against.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void aBittenHookHaulsAndALooseOneDoesNot(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.SURVIVAL);
        caster.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);

        Grapple loose = new Grapple(helper.getLevel(), caster);
        loose.setPos(caster.getX(), caster.getY() + 8.0, caster.getZ());
        helper.getLevel().addFreshEntity(loose);
        loose.tick();
        helper.assertTrue(caster.getDeltaMovement().equals(net.minecraft.world.phys.Vec3.ZERO),
                "a hook in the air pulls nothing");

        loose.bite(new net.minecraft.world.phys.BlockHitResult(loose.position(),
                net.minecraft.core.Direction.DOWN, helper.absolutePos(new BlockPos(0, 9, 0)), false));
        helper.assertTrue(loose.biting(), "it has bitten");
        loose.tick();
        helper.assertTrue(caster.getDeltaMovement().y > 0.0, "and now it hauls upwards");
        helper.succeed();
    }
    /**
     * A vis shard will not go off without a mark, and never misses once it has one.
     * <p>
     * That is the whole of what makes it the cheapest of the twelve. A focus that cannot miss has to be given
     * something to not miss; otherwise it is a free hit on an empty room.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void aVisShardNeedsAMarkAndThenKeepsIt(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wand = wand("shard", 100);
        helper.assertFalse(Casting.cast(helper.getLevel(), caster, wand, 0),
                "pointed at nothing, it does not go off");

        LivingEntity mark = helper.spawn(EntityType.PIG, new BlockPos(2, 2, 2));
        VisShard shard = new VisShard(helper.getLevel(), caster, mark);
        shard.setPos(caster.getX(), caster.getY() + 1.0, caster.getZ());
        shard.setDeltaMovement(0.0, 0.0, 0.0);
        helper.getLevel().addFreshEntity(shard);

        // one tick of steering is enough to tell a shard that chases from one that drifts
        shard.tick();
        helper.assertTrue(shard.getDeltaMovement().length() > 0.0, "it set off after its mark");
        helper.succeed();
    }

    /** Embers come by the handful and burn out on their own, so a long press leaves nothing behind. */
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void embersGoOutOnTheirOwn(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.SURVIVAL);
        Ember mote = new Ember(helper.getLevel(), caster);
        mote.setPos(caster.getX(), caster.getY() + 1.0, caster.getZ());
        helper.getLevel().addFreshEntity(mote);
        helper.assertTrue(mote.isAlive(), "it is lit");

        helper.runAfterDelay(40, () -> {
            helper.assertFalse(mote.isAlive(), "and it has gone out on its own");
            helper.succeed();
        });
    }

    /** A primal orb is a bomb. What it leaves is a hole, which is the original's doing and not a slip here. */
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void aPrimalOrbGoesOff(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos stand = new BlockPos(3, 2, 3);
        helper.setBlock(stand, net.minecraft.world.level.block.Blocks.DIRT);

        PrimalOrb orb = new PrimalOrb(helper.getLevel(), caster);
        orb.setPos(helper.absoluteVec(new net.minecraft.world.phys.Vec3(3.5, 3.0, 3.5)));
        helper.getLevel().addFreshEntity(orb);
        orb.hurtMarked = false;
        orb.lands(new net.minecraft.world.phys.BlockHitResult(orb.position(),
                net.minecraft.core.Direction.UP, helper.absolutePos(stand), false));

        helper.assertBlockPresent(net.minecraft.world.level.block.Blocks.AIR, stand);
        helper.succeed();
    }
    /**
     * A builder lays a row out from the wall it was clicked, as long as the caster is standing back.
     * <p>
     * It will not lay anything until it has been shown what to lay, and being shown costs nothing -- that is a
     * decision rather than a working.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void aBuilderLaysARowOnceItHasBeenShownWhatToLay(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.CREATIVE);
        ItemStack wand = wand("builder", 100);
        BlockPos wall = new BlockPos(1, 2, 1);
        helper.setBlock(wall, Blocks.STONE);
        caster.setPos(helper.absoluteVec(new Vec3(5.5, 2.0, 1.5)));

        BlockHitResult face = new BlockHitResult(Vec3.ZERO, Direction.EAST, helper.absolutePos(wall), false);
        helper.assertFalse(Swapping.builder(helper.getLevel(), caster, wand, face, 0),
                "with nothing shown to it, it lays nothing");

        caster.setShiftKeyDown(true);
        Swapping.builder(helper.getLevel(), caster, wand, face, 0);
        caster.setShiftKeyDown(false);
        helper.assertTrue(WandItem.focus(wand).get(ModDataComponents.PICKED_BLOCK.get()) != null,
                "it remembers what it was shown");

        helper.assertTrue(Swapping.builder(helper.getLevel(), caster, wand, face, 0), "now it lays");
        helper.assertBlockPresent(Blocks.STONE, wall.east());
        helper.succeed();
    }

    /**
     * A trade turns one kind of block into another and spreads along it.
     * <p>
     * It follows the vein rather than filling a box, so what is around the vein is left where it was.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void aTradeFollowsTheVein(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.CREATIVE);
        ItemStack wand = wand("trade", 100);
        BlockPos first = new BlockPos(2, 2, 2);
        helper.setBlock(first, Blocks.COAL_ORE);
        helper.setBlock(first.east(), Blocks.COAL_ORE);
        helper.setBlock(first.east().east(), Blocks.DIRT);

        // shown a stone, then set on the ore
        helper.setBlock(new BlockPos(2, 3, 2), Blocks.STONE);
        caster.setShiftKeyDown(true);
        Swapping.trade(helper.getLevel(), caster, wand,
                new BlockHitResult(Vec3.ZERO, Direction.UP, helper.absolutePos(new BlockPos(2, 3, 2)), false), 0);
        caster.setShiftKeyDown(false);

        helper.assertTrue(Swapping.trade(helper.getLevel(), caster, wand,
                new BlockHitResult(Vec3.ZERO, Direction.UP, helper.absolutePos(first), false), 0),
                "the vein was traded");
        helper.assertBlockPresent(Blocks.STONE, first);
        helper.assertBlockPresent(Blocks.STONE, first.east());
        // what was not ore is left alone: the trade follows the vein rather than filling a box
        helper.assertBlockPresent(Blocks.DIRT, first.east().east());
        helper.succeed();
    }
    /**
     * A portable hole bores through a wall and the wall comes back.
     * <p>
     * Nothing is broken and nothing drops. The whole worth of it is that the wall is a wall again a few seconds
     * later, so what is opened is a way through rather than a way of taking the wall.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void aHoleBoresThroughAndTheWallComesBack(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.CREATIVE);
        ItemStack wand = wand("hole", 100);
        BlockPos face = new BlockPos(2, 2, 2);
        helper.setBlock(face, Blocks.STONE);
        helper.setBlock(face.east(), Blocks.STONE);

        BlockHitResult struck = new BlockHitResult(Vec3.ZERO, Direction.WEST, helper.absolutePos(face), false);
        helper.assertTrue(Hole.cast(helper.getLevel(), caster, wand, struck, 0), "it bored");
        helper.assertBlockPresent(ModBlocks.HOLE.get(), face);

        // the layer behind it opens a tick later, which is what makes it look bored rather than deleted
        helper.runAfterDelay(2, () -> helper.assertBlockPresent(ModBlocks.HOLE.get(), face.east()));
        helper.runAfterDelay(160, () -> {
            helper.assertBlockPresent(Blocks.STONE, face);
            helper.assertBlockPresent(Blocks.STONE, face.east());
            helper.succeed();
        });
    }

    /** Nothing to bore through is nothing to pay for. */
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void aHoleInThinAirDoesNothing(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.CREATIVE);
        ItemStack wand = wand("hole", 100);
        BlockPos nothing = new BlockPos(4, 3, 4);
        helper.assertFalse(Hole.cast(helper.getLevel(), caster, wand,
                        new BlockHitResult(Vec3.ZERO, Direction.WEST, helper.absolutePos(nothing), false), 0),
                "there was nothing there to bore");
        helper.succeed();
    }
}
