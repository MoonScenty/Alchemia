package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.block.CrystallizerBlock;
import me.moonscenty.alchemia.block.entity.CentrifugeBlockEntity;
import me.moonscenty.alchemia.block.entity.CrystallizerBlockEntity;
import me.moonscenty.alchemia.item.CrystallizedEssenceItem;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The centrifuge and the crystallizer: the two things a run of pipe can end in that are neither a jar nor a lamp.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class EssentiaDeviceTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final BlockPos WHERE = new BlockPos(2, 2, 2);

    /** A compound goes in, and one of the two things it was made of comes out. */
    @GameTest(template = TEMPLATE)
    public static void aCentrifugeSplitsACompoundIntoOneOfItsParts(GameTestHelper helper) {
        helper.setBlock(WHERE, ModBlocks.CENTRIFUGE.get());
        BlockPos at = helper.absolutePos(WHERE);
        CentrifugeBlockEntity centrifuge = (CentrifugeBlockEntity) helper.getLevel().getBlockEntity(at);
        helper.assertTrue(centrifuge.accept(ModAspects.CRAFT), "it takes a compound");
        helper.assertFalse(centrifuge.wants(ModAspects.FIRE), "and nothing else while that one spins");

        for (int tick = 0; tick < 40; tick++) {
            CentrifugeBlockEntity.serverTick(helper.getLevel(), at, helper.getLevel().getBlockState(at), centrifuge);
        }
        helper.assertValueEqual(centrifuge.held().total(), 1, "one point comes out");
        var out = centrifuge.held().sortedByName().get(0).value();
        helper.assertTrue(out == ModAspects.EXCHANGE.value() || out == ModAspects.TOOL.value(),
                "and it is one of the two crafting is made of");
        helper.succeed();
    }

    /** A primal has nothing to come apart into, and is not taken. */
    @GameTest(template = TEMPLATE)
    public static void aCentrifugeRefusesThePrimals(GameTestHelper helper) {
        helper.setBlock(WHERE, ModBlocks.CENTRIFUGE.get());
        CentrifugeBlockEntity centrifuge =
                (CentrifugeBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(WHERE));
        helper.assertFalse(centrifuge.wants(ModAspects.FIRE), "fire is not split");
        helper.succeed();
    }

    /** A point of essentia comes out as a crystal, into the chest at its far end, on earth drawn from the air. */
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void aCrystallizerSetsEssentiaIntoTheChestBeyondIt(GameTestHelper helper) {
        helper.setBlock(WHERE, ModBlocks.CRYSTALLIZER.get().defaultBlockState()
                .setValue(CrystallizerBlock.FACING, Direction.DOWN));
        helper.setBlock(WHERE.above(), Blocks.CHEST);
        BlockPos at = helper.absolutePos(WHERE);
        AuraHandler.add(helper.getLevel(), at, ModAspects.EARTH, 20);
        CrystallizerBlockEntity crystallizer = (CrystallizerBlockEntity) helper.getLevel().getBlockEntity(at);
        helper.assertTrue(crystallizer.accept(ModAspects.FIRE), "it takes any aspect, the primals too");

        for (int tick = 0; tick < 60; tick++) {
            CrystallizerBlockEntity.serverTick(helper.getLevel(), at, helper.getLevel().getBlockState(at),
                    crystallizer);
        }
        ChestBlockEntity chest = (ChestBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(WHERE.above()));
        var crystal = chest.getItem(0);
        helper.assertTrue(crystal.getItem() instanceof CrystallizedEssenceItem, "a crystal is in the chest");
        helper.assertTrue(CrystallizedEssenceItem.inside(crystal).map(a -> a.value() == ModAspects.FIRE.value())
                .orElse(false), "and it is fire");
        helper.assertTrue(crystallizer.held().isEmpty(), "and the crystallizer is ready for another");
        helper.succeed();
    }




}
