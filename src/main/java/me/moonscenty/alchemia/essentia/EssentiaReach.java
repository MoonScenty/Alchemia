package me.moonscenty.alchemia.essentia;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import me.moonscenty.alchemia.aspect.Aspect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Drawing essentia out of whatever is holding it nearby, with nothing laid between.
 * <p>
 * This is how a matrix is fed, and it is deliberately not how everything else is fed. A works is plumbed: the
 * smelter fills jars through tubes and the tubes are the interesting part. But a matrix wants a point of this and
 * a point of that from a dozen jars at once, and piping twelve jars into one block would be a knot nobody enjoys
 * tying. So the altar reaches, and a shelf of jars standing round it is the whole of the plumbing.
 * <p>
 * A braced jar is passed over, since a brace is exactly the instruction not to take from this one.
 */
public final class EssentiaReach {
    private EssentiaReach() {
    }

    /**
     * Takes one point of the given aspect out of the nearest thing within reach that will part with it.
     *
     * @return where it came from, or null if nothing nearby had any
     */
    public static BlockPos drain(Level level, BlockPos from, Holder<Aspect> aspect, int range) {
        for (BlockEntity holding : nearby(level, from, range)) {
            if (holding instanceof EssentiaHolder holder && holder.held().get(aspect) > 0
                    && holder.release(aspect)) {
                return holding.getBlockPos();
            }
        }
        return null;
    }

    /** How much of an aspect is standing within reach, for telling somebody why the work has stopped. */
    public static int within(Level level, BlockPos from, Holder<Aspect> aspect, int range) {
        int found = 0;
        for (BlockEntity holding : nearby(level, from, range)) {
            if (holding instanceof EssentiaHolder holder) {
                found += holder.held().get(aspect);
            }
        }
        return found;
    }

    /**
     * Everything that holds essentia within reach, nearest first.
     * <p>
     * Walked over the loaded chunks rather than block by block: a reach of twelve is twenty-five thousand blocks
     * and a handful of block entities, and asking the chunk which ones it has is the difference between the two.
     */
    private static List<BlockEntity> nearby(Level level, BlockPos from, int range) {
        List<BlockEntity> found = new ArrayList<>();
        int reach = range * range;
        for (int chunkX = (from.getX() - range) >> 4; chunkX <= (from.getX() + range) >> 4; chunkX++) {
            for (int chunkZ = (from.getZ() - range) >> 4; chunkZ <= (from.getZ() + range) >> 4; chunkZ++) {
                if (!level.hasChunk(chunkX, chunkZ)) {
                    continue;
                }
                for (BlockEntity holding : level.getChunk(chunkX, chunkZ).getBlockEntities().values()) {
                    if (holding instanceof EssentiaHolder && holding.getBlockPos().distSqr(from) <= reach) {
                        found.add(holding);
                    }
                }
            }
        }
        found.sort(Comparator.comparingDouble(holding -> holding.getBlockPos().distSqr(from)));
        return found;
    }

    /** A thread of colour drawn from where the essentia came from to whatever asked for it. */
    public static void thread(ServerLevel level, BlockPos source, BlockPos sink, int colour) {
        Vec3 from = Vec3.atCenterOf(source);
        Vec3 to = Vec3.atCenterOf(sink);
        int steps = (int) Math.max(3, from.distanceTo(to) * 2);
        for (int step = 0; step <= steps; step++) {
            Vec3 at = from.lerp(to, (double) step / steps);
            level.sendParticles(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, colour),
                    at.x, at.y, at.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }
}
