package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.block.AlembicBlock;
import me.moonscenty.alchemia.block.SmelterAuxBlock;
import me.moonscenty.alchemia.block.entity.AlembicBlockEntity;
import me.moonscenty.alchemia.block.entity.EssentiaSmelterBlockEntity;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
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
    /** Long enough that pours every forty ticks and every thirty-two come out more than one apart. */
    private static final int POURED = 600;

    private static EssentiaSmelterBlockEntity smelter(GameTestHelper helper, ItemStack input) {
        return smelter(helper, input, new ItemStack(Items.COAL, 8));
    }

    private static EssentiaSmelterBlockEntity smelter(GameTestHelper helper, ItemStack input, ItemStack fuel) {
        helper.setBlock(WHERE, ModBlocks.ESSENTIA_SMELTER.get());
        EssentiaSmelterBlockEntity smelter =
                (EssentiaSmelterBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(WHERE));
        smelter.setItem(EssentiaSmelterBlockEntity.SLOT_INPUT, input);
        smelter.setItem(EssentiaSmelterBlockEntity.SLOT_FUEL, fuel);
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

    /**
     * Alumentum burns for four coals and hurries the pouring by a fifth.
     * <p>
     * The original hurried what rises, not what boils: the same item takes as long to come apart either way, but
     * what has come apart goes up into the vessels every four fifths of the usual wait.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void alumentumBurnsLongAndHurriesThePour(GameTestHelper helper) {
        helper.assertValueEqual(new ItemStack(ModItems.ALUMENTUM.get()).getBurnTime(RecipeType.SMELTING),
                new ItemStack(Items.COAL).getBurnTime(RecipeType.SMELTING) * 4,
                "alumentum is worth four coals in anything that takes fuel");

        // the same fire, the same iron, the same number of ticks: more has risen out of the hurried one
        EssentiaSmelterBlockEntity coal = smelter(helper, new ItemStack(Items.IRON_INGOT, 32));
        AlembicBlockEntity caughtFromCoal = alembic(helper, 1);
        run(helper, coal, POURED);

        BlockPos aside = WHERE.east(4);
        helper.setBlock(aside, ModBlocks.ESSENTIA_SMELTER.get());
        helper.setBlock(aside.above(), ModBlocks.ALEMBIC.get());
        AlembicBlockEntity caughtFromFired =
                (AlembicBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(aside.above()));
        EssentiaSmelterBlockEntity fired =
                (EssentiaSmelterBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(aside));
        fired.setItem(EssentiaSmelterBlockEntity.SLOT_INPUT, new ItemStack(Items.IRON_INGOT, 32));
        fired.setItem(EssentiaSmelterBlockEntity.SLOT_FUEL, new ItemStack(ModItems.ALUMENTUM.get(), 4));
        BlockPos at = helper.absolutePos(aside);
        for (int tick = 0; tick < POURED; tick++) {
            EssentiaSmelterBlockEntity.tick(helper.getLevel(), at, helper.getLevel().getBlockState(at), fired);
        }

        helper.assertTrue(caughtFromCoal.amount() > 0, "the one on coal poured something");
        helper.assertTrue(caughtFromFired.amount() > caughtFromCoal.amount(),
                "and the one on alumentum poured more in the same time: "
                        + caughtFromFired.amount() + " against " + caughtFromCoal.amount());
        helper.succeed();
    }

    /** A pump against its side pours a second column off the same fire. */
    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void aPumpPoursASecondColumn(GameTestHelper helper) {
        EssentiaSmelterBlockEntity smelter = smelter(helper, new ItemStack(Items.IRON_INGOT, 32));
        AlembicBlockEntity overSmelter = alembic(helper, 1);
        BlockPos pump = WHERE.east();
        helper.setBlock(pump, ModBlocks.SMELTER_AUX.get().defaultBlockState()
                .setValue(SmelterAuxBlock.FACING, Direction.WEST));
        helper.setBlock(pump.above(), ModBlocks.ALEMBIC.get());
        AlembicBlockEntity overPump =
                (AlembicBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pump.above()));

        run(helper, smelter, POURED);
        helper.assertTrue(overSmelter.amount() > 0, "the column over the smelter filled");
        helper.assertTrue(overPump.amount() > 0, "and so did the one over the pump");
        helper.succeed();
    }

    /** What a smelter fails to keep goes into the air as flux. */
    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void whatIsLostGoesUpAsFlux(GameTestHelper helper) {
        BlockPos at = helper.absolutePos(WHERE);
        int before = AuraHandler.get(helper.getLevel(), at, ModAspects.FLUX);
        EssentiaSmelterBlockEntity smelter = smelter(helper, new ItemStack(Items.IRON_INGOT, 64));
        run(helper, smelter, POURED);
        helper.assertTrue(AuraHandler.get(helper.getLevel(), at, ModAspects.FLUX) > before,
                "a plain smelter loses a fifth of what it boils, and that fifth is flux");
        helper.succeed();
    }
}
