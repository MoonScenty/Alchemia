package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspects;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.block.CrucibleBlock;
import me.moonscenty.alchemia.block.entity.CrucibleBlockEntity;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * A crucible is worth having only if it takes things apart reliably and spills reliably, since the first is what it
 * is for and the second is what makes leaving one unattended cost something.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class CrucibleTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final BlockPos WHERE = new BlockPos(2, 2, 2);

    /** A pot set up over a fire, full of water and already hot enough to work. */
    private static CrucibleBlockEntity lit(GameTestHelper helper) {
        helper.setBlock(WHERE.below(), Blocks.LAVA);
        helper.setBlock(WHERE, ModBlocks.CRUCIBLE.get());
        CrucibleBlockEntity crucible =
                (CrucibleBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(WHERE));
        crucible.fill();
        BlockPos at = helper.absolutePos(WHERE);
        // it takes a while to come up to heat, which is the point of the heat, but not of this test
        for (int tick = 0; tick < 220; tick++) {
            CrucibleBlockEntity.tick(helper.getLevel(), at, helper.getLevel().getBlockState(at), crucible);
        }
        return crucible;
    }

    private static void drop(GameTestHelper helper, ItemStack stack) {
        var where = helper.absolutePos(WHERE).getCenter();
        ItemEntity item = new ItemEntity(helper.getLevel(), where.x, where.y, where.z, stack);
        item.setDeltaMovement(0, 0, 0);
        helper.getLevel().addFreshEntity(item);
    }

    private static void run(GameTestHelper helper, CrucibleBlockEntity crucible, int times) {
        BlockPos at = helper.absolutePos(WHERE);
        for (int tick = 0; tick < times; tick++) {
            CrucibleBlockEntity.tick(helper.getLevel(), at, helper.getLevel().getBlockState(at), crucible);
        }
    }

    /** A cold pot does nothing at all, however much is thrown into it. */
    @GameTest(template = TEMPLATE)
    public static void aColdPotDoesNothing(GameTestHelper helper) {
        helper.setBlock(WHERE, ModBlocks.CRUCIBLE.get());
        CrucibleBlockEntity crucible =
                (CrucibleBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(WHERE));
        crucible.fill();
        drop(helper, new ItemStack(Items.IRON_INGOT, 4));
        run(helper, crucible, 40);

        helper.assertTrue(crucible.dissolved().isEmpty(), "nothing came apart in cold water");
        helper.succeed();
    }

    /** Things thrown into a hot pot come apart into what they are made of, one at a time. */
    @GameTest(template = TEMPLATE)
    public static void itTakesThingsApart(GameTestHelper helper) {
        CrucibleBlockEntity crucible = lit(helper);
        helper.assertTrue(crucible.working(), "the pot came up to heat");

        drop(helper, new ItemStack(Items.IRON_INGOT, 2));
        run(helper, crucible, 1);
        int once = crucible.dissolved().total();
        helper.assertTrue(once > 0, "the first ingot came apart");
        helper.assertValueEqual(once, Aspects.of(new ItemStack(Items.IRON_INGOT)).total(),
                "one ingot at a time, not the whole stack");

        run(helper, crucible, 1);
        helper.assertValueEqual(crucible.dissolved().total(), once * 2, "and then the second");
        helper.succeed();
    }

    /**
     * A pot filled past what it holds runs over, and what runs over turns up in the air as flux. Losing it quietly
     * would make a crucible free to leave alone, which is the one thing it should not be.
     */
    @GameTest(template = TEMPLATE)
    public static void whatRunsOverBecomesFlux(GameTestHelper helper) {
        CrucibleBlockEntity crucible = lit(helper);
        BlockPos at = helper.absolutePos(WHERE);
        int before = AuraHandler.get(helper.getLevel(), at, ModAspects.FLUX);

        // enough iron to push it well past what the pot holds
        for (int ingots = 0; ingots < 40; ingots++) {
            drop(helper, new ItemStack(Items.IRON_INGOT));
            run(helper, crucible, 1);
        }

        helper.assertTrue(crucible.dissolved().total() <= CrucibleBlockEntity.CAPACITY,
                "the pot holds no more than it holds");
        helper.assertTrue(AuraHandler.get(helper.getLevel(), at, ModAspects.FLUX) > before,
                "what ran over went into the air");
        helper.succeed();
    }

    /** Breaking a full pot lets the lot go at once rather than losing it quietly. */
    @GameTest(template = TEMPLATE)
    public static void breakingItLetsTheLotGo(GameTestHelper helper) {
        CrucibleBlockEntity crucible = lit(helper);
        BlockPos at = helper.absolutePos(WHERE);
        drop(helper, new ItemStack(Items.IRON_INGOT, 3));
        run(helper, crucible, 3);

        int held = crucible.dissolved().total();
        helper.assertTrue(held > 0, "there was something in it to lose");
        int before = AuraHandler.get(helper.getLevel(), at, ModAspects.FLUX);

        helper.setBlock(WHERE, Blocks.AIR);
        helper.assertValueEqual(AuraHandler.get(helper.getLevel(), at, ModAspects.FLUX), before + held,
                "all of it went into the air");
        helper.succeed();
    }

    /** What is dissolved decides the colour of the water, which is how a pot is read across a room. */
    @GameTest(template = TEMPLATE)
    public static void theWaterTakesTheColourOfWhatIsInIt(GameTestHelper helper) {
        CrucibleBlockEntity crucible = lit(helper);
        int plain = crucible.colour();
        drop(helper, new ItemStack(ModItems.SHARDS.get(me.moonscenty.alchemia.block.CrystalType.FIRE).get()));
        run(helper, crucible, 1);

        helper.assertTrue(crucible.colour() != plain, "the water stopped looking like water");
        helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(WHERE))
                .getValue(CrucibleBlock.LEVEL) > 0, "and it is still shown as wet");
        helper.succeed();
    }
}
