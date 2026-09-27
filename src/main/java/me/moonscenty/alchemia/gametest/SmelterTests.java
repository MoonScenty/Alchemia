package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.AlembicBlock;
import me.moonscenty.alchemia.block.entity.AlembicBlockEntity;
import me.moonscenty.alchemia.block.entity.EssentiaSmelterBlockEntity;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * A smelter is only worth building if what it boils off actually reaches the vessels above it, and if those vessels
 * sort what they catch. Both are what make a stack of them a thing worth arranging rather than a decoration.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class SmelterTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final BlockPos WHERE = new BlockPos(2, 1, 2);

    private static EssentiaSmelterBlockEntity smelter(GameTestHelper helper, ItemStack input) {
        helper.setBlock(WHERE, ModBlocks.ESSENTIA_SMELTER.get());
        EssentiaSmelterBlockEntity smelter =
                (EssentiaSmelterBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(WHERE));
        smelter.setItem(EssentiaSmelterBlockEntity.SLOT_INPUT, input);
        smelter.setItem(EssentiaSmelterBlockEntity.SLOT_FUEL, new ItemStack(Items.COAL, 8));
        return smelter;
    }

    private static AlembicBlockEntity alembic(GameTestHelper helper, int up) {
        BlockPos at = WHERE.above(up);
        helper.setBlock(at, ModBlocks.ALEMBIC.get());
        return (AlembicBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(at));
    }

    private static void run(GameTestHelper helper, EssentiaSmelterBlockEntity smelter, int times) {
        BlockPos at = helper.absolutePos(WHERE);
        for (int tick = 0; tick < times; tick++) {
            EssentiaSmelterBlockEntity.tick(helper.getLevel(), at, helper.getLevel().getBlockState(at), smelter);
        }
    }

    /** What it burns comes apart into what it is made of, and the fire shows while it is at it. */
    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void itBoilsThingsDown(GameTestHelper helper) {
        EssentiaSmelterBlockEntity smelter = smelter(helper, new ItemStack(Items.IRON_INGOT, 2));
        run(helper, smelter, 2);
        helper.assertTrue(smelter.lit(), "it lit itself off the coal");

        run(helper, smelter, 300);
        helper.assertTrue(smelter.held().get(ModAspects.METAL) > 0, "there is metal in it now");
        helper.succeed();
    }

    /** A vessel over the smelter catches what rises, and a smelter with nothing over it simply fills up. */
    @GameTest(template = TEMPLATE, timeoutTicks = 600)
    public static void whatRisesIsCaught(GameTestHelper helper) {
        EssentiaSmelterBlockEntity smelter = smelter(helper, new ItemStack(Items.IRON_INGOT, 8));
        AlembicBlockEntity alembic = alembic(helper, 1);

        run(helper, smelter, 500);
        helper.assertTrue(!alembic.isEmpty(), "the vessel caught something");
        helper.assertTrue(alembic.holding().orElseThrow().value() == ModAspects.METAL.value(),
                "and what it caught is what iron is made of");
        helper.succeed();
    }

    /** A vessel takes one kind and no other, which is the whole of how essentia is sorted. */
    @GameTest(template = TEMPLATE)
    public static void aVesselHoldsOneKind(GameTestHelper helper) {
        AlembicBlockEntity alembic = alembic(helper, 1);
        helper.assertTrue(alembic.accept(ModAspects.METAL), "an empty vessel takes what it is offered");
        helper.assertFalse(alembic.accept(ModAspects.FIRE), "and then takes nothing else");
        helper.assertTrue(alembic.accept(ModAspects.METAL), "but more of the same is welcome");
        helper.assertValueEqual(alembic.amount(), 2, "two of them");
        helper.succeed();
    }

    /** Stacked vessels lose their legs, since they are standing on the one below rather than on the ground. */
    @GameTest(template = TEMPLATE)
    public static void stackedVesselsStandOnEachOther(GameTestHelper helper) {
        alembic(helper, 1);
        alembic(helper, 2);
        helper.assertTrue(helper.getBlockState(WHERE.above(1)).getValue(AlembicBlock.LEGS),
                "the lower one stands on its own legs");
        helper.assertFalse(helper.getBlockState(WHERE.above(2)).getValue(AlembicBlock.LEGS),
                "the upper one stands on the lower");
        helper.succeed();
    }
}
