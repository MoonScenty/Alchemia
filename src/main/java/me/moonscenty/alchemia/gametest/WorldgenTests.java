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
    public static void silverwoodSometimesHasANodeInItsHeart(GameTestHelper helper) {
        layGround(helper, 3);
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
    public static void bigMagicTreeGrowsTall(GameTestHelper helper) {
        layGround(helper, 3);
        place(helper, ModWorldgen.BIG_MAGIC_TREE);
        helper.assertBlockPresent(Blocks.OAK_LOG, TREE_BASE);
        int[] top = {0};
        helper.forEveryBlockInStructure(pos -> {
            if (helper.getBlockState(pos).is(Blocks.OAK_LEAVES)) {
                top[0] = Math.max(top[0], pos.getY() - TREE_BASE.getY());
            }
        });
        Alchemia.LOGGER.info("big magic tree: crown {} over its foot", top[0]);
        helper.assertTrue(top[0] >= 9, "the crown should stand at least nine over the foot, stood " + top[0]);
        helper.assertTrue(count(helper, Blocks.OAK_LEAVES) >= 60, "too few leaves");
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

    @GameTest(template = TEMPLATE)
    public static void tallSilverwoodGrowsInTheForest(GameTestHelper helper) {
        growAndCheck(helper, ModWorldgen.TALL_SILVERWOOD_TREE, ModBlocks.SILVERWOOD, 5 * 8 + 8, 100);
    }

    @GameTest(template = TEMPLATE)
    public static void oresComeOneBlockAtATime(GameTestHelper helper) {
        for (BlockPos pos : BlockPos.betweenClosed(TREE_BASE.offset(-2, 0, -2), TREE_BASE.offset(2, 4, 2))) {
            helper.setBlock(pos, Blocks.STONE);
        }
        ServerLevel level = helper.getLevel();
        level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE).getOrThrow(ModWorldgen.ORE_CINNABAR)
                .place(level, level.getChunkSource().getGenerator(), level.getRandom(), helper.absolutePos(TREE_BASE.above(2)));
        helper.assertValueEqual(count(helper, ModBlocks.CINNABAR_ORE.get()), 1, "cinnabar blocks");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void crystalsKeepOutOfWater(GameTestHelper helper) {
        BlockPos centre = TREE_BASE.above(3);
        for (BlockPos pos : BlockPos.betweenClosed(centre.offset(-2, -2, -2), centre.offset(2, 2, 2))) {
            helper.setBlock(pos, Blocks.STONE);
        }
        for (BlockPos pos : BlockPos.betweenClosed(centre.offset(-1, -1, -1), centre.offset(1, 1, 1))) {
            helper.setBlock(pos, Blocks.WATER);
        }
        ServerLevel level = helper.getLevel();
        net.minecraft.util.RandomSource random = net.minecraft.util.RandomSource.create(7L);
        for (int i = 0; i < 10; i++) {
            helper.assertValueEqual(me.moonscenty.alchemia.worldgen.CrystalClustersFeature.cluster(level,
                    helper.absolutePos(centre), me.moonscenty.alchemia.block.CrystalType.WATER, random), 0,
                    "crystals grown in water");
        }
        for (BlockPos pos : BlockPos.betweenClosed(centre.offset(-1, -1, -1), centre.offset(1, 1, 1))) {
            helper.setBlock(pos, Blocks.AIR);
        }
        int grown = me.moonscenty.alchemia.worldgen.CrystalClustersFeature.cluster(level, helper.absolutePos(centre),
                me.moonscenty.alchemia.block.CrystalType.WATER, random);
        helper.assertTrue(grown > 0, "but they should grow in a dry pocket");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aWildGreatwoodCanShelterSpiders(GameTestHelper helper) {
        layGround(helper, 3);
        ServerLevel level = helper.getLevel();
        var registry = level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
        var planted = (net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration)
                registry.getOrThrow(ModWorldgen.GREATWOOD_TREE).config();
        var wild = (net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration)
                registry.getOrThrow(ModWorldgen.WILD_GREATWOOD_TREE).config();
        helper.assertTrue(planted.decorators.isEmpty(), "a planted greatwood never holds a nest");
        helper.assertTrue(wild.decorators.stream().anyMatch(d -> d instanceof me.moonscenty.alchemia.worldgen.SpiderNestDecorator),
                "a wild greatwood may");

        var always = new net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration.TreeConfigurationBuilder(
                planted.trunkProvider, planted.trunkPlacer, planted.foliageProvider, planted.foliagePlacer, planted.minimumSize)
                .decorators(List.of(new me.moonscenty.alchemia.worldgen.SpiderNestDecorator(1.0F))).ignoreVines().build();
        net.minecraft.world.level.levelgen.feature.Feature.TREE.place(always, level, level.getChunkSource().getGenerator(),
                net.minecraft.util.RandomSource.create(3L), helper.absolutePos(TREE_BASE));

        helper.assertBlockPresent(Blocks.SPAWNER, TREE_BASE.below());
        var spawner = (net.minecraft.world.level.block.entity.SpawnerBlockEntity) level.getBlockEntity(helper.absolutePos(TREE_BASE.below()));
        var spawned = spawner.getSpawner().getOrCreateDisplayEntity(level, helper.absolutePos(TREE_BASE.below()));
        helper.assertTrue(spawned != null && spawned.getType() == net.minecraft.world.entity.EntityType.CAVE_SPIDER,
                "the spawner should breed cave spiders");
        helper.assertBlockPresent(Blocks.CHEST, TREE_BASE.below(2));
        var chest = (net.minecraft.world.level.block.entity.ChestBlockEntity) level.getBlockEntity(helper.absolutePos(TREE_BASE.below(2)));
        helper.assertValueEqual(chest.getLootTable(), net.minecraft.world.level.storage.loot.BuiltInLootTables.SIMPLE_DUNGEON,
                "the chest holds dungeon loot");
        helper.assertTrue(count(helper, Blocks.COBWEB) > 0, "webs hang among the wood");
        helper.succeed();
    }
}
