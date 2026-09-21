package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aura.node.AuraNode;
import me.moonscenty.alchemia.block.entity.NodeStabilizerBlockEntity;
import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * A stabiliser is only worth anything if it reliably takes hold, since that is what stops a node wandering out of a
 * build, and if it reliably lets go, since that is the only way to get one back.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class StabilizerTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final BlockPos WHERE = new BlockPos(2, 1, 2);

    private static NodeStabilizerBlockEntity place(GameTestHelper helper) {
        helper.setBlock(WHERE, ModBlocks.NODE_STABILIZER.get());
        return (NodeStabilizerBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(WHERE));
    }

    /** A node put in the cradle is spawned where the stabiliser expects it. */
    private static AuraNode nodeAt(GameTestHelper helper, NodeStabilizerBlockEntity stabilizer, Vec3 offset) {
        AuraNode node = new AuraNode(helper.getLevel());
        Vec3 at = stabilizer.anchor().add(offset);
        node.moveTo(at.x, at.y, at.z, 0.0F, 0.0F);
        node.drawUp(RandomSource.create(1L));
        helper.getLevel().addFreshEntity(node);
        return node;
    }

    private static void run(NodeStabilizerBlockEntity stabilizer, GameTestHelper helper, int times) {
        for (int tick = 0; tick < times; tick++) {
            NodeStabilizerBlockEntity.tick(helper.getLevel(), stabilizer.getBlockPos(),
                    stabilizer.getBlockState(), stabilizer);
        }
    }

    @GameTest(template = TEMPLATE)
    public static void itTakesHoldOfWhatDriftsIn(GameTestHelper helper) {
        NodeStabilizerBlockEntity stabilizer = place(helper);
        AuraNode node = nodeAt(helper, stabilizer, new Vec3(0.3, 0.0, 0.0));

        helper.assertTrue(!node.isHeld(), "a node should not be held before anything has reached for it");
        run(stabilizer, helper, 12);
        helper.assertTrue(node.isHeld(), "a node in the cradle should have been taken hold of");
        helper.succeed();
    }

    /** The arms only come out while it has something, which is how the thing reads at a glance. */
    @GameTest(template = TEMPLATE)
    public static void theArmsStayInUntilThereIsSomethingToHold(GameTestHelper helper) {
        NodeStabilizerBlockEntity stabilizer = place(helper);
        run(stabilizer, helper, 40);
        helper.assertTrue(stabilizer.reachOut() == 0.0F,
                "with nothing to hold the arms should be in, were at " + stabilizer.reachOut());

        nodeAt(helper, stabilizer, Vec3.ZERO);
        run(stabilizer, helper, NodeStabilizerBlockEntity.STROKE + 10);
        helper.assertTrue(stabilizer.reachOut() == 1.0F,
                "holding something the arms should be all the way out, were at " + stabilizer.reachOut());
        helper.succeed();
    }

    /** Redstone is how a node is let go without breaking anything. */
    @GameTest(template = TEMPLATE)
    public static void redstoneLetsGo(GameTestHelper helper) {
        NodeStabilizerBlockEntity stabilizer = place(helper);
        AuraNode node = nodeAt(helper, stabilizer, Vec3.ZERO);
        run(stabilizer, helper, NodeStabilizerBlockEntity.STROKE + 10);
        helper.assertTrue(node.isHeld(), "it should be holding to begin with");

        helper.setBlock(WHERE.east(), Blocks.REDSTONE_BLOCK);
        run(stabilizer, helper, 10);
        helper.assertTrue(!node.isHeld(), "powered, it should let go");

        run(stabilizer, helper, NodeStabilizerBlockEntity.STROKE + 10);
        helper.assertTrue(stabilizer.reachOut() == 0.0F, "and the arms should come back in");

        helper.setBlock(WHERE.east(), Blocks.AIR);
        run(stabilizer, helper, 12);
        helper.assertTrue(node.isHeld(), "unpowered, it should take hold again");
        helper.succeed();
    }

    /** Only one node fits a cradle, so any others are worked away from it. */
    @GameTest(template = TEMPLATE)
    public static void onlyOneIsHeldAndTheRestAreShovedOff(GameTestHelper helper) {
        NodeStabilizerBlockEntity stabilizer = place(helper);
        AuraNode first = nodeAt(helper, stabilizer, new Vec3(0.1, 0.0, 0.0));
        AuraNode second = nodeAt(helper, stabilizer, new Vec3(1.2, 0.0, 0.0));

        run(stabilizer, helper, 12);
        helper.assertTrue(first.isHeld(), "the nearer one should be the one held");
        helper.assertTrue(!second.isHeld(), "the further one should not be");
        helper.assertTrue(second.getDeltaMovement().x > 0,
                "the one not held should have been pushed away from the cradle");
        helper.succeed();
    }

    /** Nothing anywhere near it means nothing to do. */
    @GameTest(template = TEMPLATE)
    public static void aNodeFarOffIsLeftAlone(GameTestHelper helper) {
        NodeStabilizerBlockEntity stabilizer = place(helper);
        AuraNode away = nodeAt(helper, stabilizer, new Vec3(8.0, 0.0, 0.0));

        run(stabilizer, helper, 40);
        helper.assertTrue(!away.isHeld(), "a node well out of reach should be left alone");
        helper.assertTrue(stabilizer.reachOut() == 0.0F, "and the arms should have stayed in");
        helper.succeed();
    }
}
