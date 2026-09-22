package me.moonscenty.alchemia.block.taint;

import me.moonscenty.alchemia.AlchemiaConfig;
import me.moonscenty.alchemia.aura.AuraGeneration;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * How the taint creeps.
 * <p>
 * Every tainted block, when its turn comes round, looks at one spot next to it. Open ground gets fibres. A block the
 * taint has all but surrounded is eaten: dirt to tainted soil, stone to tainted rock, wood to crust, a log to a
 * tainted log. Each step is paid for, now and then, with a point of flux from the aura, which is what keeps a
 * spreading taint burning down the very thing that feeds it.
 */
public final class TaintSpread {
    /** Below this much flux nothing tainted lives; above it the taint gets moving. */
    public static final int STARVES_BELOW = AuraGeneration.BASE / 25;
    public static final int SPREADS_ABOVE = AuraGeneration.BASE / 5;

    /** Anything harder than this is beyond the taint, as is anything that cannot be broken at all. */
    private static final float TOO_HARD = 10.0F;

    private TaintSpread() {
    }

    /** Whether the flux here has run so thin that tainted things start dying. */
    public static boolean starved(ServerLevel level, BlockPos pos) {
        return AuraHandler.get(level, pos, ModAspects.FLUX) < STARVES_BELOW;
    }

    /** One step of spreading from a tainted block, if the land is spoiled enough to allow it. */
    public static void spread(ServerLevel level, BlockPos from, RandomSource random) {
        if (!AlchemiaConfig.TAINT_SPREADS.get() || AuraHandler.get(level, from, ModAspects.FLUX) <= SPREADS_ABOVE) {
            return;
        }
        BlockPos at = from.offset(random.nextInt(3) - 1, random.nextInt(3) - 1, random.nextInt(3) - 1);
        if (at.equals(from)) {
            return;
        }
        BlockState state = level.getBlockState(at);
        float hardness = state.getDestroySpeed(level, at);
        if (hardness < 0.0F || hardness > TOO_HARD) {
            return;
        }

        // open ground, or something soft growing on it: fibres take hold, so long as there is something to hold to
        boolean leaves = state.is(BlockTags.LEAVES);
        boolean soft = state.isAir() || state.canBeReplaced() || state.getBlock() instanceof BushBlock;
        if (leaves || (soft && state.getFluidState().isEmpty() && besideSolid(level, at) && !onlyBesideTaint(level, at))) {
            settle(level, at, fibres(level, at), random, 1);
            return;
        }

        // anything else has to be hemmed in first, and then what it becomes depends on what it was
        if (!hemmedByTaint(level, at)) {
            return;
        }
        if (state.is(BlockTags.LOGS) && !state.is(ModTags.Blocks.TAINT)) {
            BlockState log = ModBlocks.TAINT_LOG.get().defaultBlockState();
            if (state.hasProperty(RotatedPillarBlock.AXIS)) {
                log = log.setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
            }
            settle(level, at, log, random, 2);
        } else if (state.is(ModTags.Blocks.TAINT_ROTS_TO_CRUST)) {
            settle(level, at, ModBlocks.TAINT_CRUST.get().defaultBlockState(), random, 2);
        } else if (state.is(ModTags.Blocks.TAINT_ROTS_TO_SOIL)) {
            settle(level, at, ModBlocks.TAINT_SOIL.get().defaultBlockState(), random, 2);
        } else if (state.is(ModTags.Blocks.TAINT_ROTS_TO_ROCK)) {
            settle(level, at, ModBlocks.TAINT_ROCK.get().defaultBlockState(), random, 4);
        }
    }

    /** Fibres fitted to the spot, clinging to whatever is there to cling to. */
    public static BlockState fibres(LevelReader level, BlockPos at) {
        return TaintFibreBlock.fitted(level, at, ModBlocks.TAINT_FIBRE.get().defaultBlockState());
    }

    /** Puts a tainted block down and, some of the time, charges the aura for it. */
    private static void settle(ServerLevel level, BlockPos at, BlockState taint, RandomSource random, int costTimes) {
        if (random.nextFloat() < AlchemiaConfig.TAINT_SPREAD_COST.get() * costTimes) {
            AuraHandler.drainAvailable(level, at, ModAspects.FLUX, 1);
        }
        level.setBlock(at, taint, Block.UPDATE_ALL);
    }

    public static boolean besideSolid(LevelReader level, BlockPos pos) {
        for (Direction side : Direction.values()) {
            BlockPos next = pos.relative(side);
            if (level.getBlockState(next).isFaceSturdy(level, next, side.getOpposite())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether there is nothing solid here but taint. Fibres need something untainted to cling to, which is what keeps
     * them at the edge of a patch rather than filling the middle of it.
     */
    public static boolean onlyBesideTaint(LevelReader level, BlockPos pos) {
        for (Direction side : Direction.values()) {
            BlockPos next = pos.relative(side);
            BlockState there = level.getBlockState(next);
            if (!there.isAir() && !there.is(ModTags.Blocks.TAINT) && there.isFaceSturdy(level, next, side.getOpposite())) {
                return false;
            }
        }
        return true;
    }

    /** Whether taint has more of the sides of a block than open air and soft things do. */
    public static boolean hemmedByTaint(LevelReader level, BlockPos pos) {
        int score = 0;
        for (Direction side : Direction.values()) {
            BlockPos next = pos.relative(side);
            BlockState there = level.getBlockState(next);
            if (there.is(ModTags.Blocks.TAINT)) {
                score++;
            } else if (there.isAir()) {
                score--;
            } else if (there.getFluidState().isEmpty() && !there.isFaceSturdy(level, next, side.getOpposite())) {
                score--;
            }
        }
        return score > 0;
    }
}
