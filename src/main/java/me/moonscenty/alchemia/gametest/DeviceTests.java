package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.BellowsBlock;
import me.moonscenty.alchemia.block.entity.BellowsBlockEntity;
import me.moonscenty.alchemia.crafting.InfusionRecipe;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The devices of step ten, and the parts they are built from.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class DeviceTests {
    private static final String TEMPLATE = "empty_32x40x32";

    private static void bellows(GameTestHelper helper, BlockPos at, Direction facing) {
        helper.setBlock(at, ModBlocks.BELLOWS.get().defaultBlockState().setValue(BellowsBlock.FACING, facing));
    }

    /** Bellows blowing on a furnace add a tick of cooking every second tick, so it cooks half again as fast. */
    @GameTest(template = TEMPLATE)
    public static void bellowsHurryAFurnace(GameTestHelper helper) {
        BlockPos furnaceAt = new BlockPos(2, 2, 2);
        helper.setBlock(furnaceAt, Blocks.FURNACE);
        BlockPos bellowsAt = furnaceAt.east();
        bellows(helper, bellowsAt, Direction.WEST);

        AbstractFurnaceBlockEntity furnace = (AbstractFurnaceBlockEntity) helper.getBlockEntity(furnaceAt);
        furnace.cookingTotalTime = 200;
        furnace.cookingProgress = 10;
        BellowsBlockEntity pumping = (BellowsBlockEntity) helper.getBlockEntity(bellowsAt);
        BlockState state = helper.getBlockState(bellowsAt);
        for (int tick = 0; tick < 10; tick++) {
            BellowsBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(bellowsAt), state, pumping);
        }
        helper.assertValueEqual(furnace.cookingProgress, 15, "ten ticks of blowing is five ticks of cooking");
        helper.succeed();
    }

    /** A furnace with nothing cooking in it is not started by being blown on, nor is the last tick taken from it. */
    @GameTest(template = TEMPLATE)
    public static void bellowsDoNotStartOrFinishTheWork(GameTestHelper helper) {
        BlockPos furnaceAt = new BlockPos(2, 2, 2);
        helper.setBlock(furnaceAt, Blocks.FURNACE);
        BlockPos bellowsAt = furnaceAt.east();
        bellows(helper, bellowsAt, Direction.WEST);
        AbstractFurnaceBlockEntity furnace = (AbstractFurnaceBlockEntity) helper.getBlockEntity(furnaceAt);
        BellowsBlockEntity pumping = (BellowsBlockEntity) helper.getBlockEntity(bellowsAt);
        BlockState state = helper.getBlockState(bellowsAt);

        furnace.cookingTotalTime = 200;
        furnace.cookingProgress = 0;
        for (int tick = 0; tick < 4; tick++) {
            BellowsBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(bellowsAt), state, pumping);
        }
        helper.assertValueEqual(furnace.cookingProgress, 0, "an idle furnace stays idle");

        furnace.cookingProgress = 199;
        for (int tick = 0; tick < 4; tick++) {
            BellowsBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(bellowsAt), state, pumping);
        }
        helper.assertValueEqual(furnace.cookingProgress, 199, "and the furnace finishes its own work");
        helper.succeed();
    }

    /** A signal holds them still. */
    @GameTest(template = TEMPLATE)
    public static void redstoneHoldsTheBellowsStill(GameTestHelper helper) {
        BlockPos bellowsAt = new BlockPos(2, 2, 2);
        bellows(helper, bellowsAt, Direction.WEST);
        helper.setBlock(bellowsAt.above(), Blocks.REDSTONE_BLOCK);
        helper.runAfterDelay(2, () -> {
            helper.assertBlockProperty(bellowsAt, BellowsBlock.ENABLED, false);
            helper.setBlock(bellowsAt.above(), Blocks.AIR);
            helper.runAfterDelay(2, () -> {
                helper.assertBlockProperty(bellowsAt, BellowsBlock.ENABLED, true);
                helper.succeed();
            });
        });
    }

    /** Only bellows facing the block, and pumping, are counted as blowing on it. */
    @GameTest(template = TEMPLATE)
    public static void onlyBellowsFacingInAreCounted(GameTestHelper helper) {
        BlockPos middle = new BlockPos(4, 2, 4);
        helper.setBlock(middle, Blocks.STONE);
        bellows(helper, middle.east(), Direction.WEST);
        bellows(helper, middle.west(), Direction.EAST);
        bellows(helper, middle.north(), Direction.NORTH);
        BlockPos at = helper.absolutePos(middle);
        helper.assertValueEqual(BellowsBlock.blowingOn(helper.getLevel(), at,
                Direction.EAST, Direction.WEST, Direction.NORTH), 2, "the one facing away does not count");
        helper.succeed();
    }

    /** An arcane lamp sets lights down in the dark round it, and takes them back when it is put out. */
    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void anArcaneLampLightsTheDark(GameTestHelper helper) {
        BlockPos ceiling = new BlockPos(8, 6, 8);
        helper.setBlock(ceiling, Blocks.STONE);
        BlockPos lampAt = ceiling.below();
        helper.setBlock(lampAt, ModBlocks.ARCANE_LAMP.get().defaultBlockState()
                .setValue(me.moonscenty.alchemia.block.LampBlock.FACING, Direction.UP)
                .setValue(me.moonscenty.alchemia.block.LampBlock.ENABLED, true));
        helper.runAfterDelay(300, () -> {
            helper.assertTrue(lights(helper, lampAt) > 0, "lights have been set down");
            helper.setBlock(lampAt.east(), Blocks.REDSTONE_BLOCK);
            helper.runAfterDelay(2, () -> {
                helper.assertValueEqual(lights(helper, lampAt), 0, "and a signal takes them all back");
                helper.succeed();
            });
        });
    }

    private static int lights(GameTestHelper helper, BlockPos lampAt) {
        int found = 0;
        BlockPos middle = helper.absolutePos(lampAt);
        for (BlockPos at : BlockPos.betweenClosed(middle.offset(-15, -15, -15), middle.offset(15, 15, 15))) {
            if (helper.getLevel().getBlockState(at).is(ModBlocks.LAMP_LIGHT.get())) {
                found++;
            }
        }
        return found;
    }

    /** A lamp falls when what it hangs from is taken away. */
    @GameTest(template = TEMPLATE)
    public static void aLampFallsWithoutItsSupport(GameTestHelper helper) {
        BlockPos ceiling = new BlockPos(3, 4, 3);
        helper.setBlock(ceiling, Blocks.STONE);
        helper.setBlock(ceiling.below(), ModBlocks.ARCANE_LAMP.get().defaultBlockState()
                .setValue(me.moonscenty.alchemia.block.LampBlock.FACING, Direction.UP));
        helper.setBlock(ceiling, Blocks.AIR);
        helper.assertBlockNotPresent(ModBlocks.ARCANE_LAMP.get(), ceiling.below());
        helper.succeed();
    }

    /** The lamp of growth eats only herba, and only through the face it hangs from. */
    @GameTest(template = TEMPLATE)
    public static void theGrowthLampIsFedThroughItsBack(GameTestHelper helper) {
        BlockPos at = new BlockPos(3, 2, 3);
        helper.setBlock(at.below(), Blocks.STONE);
        helper.setBlock(at, ModBlocks.GROWTH_LAMP.get().defaultBlockState()
                .setValue(me.moonscenty.alchemia.block.LampBlock.FACING, Direction.DOWN));
        var lamp = (me.moonscenty.alchemia.block.entity.GrowthLampBlockEntity) helper.getBlockEntity(at);
        helper.assertTrue(lamp.reachableFrom(Direction.DOWN), "fed from the face it hangs from");
        helper.assertFalse(lamp.reachableFrom(Direction.UP), "and from nowhere else");
        helper.assertFalse(lamp.wants(me.moonscenty.alchemia.registry.ModAspects.FIRE), "and only herba");
        helper.assertTrue(lamp.accept(me.moonscenty.alchemia.registry.ModAspects.PLANT), "takes a point");
        helper.assertFalse(lamp.wants(me.moonscenty.alchemia.registry.ModAspects.PLANT), "but only one in hand");
        helper.succeed();
    }

    /**
     * Fed, the lamp of growth reaches every plant round it. It walks the ground a column a tick in a shuffled
     * order and visits each column once before it starts again, so a full round of thirteen by thirteen is
     * certain to have found the one crop there is -- whether the crop takes the turn is the crop's business.
     */
    @GameTest(template = TEMPLATE)
    public static void theGrowthLampReachesACrop(GameTestHelper helper) {
        BlockPos lampAt = new BlockPos(4, 4, 4);
        helper.setBlock(lampAt.above(), Blocks.STONE);
        helper.setBlock(lampAt, ModBlocks.GROWTH_LAMP.get().defaultBlockState()
                .setValue(me.moonscenty.alchemia.block.LampBlock.FACING, Direction.UP));
        BlockPos crop = new BlockPos(4, 2, 5);
        helper.setBlock(crop.below(), Blocks.FARMLAND);
        helper.setBlock(crop, Blocks.WHEAT);
        var lamp = (me.moonscenty.alchemia.block.entity.GrowthLampBlockEntity) helper.getBlockEntity(lampAt);
        lamp.accept(me.moonscenty.alchemia.registry.ModAspects.PLANT);

        BlockPos where = helper.absolutePos(lampAt);
        for (int tick = 0; tick < 13 * 13 + 1; tick++) {
            me.moonscenty.alchemia.block.entity.GrowthLampBlockEntity.tick(helper.getLevel(), where,
                    helper.getLevel().getBlockState(where), lamp);
        }
        helper.assertTrue(lamp.charges() < 100, "it spent a turn of growing on the wheat");
        helper.succeed();
    }

    /** Fed, the lamp of fertility sets two of a kind to courting. */
    @GameTest(template = TEMPLATE)
    public static void theFertilityLampMakesAMatch(GameTestHelper helper) {
        BlockPos at = new BlockPos(4, 4, 4);
        helper.setBlock(at.above(), Blocks.STONE);
        helper.setBlock(at, ModBlocks.FERTILITY_LAMP.get().defaultBlockState()
                .setValue(me.moonscenty.alchemia.block.LampBlock.FACING, Direction.UP));
        var one = helper.spawn(net.minecraft.world.entity.EntityType.COW, new BlockPos(3, 2, 3));
        var two = helper.spawn(net.minecraft.world.entity.EntityType.COW, new BlockPos(5, 2, 5));
        var lamp = (me.moonscenty.alchemia.block.entity.FertilityLampBlockEntity) helper.getBlockEntity(at);
        lamp.accept(me.moonscenty.alchemia.registry.ModAspects.LIFE);
        lamp.accept(me.moonscenty.alchemia.registry.ModAspects.LIFE);
        lamp.matchmake(helper.getLevel());
        helper.assertTrue(one.isInLove() && two.isInLove(), "both are courting");
        helper.assertValueEqual(lamp.charges(), 0, "at two points the pair");
        helper.succeed();
    }

    /** A hungry chest eats what comes to rest on it. */
    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void aHungryChestEatsWhatFallsOnIt(GameTestHelper helper) {
        BlockPos at = new BlockPos(3, 2, 3);
        helper.setBlock(at, ModBlocks.HUNGRY_CHEST.get());
        helper.spawnItem(net.minecraft.world.item.Items.DIAMOND, 3.5F, 3.2F, 3.5F);
        helper.runAfterDelay(40, () -> {
            var chest = (me.moonscenty.alchemia.block.entity.HungryChestBlockEntity) helper.getBlockEntity(at);
            helper.assertTrue(chest.countItem(net.minecraft.world.item.Items.DIAMOND) == 1, "it ate the diamond");
            helper.succeed();
        });
    }

    /** The urn fills a bucket by hand, and tops up a crucible beside it by itself. */
    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void theUrnFillsWhatIsRoundIt(GameTestHelper helper) {
        BlockPos jugAt = new BlockPos(3, 2, 3);
        helper.setBlock(jugAt, ModBlocks.WATER_JUG.get());
        BlockPos potAt = jugAt.east();
        helper.setBlock(potAt, ModBlocks.CRUCIBLE.get());
        var jug = (me.moonscenty.alchemia.block.entity.WaterJugBlockEntity) helper.getBlockEntity(jugAt);

        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                new ItemStack(net.minecraft.world.item.Items.BUCKET));
        helper.useBlock(jugAt, player);
        helper.assertTrue(player.getMainHandItem().is(net.minecraft.world.item.Items.WATER_BUCKET),
                "the bucket came away full");

        // give it water in hand rather than wait on the air over a test plot
        jug.drawn(-3 * me.moonscenty.alchemia.block.entity.WaterJugBlockEntity.FILL);
        helper.runAfterDelay(380, () -> {
            var pot = (me.moonscenty.alchemia.block.entity.CrucibleBlockEntity) helper.getBlockEntity(potAt);
            helper.assertTrue(pot.water() > 0, "and the crucible has water in it");
            helper.succeed();
        });
    }

    /** A levitator facing up, with aer to burn, lifts what is dropped on it. */
    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void aLevitatorLiftsWhatIsOverIt(GameTestHelper helper) {
        BlockPos at = new BlockPos(3, 2, 3);
        helper.setBlock(at, ModBlocks.LEVITATOR.get().defaultBlockState()
                .setValue(me.moonscenty.alchemia.block.LevitatorBlock.FACING, Direction.UP));
        var levitator = (me.moonscenty.alchemia.block.entity.LevitatorBlockEntity) helper.getBlockEntity(at);
        // give it aer in hand rather than wait on the air over a test plot
        levitator.given(12000);
        var item = helper.spawnItem(net.minecraft.world.item.Items.FEATHER, 3.5F, 3.2F, 3.5F);
        double from = item.getY();
        helper.runAfterDelay(30, () -> {
            helper.assertTrue(item.getY() > from + 1.0, "the feather has risen, to " + (item.getY() - from));
            helper.assertTrue(levitator.vis() < 12000, "and the lifting cost it aer");
            helper.succeed();
        });
    }

    /** A levitator reaches as far as the first solid block, and its range steps round from eight. */
    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void aLevitatorStopsShortOfASolidBlock(GameTestHelper helper) {
        BlockPos at = new BlockPos(3, 2, 3);
        helper.setBlock(at, ModBlocks.LEVITATOR.get().defaultBlockState()
                .setValue(me.moonscenty.alchemia.block.LevitatorBlock.FACING, Direction.UP));
        helper.setBlock(at.above(3), Blocks.STONE);
        var levitator = (me.moonscenty.alchemia.block.entity.LevitatorBlockEntity) helper.getBlockEntity(at);
        helper.assertValueEqual(levitator.range(), 8, "eight blocks to begin with");
        levitator.stepRange();
        levitator.stepRange();
        levitator.stepRange();
        helper.assertValueEqual(levitator.range(), 4, "and round through sixteen and thirty-two to four");
        helper.assertValueEqual(levitator.cost(), 8, "costing twice its range");
        helper.runAfterDelay(20, () -> {
            helper.assertValueEqual(levitator.reach(), 2, "it reaches the two blocks below the stone");
            helper.succeed();
        });
    }

    private static BlockState relay(Direction facing, int in, int out) {
        return ModBlocks.REDSTONE_RELAY.get().defaultBlockState()
                .setValue(me.moonscenty.alchemia.block.RedstoneRelayBlock.FACING, facing)
                .setValue(me.moonscenty.alchemia.block.RedstoneRelayBlock.IN, in)
                .setValue(me.moonscenty.alchemia.block.RedstoneRelayBlock.OUT, out);
    }

    /** A relay answers only to a signal as strong as it asks for, and gives out what it is set to. */
    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void aRelayAnswersAtItsThreshold(GameTestHelper helper) {
        BlockPos asksTooMuch = relayLane(helper, 3, 15);
        BlockPos asksEnough = relayLane(helper, 6, 14);
        helper.runAfterDelay(6, () -> {
            helper.assertBlockProperty(asksTooMuch, me.moonscenty.alchemia.block.RedstoneRelayBlock.POWERED, false);
            helper.assertBlockProperty(asksTooMuch.south(), net.minecraft.world.level.block.RedStoneWireBlock.POWER, 0);
            helper.assertBlockProperty(asksEnough, me.moonscenty.alchemia.block.RedstoneRelayBlock.POWERED, true);
            helper.assertBlockProperty(asksEnough.south(), net.minecraft.world.level.block.RedStoneWireBlock.POWER, 6);
            helper.succeed();
        });
    }

    /**
     * A relay asking for the given signal and giving six, with a wire out of it, and then fed from a block of redstone
     * down two lengths of wire, so that what reaches it is fourteen.
     */
    private static BlockPos relayLane(GameTestHelper helper, int x, int asks) {
        for (int z = 0; z <= 4; z++) {
            helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
        }
        BlockPos at = new BlockPos(x, 2, 3);
        helper.setBlock(at, relay(Direction.NORTH, asks, 6));
        helper.setBlock(at.south(), Blocks.REDSTONE_WIRE);
        helper.setBlock(new BlockPos(x, 2, 0), Blocks.REDSTONE_BLOCK);
        helper.setBlock(new BlockPos(x, 2, 1), Blocks.REDSTONE_WIRE);
        helper.setBlock(new BlockPos(x, 2, 2), Blocks.REDSTONE_WIRE);
        return at;
    }

    /** The relay's toggles step under a click: the back one sets what it takes, the front one what it gives. */
    @GameTest(template = TEMPLATE)
    public static void aRelaysTogglesStep(GameTestHelper helper) {
        BlockPos at = new BlockPos(3, 2, 3);
        helper.setBlock(at.below(), Blocks.STONE);
        helper.setBlock(at, relay(Direction.SOUTH, 1, 15));
        BlockPos where = helper.absolutePos(at);
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.CREATIVE);
        // facing south: the input end is south, its right hand west
        click(helper, at, player, where.getCenter().add(0.0, -0.3, 0.25));
        helper.assertBlockProperty(at, me.moonscenty.alchemia.block.RedstoneRelayBlock.IN, 2);
        click(helper, at, player, where.getCenter().add(-0.25, -0.3, -0.25));
        helper.assertBlockProperty(at, me.moonscenty.alchemia.block.RedstoneRelayBlock.OUT, 1);
        click(helper, at, player, where.getCenter().add(0.25, -0.3, 0.0));
        helper.assertBlockProperty(at, me.moonscenty.alchemia.block.RedstoneRelayBlock.IN, 2);
        helper.assertBlockProperty(at, me.moonscenty.alchemia.block.RedstoneRelayBlock.OUT, 1);
        helper.succeed();
    }

    private static void click(GameTestHelper helper, BlockPos at, net.minecraft.world.entity.player.Player player,
            net.minecraft.world.phys.Vec3 hit) {
        helper.useBlock(at, player, new net.minecraft.world.phys.BlockHitResult(hit, Direction.UP,
                helper.absolutePos(at), false));
    }

    /** An ear on stone hears a note block on stone play its note, and only its note. */
    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void anArcaneEarHearsItsNote(GameTestHelper helper) {
        BlockPos stone = new BlockPos(3, 2, 3);
        helper.setBlock(stone, Blocks.STONE);
        BlockPos earAt = stone.above();
        helper.setBlock(earAt, ModBlocks.ARCANE_EAR.get().defaultBlockState()
                .setValue(me.moonscenty.alchemia.block.ArcaneEarBlock.FACING, Direction.UP));
        var ear = (me.moonscenty.alchemia.block.entity.ArcaneEarBlockEntity) helper.getBlockEntity(earAt);
        ear.raisePitch();
        helper.assertTrue(ear.voice() == net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASEDRUM,
                "it listens in the voice of stone");

        BlockPos noteAt = new BlockPos(8, 2, 8);
        helper.setBlock(noteAt.below(), Blocks.STONE);
        helper.setBlock(noteAt, Blocks.NOTE_BLOCK.defaultBlockState().setValue(
                net.minecraft.world.level.block.NoteBlock.INSTRUMENT,
                net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASEDRUM));
        helper.getLevel().blockEvent(helper.absolutePos(noteAt), Blocks.NOTE_BLOCK, 0, 0);
        helper.runAfterDelay(3, () -> {
            helper.assertBlockProperty(earAt, me.moonscenty.alchemia.block.ArcaneEarBlock.ENABLED, false);
            helper.setBlock(noteAt, helper.getBlockState(noteAt).setValue(net.minecraft.world.level.block.NoteBlock.NOTE, 1));
            helper.getLevel().blockEvent(helper.absolutePos(noteAt), Blocks.NOTE_BLOCK, 0, 0);
            helper.runAfterDelay(3, () -> {
                helper.assertBlockProperty(earAt, me.moonscenty.alchemia.block.ArcaneEarBlock.ENABLED, true);
                helper.assertTrue(helper.getLevel().getSignal(helper.absolutePos(stone), Direction.EAST) == 15,
                        "the stone it is fixed to carries its signal");
                helper.runAfterDelay(12, () -> {
                    helper.assertBlockProperty(earAt, me.moonscenty.alchemia.block.ArcaneEarBlock.ENABLED, false);
                    helper.succeed();
                });
            });
        });
    }

    /** Bath salts dissolving in still water turn it to purifying fluid; anywhere else they are only gone. */
    @GameTest(template = TEMPLATE)
    public static void bathSaltsTurnStillWater(GameTestHelper helper) {
        BlockPos pool = new BlockPos(3, 2, 3);
        helper.setBlock(pool, Blocks.WATER);
        BlockPos dry = new BlockPos(6, 2, 3);
        for (BlockPos at : new BlockPos[] {pool, dry}) {
            var salts = new net.minecraft.world.entity.item.ItemEntity(helper.getLevel(),
                    helper.absolutePos(at).getX() + 0.5, helper.absolutePos(at).getY() + 0.2,
                    helper.absolutePos(at).getZ() + 0.5, new ItemStack(ModItems.BATH_SALTS.get()));
            me.moonscenty.alchemia.item.BathSaltsItem.onExpire(
                    new net.neoforged.neoforge.event.entity.item.ItemExpireEvent(salts));
        }
        helper.assertBlockPresent(ModBlocks.PURIFYING_FLUID.get(), pool);
        helper.assertBlockPresent(Blocks.AIR, dry);
        helper.assertValueEqual(new ItemStack(ModItems.BATH_SALTS.get()).getEntityLifespan(helper.getLevel()), 200,
                "and they last ten seconds on the ground");
        helper.succeed();
    }

    /** A dip in a still pool wards a player against warp, and uses the pool up; deeper warp, shorter ward. */
    @GameTest(template = TEMPLATE)
    public static void purifyingFluidWardsAgainstWarp(GameTestHelper helper) {
        BlockPos pool = new BlockPos(3, 2, 3);
        helper.setBlock(pool, ModBlocks.PURIFYING_FLUID.get());
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        BlockPos where = helper.absolutePos(pool);
        helper.getLevel().getBlockState(where).entityInside(helper.getLevel(), where, player);
        var ward = player.getEffect(me.moonscenty.alchemia.player.effect.ModEffects.WARP_WARD);
        helper.assertTrue(ward != null && ward.getDuration() == 32000, "warded for the longest there is");
        helper.assertBlockNotPresent(ModBlocks.PURIFYING_FLUID.get(), pool);
        helper.assertValueEqual(me.moonscenty.alchemia.block.PurifyingFluidBlock.wardFor(
                new me.moonscenty.alchemia.player.WarpData(100, 0, 0, 0)), 20000, "a tenth as long at a hundred");
        helper.succeed();
    }

    /** The spa takes water from a bucket, as anything that holds liquid takes it. */
    @GameTest(template = TEMPLATE)
    public static void aSpaIsFilledFromABucket(GameTestHelper helper) {
        BlockPos at = new BlockPos(3, 2, 3);
        helper.setBlock(at, ModBlocks.SPA.get());
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                new ItemStack(net.minecraft.world.item.Items.WATER_BUCKET));
        helper.useBlock(at, player);
        var spa = (me.moonscenty.alchemia.block.entity.SpaBlockEntity) helper.getBlockEntity(at);
        helper.assertValueEqual(spa.tank().getFluidAmount(), 1000, "a bucket of water in the tank");
        helper.assertTrue(player.getMainHandItem().is(net.minecraft.world.item.Items.BUCKET), "and the bucket empty");
        helper.succeed();
    }

    /** Fed water and salts, the spa pours a pool of purifying fluid above it, spreading out from the middle. */
    @GameTest(template = TEMPLATE)
    public static void aSpaPoursAPurifyingPool(GameTestHelper helper) {
        BlockPos at = new BlockPos(4, 2, 4);
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                helper.setBlock(at.offset(x, 0, z), Blocks.STONE);
            }
        }
        helper.setBlock(at, ModBlocks.SPA.get());
        var spa = (me.moonscenty.alchemia.block.entity.SpaBlockEntity) helper.getBlockEntity(at);
        spa.tank().fill(new net.neoforged.neoforge.fluids.FluidStack(
                net.minecraft.world.level.material.Fluids.WATER, 3000),
                net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
        spa.setItem(0, new ItemStack(ModItems.BATH_SALTS.get(), 3));
        BlockPos where = helper.absolutePos(at);
        for (int tick = 0; tick < 81; tick++) {
            me.moonscenty.alchemia.block.entity.SpaBlockEntity.tick(helper.getLevel(), where,
                    helper.getLevel().getBlockState(where), spa);
        }
        helper.assertTrue(helper.getLevel().getFluidState(where.above()).is(
                me.moonscenty.alchemia.registry.ModFluids.PURIFYING.get()), "a pool of it over the spa");
        int pools = 0;
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (helper.getLevel().getFluidState(where.above().offset(x, 0, z)).isSourceOfType(
                        me.moonscenty.alchemia.registry.ModFluids.PURIFYING.get())) {
                    pools++;
                }
            }
        }
        helper.assertValueEqual(pools, 3, "three pours, three still blocks of it");
        helper.assertValueEqual(spa.tank().getFluidAmount(), 0, "all the water gone");
        helper.assertTrue(spa.getItem(0).isEmpty(), "and all the salts");
        helper.succeed();
    }

    /** Switched off mixing, the spa pours what is in its tank as it is. */
    @GameTest(template = TEMPLATE)
    public static void aSpaPoursItsTankUnmixed(GameTestHelper helper) {
        BlockPos at = new BlockPos(3, 2, 3);
        helper.setBlock(at, ModBlocks.SPA.get());
        var spa = (me.moonscenty.alchemia.block.entity.SpaBlockEntity) helper.getBlockEntity(at);
        spa.toggleMixing();
        spa.tank().fill(new net.neoforged.neoforge.fluids.FluidStack(
                net.minecraft.world.level.material.Fluids.WATER, 1000),
                net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
        BlockPos where = helper.absolutePos(at);
        me.moonscenty.alchemia.block.entity.SpaBlockEntity.tick(helper.getLevel(), where,
                helper.getLevel().getBlockState(where), spa);
        helper.assertBlockPresent(Blocks.WATER, at.above());
        helper.succeed();
    }

    /** Sets the chunk under a place to hold exactly this much aer and flux, against the usual base. */
    private static void aura(GameTestHelper helper, BlockPos at, int air, int flux) {
        helper.getLevel().getChunkAt(helper.absolutePos(at)).setData(
                me.moonscenty.alchemia.aura.ModAuraAttachment.AURA,
                new me.moonscenty.alchemia.aura.AuraChunk(me.moonscenty.alchemia.aura.AuraGeneration.BASE,
                        me.moonscenty.alchemia.aspect.AspectList.EMPTY
                                .add(me.moonscenty.alchemia.registry.ModAspects.AIR, air)
                                .add(me.moonscenty.alchemia.registry.ModAspects.FLUX, flux)));
    }

    /** A wand set on a recharge pedestal is filled from the aura; one taken off it comes back to the hand. */
    @GameTest(template = TEMPLATE)
    public static void aRechargePedestalFillsAWand(GameTestHelper helper) {
        BlockPos at = new BlockPos(3, 2, 3);
        aura(helper, at, 100, 0);
        helper.setBlock(at, ModBlocks.RECHARGE_PEDESTAL.get());
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack wand = me.moonscenty.alchemia.item.WandItem.of(me.moonscenty.alchemia.registry.ModWandParts.SILVERWOOD,
                me.moonscenty.alchemia.registry.ModWandParts.GOLD);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, wand);
        helper.useBlock(at, player);
        var pedestal = (me.moonscenty.alchemia.block.entity.RechargePedestalBlockEntity) helper.getBlockEntity(at);
        helper.assertTrue(player.getMainHandItem().isEmpty(), "the wand went onto the pedestal");
        BlockPos where = helper.absolutePos(at);
        me.moonscenty.alchemia.block.entity.RechargePedestalBlockEntity.tick(helper.getLevel(), where,
                helper.getLevel().getBlockState(where), pedestal);
        ItemStack held = pedestal.getItem(0);
        int aer = ((me.moonscenty.alchemia.item.WandItem) held.getItem()).held(held,
                me.moonscenty.alchemia.registry.ModAspects.AIR);
        helper.assertTrue(aer > 0, "and drew aer for it, " + aer);
        helper.useBlock(at, player);
        helper.assertTrue(player.getMainHandItem().getItem() instanceof me.moonscenty.alchemia.item.WandItem,
                "and gave it back");
        helper.succeed();
    }

    /** A dioptra tuned with an aer shard reads how much aer is in each chunk, and a comparator reads its own. */
    @GameTest(template = TEMPLATE)
    public static void aDioptraReadsTheAura(GameTestHelper helper) {
        BlockPos at = new BlockPos(3, 2, 3);
        aura(helper, at, 75, 0);
        helper.setBlock(at, ModBlocks.DIOPTRA.get());
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                new ItemStack(ModItems.SHARDS.get(me.moonscenty.alchemia.block.CrystalType.AIR).get()));
        BlockPos where = helper.absolutePos(at);
        helper.useBlock(at, player, new net.minecraft.world.phys.BlockHitResult(where.getCenter().add(0, 0.5, 0),
                Direction.UP, where, false));
        var dioptra = (me.moonscenty.alchemia.block.entity.DioptraBlockEntity) helper.getBlockEntity(at);
        helper.assertValueEqual(dioptra.tuning(), 1, "tuned to aer");
        helper.assertTrue(player.getMainHandItem().isEmpty(), "the shard went in");
        dioptra.read(helper.getLevel());
        int middle = me.moonscenty.alchemia.block.entity.DioptraBlockEntity.MIDDLE;
        helper.assertValueEqual(dioptra.amount(middle, middle), 50, "seventy-five is half of half again a hundred");
        helper.assertValueEqual(dioptra.signal(), 7, "and a comparator reads it as seven");
        helper.useBlock(at, player, new net.minecraft.world.phys.BlockHitResult(where.getCenter().add(0, 0.5, 0),
                Direction.UP, where, false));
        helper.assertValueEqual(dioptra.tuning(), -1, "an empty hand takes the shard out");
        helper.assertTrue(player.getInventory().countItem(
                ModItems.SHARDS.get(me.moonscenty.alchemia.block.CrystalType.AIR).get()) == 1, "and gets it back");
        helper.succeed();
    }

    /** A totem counts the poles under it, five at most, and is quickened by the inner and outer ones. */
    @GameTest(template = TEMPLATE)
    public static void aTotemCountsItsPoles(GameTestHelper helper) {
        BlockPos at = new BlockPos(3, 8, 3);
        helper.setBlock(at.below(1), ModBlocks.AURA_TOTEM_POLE_INNER.get());
        helper.setBlock(at.below(2), ModBlocks.AURA_TOTEM_POLE_OUTER.get());
        helper.setBlock(at.below(3), ModBlocks.AURA_TOTEM_POLE_OUTER.get());
        helper.setBlock(at.below(4), ModBlocks.AURA_TOTEM_POLE_PURE.get());
        helper.setBlock(at.below(5), ModBlocks.AURA_TOTEM_POLE_INNER.get());
        helper.setBlock(at.below(6), ModBlocks.AURA_TOTEM_POLE_INNER.get());
        helper.setBlock(at, ModBlocks.AURA_TOTEM_PUSH.get());
        var totem = (me.moonscenty.alchemia.block.entity.AuraTotemBlockEntity) helper.getBlockEntity(at);
        totem.countPoles();
        helper.assertValueEqual(totem.inner(), 2, "two inner poles in the first five");
        helper.assertValueEqual(totem.outer(), 2, "two outer");
        helper.assertValueEqual(totem.pure(), 1, "one stabilising, and the sixth not counted");
        helper.assertValueEqual(totem.every(), 6, "a step every six ticks");
        helper.succeed();
    }

    /** A shard put in through a pole lands in the totem above; nothing comes back out that way. */
    @GameTest(template = TEMPLATE)
    public static void aPoleFeedsItsTotem(GameTestHelper helper) {
        BlockPos at = new BlockPos(3, 3, 3);
        helper.setBlock(at.below(), ModBlocks.AURA_TOTEM_POLE_INNER.get());
        helper.setBlock(at, ModBlocks.AURA_TOTEM_PULL.get());
        var pole = helper.getLevel().getCapability(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                helper.absolutePos(at.below()), Direction.NORTH);
        helper.assertTrue(pole != null, "the pole takes things");
        ItemStack left = pole.insertItem(0, new ItemStack(ModItems.BALANCED_SHARD.get(), 2), false);
        helper.assertTrue(left.isEmpty(), "both shards went in");
        var totem = (me.moonscenty.alchemia.block.entity.AuraTotemBlockEntity) helper.getBlockEntity(at);
        helper.assertValueEqual(totem.getItem(0).getCount(), 2, "into the totem");
        helper.assertTrue(pole.extractItem(0, 1, false).isEmpty(), "and none come out through the pole");
        helper.assertTrue(pole.insertItem(0, new ItemStack(net.minecraft.world.item.Items.DIRT), false).getCount()
                == 1, "and nothing but shards goes in");
        helper.succeed();
    }

    /** A dispersal totem burns a shard and moves its aspect out of its own chunk. */
    @GameTest(template = TEMPLATE)
    public static void aDispersalTotemMovesVisOut(GameTestHelper helper) {
        BlockPos at = new BlockPos(3, 2, 3);
        aura(helper, at, 100, 0);
        helper.setBlock(at, ModBlocks.AURA_TOTEM_PUSH.get());
        var totem = (me.moonscenty.alchemia.block.entity.AuraTotemBlockEntity) helper.getBlockEntity(at);
        totem.setItem(0, new ItemStack(ModItems.SHARDS.get(me.moonscenty.alchemia.block.CrystalType.AIR).get()));
        BlockPos where = helper.absolutePos(at);
        BlockState state = helper.getLevel().getBlockState(where);
        for (int tick = 0; tick < 10 * 21; tick++) {
            me.moonscenty.alchemia.block.entity.AuraTotemBlockEntity.tick(helper.getLevel(), where, state, totem);
        }
        helper.assertTrue(totem.getItem(0).isEmpty(), "the shard was burnt");
        helper.assertValueEqual(totem.tuning(), 1, "tuned to aer");
        helper.assertValueEqual(totem.time(), me.moonscenty.alchemia.block.entity.AuraTotemBlockEntity.LASTS - 20,
                "twenty steps taken of it");
        int left = me.moonscenty.alchemia.aura.AuraHandler.get(helper.getLevel(), where,
                me.moonscenty.alchemia.registry.ModAspects.AIR);
        helper.assertValueEqual(left, 80, "a point of aer gone from its chunk each step");
        helper.succeed();
    }

    /** The totems take a wisp's essence, of any kind, as the original's did. */
    @GameTest(template = TEMPLATE)
    public static void totemsTakeAWispsEssence(GameTestHelper helper) {
        for (String name : new String[] {"aura_totem_push", "aura_totem_pull"}) {
            var recipe = (InfusionRecipe) helper.getLevel().getRecipeManager().byKey(Alchemia.id(name))
                    .orElseThrow().value();
            var fire = me.moonscenty.alchemia.item.WispyEssenceItem.of(me.moonscenty.alchemia.registry.ModAspects.FIRE);
            helper.assertTrue(recipe.ring().stream().anyMatch(part -> part.test(fire)), name + " takes it");
        }
        helper.succeed();
    }

    /** The autocaster is built round the original's made mind again, with the resonator in its ring. */
    @GameTest(template = TEMPLATE)
    public static void theAutocasterIsBuiltOfItsOwnParts(GameTestHelper helper) {
        var recipe = helper.getLevel().getRecipeManager().byKey(Alchemia.id("autocaster")).orElseThrow();
        InfusionRecipe infusion = (InfusionRecipe) recipe.value();
        helper.assertTrue(infusion.central().test(new ItemStack(ModItems.BIOTHAUMIC_MIND.get())),
                "a biothaumic mind at the middle");
        helper.assertTrue(infusion.ring().stream().anyMatch(part ->
                part.test(new ItemStack(ModItems.MORPHIC_RESONATOR.get()))), "and a resonator round it");

        var crusher = (InfusionRecipe) helper.getLevel().getRecipeManager()
                .byKey(Alchemia.id("primal_crusher")).orElseThrow().value();
        helper.assertValueEqual(crusher.ring().stream().filter(part ->
                part.test(new ItemStack(ModItems.PRIMAL_CHARM.get()))).count(), 2L, "two charms in the crusher's");
        helper.succeed();
    }
}
