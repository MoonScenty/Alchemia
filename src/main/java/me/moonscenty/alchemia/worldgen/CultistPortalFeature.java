package me.moonscenty.alchemia.worldgen;

import com.mojang.serialization.Codec;

import me.moonscenty.alchemia.entity.cult.CultistPortal;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Now and then a lesser crimson portal hangs over bare ground -- stone, earth, sand or snow, never grass -- waiting
 * for somebody to come near enough to wake it.
 */
public class CultistPortalFeature extends Feature<NoneFeatureConfiguration> {
    public CultistPortalFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos ground = context.origin().below();
        BlockState there = level.getBlockState(ground);
        boolean bare = there.is(BlockTags.BASE_STONE_OVERWORLD) || there.is(Blocks.DIRT)
                || there.is(Blocks.COARSE_DIRT) || there.is(BlockTags.SAND) || there.is(Blocks.GRAVEL)
                || there.is(Blocks.SNOW_BLOCK) || there.is(Blocks.SNOW) || there.is(Blocks.CLAY);
        if (!bare) {
            return false;
        }
        CultistPortal portal = new CultistPortal(level.getLevel());
        portal.moveTo(ground.getX() + 0.5, ground.getY() + 1, ground.getZ() + 0.5, 0.0F, 0.0F);
        portal.finalizeSpawn(level, level.getCurrentDifficultyAt(ground), MobSpawnType.STRUCTURE, null);
        return level.addFreshEntity(portal);
    }
}
