package me.moonscenty.alchemia.aura;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

// Biomes are stored per 4x4x4 cell, so that is the smallest patch that can be changed.
public final class BiomePaint {
    private BiomePaint() {
    }

    public static Optional<Holder<Biome>> oneOf(ServerLevel level, TagKey<Biome> tag, RandomSource random) {
        return level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
                .getTag(tag)
                .filter(holders -> holders.size() > 0)
                .map(holders -> holders.get(random.nextInt(holders.size())));
    }

    public static Optional<Holder<Biome>> named(ServerLevel level, ResourceKey<Biome> key) {
        return level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
                .getHolder(key).map(holder -> holder);
    }

    public static Holder<Biome> generated(ServerLevel level, BlockPos at) {
        return level.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome(QuartPos.fromBlock(at.getX()),
                QuartPos.fromBlock(at.getY()), QuartPos.fromBlock(at.getZ()),
                level.getChunkSource().randomState().sampler());
    }

    public static boolean restore(ServerLevel level, BlockPos at, ResourceKey<Biome> from, boolean tell) {
        return level.hasChunkAt(at) && at(level, at).is(from) && paint(level, at, generated(level, at), tell);
    }

    // exact noise cell; getBiome() fuzzes towards neighbours
    public static Holder<Biome> at(ServerLevel level, BlockPos at) {
        return level.getNoiseBiome(QuartPos.fromBlock(at.getX()), QuartPos.fromBlock(at.getY()),
                QuartPos.fromBlock(at.getZ()));
    }

    public static boolean isAlready(ServerLevel level, BlockPos at, Holder<Biome> biome) {
        return at(level, at).value() == biome.value();
    }

    public static boolean paint(ServerLevel level, BlockPos at, Holder<Biome> biome) {
        return paint(level, at, biome, true);
    }

    // each resend ships the whole chunk's biomes, so heavy painters pass tell=false and resend now and then
    public static boolean paint(ServerLevel level, BlockPos at, Holder<Biome> biome, boolean tell) {
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

        BiomeResolver resolver = (x, y, z, sampler) -> x == wantX && y == wantY && z == wantZ
                ? biome
                : section.getNoiseBiome(x & 3, y & 3, z & 3);
        section.fillBiomesFromNoise(resolver, level.getChunkSource().randomState().sampler(), originX, originY, originZ);

        chunk.setUnsaved(true);
        if (tell) {
            level.getChunkSource().chunkMap.resendBiomesForChunks(List.of(chunk));
        }
        return true;
    }
}
