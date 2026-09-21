package me.moonscenty.alchemia.aura;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.registry.ModAspects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * Working out what the aura over a chunk should be, the first time anyone looks.
 * <p>
 * The land decides it: how much the biome holds, averaged with the four around it so a border does not come out as a
 * wall, and which primal it leans towards, which it gets a full measure of while the rest come out thinner.
 */
public final class AuraGeneration {
    /** As much of one aspect as ordinary land holds. */
    public static final int BASE = 100;
    /** How high the middle of a chunk is taken to be when asking which biome it is in. */
    private static final int SAMPLE_HEIGHT = 64;

    private AuraGeneration() {
    }

    public static AuraChunk drawUp(LevelReader level, ChunkPos at, RandomSource random) {
        AuraTrait here = traitAt(level, at);
        float sum = here.level();
        for (Direction side : Direction.Plane.HORIZONTAL) {
            sum += traitAt(level, new ChunkPos(at.x + side.getStepX(), at.z + side.getStepZ())).level();
        }
        // averaged with the land around it, so a border between biomes is a slope rather than a wall
        float strength = sum / 5.0F;
        Holder<Aspect> leaning = here.aspect();

        int base = 0;
        AspectList aspects = AspectList.EMPTY;
        for (Holder<Aspect> primal : ModAspects.primals()) {
            // the primal the land leans towards comes out whole; the others vary a little either way
            float share = primal.value() == leaning.value()
                    ? 1.0F
                    : 0.5F + (random.nextFloat() - random.nextFloat()) * 0.1F;
            base = Math.max(base, Math.round(strength * BASE * share));
            aspects = aspects.add(primal, Math.round(strength * BASE * (0.8F + random.nextFloat() * 0.2F) * share));
        }
        return new AuraChunk(base, aspects);
    }

    /** What the land at a chunk is like. Anywhere the pack says nothing about is taken to be ordinary. */
    public static AuraTrait traitAt(LevelReader level, ChunkPos at) {
        BlockPos middle = new BlockPos(at.getMiddleBlockX(), SAMPLE_HEIGHT, at.getMiddleBlockZ());
        Holder<Biome> biome = level.getBiome(middle);
        return biome.getData(AuraTrait.MAP) instanceof AuraTrait trait ? trait : AuraTrait.ORDINARY;
    }

    /** Fills in a chunk's aura if it has never had one. */
    public static void ensure(LevelReader level, LevelChunk chunk, RandomSource random) {
        if (!chunk.getData(ModAuraAttachment.AURA).exists()) {
            chunk.setData(ModAuraAttachment.AURA, drawUp(level, chunk.getPos(), random));
            chunk.setUnsaved(true);
        }
    }
}
