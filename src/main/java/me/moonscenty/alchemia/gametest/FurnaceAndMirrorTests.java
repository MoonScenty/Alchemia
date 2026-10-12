package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.InfernalFurnaceBlock;
import me.moonscenty.alchemia.block.MirrorBlock;
import me.moonscenty.alchemia.block.entity.InfernalFurnaceBlockEntity;
import me.moonscenty.alchemia.block.entity.JarBlockEntity;
import me.moonscenty.alchemia.block.entity.MirrorBlockEntity;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The infernal furnace and the mirrors: the last of the large devices. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class FurnaceAndMirrorTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final BlockPos CORNER = new BlockPos(4, 2, 4);

    /** Nether brick at the corners, obsidian on the edges and floor, lava in the middle, bars on the north side. */
    private static void layOut(GameTestHelper helper) {
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    BlockPos at = CORNER.offset(x, y, z);
                    if (x != 1 && z != 1) {
                        helper.setBlock(at, Blocks.NETHER_BRICKS);
                    } else if (x == 1 && z == 1) {
                        helper.setBlock(at, y == 0 ? Blocks.OBSIDIAN : y == 1 ? Blocks.LAVA : Blocks.AIR);
                    } else {
                        helper.setBlock(at, Blocks.OBSIDIAN);
                    }
                }
            }
        }
        helper.setBlock(CORNER.offset(1, 1, 0), Blocks.IRON_BARS);
    }

    /** Laid out right, it is made into a furnace whose mouth is where the bars were. */
    @GameTest(template = TEMPLATE)
    public static void anInfernalFurnaceIsMadeFromItsBlueprint(GameTestHelper helper) {
        layOut(helper);
        BlockPos corner = helper.absolutePos(CORNER);
        helper.assertTrue(InfernalFurnaceBlock.fits(helper.getLevel(), corner), "the layout fits");
        InfernalFurnaceBlock.build(helper.getLevel(), corner);
        helper.assertBlockPresent(ModBlocks.INFERNAL_FURNACE.get(), CORNER.offset(1, 1, 1));
        helper.assertBlockPresent(Blocks.AIR, CORNER.offset(1, 1, 0));
        helper.assertTrue(helper.getBlockState(CORNER.offset(1, 1, 1)).getValue(InfernalFurnaceBlock.FACING)
                == Direction.SOUTH, "it faces away from the mouth");
        helper.succeed();
    }

    /** Something smeltable put in comes out smelted, out of the mouth. */
    @GameTest(template = TEMPLATE, timeoutTicks = 300)
    public static void anInfernalFurnaceSmeltsWhatIsPutInIt(GameTestHelper helper) {
        layOut(helper);
        InfernalFurnaceBlock.build(helper.getLevel(), helper.absolutePos(CORNER));
        BlockPos heart = helper.absolutePos(CORNER.offset(1, 1, 1));
        InfernalFurnaceBlockEntity furnace = (InfernalFurnaceBlockEntity) helper.getLevel().getBlockEntity(heart);
        helper.assertTrue(furnace.take(new ItemStack(Items.RAW_IRON)), "it takes the raw iron");
        for (int tick = 0; tick < 200; tick++) {
            InfernalFurnaceBlockEntity.tick(helper.getLevel(), heart, helper.getLevel().getBlockState(heart),
                    furnace);
        }
        helper.assertItemEntityPresent(Items.IRON_INGOT, CORNER.offset(1, 1, 0), 2.0);
        helper.succeed();
    }

    /** Breaking any of its shell puts the rest back as it was, lava and bars too. */
    @GameTest(template = TEMPLATE)
    public static void breakingAnInfernalFurnacePutsItBack(GameTestHelper helper) {
        layOut(helper);
        InfernalFurnaceBlock.build(helper.getLevel(), helper.absolutePos(CORNER));
        helper.getLevel().destroyBlock(helper.absolutePos(CORNER), false);
        helper.assertBlockPresent(Blocks.LAVA, CORNER.offset(1, 1, 1));
        helper.assertBlockPresent(Blocks.IRON_BARS, CORNER.offset(1, 1, 0));
        helper.assertBlockPresent(Blocks.OBSIDIAN, CORNER.offset(1, 0, 1));
        helper.succeed();
    }

    private static MirrorBlockEntity mirror(GameTestHelper helper, BlockPos at, boolean essentia) {
        helper.setBlock(at.below(), Blocks.STONE);
        helper.setBlock(at, (essentia ? ModBlocks.MIRROR_ESSENTIA : ModBlocks.MIRROR).get().defaultBlockState()
                .setValue(MirrorBlock.FACING, Direction.UP));
        return (MirrorBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(at));
    }

    /** A stack that comes into one of a pair comes out of the other, an item at a time. */
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void aPairOfMirrorsPassesItems(GameTestHelper helper) {
        MirrorBlockEntity near = mirror(helper, new BlockPos(2, 2, 2), false);
        MirrorBlockEntity far = mirror(helper, new BlockPos(10, 2, 10), false);
        near.remember(far.here());
        helper.assertTrue(near.paired() && far.paired(), "the two are a pair");

        BlockPos at = helper.absolutePos(new BlockPos(2, 2, 2));
        ItemEntity thrown = new ItemEntity(helper.getLevel(), at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5,
                new ItemStack(Items.DIAMOND, 3));
        helper.getLevel().addFreshEntity(thrown);
        helper.assertTrue(near.send(thrown), "the near one takes the diamonds");
        BlockPos farAt = helper.absolutePos(new BlockPos(10, 2, 10));
        for (int tick = 0; tick < 40; tick++) {
            MirrorBlockEntity.tick(helper.getLevel(), farAt, helper.getLevel().getBlockState(farAt), far);
        }
        helper.assertItemEntityPresent(Items.DIAMOND, new BlockPos(10, 2, 10), 2.0);
        helper.assertTrue(far.queued().isEmpty(), "and all three came out");
        helper.succeed();
    }

    /** An essentia mirror stands in for the jar in front of its partner. */
    @GameTest(template = TEMPLATE)
    public static void anEssentiaMirrorDrawsOnTheJarsByItsPartner(GameTestHelper helper) {
        MirrorBlockEntity near = mirror(helper, new BlockPos(2, 2, 2), true);
        MirrorBlockEntity far = mirror(helper, new BlockPos(10, 2, 10), true);
        near.remember(far.here());
        helper.setBlock(new BlockPos(10, 3, 10), ModBlocks.JAR.get());
        JarBlockEntity jar = (JarBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(
                new BlockPos(10, 3, 10)));
        for (int i = 0; i < 5; i++) {
            jar.accept(ModAspects.FIRE);
        }
        helper.assertValueEqual(near.held().get(ModAspects.FIRE), 5, "the near one shows the far jar's fire");
        helper.assertTrue(near.release(ModAspects.FIRE), "and gives it up");
        helper.assertValueEqual(jar.held().get(ModAspects.FIRE), 4, "out of that jar");
        helper.succeed();
    }
}
