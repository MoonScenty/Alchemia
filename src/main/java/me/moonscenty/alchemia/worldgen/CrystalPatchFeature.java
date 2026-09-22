package me.moonscenty.alchemia.worldgen;

import com.mojang.serialization.Codec;

import me.moonscenty.alchemia.block.CrystalBlock;
import me.moonscenty.alchemia.block.CrystalGrowth;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.material.Fluids;

/**
 * Scatters crystals through the 3x3x3 area around the origin, on every open spot that touches stone.
 */
public class CrystalPatchFeature extends Feature<BlockStateConfiguration> {
    public CrystalPatchFeature(Codec<BlockStateConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<BlockStateConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockState crystal = context.config().state;
        boolean placed = false;

        for (BlockPos pos : BlockPos.betweenClosed(context.origin().offset(-1, -1, -1), context.origin().offset(1, 1, 1))) {
            if (random.nextInt(3) == 0) {
                continue;
            }

            BlockState existing = level.getBlockState(pos);
            boolean water = existing.getFluidState().is(Fluids.WATER) && existing.getFluidState().isSource();
            if (!existing.isAir() && !(water && existing.canBeReplaced())) {
                continue;
            }

            Direction support = CrystalGrowth.support(level, pos, random);
            if (support == null) {
                continue;
            }

            level.setBlock(pos, crystal
                    .setValue(CrystalBlock.FACING, support.getOpposite())
                    .setValue(CrystalBlock.AGE, 1 + random.nextInt(CrystalBlock.MAX_AGE))
                    .setValue(CrystalBlock.WATERLOGGED, water), 2);
            placed = true;
        }

        return placed;
    }
}
