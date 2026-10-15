package me.moonscenty.alchemia.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A tallow candle, the original's {@code BlockCandle}: a stub of wax in one of the sixteen dye colours, burning
 * bright enough to read by, standing on anything with a flat top. Nothing bumps into it. It steadies an infusion
 * as a skull does, which is why anybody makes sixteen colours of them.
 */
public class TallowCandleBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(6, 0, 6, 10, 8, 10);
    private final DyeColor colour;

    public TallowCandleBlock(DyeColor colour, Properties properties) {
        super(properties);
        this.colour = colour;
    }

    public DyeColor colour() {
        return colour;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction side, BlockState other, LevelAccessor level,
            BlockPos pos, BlockPos otherPos) {
        return side == Direction.DOWN && !canSurvive(state, level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, side, other, level, pos, otherPos);
    }

    /** A wisp of smoke and a flame at the wick, every time it is asked. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.7;
        double z = pos.getZ() + 0.5;
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
        level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0);
    }
}
