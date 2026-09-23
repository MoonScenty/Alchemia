package me.moonscenty.alchemia.research;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;

/**
 * Working a note out.
 * <p>
 * Two aspects lying next to each other hold together only when one is made partly of the other, so a run across the
 * board is a chain of reasoning: this comes from that, which comes from the other. The note is solved once every
 * aspect the subject pinned down is part of one such chain.
 * <p>
 * All of this is decided without a player or a level, so it can be exercised on its own.
 */
public final class NoteSolving {
    private NoteSolving() {
    }

    /** Whether two aspects hold together, which is to say one is made partly of the other. */
    public static boolean linked(Holder<Aspect> one, Holder<Aspect> other) {
        return one.value().hasComponent(other) || other.value().hasComponent(one);
    }

    /** Why an aspect could not be laid down, or that it can. */
    public enum Refusal {
        OK,
        /** The note is already worked out, so there is nothing left to do to it. */
        SOLVED,
        /** There is no space there: either off the board, or a tear in the sheet. */
        NO_SPACE,
        /** The subject pinned that one down; it is not the reader's to move. */
        PINNED,
        /** None of that aspect is left on the sheet to spend. */
        SPENT,
        /** Those two make nothing between them. */
        NO_SUCH_MIX;

        public boolean allowed() {
            return this == OK;
        }
    }

    /**
     * What the sheet holds is the whole of what may be written on it. There is deliberately no check that the reader
     * has met the aspect: the pool starts as the six primals and only grows by mixing, so a reader can only ever lay
     * down something the sheet itself gave them. Gating on what they have met besides would leave notes that cannot
     * be worked out at all, since the aspects a chain passes through are rarely ones the subject named.
     */
    public static Refusal check(ResearchNote note, HexGrid.Hex at, Optional<Holder<Aspect>> aspect) {
        if (note.complete()) {
            return Refusal.SOLVED;
        }
        ResearchNote.Cell cell = note.cellAt(at).orElse(null);
        if (cell == null) {
            return Refusal.NO_SPACE;
        }
        if (cell.pinned()) {
            return Refusal.PINNED;
        }
        if (aspect.isEmpty()) {
            return Refusal.OK;
        }
        return note.budget().get(aspect.get()) < 1 ? Refusal.SPENT : Refusal.OK;
    }

    /** The aspect made of exactly these two, whichever way round they are named. */
    public static Optional<Holder<Aspect>> mixOf(Registry<Aspect> aspects, Holder<Aspect> one, Holder<Aspect> other) {
        return aspects.holders()
                .filter(holder -> holder.value().hasComponent(one) && holder.value().hasComponent(other))
                .filter(holder -> one.value() != other.value()
                        || holder.value().components().map(parts -> parts.get(0).value() == parts.get(1).value()).orElse(false))
                .map(holder -> (Holder<Aspect>) holder)
                .findFirst();
    }

    public static Refusal checkMix(ResearchNote note, Holder<Aspect> one, Holder<Aspect> other,
            Optional<Holder<Aspect>> result) {
        if (note.complete()) {
            return Refusal.SOLVED;
        }
        if (result.isEmpty()) {
            return Refusal.NO_SUCH_MIX;
        }
        int needed = one.value() == other.value() ? 2 : 1;
        if (note.budget().get(one) < needed || note.budget().get(other) < needed) {
            return Refusal.SPENT;
        }
        return Refusal.OK;
    }

    /** Spends one of each and puts what they make back on the sheet. */
    public static ResearchNote mix(ResearchNote note, Holder<Aspect> one, Holder<Aspect> other, Holder<Aspect> result) {
        AspectList budget = note.budget().reduce(one, 1).reduce(other, 1).add(result, 1);
        return new ResearchNote(note.research(), budget, note.cells(), note.complete());
    }

    /**
     * The note with one cell rewritten. Laying an aspect down spends one from the sheet; lifting one back off does
     * not hand it back, so a guess costs something.
     */
    public static ResearchNote place(ResearchNote note, HexGrid.Hex at, Optional<Holder<Aspect>> aspect) {
        List<ResearchNote.Cell> cells = new ArrayList<>(note.cells().size());
        for (ResearchNote.Cell cell : note.cells()) {
            cells.add(cell.at().equals(at) ? new ResearchNote.Cell(at, aspect, false) : cell);
        }

        AspectList budget = aspect.map(held -> note.budget().reduce(held, 1)).orElse(note.budget());
        return new ResearchNote(note.research(), budget, List.copyOf(cells), note.complete());
    }

