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

/**
 * Purifying fluid standing in the world.
 *
 * <p>A player who steps into a still pool of it, and is not warded already, comes out warded against warp, and the
 * pool is used up. The ward lasts a long while for someone barely touched by warp and less the more deeply it has
 * set in: the original's two hundred thousand ticks over the square root of the lasting warp, at most some
 * twenty-seven minutes.
 */
public class PurifyingFluidBlock extends LiquidBlock {
    /** The original's numbers: what the ward is worth at no warp, and the most it is ever given for. */
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
            // not removeBlock, which puts back whatever liquid stands in the place -- which is this
            level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),
                    net.minecraft.world.level.block.Block.UPDATE_ALL);
        }
        super.entityInside(state, level, pos, entity);
    }

    /** How long a dip wards someone with this much warp. */
    public static int wardFor(WarpData warp) {
        int lasting = warp.permanent();
        int divisor = Math.max(1, (int) Math.sqrt(lasting));
        return Math.min(LONGEST, WORTH / divisor);
    }

    /** Bubbles rising through it now and then, and a pop at the surface. */
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
