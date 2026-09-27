package me.moonscenty.alchemia.essentia;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import net.minecraft.core.Holder;

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
}
