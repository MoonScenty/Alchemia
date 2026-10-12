package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.golem.Golem;
import me.moonscenty.alchemia.golem.GolemDesign;
import me.moonscenty.alchemia.golem.GolemParts;
import me.moonscenty.alchemia.golem.seal.Matching;
import me.moonscenty.alchemia.golem.seal.Provisioning;
import me.moonscenty.alchemia.golem.seal.Seal;
import me.moonscenty.alchemia.golem.seal.SealEntity;
import me.moonscenty.alchemia.golem.seal.SealPos;
import me.moonscenty.alchemia.golem.seal.SealTypes;
import me.moonscenty.alchemia.golem.seal.Seals;
import me.moonscenty.alchemia.golem.task.Task;
import me.moonscenty.alchemia.menu.SealMenu;
import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Golems, step twelve continued: seals, the tasks they give out, and golems doing them. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class SealTests {
    private static final String TEMPLATE = "empty_32x40x32";

    /** A stone floor nine by nine to walk on, its middle at this height. */
    private static void floor(GameTestHelper helper, int y) {
        for (int x = 1; x <= 9; x++) {
            for (int z = 1; z <= 9; z++) {
                helper.setBlock(new BlockPos(x, y, z), Blocks.STONE);
            }
        }
    }

    private static Golem golem(GameTestHelper helper, Player owner, GolemDesign design, BlockPos at) {
        Golem golem = ModEntities.GOLEM.get().create(helper.getLevel());
        BlockPos where = helper.absolutePos(at);
        golem.moveTo(where.getX() + 0.5, where.getY(), where.getZ() + 0.5, 0.0F, 0.0F);
        golem.placed(owner.getUUID(), design, 0, where);
        helper.getLevel().addFreshEntity(golem);
        return golem;
    }

    private static SealEntity stick(GameTestHelper helper, String kind, BlockPos on, Direction face, Player owner) {
        Seal seal = SealTypes.make(kind);
        BlockPos pos = helper.absolutePos(on);
        Seals.add(helper.getLevel(), pos, face, seal, owner);
        return Seals.get(helper.getLevel(), new SealPos(pos, face));
    }

    /** A seal sticks where it can, and falls off as its item once what it was on is gone. */
    @GameTest(template = TEMPLATE, timeoutTicks = 60)
    public static void aSealFallsOffWhatIsGone(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos on = new BlockPos(5, 2, 5);
        helper.setBlock(on, Blocks.STONE);
        SealEntity seal = stick(helper, "pickup", on, Direction.UP, owner);
        helper.assertTrue(seal != null && owner.getUUID().equals(seal.owner()), "it is up, and its owner's");
        helper.assertFalse(Seals.add(helper.getLevel(), helper.absolutePos(on), Direction.UP,
                SealTypes.make("fill"), owner), "and nothing else goes on the same face");
        helper.setBlock(on, Blocks.AIR);
        helper.succeedWhen(() -> {
            helper.assertTrue(Seals.get(helper.getLevel(), seal.pos()) == null, "it has fallen off");
            helper.assertTrue(helper.getLevel().getEntitiesOfClass(ItemEntity.class, helper.getBounds().inflate(2))
                    .stream().anyMatch(item -> item.getItem().is(ModItems.SEALS.get("pickup").get())),
                    "and lies there as a seal");
        });
    }

    /** A golem goes and picks up what lies in front of a collecting seal. */
    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void aGolemCollectsForASeal(GameTestHelper helper) {
        floor(helper, 1);
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        Golem golem = golem(helper, owner, GolemDesign.BASIC, new BlockPos(2, 2, 2));
        stick(helper, "pickup", new BlockPos(6, 1, 6), Direction.UP, owner);
        BlockPos there = helper.absolutePos(new BlockPos(6, 2, 6));
        ItemEntity lying = new ItemEntity(helper.getLevel(), there.getX() + 0.5, there.getY(), there.getZ() + 0.5,
                new ItemStack(Items.COBBLESTONE, 5));
        lying.setNoPickUpDelay();
        helper.getLevel().addFreshEntity(lying);
        helper.succeedWhen(() -> helper.assertTrue(golem.getMainHandItem().is(Items.COBBLESTONE),
                "the golem has the cobblestone"));
    }

    /** A golem carrying something puts it into a chest with a storing seal on it. */
    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void aGolemStoresForASeal(GameTestHelper helper) {
        floor(helper, 1);
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos chest = new BlockPos(6, 2, 6);
        helper.setBlock(chest, Blocks.CHEST);
        Golem golem = golem(helper, owner, GolemDesign.BASIC, new BlockPos(2, 2, 2));
        golem.hold(new ItemStack(Items.COBBLESTONE, 8));
        stick(helper, "fill", chest, Direction.UP, owner);
        helper.succeedWhen(() -> {
            ChestBlockEntity box = helper.getBlockEntity(chest);
            helper.assertTrue(box.countItem(Items.COBBLESTONE) == 8, "the chest has it");
            helper.assertTrue(golem.getMainHandItem().isEmpty(), "and the golem does not");
        });
    }

    /** A golem takes what is in a chest with an emptying seal on it. */
    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void aGolemEmptiesForASeal(GameTestHelper helper) {
        floor(helper, 1);
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos chest = new BlockPos(6, 2, 6);
        helper.setBlock(chest, Blocks.CHEST);
        ChestBlockEntity box = helper.getBlockEntity(chest);
        box.setItem(0, new ItemStack(Items.COBBLESTONE, 12));
        Golem golem = golem(helper, owner, GolemDesign.BASIC, new BlockPos(2, 2, 2));
        stick(helper, "empty", chest, Direction.UP, owner);
        helper.succeedWhen(() -> helper.assertTrue(golem.getMainHandItem().is(Items.COBBLESTONE)
                && golem.getMainHandItem().getCount() == 12 && box.isEmpty(), "the golem has emptied the chest"));
    }

    /** A smart golem fetches out of a providing chest what another seal has asked for. */
    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void aGolemFetchesWhatASealAskedFor(GameTestHelper helper) {
        floor(helper, 1);
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos store = new BlockPos(6, 2, 6);
        helper.setBlock(store, Blocks.CHEST);
        ChestBlockEntity box = helper.getBlockEntity(store);
        box.setItem(0, new ItemStack(Items.TORCH, 20));
        helper.setBlock(new BlockPos(2, 2, 8), Blocks.STONE);
        SealEntity asking = stick(helper, "pickup", new BlockPos(2, 2, 8), Direction.UP, owner);
        Golem golem = golem(helper, owner, GolemDesign.BASIC.withHead(GolemParts.Head.SMART),
                new BlockPos(2, 2, 2));
        stick(helper, "provider", store, Direction.UP, owner);
        Provisioning.request(helper.getLevel(), asking, new ItemStack(Items.TORCH));
        helper.succeedWhen(() -> helper.assertTrue(golem.getMainHandItem().is(Items.TORCH),
                "the golem has fetched the torches"));
    }

    /** A seal set for golems of one colour gives its work to no golem of another. */
    @GameTest(template = TEMPLATE)
    public static void aColouredSealWantsGolemsOfItsColour(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.setBlock(new BlockPos(5, 2, 5), Blocks.STONE);
        SealEntity seal = stick(helper, "fill", new BlockPos(5, 2, 5), Direction.UP, owner);
        Golem golem = golem(helper, owner, GolemDesign.BASIC, new BlockPos(2, 2, 2));
        golem.hold(new ItemStack(Items.COBBLESTONE));
        Task task = new Task(seal.pos(), seal.pos().pos());
        helper.assertTrue(task.canBeDoneBy(golem), "any golem may, to begin with");
        seal.setColour(5);
        golem.setColour(3);
        helper.assertFalse(task.canBeDoneBy(golem), "not one of another colour");
        golem.setColour(5);
        helper.assertTrue(task.canBeDoneBy(golem), "one of its colour may");
        helper.succeed();
    }

    /** A locked seal is worked only by its owner's golems, and a seal wanting smart golems only by smart ones. */
    @GameTest(template = TEMPLATE)
    public static void aSealChoosesItsGolems(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        Player stranger = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.setBlock(new BlockPos(5, 2, 5), Blocks.STONE);
        SealEntity seal = stick(helper, "pickup", new BlockPos(5, 2, 5), Direction.UP, owner);
        Golem theirs = golem(helper, stranger, GolemDesign.BASIC, new BlockPos(2, 2, 2));
        helper.assertTrue(theirs.mayWork(seal), "an unlocked seal takes anybody's golem");
        seal.setLocked(true);
        helper.assertFalse(theirs.mayWork(seal), "a locked one does not");
        Golem clumsy = golem(helper, owner, GolemDesign.BASIC.withArms(GolemParts.Arms.BREAKERS),
                new BlockPos(3, 2, 2));
        helper.assertFalse(clumsy.mayWork(seal), "nor a clumsy one, to pick things up");
        helper.setBlock(new BlockPos(7, 2, 5), Blocks.STONE);
        SealEntity advanced = stick(helper, "pickup_advanced", new BlockPos(7, 2, 5), Direction.UP, owner);
        Golem plain = golem(helper, owner, GolemDesign.BASIC, new BlockPos(4, 2, 2));
        helper.assertFalse(plain.mayWork(advanced), "an advanced seal wants a smart golem");
        helper.succeed();
    }

    /** The panel's buttons change the seal, and only its owner may lock it. */
    @GameTest(template = TEMPLATE)
    public static void aSealsPanelSetsItUp(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        Player stranger = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.setBlock(new BlockPos(5, 2, 5), Blocks.STONE);
        SealEntity seal = stick(helper, "pickup", new BlockPos(5, 2, 5), Direction.UP, owner);
        SealMenu panel = new SealMenu(0, owner.getInventory(), seal, helper.getLevel());
        panel.clickMenuButton(owner, 2);
        panel.clickMenuButton(owner, SealMenu.PRIORITY_UP);
        panel.clickMenuButton(owner, SealMenu.PRIORITY_UP);
        helper.assertValueEqual(seal.priority(), 2, "priority up two");
        panel.clickMenuButton(owner, SealMenu.COLOUR_UP);
        helper.assertValueEqual(seal.colour(), 1, "for white golems");
        SealMenu theirs = new SealMenu(1, stranger.getInventory(), seal, helper.getLevel());
        theirs.clickMenuButton(stranger, 2);
        theirs.clickMenuButton(stranger, SealMenu.LOCK);
        helper.assertFalse(seal.locked(), "a stranger cannot lock it");
        panel.clickMenuButton(owner, SealMenu.LOCK);
        helper.assertTrue(seal.locked(), "its owner can");
        panel.clickMenuButton(owner, 0);
        panel.clickMenuButton(owner, SealMenu.AREA + 3);
        helper.assertValueEqual(seal.area().getX(), 4, "its area widens");
        helper.succeed();
    }

    /** A filter square takes a picture of what is in hand, and a counting seal counts it. */
    @GameTest(template = TEMPLATE)
    public static void aFilterTakesAPicture(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.setBlock(new BlockPos(5, 2, 5), Blocks.CHEST);
        SealEntity seal = stick(helper, "fill", new BlockPos(5, 2, 5), Direction.UP, owner);
        SealMenu panel = new SealMenu(0, owner.getInventory(), seal, helper.getLevel());
        panel.setCarried(new ItemStack(Items.COBBLESTONE, 16));
        panel.clicked(0, 0, ClickType.PICKUP, owner);
        helper.assertTrue(seal.seal().filter().get(0).is(Items.COBBLESTONE), "the filter shows cobblestone");
        helper.assertValueEqual(panel.getCarried().getCount(), 16, "and the hand still holds all of it");
        panel.clickMenuButton(owner, SealMenu.WHITELIST);
        panel.setCarried(ItemStack.EMPTY);
        panel.clicked(0, 0, ClickType.PICKUP, owner);
        helper.assertValueEqual(seal.seal().limit(0), 2, "a whitelist counts, one more for a click");
        helper.succeed();
    }

    /** Things are alike by item, and by damage and data only when asked; by common tag or mod when asked. */
    @GameTest(template = TEMPLATE)
    public static void thingsAreToldApartAsAsked(GameTestHelper helper) {
        ItemStack worn = new ItemStack(Items.IRON_PICKAXE);
        worn.setDamageValue(10);
        ItemStack fresh = new ItemStack(Items.IRON_PICKAXE);
        helper.assertFalse(Matching.Rule.STRICT.alike(worn, fresh), "strictly, worn is not fresh");
        helper.assertTrue(new Matching.Rule(false, true, false, false).alike(worn, fresh), "ignoring damage it is");
        helper.assertTrue(new Matching.Rule(true, true, true, false).alike(new ItemStack(Items.IRON_INGOT),
                new ItemStack(Items.GOLD_INGOT)), "two ingots share a common tag");
        helper.assertFalse(Matching.Rule.STRICT.alike(new ItemStack(Items.IRON_INGOT),
                new ItemStack(Items.GOLD_INGOT)), "but are not the same thing");
        helper.assertTrue(new Matching.Rule(true, true, false, true).alike(new ItemStack(Items.STONE),
                new ItemStack(Items.DIRT)), "anything from the same mod");
        helper.assertFalse(new Matching.Rule(true, true, false, true).alike(new ItemStack(Items.STONE),
                new ItemStack(ModItems.TALLOW.get())), "but not from another");
        helper.succeed();
    }
}
