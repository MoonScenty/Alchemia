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

// Everything moves one point at a time, so a half-done move never needs undoing.
public interface EssentiaHolder {
    AspectList held();

    default boolean wants(Holder<Aspect> aspect) {
        return false;
    }

    default boolean accept(Holder<Aspect> aspect) {
        return false;
    }

    default boolean release(Holder<Aspect> aspect) {
        return false;
    }

    default boolean reachableFrom(Direction side) {
        return true;
    }

    default boolean takesFrom(Direction side) {
        return true;
    }

    default boolean givesTo(Direction side) {
        return true;
    }

    // tubes are skipped: a pipe moves essentia on its own
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

    static boolean drawFrom(Level level, BlockPos pos, Direction side, Holder<Aspect> aspect) {
        EssentiaHolder source = vesselBeside(level, pos, side);
        return source != null && source.held().get(aspect) > 0 && source.release(aspect);
    }

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
