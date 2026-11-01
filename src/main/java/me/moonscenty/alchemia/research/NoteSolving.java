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

// Neighbouring aspects link only when one is a component of the other; solved once every pinned aspect is on one chain.
public final class NoteSolving {
    private NoteSolving() {
    }

    public static boolean linked(Holder<Aspect> one, Holder<Aspect> other) {
        return one.value().hasComponent(other) || other.value().hasComponent(one);
    }

    public enum Refusal {
        OK,
        SOLVED,
        NO_SPACE,
        PINNED,
        SPENT,
        NO_SUCH_MIX;

        public boolean allowed() {
            return this == OK;
        }
    }

    // deliberately no "has the reader met this aspect" check: the sheet only offers what mixing can make,
    // and the gate would leave notes whose chains pass through unseen aspects unsolvable
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

    public static ResearchNote mix(ResearchNote note, Holder<Aspect> one, Holder<Aspect> other, Holder<Aspect> result) {
        AspectList budget = note.budget().reduce(one, 1).reduce(other, 1).add(result, 1);
        return new ResearchNote(note.research(), budget, note.cells(), note.complete());
    }

    // lifting an aspect back off does not refund it
    public static ResearchNote place(ResearchNote note, HexGrid.Hex at, Optional<Holder<Aspect>> aspect) {
        List<ResearchNote.Cell> cells = new ArrayList<>(note.cells().size());
        for (ResearchNote.Cell cell : note.cells()) {
            cells.add(cell.at().equals(at) ? new ResearchNote.Cell(at, aspect, false) : cell);
        }

        AspectList budget = aspect.map(held -> note.budget().reduce(held, 1)).orElse(note.budget());
        return new ResearchNote(note.research(), budget, List.copyOf(cells), note.complete());
    }

    public static boolean isSolved(ResearchNote note) {
        Set<HexGrid.Hex> pinned = new HashSet<>();
        note.pinnedCells().forEach(cell -> pinned.add(cell.at()));
        if (pinned.size() <= 1) {
            return !pinned.isEmpty();
        }
        return reach(note, note.pinnedCells().getFirst().at()).containsAll(pinned);
    }

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

    public record Placement(HexGrid.Hex at, Holder<Aspect> aspect) {
    }

    public static AspectList primalCost(Holder<Aspect> aspect) {
        return COSTS.computeIfAbsent(aspect, held -> Optional.ofNullable(primalCost(held, new HashSet<>()))).orElse(null);
    }

    private static final Map<Holder<Aspect>, Optional<AspectList>> COSTS = new ConcurrentHashMap<>();

    private static AspectList primalCost(Holder<Aspect> aspect, Set<Holder<Aspect>> seen) {
        if (aspect.value().isPrimal()) {
            return AspectList.of(aspect, 1);
        }
        // a compound that lists itself among its parts
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

    // greedy: joins pinned cells one at a time by the cheapest run; not optimal, but proves the note solvable
    public static Optional<List<Placement>> solve(Registry<Aspect> aspects, ResearchNote note) {
        List<ResearchNote.Cell> pinned = note.pinnedCells();
        if (pinned.size() <= 1) {
            return Optional.of(List.of());
        }

        List<Placement> answer = new ArrayList<>();
        ResearchNote working = note;
        Set<HexGrid.Hex> chain = new HashSet<>();
        chain.add(pinned.getFirst().at());
        Links links = Links.of(aspects);

        for (int index = 1; index < pinned.size(); index++) {
            List<Placement> leg = cheapestRun(links, working, chain, pinned.get(index).at());
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

    private record Reached(HexGrid.Hex at, Holder<Aspect> aspect) {
    }

    // cost stored with the entry so queue order never shifts; a dearer stale copy is skipped
    private record Queued(Reached reached, int cost) {
    }

    private record Links(List<Holder<Aspect>> writable, Map<Aspect, List<Holder<Aspect>>> after,
            Map<Aspect, Integer> cost) {
        static Links of(Registry<Aspect> aspects) {
            List<Holder<Aspect>> writable = aspects.holders()
                    .map(holder -> (Holder<Aspect>) holder)
                    .filter(holder -> primalCost(holder) != null)
                    .toList();
            Map<Aspect, List<Holder<Aspect>>> after = new HashMap<>();
            Map<Aspect, Integer> cost = new HashMap<>();
            for (Holder<Aspect> one : writable) {
                cost.put(one.value(), primalCost(one).total());
                after.put(one.value(), writable.stream().filter(other -> linked(one, other)).toList());
            }
            return new Links(writable, after, cost);
        }
    }

    private static List<Holder<Aspect>> following(Links links, Holder<Aspect> aspect) {
        List<Holder<Aspect>> known = links.after().get(aspect.value());
        return known != null ? known
                : links.writable().stream().filter(other -> linked(aspect, other)).toList();
    }

    // searched over (cell, aspect) by primal cost, so a long cheap run beats a short dear one
    private static List<Placement> cheapestRun(Links links, ResearchNote note, Set<HexGrid.Hex> chain,
            HexGrid.Hex target) {
        Holder<Aspect> goal = note.cellAt(target).flatMap(ResearchNote.Cell::aspect).orElse(null);
        if (goal == null) {
            return null;
        }
        Map<Reached, Integer> best = new HashMap<>();
        Map<Reached, Reached> cameFrom = new HashMap<>();
        PriorityQueue<Queued> queue = new PriorityQueue<>(Comparator.comparingInt(Queued::cost));

        for (HexGrid.Hex at : chain) {
            Holder<Aspect> held = note.cellAt(at).flatMap(ResearchNote.Cell::aspect).orElse(null);
            if (held != null) {
                Reached start = new Reached(at, held);
                best.put(start, 0);
                cameFrom.put(start, null);
                queue.add(new Queued(start, 0));
            }
        }

        while (!queue.isEmpty()) {
            Queued polled = queue.poll();
            Reached here = polled.reached();
            int spent = polled.cost();
            if (spent > best.get(here)) {
                continue;
            }
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
                if (cell.pinned() || cell.aspect().isPresent()) {
                    continue;
                }
                for (Holder<Aspect> lay : following(links, here.aspect())) {
                    Reached step = new Reached(next, lay);
                    int cost = spent + links.cost().get(lay.value());
                    if (cost < best.getOrDefault(step, Integer.MAX_VALUE)) {
                        best.put(step, cost);
                        cameFrom.put(step, here);
                        queue.add(new Queued(step, cost));
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

    // rubs off anything no chain runs through, as the original did
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
