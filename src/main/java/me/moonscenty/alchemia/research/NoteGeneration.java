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

public final class NoteGeneration {
    private static final int BASE_RADIUS = 1;
    private static final int TEARS_PER_COMPLEXITY = 2;
    private static final int MIN_WAYS_OUT = 2;
    // the sheet holds this much more than the found answer needs, leaving room for detours
    private static final float SLACK = 1.5F;
    // before giving up on tearing and handing out an untorn board
    private static final int ATTEMPTS = 8;

    private NoteGeneration() {
    }

    public static ResearchNote draw(ResourceLocation research, ResearchEntry entry, RandomSource random) {
        int radius = BASE_RADIUS + Math.min(3, entry.complexity());
        List<Holder<Aspect>> wanted = entry.requirements().sortedByName();

        // a board that can't be crossed at all is laid out and torn again from scratch
        Map<HexGrid.Hex, ResearchNote.Cell> whole = null;
        ResearchNote note = null;
        for (int attempt = 0; attempt < ATTEMPTS && note == null; attempt++) {
            whole = laidOut(radius, wanted, random);
            Map<HexGrid.Hex, ResearchNote.Cell> board = new LinkedHashMap<>(whole);
            // original: the simplest notes are never torn
            tear(board, entry.complexity() > 1 ? entry.complexity() * TEARS_PER_COMPLEXITY : 0, random);
            note = affordable(research, board, wanted.size(), radius, random);
        }
        if (note == null) {
            // fall back to an untorn board: wide open, but solvable
            note = affordable(research, whole, wanted.size(), radius, random);
        }
        return note != null ? note
                : new ResearchNote(research, budget(wanted.size(), radius, random), List.copyOf(whole.values()), false);
    }

    private static Map<HexGrid.Hex, ResearchNote.Cell> laidOut(int radius, List<Holder<Aspect>> wanted,
            RandomSource random) {
        Map<HexGrid.Hex, ResearchNote.Cell> board = new LinkedHashMap<>();
        for (HexGrid.Hex hex : HexGrid.filled(radius)) {
            board.put(hex, ResearchNote.Cell.blank(hex));
        }
        List<HexGrid.Hex> ends = HexGrid.spaceAroundRing(radius, wanted.size(), random);
        for (int index = 0; index < ends.size(); index++) {
            board.put(ends.get(index), ResearchNote.Cell.pinned(ends.get(index), wanted.get(index)));
        }
        return board;
    }

    // the budget is sized from an actual answer, so a board forcing dear compounds stays affordable;
    // null when no chain crosses the board
    private static ResearchNote affordable(ResourceLocation research, Map<HexGrid.Hex, ResearchNote.Cell> board,
            int ends, int radius, RandomSource random) {
        ResearchNote note = new ResearchNote(research, budget(ends, radius, random), List.copyOf(board.values()), false);
        List<NoteSolving.Placement> answer = NoteSolving.solve(ModAspects.REGISTRY, note).orElse(null);
        if (answer == null) {
            return null;
        }

        AspectList needed = NoteSolving.costOf(answer).scale(SLACK);
        AspectList budget = note.budget();
        for (Holder<Aspect> primal : needed.sortedByName()) {
            budget = budget.mergeMax(primal, needed.get(primal));
        }
        return new ResearchNote(research, budget, note.cells(), false);
    }

    // gives up after a while: a small crowded board may have nothing safe left to tear
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

    private static AspectList budget(int ends, int radius, RandomSource random) {
        AspectList budget = AspectList.EMPTY;
        for (Holder<Aspect> primal : ModAspects.primals()) {
            budget = budget.add(primal, ends + radius + random.nextInt(radius));
        }
        return budget;
    }
}
