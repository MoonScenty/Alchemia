package me.moonscenty.alchemia.research;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
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
