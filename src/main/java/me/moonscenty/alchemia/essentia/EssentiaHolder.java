package me.moonscenty.alchemia.essentia;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.block.TubeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;

/**
 * Anything a tube can draw essentia out of or push it into: an alembic, a jar, a smelter.
 * <p>
 * Everything moves one point at a time. That is slow on purpose — a pipe run is meant to be something you watch
 * fill, and it also means nothing has to be put back when a move half succeeds.
 */
public interface EssentiaHolder {
    /** What is in it at the moment. */
    AspectList held();

    /** Whether one more of this would fit. A holder that never takes anything says no to everything. */
    default boolean wants(Holder<Aspect> aspect) {
        return false;
    }

    /** Puts one in, and says whether it went. */
    default boolean accept(Holder<Aspect> aspect) {
        return false;
    }

    /** Takes one out, and says whether there was one. A holder that never gives anything up says no. */
    default boolean release(Holder<Aspect> aspect) {
        return false;
    }

    /**
     * Whether a pipe touching this side can reach in at all.
     * <p>
     * A jar has a lid and glass, and only the lid is a way in. Most things are open on every side and say so by
     * saying nothing.
     */
    default boolean reachableFrom(Direction side) {
        return true;
    }

    /**
     * Whether a pipe on this side may fill it. Most things take in on every side they can be reached from; a
     * centrifuge takes in only from below.
     */
    default boolean takesFrom(Direction side) {
        return true;
    }

    /** Whether a pipe on this side may draw from it. A centrifuge gives up only what it has split, and only above. */
    default boolean givesTo(Direction side) {
        return true;
    }

    /**
     * The vessel on that side of a block, if a pipe there could draw from it. A tube is passed over, since a pipe
     * moves essentia on its own.
     */
    @Nullable
    static EssentiaHolder vesselBeside(Level level, BlockPos pos, Direction side) {
        BlockPos beside = pos.relative(side);
        if (level.getBlockState(beside).getBlock() instanceof TubeBlock
                || !(level.getBlockEntity(beside) instanceof EssentiaHolder source)
                || !source.reachableFrom(side.getOpposite()) || !source.givesTo(side.getOpposite())) {
            return null;
        }
        return source;
    }

    /** Takes one point of this aspect straight out of the vessel on that side, and says whether there was one. */
    static boolean drawFrom(Level level, BlockPos pos, Direction side, Holder<Aspect> aspect) {
        EssentiaHolder source = vesselBeside(level, pos, side);
        return source != null && source.held().get(aspect) > 0 && source.release(aspect);
    }

    /** Takes one point out of the vessel on that side, of the most plentiful aspect wanted; null if none was taken. */
    @Nullable
    static Holder<Aspect> drawFrom(Level level, BlockPos pos, Direction side, Predicate<Holder<Aspect>> wanted) {
        EssentiaHolder source = vesselBeside(level, pos, side);
        if (source == null) {
            return null;
        }
        for (Holder<Aspect> aspect : source.held().sortedByAmount()) {
            if (wanted.test(aspect) && source.release(aspect)) {
                return aspect;
            }
        }
        return null;
    }
}
