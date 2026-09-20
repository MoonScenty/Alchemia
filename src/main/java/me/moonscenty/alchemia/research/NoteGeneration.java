package me.moonscenty.alchemia.research;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.registry.ModAspects;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

/**
 * Draws up a fresh note for a piece of research.
 * <p>
 * The board grows with the subject: a harder one is drawn wider and has more of the sheet torn away, which leaves
 * fewer ways across and forces the reader to think about which aspects to spend.
 */
public final class NoteGeneration {
    /** Even the simplest subject gets a ring around the middle to work in. */
    private static final int BASE_RADIUS = 1;
    /** How many cells are torn out per point of difficulty. */
    private static final int TEARS_PER_COMPLEXITY = 2;
    /** A pinned aspect needs somewhere to go, so it is never left with fewer neighbours than this. */
    private static final int MIN_WAYS_OUT = 2;

    private NoteGeneration() {
    }

    public static ResearchNote draw(ResourceLocation research, ResearchEntry entry, RandomSource random) {
        int radius = BASE_RADIUS + Math.min(3, entry.complexity());
        List<Holder<Aspect>> wanted = entry.requirements().sortedByName();

        Map<HexGrid.Hex, ResearchNote.Cell> board = new LinkedHashMap<>();
        for (HexGrid.Hex hex : HexGrid.filled(radius)) {
            board.put(hex, ResearchNote.Cell.blank(hex));
        }

        List<HexGrid.Hex> ends = HexGrid.spaceAroundRing(radius, wanted.size(), random);
        for (int index = 0; index < ends.size(); index++) {
            board.put(ends.get(index), ResearchNote.Cell.pinned(ends.get(index), wanted.get(index)));
        }

        tear(board, entry.complexity() * TEARS_PER_COMPLEXITY, random);
        return new ResearchNote(research, budget(wanted.size(), radius, random), List.copyOf(board.values()), false);
    }

    /**
     * Tears blank cells out of the sheet, never one that would leave a pinned aspect with too few ways out. Giving up
     * after a while rather than looping, since a small crowded board may have nothing left it is safe to take.
     */
    private static void tear(Map<HexGrid.Hex, ResearchNote.Cell> board, int count, RandomSource random) {
        List<HexGrid.Hex> candidates = new ArrayList<>(board.keySet());
        int attempts = candidates.size() * 4;

        while (count > 0 && attempts-- > 0 && !candidates.isEmpty()) {
            HexGrid.Hex hex = candidates.get(random.nextInt(candidates.size()));
            ResearchNote.Cell cell = board.get(hex);
            if (cell == null || cell.pinned() || hex.equals(HexGrid.CENTRE)) {
                continue;
            }

            board.remove(hex);
            if (strandsAnEnd(board)) {
                board.put(hex, cell);
                continue;
            }
            candidates.remove(hex);
            count--;
        }
    }

    /** True if any pinned aspect has been cut off from the rest of the board. */
    private static boolean strandsAnEnd(Map<HexGrid.Hex, ResearchNote.Cell> board) {
        for (ResearchNote.Cell cell : board.values()) {
            if (!cell.pinned()) {
                continue;
            }
            long ways = cell.at().neighbours().stream().filter(board::containsKey).count();
            if (ways < MIN_WAYS_OUT) {
                return true;
            }
        }
        return false;
    }

    /**
     * What the reader has to spend. Every primal is offered, in enough quantity that the board can be crossed without
     * the count itself being the puzzle.
     */
    private static AspectList budget(int ends, int radius, RandomSource random) {
        AspectList budget = AspectList.EMPTY;
        for (Holder<Aspect> primal : ModAspects.primals()) {
            budget = budget.add(primal, ends + radius + random.nextInt(radius));
        }
        return budget;
    }
}
