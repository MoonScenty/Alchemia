package me.moonscenty.alchemia.gametest;

import java.util.List;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aura.AuraChunk;
import me.moonscenty.alchemia.aura.AuraGeneration;
import me.moonscenty.alchemia.aura.ModAuraAttachment;
import me.moonscenty.alchemia.block.taint.TaintFibreBlock;
import me.moonscenty.alchemia.block.taint.TaintSpread;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class TaintRuleTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final BlockPos HERE = new BlockPos(8, 8, 8);

    private static void flux(GameTestHelper helper) {
        BlockPos middle = helper.absolutePos(HERE);
        for (int east = -16; east <= 16; east += 16) {
            for (int south = -16; south <= 16; south += 16) {
                helper.getLevel().getChunkAt(middle.offset(east, 0, south)).setData(ModAuraAttachment.AURA,
                        new AuraChunk(AuraGeneration.BASE,
                                AspectList.EMPTY.add(ModAspects.FLUX, AuraGeneration.BASE)));
            }
        }
    }

    private static void tick(GameTestHelper helper, BlockPos at, long seed) {
        BlockPos where = helper.absolutePos(at);
        helper.getLevel().getBlockState(where).randomTick(helper.getLevel(), where, RandomSource.create(seed));
    }

    private static List<FallingBlockEntity> falling(GameTestHelper helper) {
        return helper.getLevel().getEntitiesOfClass(FallingBlockEntity.class, helper.getBounds().inflate(2.0));
    }

    private static ItemStack enchanted(GameTestHelper helper, net.minecraft.resources.ResourceKey<
            net.minecraft.world.item.enchantment.Enchantment> which) {
        ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
        tool.enchant(helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(which), 1);
        return tool;
    }

    private static List<ItemStack> drops(GameTestHelper helper, Block block, ItemStack tool) {
        BlockPos where = helper.absolutePos(HERE);
        return Block.getDrops(block.defaultBlockState(), helper.getLevel(), where, null, null, tool);
    }

    @GameTest(template = TEMPLATE)
    public static void crustWaitsForARandomTickToFall(GameTestHelper helper) {
        flux(helper);
        helper.setBlock(HERE, ModBlocks.TAINT_CRUST.get());
        helper.runAfterDelay(10, () -> {
            helper.assertBlockPresent(ModBlocks.TAINT_CRUST.get(), HERE);
            tick(helper, HERE, 1L);
            helper.assertBlockPresent(Blocks.AIR, HERE);
            helper.assertTrue(falling(helper).size() == 1, "it should be falling now");
            helper.assertFalse(falling(helper).get(0).dropItem, "and it should never come back as an item");
            falling(helper).get(0).discard();
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE)
    public static void crustClingsToWhatIsNotTaint(GameTestHelper helper) {
        flux(helper);
        helper.setBlock(HERE, ModBlocks.TAINT_CRUST.get());
        helper.setBlock(HERE.east(), Blocks.STONE);
        tick(helper, HERE, 2L);
        helper.assertBlockPresent(ModBlocks.TAINT_CRUST.get(), HERE);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void crustHangsOnBesideATaintedLog(GameTestHelper helper) {
        flux(helper);
        helper.setBlock(HERE, ModBlocks.TAINT_CRUST.get());
        helper.setBlock(HERE.below().east(), ModBlocks.TAINT_LOG.get());
        tick(helper, HERE, 3L);
        helper.assertBlockPresent(ModBlocks.TAINT_CRUST.get(), HERE);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aTaintedLogHoldsItsLeaves(GameTestHelper helper) {
        helper.setBlock(HERE, Blocks.OAK_LEAVES);
        helper.setBlock(HERE.east(), ModBlocks.TAINT_LOG.get());
        helper.runAfterDelay(5, () -> {
            helper.assertTrue(helper.getBlockState(HERE).getValue(LeavesBlock.DISTANCE) == 1,
                    "the leaves should count the tainted log as a trunk");
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE)
    public static void taintBurnsAndGlowsAsTheOriginal(GameTestHelper helper) {
        BlockPos where = helper.absolutePos(HERE);
        BlockState fibre = ModBlocks.TAINT_FIBRE.get().defaultBlockState();
        BlockState log = ModBlocks.TAINT_LOG.get().defaultBlockState();
        helper.assertValueEqual(fibre.getFlammability(helper.getLevel(), where, Direction.UP), 3, "fibre flammability");
        helper.assertValueEqual(fibre.getFireSpreadSpeed(helper.getLevel(), where, Direction.UP), 3, "fibre spread");
        helper.assertValueEqual(log.getFlammability(helper.getLevel(), where, Direction.UP), 4, "log flammability");
        helper.assertValueEqual(log.getFireSpreadSpeed(helper.getLevel(), where, Direction.UP), 4, "log spread");
        helper.assertValueEqual(TaintFibreBlock.lightOf(fibre.setValue(TaintFibreBlock.GROWTH, TaintFibreBlock.LUMP)), 12,
                "lump light");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void onlySilkTouchKeepsTaintedGround(GameTestHelper helper) {
        ItemStack silk = enchanted(helper, Enchantments.SILK_TOUCH);
        for (Block block : List.of(ModBlocks.TAINT_SOIL.get(), ModBlocks.TAINT_CRUST.get(), ModBlocks.TAINT_ROCK.get(),
                ModBlocks.TAINT_GEYSER.get())) {
            List<ItemStack> kept = drops(helper, block, silk);
            helper.assertTrue(kept.size() == 1 && kept.get(0).is(block.asItem()), block + " should come away whole");
        }
        helper.assertTrue(drops(helper, ModBlocks.TAINT_SOIL.get(), ItemStack.EMPTY).isEmpty(), "plain soil gives nothing");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void taintedRockSometimesLeavesAFluxCrystal(GameTestHelper helper) {
        int crystals = 0;
        int rolls = 3000;
        for (int i = 0; i < rolls; i++) {
            for (ItemStack dropped : drops(helper, ModBlocks.TAINT_ROCK.get(), ItemStack.EMPTY)) {
                helper.assertTrue(dropped.is(ModItems.CRYSTALLIZED_ESSENCE.get()), "only a crystal, found " + dropped);
                crystals++;
            }
        }
        helper.assertTrue(crystals > rolls / 30 && crystals < rolls / 8, "about one in fifteen, got " + crystals);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aTaintedLogDropsItself(GameTestHelper helper) {
        List<ItemStack> dropped = drops(helper, ModBlocks.TAINT_LOG.get(), ItemStack.EMPTY);
        helper.assertTrue(dropped.size() == 1 && dropped.get(0).is(ModBlocks.TAINT_LOG.get().asItem()), "got " + dropped);
        helper.succeed();
    }

    private static void hem(GameTestHelper helper, Block block) {
        flux(helper);
        helper.setBlock(HERE, block);
        for (Direction side : Direction.Plane.HORIZONTAL) {
            helper.setBlock(HERE.relative(side), ModBlocks.TAINT_SOIL.get());
        }
        helper.setBlock(HERE.below(), ModBlocks.TAINT_ROCK.get());
        RandomSource random = RandomSource.create(9L);
        for (int i = 0; i < 400; i++) {
            TaintSpread.spread(helper.getLevel(), helper.absolutePos(HERE.north()), random);
        }
    }

    @GameTest(template = TEMPLATE)
    public static void bricksRotToRock(GameTestHelper helper) {
        hem(helper, Blocks.BRICKS);
        helper.assertBlockPresent(ModBlocks.TAINT_ROCK.get(), HERE);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void oresRotToRock(GameTestHelper helper) {
        hem(helper, Blocks.IRON_ORE);
        helper.assertBlockPresent(ModBlocks.TAINT_ROCK.get(), HERE);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void netherrackRotsToRock(GameTestHelper helper) {
        hem(helper, Blocks.NETHERRACK);
        helper.assertBlockPresent(ModBlocks.TAINT_ROCK.get(), HERE);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void woolIsLeftAlone(GameTestHelper helper) {
        hem(helper, Blocks.WHITE_WOOL);
        helper.assertBlockPresent(Blocks.WHITE_WOOL, HERE);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void hayRotsToSoil(GameTestHelper helper) {
        hem(helper, Blocks.HAY_BLOCK);
        helper.assertBlockPresent(ModBlocks.TAINT_SOIL.get(), HERE);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void sugarCaneTurnsToFibres(GameTestHelper helper) {
        flux(helper);
        BlockPos cane = HERE.above();
        helper.setBlock(HERE, Blocks.STONE);
        helper.setBlock(HERE.east(), Blocks.WATER);
        helper.setBlock(HERE.north(), Blocks.SAND);
        helper.setBlock(cane.north(), ModBlocks.TAINT_SOIL.get());
        helper.setBlock(HERE, Blocks.SAND);
        helper.setBlock(cane, Blocks.SUGAR_CANE);
        RandomSource random = RandomSource.create(11L);
        for (int i = 0; i < 400 && helper.getBlockState(cane).is(Blocks.SUGAR_CANE); i++) {
            TaintSpread.spread(helper.getLevel(), helper.absolutePos(cane.north()), random);
        }
        helper.assertBlockPresent(ModBlocks.TAINT_FIBRE.get(), cane);
        helper.succeed();
    }
}
