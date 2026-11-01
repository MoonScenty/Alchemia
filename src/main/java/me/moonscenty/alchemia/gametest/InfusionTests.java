package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.block.entity.ArcanePedestalBlockEntity;
import me.moonscenty.alchemia.block.entity.InfusionMatrixBlockEntity;
import me.moonscenty.alchemia.block.entity.JarBlockEntity;
import me.moonscenty.alchemia.player.ModAttachments;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModWandParts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.registries.DeferredBlock;

@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class InfusionTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final BlockPos MATRIX = new BlockPos(8, 6, 8);

    private static ArcanePedestalBlockEntity stand(GameTestHelper helper, BlockPos at, ItemStack holding) {
        helper.setBlock(at, ModBlocks.ARCANE_PEDESTAL.get());
        ArcanePedestalBlockEntity stand =
                (ArcanePedestalBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(at));
        stand.hold(holding);
        return stand;
    }

    private static void jar(GameTestHelper helper, BlockPos at, Holder<me.moonscenty.alchemia.aspect.Aspect> aspect,
            int amount) {
        helper.setBlock(at, ModBlocks.JAR.get());
        JarBlockEntity jar = (JarBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(at));
        for (int one = 0; one < amount; one++) {
            jar.accept(aspect);
        }
    }

    private static Player scholar(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setData(ModAttachments.KNOWLEDGE,
                PlayerKnowledge.of(player).withResearch(Alchemia.id("wand_rods")));
        return player;
    }

    private static void altar(GameTestHelper helper) {
        helper.setBlock(MATRIX, ModBlocks.INFUSION_MATRIX.get());
        for (int east = -1; east <= 1; east += 2) {
            for (int south = -1; south <= 1; south += 2) {
                helper.setBlock(MATRIX.offset(east, -2, south), ModBlocks.ARCANE_STONE.block().get());
                helper.setBlock(MATRIX.offset(east, -1, south), ModBlocks.ARCANE_STONE.block().get());
            }
        }
    }

    private static void under(GameTestHelper helper, DeferredBlock<net.minecraft.world.level.block.Block> stone) {
        for (int east = -1; east <= 1; east += 2) {
            for (int south = -1; south <= 1; south += 2) {
                helper.setBlock(MATRIX.offset(east, -3, south), stone.get());
            }
        }
    }

    private static InfusionMatrixBlockEntity laidOut(GameTestHelper helper) {
        altar(helper);
        stand(helper, MATRIX.below(2), new ItemStack(Items.OBSIDIAN));
        stand(helper, MATRIX.offset(2, -2, 0), new ItemStack(ModItems.BALANCED_SHARD.get()));
        stand(helper, MATRIX.offset(-2, -2, 0),
                new ItemStack(ModItems.SHARDS.get(me.moonscenty.alchemia.block.CrystalType.EARTH).get()));

        jar(helper, MATRIX.offset(0, -2, 3), ModAspects.EARTH, 16);
        jar(helper, MATRIX.offset(0, -2, -3), ModAspects.ENERGY, 16);
        jar(helper, MATRIX.offset(3, -2, 3), ModAspects.DARKNESS, 16);
        return (InfusionMatrixBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(MATRIX));
    }

    private static void run(GameTestHelper helper, InfusionMatrixBlockEntity matrix, int turns) {
        BlockPos at = helper.absolutePos(MATRIX);
        for (int tick = 0; tick < turns * InfusionMatrixBlockEntity.CYCLE + turns; tick++) {
            InfusionMatrixBlockEntity.tick(helper.getLevel(), at, helper.getLevel().getBlockState(at), matrix);
        }
    }

    @GameTest(template = TEMPLATE)
    public static void theFirstTouchWakesTheAltar(GameTestHelper helper) {
        InfusionMatrixBlockEntity matrix = laidOut(helper);
        Player player = scholar(helper);

        helper.assertTrue(!matrix.awake(), "a matrix hung over loose stone is asleep");
        helper.assertValueEqual(matrix.wake(player), InfusionMatrixBlockEntity.Woken.WOKEN, "the wand woke it");
        helper.assertTrue(matrix.awake(), "and it is awake");
        helper.assertBlockPresent(ModBlocks.ARCANE_PILLAR.get(), MATRIX.offset(1, -2, 1));
        helper.assertBlockPresent(ModBlocks.ARCANE_PILLAR.get(), MATRIX.offset(1, -1, 1));

        helper.assertValueEqual(matrix.wake(player), InfusionMatrixBlockEntity.Woken.STARTED,
                "and the touch after that starts the working");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void breakingOneCornerPutsItToSleep(GameTestHelper helper) {
        InfusionMatrixBlockEntity matrix = laidOut(helper);
        matrix.wake(scholar(helper));
        helper.setBlock(MATRIX.offset(1, -2, 1), Blocks.AIR);

        run(helper, matrix, 2);
        helper.assertTrue(!matrix.awake(), "the altar went back to sleep");
        helper.assertBlockPresent(ModBlocks.ARCANE_STONE.block().get(), MATRIX.offset(-1, -2, -1));
        helper.assertBlockPresent(ModBlocks.ARCANE_STONE.block().get(), MATRIX.offset(-1, -1, -1));
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void itWillNotWakeWithoutAnAltar(GameTestHelper helper) {
        InfusionMatrixBlockEntity matrix = laidOut(helper);
        helper.setBlock(MATRIX.offset(1, -2, 1), Blocks.AIR);

        helper.assertValueEqual(matrix.wake(scholar(helper)), InfusionMatrixBlockEntity.Woken.UNBUILT,
                "three corners is not an altar");
        helper.assertTrue(!matrix.awake(), "so it stayed asleep");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void breakingTheAltarEndsTheWorking(GameTestHelper helper) {
        InfusionMatrixBlockEntity matrix = laidOut(helper);
        Player player = scholar(helper);
        matrix.wake(player);
        matrix.wake(player);
        helper.assertTrue(matrix.busy(), "the working started");

        helper.setBlock(MATRIX.offset(-1, -2, 1), Blocks.AIR);
        run(helper, matrix, 2);
        helper.assertTrue(!matrix.busy(), "and came apart with the altar");
        helper.assertTrue(!matrix.awake(), "which also put the stones back to sleep");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aLopsidedRingIsWhatGoesWrong(GameTestHelper helper) {
        InfusionMatrixBlockEntity matrix = laidOut(helper);
        helper.assertValueEqual(matrix.symmetry(helper.getLevel()), 0, "two pedestals facing each other are even");

        stand(helper, MATRIX.offset(0, -2, 3), new ItemStack(Items.IRON_INGOT));
        helper.assertTrue(matrix.symmetry(helper.getLevel()) > 0, "a third with nothing opposite it is not");

        stand(helper, MATRIX.offset(0, -2, -3), new ItemStack(Items.IRON_INGOT));
        helper.assertValueEqual(matrix.symmetry(helper.getLevel()), 0, "and its opposite number settles it again");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void skullsInPairsHoldItSteady(GameTestHelper helper) {
        InfusionMatrixBlockEntity matrix = laidOut(helper);
        helper.assertValueEqual(matrix.symmetry(helper.getLevel()), 0, "nothing about it yet");

        for (int pair = 0; pair < 10; pair++) {
            helper.setBlock(MATRIX.offset(3 + pair, -2, 4), Blocks.SKELETON_SKULL);
            helper.setBlock(MATRIX.offset(-3 - pair, -2, -4), Blocks.SKELETON_SKULL);
        }
        helper.assertValueEqual(matrix.symmetry(helper.getLevel()), -2, "twenty paired skulls are worth two points");

        helper.setBlock(MATRIX.offset(0, -2, 5), Blocks.SKELETON_SKULL);
        helper.assertValueEqual(matrix.symmetry(helper.getLevel()), -1,
                "and one on its own puts a little back on");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theRingMakesTheWorkingDangerous(GameTestHelper helper) {
        InfusionMatrixBlockEntity matrix = laidOut(helper);
        Player player = scholar(helper);
        stand(helper, MATRIX.offset(0, -2, 3), new ItemStack(Items.IRON_INGOT));

        matrix.wake(player);
        matrix.start(player);
        helper.assertTrue(!matrix.busy(), "a ring with a stray ingot on it is not a recipe");

        helper.setBlock(MATRIX.offset(0, -2, 3), Blocks.AIR);
        stand(helper, MATRIX.offset(0, -2, 3), ItemStack.EMPTY);
        matrix.start(player);
        helper.assertTrue(matrix.busy(), "an empty pedestal is not an ingredient");
        helper.assertTrue(matrix.instability() > 3, "but it is a lopsided ring, and the working knows it");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void itFindsTheRing(GameTestHelper helper) {
        helper.setBlock(MATRIX, ModBlocks.INFUSION_MATRIX.get());
        stand(helper, MATRIX.offset(3, -1, 0), ItemStack.EMPTY);
        stand(helper, MATRIX.offset(3, -4, 0), ItemStack.EMPTY);
        stand(helper, MATRIX.offset(0, -2, -5), ItemStack.EMPTY);

        var found = InfusionMatrixBlockEntity.around(helper.getLevel(), helper.absolutePos(MATRIX));
        helper.assertValueEqual(found.size(), 2, "two columns, however many pedestals are stacked in one");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void itStartsOnWhatIsLaidOut(GameTestHelper helper) {
        InfusionMatrixBlockEntity matrix = laidOut(helper);
        Player player = scholar(helper);
        matrix.wake(player);
        helper.assertTrue(matrix.start(player), "the working was laid out right");
        helper.assertTrue(matrix.busy(), "so it started");
        helper.assertTrue(!matrix.owed().isEmpty(), "and it owes for it");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void itWantsTheResearchDone(GameTestHelper helper) {
        InfusionMatrixBlockEntity matrix = laidOut(helper);
        matrix.wake(scholar(helper));
        helper.assertTrue(!matrix.start(helper.makeMockPlayer(GameType.SURVIVAL)),
                "nobody makes a rod by accident");
        helper.assertTrue(!matrix.busy(), "and nothing started");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 600)
    public static void itDrinksAndFinishes(GameTestHelper helper) {
        InfusionMatrixBlockEntity matrix = laidOut(helper);
        Player player = scholar(helper);
        matrix.wake(player);
        matrix.start(player);
        matrix.steady();

        run(helper, matrix, 20);

        ArcanePedestalBlockEntity under =
                (ArcanePedestalBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(MATRIX.below(2)));
        helper.assertTrue(under.held().is(ModItems.WAND_RODS.get("obsidian").get()),
                "the obsidian became a rod");
        helper.assertTrue(!matrix.busy(), "and the matrix went still");

        ArcanePedestalBlockEntity ring = (ArcanePedestalBlockEntity) helper.getLevel()
                .getBlockEntity(helper.absolutePos(MATRIX.offset(2, -2, 0)));
        helper.assertTrue(ring.held().isEmpty(), "the ring was eaten");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void takingTheWorkAwayEndsIt(GameTestHelper helper) {
        InfusionMatrixBlockEntity matrix = laidOut(helper);
        Player player = scholar(helper);
        matrix.wake(player);
        matrix.start(player);
        helper.setBlock(MATRIX.below(2), Blocks.AIR);

        run(helper, matrix, 1);
        helper.assertTrue(!matrix.busy(), "with nothing to work on there is no working");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theStonesUnderAnAltarChangeThePriceAndThePace(GameTestHelper helper) {
        InfusionMatrixBlockEntity plain = laidOut(helper);
        Player player = scholar(helper);
        plain.wake(player);
        plain.wake(player);
        helper.assertValueEqual(plain.cycle(), InfusionMatrixBlockEntity.CYCLE, "a bare altar turns at twenty");
        helper.assertValueEqual(plain.cost(), InfusionMatrixBlockEntity.FULL_COST, "and pays in full");
        AspectList full = plain.owed();

        under(helper, ModBlocks.INFUSION_SPEED_STONE);
        InfusionMatrixBlockEntity hurried = laidOut(helper);
        hurried.wake(player);
        hurried.wake(player);
        helper.assertValueEqual(hurried.cycle(), 12, "four speed stones take a turn down to twelve");
        helper.assertTrue(hurried.cost() > InfusionMatrixBlockEntity.FULL_COST, "and charge for it");

        under(helper, ModBlocks.INFUSION_COST_STONE);
        InfusionMatrixBlockEntity thrifty = laidOut(helper);
        thrifty.wake(player);
        thrifty.wake(player);
        helper.assertValueEqual(thrifty.cycle(), 24, "four cost stones make a turn longer");
        helper.assertValueEqual(thrifty.cost(), 92, "and take eight parts in a hundred off the bill");
        helper.assertTrue(thrifty.owed().total() <= full.total(),
                "which is less essentia to find, not more: " + thrifty.owed().total() + " of " + full.total());
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theLastTwoCapsAreFinishedOnTheAltar(GameTestHelper helper) {
        helper.assertTrue(
                ModWandParts.CAPS.keySet().stream().noneMatch(
                        at -> at.getPath().endsWith("_inert")),
                "a casting is not registered as a cap");

        altar(helper);
        stand(helper, MATRIX.below(2), new ItemStack(ModItems.INERT_CAPS.get("alchemium").get()));
        stand(helper, MATRIX.offset(2, -2, 0), new ItemStack(ModItems.SALIS_MUNDUS.get()));
        stand(helper, MATRIX.offset(-2, -2, 0), new ItemStack(ModItems.SALIS_MUNDUS.get()));
        stand(helper, MATRIX.offset(0, -2, 2), new ItemStack(ModItems.SALIS_MUNDUS.get()));
        jar(helper, MATRIX.offset(0, -2, -3), ModAspects.ENERGY, 32);
        jar(helper, MATRIX.offset(3, -2, 3), ModAspects.AURA, 32);

        InfusionMatrixBlockEntity matrix =
                (InfusionMatrixBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(MATRIX));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setData(ModAttachments.KNOWLEDGE,
                PlayerKnowledge.of(player).withResearch(Alchemia.id("wand_cap_alchemium")));
        matrix.wake(player);
        helper.assertValueEqual(matrix.wake(player), InfusionMatrixBlockEntity.Woken.STARTED,
                "the altar took the casting and the salis round it");
        matrix.steady();

        run(helper, matrix, 40);
        ArcanePedestalBlockEntity under =
                (ArcanePedestalBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(MATRIX.below(2)));
        helper.assertTrue(under.held().is(ModItems.WAND_CAPS.get("alchemium").get()),
                "and what is left on the pedestal is the finished cap, not the casting: " + under.held());
        helper.succeed();
    }

    private static void rebuildCorners(GameTestHelper helper, net.minecraft.world.level.block.Block stone) {
        for (int east = -1; east <= 1; east += 2) {
            for (int south = -1; south <= 1; south += 2) {
                helper.setBlock(MATRIX.offset(east, -2, south), stone);
                helper.setBlock(MATRIX.offset(east, -1, south), stone);
            }
        }
    }

    @GameTest(template = TEMPLATE)
    public static void olderStonesMakeADifferentAltar(GameTestHelper helper) {
        Player player = scholar(helper);
        laidOut(helper);
        rebuildCorners(helper, ModBlocks.ELDRITCH_STONE.block().get());
        InfusionMatrixBlockEntity eldritch =
                (InfusionMatrixBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(MATRIX));
        eldritch.wake(player);
        helper.assertTrue(helper.getBlockState(MATRIX.offset(1, -2, 1))
                .getValue(me.moonscenty.alchemia.block.ArcanePillarBlock.KIND)
                == me.moonscenty.alchemia.block.PillarKind.ELDRITCH, "it woke into eldritch pillars");
        eldritch.wake(player);
        helper.assertValueEqual(eldritch.cycle(), 16, "eldritch corners take a turn down to sixteen");
        helper.assertValueEqual(eldritch.cost(), 105, "and cost five parts in a hundred more");

        laidOut(helper);
        rebuildCorners(helper, ModBlocks.ANCIENT_STONE.block().get());
        InfusionMatrixBlockEntity ancient =
                (InfusionMatrixBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(MATRIX));
        ancient.wake(player);
        ancient.wake(player);
        helper.assertValueEqual(ancient.cycle(), 18, "ancient corners take a turn down to eighteen");
        helper.assertValueEqual(ancient.cost(), 90, "and take a tenth off the bill");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void deadTaintedRockIsPorous(GameTestHelper helper) {
        BlockPos at = new BlockPos(2, 2, 2);
        helper.setBlock(at, ModBlocks.TAINT_ROCK.get());
        ((me.moonscenty.alchemia.block.taint.TaintGroundBlock) ModBlocks.TAINT_ROCK.get()).die(helper.getLevel(),
                helper.absolutePos(at), helper.getBlockState(at));
        helper.assertBlockPresent(ModBlocks.POROUS_STONE.get(), at);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 600)
    public static void theRingMustStayToTheEnd(GameTestHelper helper) {
        InfusionMatrixBlockEntity matrix = laidOut(helper);
        Player player = scholar(helper);
        matrix.wake(player);
        matrix.start(player);
        matrix.steady();
        run(helper, matrix, 12);
        stand(helper, MATRIX.offset(2, -2, 0), ItemStack.EMPTY);
        stand(helper, MATRIX.offset(-2, -2, 0), ItemStack.EMPTY);
        matrix.steady();
        run(helper, matrix, 12);

        ArcanePedestalBlockEntity under =
                (ArcanePedestalBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(MATRIX.below(2)));
        helper.assertTrue(under.held().is(Items.OBSIDIAN), "nothing was made without the ring");
        helper.assertTrue(matrix.busy(), "and the work is still waiting on it");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void swappingTheWorkEndsIt(GameTestHelper helper) {
        InfusionMatrixBlockEntity matrix = laidOut(helper);
        Player player = scholar(helper);
        matrix.wake(player);
        matrix.start(player);
        stand(helper, MATRIX.below(2), new ItemStack(Items.STONE));
        run(helper, matrix, 1);
        helper.assertTrue(!matrix.busy(), "a different thing is not the working's to finish");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void enchantingCostsMoreOnAWorkedTool(GameTestHelper helper) {
        var recipe = (me.moonscenty.alchemia.crafting.InfusionRecipe) helper.getLevel().getRecipeManager()
                .byKey(Alchemia.id("infusion_enchantment_collector")).orElseThrow().value();
        ItemStack plain = new ItemStack(Items.IRON_PICKAXE);
        ItemStack worked = me.moonscenty.alchemia.enchantment.InfusionEnchantments.raised(plain.copy(),
                me.moonscenty.alchemia.enchantment.InfusionEnchantment.REFINING);
        helper.assertValueEqual(recipe.essentiaFor(plain).get(ModAspects.DESIRE), 16, "the plain price");
        helper.assertValueEqual(recipe.essentiaFor(worked).get(ModAspects.DESIRE), (int) (16 * 1.33F),
                "a third again for one other enchantment");
        helper.succeed();
    }
}
