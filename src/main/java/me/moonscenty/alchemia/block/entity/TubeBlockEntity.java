package me.moonscenty.alchemia.block.entity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.block.TubeBlock;
import me.moonscenty.alchemia.essentia.EssentiaHolder;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Moves one point of essentia along a run of pipe, now and then.
 * <p>
 * The pull is done by the far end rather than the near one. A tube next to something that has room looks back
 * along the pipe for something that will give a point up, and takes it. Pushing instead would mean every source
 * shoving into the first thing it found, and a run with two ends on it would fill whichever end it reached first
 * rather than whichever end still had room.
 * <p>
 * The walk back is where the odd kinds of pipe have their say. Every tube along the way is asked whether anything
 * gets through it at all, whether it insists on one aspect, and whether it wants the run to wait a turn. What comes
 * back decides which sources the pull may take from, and when.
 */
public class TubeBlockEntity extends BlockEntity {
    /** How often a point moves, in ticks. */
    private static final int PULLS_EVERY = 10;
    /** How far along the pipe a pull will look. A run longer than this simply needs a second pump of its own. */
    private static final int REACH = 96;

    private int counter;
    /** How many turns this tube has taken. A run held up along the way only moves on some of them. */
    private long turns;

    public TubeBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.TUBE.get(), pos, state);
    }

    protected TubeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TubeBlockEntity tube) {
        if (tube.counter++ < PULLS_EVERY) {
            return;
        }
        tube.counter = 0;
        tube.turns++;
        tube.pull(level, pos, state);
    }

    /** Finds something this tube can fill, and something along the pipe that will fill it. */
    private void pull(Level level, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof TubeBlock)) {
            return;
        }
        // a buffer is a length of pipe that is also a vessel, so the first thing it fills is itself
        if (this instanceof EssentiaHolder self && draw(level, pos, self, null)) {
            return;
        }
        for (Direction side : Direction.values()) {
            if (state.getValue(TubeBlock.SIDES.get(side)) != TubeBlock.Link.BLOCK) {
                continue;
            }
            if (level.getBlockEntity(pos.relative(side)) instanceof EssentiaHolder sink
                    && sink.reachableFrom(side.getOpposite())
                    && draw(level, pos, sink, side)) {
                return;
            }
        }
    }

    /**
     * Moves one point into the given vessel if anything up the pipe will part with one.
     *
     * @param into the side of this tube the vessel sits on, or null when the vessel is this tube itself
     */
    private boolean draw(Level level, BlockPos pos, EssentiaHolder sink, Direction into) {
        for (Route route : upstreamOf(level, pos, into)) {
            if (route.from() == sink) {
                continue;
            }
            // a buffer standing on essentia is not somewhere for another buffer to move it to
            if (sink instanceof BufferTubeBlockEntity && route.from() instanceof BufferTubeBlockEntity) {
                continue;
            }
            if (turns % (route.waits() + 1) != 0) {
                continue;
            }
            for (Holder<Aspect> aspect : route.from().held().sortedByAmount()) {
                if (route.only() != null && route.only().value() != aspect.value()) {
                    continue;
                }
                if (!sink.wants(aspect) || !route.from().release(aspect)) {
                    continue;
                }
                if (sink.accept(aspect)) {
                    return true;
                }
                // the sink changed its mind between being asked and being handed it; put it back
                route.from().accept(aspect);
            }
        }
        return false;
    }

    /** One tube the walk has reached, and what the way back from it allows. */
    private record Step(BlockPos at, Direction out, Holder<Aspect> only, int waits) {
    }

    /** A tube the walk has been at, reached from a particular side. */
    private record Seen(BlockPos at, Direction out) {
    }

    /** Something the pipe can draw from, with what the way there allows. */
    private record Route(EssentiaHolder from, Holder<Aspect> only, int waits) {
    }

    /**
     * Everything the pipe can reach from here, walked against the flow.
     * <p>
     * Walked rather than cached: a pipe run is a handful of blocks and is pulled from six times a second, and a
     * cache would have to be torn up every time anyone placed or broke anything anywhere along it.
     *
     * @param out the side of the first tube essentia would leave by, or null when it stops in that tube
     */
    private static List<Route> upstreamOf(Level level, BlockPos start, Direction out) {
        Set<Seen> seen = new HashSet<>();
        Deque<Step> todo = new ArrayDeque<>();
        List<Route> found = new ArrayList<>();
        seen.add(new Seen(start, out));
        todo.add(new Step(start, out, null, 0));

        while (!todo.isEmpty() && seen.size() <= REACH) {
            Step step = todo.removeFirst();
            BlockState state = level.getBlockState(step.at());
            if (!(state.getBlock() instanceof TubeBlock tube)) {
                continue;
            }

            Holder<Aspect> only = step.only();
            Optional<Holder<Aspect>> insists = tube.insistsOn(level, step.at());
            if (insists.isPresent()) {
                // two filters set to different things in one run are a wall, not a choice
                if (only != null && only.value() != insists.get().value()) {
                    continue;
                }
                only = insists.get();
            }
            int waits = step.waits() + tube.holdsUp();

            // a buffer standing in the run is itself something to draw from
            if (!step.at().equals(start) && level.getBlockEntity(step.at()) instanceof EssentiaHolder holder) {
                found.add(new Route(holder, only, waits));
            }

            for (Direction side : Direction.values()) {
                if (side == step.out() || !tube.passes(state, side, step.out())) {
                    continue;
                }
                BlockPos next = step.at().relative(side);
                switch (state.getValue(TubeBlock.SIDES.get(side))) {
                    case TUBE -> {
                        if (seen.add(new Seen(next, side.getOpposite()))) {
                            todo.add(new Step(next, side.getOpposite(), only, waits));
                        }
                    }
                    case BLOCK -> {
                        if (level.getBlockEntity(next) instanceof EssentiaHolder holder
                                && holder.reachableFrom(side.getOpposite())) {
                            found.add(new Route(holder, only, waits));
                        }
                    }
                    default -> {
                    }
                }
            }
        }
        return found;
    }
}
