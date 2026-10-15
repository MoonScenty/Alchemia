package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.taint.TaintDustBlock;
import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Taint dust: what dead taint leaves, falling and settling like poured sand. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class TaintDustTests {
    private static final String TEMPLATE = "empty_32x40x32";

    /** A full heap over open air falls, and on flat ground spreads a depth to the side. */
    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void dustFallsAndSettles(GameTestHelper helper) {
        for (int x = 2; x <= 6; x++) {
            for (int z = 2; z <= 6; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
            }
        }
        BlockPos dropped = new BlockPos(4, 4, 4);
        helper.setBlock(dropped, TaintDustBlock.of(ModBlocks.TAINT_DUST.get(), TaintDustBlock.FULL));
        helper.succeedWhen(() -> {
            helper.assertBlockNotPresent(ModBlocks.TAINT_DUST.get(), dropped);
            boolean spread = false;
            for (var side : net.minecraft.core.Direction.Plane.HORIZONTAL) {
                spread |= helper.getBlockState(new BlockPos(4, 2, 4).relative(side)).is(ModBlocks.TAINT_DUST.get());
            }
            helper.assertTrue(helper.getBlockState(new BlockPos(4, 2, 4)).is(ModBlocks.TAINT_DUST.get()),
                    "it fell to the ground");
            helper.assertTrue(spread, "and spread a little to the side");
        });
    }

    /** A dead tainted log crumbles to a full heap. */
    @GameTest(template = TEMPLATE)
    public static void deadLogCrumbles(GameTestHelper helper) {
        BlockPos at = new BlockPos(4, 2, 4);
        helper.setBlock(at.below(), Blocks.STONE);
        helper.setBlock(at, ModBlocks.TAINT_LOG.get());
        ((me.moonscenty.alchemia.block.taint.TaintLogBlock) ModBlocks.TAINT_LOG.get()).die(helper.getLevel(),
                helper.absolutePos(at), helper.getBlockState(at));
        helper.assertBlockPresent(ModBlocks.TAINT_DUST.get(), at);
        helper.assertValueEqual(helper.getBlockState(at).getValue(TaintDustBlock.LEVEL), TaintDustBlock.FULL,
                "a full heap");
        helper.succeed();
    }
}
