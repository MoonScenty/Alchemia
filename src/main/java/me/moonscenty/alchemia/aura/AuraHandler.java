package me.moonscenty.alchemia.aura;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * Drawing on the aura, and putting it back.
 * <p>
 * Everything here works in whole chunks: a position only says which chunk is meant. Draining never takes more than
 * is there, and asks before it takes, so a device that needs several aspects at once cannot end up having spent half
 * of what it wanted.
 */
public final class AuraHandler {
    private AuraHandler() {
    }

    public static AuraChunk at(LevelAccessor level, BlockPos pos) {
        LevelChunk chunk = chunkAt(level, pos);
        return chunk == null ? AuraChunk.NONE : chunk.getData(ModAuraAttachment.AURA);
    }

    private static LevelChunk chunkAt(LevelAccessor level, BlockPos pos) {
        if (!(level instanceof Level real) || !real.hasChunkAt(pos)) {
            return null;
        }
        return real.getChunkAt(pos);
    }

    private static void store(LevelAccessor level, BlockPos pos, AuraChunk aura) {
        LevelChunk chunk = chunkAt(level, pos);
        if (chunk != null) {
            chunk.setData(ModAuraAttachment.AURA, aura);
            chunk.setUnsaved(true);
        }
    }

    public static int get(LevelAccessor level, BlockPos pos, Holder<Aspect> aspect) {
        return at(level, pos).get(aspect);
    }

    public static int base(LevelAccessor level, BlockPos pos) {
        return at(level, pos).base();
    }

    public static void add(LevelAccessor level, BlockPos pos, Holder<Aspect> aspect, int amount) {
        AuraChunk aura = at(level, pos);
        if (aura.exists() && amount > 0) {
            store(level, pos, aura.add(aspect, amount));
        }
    }

    /** Takes the lot or nothing, so a call that fails leaves the chunk as it was. */
    public static boolean drain(LevelAccessor level, BlockPos pos, Holder<Aspect> aspect, int amount) {
        AuraChunk aura = at(level, pos);
        if (!aura.exists() || aura.get(aspect) < amount) {
            return false;
        }
        store(level, pos, aura.reduce(aspect, amount));
        return true;
    }

    /** The same for several aspects at once: every one is checked before any of them is spent. */
    public static boolean drain(LevelAccessor level, BlockPos pos, AspectList wanted) {
        AuraChunk aura = at(level, pos);
        if (!aura.exists()) {
            return false;
        }
        for (Holder<Aspect> aspect : wanted.sortedByName()) {
            if (aura.get(aspect) < wanted.get(aspect)) {
                return false;
            }
        }

        AspectList left = aura.aspects();
        for (Holder<Aspect> aspect : wanted.sortedByName()) {
            left = left.reduce(aspect, wanted.get(aspect));
        }
        store(level, pos, aura.with(left));
        return true;
    }

    /** Takes as much as is there, up to what was asked for, and says how much that was. */
    public static int drainAvailable(LevelAccessor level, BlockPos pos, Holder<Aspect> aspect, int amount) {
        AuraChunk aura = at(level, pos);
        int taken = Math.min(amount, aura.get(aspect));
        if (taken > 0) {
            store(level, pos, aura.reduce(aspect, taken));
        }
        return taken;
    }

    /**
     * Whether the aura here is thin enough that it should be left alone. The original let a reader who had studied
     * the matter notice this; here it is simply true of anywhere run down past a tenth of what it holds.
     */
    public static boolean shouldSpare(LevelAccessor level, BlockPos pos, Holder<Aspect> aspect) {
        AuraChunk aura = at(level, pos);
        return aura.exists() && (float) aura.get(aspect) / aura.base() < 0.1F;
    }
}
