package me.moonscenty.alchemia.gametest;

import java.util.List;
import java.util.Set;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.research.HexGrid;
import me.moonscenty.alchemia.research.ModResearch;
import me.moonscenty.alchemia.research.NoteGeneration;
import me.moonscenty.alchemia.research.ResearchEntry;
import me.moonscenty.alchemia.research.ResearchNote;
import net.minecraft.core.Registry;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * A note has to be solvable the moment it is drawn, and there is no way to check that by hand for every seed. These
 * walk a spread of them instead.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class NoteTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final int SEEDS = 200;

    @GameTest(template = TEMPLATE)
    public static void aRingIsAsLongAsItsRadius(GameTestHelper helper) {
        for (int radius = 1; radius <= 4; radius++) {
            List<HexGrid.Hex> ring = HexGrid.ring(radius);
            helper.assertTrue(ring.size() == HexGrid.SIDES * radius,
                    "ring " + radius + " should hold " + (HexGrid.SIDES * radius) + " cells, held " + ring.size());
            for (HexGrid.Hex hex : ring) {
                helper.assertTrue(hex.distanceTo(HexGrid.CENTRE) == radius,
                        hex + " sits " + hex.distanceTo(HexGrid.CENTRE) + " out, not " + radius);
            }
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aFilledBoardIsTheCentredHexNumber(GameTestHelper helper) {
        for (int radius = 0; radius <= 4; radius++) {
            int expected = 1 + 3 * radius * (radius + 1);
            Set<HexGrid.Hex> cells = HexGrid.filled(radius);
            helper.assertTrue(cells.size() == expected,
                    "a board of radius " + radius + " should hold " + expected + " cells, held " + cells.size());
        }
        helper.succeed();
    }

    /** Endpoints spaced around the edge must never land on top of one another, or the puzzle starts solved. */
    @GameTest(template = TEMPLATE)
    public static void endpointsNeverShareACell(GameTestHelper helper) {
        RandomSource random = RandomSource.create(1234L);
        for (int seed = 0; seed < SEEDS; seed++) {
            for (int count = 2; count <= 6; count++) {
                List<HexGrid.Hex> ends = HexGrid.spaceAroundRing(3, count, random);
                helper.assertTrue(ends.size() == count, "asked for " + count + " ends, got " + ends.size());
                helper.assertTrue(Set.copyOf(ends).size() == count,
                        "two ends landed on the same cell: " + ends);
            }
        }
        helper.succeed();
    }

    /** However the sheet is torn, every pinned aspect has to keep somewhere to go. */
    @GameTest(template = TEMPLATE)
    public static void noDrawnNoteStrandsItsEndpoints(GameTestHelper helper) {
        Registry<ResearchEntry> entries = helper.getLevel().registryAccess().registryOrThrow(ModResearch.ENTRY_KEY);
        RandomSource random = RandomSource.create(4321L);

        for (ResourceLocation id : entries.keySet()) {
            ResearchEntry entry = entries.get(id);
            if (entry.requirements().isEmpty()) {
                continue;
            }
            for (int seed = 0; seed < SEEDS; seed++) {
                ResearchNote note = NoteGeneration.draw(id, entry, random);
                helper.assertTrue(note.pinnedCells().size() == entry.requirements().size(),
                        id + " pinned " + note.pinnedCells().size() + " aspects, wanted " + entry.requirements().size());
                for (ResearchNote.Cell pinned : note.pinnedCells()) {
                    long ways = pinned.at().neighbours().stream().filter(note::holds).count();
                    helper.assertTrue(ways >= 2, id + " stranded " + pinned.at() + " with " + ways + " ways out");
                }
            }
        }
        helper.succeed();
    }

    /** A note is no use if the reader cannot afford to cross it, so every primal is stocked. */
    @GameTest(template = TEMPLATE)
    public static void everyNoteComesWithSomethingToSpend(GameTestHelper helper) {
        Registry<ResearchEntry> entries = helper.getLevel().registryAccess().registryOrThrow(ModResearch.ENTRY_KEY);
        RandomSource random = RandomSource.create(99L);

        for (ResourceLocation id : entries.keySet()) {
            ResearchEntry entry = entries.get(id);
            if (entry.requirements().isEmpty()) {
                continue;
            }
            ResearchNote note = NoteGeneration.draw(id, entry, random);
            helper.assertTrue(note.budget().size() == 6, id + " should stock all six primals, stocked " + note.budget().size());
            helper.assertTrue(note.budget().total() > 0, id + " was drawn with nothing to spend");
            helper.assertTrue(!note.complete(), id + " was drawn already solved");
        }
        helper.succeed();
    }
}
