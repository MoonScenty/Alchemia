package me.moonscenty.alchemia.worldgen;

import com.mojang.serialization.Codec;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aura.BiomeLeanings;
import me.moonscenty.alchemia.block.CrystalBlock;
import me.moonscenty.alchemia.block.CrystalGrowth;
import me.moonscenty.alchemia.block.CrystalType;
import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

// Up to 8 clusters a chunk, stopping once more than 64 crystals are down.
public class CrystalClustersFeature extends Feature<NoneFeatureConfiguration> {
    private static final int CLUSTERS = 8;
    private static final int MOST = 64;
    private static final CrystalType[] NATURAL = java.util.Arrays.stream(CrystalType.values())
            .filter(CrystalType::generatesNaturally).toArray(CrystalType[]::new);

    public CrystalClustersFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos corner = context.origin();
        int grown = 0;

        for (int i = 0; i < CLUSTERS && grown <= MOST; i++) {
            int x = corner.getX() + random.nextInt(16);
            int z = corner.getZ() + random.nextInt(16);
            int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
            BlockPos centre = new BlockPos(x, random.nextInt(Math.max(5, surface - 5)), z);
            grown += cluster(level, centre, pick(level, centre, random), random);
        }
        return grown > 0;
    }

    private static CrystalType pick(WorldGenLevel level, BlockPos at, RandomSource random) {
        CrystalType type = NATURAL[random.nextInt(NATURAL.length)];
        if (random.nextInt(3) == 0) {
            Holder<Aspect> leaning = BiomeLeanings.anyOf(level.getBiome(at), random);
            if (leaning == null) {
                return NATURAL[random.nextInt(NATURAL.length)];
            }
            for (CrystalType natural : NATURAL) {
                if (natural.aspect().value() == leaning.value()) {
                    return natural;
                }
            }
        }
        return type;
    }

    public static int cluster(WorldGenLevel level, BlockPos centre, CrystalType type, RandomSource random) {
        BlockState crystal = ModBlocks.CRYSTALS.get(type).get().defaultBlockState();
        int grown = 0;
        for (BlockPos pos : BlockPos.betweenClosed(centre.offset(-1, -1, -1), centre.offset(1, 1, 1))) {
            if (random.nextInt(3) == 0) {
                continue;
            }
            BlockState existing = level.getBlockState(pos);
            if (!existing.getFluidState().isEmpty() || !existing.canBeReplaced()) {
                continue;
            }
            Direction support = CrystalGrowth.support(level, pos, random);
            if (support == null) {
                continue;
            }
            int size = 1 + random.nextInt(CrystalBlock.MAX_AGE);
            level.setBlock(pos, crystal
                    .setValue(CrystalBlock.FACING, support.getOpposite())
                    .setValue(CrystalBlock.AGE, size), 2);
            grown += size;
        }
        return grown;
    }
}
