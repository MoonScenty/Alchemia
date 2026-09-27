package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.entity.ArcanePedestalBlockEntity;
import me.moonscenty.alchemia.block.entity.InfusionMatrixBlockEntity;
import me.moonscenty.alchemia.block.entity.JarBlockEntity;
import me.moonscenty.alchemia.player.ModAttachments;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * An infusion from end to end: laid out, started, drunk and finished.
 * <p>
 * The smallest working there is -- an obsidian block under the matrix, two shards on the ring, and jars of what it
 * asks for standing nearby. Everything the altar does is in that, so it is worth having it run in a test rather
 * than only in somebody's world.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class InfusionTests {
    private static final String TEMPLATE = "empty_32x40x32";
    /** The matrix hangs here; the thing worked on stands two below it. */
    private static final BlockPos MATRIX = new BlockPos(8, 6, 8);

    private static ArcanePedestalBlockEntity stand(GameTestHelper helper, BlockPos at, ItemStack holding) {
        helper.setBlock(at, ModBlocks.ARCANE_PEDESTAL.get());
        ArcanePedestalBlockEntity stand =
                (ArcanePedestalBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(at));
        stand.hold(holding);
        return stand;
    }

    /** A jar of one thing, standing where the matrix can reach it. */
    private static void jar(GameTestHelper helper, BlockPos at, Holder<me.moonscenty.alchemia.aspect.Aspect> aspect,
            int amount) {
        helper.setBlock(at, ModBlocks.JAR.get());
        JarBlockEntity jar = (JarBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(at));
        for (int one = 0; one < amount; one++) {
            jar.accept(aspect);
        }
    }

    /** Somebody who has done the reading. */
    private static Player scholar(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setData(ModAttachments.KNOWLEDGE,
                PlayerKnowledge.of(player).withResearch(Alchemia.id("wand_rods")));
        return player;
    }

    /**
     * The altar itself: a pedestal two below, and a stone at each corner for a pillar to be drawn standing on.
     */
    private static void altar(GameTestHelper helper) {
        helper.setBlock(MATRIX, ModBlocks.INFUSION_MATRIX.get());
        for (int east = -1; east <= 1; east += 2) {
            for (int south = -1; south <= 1; south += 2) {
                helper.setBlock(MATRIX.offset(east, -2, south), ModBlocks.ARCANE_STONE.block().get());
            }
        }
    }

    /** The whole working: an obsidian rod, laid out and made. */
    private static InfusionMatrixBlockEntity laidOut(GameTestHelper helper) {
        altar(helper);
        stand(helper, MATRIX.below(2), new ItemStack(Items.OBSIDIAN));
        stand(helper, MATRIX.offset(2, -2, 0), new ItemStack(ModItems.BALANCED_SHARD.get()));
        stand(helper, MATRIX.offset(-2, -2, 0),
                new ItemStack(ModItems.SHARDS.get(me.moonscenty.alchemia.block.CrystalType.EARTH).get()));

        jar(helper, MATRIX.offset(0, -2, 3), ModAspects.EARTH, 16);
        jar(helper, MATRIX.offset(0, -2, -3), ModAspects.ENERGY, 16);
        jar(helper, MATRIX.offset(3, -2, 3), ModAspects.DARKNESS, 16);
        return (InfusionMatrixBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(MATRIX));
    }

    private static void run(GameTestHelper helper, InfusionMatrixBlockEntity matrix, int turns) {
        BlockPos at = helper.absolutePos(MATRIX);
        for (int tick = 0; tick < turns * InfusionMatrixBlockEntity.CYCLE + turns; tick++) {
            InfusionMatrixBlockEntity.tick(helper.getLevel(), at, helper.getLevel().getBlockState(at), matrix);
        }
    }

    /** The first touch of a wand wakes the altar; the touch after that starts the working. */
    @GameTest(template = TEMPLATE)
    public static void theFirstTouchWakesTheAltar(GameTestHelper helper) {
        InfusionMatrixBlockEntity matrix = laidOut(helper);
        Player player = scholar(helper);

        helper.assertTrue(!matrix.awake(), "a matrix hung over loose stone is asleep");
        helper.assertValueEqual(matrix.wake(player), InfusionMatrixBlockEntity.Woken.WOKEN, "the wand woke it");
        helper.assertTrue(matrix.awake(), "and it is awake");
        // the stones stay stones: a pillar is a picture the matrix draws over them
        helper.assertBlockPresent(ModBlocks.ARCANE_STONE.block().get(), MATRIX.offset(1, -2, 1));

        helper.assertValueEqual(matrix.wake(player), InfusionMatrixBlockEntity.Woken.STARTED,
                "and the touch after that starts the working");
        helper.succeed();
    }

    /**
     * Take one corner out and the altar stops being one, within the second.
     * <p>
     * A corner stone is not a neighbour of the matrix, so nothing tells it when one goes; it has to look. What is
     * worth holding to is that one stone is enough, since somebody who breaks one and sees nothing happen will
     * break another to find out why.
     */
    @GameTest(template = TEMPLATE)
    public static void breakingOneCornerPutsItToSleep(GameTestHelper helper) {
        InfusionMatrixBlockEntity matrix = laidOut(helper);
        matrix.wake(scholar(helper));
        helper.setBlock(MATRIX.offset(1, -2, 1), Blocks.AIR);

        run(helper, matrix, 2);
        helper.assertTrue(!matrix.awake(), "the altar went back to sleep");
        helper.succeed();
    }

    /** Without its four corners there is no altar, and nothing wakes. */
    @GameTest(template = TEMPLATE)
    public static void itWillNotWakeWithoutAnAltar(GameTestHelper helper) {
        InfusionMatrixBlockEntity matrix = laidOut(helper);
        helper.setBlock(MATRIX.offset(1, -2, 1), Blocks.AIR);

        helper.assertValueEqual(matrix.wake(scholar(helper)), InfusionMatrixBlockEntity.Woken.UNBUILT,
                "three corners is not an altar");
        helper.assertTrue(!matrix.awake(), "so it stayed asleep");
        helper.succeed();
    }

    /** Pulling a pillar out from under a working ends it and puts the matrix back to sleep. */
    @GameTest(template = TEMPLATE)
    public static void breakingTheAltarEndsTheWorking(GameTestHelper helper) {
        InfusionMatrixBlockEntity matrix = laidOut(helper);
        Player player = scholar(helper);
        matrix.wake(player);
        matrix.wake(player);
        helper.assertTrue(matrix.busy(), "the working started");

        helper.setBlock(MATRIX.offset(-1, -2, 1), Blocks.AIR);
        run(helper, matrix, 2);
        helper.assertTrue(!matrix.busy(), "and came apart with the altar");
        helper.assertTrue(!matrix.awake(), "which also put the stones back to sleep");
        helper.succeed();
    }

    /** The matrix finds the ring below it, one pedestal to a column. */
    @GameTest(template = TEMPLATE)
    public static void itFindsTheRing(GameTestHelper helper) {
        helper.setBlock(MATRIX, ModBlocks.INFUSION_MATRIX.get());
        stand(helper, MATRIX.offset(3, -1, 0), ItemStack.EMPTY);
        stand(helper, MATRIX.offset(3, -4, 0), ItemStack.EMPTY);
        stand(helper, MATRIX.offset(0, -2, -5), ItemStack.EMPTY);

        var found = InfusionMatrixBlockEntity.around(helper.getLevel(), helper.absolutePos(MATRIX));
        helper.assertValueEqual(found.size(), 2, "two columns, however many pedestals are stacked in one");
        helper.succeed();
    }

    /** Laid out properly, it starts; laid out badly, it does not. */
    @GameTest(template = TEMPLATE)
    public static void itStartsOnWhatIsLaidOut(GameTestHelper helper) {
        InfusionMatrixBlockEntity matrix = laidOut(helper);
        Player player = scholar(helper);
        matrix.wake(player);
        helper.assertTrue(matrix.start(player), "the working was laid out right");
        helper.assertTrue(matrix.busy(), "so it started");
        helper.assertTrue(!matrix.owed().isEmpty(), "and it owes for it");
        helper.succeed();
    }

    /** Without the reading behind it, the same ring makes nothing. */
    @GameTest(template = TEMPLATE)
    public static void itWantsTheResearchDone(GameTestHelper helper) {
        InfusionMatrixBlockEntity matrix = laidOut(helper);
        matrix.wake(scholar(helper));
        helper.assertTrue(!matrix.start(helper.makeMockPlayer(GameType.SURVIVAL)),
                "nobody makes a rod by accident");
        helper.assertTrue(!matrix.busy(), "and nothing started");
        helper.succeed();
    }

    /** It drinks the jars dry, eats the ring, and leaves the rod on the pedestal. */
    @GameTest(template = TEMPLATE, timeoutTicks = 600)
    public static void itDrinksAndFinishes(GameTestHelper helper) {
        InfusionMatrixBlockEntity matrix = laidOut(helper);
        Player player = scholar(helper);
        matrix.wake(player);
        matrix.start(player);

        // twelve of one essentia is the longest part, and then a turn apiece for the two things on the ring
        run(helper, matrix, 20);

        ArcanePedestalBlockEntity under =
                (ArcanePedestalBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(MATRIX.below(2)));
        helper.assertTrue(under.held().is(ModItems.WAND_RODS.get("obsidian").get()),
                "the obsidian became a rod");
        helper.assertTrue(!matrix.busy(), "and the matrix went still");

        ArcanePedestalBlockEntity ring = (ArcanePedestalBlockEntity) helper.getLevel()
                .getBlockEntity(helper.absolutePos(MATRIX.offset(2, -2, 0)));
        helper.assertTrue(ring.held().isEmpty(), "the ring was eaten");
        helper.succeed();
    }

    /** Take the thing being worked on away mid-working and the whole thing comes apart. */
    @GameTest(template = TEMPLATE)
    public static void takingTheWorkAwayEndsIt(GameTestHelper helper) {
        InfusionMatrixBlockEntity matrix = laidOut(helper);
        Player player = scholar(helper);
        matrix.wake(player);
        matrix.start(player);
        helper.setBlock(MATRIX.below(2), Blocks.AIR);

        run(helper, matrix, 1);
        helper.assertTrue(!matrix.busy(), "with nothing to work on there is no working");
        helper.succeed();
    }
}
