package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A pale flower that grows in the shade of silverwood and gives off cold sparks.
 */
public class ShimmerleafBlock extends BushBlock {
    public static final MapCodec<ShimmerleafBlock> CODEC = simpleCodec(ShimmerleafBlock::new);
    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 13, 13);

    public ShimmerleafBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<ShimmerleafBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) != 0) {
            return;
        }

        double x = pos.getX() + 0.5 + (random.nextDouble() - random.nextDouble()) * 0.2;
        double y = pos.getY() + 0.5 + (random.nextDouble() - random.nextDouble()) * 0.3;
        double z = pos.getZ() + 0.5 + (random.nextDouble() - random.nextDouble()) * 0.2;
        level.addParticle(ParticleTypes.END_ROD, x, y, z, 0, 0.01, 0);
    }
}
