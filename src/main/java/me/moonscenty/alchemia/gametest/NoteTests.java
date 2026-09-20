package me.moonscenty.alchemia.gametest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.research.HexGrid;
import me.moonscenty.alchemia.research.ModResearch;
import me.moonscenty.alchemia.research.NoteGeneration;
import me.moonscenty.alchemia.research.NoteSolving;
import me.moonscenty.alchemia.research.ResearchEntry;
import me.moonscenty.alchemia.research.ResearchNote;
import net.minecraft.core.Holder;
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

    /** An aspect only holds to one it is made of, or one made of it. */
    @GameTest(template = TEMPLATE)
    public static void aspectsHoldOnlyToWhatTheyAreMadeOf(GameTestHelper helper) {
        helper.assertTrue(NoteSolving.linked(ModAspects.METAL, ModAspects.EARTH),
                "metal is made of earth, so the two should hold");
        helper.assertTrue(NoteSolving.linked(ModAspects.EARTH, ModAspects.METAL),
                "which way round they lie should not matter");
        helper.assertTrue(!NoteSolving.linked(ModAspects.EARTH, ModAspects.AIR),
                "two primals are made of nothing, so nothing holds them together");
        helper.succeed();
    }

    /** A board one aspect short of a chain is not solved; finishing the chain solves it. */
    @GameTest(template = TEMPLATE)
    public static void aChainHasToReachEveryPinnedAspect(GameTestHelper helper) {
        HexGrid.Hex left = new HexGrid.Hex(-1, 0);
        HexGrid.Hex right = new HexGrid.Hex(1, 0);
        List<ResearchNote.Cell> cells = List.of(
                ResearchNote.Cell.pinned(left, ModAspects.EARTH),
                ResearchNote.Cell.blank(HexGrid.CENTRE),
                ResearchNote.Cell.pinned(right, ModAspects.EARTH));
        ResearchNote open = new ResearchNote(ResourceLocation.parse("alchemia:test"),
                AspectList.of(ModAspects.METAL, 2), cells, false);
        helper.assertTrue(!NoteSolving.isSolved(open), "the two ends are not joined yet");

        ResearchNote joined = NoteSolving.place(open, HexGrid.CENTRE, Optional.of(ModAspects.METAL));
        helper.assertTrue(NoteSolving.isSolved(joined), "earth-metal-earth should join the two ends");
        helper.assertTrue(joined.budget().get(ModAspects.METAL) == 1, "laying one down should spend one");
        helper.succeed();
    }

    /** Laying an aspect that holds to nothing leaves the ends as far apart as they were. */
    @GameTest(template = TEMPLATE)
    public static void anUnrelatedAspectJoinsNothing(GameTestHelper helper) {
        List<ResearchNote.Cell> cells = List.of(
                ResearchNote.Cell.pinned(new HexGrid.Hex(-1, 0), ModAspects.EARTH),
                ResearchNote.Cell.blank(HexGrid.CENTRE),
                ResearchNote.Cell.pinned(new HexGrid.Hex(1, 0), ModAspects.EARTH));
        ResearchNote note = new ResearchNote(ResourceLocation.parse("alchemia:test"),
                AspectList.of(ModAspects.AIR, 1), cells, false);

        ResearchNote laid = NoteSolving.place(note, HexGrid.CENTRE, Optional.of(ModAspects.AIR));
        helper.assertTrue(!NoteSolving.isSolved(laid), "air holds to neither earth, so nothing is joined");
        helper.succeed();
    }

    /** What the reader may write on the sheet, and what they may not. */
    @GameTest(template = TEMPLATE)
    public static void theBoardRefusesWhatItShould(GameTestHelper helper) {
        HexGrid.Hex pinned = new HexGrid.Hex(-1, 0);
        List<ResearchNote.Cell> cells = List.of(
                ResearchNote.Cell.pinned(pinned, ModAspects.EARTH),
                ResearchNote.Cell.blank(HexGrid.CENTRE));
        ResearchNote note = new ResearchNote(ResourceLocation.parse("alchemia:test"),
                AspectList.of(ModAspects.METAL, 1), cells, false);
        helper.assertTrue(NoteSolving.check(note, HexGrid.CENTRE, Optional.of(ModAspects.METAL))
                == NoteSolving.Refusal.OK, "an aspect the sheet holds should be allowed");
        helper.assertTrue(NoteSolving.check(note, pinned, Optional.of(ModAspects.METAL))
                == NoteSolving.Refusal.PINNED, "the subject's own aspects are not the reader's to move");
        helper.assertTrue(NoteSolving.check(note, new HexGrid.Hex(9, 9), Optional.of(ModAspects.METAL))
                == NoteSolving.Refusal.NO_SPACE, "there is no such space on the sheet");
        helper.assertTrue(NoteSolving.check(note, HexGrid.CENTRE, Optional.of(ModAspects.AIR))
                == NoteSolving.Refusal.SPENT, "nothing of that is left on the sheet");
        helper.succeed();
    }

    /** Settling a solved note keeps the answer and rubs off the working. */
    @GameTest(template = TEMPLATE)
    public static void settlingKeepsTheChainAndRubsOffTheRest(GameTestHelper helper) {
        HexGrid.Hex stray = new HexGrid.Hex(0, -1);
        List<ResearchNote.Cell> cells = List.of(
                ResearchNote.Cell.pinned(new HexGrid.Hex(-1, 0), ModAspects.EARTH),
                new ResearchNote.Cell(HexGrid.CENTRE, Optional.of(ModAspects.METAL), false),
                ResearchNote.Cell.pinned(new HexGrid.Hex(1, 0), ModAspects.EARTH),
                new ResearchNote.Cell(stray, Optional.of(ModAspects.AIR), false));
        ResearchNote note = new ResearchNote(ResourceLocation.parse("alchemia:test"),
                AspectList.EMPTY, cells, false);
        helper.assertTrue(NoteSolving.isSolved(note), "the chain is already in place");

        ResearchNote settled = NoteSolving.settle(note);
        helper.assertTrue(settled.complete(), "a settled note is worked out");
        helper.assertTrue(settled.cellAt(HexGrid.CENTRE).orElseThrow().aspect().isPresent(),
                "the aspect the chain runs through should stay");
        helper.assertTrue(settled.cellAt(stray).orElseThrow().isEmpty(),
                "an aspect no chain runs through should be rubbed off");
        helper.succeed();
    }

    /** Every compound has to be reachable by mixing upwards from the primals, or a note could stock nothing useful. */
    @GameTest(template = TEMPLATE)
    public static void everyAspectCanBeMixedUpFromThePrimals(GameTestHelper helper) {
        Registry<Aspect> aspects = ModAspects.REGISTRY;
        Set<Aspect> reachable = new HashSet<>();
        ModAspects.primals().forEach(primal -> reachable.add(primal.value()));

        boolean grew = true;
        while (grew) {
            grew = false;
            for (Aspect aspect : aspects) {
                if (reachable.contains(aspect)) {
                    continue;
                }
                boolean fromReachable = aspect.components()
                        .map(parts -> parts.stream().allMatch(part -> reachable.contains(part.value())))
                        .orElse(false);
                if (fromReachable) {
                    reachable.add(aspect);
                    grew = true;
                }
            }
        }

        List<String> stranded = new ArrayList<>();
        for (Aspect aspect : aspects) {
            if (!reachable.contains(aspect)) {
                stranded.add(aspect.tag());
            }
        }
        helper.assertTrue(stranded.isEmpty(), "these cannot be mixed up from the primals: " + stranded);
        helper.succeed();
    }

    /**
     * A pinned aspect the reader can never lay anything beside would make a note impossible, so every one of them
     * has to have at least one aspect that holds to it.
     */
    @GameTest(template = TEMPLATE)
    public static void everyPinnedAspectHasSomethingThatHoldsToIt(GameTestHelper helper) {
        Registry<ResearchEntry> entries = helper.getLevel().registryAccess().registryOrThrow(ModResearch.ENTRY_KEY);
        List<String> stranded = new ArrayList<>();

        for (ResourceLocation id : entries.keySet()) {
            for (Holder<Aspect> pinned : entries.get(id).requirements().sortedByName()) {
                boolean anyHolds = ModAspects.REGISTRY.holders()
                        .anyMatch(other -> NoteSolving.linked(pinned, (Holder<Aspect>) other));
                if (!anyHolds) {
                    stranded.add(id + " -> " + pinned.value().tag());
                }
            }
        }
        helper.assertTrue(stranded.isEmpty(), "nothing holds to these pinned aspects: " + stranded);
        helper.succeed();
    }
}
