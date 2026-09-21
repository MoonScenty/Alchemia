package me.moonscenty.alchemia.gametest;

import java.util.EnumMap;
import java.util.Map;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aura.AuraGeneration;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.aura.ModAuraAttachment;
import me.moonscenty.alchemia.aura.node.AuraNode;
import me.moonscenty.alchemia.aura.node.NodeType;
import me.moonscenty.alchemia.registry.ModAspects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Nodes are the only thing that puts aura back, so what matters is that they are made sensibly and that feeding the
 * land actually reaches it.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class NodeTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final int SEEDS = 400;

    private static AuraNode node(GameTestHelper helper, RandomSource random) {
        AuraNode node = new AuraNode(helper.getLevel());
        BlockPos at = helper.absolutePos(new BlockPos(1, 2, 1));
        node.moveTo(at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5, 0.0F, 0.0F);
        node.drawUp(random);
        return node;
    }

    /** A fresh node is always something: a size, a kind and an aspect. */
    @GameTest(template = TEMPLATE)
    public static void everyNodeIsDrawnUpWhole(GameTestHelper helper) {
        RandomSource random = RandomSource.create(11L);
        for (int seed = 0; seed < SEEDS; seed++) {
            AuraNode node = node(helper, random);
            helper.assertTrue(node.getSize() > 0, "a node was drawn up with no size");
            helper.assertTrue(node.aspect() != null, "a node was drawn up made of nothing");
            helper.assertTrue(node.type() != null, "a node was drawn up as no kind of thing");
            node.discard();
        }
        helper.succeed();
    }

    /** Most nodes are plain, and every kind turns up eventually. */
    @GameTest(template = TEMPLATE)
    public static void oddNodesAreTheMinority(GameTestHelper helper) {
        RandomSource random = RandomSource.create(12L);
        Map<NodeType, Integer> seen = new EnumMap<>(NodeType.class);
        for (int seed = 0; seed < SEEDS * 5; seed++) {
            AuraNode node = node(helper, random);
            seen.merge(node.type(), 1, Integer::sum);
            node.discard();
        }

        int total = SEEDS * 5;
        int plain = seen.getOrDefault(NodeType.PLAIN, 0);
        helper.assertTrue(plain > total / 2, "plain nodes should be the common sort, saw " + plain + " of " + total);
        for (NodeType type : NodeType.values()) {
            helper.assertTrue(seen.getOrDefault(type, 0) > 0, type + " never turned up in " + total + " nodes");
        }
        helper.succeed();
    }

    /** A node made of something compound still feeds the land one of the six it is made from. */
    @GameTest(template = TEMPLATE)
    public static void whatANodeFeedsIsAlwaysPrimal(GameTestHelper helper) {
        RandomSource random = RandomSource.create(13L);
        for (int seed = 0; seed < SEEDS; seed++) {
            AuraNode node = node(helper, random);
            Holder<Aspect> giving = node.feedsWith(random);
            helper.assertTrue(giving != null, "a node should always have something to give");
            helper.assertTrue(giving.value().isPrimal(),
                    "a node fed the land " + giving.value().tag() + ", which is not one of the six");
            node.discard();
        }
        helper.succeed();
    }

    /** Every kind gives something rather than nothing, however dim or bright it is. */
    @GameTest(template = TEMPLATE)
    public static void everyKindIsWorthSomething(GameTestHelper helper) {
        RandomSource random = RandomSource.create(14L);
        AuraNode node = node(helper, random);
        for (NodeType type : NodeType.values()) {
            node.setType(type);
            int strength = type.strengthOf(node, helper.getLevel());
            helper.assertTrue(strength >= 1, type + " worked out to " + strength + ", which is nothing at all");
        }
        node.discard();
        helper.succeed();
    }

    /** Only a hungry node takes rather than gives. */
    @GameTest(template = TEMPLATE)
    public static void onlyTheHungryOneTakes(GameTestHelper helper) {
        for (NodeType type : NodeType.values()) {
            helper.assertTrue(type.feeds() == (type != NodeType.HUNGRY),
                    type + " should " + (type == NodeType.HUNGRY ? "not " : "") + "feed the land");
        }
        helper.succeed();
    }

    /** Putting aura back reaches the chunk, and a chunk already full does not simply keep swelling. */
    @GameTest(template = TEMPLATE)
    public static void feedingTheLandReachesIt(GameTestHelper helper) {
        BlockPos here = helper.absolutePos(BlockPos.ZERO);
        LevelChunk chunk = helper.getLevel().getChunkAt(here);
        AuraGeneration.ensure(helper.getLevel(), chunk, RandomSource.create(15L));

        AuraHandler.drainAvailable(helper.getLevel(), here, ModAspects.AIR, 10_000);
        helper.assertTrue(AuraHandler.get(helper.getLevel(), here, ModAspects.AIR) == 0, "the chunk should be empty");

        RandomSource random = RandomSource.create(16L);
        for (int round = 0; round < 50; round++) {
            AuraHandler.recharge(helper.getLevel(), here, ModAspects.AIR, 1, random);
        }
        int after = AuraHandler.get(helper.getLevel(), here, ModAspects.AIR);
        helper.assertTrue(after > 0, "feeding an empty chunk should have put something back");

        int base = chunk.getData(ModAuraAttachment.AURA).base();
        for (int round = 0; round < 2000; round++) {
            AuraHandler.recharge(helper.getLevel(), here, ModAspects.AIR, 1, random);
        }
        int full = AuraHandler.get(helper.getLevel(), here, ModAspects.AIR);
        helper.assertTrue(full > base, "a chunk should be able to run a little over what it holds");
        helper.assertTrue(full < base * 2,
                "a chunk fed forever reached " + full + " against a base of " + base + ", so nothing is holding it back");
        helper.succeed();
    }

    /** Land with no aura at all must not swallow what a node gives without trace. */
    @GameTest(template = TEMPLATE)
    public static void feedingNowhereDoesNothing(GameTestHelper helper) {
        BlockPos away = new BlockPos(30_000_000, 64, 30_000_000);
        AuraHandler.recharge(helper.getLevel(), away, ModAspects.AIR, 10, RandomSource.create(17L));
        helper.assertTrue(AuraHandler.get(helper.getLevel(), away, ModAspects.AIR) == 0,
                "unloaded land should still read as empty");
        helper.succeed();
    }
}
