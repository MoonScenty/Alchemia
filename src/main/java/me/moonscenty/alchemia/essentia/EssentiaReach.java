package me.moonscenty.alchemia.essentia;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.joml.Vector3f;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.particle.EssenceOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

// Pipeless essentia draw for the matrix and similar; braced jars are skipped.
public final class EssentiaReach {
    private EssentiaReach() {
    }

    // only jars and essentia mirrors feed an infusion, nearest first, within a cube of range
    public static BlockPos drainForInfusion(Level level, BlockPos from, Holder<Aspect> aspect, int range) {
        for (BlockEntity holding : nearby(level, from, range * 2)) {
            BlockPos at = holding.getBlockPos();
            if (Math.abs(at.getX() - from.getX()) > range || Math.abs(at.getY() - from.getY()) > range
                    || Math.abs(at.getZ() - from.getZ()) > range) {
                continue;
            }
            if ((holding instanceof me.moonscenty.alchemia.block.entity.JarBlockEntity
                    || holding instanceof me.moonscenty.alchemia.block.entity.MirrorBlockEntity)
                    && holding instanceof EssentiaHolder holder && holder.held().get(aspect) > 0
                    && holder.release(aspect)) {
                return at;
            }
        }
        return null;
    }

    public static List<BlockEntity> holders(Level level, BlockPos from, int range) {
        return nearby(level, from, range);
    }

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

    private static final int DROPS = 3;

    public static void thread(ServerLevel level, BlockPos source, BlockPos sink, int colour) {
        Vec3 from = Vec3.atCenterOf(source);
        Vector3f to = Vec3.atCenterOf(sink).toVector3f();
        int first = level.random.nextInt(64);
        for (int drop = 0; drop < DROPS; drop++) {
            level.sendParticles(new EssenceOptions(colour, to, first + drop),
                    from.x, from.y, from.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }
}
