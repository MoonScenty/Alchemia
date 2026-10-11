package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.enchantment.InfusionEnchantment;
import me.moonscenty.alchemia.enchantment.InfusionEnchantments;
import me.moonscenty.alchemia.item.Clusters;
import me.moonscenty.alchemia.item.WarpingGear;
import me.moonscenty.alchemia.item.tool.ElementalShovelItem;
import me.moonscenty.alchemia.player.WarpData;
import me.moonscenty.alchemia.player.WarpHandler;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The five elemental tools and the crusher.
 * <p>
 * What is worth proving is mostly what they do that an ordinary tool does not: the workings they come with, the
 * shovel laying a square out of the bag, the hoe tilling a square, the crusher mending itself and weighing on its
 * holder. And the clusters, which three different things now hand out and which have to be the right ones.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class ElementalToolTests {
    private static final String TEMPLATE = "empty_32x40x32";

    private static ItemStack tool(String name) {
        return new ItemStack(ModItems.ELEMENTAL_TOOLS.get(name).get());
    }

    /** Ore of all four kinds has a cluster, and what is not ore has none. */
    @GameTest(template = TEMPLATE)
    public static void oreHasItsCluster(GameTestHelper helper) {
        helper.assertTrue(Clusters.of(new ItemStack(Items.RAW_IRON)).is(ModItems.IRON_CLUSTER.get()),
                "raw iron refines to an iron cluster");
        helper.assertTrue(Clusters.of(new ItemStack(Items.RAW_GOLD, 2)).getCount() == 2,
                "as many clusters as there were pieces");
        helper.assertTrue(Clusters.of(new ItemStack(Items.IRON_ORE)).is(ModItems.IRON_CLUSTER.get()),
                "and an ore block taken up whole counts as much");
        helper.assertTrue(Clusters.of(new ItemStack(Items.RAW_COPPER)).is(ModItems.COPPER_CLUSTER.get()),
                "copper too");
        helper.assertTrue(Clusters.of(new ItemStack(Items.COBBLESTONE)).isEmpty(), "but stone is only stone");

        RandomSource random = RandomSource.create(1L);
        ItemStack raw = new ItemStack(Items.RAW_IRON);
        helper.assertTrue(Clusters.maybe(raw, 1.0F, random).is(ModItems.IRON_CLUSTER.get()), "a sure thing is sure");
        helper.assertTrue(Clusters.maybe(raw, 0.0F, random) == raw, "and no chance is no chance");
        helper.succeed();
    }

    /** Each comes with what its element is in a tool, from the moment it exists. */
    @GameTest(template = TEMPLATE)
    public static void eachComesWithItsWorkings(GameTestHelper helper) {
        helper.assertTrue(InfusionEnchantments.has(tool("elemental_axe"), InfusionEnchantment.BURROWING)
                && InfusionEnchantments.has(tool("elemental_axe"), InfusionEnchantment.COLLECTOR),
                "the axe fells from the top and sends what falls home");
        helper.assertValueEqual(InfusionEnchantments.level(tool("elemental_pickaxe"), InfusionEnchantment.SOUNDING),
                2, "the pickaxe sounds twice as deep");
        helper.assertTrue(InfusionEnchantments.has(tool("elemental_pickaxe"), InfusionEnchantment.REFINING),
                "and refines");
        helper.assertValueEqual(InfusionEnchantments.level(tool("elemental_sword"), InfusionEnchantment.ARCING), 2,
                "the sword sweeps");
        helper.assertTrue(InfusionEnchantments.has(tool("elemental_shovel"), InfusionEnchantment.DESTRUCTIVE),
                "the shovel digs a square");
        ItemStack crusher = new ItemStack(ModItems.PRIMAL_CRUSHER.get());
        helper.assertTrue(InfusionEnchantments.has(crusher, InfusionEnchantment.DESTRUCTIVE)
                && InfusionEnchantments.has(crusher, InfusionEnchantment.REFINING), "the crusher both");

        ItemStack raised = InfusionEnchantments.raised(tool("elemental_pickaxe"), InfusionEnchantment.SOUNDING);
        helper.assertValueEqual(InfusionEnchantments.level(raised, InfusionEnchantment.SOUNDING), 3,
                "and an altar can still take one further");
        helper.succeed();
    }

    /** The shovel lays a square of what it was used against, out of the bag, one per square. */
    @GameTest(template = TEMPLATE)
    public static void theShovelLaysASquareOutOfTheBag(GameTestHelper helper) {
        Player digger = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack shovel = tool("elemental_shovel");
        digger.setItemInHand(InteractionHand.MAIN_HAND, shovel);
        digger.getInventory().add(new ItemStack(Blocks.COBBLESTONE, 9));

        BlockPos clicked = new BlockPos(4, 2, 4);
        helper.setBlock(clicked, Blocks.COBBLESTONE);
        BlockHitResult face = new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(clicked)), Direction.UP,
                helper.absolutePos(clicked), false);
        shovel.useOn(new UseOnContext(digger, InteractionHand.MAIN_HAND, face));

        for (BlockPos at : ElementalShovelItem.square(shovel, helper.absolutePos(clicked), Direction.UP, 0.0F)) {
            helper.assertTrue(helper.getLevel().getBlockState(at).is(Blocks.COBBLESTONE), "a stone at " + at);
        }
        helper.assertTrue(digger.getInventory().countItem(Blocks.COBBLESTONE.asItem()) == 0,
                "nine squares took nine stones");
        helper.succeed();
    }

    /** It runs out when the bag does, and lays only as many as it was given. */
    @GameTest(template = TEMPLATE)
    public static void theShovelStopsWhenTheBagIsEmpty(GameTestHelper helper) {
        Player digger = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack shovel = tool("elemental_shovel");
        digger.setItemInHand(InteractionHand.MAIN_HAND, shovel);
        digger.getInventory().add(new ItemStack(Blocks.COBBLESTONE, 3));

        BlockPos clicked = new BlockPos(4, 2, 4);
        helper.setBlock(clicked, Blocks.COBBLESTONE);
        shovel.useOn(new UseOnContext(digger, InteractionHand.MAIN_HAND, new BlockHitResult(
                Vec3.atCenterOf(helper.absolutePos(clicked)), Direction.UP, helper.absolutePos(clicked), false)));

        int laid = 0;
        for (BlockPos at : ElementalShovelItem.square(shovel, helper.absolutePos(clicked), Direction.UP, 0.0F)) {
            if (helper.getLevel().getBlockState(at).is(Blocks.COBBLESTONE)) {
                laid++;
            }
        }
        helper.assertValueEqual(laid, 3, "three stones, three squares");
        helper.succeed();
    }

    /** The setting key turns it round its three ways of laying, and back to the first. */
    @GameTest(template = TEMPLATE)
    public static void theShovelTurnsRoundItsThreeWays(GameTestHelper helper) {
        Player digger = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack shovel = tool("elemental_shovel");
        helper.assertValueEqual(ElementalShovelItem.laying(shovel), 0, "it starts flat");
        ElementalShovelItem.turn(digger, shovel);
        ElementalShovelItem.turn(digger, shovel);
        helper.assertValueEqual(ElementalShovelItem.laying(shovel), 2, "two turns is the third way");
        ElementalShovelItem.turn(digger, shovel);
        helper.assertValueEqual(ElementalShovelItem.laying(shovel), 0, "and a third goes back round");
        helper.succeed();
    }

    /** The hoe tills a square where an ordinary hoe tills one. */
    @GameTest(template = TEMPLATE)
    public static void theHoeTillsASquare(GameTestHelper helper) {
        Player farmer = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack hoe = tool("elemental_hoe");
        farmer.setItemInHand(InteractionHand.MAIN_HAND, hoe);
        BlockPos middle = new BlockPos(4, 1, 4);
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                helper.setBlock(middle.offset(x, 0, z), Blocks.DIRT);
                helper.setBlock(middle.offset(x, 1, z), Blocks.AIR);
            }
        }
        hoe.useOn(new UseOnContext(farmer, InteractionHand.MAIN_HAND, new BlockHitResult(
                Vec3.atCenterOf(helper.absolutePos(middle)), Direction.UP, helper.absolutePos(middle), false)));

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                helper.assertBlockPresent(Blocks.FARMLAND, middle.offset(x, 0, z));
            }
        }
        helper.succeed();
    }

    /** The crusher mends itself wherever it is carried. */
    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void theCrusherMendsItself(GameTestHelper helper) {
        Player holder = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack crusher = new ItemStack(ModItems.PRIMAL_CRUSHER.get());
        crusher.setDamageValue(10);
        for (int tick = 0; tick <= 40; tick++) {
            holder.tickCount = tick;
            crusher.inventoryTick(helper.getLevel(), holder, 0, true);
        }
        helper.assertTrue(crusher.getDamageValue() < 10, "it is less worn than it was");
        helper.succeed();
    }

    /** It is the right tool for stone and earth alike, and for nothing that grows. */
    @GameTest(template = TEMPLATE)
    public static void theCrusherIsAPickaxeAndAShovel(GameTestHelper helper) {
        ItemStack crusher = new ItemStack(ModItems.PRIMAL_CRUSHER.get());
        helper.assertTrue(crusher.isCorrectToolForDrops(Blocks.STONE.defaultBlockState()), "stone");
        helper.assertTrue(crusher.isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState()), "obsidian");
        helper.assertTrue(crusher.getDestroySpeed(Blocks.DIRT.defaultBlockState()) > 1.0F, "earth, quickly");
        helper.assertTrue(crusher.getDestroySpeed(Blocks.OAK_LOG.defaultBlockState()) <= 1.0F,
                "but not a tree");
        helper.succeed();
    }

    /** Held, it weighs on its holder: warp events come likelier and worse, and wind down slower. */
    @GameTest(template = TEMPLATE)
    public static void theCrusherCarriesWarp(GameTestHelper helper) {
        Player holder = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertValueEqual(WarpingGear.carried(holder), 0, "an empty hand carries none");
        holder.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.PRIMAL_CRUSHER.get()));
        helper.assertValueEqual(WarpingGear.carried(holder), 2, "the crusher carries two");

        WarpData warp = WarpData.NONE.add(WarpData.Kind.PERMANENT, 10).withCounter(100);
        helper.assertTrue(WarpHandler.severityOf(warp, 2) > WarpHandler.severityOf(warp, 0), "and it hurts more");
        helper.assertTrue(WarpHandler.afterEvent(warp, 2).counter() > WarpHandler.afterEvent(warp, 0).counter(),
                "and it lets go slower");
        helper.succeed();
    }

    /** Held up, the axe draws loose things towards whoever is holding it. */
    @GameTest(template = TEMPLATE)
    public static void theAxeDrawsLooseThingsIn(GameTestHelper helper) {
        Player holder = helper.makeMockPlayer(GameType.SURVIVAL);
        holder.moveTo(helper.absoluteVec(new Vec3(2.5, 2.0, 2.5)));
        ItemStack axe = tool("elemental_axe");
        ItemEntity loose = new ItemEntity(helper.getLevel(), holder.getX() + 6.0, holder.getY(), holder.getZ(),
                new ItemStack(Items.STICK));
        loose.setDeltaMovement(Vec3.ZERO);
        helper.getLevel().addFreshEntity(loose);

        axe.getItem().onUseTick(helper.getLevel(), holder, axe, 100);
        helper.assertTrue(loose.getDeltaMovement().x < 0.0, "it is moving back towards the axe");
        helper.succeed();
    }
}
