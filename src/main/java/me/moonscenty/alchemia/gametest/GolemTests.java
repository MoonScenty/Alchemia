package me.moonscenty.alchemia.gametest;

import java.util.List;
import java.util.Set;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.GolemPressBlock;
import me.moonscenty.alchemia.block.entity.GolemPressBlockEntity;
import me.moonscenty.alchemia.entity.OwnedDeaths;
import me.moonscenty.alchemia.golem.Golem;
import me.moonscenty.alchemia.golem.GolemDesign;
import me.moonscenty.alchemia.golem.GolemParts;
import me.moonscenty.alchemia.golem.GolemTrait;
import me.moonscenty.alchemia.item.GolemItem;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.player.ModAttachments;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModWandParts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Golems, step twelve: what they are pressed from, what that makes them, and the press itself. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class GolemTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final BlockPos AT = new BlockPos(5, 2, 5);

    private static Golem golem(GameTestHelper helper, Player owner, GolemDesign design) {
        helper.setBlock(AT.below(), Blocks.STONE);
        Golem golem = ModEntities.GOLEM.get().create(helper.getLevel());
        BlockPos where = helper.absolutePos(AT);
        golem.moveTo(where.getX() + 0.5, where.getY(), where.getZ() + 0.5, 0.0F, 0.0F);
        golem.placed(owner.getUUID(), design, 0, where);
        helper.getLevel().addFreshEntity(golem);
        return golem;
    }

    /** A brass golem with a smart head and armour on is only smart: the armour and the weight each cancel. */
    @GameTest(template = TEMPLATE)
    public static void pairedTraitsCancel(GameTestHelper helper) {
        GolemDesign design = GolemDesign.BASIC.withMaterial(GolemParts.Material.BRASS)
                .withHead(GolemParts.Head.SMART).withAddon(GolemParts.Addon.ARMORED);
        helper.assertValueEqual(design.traits(), Set.of(GolemTrait.SMART), "only smart is left");
        helper.succeed();
    }

    /** A design packs into one number and back out the same, rank and all. */
    @GameTest(template = TEMPLATE)
    public static void aDesignPacksAndUnpacks(GameTestHelper helper) {
        GolemDesign design = new GolemDesign(GolemParts.Material.VOID, GolemParts.Head.SMART_SCOUT,
                GolemParts.Arms.DARTS, GolemParts.Legs.FLYER, GolemParts.Addon.HAULER, 7);
        helper.assertValueEqual(GolemDesign.fromLong(design.toLong()), design, "the same golem");
        helper.succeed();
    }

    /** The plainest golem takes three planks, two gears and a clockwork mind, and eight measures of machina. */
    @GameTest(template = TEMPLATE)
    public static void thePlainestGolemTakesWhatItShould(GameTestHelper helper) {
        List<ItemStack> needs = GolemDesign.BASIC.components();
        helper.assertValueEqual(needs.size(), 3, "three kinds of thing");
        helper.assertTrue(needs.get(0).is(ModBlocks.GREATWOOD.planks().get().asItem())
                && needs.get(0).getCount() == 3, "three planks");
        helper.assertTrue(needs.get(1).is(ModItems.BRASS_GEAR.get()) && needs.get(1).getCount() == 2, "two gears");
        helper.assertTrue(needs.get(2).is(ModItems.CLOCKWORK_MIND.get()), "and a mind");
        helper.assertValueEqual(GolemDesign.BASIC.cost(), 8, "eight measures, two of them for being light");
        helper.succeed();
    }

    /** What it is made of says how much it takes and how well it is armoured. */
    @GameTest(template = TEMPLATE)
    public static void aGolemIsWhatItIsMadeOf(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        Golem wood = golem(helper, owner, GolemDesign.BASIC);
        helper.assertValueEqual((int) wood.getMaxHealth(), 16, "a wooden golem has sixteen");
        helper.assertValueEqual(wood.getArmorValue(), 2, "and two armour");
        Golem iron = golem(helper, owner, GolemDesign.BASIC.withMaterial(GolemParts.Material.IRON));
        helper.assertValueEqual((int) iron.getMaxHealth(), 30, "an iron one thirty");
        helper.assertValueEqual(iron.getArmorValue(), 8, "and eight armour");
        helper.succeed();
    }

    /** Fire does nothing to an iron golem, and burns a wooden one. */
    @GameTest(template = TEMPLATE)
    public static void aFireproofGolemDoesNotBurn(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        Golem iron = golem(helper, owner, GolemDesign.BASIC.withMaterial(GolemParts.Material.IRON));
        Golem wood = golem(helper, owner, GolemDesign.BASIC);
        iron.hurt(helper.getLevel().damageSources().onFire(), 4.0F);
        wood.hurt(helper.getLevel().damageSources().onFire(), 4.0F);
        helper.assertValueEqual(iron.getHealth(), iron.getMaxHealth(), "the iron golem is untouched");
        helper.assertTrue(wood.getHealth() < wood.getMaxHealth(), "the wooden one is not");
        helper.succeed();
    }

    /** A wand folds a golem back into an item that remembers what it was. */
    @GameTest(template = TEMPLATE)
    public static void aWandTakesAGolemBackUp(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        GolemDesign design = GolemDesign.BASIC.withMaterial(GolemParts.Material.CLAY);
        Golem golem = golem(helper, owner, design);
        owner.setItemInHand(InteractionHand.MAIN_HAND, WandItem.of(ModWandParts.SILVERWOOD, ModWandParts.GOLD));
        golem.mobInteract(owner, InteractionHand.MAIN_HAND);
        helper.assertTrue(golem.isRemoved(), "the golem is gone");
        ItemEntity dropped = helper.getLevel().getEntitiesOfClass(ItemEntity.class, helper.getBounds().inflate(2))
                .stream().findFirst().orElse(null);
        helper.assertTrue(dropped != null && dropped.getItem().is(ModItems.GOLEM.get())
                && GolemItem.design(dropped.getItem()).equals(design), "and a clay golem lies where it stood");
        helper.succeed();
    }

    /** Somebody else's golem does nothing for them. */
    @GameTest(template = TEMPLATE)
    public static void aGolemAnswersOnlyItsOwner(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        Player stranger = helper.makeMockPlayer(GameType.SURVIVAL);
        Golem golem = golem(helper, owner, GolemDesign.BASIC);
        stranger.setItemInHand(InteractionHand.MAIN_HAND, WandItem.of(ModWandParts.SILVERWOOD, ModWandParts.GOLD));
        golem.mobInteract(stranger, InteractionHand.MAIN_HAND);
        helper.assertFalse(golem.isRemoved(), "it is still there");
        helper.succeed();
    }

    /** Once its owner knows how, a bell sends it after them, and again keeps it where it is. */
    @GameTest(template = TEMPLATE)
    public static void aBellSendsAGolemAfterItsOwner(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        Golem golem = golem(helper, owner, GolemDesign.BASIC);
        owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.GOLEM_BELL.get()));
        golem.mobInteract(owner, InteractionHand.MAIN_HAND);
        helper.assertFalse(golem.following(), "not until its owner has learned how");
        owner.setData(ModAttachments.KNOWLEDGE,
                PlayerKnowledge.of(owner).withResearch(Alchemia.id("golem_direct")));
        golem.mobInteract(owner, InteractionHand.MAIN_HAND);
        helper.assertTrue(golem.following(), "it follows");
        golem.mobInteract(owner, InteractionHand.MAIN_HAND);
        helper.assertFalse(golem.following(), "and stays");
        helper.succeed();
    }

    /** A hauler carries two stacks; any other golem one. */
    @GameTest(template = TEMPLATE)
    public static void aHaulerCarriesTwoStacks(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        Golem plain = golem(helper, owner, GolemDesign.BASIC);
        Golem hauler = golem(helper, owner, GolemDesign.BASIC.withAddon(GolemParts.Addon.HAULER));
        helper.assertTrue(plain.hold(new ItemStack(Items.COBBLESTONE, 64)).isEmpty(), "one stack goes in");
        helper.assertValueEqual(plain.hold(new ItemStack(Items.DIRT, 64)).getCount(), 64, "a second does not");
        hauler.hold(new ItemStack(Items.COBBLESTONE, 64));
        helper.assertTrue(hauler.hold(new ItemStack(Items.DIRT, 64)).isEmpty(), "a hauler takes a second");
        ItemStack put = hauler.drop(ItemStack.EMPTY);
        helper.assertTrue(put.is(Items.COBBLESTONE) && hauler.getMainHandItem().is(Items.DIRT),
                "and what is left moves to the first hand");
        helper.succeed();
    }

    /** A smart golem comes on a rank with enough learned, and is the stronger for it. */
    @GameTest(template = TEMPLATE)
    public static void aSmartGolemComesOnARank(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        Golem golem = golem(helper, owner, GolemDesign.BASIC.withHead(GolemParts.Head.SMART));
        float before = golem.getMaxHealth();
        golem.addRankXp(GolemDesign.RANK_XP);
        helper.assertValueEqual(golem.design().rank(), 1, "it is rank one");
        helper.assertValueEqual(golem.getMaxHealth(), before + 1, "and has one more health");
        Golem dull = golem(helper, owner, GolemDesign.BASIC);
        dull.addRankXp(GolemDesign.RANK_XP);
        helper.assertValueEqual(dull.design().rank(), 0, "a clockwork one does not learn");
        helper.succeed();
    }

    /** Lays out the blocks of a press round a piston here, its table to the north. */
    private static void layOut(GameTestHelper helper, BlockPos heart) {
        helper.setBlock(heart.below(), Blocks.STONE);
        helper.setBlock(heart, Blocks.PISTON.defaultBlockState().setValue(PistonBaseBlock.FACING, Direction.UP));
        helper.setBlock(heart.above(), Blocks.IRON_BARS);
        helper.setBlock(heart.north(), ModBlocks.STONE_TABLE.get());
        helper.setBlock(heart.north().east(), Blocks.ANVIL);
        helper.setBlock(heart.east(), Blocks.CAULDRON);
    }

    /** Laid out right, the blocks make a press facing its table; break one and the rest go back. */
    @GameTest(template = TEMPLATE)
    public static void aPressIsMadeAndUnmade(GameTestHelper helper) {
        BlockPos heart = new BlockPos(5, 2, 5);
        layOut(helper, heart);
        helper.assertValueEqual(GolemPressBlock.fits(helper.getLevel(), helper.absolutePos(heart)), Direction.NORTH,
                "it fits, facing the table");
        GolemPressBlock.build(helper.getLevel(), helper.absolutePos(heart), Direction.NORTH);
        helper.assertBlockPresent(ModBlocks.GOLEM_PRESS.get(), heart);
        helper.assertBlockPresent(ModBlocks.GOLEM_PRESS_PART.get(), heart.north().east());
        helper.setBlock(heart.north().east(), Blocks.AIR);
        helper.assertBlockPresent(Blocks.IRON_BARS, heart.above());
        helper.assertBlockPresent(ModBlocks.STONE_TABLE.get(), heart.north());
        helper.assertBlockPresent(Blocks.CAULDRON, heart.east());
        helper.assertBlockPresent(Blocks.PISTON, heart);
        helper.succeed();
    }

    /** Without the clockwork mind learned, a wand does nothing to the blocks. */
    @GameTest(template = TEMPLATE)
    public static void aPressNeedsTheClockworkMind(GameTestHelper helper) {
        BlockPos heart = new BlockPos(5, 2, 5);
        layOut(helper, heart);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wand = WandItem.brimming(WandItem.of(ModWandParts.SILVERWOOD, ModWandParts.GOLD));
        helper.assertFalse(GolemPressBlock.form(helper.getLevel(), helper.absolutePos(heart), player, wand),
                "nothing happens");
        helper.assertBlockPresent(Blocks.PISTON, heart);
        helper.succeed();
    }

    /** A press takes what the golem needs from its maker, presses it a measure at a time, and drops it out. */
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void aPressPressesAGolem(GameTestHelper helper) {
        BlockPos heart = new BlockPos(5, 2, 5);
        layOut(helper, heart);
        GolemPressBlock.build(helper.getLevel(), helper.absolutePos(heart), Direction.NORTH);
        Player maker = helper.makeMockPlayer(GameType.SURVIVAL);
        maker.getInventory().add(new ItemStack(ModBlocks.GREATWOOD.planks().get(), 3));
        maker.getInventory().add(new ItemStack(ModItems.BRASS_GEAR.get(), 2));
        maker.getInventory().add(new ItemStack(ModItems.CLOCKWORK_MIND.get()));
        GolemPressBlockEntity press = (GolemPressBlockEntity) helper.getBlockEntity(heart);
        helper.assertTrue(press.start(GolemDesign.BASIC, maker), "it starts");
        helper.assertTrue(maker.getInventory().isEmpty(), "and has taken everything");
        helper.onEachTick(() -> press.accept(ModAspects.MECHANISM));
        helper.succeedWhen(() -> helper.assertTrue(press.getItem(0).is(ModItems.GOLEM.get()), "a golem came out"));
    }

    /** A golem with a name tells its owner how it died; one without a name does not. */
    @GameTest(template = TEMPLATE)
    public static void aNamedGolemTellsItsOwnerOfItsDeath(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        Golem golem = golem(helper, owner, GolemDesign.BASIC);
        helper.assertFalse(OwnedDeaths.tells(golem), "a golem with no name tells nobody");
        golem.setCustomName(net.minecraft.network.chat.Component.literal("Clank"));
        helper.assertTrue(OwnedDeaths.tells(golem), "a named golem tells");
        golem.hurt(helper.getLevel().damageSources().generic(), 1000.0F);
        helper.assertTrue(golem.getCombatTracker().getDeathMessage().getString().contains("Clank"),
                "the message names it");
        helper.succeed();
    }
}
