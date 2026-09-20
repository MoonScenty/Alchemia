package me.moonscenty.alchemia.block;

import org.joml.Vector3f;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A mushroom swollen with raw vis. Brushing past one leaves the head swimming.
 */
public class VishroomBlock extends BushBlock {
    public static final MapCodec<VishroomBlock> CODEC = simpleCodec(VishroomBlock::new);
    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 10, 13);
    private static final DustParticleOptions HAZE = new DustParticleOptions(new Vector3f(0.5F, 0.3F, 0.8F), 1.0F);
    private static final int CONFUSION_TICKS = 200;

    public VishroomBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<VishroomBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** Unlike the other plants this one takes root on any solid block, in caves as readily as on grass. */
    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.isFaceSturdy(level, pos, Direction.UP);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity living && level.getRandom().nextInt(5) == 0) {
            living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, CONFUSION_TICKS));
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) != 0) {
            return;
        }

        double x = pos.getX() + 0.5 + (random.nextDouble() - random.nextDouble()) * 0.4;
        double y = pos.getY() + 0.3;
        double z = pos.getZ() + 0.5 + (random.nextDouble() - random.nextDouble()) * 0.4;
        level.addParticle(HAZE, x, y, z, 0, -0.01, 0);
    }
}
