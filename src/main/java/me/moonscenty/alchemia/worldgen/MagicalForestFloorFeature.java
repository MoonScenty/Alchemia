package me.moonscenty.alchemia.worldgen;

import com.mojang.serialization.Codec;

import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * The magical forest's floor, as the original dressed it: one patch of grass in a chunk that glimmers at night,
 * and vishrooms put up here and there beside the roots of trees. Both look down from the top of the world for the
 * grass; the original gave up looking below a certain height.
 */
public class MagicalForestFloorFeature extends Feature<NoneFeatureConfiguration> {
    /** Which of the two it lays. */
    private final boolean glimmer;

    public MagicalForestFloorFeature(Codec<NoneFeatureConfiguration> codec, boolean glimmer) {
        super(codec);
        this.glimmer = glimmer;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos corner = new BlockPos(context.origin().getX() & ~15, 0, context.origin().getZ() & ~15);
        return glimmer ? glimmer(level, random, corner) : vishrooms(level, random, corner);
    }

    /** Three tries at grass somewhere in the middle of the chunk, and the first found glimmers. */
    private static boolean glimmer(WorldGenLevel level, RandomSource random, BlockPos corner) {
        for (int attempt = 0; attempt < 3; attempt++) {
            BlockPos at = grass(level, corner.offset(4 + random.nextInt(8), 0, 4 + random.nextInt(8)), 30);
            if (at != null) {
                level.setBlock(at, ModBlocks.AMBIENT_GRASS.get().defaultBlockState(), 2);
                return true;
            }
        }
        return false;
    }

    /** Eight tries at open grass beside a log, each putting up a vishroom where it finds one. */
    private static boolean vishrooms(WorldGenLevel level, RandomSource random, BlockPos corner) {
        boolean any = false;
        for (int attempt = 0; attempt < 8; attempt++) {
            BlockPos at = grass(level, corner.offset(random.nextInt(16), 0, random.nextInt(16)), 50);
            if (at != null && level.getBlockState(at.above()).canBeReplaced() && besideWood(level, at.above())) {
                level.setBlock(at.above(), ModBlocks.VISHROOM.get().defaultBlockState(), 2);
                any = true;
            }
        }
        return any;
    }

    /** The grass under the top of the world here, looking no lower than the floor given. */
    private static BlockPos grass(WorldGenLevel level, BlockPos column, int floor) {
        BlockPos at = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, column);
        while (at.getY() > floor && !level.getBlockState(at).is(Blocks.GRASS_BLOCK)) {
            at = at.below();
        }
        return level.getBlockState(at).is(Blocks.GRASS_BLOCK) ? at : null;
    }

    private static boolean besideWood(WorldGenLevel level, BlockPos at) {
        for (BlockPos near : BlockPos.betweenClosed(at.offset(-1, -1, -1), at.offset(1, 1, 1))) {
            if (!near.equals(at) && level.getBlockState(near).is(BlockTags.LOGS)) {
                return true;
            }
        }
        return false;
    }
}
