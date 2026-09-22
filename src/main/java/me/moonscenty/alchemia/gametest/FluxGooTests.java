package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aura.AuraChunk;
import me.moonscenty.alchemia.aura.AuraGeneration;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.aura.FluxEvents;
import me.moonscenty.alchemia.aura.ModAuraAttachment;
import me.moonscenty.alchemia.aura.TaintCloud;
import me.moonscenty.alchemia.block.taint.FluxGooBlock;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Goo is the seed of the taint, so it has to move like a liquid without ever making more of itself, and it has to
 * dry into something.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class FluxGooTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final BlockPos HERE = new BlockPos(8, 6, 8);
    private static final int BASE = AuraGeneration.BASE;

    private static void flux(GameTestHelper helper, int amount) {
        helper.getLevel().getChunkAt(helper.absolutePos(HERE)).setData(ModAuraAttachment.AURA,
                new AuraChunk(BASE, AspectList.EMPTY.add(ModAspects.FLUX, amount)));
    }

    private static int fluxLeft(GameTestHelper helper) {
        return AuraHandler.get(helper.getLevel(), helper.absolutePos(HERE), ModAspects.FLUX);
    }

    private static BlockState goo(int depth) {
        return FluxGooBlock.of(ModBlocks.FLUX_GOO.get(), depth);
    }

    /** Runs the flow tick on every puddle in a small box, as the scheduler would over a few seconds. */
    private static void flow(GameTestHelper helper, int rounds) {
        RandomSource random = RandomSource.create(1L);
        for (int round = 0; round < rounds; round++) {
            for (BlockPos at : BlockPos.betweenClosed(HERE.offset(-4, -4, -4), HERE.offset(4, 2, 4))) {
                BlockState state = helper.getBlockState(at);
                if (state.is(ModBlocks.FLUX_GOO.get())) {
                    state.tick(helper.getLevel(), helper.absolutePos(at), random);
                }
            }
        }
    }

    /** Every depth counts one, so a film is one and a full block is eight; the sum must never change by flowing. */
    private static int volume(GameTestHelper helper) {
        int total = 0;
        for (BlockPos at : BlockPos.betweenClosed(HERE.offset(-4, -4, -4), HERE.offset(4, 2, 4))) {
            BlockState state = helper.getBlockState(at);
            if (state.is(ModBlocks.FLUX_GOO.get())) {
                total += state.getValue(FluxGooBlock.LEVEL) + 1;
            }
        }
        return total;
    }

    @GameTest(template = TEMPLATE)
    public static void gooRunsDownhill(GameTestHelper helper) {
        helper.setBlock(HERE.below(3), Blocks.STONE);
        helper.setBlock(HERE, goo(FluxGooBlock.FULL));

        flow(helper, 4);
        helper.assertBlockPresent(Blocks.AIR, HERE);
        // it lands on the stone and, the stone being a single block, at once starts running off the sides of it
        helper.assertBlockPresent(ModBlocks.FLUX_GOO.get(), HERE.below(2));
        helper.assertTrue(volume(helper) == FluxGooBlock.FULL + 1, "falling should not change how much there is");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void gooLevelsOutWithoutMakingMoreOfItself(GameTestHelper helper) {
        for (BlockPos floor : BlockPos.betweenClosed(HERE.offset(-4, -1, -4), HERE.offset(4, -1, 4))) {
            helper.setBlock(floor, Blocks.STONE);
        }
        helper.setBlock(HERE, goo(FluxGooBlock.FULL));
        int before = volume(helper);

        flow(helper, 30);
        int spread = 0;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (helper.getBlockState(HERE.relative(side)).is(ModBlocks.FLUX_GOO.get())) {
                spread++;
            }
        }
        helper.assertTrue(spread > 0, "a full puddle on a flat floor should have run out sideways");
        helper.assertTrue(helper.getBlockState(HERE).getValue(FluxGooBlock.LEVEL) < FluxGooBlock.FULL,
                "and be shallower for it");
        helper.assertTrue(volume(helper) == before, "flowing should neither make goo nor lose it: " + before + " -> " + volume(helper));
        helper.succeed();
    }

    /** A film of goo dries away, and what it leaves is either flux in the air or the first fibres. */
    @GameTest(template = TEMPLATE)
    public static void aFilmOfGooDriesIntoFluxOrFibres(GameTestHelper helper) {
        flux(helper, 0);
        helper.setBlock(HERE.below(), Blocks.STONE);
        helper.setBlock(HERE, goo(0));

        RandomSource random = RandomSource.create(2L);
        for (int i = 0; i < 100 && helper.getBlockState(HERE).is(ModBlocks.FLUX_GOO.get()); i++) {
            helper.getBlockState(HERE).randomTick(helper.getLevel(), helper.absolutePos(HERE), random);
        }
        BlockState left = helper.getBlockState(HERE);
        boolean fibres = left.is(ModBlocks.TAINT_FIBRE.get());
        helper.assertTrue(fibres || left.isAir(), "the film should have dried to fibres or nothing, is " + left);
        helper.assertTrue(fibres || fluxLeft(helper) > 0, "and if to nothing, its flux should be in the air");
        helper.succeed();
    }

    /** Lightning is what starts a patch in the open: it leaves a full puddle where it lands. */
    @GameTest(template = TEMPLATE)
    public static void lightningLeavesAPuddle(GameTestHelper helper) {
        flux(helper, BASE * 4);
        helper.setBlock(HERE.below(), Blocks.STONE);
        BlockPos at = helper.absolutePos(HERE);

        RandomSource random = RandomSource.create(3L);
        FluxEvents.Trouble happened = null;
        for (int i = 0; i < 200 && (happened == null || !happened.name().equals("lightning")); i++) {
            happened = FluxEvents.strikeAt(helper.getLevel(), at, random);
            // a cloud that gathers first would keep lightning off, so it is sent away again
            helper.getLevel().getEntitiesOfClass(TaintCloud.class, helper.getBounds().inflate(40)).forEach(TaintCloud::discard);
        }
        helper.assertTrue(happened != null && happened.name().equals("lightning"), "lightning should have struck by now");
        helper.assertBlockState(HERE, state -> state.is(ModBlocks.FLUX_GOO.get())
                && state.getValue(FluxGooBlock.LEVEL) == FluxGooBlock.FULL, () -> "a full puddle should be where it struck");
        helper.succeed();
    }

    /** A cloud drops films of goo and the land pays a point of flux for each one. */
    @GameTest(template = TEMPLATE)
    public static void aCloudRainsGooAtThePriceOfFlux(GameTestHelper helper) {
        flux(helper, BASE);
        helper.setBlock(HERE.below(), Blocks.STONE);
        TaintCloud cloud = new TaintCloud(helper.getLevel(), helper.absolutePos(HERE.above(10)), 600);
        helper.getLevel().addFreshEntity(cloud);

        int before = fluxLeft(helper);
        helper.assertTrue(cloud.rain(helper.absolutePos(HERE)), "the cloud should have left a puddle on the stone");
        helper.assertBlockPresent(ModBlocks.FLUX_GOO.get(), HERE);
        helper.assertTrue(fluxLeft(helper) == before - 1, "and the land should have paid one flux for it");

        // it will not rain on what is already tainted
        helper.assertTrue(!cloud.rain(helper.absolutePos(HERE)), "and it should not pile goo on goo");
        helper.succeed();
    }
}
