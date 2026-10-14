package me.moonscenty.alchemia.block.eldritch;

import me.moonscenty.alchemia.client.EldritchEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

/**
 * Liquid death: a purple, glowing ooze that dissolves whatever lives and stands in it -- five a hit at its source,
 * a point less with each step it has flowed. It bubbles, and now and then pops.
 */
public class LiquidDeathBlock extends LiquidBlock {
    public LiquidDeathBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity) {
            int flowed = Math.min(state.getValue(LEVEL), 3);
            entity.hurt(level.damageSources().magic(), 4 - flowed + 1);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(20) == 0) {
            EldritchEffects.deathBubble(level, pos, state.getValue(LEVEL), random);
        }
        if (random.nextInt(50) == 0) {
            level.playLocalSound(pos.getX() + random.nextFloat(), pos.getY() + 0.5, pos.getZ() + random.nextFloat(),
                    SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.1F + random.nextFloat() * 0.1F,
                    0.9F + random.nextFloat() * 0.15F, false);
        }
    }
}