    /** Whether every pinned aspect now hangs off the same chain. */
    public static boolean isSolved(ResearchNote note) {
        Set<HexGrid.Hex> pinned = new HashSet<>();
        note.pinnedCells().forEach(cell -> pinned.add(cell.at()));
        if (pinned.size() <= 1) {
            return !pinned.isEmpty();
        }
        return reach(note, note.pinnedCells().getFirst().at()).containsAll(pinned);
    }

    /**
     * Every cell that can be walked to from a starting one, stepping only between aspects that hold together. The
     * start is included whether or not anything leads out of it.
     */
    public static Set<HexGrid.Hex> reach(ResearchNote note, HexGrid.Hex from) {
        Set<HexGrid.Hex> seen = new HashSet<>();
        Deque<HexGrid.Hex> queue = new ArrayDeque<>();
        seen.add(from);
        queue.add(from);

        while (!queue.isEmpty()) {
            HexGrid.Hex here = queue.poll();
            Holder<Aspect> held = note.cellAt(here).flatMap(ResearchNote.Cell::aspect).orElse(null);
            if (held == null) {
                continue;
            }
            for (HexGrid.Hex next : here.neighbours()) {
                if (seen.contains(next)) {
                    continue;
                }
                Holder<Aspect> there = note.cellAt(next).flatMap(ResearchNote.Cell::aspect).orElse(null);
                if (there != null && linked(held, there)) {
                    seen.add(next);
                    queue.add(next);
                }
            }
        }
        return seen;
    }


    // --- working one out ------------------------------------------------------

    /** One aspect written into one cell. */
    public record Placement(HexGrid.Hex at, Holder<Aspect> aspect) {
    }

    /**
     * What it takes off the sheet to end up with one of an aspect: the aspect itself if it is primal, otherwise
     * everything its parts take, since a compound is mixed up from them.
     */
    public static AspectList primalCost(Holder<Aspect> aspect) {
        // the search asks this of every aspect at every step, and the answer never changes
        return COSTS.computeIfAbsent(aspect, held -> Optional.ofNullable(primalCost(held, new HashSet<>()))).orElse(null);
    }

    private static final Map<Holder<Aspect>, Optional<AspectList>> COSTS = new ConcurrentHashMap<>();

    private static AspectList primalCost(Holder<Aspect> aspect, Set<Holder<Aspect>> seen) {
        if (aspect.value().isPrimal()) {
            return AspectList.of(aspect, 1);
        }
        // a compound that somehow lists itself among its parts would never come out, so it costs nothing knowable
        if (!seen.add(aspect)) {
            return null;
        }
        AspectList total = AspectList.EMPTY;
        for (Holder<Aspect> part : aspect.value().components().orElse(List.of())) {
            AspectList theirs = primalCost(part, seen);
            if (theirs == null) {
                return null;
            }
            total = total.add(theirs);
        }
        seen.remove(aspect);
        return total;
    }

    /**
     * A cheap way of working a note out: which aspect goes in which cell so that every pinned one ends up on the
     * same chain.
     * <p>
     * Pinned cells are joined to the chain one at a time, each by the cheapest run of cells that reaches it, cost
     * being the primals the aspects along it take to mix up. That is not provably the cheapest answer overall, but
     * it is an answer, which is what the drawing of a note needs in order to promise there is one.
     *
     * @return the placements, or nothing if no run of aspects joins them at all
     */
    public static Optional<List<Placement>> solve(Registry<Aspect> aspects, ResearchNote note) {
        List<ResearchNote.Cell> pinned = note.pinnedCells();
        if (pinned.size() <= 1) {
            return Optional.of(List.of());
        }

        List<Placement> answer = new ArrayList<>();
        ResearchNote working = note;
        Set<HexGrid.Hex> chain = new HashSet<>();
        chain.add(pinned.getFirst().at());

        for (int index = 1; index < pinned.size(); index++) {
            List<Placement> leg = cheapestRun(aspects, working, chain, pinned.get(index).at());
            if (leg == null) {
                return Optional.empty();
            }
            for (Placement step : leg) {
                working = place(working, step.at(), Optional.of(step.aspect()));
            }
            answer.addAll(leg);
            chain = reach(working, pinned.getFirst().at());
            if (!chain.contains(pinned.get(index).at())) {
                return Optional.empty();
            }
        }
        return isSolved(working) ? Optional.of(List.copyOf(answer)) : Optional.empty();
    }

