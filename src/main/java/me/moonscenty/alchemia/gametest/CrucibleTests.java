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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import me.moonscenty.alchemia.player.ModAttachments;
import me.moonscenty.alchemia.player.PlayerKnowledge;
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

    /** Things thrown into a hot pot come apart into what they are made of, the whole stack at once. */
    @GameTest(template = TEMPLATE)
    public static void itTakesThingsApart(GameTestHelper helper) {
        CrucibleBlockEntity crucible = lit(helper);
        helper.assertTrue(crucible.working(), "the pot came up to heat");

        drop(helper, new ItemStack(Items.IRON_INGOT, 3));
        run(helper, crucible, 1);
        helper.assertValueEqual(crucible.dissolved().total(), 3 * Aspects.of(new ItemStack(Items.IRON_INGOT)).total(),
                "all three ingots came apart together, as the original took a stack");
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

        // enough iron to push it well past what the pot holds, all at once
        drop(helper, new ItemStack(Items.IRON_INGOT, 40));
        run(helper, crucible, 1);
        int over = crucible.dissolved().total();
        helper.assertTrue(over > CrucibleBlockEntity.CAPACITY + 1, "it should be over the brim, at " + over);

        run(helper, crucible, 1);
        helper.assertValueEqual(crucible.dissolved().total(), over - 1, "running over loses a point a tick");
        run(helper, crucible, over);
        helper.assertTrue(crucible.dissolved().total() <= CrucibleBlockEntity.CAPACITY,
                "until the pot holds no more than it holds");
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

    /**
     * A recipe wants only a little water left, and drinks what there is. What it makes is thrown up out of the pot
     * and is not taken back in when it falls. Things can be put in by hand as well as thrown.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void aLittleWaterStillWorksAndTheWorkIsLeftBe(GameTestHelper helper) {
        helper.setBlock(WHERE.below(), Blocks.LAVA);
        helper.setBlock(WHERE, ModBlocks.CRUCIBLE.get());
        CrucibleBlockEntity crucible =
                (CrucibleBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(WHERE));
        crucible.pour(10, true);
        run(helper, crucible, 220);
        helper.assertTrue(crucible.working(), "ten water is enough to come up to heat");

        Player maker = helper.makeMockPlayer(GameType.SURVIVAL);
        maker.setData(ModAttachments.KNOWLEDGE, PlayerKnowledge.of(maker).withResearch(Alchemia.id("tallow")));
        helper.assertTrue(crucible.putByHand(new ItemStack(Items.COAL), maker), "coal goes in by hand");
        helper.assertTrue(crucible.putByHand(new ItemStack(Items.ROTTEN_FLESH), maker), "and so does the flesh");
        helper.assertValueEqual(crucible.water(), 0, "the work drank what water there was");

        helper.runAfterDelay(60, () -> {
            var made = helper.getLevel().getEntitiesOfClass(ItemEntity.class, helper.getBounds().inflate(2.0),
                    item -> item.getItem().is(ModItems.TALLOW.get()));
            helper.assertFalse(made.isEmpty(), "the tallow should still be about, not melted back down");
            helper.succeed();
        });
    }

    /** Something alive standing in a hot pot is scalded one touch in ten. */
    @GameTest(template = TEMPLATE)
    public static void aHotPotScalds(GameTestHelper helper) {
        CrucibleBlockEntity crucible = lit(helper);
        var zombie = helper.spawn(net.minecraft.world.entity.EntityType.ZOMBIE, WHERE.above(3));
        float whole = zombie.getHealth();
        for (int touch = 0; touch < 9; touch++) {
            crucible.touched(zombie);
        }
        helper.assertValueEqual(zombie.getHealth(), whole, "nine touches are nothing");
        crucible.touched(zombie);
        helper.assertTrue(zombie.getHealth() < whole, "the tenth scalds");
        helper.succeed();
    }

    /** What is dissolved darkens the water, which is how a pot is read across a room. */
    @GameTest(template = TEMPLATE)
    public static void theWaterDarkensWithWhatIsInIt(GameTestHelper helper) {
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
