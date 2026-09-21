package me.moonscenty.alchemia.aura;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.registry.ModAspects;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Lets the aura settle.
 * <p>
 * Magic runs downhill: a chunk gives a little of what it has to whichever neighbour has least, so somewhere drawn
 * dry slowly fills again from the land around it and nowhere stays a hole forever. Nothing is created by this, only
 * moved, which is what makes drawing on the aura cost something.
 * <p>
 * The original did this on a thread of its own with a time budget. Here the chunks are walked a slice at a time on
 * the server thread instead, which is simpler to reason about and cannot race with anything.
 */
@EventBusSubscriber(modid = Alchemia.MODID)
public class AuraTicker {
    /** How many ticks it takes to come back round to the same chunk. */
    private static final int ROUND = 20;
    /** The least that has to be between two chunks before anything moves, as a share of the usual aura. */
    private static final int LEAST_GAP = AuraGeneration.BASE / 20;
    private static final int MOST_GAP = AuraGeneration.BASE / 5;
    /** How much of the gap flux insists on before it will spread, against the others. */
    private static final float FLUX_RELUCTANCE = 0.66F;
    private static final float ORDINARY_RELUCTANCE = 0.25F;
    /** Below a fifteenth of its base, a chunk starts going bad. */
    private static final int STARVED = 15;
    private static final float ROT_CHANCE = 75.0F;

    private static final Map<ResourceKey<Level>, List<ChunkPos>> LOADED = new HashMap<>();

    private AuraTicker() {
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level && event.getChunk() instanceof LevelChunk chunk) {
            AuraGeneration.ensure(level, chunk, level.getRandom());
            LOADED.computeIfAbsent(level.dimension(), key -> new ArrayList<>()).add(chunk.getPos());
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            List<ChunkPos> known = LOADED.get(level.dimension());
            if (known != null) {
                known.remove(event.getChunk().getPos());
            }
        }
    }

    @SubscribeEvent
    public static void onTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        List<ChunkPos> known = LOADED.get(level.dimension());
        if (known == null || known.isEmpty()) {
            return;
        }

        // a slice each tick, so every chunk comes round about once a second however many are loaded
        for (int index = (int) (level.getGameTime() % ROUND); index < known.size(); index += ROUND) {
            settle(level, known.get(index));
        }
    }

    private static void settle(ServerLevel level, ChunkPos at) {
        if (!level.hasChunk(at.x, at.z)) {
            return;
        }
        LevelChunk chunk = level.getChunk(at.x, at.z);
        AuraChunk aura = chunk.getData(ModAuraAttachment.AURA);
        if (!aura.exists()) {
            return;
        }

        RandomSource random = level.getRandom();
        AuraChunk after = aura;
        for (Holder<Aspect> aspect : aura.aspects().sortedByName()) {
            after = spread(level, at, after, aspect, random);
            after = rot(after, aspect, random);
        }

        if (after != aura) {
            chunk.setData(ModAuraAttachment.AURA, after);
            chunk.setUnsaved(true);
        }
    }

    /** Hands one point of an aspect to whichever neighbour is worst off, if the difference is worth the trouble. */
    private static AuraChunk spread(ServerLevel level, ChunkPos at, AuraChunk aura, Holder<Aspect> aspect,
            RandomSource random) {
        int here = aura.get(aspect);
        List<Direction> sides = new ArrayList<>(List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST));
        Collections.shuffle(sides, new java.util.Random(random.nextLong()));

        ChunkPos poorest = null;
        AuraChunk poorestAura = null;
        int lowest = Integer.MAX_VALUE;
        for (Direction side : sides) {
            ChunkPos next = new ChunkPos(at.x + side.getStepX(), at.z + side.getStepZ());
            if (!level.hasChunk(next.x, next.z)) {
                continue;
            }
            AuraChunk theirs = level.getChunk(next.x, next.z).getData(ModAuraAttachment.AURA);
            int amount = theirs.get(aspect);
            if (theirs.exists() && amount < theirs.base() && amount < here && amount < lowest) {
                poorest = next;
                poorestAura = theirs;
                lowest = amount;
            }
        }
        if (poorest == null) {
            return aura;
        }

        float reluctance = aspect.value() == ModAspects.FLUX.value() ? FLUX_RELUCTANCE : ORDINARY_RELUCTANCE;
        int gap = (int) Math.max(LEAST_GAP, Math.min(MOST_GAP, lowest * reluctance));
        if (lowest >= here - gap) {
            return aura;
        }

        LevelChunk theirChunk = level.getChunk(poorest.x, poorest.z);
        theirChunk.setData(ModAuraAttachment.AURA, poorestAura.add(aspect, 1));
        theirChunk.setUnsaved(true);
        return aura.reduce(aspect, 1);
    }

    /** Land run down far enough starts turning what is left of its magic bad. */
    private static AuraChunk rot(AuraChunk aura, Holder<Aspect> aspect, RandomSource random) {
        if (aspect.value() == ModAspects.FLUX.value()) {
            return aura;
        }
        float floor = aura.base() / (float) STARVED;
        int here = aura.get(aspect);
        if (here >= floor) {
            return aura;
        }
        return random.nextFloat() < (floor - here) / (aura.base() * ROT_CHANCE)
                ? aura.add(ModAspects.FLUX, 1)
                : aura;
    }
}
