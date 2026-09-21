package me.moonscenty.alchemia.aura;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

/**
 * Changing what kind of land a place is.
 * <p>
 * Biomes are kept four blocks to a cell, so the smallest patch that can be changed is a four block cube. That is
 * about right for something creeping outwards a step at a time.
 */
public final class BiomePaint {
    private BiomePaint() {
    }

    /** One biome out of a tag, or nothing if the pack has put none in it. */
    public static Optional<Holder<Biome>> oneOf(ServerLevel level, TagKey<Biome> tag, RandomSource random) {
        return level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
                .getTag(tag)
                .filter(holders -> holders.size() > 0)
                .map(holders -> holders.get(random.nextInt(holders.size())));
    }

    /** Whether the land at a place is already of a kind. */
    public static boolean isAlready(ServerLevel level, BlockPos at, Holder<Biome> biome) {
        return level.getBiome(at).value() == biome.value();
    }

    /**
     * Turns the four block cube around a position into another kind of land, and tells anyone watching.
     *
     * @return whether anything changed
     */
    public static boolean paint(ServerLevel level, BlockPos at, Holder<Biome> biome) {
        if (!level.hasChunkAt(at) || isAlready(level, at, biome)) {
            return false;
        }
        LevelChunk chunk = level.getChunkAt(at);
        int index = chunk.getSectionIndex(at.getY());
        if (index < 0 || index >= chunk.getSections().length) {
            return false;
        }

        LevelChunkSection section = chunk.getSection(index);
        int originX = QuartPos.fromBlock(chunk.getPos().getMinBlockX());
        int originY = QuartPos.fromBlock(chunk.getSectionYFromSectionIndex(index) << 4);
        int originZ = QuartPos.fromBlock(chunk.getPos().getMinBlockZ());
        int wantX = QuartPos.fromBlock(at.getX());
        int wantY = QuartPos.fromBlock(at.getY());
        int wantZ = QuartPos.fromBlock(at.getZ());

        // every cell keeps what it had except the one being changed
        BiomeResolver resolver = (x, y, z, sampler) -> x == wantX && y == wantY && z == wantZ
                ? biome
                : section.getNoiseBiome(x & 3, y & 3, z & 3);
        section.fillBiomesFromNoise(resolver, level.getChunkSource().randomState().sampler(), originX, originY, originZ);

        chunk.setUnsaved(true);
        level.getChunkSource().chunkMap.resendBiomesForChunks(List.of(chunk));
        return true;
    }
}
