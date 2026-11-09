package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.block.AlembicBlock;
import me.moonscenty.alchemia.block.JarBlock;
import me.moonscenty.alchemia.block.LootBlock;
import me.moonscenty.alchemia.block.entity.AlembicBlockEntity;
import me.moonscenty.alchemia.block.entity.ArcanePedestalBlockEntity;
import me.moonscenty.alchemia.block.entity.CrucibleBlockEntity;
import me.moonscenty.alchemia.block.entity.EssentiaSmelterBlockEntity;
import me.moonscenty.alchemia.block.entity.JarBlockEntity;
import me.moonscenty.alchemia.item.PhialItem;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.menu.ArcaneWorkbenchMenu;
import me.moonscenty.alchemia.menu.EssentiaSmelterMenu;
import me.moonscenty.alchemia.player.ModAttachments;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModWandParts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;

@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class DeviceRulesTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final BlockPos WHERE = new BlockPos(4, 2, 4);

    private static BlockHitResult face(GameTestHelper helper, Direction side) {
        return new BlockHitResult(helper.absolutePos(WHERE).getCenter(), side, helper.absolutePos(WHERE), false);
    }

    private static CrucibleBlockEntity hot(GameTestHelper helper, AspectList dissolved) {
        helper.setBlock(WHERE, ModBlocks.CRUCIBLE.get());
        CrucibleBlockEntity crucible = helper.getBlockEntity(WHERE);
        CompoundTag tag = new CompoundTag();
        tag.putInt("water", 500);
        tag.putInt("heat", 200);
        AspectList.write(tag, "dissolved", dissolved, helper.getLevel().registryAccess());
        crucible.loadCustomOnly(tag, helper.getLevel().registryAccess());
        return crucible;
    }

    private static JarBlockEntity jar(GameTestHelper helper, int amount) {
        helper.setBlock(WHERE, ModBlocks.JAR.get());
        JarBlockEntity jar = helper.getBlockEntity(WHERE);
        for (int i = 0; i < amount; i++) {
            jar.accept(ModAspects.FIRE);
        }
        return jar;
    }

    private static AlembicBlockEntity alembic(GameTestHelper helper, int amount) {
        helper.setBlock(WHERE, ModBlocks.ALEMBIC.get());
        AlembicBlockEntity alembic = helper.getBlockEntity(WHERE);
        for (int i = 0; i < amount; i++) {
            alembic.accept(ModAspects.FIRE);
        }
        return alembic;
    }

    private static int signal(GameTestHelper helper) {
        BlockState state = helper.getBlockState(WHERE);
        return state.getAnalogOutputSignal(helper.getLevel(), helper.absolutePos(WHERE));
    }

    @GameTest(template = TEMPLATE)
    public static void waterDoesNotWashThePotOut(GameTestHelper helper) {
        CrucibleBlockEntity crucible = hot(helper, AspectList.of(ModAspects.FIRE, 5));
        crucible.fill();
        helper.assertValueEqual(crucible.dissolved().get(ModAspects.FIRE), 5, "what was melted stays");
        helper.assertValueEqual(signal(helper), 1 + 5 * 14 / 100, "and shows on a comparator");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aBottleTopsThePotUpAndIsNotMelted(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        CrucibleBlockEntity crucible = hot(helper, AspectList.EMPTY);
        ItemStack bottle = new ItemStack(Items.POTION);
        bottle.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER));
        player.setItemInHand(InteractionHand.MAIN_HAND, bottle);
        helper.getBlockState(WHERE).useItemOn(bottle, helper.getLevel(), player, InteractionHand.MAIN_HAND, face(helper, Direction.UP));
        helper.assertValueEqual(crucible.water(), 750, "a bottle is a quarter of a bucket");
        helper.assertTrue(player.getMainHandItem().is(Items.GLASS_BOTTLE), "and leaves the bottle");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void thePotPrefersTheRichestRecipeYouKnow(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setData(ModAttachments.KNOWLEDGE, PlayerKnowledge.of(player)
                .withResearch(Alchemia.id("alchemical_manufacture")).withResearch(Alchemia.id("entropic_processing")));
        AspectList both = AspectList.of(ModAspects.PLANT, 2).add(ModAspects.ENTROPY, 2);

        CrucibleBlockEntity crucible = hot(helper, both);
        crucible.putByHand(new ItemStack(Items.DIRT), player);
        helper.assertItemEntityPresent(Items.MYCELIUM, WHERE, 3.0);
        helper.assertItemEntityNotPresent(Items.GRASS_BLOCK, WHERE, 3.0);

        Player novice = helper.makeMockPlayer(GameType.SURVIVAL);
        novice.setData(ModAttachments.KNOWLEDGE, PlayerKnowledge.of(novice).withResearch(Alchemia.id("alchemical_manufacture")));
        hot(helper, both).putByHand(new ItemStack(Items.DIRT), novice);
        helper.assertItemEntityPresent(Items.GRASS_BLOCK, WHERE, 3.0);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aSneakingHandEmptiesAJar(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setShiftKeyDown(true);
        JarBlockEntity jar = jar(helper, 8);
        helper.assertValueEqual(signal(helper), 1 + 8 * 14 / 64, "a comparator reads it");
        helper.getBlockState(WHERE).useWithoutItem(helper.getLevel(), player, face(helper, Direction.NORTH));
        helper.assertValueEqual(jar.amount(), 0, "it is poured away");
        helper.assertTrue(jar.holding().isEmpty(), "and forgets what it held");
        helper.assertValueEqual(signal(helper), 0, "the comparator falls silent");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aJarLabelComesOffOnlyAtItsFace(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setShiftKeyDown(true);
        JarBlockEntity jar = jar(helper, 8);
        jar.label(ModAspects.FIRE);

        helper.getBlockState(WHERE).useWithoutItem(helper.getLevel(), player, face(helper, Direction.SOUTH));
        helper.assertValueEqual(jar.amount(), 0, "any other face just empties it");
        helper.assertTrue(jar.label().isPresent(), "and the label stays");
        helper.assertTrue(jar.holding().isPresent(), "still promising the same thing");

        helper.getBlockState(WHERE).useWithoutItem(helper.getLevel(), player,
                face(helper, helper.getBlockState(WHERE).getValue(JarBlock.FACING)));
        helper.assertTrue(jar.label().isEmpty(), "the label's own face takes it off");
        helper.assertItemEntityPresent(ModItems.JAR_LABEL.get(), WHERE, 2.0);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aFullJarLabelsItselfWhateverThePaperSays(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        JarBlockEntity jar = jar(helper, 4);
        ItemStack label = JarBlock.labelFor(ModAspects.WATER);
        player.setItemInHand(InteractionHand.MAIN_HAND, label);
        helper.getBlockState(WHERE).useItemOn(label, helper.getLevel(), player, InteractionHand.MAIN_HAND, face(helper, Direction.NORTH));
        helper.assertTrue(jar.label().isPresent() && jar.label().get().value() == ModAspects.FIRE.value(),
                "it is named for its contents");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void anAlembicOnlyGives(GameTestHelper helper) {
        AlembicBlockEntity alembic = alembic(helper, 4);
        helper.assertFalse(alembic.takesFrom(Direction.NORTH), "no tube fills it");
        helper.assertFalse(alembic.reachableFrom(Direction.DOWN), "nothing reaches it from below");
        helper.assertTrue(alembic.reachableFrom(Direction.NORTH), "the sides are open");
        helper.assertValueEqual(signal(helper), 1 + 4 * 14 / 32, "a comparator reads it");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aLabelledAlembicKeepsToItsWord(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        AlembicBlockEntity alembic = alembic(helper, 0);
        ItemStack label = JarBlock.labelFor(ModAspects.FIRE);
        player.setItemInHand(InteractionHand.MAIN_HAND, label);
        BlockState state = helper.getBlockState(WHERE);

        state.useItemOn(label, helper.getLevel(), player, InteractionHand.MAIN_HAND, face(helper, Direction.UP));
        helper.assertTrue(alembic.filter().isEmpty(), "only the sides take a label");
        state.useItemOn(label, helper.getLevel(), player, InteractionHand.MAIN_HAND, face(helper, Direction.EAST));
        helper.assertTrue(alembic.filter().isPresent(), "a side does");
        helper.assertFalse(alembic.wants(ModAspects.WATER), "it refuses anything else");
        helper.assertTrue(alembic.wants(ModAspects.FIRE), "and takes what it says");
        helper.assertFalse(alembic.reachableFrom(Direction.EAST), "the labelled side is closed");

        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.setShiftKeyDown(true);
        state.useWithoutItem(helper.getLevel(), player, face(helper, Direction.EAST));
        helper.assertTrue(alembic.filter().isEmpty(), "and it peels off");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aJarScoopsAnAlembic(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        AlembicBlockEntity alembic = alembic(helper, 10);
        ItemStack jars = new ItemStack(ModBlocks.JAR.get(), 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, jars);
        jars.getItem().onItemUseFirst(jars, new UseOnContext(helper.getLevel(), player, InteractionHand.MAIN_HAND, jars,
                face(helper, Direction.UP)));
        helper.assertValueEqual(alembic.amount(), 0, "the alembic is emptied");
        helper.assertValueEqual(jars.getCount(), 1, "one jar is used");
        boolean filled = false;
        for (ItemStack carried : player.getInventory().items) {
            AspectList inside = carried.get(ModDataComponents.CONTENTS.get());
            filled |= inside != null && inside.get(ModAspects.FIRE) == 10;
        }
        helper.assertTrue(filled, "and carries what was in it");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void hoppersCanServeAPedestal(GameTestHelper helper) {
        helper.setBlock(WHERE, ModBlocks.ARCANE_PEDESTAL.get());
        ArcanePedestalBlockEntity pedestal = helper.getBlockEntity(WHERE);
        IItemHandler handler = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(WHERE), Direction.UP);
        helper.assertTrue(handler != null, "there is a way in");
        ItemStack left = handler.insertItem(0, new ItemStack(Items.DIAMOND, 3), false);
        helper.assertValueEqual(left.getCount(), 2, "it takes one");
        helper.assertValueEqual(pedestal.held().getCount(), 1, "and holds it");
        helper.assertValueEqual(handler.insertItem(0, new ItemStack(Items.DIAMOND), false).getCount(), 1, "no second");
        helper.assertTrue(handler.extractItem(0, 1, false).is(Items.DIAMOND), "and it can be taken back");
        helper.assertTrue(pedestal.held().isEmpty(), "leaving it bare");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void arcaneStoneCanBaseABeacon(GameTestHelper helper) {
        for (Block block : new Block[] {ModBlocks.ARCANE_STONE.block().get(), ModBlocks.ANCIENT_TILE.get()}) {
            helper.assertTrue(block.defaultBlockState().is(BlockTags.BEACON_BASE_BLOCKS), block + " should do");
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void silkTouchLiftsACrateWhole(GameTestHelper helper) {
        ItemStack pick = new ItemStack(Items.DIAMOND_PICKAXE);
        pick.enchant(helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), 1);
        BlockState crate = ModBlocks.LOOT_CRATE.get().defaultBlockState().setValue(LootBlock.RARITY, 2);
        var drops = crate.getDrops(new LootParams.Builder(helper.getLevel())
                .withParameter(LootContextParams.ORIGIN, helper.absolutePos(WHERE).getCenter())
                .withParameter(LootContextParams.TOOL, pick));
        helper.assertValueEqual(drops.size(), 1, "one crate");
        helper.assertTrue(drops.get(0).is(ModBlocks.LOOT_CRATE.get().asItem()), "and that is what it is");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void shiftClickSendsFuelToTheFuelSlot(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        SimpleContainer smelter = new SimpleContainer(2);
        EssentiaSmelterMenu menu = new EssentiaSmelterMenu(0, player.getInventory(), smelter,
                new SimpleContainerData(EssentiaSmelterBlockEntity.READINGS), ContainerLevelAccess.NULL, BlockPos.ZERO);
        player.getInventory().setItem(9, new ItemStack(Items.COAL, 4));
        menu.quickMoveStack(player, 2);
        helper.assertTrue(smelter.getItem(EssentiaSmelterBlockEntity.SLOT_FUEL).is(Items.COAL), "coal burns");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void shiftClickDoesNotFillTheBenchGrid(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        SimpleContainer bench = new SimpleContainer(10);
        ArcaneWorkbenchMenu menu = new ArcaneWorkbenchMenu(0, player.getInventory(), bench, ContainerLevelAccess.NULL);
        player.getInventory().setItem(9, new ItemStack(Items.STICK, 4));
        menu.quickMoveStack(player, ArcaneWorkbenchMenu.SLOT_WAND + 1);
        helper.assertTrue(bench.getItem(0).isEmpty(), "the grid is laid by hand");
        helper.assertTrue(player.getInventory().getItem(0).is(Items.STICK), "it went to the belt");

        player.getInventory().setItem(10, WandItem.of(ModWandParts.SILVERWOOD, ModWandParts.GOLD));
        menu.quickMoveStack(player, ArcaneWorkbenchMenu.SLOT_WAND + 2);
        helper.assertTrue(menu.wand().getItem() instanceof WandItem, "a wand goes in its place");
        helper.succeed();
    }
}
