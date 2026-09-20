package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.WoodSet;
import me.moonscenty.alchemia.worldgen.ModWorldgen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class WorldgenTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final BlockPos TREE_BASE = new BlockPos(15, 2, 15);

    @GameTest(template = TEMPLATE)
    public static void greatwoodTreeGrows(GameTestHelper helper) {
        growAndCheck(helper, ModWorldgen.GREATWOOD_TREE, ModBlocks.GREATWOOD, 4 * 12 + 10, 150);
    }

    @GameTest(template = TEMPLATE)
    public static void silverwoodTreeGrows(GameTestHelper helper) {
        growAndCheck(helper, ModWorldgen.SILVERWOOD_TREE, ModBlocks.SILVERWOOD, 5 * 7 + 8, 100);
    }

    /** The silverwood carries its own undergrowth, so both plants must appear with the tree. */
    @GameTest(template = TEMPLATE)
    public static void silverwoodBringsUndergrowth(GameTestHelper helper) {
        layGround(helper, 9);
        place(helper, ModWorldgen.SILVERWOOD_TREE);

        int shimmerleaf = count(helper, ModBlocks.SHIMMERLEAF.get());
        int vishroom = count(helper, ModBlocks.VISHROOM.get());
        Alchemia.LOGGER.info("undergrowth: {} shimmerleaf, {} vishroom", shimmerleaf, vishroom);
        helper.assertTrue(shimmerleaf > 0, "no shimmerleaf grew under the silverwood");
        helper.assertTrue(vishroom > 0, "no vishroom grew under the silverwood");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void cinderpearlPatchGrows(GameTestHelper helper) {
        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {
                helper.setBlock(TREE_BASE.offset(x, -1, z), Blocks.SAND);
            }
        }
        place(helper, ModWorldgen.CINDERPEARL_PATCH);

        helper.assertTrue(count(helper, ModBlocks.CINDERPEARL.get()) > 0, "no cinderpearl grew on the sand");
        helper.succeed();
    }

    private static void growAndCheck(GameTestHelper helper, ResourceKey<ConfiguredFeature<?, ?>> tree, WoodSet wood, int minLogs, int minLeaves) {
        layGround(helper, 3);
        place(helper, tree);

        int logs = count(helper, wood.log().get());
        int leaves = count(helper, wood.leaves().get());
        Alchemia.LOGGER.info("{}: {} logs, {} leaves", tree.location(), logs, leaves);
        helper.assertTrue(logs >= minLogs, "expected at least " + minLogs + " logs but found " + logs);
        helper.assertTrue(leaves >= minLeaves, "expected at least " + minLeaves + " leaves but found " + leaves);
        helper.assertBlockPresent(wood.log().get(), TREE_BASE);
        helper.succeed();
    }

    private static void layGround(GameTestHelper helper, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                helper.setBlock(TREE_BASE.offset(x, -1, z), Blocks.GRASS_BLOCK);
            }
        }
    }

    private static void place(GameTestHelper helper, ResourceKey<ConfiguredFeature<?, ?>> key) {
        ServerLevel level = helper.getLevel();
        ConfiguredFeature<?, ?> feature = level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE).getOrThrow(key);
        boolean placed = feature.place(level, level.getChunkSource().getGenerator(), level.getRandom(), helper.absolutePos(TREE_BASE));
        helper.assertTrue(placed, key.location() + " refused to generate");
    }

    private static int count(GameTestHelper helper, Block block) {
        int[] found = new int[1];
        helper.forEveryBlockInStructure(pos -> {
            if (helper.getBlockState(pos).is(block)) {
                found[0]++;
            }
        });
        return found[0];
    }
}
