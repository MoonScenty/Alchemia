package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aura.AuraChunk;
import me.moonscenty.alchemia.aura.AuraGeneration;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.aura.ModAuraAttachment;
import me.moonscenty.alchemia.block.taint.TaintDustBlock;
import me.moonscenty.alchemia.block.taint.TaintFeatureBlock;
import me.moonscenty.alchemia.block.taint.TaintFibreBlock;
import me.moonscenty.alchemia.block.taint.TaintSpread;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class TaintFeatureTests {
    private static final String TEMPLATE = "empty_32x40x32";

    private static void flux(GameTestHelper helper, BlockPos at, int flux) {
        helper.getLevel().getChunkAt(helper.absolutePos(at)).setData(ModAuraAttachment.AURA,
                new AuraChunk(AuraGeneration.BASE, AspectList.EMPTY.add(ModAspects.FLUX, flux)));
    }

    @GameTest(template = TEMPLATE)
    public static void leavesBesideATaintedLogSwell(GameTestHelper helper) {
        BlockPos log = new BlockPos(4, 3, 4);
        BlockPos leaf = log.east();
        helper.setBlock(log.below(), Blocks.STONE);
        helper.setBlock(log, ModBlocks.TAINT_LOG.get());
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        helper.setBlock(leaf, leaves);
        BlockPos from = helper.absolutePos(log);
        BlockPos where = helper.absolutePos(leaf);
        RandomSource random = RandomSource.create(3L);
        int pods = 0;
        int fibres = 0;
        for (int turn = 0; turn < 4000 && (pods == 0 || fibres == 0); turn++) {
            flux(helper, log, AuraGeneration.BASE);
            TaintSpread.spread(helper.getLevel(), from, random);
            BlockState now = helper.getLevel().getBlockState(where);
            if (now.is(ModBlocks.TAINT_FEATURE.get())) {
                helper.assertTrue(now.getValue(TaintFeatureBlock.FACING) == Direction.EAST,
                        "the pod should point away from the log, points " + now.getValue(TaintFeatureBlock.FACING));
                pods++;
            } else if (now.is(ModBlocks.TAINT_FIBRE.get())) {
                fibres++;
            }
            if (!now.equals(leaves)) {
                helper.setBlock(leaf, leaves);
            }
        }
        helper.assertTrue(pods > 0, "beside the log the leaves should sometimes swell");
        helper.assertTrue(fibres > 0, "and sometimes just grow fibres");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aStarvedPodLeavesALittleDust(GameTestHelper helper) {
        BlockPos at = new BlockPos(4, 2, 4);
        helper.setBlock(at.below(), Blocks.STONE);
        helper.setBlock(at, ModBlocks.TAINT_FEATURE.get());
        BlockPos where = helper.absolutePos(at);
        RandomSource random = RandomSource.create(5L);
        for (int turn = 0; turn < 500 && helper.getLevel().getBlockState(where).is(ModBlocks.TAINT_FEATURE.get());
                turn++) {
            flux(helper, at, 0);
            helper.getLevel().getBlockState(where).randomTick(helper.getLevel(), where, random);
        }
        BlockState left = helper.getLevel().getBlockState(where);
        helper.assertTrue(left.is(ModBlocks.TAINT_DUST.get()), "a starved pod should turn to dust, found " + left);
        helper.assertTrue(left.getValue(TaintDustBlock.LEVEL) <= 1, "and only a little, found "
                + left.getValue(TaintDustBlock.LEVEL));
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aPodTornOffItsHoldDrops(GameTestHelper helper) {
        BlockPos at = new BlockPos(4, 2, 4);
        helper.setBlock(at.below(), Blocks.STONE);
        flux(helper, at, 10);
        helper.setBlock(at, ModBlocks.TAINT_FEATURE.get());
        helper.setBlock(at.below(), Blocks.AIR);
        helper.assertBlockNotPresent(ModBlocks.TAINT_FEATURE.get(), at);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 40)
    public static void crustHangsOnBesideALog(GameTestHelper helper) {
        BlockPos crust = new BlockPos(4, 4, 4);
        helper.setBlock(crust.west(), Blocks.OAK_LOG);
        helper.setBlock(crust, ModBlocks.TAINT_CRUST.get());
        helper.runAfterDelay(20, () -> {
            helper.assertBlockPresent(ModBlocks.TAINT_CRUST.get(), crust);
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE)
    public static void aBrokenLumpLetsOutFlux(GameTestHelper helper) {
        BlockPos at = new BlockPos(4, 2, 4);
        helper.setBlock(at.below(), Blocks.STONE);
        flux(helper, at, 0);
        helper.setBlock(at, ModBlocks.TAINT_FIBRE.get().defaultBlockState().setValue(TaintFibreBlock.DOWN, true)
                .setValue(TaintFibreBlock.GROWTH, TaintFibreBlock.LUMP));
        BlockPos where = helper.absolutePos(at);
        helper.getLevel().destroyBlock(where, true);
        int flux = AuraHandler.get(helper.getLevel(), where, ModAspects.FLUX);
        helper.assertTrue(flux >= 3 && flux <= 5, "a broken lump should let out three to five flux, gave " + flux);
        helper.succeed();
    }
}
