package me.moonscenty.alchemia.block.taint;

import me.moonscenty.alchemia.AlchemiaConfig;
import me.moonscenty.alchemia.aura.AuraGeneration;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.aura.BiomePaint;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModSounds;
import me.moonscenty.alchemia.registry.ModTags;
import me.moonscenty.alchemia.worldgen.ModBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class TaintSpread {
    public static final int STARVES_BELOW = AuraGeneration.BASE / 25;
    public static final int SPREADS_ABOVE = AuraGeneration.BASE / 5;

    private static final float TOO_HARD = 10.0F;

    private TaintSpread() {
    }

    public static boolean starved(ServerLevel level, BlockPos pos) {
        return AuraHandler.get(level, pos, ModAspects.FLUX) < STARVES_BELOW;
    }

    public static void spread(ServerLevel level, BlockPos from, RandomSource random) {
        if (!AlchemiaConfig.TAINT_SPREADS.get() || AuraHandler.get(level, from, ModAspects.FLUX) <= SPREADS_ABOVE) {
            return;
        }
        spoil(level, from, random.nextInt(10) == 0);
        BlockPos at = from.offset(random.nextInt(3) - 1, random.nextInt(3) - 1, random.nextInt(3) - 1);
        if (at.equals(from)) {
            return;
        }
        BlockState state = level.getBlockState(at);
        float hardness = state.getDestroySpeed(level, at);
        if (hardness < 0.0F || hardness > TOO_HARD) {
            return;
        }

        boolean leaves = state.is(BlockTags.LEAVES);
        boolean soft = state.isAir() || state.canBeReplaced() || state.getBlock() instanceof BushBlock;
        if (leaves || (soft && state.getFluidState().isEmpty() && besideSolid(level, at) && !onlyBesideTaint(level, at))) {
            settle(level, at, fibres(level, at), random, 1);
            return;
        }

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

    public static BlockState fibres(LevelReader level, BlockPos at) {
        return TaintFibreBlock.fitted(level, at, ModBlocks.TAINT_FIBRE.get().defaultBlockState());
    }

    private static void settle(ServerLevel level, BlockPos at, BlockState taint, RandomSource random, int costTimes) {
        if (random.nextFloat() < AlchemiaConfig.TAINT_SPREAD_COST.get() * costTimes) {
            AuraHandler.drainAvailable(level, at, ModAspects.FLUX, 1);
        }
        level.setBlock(at, taint, Block.UPDATE_ALL);
        level.playSound(null, at, ModSounds.ROOTS.get(), SoundSource.BLOCKS, 0.1F,
                0.9F + random.nextFloat() * 0.2F);
        spoil(level, at, true);
    }

    public static void spoil(ServerLevel level, BlockPos at, boolean tell) {
        BiomePaint.named(level, ModBiomes.TAINTED_LAND).ifPresent(land -> BiomePaint.paint(level, at, land, tell));
    }

    public static void heal(ServerLevel level, BlockPos at, RandomSource random, int tellOneIn) {
        BiomePaint.restore(level, at, ModBiomes.TAINTED_LAND, random.nextInt(tellOneIn) == 0);
    }

    public static boolean besideSolid(LevelReader level, BlockPos pos) {
        for (Direction side : Direction.values()) {
            if (TaintFibreBlock.clingsTo(level, pos, side)) {
                return true;
            }
        }
        return false;
    }

    // fibres need something untainted to cling to, which keeps them at the edge of a patch
    public static boolean onlyBesideTaint(LevelReader level, BlockPos pos) {
        for (Direction side : Direction.values()) {
            BlockPos next = pos.relative(side);
            BlockState there = level.getBlockState(next);
            if (!there.isAir() && !there.is(ModTags.Blocks.TAINT) && TaintFibreBlock.clingsTo(level, pos, side)) {
                return false;
            }
        }
        return true;
    }

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
