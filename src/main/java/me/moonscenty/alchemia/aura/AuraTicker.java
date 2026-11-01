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

// Each chunk hands a little to its poorest neighbour: aura is only moved, never created.
@EventBusSubscriber(modid = Alchemia.MODID)
public class AuraTicker {
    // ticks per full pass over the loaded chunks
    private static final int ROUND = 20;
    private static final int LEAST_GAP = AuraGeneration.BASE / 20;
    private static final int MOST_GAP = AuraGeneration.BASE / 5;
    private static final float FLUX_RELUCTANCE = 0.66F;
    private static final float ORDINARY_RELUCTANCE = 0.25F;
    // below base / STARVED a chunk starts turning to flux
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

        if (FluxEvents.brewing(after, random)) {
            FluxEvents.strike(level, at, random);
        }
    }

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
