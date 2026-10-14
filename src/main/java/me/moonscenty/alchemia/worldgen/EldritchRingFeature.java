package me.moonscenty.alchemia.worldgen;

import com.mojang.serialization.Codec;

import me.moonscenty.alchemia.block.BannerBlockEntity;
import me.moonscenty.alchemia.block.eldritch.EldritchAltarBlockEntity;
import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * An eldritch ring: a bowl of obsidian and eldritch stone sunk into open ground, an altar at its heart with the shut
 * way to the Outer Lands over it and an obelisk over that, and eight capstones standing round. Two in five are a
 * Crimson Cult altar with the cult's banners about it, one in five a guardian's; the rest are only stone.
 * <p>
 * The original also began building the Outer Lands the moment it raised a ring. That comes with the Outer Lands.
 */
public class EldritchRingFeature extends Feature<NoneFeatureConfiguration> {
    public EldritchRingFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        // the top block of the ground, not the air over it
        BlockPos o = context.origin().below();
        for (int[] at : new int[][] {{-3, -3}, {0, 0}, {3, 0}, {3, 3}, {0, 3}}) {
            if (!ground(level, o.offset(at[0], 0, at[1]))) {
                return false;
            }
        }
        // the bowl, and clear air over it
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                for (int dy = -3; dy <= 4; dy++) {
                    BlockPos at = o.offset(dx, dy, dz);
                    if (dy <= 0) {
                        if (dx * dx + dy * dy + dz * dz < 15) {
                            set(level, at, random.nextInt(3) == 0
                                    ? ModBlocks.ELDRITCH_STONE.block().get().defaultBlockState()
                                    : Blocks.OBSIDIAN.defaultBlockState());
                        }
                    } else {
                        set(level, at, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
        // the heart: stone, altar, the shut way, the obelisk
        set(level, o, ModBlocks.ELDRITCH_STONE.block().get().defaultBlockState());
        set(level, o.above(), ModBlocks.ELDRITCH_ALTAR.get().defaultBlockState());
        set(level, o.above(2), ModBlocks.ELDRITCH_PORTAL.get().defaultBlockState());
        set(level, o.above(3), ModBlocks.ELDRITCH_OBELISK.get().defaultBlockState());
        for (int up = 4; up <= 7; up++) {
            set(level, o.above(up), ModBlocks.ELDRITCH_OBELISK_BODY.get().defaultBlockState());
        }
        int kind = random.nextInt(10);
        if (level.getBlockEntity(o.above()) instanceof EldritchAltarBlockEntity altar) {
            if (kind >= 1 && kind <= 4) {
                altar.setSpawner(true, 0);
                // the cult's banners, each turned toward the altar
                banner(level, o.offset(0, 1, -3), 0);
                banner(level, o.offset(3, 1, 0), 4);
                banner(level, o.offset(0, 1, 3), 8);
                banner(level, o.offset(-3, 1, 0), 12);
            } else if (kind == 6 || kind == 7) {
                altar.setSpawner(true, 1);
            }
        }
        // eight capstones round the edge
        for (int[] cap : new int[][] {{3, 1}, {3, -1}, {-3, 1}, {-3, -1}, {1, 3}, {-1, 3}, {1, -3}, {-1, -3}}) {
            set(level, o.offset(cap[0], 1, cap[1]), ModBlocks.ELDRITCH_CAP.get().defaultBlockState());
        }
        return true;
    }

    private static void banner(WorldGenLevel level, BlockPos at, int facing) {
        set(level, at, ModBlocks.BANNER.get().defaultBlockState());
        if (level.getBlockEntity(at) instanceof BannerBlockEntity banner) {
            banner.hang(facing, false);
        }
    }

    private static void set(WorldGenLevel level, BlockPos at, BlockState state) {
        level.setBlock(at, state, Block.UPDATE_CLIENTS);
    }

    /** Ground the original would build on: stone, sand, ice, grass, gravel or dirt, with open air above. */
    static boolean ground(WorldGenLevel level, BlockPos at) {
        BlockState there = level.getBlockState(at);
        BlockPos top = at;
        if (there.canBeReplaced() && there.getFluidState().isEmpty()) {
            top = at.below();
            there = level.getBlockState(top);
        }
        boolean fit = there.is(BlockTags.BASE_STONE_OVERWORLD) || there.is(BlockTags.SAND)
                || there.is(Blocks.PACKED_ICE) || there.is(Blocks.GRASS_BLOCK) || there.is(Blocks.GRAVEL)
                || there.is(BlockTags.DIRT);
        BlockState above = level.getBlockState(top.above());
        return fit && (above.isAir() || above.canBeReplaced() && above.getFluidState().isEmpty());
    }
}
