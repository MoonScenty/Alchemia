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

public final class AuraGeneration {
    // per aspect, on ordinary land
    public static final int BASE = 100;
    private static final int SAMPLE_HEIGHT = 64;

    private AuraGeneration() {
    }

    public static AuraChunk drawUp(LevelReader level, ChunkPos at, RandomSource random) {
        Holder<Biome> here = biomeAt(level, at);
        float sum = strength(here);
        for (Direction side : Direction.Plane.HORIZONTAL) {
            sum += strength(biomeAt(level, new ChunkPos(at.x + side.getStepX(), at.z + side.getStepZ())));
        }
        // averaged with the 4 neighbours, so biome borders slope instead of stepping
        float strength = sum / 5.0F;
        Holder<Aspect> leaning = leaning(here, random);

        int base = 0;
        AspectList aspects = AspectList.EMPTY;
        for (Holder<Aspect> primal : ModAspects.primals()) {
            // the leaning primal comes out whole; the others vary a little
            float share = leaning != null && primal.value() == leaning.value()
                    ? 1.0F
                    : 0.5F + (random.nextFloat() - random.nextFloat()) * 0.1F;
            base = Math.max(base, (int) (strength * BASE * share));
            aspects = aspects.add(primal, (int) (strength * BASE * (0.8 + random.nextFloat() * 0.2F) * share));
        }
        return new AuraChunk(base, aspects);
    }

    public static Holder<Biome> biomeAt(LevelReader level, ChunkPos at) {
        return level.getBiome(new BlockPos(at.getMiddleBlockX(), SAMPLE_HEIGHT, at.getMiddleBlockZ()));
    }

    // original's: the average over every type the biome belongs to; a datapack may set it outright
    public static float strength(Holder<Biome> biome) {
        if (biome.getData(AuraTrait.MAP) instanceof AuraTrait set) {
            return set.level();
        }
        float sum = 0.0F;
        int count = 0;
        for (BiomeLeanings.Leaning leaning : BiomeLeanings.ALL) {
            if (biome.is(leaning.where())) {
                sum += leaning.level();
                count++;
            }
        }
        return count == 0 ? AuraTrait.ORDINARY.level() : sum / count;
    }

    // original's: each chunk leans towards one of its biome's types, picked afresh
    public static Holder<Aspect> leaning(Holder<Biome> biome, RandomSource random) {
        if (biome.getData(AuraTrait.MAP) instanceof AuraTrait set) {
            return set.aspect();
        }
        return BiomeLeanings.anyOf(biome, random);
    }

    public static void ensure(LevelReader level, LevelChunk chunk, RandomSource random) {
        if (!chunk.getData(ModAuraAttachment.AURA).exists()) {
            chunk.setData(ModAuraAttachment.AURA, drawUp(level, chunk.getPos(), random));
            chunk.setUnsaved(true);
        }
    }
}
