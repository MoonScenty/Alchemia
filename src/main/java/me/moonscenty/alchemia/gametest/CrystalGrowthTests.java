package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aura.AuraChunk;
import me.moonscenty.alchemia.aura.AuraGeneration;
import me.moonscenty.alchemia.aura.ModAuraAttachment;
import me.moonscenty.alchemia.block.CrystalBlock;
import me.moonscenty.alchemia.block.CrystalGrowth;
import me.moonscenty.alchemia.block.CrystalType;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * A crystal is the one thing that turns aura into something a player can pick up, so the land has to be able to feed
 * it, starve it and spoil it, and none of those may happen for free.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class CrystalGrowthTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final BlockPos ROCK = new BlockPos(2, 1, 2);
    private static final BlockPos CRYSTAL = ROCK.above();
    private static final int BASE = AuraGeneration.BASE;

    /** Sets the land to hold exactly this much of everything, so a test can put it wherever it likes. */
    private static void land(GameTestHelper helper, int base, int air, int flux) {
        AspectList aspects = AspectList.EMPTY.add(ModAspects.AIR, air).add(ModAspects.FLUX, flux);
        helper.getLevel().getChunkAt(helper.absolutePos(CRYSTAL)).setData(ModAuraAttachment.AURA, new AuraChunk(base, aspects));
    }

    private static int airInLand(GameTestHelper helper) {
        return helper.getLevel().getChunkAt(helper.absolutePos(CRYSTAL)).getData(ModAuraAttachment.AURA).get(ModAspects.AIR);
    }

    private static BlockState crystal(int age, int generation) {
        return ModBlocks.CRYSTALS.get(CrystalType.AIR).get().defaultBlockState()
                .setValue(CrystalBlock.FACING, Direction.UP)
                .setValue(CrystalBlock.AGE, age)
                .setValue(CrystalBlock.GENERATION, generation);
    }

    private static void plant(GameTestHelper helper, BlockState crystal) {
        helper.setBlock(ROCK, Blocks.STONE);
        helper.setBlock(CRYSTAL, crystal);
    }

    /** Runs the growth enough times that the per-generation dice cannot keep it from happening. */
    private static void tickHard(GameTestHelper helper, long seed) {
        RandomSource random = RandomSource.create(seed);
        for (int i = 0; i < 64; i++) {
            BlockState state = helper.getBlockState(CRYSTAL);
            CrystalGrowth.tick(state, helper.getLevel(), helper.absolutePos(CRYSTAL), random);
        }
    }

    @GameTest(template = TEMPLATE)
    public static void aGluttedLandGrowsItsCrystalsAndPaysForIt(GameTestHelper helper) {
        plant(helper, crystal(0, 1));
        // more than enough to grow all the way, but not so much that growth reads as free
        land(helper, BASE, BASE * 2, 0);
        int before = airInLand(helper);

        tickHard(helper, 1L);

        int age = helper.getBlockState(CRYSTAL).getValue(CrystalBlock.AGE);
        helper.assertTrue(age == CrystalBlock.MAX_AGE, "with plenty about the crystal should have grown out, is " + age);
        helper.assertTrue(airInLand(helper) < before, "growing should have taken something out of the land");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aStarvedLandTakesItsCrystalsBack(GameTestHelper helper) {
        plant(helper, crystal(CrystalBlock.MAX_AGE, 1));
        land(helper, BASE, 0, 0);

        tickHard(helper, 2L);

        // each size given back feeds the land a little, so it stops being starved before the crystal is all gone
        int age = helper.getBlockState(CRYSTAL).getValue(CrystalBlock.AGE);
        helper.assertTrue(age < CrystalBlock.MAX_AGE, "with nothing to live on the crystal should have shrunk, is " + age);
        helper.assertTrue(airInLand(helper) > 0, "what the crystal gave up should have gone back into the land");
        helper.assertTrue(helper.getBlockState(CRYSTAL).is(ModBlocks.CRYSTALS.get(CrystalType.AIR).get()),
                "a lone crystal should hang on at its smallest rather than vanish");
        helper.succeed();
    }

    /** The last of a patch stays; it is only one with company that goes altogether. */
    @GameTest(template = TEMPLATE)
    public static void aStarvedCrystalWithCompanyIsTheOneThatGoes(GameTestHelper helper) {
        plant(helper, crystal(0, 1));
        helper.setBlock(CRYSTAL.east().below(), Blocks.STONE);
        helper.setBlock(CRYSTAL.east(), crystal(0, 1));
        land(helper, BASE, 0, 0);

        tickHard(helper, 3L);

        helper.assertTrue(helper.getBlockState(CRYSTAL).isAir(), "the smallest crystal in a patch should have gone");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aLandThickWithFluxSoursItsCrystals(GameTestHelper helper) {
        plant(helper, crystal(2, 1));
        // neither starved nor glutted, so nothing else fires, and flux ahead of what the crystal is made of
        land(helper, BASE, BASE / 2, BASE);

        tickHard(helper, 4L);

        BlockState now = helper.getBlockState(CRYSTAL);
        helper.assertTrue(now.is(ModBlocks.CRYSTALS.get(CrystalType.FLUX).get()), "the crystal should have turned to flux");
        helper.assertTrue(now.getValue(CrystalBlock.AGE) == 2, "and kept its size in the turning");
        helper.succeed();
    }

    /** The world's own crystals can stand tall; each seeding stops shorter than the last. */
    @GameTest(template = TEMPLATE)
    public static void laterGenerationsStopShorter(GameTestHelper helper) {
        BlockPos anywhere = new BlockPos(7, 9, 11);
        int first = CrystalGrowth.fullGrown(anywhere, 1);
        int last = CrystalGrowth.fullGrown(anywhere, CrystalBlock.LAST_GENERATION);
        helper.assertTrue(first > last, "a later generation should be capped lower than the first");
        helper.assertTrue(last >= 1, "but even the last should get past its seed size");
        helper.succeed();
    }
}
