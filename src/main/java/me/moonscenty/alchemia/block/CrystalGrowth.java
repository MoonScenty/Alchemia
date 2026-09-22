package me.moonscenty.alchemia.block;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aura.AuraGeneration;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * How a crystal lives off the aura around it.
 * <p>
 * A crystal is aura that has set hard. Where there is more than the land can hold it grows and, once grown, seeds
 * the rock around it; where the land has been run dry it gives itself back, a size at a time, and the last of it
 * goes with a little flux. Where flux has got the upper hand, the crystal turns.
 * <p>
 * Each seeding is a generation on from its parent. Later generations tick less often, stop growing sooner and
 * the fourth does not seed at all, which is what keeps a lucky cave from filling to the roof.
 */
public final class CrystalGrowth {
    /** Below this share of the base the land is starved, and above the base by this much it is glutted. */
    private static final int STARVED_DIVISOR = 8;
    private static final int GLUT_OVER = AuraGeneration.BASE / 16;

    /** What a step of growth costs the land, and what a step of shrinking gives back. */
    private static final int GROW_COST = AuraGeneration.BASE / 8;
    private static final int SHRINK_REFUND = AuraGeneration.BASE / 9;

    /** How often a crystal that could seed actually finds somewhere to. */
    private static final int SEED_CHANCE = 16;
    /** One seeding in this many comes out no further down the line than its parent. */
    private static final int SAME_GENERATION_CHANCE = 8;

    private CrystalGrowth() {
    }

    public static void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!(state.getBlock() instanceof CrystalBlock crystal)) {
            return;
        }
        int generation = state.getValue(CrystalBlock.GENERATION);
        // the further down the line a crystal is, the more slowly it goes about anything
        if (random.nextInt(3 + generation) != 0) {
            return;
        }
        if (!AuraHandler.at(level, pos).exists()) {
            return;
        }

        Holder<Aspect> aspect = crystal.getType().aspect();
        int here = AuraHandler.get(level, pos, aspect);
        int base = AuraHandler.base(level, pos);
        int age = state.getValue(CrystalBlock.AGE);

        if (here < base / STARVED_DIVISOR) {
            starve(state, level, pos, crystal, aspect, age, random);
        } else if (here >= base + GLUT_OVER) {
            feast(state, level, pos, crystal, aspect, age, generation, random);
        } else if (crystal.getType() != CrystalType.FLUX) {
            sour(state, level, pos, aspect, here, base, age);
        }
    }

    /** With nothing to live on it gives itself back, and the last of it goes when it is the odd one out no longer. */
    private static void starve(BlockState state, ServerLevel level, BlockPos pos, CrystalBlock crystal,
            Holder<Aspect> aspect, int age, RandomSource random) {
        if (age > 0) {
            level.setBlock(pos, state.setValue(CrystalBlock.AGE, age - 1), Block.UPDATE_ALL);
            AuraHandler.recharge(level, pos, aspect, SHRINK_REFUND, random);
        } else if (touching(level, pos, crystal)) {
            level.removeBlock(pos, false);
            AuraHandler.recharge(level, pos, aspect, SHRINK_REFUND, random);
            AuraHandler.recharge(level, pos, ModAspects.FLUX, 1, random);
        }
    }

    /** With more than the land can hold it grows, and once it has grown as far as it will, it seeds. */
    private static void feast(BlockState state, ServerLevel level, BlockPos pos, CrystalBlock crystal,
            Holder<Aspect> aspect, int age, int generation, RandomSource random) {
        if (age < CrystalBlock.MAX_AGE && age < fullGrown(pos, generation)) {
            if (AuraHandler.drain(level, pos, aspect, GROW_COST)) {
                level.setBlock(pos, state.setValue(CrystalBlock.AGE, age + 1), Block.UPDATE_ALL);
            }
            return;
        }
        if (generation >= CrystalBlock.LAST_GENERATION) {
            return;
        }

        BlockPos seedAt = pos.offset(random.nextInt(3) - 1, random.nextInt(3) - 1, random.nextInt(3) - 1);
        if (seedAt.equals(pos) || !roomFor(level, seedAt) || random.nextInt(SEED_CHANCE) != 0) {
            return;
        }
        Direction support = support(level, seedAt, random);
        if (support == null || !AuraHandler.drain(level, pos, aspect, GROW_COST)) {
            return;
        }

        int next = random.nextInt(SAME_GENERATION_CHANCE) == 0 ? generation : generation + 1;
        level.setBlock(seedAt, crystal.defaultBlockState()
                .setValue(CrystalBlock.FACING, support.getOpposite())
                .setValue(CrystalBlock.GENERATION, next), Block.UPDATE_ALL);
    }

    /** Where flux has the upper hand over what the crystal is made of, it turns. */
    private static void sour(BlockState state, ServerLevel level, BlockPos pos, Holder<Aspect> aspect, int here,
            int base, int age) {
        int flux = AuraHandler.get(level, pos, ModAspects.FLUX);
        if (flux > here && flux > base / 2 && AuraHandler.drain(level, pos, ModAspects.FLUX, age + 1)) {
            BlockState turned = ModBlocks.CRYSTALS.get(CrystalType.FLUX).get().defaultBlockState()
                    .setValue(CrystalBlock.AGE, age)
                    .setValue(CrystalBlock.FACING, state.getValue(CrystalBlock.FACING))
                    .setValue(CrystalBlock.WATERLOGGED, state.getValue(CrystalBlock.WATERLOGGED))
                    .setValue(CrystalBlock.GENERATION, state.getValue(CrystalBlock.GENERATION));
            level.setBlock(pos, turned, Block.UPDATE_ALL);
        }
    }

    /**
     * How large a crystal of this generation gets, which is less the further down the line it is.
     * <p>
     * A little of it depends on where the crystal stands, so a patch is not all cut to the same height.
     */
    public static int fullGrown(BlockPos pos, int generation) {
        return (5 - generation) + (int) Math.floorMod(pos.asLong(), 3L);
    }

    private static boolean touching(LevelReader level, BlockPos pos, Block block) {
        for (Direction side : Direction.values()) {
            if (level.getBlockState(pos.relative(side)).is(block)) {
                return true;
            }
        }
        return false;
    }

    /** Somewhere a seed can take: open, and not under water, which a crystal that small would be lost in. */
    private static boolean roomFor(LevelReader level, BlockPos pos) {
        BlockState there = level.getBlockState(pos);
        return (there.isAir() || there.canBeReplaced()) && there.getFluidState().isEmpty();
    }

    /**
     * A face of rock a crystal could stand on, looked for in no particular order.
     *
     * @return which way the rock lies from the spot, or null if there is none
     */
    public static Direction support(LevelReader level, BlockPos pos, RandomSource random) {
        for (Direction direction : Direction.allShuffled(random)) {
            BlockPos supportPos = pos.relative(direction);
            BlockState state = level.getBlockState(supportPos);
            if (state.is(ModTags.Blocks.CRYSTAL_GROWABLE) && state.isFaceSturdy(level, supportPos, direction.getOpposite())) {
                return direction;
            }
        }
        return null;
    }
}
