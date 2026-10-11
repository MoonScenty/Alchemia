package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aura.AuraGeneration;
import me.moonscenty.alchemia.entity.Autocaster;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The autocaster: a focus that works while nobody is holding it.
 * <p>
 * What is worth proving is that it only takes foci worth aiming, that it is its owner's and nobody else's, that
 * it fills itself out of the air, and that it will not shoot on an empty reservoir. The shooting itself is the
 * focus's own, and is tested where the focus is.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class AutocasterTests {
    private static final String TEMPLATE = "empty_32x40x32";

    /** One against a wall, owned by whoever put it there. */
    private static Autocaster placed(GameTestHelper helper, Player owner, BlockPos at, Direction face) {
        Autocaster turret = new Autocaster(helper.getLevel(), helper.absolutePos(at), face);
        turret.setOwner(owner.getUUID());
        helper.getLevel().addFreshEntity(turret);
        return turret;
    }

    private static void fit(Autocaster turret, String focus) {
        turret.setItem(0, new ItemStack(ModItems.FOCI.get(focus).get()));
    }

    /** Seven of the twelve are worth aiming at a thing; the rest mean nothing without a hand behind them. */
    @GameTest(template = TEMPLATE)
    public static void itOnlyTakesAFocusWorthAiming(GameTestHelper helper) {
        helper.assertTrue(Autocaster.takes(new ItemStack(ModItems.FOCI.get("shock").get())),
                "a shock is worth aiming");
        helper.assertTrue(Autocaster.takes(new ItemStack(ModItems.FOCI.get("fire").get())),
                "so is a spray of fire");
        helper.assertFalse(Autocaster.takes(new ItemStack(ModItems.FOCI.get("hole").get())),
                "a hole is somebody deciding where to put a door");
        helper.assertFalse(Autocaster.takes(new ItemStack(ModItems.FOCI.get("builder").get())),
                "and a builder has nothing to build without one");
        helper.assertTrue(Autocaster.takes(ItemStack.EMPTY), "an empty square is always allowed");
        helper.succeed();
    }

    /** Put against a face: it stands where it was put, pointing out of the wall it is on. */
    @GameTest(template = TEMPLATE)
    public static void aPlacerPutsOneAgainstTheFaceThatWasClicked(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos wall = new BlockPos(2, 2, 2);
        helper.setBlock(wall, Blocks.STONE);

        ItemStack placer = new ItemStack(ModItems.AUTOCASTER.get());
        owner.setItemInHand(InteractionHand.MAIN_HAND, placer);
        BlockHitResult struck = new BlockHitResult(Vec3.ZERO, Direction.UP, helper.absolutePos(wall), false);
        ModItems.AUTOCASTER.get().useOn(new UseOnContext(owner, InteractionHand.MAIN_HAND, struck));

        BlockPos above = wall.above();
        Autocaster turret = helper.getLevel()
                .getEntitiesOfClass(Autocaster.class, helper.getBounds().inflate(2.0))
                .stream().findFirst().orElse(null);
        helper.assertTrue(turret != null, "there is a turret where it was put");
        helper.assertTrue(turret.getFacing() == Direction.UP, "standing on the face that was clicked");
        helper.assertTrue(owner.getUUID().equals(turret.getOwnerUUID()), "and it belongs to whoever put it there");
        helper.assertTrue(turret.blockPosition().equals(helper.absolutePos(above)),
                "in the square in front of the wall");
        helper.succeed();
    }

    /** A focus with a longer reach is a turret that looks further, and no vis is lost in the change. */
    @GameTest(template = TEMPLATE)
    public static void changingTheFocusChangesHowFarItLooks(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        Autocaster turret = placed(helper, owner, new BlockPos(2, 2, 2), Direction.UP);
        turret.fill();

        fit(turret, "fire");
        helper.assertValueEqual((int) turret.getAttributeValue(
                net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE), 12, "a spray reaches twelve");
        fit(turret, "shock");
        helper.assertValueEqual((int) turret.getAttributeValue(
                net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE), 20, "a bolt reaches twenty");
        helper.assertValueEqual(turret.vis().get(ModAspects.AIR), Autocaster.HOLDS,
                "and what it had drawn is still in it");
        helper.succeed();
    }

    /** It fills itself out of the air it is standing in, a drop at a time and the thinnest primal first. */
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void itDrawsItsOwnVisOutOfTheAir(GameTestHelper helper) {
        AuraGeneration.ensure(helper.getLevel(),
                helper.getLevel().getChunkAt(helper.absolutePos(BlockPos.ZERO)), RandomSource.create(7L));
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        Autocaster turret = placed(helper, owner, new BlockPos(2, 2, 2), Direction.UP);
        helper.assertTrue(turret.vis().isEmpty(), "it starts dry");

        helper.runAfterDelay(100, () -> {
            helper.assertTrue(!turret.vis().isEmpty(), "and fills itself from the air around it");
            helper.assertTrue(turret.vis().size() > 1, "evenly, rather than on whatever the land is thick with");
            helper.succeed();
        });
    }

    /** A dry turret with a good mark in front of it holds its fire rather than shooting for nothing. */
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void aDryTurretDoesNotShoot(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        Autocaster turret = placed(helper, owner, new BlockPos(2, 2, 2), Direction.UP);
        fit(turret, "shock");
        turret.mind(Autocaster.ANIMALS, true);

        LivingEntity mark = helper.spawn(EntityType.PIG, new BlockPos(5, 2, 2));
        float whole = mark.getHealth();
        helper.runAfterDelay(80, () -> {
            helper.assertValueEqual(mark.getHealth(), whole, "nothing came out of an empty turret");
            helper.succeed();
        });
    }

    /** Filled, told to mind animals and pointed into an open room, it shoots. */
    @GameTest(template = TEMPLATE, timeoutTicks = 300)
    public static void aFilledTurretShootsWhatItIsToldToMind(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        Autocaster turret = placed(helper, owner, new BlockPos(2, 3, 2), Direction.UP);
        fit(turret, "shock");
        turret.fill();
        turret.mind(Autocaster.ANIMALS, true);

        LivingEntity mark = helper.spawn(EntityType.PIG, new BlockPos(6, 3, 2));
        float whole = mark.getHealth();
        helper.runAfterDelay(160, () -> {
            helper.assertTrue(!mark.isAlive() || mark.getHealth() < whole, "the pig was shot at");
            helper.assertTrue(turret.vis().get(ModAspects.AIR) < Autocaster.HOLDS, "and it was paid for");
            helper.succeed();
        });
    }

    /** What it is not told to mind, it leaves alone. */
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void itLeavesAloneWhatItWasNotToldToMind(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        Autocaster turret = placed(helper, owner, new BlockPos(2, 3, 2), Direction.UP);
        fit(turret, "shock");
        turret.fill();
        turret.mind(Autocaster.ANIMALS, false);
        turret.mind(Autocaster.MONSTERS, false);

        LivingEntity mark = helper.spawn(EntityType.PIG, new BlockPos(6, 3, 2));
        float whole = mark.getHealth();
        helper.runAfterDelay(100, () -> {
            helper.assertValueEqual(mark.getHealth(), whole, "a turret minding nothing shoots nothing");
            helper.succeed();
        });
    }

    /** Broken open, it gives back whatever was bolted into it. */
    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void itGivesBackItsFocusWhenItIsKilled(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        Autocaster turret = placed(helper, owner, new BlockPos(2, 2, 2), Direction.UP);
        fit(turret, "shock");

        turret.kill();
        helper.runAfterDelay(5, () -> {
            helper.assertItemEntityPresent(ModItems.FOCI.get("shock").get(), new BlockPos(2, 2, 2), 3.0);
            helper.assertTrue(turret.getItem(0).isEmpty(), "and does not keep a copy of it");
            helper.succeed();
        });
    }

    /** It will not be opened or taken down by somebody who did not put it there. */
    @GameTest(template = TEMPLATE)
    public static void itIsNobodyElsesToTouch(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        Player stranger = helper.makeMockPlayer(GameType.SURVIVAL);
        Autocaster turret = placed(helper, owner, new BlockPos(2, 2, 2), Direction.UP);

        helper.assertTrue(turret.owns(owner), "whoever put it there may open it");
        helper.assertFalse(turret.owns(stranger), "and nobody else may");
        helper.assertFalse(turret.stillValid(stranger), "nor keep a window on it open");
        helper.succeed();
    }

    /** The one thing it carries is in its hand, which is what the client is shown and what a renderer reads. */
    @GameTest(template = TEMPLATE)
    public static void itsFocusIsTheOneThingItHolds(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        Autocaster turret = placed(helper, owner, new BlockPos(2, 2, 2), Direction.UP);
        fit(turret, "frost");

        helper.assertTrue(turret.fitted() != null, "it knows what is in it");
        helper.assertTrue(turret.getItemBySlot(EquipmentSlot.MAINHAND)
                .is(ModItems.FOCI.get("frost").get()), "and holds it where everyone can see it");
        helper.assertTrue(turret.getType() == ModEntities.AUTOCASTER.get(), "and is what it says it is");
        helper.succeed();
    }
}
