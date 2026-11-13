package me.moonscenty.alchemia.block;

import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

// original's electrified ground left by an earth shock
public class ShockBlock extends Block {
    public ShockBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.removeBlock(pos, false);
    }

    // original: 1-2 damage and a second of slowness, and one touch in a hundred spends it
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide) {
            return;
        }
        entity.hurt(level.damageSources().magic(), 1 + level.random.nextInt(2));
        if (entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 0, true, true));
        }
        if (level.random.nextInt(100) == 0) {
            level.removeBlock(pos, false);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        float rise = random.nextFloat() * 0.33F;
        level.addParticle(ParticleTypes.ELECTRIC_SPARK, pos.getX() + random.nextFloat(),
                pos.getY() + 0.1515F + rise / 2.0F, pos.getZ() + random.nextFloat(), 0.0, 0.0, 0.0);
        if (random.nextInt(50) == 0) {
            level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), ModSounds.JACOBS.get(), SoundSource.BLOCKS,
                    0.5F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F, false);
        }
    }
}
