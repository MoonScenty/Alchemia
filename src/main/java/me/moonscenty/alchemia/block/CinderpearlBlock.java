package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.registry.ModTags;
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
 * A desert flower that smoulders, trailing smoke and flame.
 */
public class CinderpearlBlock extends BushBlock {
    public static final MapCodec<CinderpearlBlock> CODEC = simpleCodec(CinderpearlBlock::new);
    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 13, 13);

    public CinderpearlBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<CinderpearlBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(ModTags.Blocks.CINDERPEARL_PLACEABLE);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!random.nextBoolean()) {
            return;
        }

        double x = pos.getX() + 0.5 + (random.nextDouble() - random.nextDouble()) * 0.2;
        double y = pos.getY() + 0.6 + (random.nextDouble() - random.nextDouble()) * 0.2;
        double z = pos.getZ() + 0.5 + (random.nextDouble() - random.nextDouble()) * 0.2;
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
        level.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0, 0);
    }
}
