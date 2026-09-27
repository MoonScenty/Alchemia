package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.TubeBlock;
import me.moonscenty.alchemia.block.entity.AlembicBlockEntity;
import me.moonscenty.alchemia.block.entity.JarBlockEntity;
import me.moonscenty.alchemia.block.entity.TubeBlockEntity;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * A pipe is worth laying only if it knows what it is plugged into and actually carries something along its length.
 * The first is what the model is built out of; the second is the whole point.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class TubeTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final BlockPos START = new BlockPos(2, 1, 2);

    private static AlembicBlockEntity alembic(GameTestHelper helper, BlockPos at) {
        helper.setBlock(at, ModBlocks.ALEMBIC.get());
        return (AlembicBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(at));
    }

    /** A run of pipe from one spot to another, laid along the x axis. */
    private static void pipe(GameTestHelper helper, BlockPos from, int length) {
        for (int step = 0; step < length; step++) {
            helper.setBlock(from.east(step), ModBlocks.TUBE.get());
        }
    }

    private static void run(GameTestHelper helper, BlockPos at, int times) {
        BlockPos where = helper.absolutePos(at);
        TubeBlockEntity tube = (TubeBlockEntity) helper.getLevel().getBlockEntity(where);
        for (int tick = 0; tick < times; tick++) {
            TubeBlockEntity.tick(helper.getLevel(), where, helper.getLevel().getBlockState(where), tube);
        }
    }

    /** Each side of a tube knows whether it is up against another tube, something worth reaching into, or air. */
    @GameTest(template = TEMPLATE)
    public static void itKnowsWhatItIsPluggedInto(GameTestHelper helper) {
        alembic(helper, START);
        pipe(helper, START.east(), 2);
        helper.setBlock(START.east(2).east(), Blocks.STONE);

        var state = helper.getBlockState(START.east());
        helper.assertValueEqual(state.getValue(TubeBlock.SIDES.get(Direction.WEST)), TubeBlock.Link.BLOCK,
                "the vessel on one side is something to reach into");
        helper.assertValueEqual(state.getValue(TubeBlock.SIDES.get(Direction.EAST)), TubeBlock.Link.TUBE,
                "the tube on the other side is just more pipe");
        helper.assertValueEqual(state.getValue(TubeBlock.SIDES.get(Direction.UP)), TubeBlock.Link.NONE,
                "and the air above is nothing");

        helper.assertValueEqual(helper.getBlockState(START.east(2))
                        .getValue(TubeBlock.SIDES.get(Direction.EAST)), TubeBlock.Link.NONE,
                "plain stone is not something a tube reaches into");
        helper.succeed();
    }

    /**
     * Essentia crosses a run of pipe from a full vessel to an empty one.
     * <p>
     * Four tubes between them, so this is not two things happening to touch: it has to be carried.
     */
    @GameTest(template = TEMPLATE)
    public static void itCarriesAlongTheRun(GameTestHelper helper) {
        AlembicBlockEntity from = alembic(helper, START);
        pipe(helper, START.east(), 4);
        AlembicBlockEntity to = alembic(helper, START.east(5));

        for (int filled = 0; filled < 6; filled++) {
            from.accept(ModAspects.METAL);
        }
        helper.assertValueEqual(from.amount(), 6, "the first vessel was filled");
        helper.assertTrue(to.isEmpty(), "and the far one was not");

        // the tube beside the empty vessel is the one that pulls
        run(helper, START.east(4), 60);

        helper.assertTrue(!to.isEmpty(), "something reached the far end");
        helper.assertValueEqual(to.holding().orElseThrow().value(), ModAspects.METAL.value(),
                "and it is what set off");
        helper.assertValueEqual(from.amount() + to.amount(), 6, "nothing was made or lost on the way");
        helper.succeed();
    }

    /** A pipe with nowhere to put anything does not quietly swallow it. */
    @GameTest(template = TEMPLATE)
    public static void aPipeToNowhereTakesNothing(GameTestHelper helper) {
        AlembicBlockEntity from = alembic(helper, START);
        pipe(helper, START.east(), 3);
        from.accept(ModAspects.METAL);

        run(helper, START.east(2), 60);
        helper.assertValueEqual(from.amount(), 1, "it is still where it was");
        helper.succeed();
    }

    /**
     * A jar takes a pipe on its lid and nowhere else.
     * <p>
     * A tube beside one sees glass and treats it as nothing at all, so a wall of jars has to be piped along the
     * top rather than threaded through.
     */
    @GameTest(template = TEMPLATE)
    public static void aJarIsOnlyOpenAtTheTop(GameTestHelper helper) {
        helper.setBlock(START, ModBlocks.JAR.get());
        helper.setBlock(START.above(), ModBlocks.TUBE.get());
        helper.setBlock(START.east(), ModBlocks.TUBE.get());

        helper.assertValueEqual(helper.getBlockState(START.above())
                        .getValue(TubeBlock.SIDES.get(Direction.DOWN)), TubeBlock.Link.BLOCK,
                "the tube on the lid reaches in");
        helper.assertValueEqual(helper.getBlockState(START.east())
                        .getValue(TubeBlock.SIDES.get(Direction.WEST)), TubeBlock.Link.NONE,
                "the one against the glass does not");
        helper.succeed();
    }

    /** Essentia carried down a pipe and into a jar through its lid. */
    @GameTest(template = TEMPLATE)
    public static void aJarFillsThroughItsLid(GameTestHelper helper) {
        AlembicBlockEntity from = alembic(helper, START);
        pipe(helper, START.east(), 3);
        helper.setBlock(START.east(3).above(), ModBlocks.TUBE.get());
        helper.setBlock(START.east(3).above(2), ModBlocks.TUBE.get());
        helper.setBlock(START.east(4).above(2), ModBlocks.TUBE.get());
        helper.setBlock(START.east(4).above(), ModBlocks.JAR.get());

        JarBlockEntity jar = (JarBlockEntity) helper.getLevel()
                .getBlockEntity(helper.absolutePos(START.east(4).above()));
        for (int filled = 0; filled < 4; filled++) {
            from.accept(ModAspects.FIRE);
        }

        run(helper, START.east(4).above(2), 60);
        helper.assertTrue(jar.amount() > 0, "it came down the pipe and into the jar");
        helper.assertValueEqual(jar.holding().orElseThrow().value(), ModAspects.FIRE.value(),
                "and it is what set off");
        helper.succeed();
    }
}
