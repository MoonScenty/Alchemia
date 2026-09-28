package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.OnewayTubeBlock;
import me.moonscenty.alchemia.block.TubeBlock;
import me.moonscenty.alchemia.block.ValveTubeBlock;
import me.moonscenty.alchemia.block.entity.AlembicBlockEntity;
import me.moonscenty.alchemia.block.entity.BufferTubeBlockEntity;
import me.moonscenty.alchemia.block.entity.FilterTubeBlockEntity;
import me.moonscenty.alchemia.block.entity.TubeBlockEntity;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The five kinds of pipe that are not plain pipe.
 * <p>
 * Every one of them is the same length of brass to look at, so what tells them apart is only what they do to what
 * passes through, and that is what is written down here. Each test lays a run from a full vessel to an empty one
 * with the odd length in the middle, and watches what arrives.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class TubeKindTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final BlockPos START = new BlockPos(2, 1, 2);
    /** Eleven ticks to a turn: ten of waiting and the one it moves on. */
    private static final int TURN = 11;

    private static AlembicBlockEntity alembic(GameTestHelper helper, BlockPos at) {
        helper.setBlock(at, ModBlocks.ALEMBIC.get());
        return (AlembicBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(at));
    }

    /** A run of pipe laid east from a spot, of whatever lengths are handed in. */
    private static void pipe(GameTestHelper helper, BlockPos from, Block... lengths) {
        for (int step = 0; step < lengths.length; step++) {
            helper.setBlock(from.east(step), lengths[step]);
        }
    }

    private static void run(GameTestHelper helper, BlockPos at, int ticks) {
        BlockPos where = helper.absolutePos(at);
        TubeBlockEntity tube = (TubeBlockEntity) helper.getLevel().getBlockEntity(where);
        for (int tick = 0; tick < ticks; tick++) {
            TubeBlockEntity.tick(helper.getLevel(), where, helper.getLevel().getBlockState(where), tube);
        }
    }

    private static void fill(AlembicBlockEntity vessel, int points) {
        for (int one = 0; one < points; one++) {
            vessel.accept(ModAspects.METAL);
        }
    }

    /** A shut valve is a wall; the same valve opened is pipe again. */
    @GameTest(template = TEMPLATE)
    public static void aShutValveStopsTheRun(GameTestHelper helper) {
        AlembicBlockEntity from = alembic(helper, START);
        pipe(helper, START.east(), ModBlocks.TUBE.get(), ModBlocks.TUBE_VALVE.get(), ModBlocks.TUBE.get());
        AlembicBlockEntity to = alembic(helper, START.east(4));
        fill(from, 4);

        BlockPos valve = START.east(2);
        helper.setBlock(valve, helper.getBlockState(valve).setValue(ValveTubeBlock.OPEN, false));
        run(helper, START.east(3), 4 * TURN);
        helper.assertTrue(to.isEmpty(), "nothing gets past a shut valve");

        helper.setBlock(valve, helper.getBlockState(valve).setValue(ValveTubeBlock.OPEN, true));
        run(helper, START.east(3), 4 * TURN);
        helper.assertTrue(!to.isEmpty(), "and it flows again once it is opened");
        helper.succeed();
    }

    /**
     * A handle is not a socket. Nothing joins to the side a valve's handle stands on, from either end.
     * <p>
     * Both ends have to agree about it, or a tube laid against the handle would grow an arm into it and the pipe
     * would look joined while nothing went through.
     */
    @GameTest(template = TEMPLATE)
    public static void aValveHandleIsNotASocket(GameTestHelper helper) {
        helper.setBlock(START, ModBlocks.TUBE_VALVE.get().defaultBlockState()
                .setValue(ValveTubeBlock.FACING, Direction.EAST));
        helper.setBlock(START.east(), ModBlocks.TUBE.get());
        helper.setBlock(START.west(), ModBlocks.TUBE.get());

        helper.assertValueEqual(helper.getBlockState(START).getValue(TubeBlock.SIDES.get(Direction.EAST)),
                TubeBlock.Link.NONE, "the valve keeps that side for its handle");
        helper.assertValueEqual(helper.getBlockState(START).getValue(TubeBlock.SIDES.get(Direction.WEST)),
                TubeBlock.Link.TUBE, "and joins on the other side as any pipe would");
        helper.assertValueEqual(helper.getBlockState(START.east())
                        .getValue(TubeBlock.SIDES.get(Direction.WEST)), TubeBlock.Link.NONE,
                "and the pipe against the handle knows better than to reach for it");
        helper.succeed();
    }

    /** A signal arriving shuts a valve, and the signal leaving opens it again. */
    @GameTest(template = TEMPLATE)
    public static void redstoneWorksTheValve(GameTestHelper helper) {
        BlockPos valve = START;
        helper.setBlock(valve, ModBlocks.TUBE_VALVE.get());
        helper.assertTrue(helper.getBlockState(valve).getValue(ValveTubeBlock.OPEN), "it is laid open");

        helper.setBlock(valve.above(), Blocks.REDSTONE_BLOCK);
        helper.assertTrue(!helper.getBlockState(valve).getValue(ValveTubeBlock.OPEN), "power shuts it");

        helper.setBlock(valve.above(), Blocks.AIR);
        helper.assertTrue(helper.getBlockState(valve).getValue(ValveTubeBlock.OPEN), "and losing the power opens it");
        helper.succeed();
    }

    /** A one-way length carries only the way it is pointed. */
    @GameTest(template = TEMPLATE)
    public static void aOneWayCarriesOneWay(GameTestHelper helper) {
        AlembicBlockEntity from = alembic(helper, START);
        pipe(helper, START.east(), ModBlocks.TUBE.get(), ModBlocks.TUBE_ONEWAY.get(), ModBlocks.TUBE.get());
        AlembicBlockEntity to = alembic(helper, START.east(4));
        fill(from, 4);

        BlockPos oneway = START.east(2);
        BlockState against = helper.getBlockState(oneway).setValue(OnewayTubeBlock.FACING, Direction.WEST);
        helper.setBlock(oneway, against);
        run(helper, START.east(3), 4 * TURN);
        helper.assertTrue(to.isEmpty(), "nothing goes against a one-way");

        helper.setBlock(oneway, against.setValue(OnewayTubeBlock.FACING, Direction.EAST));
        run(helper, START.east(3), 4 * TURN);
        helper.assertTrue(!to.isEmpty(), "and it flows the way the length points");
        helper.succeed();
    }

    /** A filter passes the one aspect it was told and turns the rest back. */
    @GameTest(template = TEMPLATE)
    public static void aFilterLetsOneAspectBy(GameTestHelper helper) {
        AlembicBlockEntity from = alembic(helper, START);
        pipe(helper, START.east(), ModBlocks.TUBE.get(), ModBlocks.TUBE_FILTER.get(), ModBlocks.TUBE.get());
        AlembicBlockEntity to = alembic(helper, START.east(4));
        fill(from, 4);

        FilterTubeBlockEntity filter = (FilterTubeBlockEntity) helper.getLevel()
                .getBlockEntity(helper.absolutePos(START.east(2)));
        filter.only(ModAspects.FIRE);
        run(helper, START.east(3), 4 * TURN);
        helper.assertTrue(to.isEmpty(), "a filter set to one thing turns everything else back");

        filter.only(ModAspects.METAL);
        run(helper, START.east(3), 4 * TURN);
        helper.assertTrue(!to.isEmpty(), "and passes what it was set to");
        helper.succeed();
    }

    /** Two filters that disagree are a wall between them, not a choice. */
    @GameTest(template = TEMPLATE)
    public static void twoFiltersThatDisagreePassNothing(GameTestHelper helper) {
        AlembicBlockEntity from = alembic(helper, START);
        pipe(helper, START.east(), ModBlocks.TUBE_FILTER.get(), ModBlocks.TUBE_FILTER.get(), ModBlocks.TUBE.get());
        AlembicBlockEntity to = alembic(helper, START.east(4));
        fill(from, 4);

        ((FilterTubeBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(START.east())))
                .only(ModAspects.METAL);
        ((FilterTubeBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(START.east(2))))
                .only(ModAspects.FIRE);

        run(helper, START.east(3), 4 * TURN);
        helper.assertTrue(to.isEmpty(), "what one lets by the other turns back");
        helper.succeed();
    }

    /** A restrict halves what gets down a run: five turns in ten carry nothing. */
    @GameTest(template = TEMPLATE)
    public static void aRestrictHalvesTheFlow(GameTestHelper helper) {
        AlembicBlockEntity from = alembic(helper, START);
        pipe(helper, START.east(), ModBlocks.TUBE.get(), ModBlocks.TUBE_RESTRICT.get(), ModBlocks.TUBE.get());
        AlembicBlockEntity to = alembic(helper, START.east(4));
        fill(from, 10);

        run(helper, START.east(3), 10 * TURN);
        helper.assertValueEqual(to.amount(), 5, "ten turns of a narrowed run carry five");
        helper.succeed();
    }

    /** A buffer draws into itself whatever the run has to spare, up to the little it holds. */
    @GameTest(template = TEMPLATE)
    public static void aBufferFillsItself(GameTestHelper helper) {
        AlembicBlockEntity from = alembic(helper, START);
        pipe(helper, START.east(), ModBlocks.TUBE.get(), ModBlocks.TUBE_BUFFER.get());
        fill(from, 12);

        run(helper, START.east(2), 12 * TURN);
        BufferTubeBlockEntity buffer = (BufferTubeBlockEntity) helper.getLevel()
                .getBlockEntity(helper.absolutePos(START.east(2)));
        helper.assertValueEqual(buffer.held().total(), BufferTubeBlockEntity.CAPACITY, "it filled itself and no more");
        helper.assertValueEqual(from.amount(), 12 - BufferTubeBlockEntity.CAPACITY, "and it came out of the vessel");
        helper.succeed();
    }

    /** What a buffer is sitting on can be drawn back out of it. */
    @GameTest(template = TEMPLATE)
    public static void aBufferGivesItBack(GameTestHelper helper) {
        pipe(helper, START, ModBlocks.TUBE_BUFFER.get(), ModBlocks.TUBE.get());
        AlembicBlockEntity to = alembic(helper, START.east(2));

        BufferTubeBlockEntity buffer = (BufferTubeBlockEntity) helper.getLevel()
                .getBlockEntity(helper.absolutePos(START));
        for (int one = 0; one < 4; one++) {
            buffer.accept(ModAspects.METAL);
        }

        run(helper, START.east(), 4 * TURN);
        helper.assertTrue(to.amount() > 0, "the vessel drew on the buffer");
        helper.assertValueEqual(buffer.held().total() + to.amount(), 4, "nothing was made or lost");
        helper.succeed();
    }
}
