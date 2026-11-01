package me.moonscenty.alchemia.block;

import me.moonscenty.alchemia.player.WarpData;
import me.moonscenty.alchemia.player.effect.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

public class PurifyingFluidBlock extends LiquidBlock {
    // original's values: ward ticks = WORTH / sqrt(permanent warp), capped
    private static final int WORTH = 200_000;
    private static final int LONGEST = 32_000;

    public PurifyingFluidBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && state.getValue(LEVEL) == 0 && entity instanceof Player player
                && !player.hasEffect(ModEffects.WARP_WARD)) {
            player.addEffect(new MobEffectInstance(ModEffects.WARP_WARD, wardFor(WarpData.of(player)), 0,
                    true, true));
            // not removeBlock, which would put this fluid back
            level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),
                    net.minecraft.world.level.block.Block.UPDATE_ALL);
        }
        super.entityInside(state, level, pos, entity);
    }

    public static int wardFor(WarpData warp) {
        int lasting = warp.permanent();
        int divisor = Math.max(1, (int) Math.sqrt(lasting));
        return Math.min(LONGEST, WORTH / divisor);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(10) == 0) {
            level.addParticle(ParticleTypes.BUBBLE_POP, pos.getX() + random.nextDouble(),
                    pos.getY() + 0.125 * (8 - state.getValue(LEVEL) % 8), pos.getZ() + random.nextDouble(), 0, 0, 0);
        }
        if (random.nextInt(50) == 0) {
            level.playLocalSound(pos.getX() + random.nextDouble(), pos.getY() + 1.0, pos.getZ() + random.nextDouble(),
                    SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.1F + random.nextFloat() * 0.1F,
                    0.9F + random.nextFloat() * 0.15F, false);
        }
        super.animateTick(state, level, pos, random);
    }
}