    /** What a whole answer costs off the sheet. */
    public static AspectList costOf(List<Placement> answer) {
        AspectList total = AspectList.EMPTY;
        for (Placement step : answer) {
            AspectList cost = primalCost(step.aspect());
            if (cost != null) {
                total = total.add(cost);
            }
        }
        return total;
    }

    /** Where a search has got to: a cell and what was written in it, since that decides what may come next. */
    private record Reached(HexGrid.Hex at, Holder<Aspect> aspect) {
    }

    /**
     * The cheapest run of blank cells from anywhere on the chain to a pinned cell, every step holding to the one
     * before it. Searched over cell and aspect together and by cost rather than by length, so a long run of cheap
     * aspects is preferred to a short run of dear ones.
     */
    private static List<Placement> cheapestRun(Registry<Aspect> aspects, ResearchNote note, Set<HexGrid.Hex> chain,
            HexGrid.Hex target) {
        Holder<Aspect> goal = note.cellAt(target).flatMap(ResearchNote.Cell::aspect).orElse(null);
        if (goal == null) {
            return null;
        }
        List<Holder<Aspect>> writable = aspects.holders()
                .map(holder -> (Holder<Aspect>) holder)
                .filter(holder -> primalCost(holder) != null)
                .toList();

        Map<Reached, Integer> best = new HashMap<>();
        Map<Reached, Reached> cameFrom = new HashMap<>();
        PriorityQueue<Reached> queue = new PriorityQueue<>(Comparator.comparingInt(best::get));

        for (HexGrid.Hex at : chain) {
            Holder<Aspect> held = note.cellAt(at).flatMap(ResearchNote.Cell::aspect).orElse(null);
            if (held != null) {
                Reached start = new Reached(at, held);
                best.put(start, 0);
                cameFrom.put(start, null);
                queue.add(start);
            }
        }

        while (!queue.isEmpty()) {
            Reached here = queue.poll();
            int spent = best.get(here);
            for (HexGrid.Hex next : here.at().neighbours()) {
                ResearchNote.Cell cell = note.cellAt(next).orElse(null);
                if (cell == null) {
                    continue;
                }
                if (next.equals(target)) {
                    if (linked(here.aspect(), goal)) {
                        return trace(cameFrom, here);
                    }
                    continue;
                }
                // a cell that is pinned, or already written in, is not the reader's to use on the way past
                if (cell.pinned() || cell.aspect().isPresent()) {
                    continue;
                }
                for (Holder<Aspect> lay : writable) {
                    if (!linked(here.aspect(), lay)) {
                        continue;
                    }
                    Reached step = new Reached(next, lay);
                    int cost = spent + primalCost(lay).total();
                    if (cost < best.getOrDefault(step, Integer.MAX_VALUE)) {
                        best.put(step, cost);
                        cameFrom.put(step, here);
                        queue.add(step);
                    }
                }
            }
        }
        return null;
    }

    private static List<Placement> trace(Map<Reached, Reached> cameFrom, Reached last) {
        List<Placement> run = new ArrayList<>();
        for (Reached at = last; at != null && cameFrom.get(at) != null; at = cameFrom.get(at)) {
            run.addFirst(new Placement(at.at(), at.aspect()));
        }
        return run;
    }

    /**
     * Marks the note worked out and rubs off anything left lying about that no chain runs through, which is how the
     * original left a solved sheet looking like the answer rather than the working.
     */
    public static ResearchNote settle(ResearchNote note) {
        Set<HexGrid.Hex> kept = note.pinnedCells().isEmpty()
                ? Set.of()
                : reach(note, note.pinnedCells().getFirst().at());

        List<ResearchNote.Cell> cells = new ArrayList<>(note.cells().size());
        for (ResearchNote.Cell cell : note.cells()) {
            cells.add(cell.pinned() || kept.contains(cell.at()) ? cell : ResearchNote.Cell.blank(cell.at()));
        }
        return new ResearchNote(note.research(), note.budget(), List.copyOf(cells), true);
    }
}
