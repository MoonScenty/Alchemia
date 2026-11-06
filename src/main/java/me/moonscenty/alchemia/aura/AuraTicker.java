package me.moonscenty.alchemia.aura;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.registry.ModAspects;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

// Each chunk hands a little to its poorest neighbour: aura is only moved, never created.
@EventBusSubscriber(modid = Alchemia.MODID)
public class AuraTicker {
    // ticks per full pass over the loaded chunks (the original's half second)
    private static final int ROUND = 10;
    // at most one flux event per dimension this often
    private static final int TROUBLE_EVERY = 20;
    private static final int LEAST_GAP = AuraGeneration.BASE / 20;
    private static final int MOST_GAP = AuraGeneration.BASE / 5;
    private static final float FLUX_RELUCTANCE = 0.66F;
    private static final float ORDINARY_RELUCTANCE = 0.25F;
    // below base / STARVED a chunk starts turning to flux
    private static final int STARVED = 15;
    private static final float ROT_CHANCE = 75.0F;
    // owed aura is paid out in at most this many portions
    private static final int PORTIONS = 10;
    // an overfull chunk tolerates twice as much spare flux
    private static final int FLUX_TOLERANCE = 2;
    private static final float LOST = 0.33F;

    private static final Map<ResourceKey<Level>, List<ChunkPos>> LOADED = new HashMap<>();
    private static final Map<ResourceKey<Level>, Map<ChunkPos, AspectList>> OWED = new HashMap<>();
    // the latest chunk to brew trouble in each dimension
    private static final Map<ResourceKey<Level>, ChunkPos> TROUBLED = new HashMap<>();

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
    public static void onStopped(ServerStoppedEvent event) {
        LOADED.clear();
        OWED.clear();
        TROUBLED.clear();
    }

    // node feeding and refunds: paid on the chunk's next settling
    public static void owe(ServerLevel level, ChunkPos at, Holder<Aspect> aspect, int amount) {
        if (amount <= 0) {
            return;
        }
        OWED.computeIfAbsent(level.dimension(), key -> new HashMap<>())
                .merge(at, AspectList.of(aspect, amount), AspectList::add);
    }

    public static int owed(ServerLevel level, ChunkPos at, Holder<Aspect> aspect) {
        Map<ChunkPos, AspectList> owed = OWED.get(level.dimension());
        AspectList here = owed == null ? null : owed.get(at);
        return here == null ? 0 : here.get(aspect);
    }

    @SubscribeEvent
    public static void onTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        List<ChunkPos> known = LOADED.get(level.dimension());
        if (known != null) {
            for (int index = (int) (level.getGameTime() % ROUND); index < known.size(); index += ROUND) {
                settle(level, known.get(index));
            }
        }
        if (level.getGameTime() % TROUBLE_EVERY == 0) {
            ChunkPos troubled = TROUBLED.remove(level.dimension());
            if (troubled != null) {
                FluxEvents.strike(level, troubled, level.getRandom());
            }
        }
    }

    public static void settle(ServerLevel level, ChunkPos at) {
        if (!level.hasChunk(at.x, at.z)) {
            return;
        }
        LevelChunk chunk = level.getChunk(at.x, at.z);
        AuraChunk aura = chunk.getData(ModAuraAttachment.AURA);
        if (!aura.exists()) {
            return;
        }

        RandomSource random = level.getRandom();
        AuraChunk after = payOut(level, at, aura, random);
        for (Holder<Aspect> aspect : after.aspects().sortedByName()) {
            after = spread(level, at, after, aspect, random);
            after = rot(after, aspect, random);
        }

        if (after != aura) {
            chunk.setData(ModAuraAttachment.AURA, after);
            chunk.setUnsaved(true);
        }

        if (FluxEvents.brewing(after, random)) {
            TROUBLED.put(level.dimension(), at);
        }
    }

    // past its base, the further over a chunk is the likelier each portion goes on to a chunk one to three off,
    // and a third of those are lost
    private static AuraChunk payOut(ServerLevel level, ChunkPos at, AuraChunk aura, RandomSource random) {
        Map<ChunkPos, AspectList> owed = OWED.get(level.dimension());
        AspectList due = owed == null ? null : owed.remove(at);
        if (due == null) {
            return aura;
        }
        float tenth = aura.base() * 0.1F;
        for (Holder<Aspect> aspect : due.sortedByName()) {
            int was = aura.get(aspect);
            int now = was;
            int portions = due.get(aspect);
            int portion = 1;
            if (portions > PORTIONS) {
                portion = portions / PORTIONS;
                portions = PORTIONS;
            }
            int tolerance = aspect.value() == ModAspects.FLUX.value() ? FLUX_TOLERANCE : 1;
            for (int i = 0; i < portions; i++) {
                if (now > aura.base()) {
                    float over = (now - aura.base()) / tenth / tolerance;
                    if (random.nextFloat() > over) {
                        now += portion;
                    } else if (random.nextFloat() > LOST) {
                        Direction side = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                        int far = Mth.nextInt(random, 1, 3);
                        owe(level, new ChunkPos(at.x + side.getStepX() * far, at.z + side.getStepZ() * far), aspect, portion);
                    }
                } else {
                    now += portion;
                }
            }
            if (now != was) {
                aura = aura.add(aspect, now - was);
            }
        }
        return aura;
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
