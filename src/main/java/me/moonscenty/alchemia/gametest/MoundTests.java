package me.moonscenty.alchemia.gametest;

import java.util.List;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aura.node.AuraNode;
import me.moonscenty.alchemia.aura.node.NodeType;
import me.moonscenty.alchemia.block.LootBlock;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.worldgen.MoundFeature;
import me.moonscenty.alchemia.worldgen.MoundLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The mound, read out of the original's jar, and the urns and crates it keeps. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class MoundTests {
    private static final String TEMPLATE = "empty_32x40x32";

    /** The layout comes out of the jar whole: some two and a half thousand blocks, the chest and two spawners. */
    @GameTest(template = TEMPLATE)
    public static void theMoundIsReadFromTheJar(GameTestHelper helper) {
        MoundLayout layout = MoundLayout.get().orElse(null);
        helper.assertTrue(layout != null, "the jar in mods/ has a mound");
        helper.assertTrue(layout.blocks().size() > 2000, "it has its blocks: " + layout.blocks().size());
        helper.assertTrue(layout.chest().equals(new BlockPos(10, 1, 9)), "the chest is in the chamber");
        helper.assertTrue(layout.spawners().equals(List.of(new BlockPos(4, 5, 4), new BlockPos(4, 5, 14))),
                "and two spawners keep it");
        long kinds = layout.blocks().stream().map(placement -> placement.state().getBlock()).distinct().count();
        helper.assertTrue(kinds >= 8, "of grass, earth, cobble, moss, stairs and the rest: " + kinds);
        helper.succeed();
    }

    /** Built, it has its chest (or trapped chest), urns or crates by it, spawners and a sinister node. */
    @GameTest(template = TEMPLATE)
    public static void aMoundIsBuilt(GameTestHelper helper) {
        MoundLayout layout = MoundLayout.get().orElseThrow();
        BlockPos corner = helper.absolutePos(new BlockPos(4, 3, 4));
        helper.assertTrue(MoundFeature.build(helper.getLevel(), corner, helper.getLevel().getRandom()),
                "it builds");

        BlockState chest = helper.getLevel().getBlockState(corner.offset(layout.chest()));
        helper.assertTrue(chest.is(Blocks.CHEST) || chest.is(Blocks.TRAPPED_CHEST), "a chest: " + chest);
        for (int side : new int[] {-2, 2}) {
            BlockState urn = helper.getLevel().getBlockState(corner.offset(layout.chest()).offset(-1, 0, side));
            helper.assertTrue(urn.is(ModBlocks.LOOT_URN.get()) || urn.is(ModBlocks.LOOT_CRATE.get()),
                    "an urn or crate by it: " + urn);
        }
        List<EntityType<?>> keepers = List.of(EntityType.SKELETON, EntityType.ZOMBIE);
        for (int i = 0; i < 2; i++) {
            BlockPos at = corner.offset(layout.spawners().get(i));
            helper.assertTrue(helper.getLevel().getBlockEntity(at) instanceof SpawnerBlockEntity,
                    "a spawner at " + at);
        }
        List<AuraNode> nodes = helper.getLevel().getEntitiesOfClass(AuraNode.class,
                new AABB(corner.offset(9, 8, 9)).inflate(1.0));
        helper.assertTrue(nodes.size() == 1 && nodes.get(0).type() == NodeType.DARK, "a sinister node above");
        nodes.forEach(AuraNode::discard);
        helper.succeed();
    }

    /** An urn broken gives up one to three things, and more the rarer it is. */
    @GameTest(template = TEMPLATE)
    public static void aRareUrnBreaksIntoLoot(GameTestHelper helper) {
        BlockPos at = new BlockPos(4, 2, 4);
        helper.setBlock(at, ModBlocks.LOOT_URN.get().defaultBlockState().setValue(LootBlock.RARITY, 2));
        helper.getLevel().destroyBlock(helper.absolutePos(at), true);
        int dropped = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                new AABB(helper.absolutePos(at)).inflate(2.0)).size();
        helper.assertTrue(dropped >= 3 && dropped <= 5, "a rare urn gives three to five things: " + dropped);
        helper.assertBlockNotPresent(ModBlocks.LOOT_URN.get(), at);
        helper.succeed();
    }
}
