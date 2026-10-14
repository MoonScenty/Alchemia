package me.moonscenty.alchemia.gametest;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.outer.Cell;
import me.moonscenty.alchemia.outer.Labyrinth;
import me.moonscenty.alchemia.outer.MazeBuilder;
import me.moonscenty.alchemia.outer.OuterLands;
import me.moonscenty.alchemia.outer.OuterLandsChunkGenerator;
import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The Outer Lands: their mazes, and how a maze is carved out of their bedrock. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class OuterLandsTests {
    private static final String TEMPLATE = "empty_32x40x32";

    /**
     * A maze laid out for an altar has one portal, one key room and a great hall of four cells with one door, and
     * every cell of it can be walked to from the portal.
     */
    @GameTest(template = TEMPLATE)
    public static void aMazeHangsTogether(GameTestHelper helper) {
        Labyrinth labyrinth = new Labyrinth();
        BlockPos altar = new BlockPos(16 * 20000, 64, 16 * 20000);
        helper.assertFalse(labyrinth.ensureMaze(altar, helper.getLevel().random), "there was no maze yet");
        helper.assertTrue(labyrinth.ensureMaze(altar, helper.getLevel().random), "and now there is one");
        int portals = 0;
        int keys = 0;
        int hall = 0;
        int doors = 0;
        Set<Long> cells = new HashSet<>();
        for (int x = 20000 - 20; x <= 20000 + 20; x++) {
            for (int z = 20000 - 20; z <= 20000 + 20; z++) {
                Cell cell = labyrinth.get(x, z);
                if (cell == null) {
                    continue;
                }
                cells.add(net.minecraft.world.level.ChunkPos.asLong(x, z));
                portals += cell.feature == Cell.PORTAL ? 1 : 0;
                keys += cell.feature == Cell.KEY_ROOM ? 1 : 0;
                if (cell.isBossRoom()) {
                    hall++;
                    doors += cell.exits();
                }
            }
        }
        helper.assertTrue(portals == 1 && labyrinth.get(20000, 20000).feature == Cell.PORTAL,
                "one portal, in the altar's chunk");
        helper.assertTrue(keys == 1, "one key room: " + keys);
        helper.assertTrue(hall == 4 && doors == 1, "a hall of four with one door: " + hall + ", " + doors);
        Set<Long> reached = new HashSet<>();
        Deque<int[]> open = new ArrayDeque<>();
        open.add(new int[] {20000, 20000});
        reached.add(net.minecraft.world.level.ChunkPos.asLong(20000, 20000));
        while (!open.isEmpty()) {
            int[] at = open.poll();
            Cell cell = labyrinth.get(at[0], at[1]);
            int[][] steps = {{0, -1, cell.north ? 1 : 0}, {0, 1, cell.south ? 1 : 0}, {1, 0, cell.east ? 1 : 0},
                    {-1, 0, cell.west ? 1 : 0}};
            for (int[] step : steps) {
                long next = net.minecraft.world.level.ChunkPos.asLong(at[0] + step[0], at[1] + step[1]);
                if (step[2] == 1 && reached.add(next)) {
                    open.add(new int[] {at[0] + step[0], at[1] + step[1]});
                }
            }
        }
        long hallReached = cells.stream().filter(reached::contains)
                .filter(c -> labyrinth.get(new net.minecraft.world.level.ChunkPos(c).x,
                        new net.minecraft.world.level.ChunkPos(c).z).isBossRoom()).count();
        helper.assertTrue(hallReached >= 1, "the hall's door is reached from the portal");
        helper.succeed();
    }

    /**
     * The Outer Lands' kind of dimension and their generator are there, and the chunk a maze's portal is in is
     * carved: the cap, the portal over it and the obelisk over that.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void theOuterLandsAreCarved(GameTestHelper helper) {
        // a test server builds its world from its preset alone, so the stem itself is not to be seen here
        helper.assertTrue(helper.getLevel().registryAccess().registryOrThrow(Registries.DIMENSION_TYPE)
                .containsKey(OuterLands.TYPE), "the Outer Lands' kind of dimension is there");
        helper.assertTrue(net.minecraft.core.registries.BuiltInRegistries.CHUNK_GENERATOR.get(Alchemia.id("outer_lands"))
                == OuterLandsChunkGenerator.CODEC, "and their generator");
        Labyrinth labyrinth = new Labyrinth();
        int cx = 30000;
        int cz = 30000;
        labyrinth.ensureMaze(new BlockPos(cx * 16 + 5, 70, cz * 16 + 5), helper.getLevel().random);
        ServerLevel level = helper.getLevel();
        level.getChunk(cx, cz);
        MazeBuilder.generate(level, labyrinth, cx, cz);
        int x = cx * 16 + 8;
        int z = cz * 16 + 8;
        int y = MazeBuilder.Y;
        helper.assertTrue(level.getBlockState(new BlockPos(x, y + 2, z)).is(ModBlocks.ELDRITCH_CAP.get()),
                "the cap: " + level.getBlockState(new BlockPos(x, y + 2, z)));
        helper.assertTrue(level.getBlockState(new BlockPos(x, y + 3, z)).is(ModBlocks.ELDRITCH_PORTAL.get()),
                "the way out over it");
        helper.assertTrue(level.getBlockState(new BlockPos(x, y + 4, z)).is(ModBlocks.ELDRITCH_OBELISK.get()),
                "and the obelisk over that");
        helper.assertTrue(level.getBlockState(new BlockPos(x, y + 1, z)).is(ModBlocks.ANCIENT_TILE.get()),
                "on a tiled floor");
        helper.succeed();
    }
}
