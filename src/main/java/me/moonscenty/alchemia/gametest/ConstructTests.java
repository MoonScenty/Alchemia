package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aura.node.AuraNode;
import me.moonscenty.alchemia.entity.AdvancedAutomatedCrossbow;
import me.moonscenty.alchemia.entity.Autocaster;
import me.moonscenty.alchemia.entity.AutomatedCrossbow;
import me.moonscenty.alchemia.entity.Champions;
import me.moonscenty.alchemia.entity.NodeMagnet;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.player.ModAttachments;
import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModWandParts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The constructs on tripods: the automated crossbows, which shoot what they are told to and reload from a dispenser
 * under them, and the node magnet, which drags a node towards itself.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class ConstructTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final BlockPos FLOOR = new BlockPos(8, 1, 8);

    /** Set down from its item by a player standing by, as a player would. */
    private static <T extends net.minecraft.world.entity.Entity> T setDown(GameTestHelper helper, Player owner,
            ItemStack placer, BlockPos on, Class<T> kind) {
        helper.setBlock(on, Blocks.STONE);
        owner.setItemInHand(InteractionHand.MAIN_HAND, placer);
        BlockHitResult struck = new BlockHitResult(Vec3.ZERO, Direction.UP, helper.absolutePos(on), false);
        placer.getItem().useOn(new UseOnContext(owner, InteractionHand.MAIN_HAND, struck));
        return helper.getLevel().getEntitiesOfClass(kind, helper.getBounds().inflate(2.0)).stream().findFirst()
                .orElseThrow(() -> new AssertionError("nothing was set down"));
    }

    /** Loaded with arrows, a crossbow shoots a monster it can see. */
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void aLoadedCrossbowShootsAMonster(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        AutomatedCrossbow turret = setDown(helper, owner, new ItemStack(ModItems.AUTOMATED_CROSSBOW.get()), FLOOR,
                AutomatedCrossbow.class);
        turret.setItem(0, new ItemStack(Items.ARROW, 16));
        helper.setBlock(FLOOR.east(6), Blocks.STONE);
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, FLOOR.east(6).above());
        zombie.setData(ModAttachments.CHAMPION, Champions.ORDINARY);
        zombie.setNoAi(true);

        helper.succeedWhen(() -> {
            helper.assertTrue(turret.getItem(0).getCount() < 16, "it should have spent an arrow");
            helper.assertTrue(zombie.getHealth() < zombie.getMaxHealth() || !zombie.isAlive(),
                    "on the zombie");
        });
    }

    /** Stood on a dispenser that faces up, a dry crossbow takes a whole stack of arrows out of it. */
    @GameTest(template = TEMPLATE)
    public static void aDryCrossbowReloadsFromTheDispenserUnderIt(GameTestHelper helper) {
        helper.setBlock(FLOOR, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.UP));
        DispenserBlockEntity dispenser = (DispenserBlockEntity) helper.getBlockEntity(FLOOR);
        dispenser.setItem(4, new ItemStack(Items.ARROW, 20));
        AutomatedCrossbow turret = AutomatedCrossbow.basic(helper.getLevel(), helper.absolutePos(FLOOR.above()));
        helper.getLevel().addFreshEntity(turret);

        helper.succeedWhen(() -> {
            helper.assertValueEqual(turret.getItem(0).getCount(), 20, "the crossbow should have all twenty");
            helper.assertTrue(dispenser.getItem(4).isEmpty(), "and the dispenser none");
        });
    }

    /** Its owner takes it down with a wand, and gets back the crossbow and whatever it was loaded with. */
    @GameTest(template = TEMPLATE)
    public static void aWandTakesACrossbowDown(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        AutomatedCrossbow turret = setDown(helper, owner, new ItemStack(ModItems.AUTOMATED_CROSSBOW.get()), FLOOR,
                AutomatedCrossbow.class);
        turret.setItem(0, new ItemStack(Items.ARROW, 5));

        owner.setItemInHand(InteractionHand.MAIN_HAND, WandItem.of(ModWandParts.GREATWOOD, ModWandParts.GOLD));
        turret.mobInteract(owner, InteractionHand.MAIN_HAND);
        helper.assertTrue(turret.isRemoved(), "the crossbow should be gone");
        var dropped = helper.getLevel().getEntitiesOfClass(ItemEntity.class, helper.getBounds().inflate(2.0));
        helper.assertTrue(dropped.stream().anyMatch(item -> item.getItem().is(ModItems.AUTOMATED_CROSSBOW.get())),
                "and handed back as an item");
        helper.assertTrue(dropped.stream().anyMatch(item -> item.getItem().is(Items.ARROW)),
                "with its arrows");
        helper.succeed();
    }

    /** The advanced one minds monsters until it is told to mind animals too, and its owner only if told to. */
    @GameTest(template = TEMPLATE)
    public static void theAdvancedCrossbowShootsOnlyWhatItIsToldTo(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        AdvancedAutomatedCrossbow turret = setDown(helper, owner,
                new ItemStack(ModItems.ADVANCED_AUTOMATED_CROSSBOW.get()), FLOOR, AdvancedAutomatedCrossbow.class);
        Cow cow = helper.spawn(EntityType.COW, FLOOR.east(3).above());
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, FLOOR.west(3).above());

        helper.assertTrue(turret.worthShooting(zombie), "a monster, from the start");
        helper.assertFalse(turret.worthShooting(cow), "but not a cow");
        turret.mind(Autocaster.ANIMALS, true);
        helper.assertTrue(turret.worthShooting(cow), "until it is told to mind animals");
        turret.mind(Autocaster.PLAYERS, true);
        helper.assertFalse(turret.worthShooting(owner), "never its owner unless told to mind friends");
        turret.mind(Autocaster.FRIENDS, true);
        helper.assertTrue(turret.worthShooting(owner), "and then even them");
        helper.succeed();
    }

    /** A charged magnet turns on the nearest node that nothing holds still and drags it closer. */
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void aMagnetDrawsANodeIn(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        NodeMagnet magnet = setDown(helper, owner, new ItemStack(ModItems.NODE_MAGNET.get()), FLOOR,
                NodeMagnet.class);
        magnet.setCharge(100);
        AuraNode node = new AuraNode(helper.getLevel());
        Vec3 start = Vec3.atCenterOf(helper.absolutePos(FLOOR.east(8).above(3)));
        node.moveTo(start.x, start.y, start.z, 0.0F, 0.0F);
        node.setSize(5);
        helper.getLevel().addFreshEntity(node);
        double apart = magnet.distanceTo(node);

        helper.succeedWhen(() -> {
            helper.assertTrue(magnet.node() == node, "it should have taken hold of the node");
            helper.assertTrue(magnet.distanceTo(node) < apart - 0.25,
                    "and drawn it in, from " + apart + " to " + magnet.distanceTo(node));
            helper.assertTrue(magnet.charge() < 100, "spending charge to do it");
        });
    }

    /** One the server never set down from an item belongs to nobody, and nobody's turret is anybody's to open. */
    @GameTest(template = TEMPLATE)
    public static void aStrayCrossbowIsAnybodys(GameTestHelper helper) {
        Player passer = helper.makeMockPlayer(GameType.SURVIVAL);
        AutomatedCrossbow turret = AutomatedCrossbow.at(ModEntities.AUTOMATED_CROSSBOW.get(), helper.getLevel(),
                helper.absolutePos(FLOOR.above()));
        helper.assertTrue(turret.owns(passer), "anybody may work one with no owner");
        turret.setOwner(java.util.UUID.randomUUID());
        helper.assertFalse(turret.owns(passer), "but not one with an owner who is somebody else");
        helper.succeed();
    }
}
