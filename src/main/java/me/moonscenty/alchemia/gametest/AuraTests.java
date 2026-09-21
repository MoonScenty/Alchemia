package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aura.AuraChunk;
import me.moonscenty.alchemia.aura.AuraGeneration;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.aura.ModAuraAttachment;
import me.moonscenty.alchemia.registry.ModAspects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The aura is the one thing in the mod that changes on its own, so what matters is that nothing is created or lost
 * by accident and that a chunk drawn dry can come back.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class AuraTests {
    private static final String TEMPLATE = "empty_32x40x32";

    private static LevelChunk chunkOf(GameTestHelper helper) {
        BlockPos here = helper.absolutePos(BlockPos.ZERO);
        return helper.getLevel().getChunkAt(here);
    }

    @GameTest(template = TEMPLATE)
    public static void landIsGivenAnAuraTheFirstTimeItIsSeen(GameTestHelper helper) {
        LevelChunk chunk = chunkOf(helper);
        AuraGeneration.ensure(helper.getLevel(), chunk, RandomSource.create(1L));
        AuraChunk aura = chunk.getData(ModAuraAttachment.AURA);

        helper.assertTrue(aura.exists(), "a chunk that has been loaded should have an aura");
        helper.assertTrue(aura.aspects().size() == 6, "all six primals should be in the air, found " + aura.aspects().size());
        for (Holder<Aspect> primal : ModAspects.primals()) {
            helper.assertTrue(aura.get(primal) > 0, primal.value().tag() + " should be in the air");
        }
        helper.succeed();
    }

    /** Drawing it up once must not draw it up again, or walking away and back would reset the land. */
    @GameTest(template = TEMPLATE)
    public static void anAuraIsOnlyDrawnUpOnce(GameTestHelper helper) {
        LevelChunk chunk = chunkOf(helper);
        AuraGeneration.ensure(helper.getLevel(), chunk, RandomSource.create(2L));
        AuraChunk first = chunk.getData(ModAuraAttachment.AURA);

        AuraHandler.drain(helper.getLevel(), helper.absolutePos(BlockPos.ZERO), ModAspects.AIR, 5);
        AuraGeneration.ensure(helper.getLevel(), chunk, RandomSource.create(3L));
        AuraChunk second = chunk.getData(ModAuraAttachment.AURA);

        helper.assertTrue(second.base() == first.base(), "the land should hold the same as it did");
        helper.assertTrue(second.get(ModAspects.AIR) == first.get(ModAspects.AIR) - 5,
                "what was drawn should still be missing");
        helper.succeed();
    }

    /** A draw that cannot be met takes nothing at all, so a half-paid cost is impossible. */
    @GameTest(template = TEMPLATE)
    public static void drawingMoreThanIsThereTakesNothing(GameTestHelper helper) {
        LevelChunk chunk = chunkOf(helper);
        AuraGeneration.ensure(helper.getLevel(), chunk, RandomSource.create(4L));
        BlockPos here = helper.absolutePos(BlockPos.ZERO);
        int before = AuraHandler.get(helper.getLevel(), here, ModAspects.WATER);

        helper.assertTrue(!AuraHandler.drain(helper.getLevel(), here, ModAspects.WATER, before + 1),
                "drawing more than is there should fail");
        helper.assertTrue(AuraHandler.get(helper.getLevel(), here, ModAspects.WATER) == before,
                "a failed draw should have left the chunk alone");

        helper.assertTrue(AuraHandler.drain(helper.getLevel(), here, ModAspects.WATER, before),
                "drawing exactly what is there should work");
        helper.assertTrue(AuraHandler.get(helper.getLevel(), here, ModAspects.WATER) == 0, "which should empty it");
        helper.succeed();
    }

    /** Asking for several at once is all or nothing too, however the list is ordered. */
    @GameTest(template = TEMPLATE)
    public static void drawingSeveralAtOnceIsAllOrNothing(GameTestHelper helper) {
        LevelChunk chunk = chunkOf(helper);
        AuraGeneration.ensure(helper.getLevel(), chunk, RandomSource.create(5L));
        BlockPos here = helper.absolutePos(BlockPos.ZERO);
        int air = AuraHandler.get(helper.getLevel(), here, ModAspects.AIR);
        int fire = AuraHandler.get(helper.getLevel(), here, ModAspects.FIRE);

        AspectList tooMuch = AspectList.of(ModAspects.AIR, 1).add(ModAspects.FIRE, fire + 1);
        helper.assertTrue(!AuraHandler.drain(helper.getLevel(), here, tooMuch), "one aspect short should fail the lot");
        helper.assertTrue(AuraHandler.get(helper.getLevel(), here, ModAspects.AIR) == air,
                "the aspect there was enough of should not have been touched");

        AspectList fits = AspectList.of(ModAspects.AIR, 1).add(ModAspects.FIRE, 1);
        helper.assertTrue(AuraHandler.drain(helper.getLevel(), here, fits), "a draw that fits should work");
        helper.assertTrue(AuraHandler.get(helper.getLevel(), here, ModAspects.AIR) == air - 1
                && AuraHandler.get(helper.getLevel(), here, ModAspects.FIRE) == fire - 1,
                "both should have come down by one");
        helper.succeed();
    }

    /** Taking what is available never takes more than is there and says truthfully how much it took. */
    @GameTest(template = TEMPLATE)
    public static void takingWhatIsAvailableIsHonest(GameTestHelper helper) {
        LevelChunk chunk = chunkOf(helper);
        AuraGeneration.ensure(helper.getLevel(), chunk, RandomSource.create(6L));
        BlockPos here = helper.absolutePos(BlockPos.ZERO);
        int there = AuraHandler.get(helper.getLevel(), here, ModAspects.EARTH);

        int taken = AuraHandler.drainAvailable(helper.getLevel(), here, ModAspects.EARTH, there + 100);
        helper.assertTrue(taken == there, "should have taken the " + there + " that was there, took " + taken);
        helper.assertTrue(AuraHandler.get(helper.getLevel(), here, ModAspects.EARTH) == 0, "which empties it");
        helper.assertTrue(AuraHandler.drainAvailable(helper.getLevel(), here, ModAspects.EARTH, 10) == 0,
                "and there is nothing left to take");
        helper.succeed();
    }

    /** Land run down past a tenth of what it holds is worth leaving alone. */
    @GameTest(template = TEMPLATE)
    public static void thinAuraAsksToBeSpared(GameTestHelper helper) {
        LevelChunk chunk = chunkOf(helper);
        AuraGeneration.ensure(helper.getLevel(), chunk, RandomSource.create(7L));
        BlockPos here = helper.absolutePos(BlockPos.ZERO);

        helper.assertTrue(!AuraHandler.shouldSpare(helper.getLevel(), here, ModAspects.ORDER),
                "untouched land should not need sparing");
        AuraHandler.drainAvailable(helper.getLevel(), here, ModAspects.ORDER, 10000);
        helper.assertTrue(AuraHandler.shouldSpare(helper.getLevel(), here, ModAspects.ORDER),
                "land drawn dry should ask to be spared");
        helper.succeed();
    }

    /** Somewhere with no aura at all must refuse everything rather than pretend. */
    @GameTest(template = TEMPLATE)
    public static void landWithoutAnAuraGivesNothing(GameTestHelper helper) {
        AuraChunk none = AuraChunk.NONE;
        helper.assertTrue(!none.exists(), "an unvisited chunk holds nothing");
        helper.assertTrue(none.get(ModAspects.AIR) == 0, "and reads as empty");

        // a position far outside the loaded area has no chunk to speak of
        BlockPos away = new BlockPos(30_000_000, 64, 30_000_000);
        helper.assertTrue(AuraHandler.get(helper.getLevel(), away, ModAspects.AIR) == 0, "unloaded land reads as empty");
        helper.assertTrue(!AuraHandler.drain(helper.getLevel(), away, ModAspects.AIR, 1), "and gives nothing");
        helper.succeed();
    }

    /** The land is what decides the aura, so a chunk is never blank and never unbounded. */
    @GameTest(template = TEMPLATE)
    public static void whatTheLandHoldsStaysWithinReason(GameTestHelper helper) {
        for (long seed = 0; seed < 50; seed++) {
            AuraChunk aura = AuraGeneration.drawUp(helper.getLevel(),
                    new ChunkPos(helper.absolutePos(BlockPos.ZERO)), RandomSource.create(seed));
            helper.assertTrue(aura.base() > 0, "seed " + seed + " gave a chunk that holds nothing");
            helper.assertTrue(aura.base() <= AuraGeneration.BASE * 2,
                    "seed " + seed + " gave a chunk holding " + aura.base() + ", far past the usual "
                            + AuraGeneration.BASE);
            for (Holder<Aspect> primal : ModAspects.primals()) {
                helper.assertTrue(aura.get(primal) <= aura.base(),
                        "seed " + seed + " put more " + primal.value().tag() + " in the air than the land holds");
            }
        }
        helper.succeed();
    }
}
