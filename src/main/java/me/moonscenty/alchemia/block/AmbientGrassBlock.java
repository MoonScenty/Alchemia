package me.moonscenty.alchemia.block;

import me.moonscenty.alchemia.client.MagicalForestEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Grass that glimmers: one patch in each chunk of magical forest. At night it sets little drifting motes of light
 * over the grass around it, more the darker it is. Otherwise it is grass, and what it spreads is ordinary grass.
 */
public class AmbientGrassBlock extends GrassBlock {
    public AmbientGrassBlock(Properties properties) {
        super(properties);
    }

    /** It dies back to dirt as grass does, and spreads ordinary grass, as the original's did. */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Blocks.GRASS_BLOCK.defaultBlockState().randomTick(level, pos, random);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        MagicalForestEffects.motes(level, pos, random);
    }
}
