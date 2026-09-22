package me.moonscenty.alchemia.gametest;

import java.util.List;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aura.node.AuraNode;
import me.moonscenty.alchemia.aura.node.NodeType;
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
import net.minecraft.world.phys.AABB;
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

    /**
     * About one silverwood in three has a pure node in its heart. Enough trees are grown, one after another on the
     * same spot, that missing every time would be a fault rather than bad luck.
     */
    @GameTest(template = TEMPLATE)
    public static void silverwoodSometimesHasANodeInItsHeart(GameTestHelper helper) {
        layGround(helper, 3);
        // a full run of the space is also the way to prove no node is placed anywhere else
        for (int attempt = 0; attempt < 20; attempt++) {
            clearAbove(helper, 3);
            place(helper, ModWorldgen.SILVERWOOD_TREE);
            List<AuraNode> nodes = helper.getLevel().getEntitiesOfClass(AuraNode.class,
                    new AABB(helper.absolutePos(TREE_BASE)).inflate(4, 16, 4));
            if (nodes.isEmpty()) {
                continue;
            }
            AuraNode node = nodes.getFirst();
            helper.assertTrue(node.type() == NodeType.PURE, "the node in a silverwood should be pure, was " + node.type());
            BlockPos foot = helper.absolutePos(TREE_BASE);
            helper.assertTrue(helper.getLevel().getBlockState(node.blockPosition()).is(ModBlocks.SILVERWOOD.log().get()),
                    "the node should sit inside the wood, but hangs at " + node.blockPosition() + " with the tree at " + foot);
            helper.assertTrue(node.blockPosition().getY() > foot.getY(),
                    "the node should hang partway up the trunk, not at the foot");
            helper.succeed();
            return;
        }
        helper.fail("twenty silverwoods grew and not one had a node in it");
    }

    private static void clearAbove(GameTestHelper helper, int radius) {
        for (int x = -radius - 3; x <= radius + 3; x++) {
            for (int z = -radius - 3; z <= radius + 3; z++) {
                for (int y = 0; y < 20; y++) {
                    helper.setBlock(TREE_BASE.offset(x, y, z), Blocks.AIR);
                }
            }
        }
        helper.getLevel().getEntitiesOfClass(AuraNode.class, new AABB(helper.absolutePos(TREE_BASE)).inflate(8, 20, 8))
                .forEach(AuraNode::discard);
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
