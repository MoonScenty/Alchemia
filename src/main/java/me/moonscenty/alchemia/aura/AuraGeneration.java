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
        AuraTrait here = traitAt(level, at);
        float sum = here.level();
        for (Direction side : Direction.Plane.HORIZONTAL) {
            sum += traitAt(level, new ChunkPos(at.x + side.getStepX(), at.z + side.getStepZ())).level();
        }
        // averaged with the 4 neighbours, so biome borders slope instead of stepping
        float strength = sum / 5.0F;
        Holder<Aspect> leaning = here.aspect();

        int base = 0;
        AspectList aspects = AspectList.EMPTY;
        for (Holder<Aspect> primal : ModAspects.primals()) {
            // the leaning primal comes out whole; the others vary a little
            float share = primal.value() == leaning.value()
                    ? 1.0F
                    : 0.5F + (random.nextFloat() - random.nextFloat()) * 0.1F;
            base = Math.max(base, Math.round(strength * BASE * share));
            aspects = aspects.add(primal, Math.round(strength * BASE * (0.8F + random.nextFloat() * 0.2F) * share));
        }
        return new AuraChunk(base, aspects);
    }

    public static AuraTrait traitAt(LevelReader level, ChunkPos at) {
        BlockPos middle = new BlockPos(at.getMiddleBlockX(), SAMPLE_HEIGHT, at.getMiddleBlockZ());
        Holder<Biome> biome = level.getBiome(middle);
        return biome.getData(AuraTrait.MAP) instanceof AuraTrait trait ? trait : AuraTrait.ORDINARY;
    }

    public static void ensure(LevelReader level, LevelChunk chunk, RandomSource random) {
        if (!chunk.getData(ModAuraAttachment.AURA).exists()) {
            chunk.setData(ModAuraAttachment.AURA, drawUp(level, chunk.getPos(), random));
            chunk.setUnsaved(true);
        }
    }
}
