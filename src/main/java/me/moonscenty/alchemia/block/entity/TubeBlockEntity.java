package me.moonscenty.alchemia.block.entity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
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
import net.minecraft.world.level.block.state.BlockState;

/**
 * Moves one point of essentia along a run of pipe, now and then.
 * <p>
 * The pull is done by the far end rather than the near one. A tube next to something that has room looks back
 * along the pipe for something that will give a point up, and takes it. Pushing instead would mean every source
 * shoving into the first thing it found, and a run with two ends on it would fill whichever end it reached first
 * rather than whichever end still had room.
 */
public class TubeBlockEntity extends BlockEntity {
    /** How often a point moves, in ticks. */
    private static final int PULLS_EVERY = 10;
    /** How far along the pipe a pull will look. A run longer than this simply needs a second pump of its own. */
    private static final int REACH = 96;

    private int counter;

    public TubeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TUBE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TubeBlockEntity tube) {
        if (tube.counter++ < PULLS_EVERY) {
            return;
        }
        tube.counter = 0;
        tube.pull(level, pos);
    }

    /** Finds something next to this tube with room in it, and something along the pipe that will fill it. */
    private void pull(Level level, BlockPos pos) {
        List<EssentiaHolder> sinks = besideMe(level, pos);
        if (sinks.isEmpty()) {
            return;
        }
        List<EssentiaHolder> sources = alongThePipe(level, pos);
        if (sources.isEmpty()) {
            return;
        }

        for (EssentiaHolder sink : sinks) {
            for (EssentiaHolder source : sources) {
                if (source == sink) {
                    continue;
                }
                for (Holder<Aspect> aspect : source.held().sortedByAmount()) {
                    if (sink.wants(aspect) && source.release(aspect)) {
                        if (sink.accept(aspect)) {
                            return;
                        }
                        // the sink changed its mind between being asked and being handed it; put it back
                        source.accept(aspect);
                    }
                }
            }
        }
    }

    /** What this one tube is plugged into. */
    private static List<EssentiaHolder> besideMe(Level level, BlockPos pos) {
        List<EssentiaHolder> out = new ArrayList<>();
        for (Direction side : Direction.values()) {
            if (level.getBlockEntity(pos.relative(side)) instanceof EssentiaHolder holder
                    && holder.reachableFrom(side.getOpposite())) {
                out.add(holder);
            }
        }
        return out;
    }

    /**
     * Everything plugged into any tube this one can be reached from.
     * <p>
     * Walked rather than cached: a pipe run is a handful of blocks and is pulled from six times a second, and a
     * cache would have to be torn up every time anyone placed or broke anything anywhere along it.
     */
    private static List<EssentiaHolder> alongThePipe(Level level, BlockPos from) {
        Set<BlockPos> seen = new HashSet<>();
        Deque<BlockPos> todo = new ArrayDeque<>();
        List<EssentiaHolder> out = new ArrayList<>();
        seen.add(from);
        todo.add(from);

        while (!todo.isEmpty() && seen.size() <= REACH) {
            BlockPos at = todo.removeFirst();
            BlockState state = level.getBlockState(at);
            if (!(state.getBlock() instanceof TubeBlock)) {
                continue;
            }
            for (Direction side : Direction.values()) {
                BlockPos next = at.relative(side);
                switch (state.getValue(TubeBlock.SIDES.get(side))) {
                    case TUBE -> {
                        if (seen.add(next)) {
                            todo.add(next);
                        }
                    }
                    case BLOCK -> {
                        if (level.getBlockEntity(next) instanceof EssentiaHolder holder
                                && holder.reachableFrom(side.getOpposite())) {
                            out.add(holder);
                        }
                    }
                    default -> {
                    }
                }
            }
        }
        return out;
    }
}
