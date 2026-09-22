package me.moonscenty.alchemia.block.taint;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A log the taint has got into. It keeps the grain of the tree it was, spreads like the rest, and rots away to
 * nothing when the flux runs out. The original left tainted dust behind; that arrives with the flux goo.
 */
public class TaintLogBlock extends RotatedPillarBlock implements Tainted {
    private static final int DIE_CHANCE = 10;

    public TaintLogBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void die(ServerLevel level, BlockPos pos, BlockState state) {
        level.removeBlock(pos, false);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (TaintSpread.starved(level, pos) && random.nextInt(DIE_CHANCE) == 0) {
            die(level, pos, state);
        } else {
            TaintSpread.spread(level, pos, random);
        }
    }

    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return true;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 5;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 5;
    }
}
