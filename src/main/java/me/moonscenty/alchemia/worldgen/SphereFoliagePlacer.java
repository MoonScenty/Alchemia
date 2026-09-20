package me.moonscenty.alchemia.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.moonscenty.alchemia.registry.ModFeatures;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

/**
 * A ball of leaves with a ragged surface. {@code stretch} pulls the ball upwards into a capsule of that many extra blocks.
 */
public class SphereFoliagePlacer extends FoliagePlacer {
    public static final MapCodec<SphereFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> foliagePlacerParts(instance)
            .and(Codec.intRange(0, 16).fieldOf("stretch").forGetter(placer -> placer.stretch))
            .apply(instance, SphereFoliagePlacer::new));

    private final int stretch;

    public SphereFoliagePlacer(IntProvider radius, IntProvider offset, int stretch) {
        super(radius, offset);
        this.stretch = stretch;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return ModFeatures.SPHERE_FOLIAGE.get();
    }

    @Override
    protected void createFoliage(LevelSimulatedReader level, FoliageSetter blockSetter, RandomSource random, TreeConfiguration config,
            int maxFreeTreeHeight, FoliageAttachment attachment, int foliageHeight, int foliageRadius, int offset) {
        int radius = foliageRadius + attachment.radiusOffset();
        // a two block wide trunk has its center between the blocks
        double centerShift = attachment.doubleTrunk() ? 0.5 : 0;
        BlockPos center = attachment.pos().above(offset);
        int reach = radius + 2;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = -reach; y <= reach + stretch; y++) {
            double dy = y - Mth.clamp(y, 0, stretch);
            for (int x = -reach; x <= reach + 1; x++) {
                for (int z = -reach; z <= reach + 1; z++) {
                    double dx = x - centerShift;
                    double dz = z - centerShift;
                    if (dx * dx + dy * dy + dz * dz < radius * radius + 1 + random.nextInt(radius * 2 + 2)) {
                        pos.setWithOffset(center, x, y, z);
                        tryPlaceLeaf(level, blockSetter, random, config, pos);
                    }
                }
            }
        }
    }

    @Override
    public int foliageHeight(RandomSource random, int height, TreeConfiguration config) {
        return 0;
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource random, int localX, int localY, int localZ, int range, boolean large) {
        return false;
    }
}
