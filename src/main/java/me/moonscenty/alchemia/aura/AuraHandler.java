package me.moonscenty.alchemia.aura;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.LevelChunk;

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

    // all or nothing
    public static boolean drain(LevelAccessor level, BlockPos pos, Holder<Aspect> aspect, int amount) {
        AuraChunk aura = at(level, pos);
        if (!aura.exists() || aura.get(aspect) < amount) {
            return false;
        }
        store(level, pos, aura.reduce(aspect, amount));
        return true;
    }

    // every aspect is checked before any is spent
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

    public static int drainAvailable(LevelAccessor level, BlockPos pos, Holder<Aspect> aspect, int amount) {
        AuraChunk aura = at(level, pos);
        int taken = Math.min(amount, aura.get(aspect));
        if (taken > 0) {
            store(level, pos, aura.reduce(aspect, taken));
        }
        return taken;
    }

    // owed to the chunk and paid out on its next settling, where an overfull chunk spills it further off
    public static void recharge(ServerLevel level, BlockPos pos, Holder<Aspect> aspect, int amount,
            RandomSource random) {
        AuraTicker.owe(level, new net.minecraft.world.level.ChunkPos(pos), aspect, amount);
    }

    // original: what a player carries holds back from a thin aura only once they have learned to
    public static boolean spares(net.minecraft.world.entity.player.Player player) {
        return player == null || me.moonscenty.alchemia.player.PlayerKnowledge.of(player)
                .hasResearch(me.moonscenty.alchemia.Alchemia.id("aura_preserve"));
    }

    public static boolean shouldSpare(LevelAccessor level, BlockPos pos, Holder<Aspect> aspect) {
        AuraChunk aura = at(level, pos);
        return aura.exists() && (float) aura.get(aspect) / aura.base() < 0.1F;
    }

    @SafeVarargs
    public static boolean shouldSpareAny(LevelAccessor level, BlockPos pos, Holder<Aspect>... aspects) {
        for (Holder<Aspect> aspect : aspects) {
            if (shouldSpare(level, pos, aspect)) {
                return true;
            }
        }
        return false;
    }

    public static boolean drainUnlessSpared(LevelAccessor level, BlockPos pos, Holder<Aspect> aspect, int amount) {
        return !shouldSpare(level, pos, aspect) && drain(level, pos, aspect, amount);
    }

    public static boolean drainUnlessSpared(LevelAccessor level, BlockPos pos, AspectList wanted) {
        for (Holder<Aspect> aspect : wanted.sortedByName()) {
            if (shouldSpare(level, pos, aspect)) {
                return false;
            }
        }
        return drain(level, pos, wanted);
    }

    public static int drainAvailableUnlessSpared(LevelAccessor level, BlockPos pos, Holder<Aspect> aspect,
            int amount) {
        return shouldSpare(level, pos, aspect) ? 0 : drainAvailable(level, pos, aspect, amount);
    }
}
