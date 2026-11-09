package me.moonscenty.alchemia.gametest;

import java.util.ArrayList;
import java.util.List;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.enchantment.InfusionEnchantment;
import me.moonscenty.alchemia.enchantment.InfusionEnchantments;
import me.moonscenty.alchemia.item.FortressArmourEvents;
import me.moonscenty.alchemia.item.LootBagItem;
import me.moonscenty.alchemia.item.PrimalArrowItem;
import me.moonscenty.alchemia.item.ThaumostaticHarnessItem;
import me.moonscenty.alchemia.player.effect.ModEffects;
import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class GearRuleTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final BlockPos HERE = new BlockPos(8, 2, 8);

    private static Zombie zombie(GameTestHelper helper, BlockPos at) {
        Zombie zombie = EntityType.ZOMBIE.create(helper.getLevel());
        zombie.setData(me.moonscenty.alchemia.player.ModAttachments.CHAMPION,
                me.moonscenty.alchemia.entity.Champions.ORDINARY);
        var where = helper.absoluteVec(at.getBottomCenter());
        zombie.moveTo(where.x, where.y, where.z, 0.0F, 0.0F);
        zombie.setNoAi(true);
        helper.getLevel().addFreshEntity(zombie);
        return zombie;
    }

    private static DamageSource magic(GameTestHelper helper) {
        return helper.getLevel().damageSources().magic();
    }

    private static void wearFortress(net.minecraft.world.entity.LivingEntity wearer) {
        wearer.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.FORTRESS.get("fortress_helm").get()));
        wearer.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.FORTRESS.get("fortress_chest").get()));
        wearer.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.FORTRESS.get("fortress_legs").get()));
    }

    private static void breakWith(GameTestHelper helper, Player player, BlockPos at) {
        BlockPos where = helper.absolutePos(at);
        NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(helper.getLevel(), where, helper.getLevel().getBlockState(where),
                player));
    }

    @GameTest(template = TEMPLATE)
    public static void fortressPlateTurnsMagicAside(GameTestHelper helper) {
        Zombie plated = zombie(helper, HERE);
        wearFortress(plated);
        plated.hurt(magic(helper), 10.0F);
        float lost = plated.getMaxHealth() - plated.getHealth();
        helper.assertTrue(lost > 0.0F && lost < 10.0F, "the plate should take some of the magic, lost " + lost);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aVoidRobeTurnsMagicButNotFire(GameTestHelper helper) {
        Zombie robed = zombie(helper, HERE);
        robed.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.ROBES.get("void_robe_chest").get()));
        helper.assertTrue(FortressArmourEvents.throughBypass(robed, magic(helper)) > 0.0F, "magic is turned");
        helper.assertValueEqual(FortressArmourEvents.throughBypass(robed, helper.getLevel().damageSources().onFire()), 0.0F,
                "burning is not");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void anArcIsAHalfBlowOnTheNeighbour(GameTestHelper helper) {
        Zombie struck = zombie(helper, HERE);
        Zombie beside = zombie(helper, HERE.east());
        float whole = beside.getHealth();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND,
                InfusionEnchantments.raised(new ItemStack(Items.WOODEN_PICKAXE), InfusionEnchantment.ARCING));
        CommonHooks.onPlayerAttackTarget(player, struck);
        float lost = whole - beside.getHealth();
        float full = (float) player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        helper.assertTrue(lost > 0.0F && lost <= full * 0.5F + 0.01F, "only half a blow, lost " + lost);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void arcingAndEssenceFitAnyTool(GameTestHelper helper) {
        helper.assertTrue(InfusionEnchantment.ARCING.fits(new ItemStack(Items.IRON_PICKAXE)), "arcing on a pickaxe");
        helper.assertTrue(InfusionEnchantment.ESSENCE.fits(new ItemStack(Items.IRON_SHOVEL)), "essence on a shovel");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void burrowingWalksTheWholeSeamAndSparesTheTool(GameTestHelper helper) {
        for (int east = 0; east < 5; east++) {
            helper.setBlock(HERE.east(east), Blocks.IRON_ORE);
        }
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack pick = InfusionEnchantments.raised(new ItemStack(Items.DIAMOND_PICKAXE), InfusionEnchantment.BURROWING);
        player.setItemInHand(InteractionHand.MAIN_HAND, pick);
        breakWith(helper, player, HERE);
        helper.assertBlockPresent(Blocks.IRON_ORE, HERE.east(3));
        helper.assertBlockPresent(Blocks.AIR, HERE.east(4));
        helper.assertValueEqual(player.getMainHandItem().getDamageValue(), 0, "burrowing costs the tool nothing");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void burrowingKeepsTheToolsSilkTouch(GameTestHelper helper) {
        helper.setBlock(HERE, Blocks.IRON_ORE);
        helper.setBlock(HERE.east(), Blocks.IRON_ORE);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack pick = InfusionEnchantments.raised(new ItemStack(Items.DIAMOND_PICKAXE), InfusionEnchantment.BURROWING);
        pick.enchant(helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.SILK_TOUCH), 1);
        player.setItemInHand(InteractionHand.MAIN_HAND, pick);
        breakWith(helper, player, HERE);
        helper.assertTrue(helper.getLevel().getEntitiesOfClass(ItemEntity.class, helper.getBounds().inflate(2.0)).stream()
                .anyMatch(item -> item.getItem().is(Items.IRON_ORE)), "the ore should come away whole");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aCollectorSwordGathersWhatTheMobDrops(GameTestHelper helper) {
        Zombie dead = zombie(helper, HERE);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND,
                InfusionEnchantments.raised(new ItemStack(Items.IRON_SWORD), InfusionEnchantment.COLLECTOR));
        List<ItemEntity> drops = new ArrayList<>();
        drops.add(new ItemEntity(helper.getLevel(), dead.getX(), dead.getY(), dead.getZ(), new ItemStack(Items.ROTTEN_FLESH)));
        LivingDropsEvent event = new LivingDropsEvent(dead, helper.getLevel().damageSources().playerAttack(player), drops,
                true);
        NeoForge.EVENT_BUS.post(event);
        helper.assertTrue(event.getDrops().isEmpty(), "nothing left on the ground");
        helper.assertTrue(player.getInventory().contains(new ItemStack(Items.ROTTEN_FLESH)), "and it is in the pack");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void primalArrowsAreNotSavedByInfinity(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack bow = new ItemStack(Items.BOW);
        bow.enchant(helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.INFINITY), 1);
        var arrows = new ItemStack(ModItems.PRIMAL_ARROWS.values().iterator().next().get());
        for (int i = 0; i < 200; i++) {
            helper.assertFalse(((PrimalArrowItem) arrows.getItem()).isInfinite(arrows, bow, player), "never saved");
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aHoveringWearerDigsAsIfGrounded(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setOnGround(false);
        var stone = Blocks.STONE.defaultBlockState();
        ItemStack harness = new ItemStack(ModItems.THAUMOSTATIC_HARNESS.get());
        ThaumostaticHarnessItem.setFuel(harness, 100);
        player.setItemSlot(EquipmentSlot.CHEST, harness);
        float idle = player.getDigSpeed(stone, helper.absolutePos(HERE));
        harness.set(ModDataComponents.HOVERING.get(), true);
        float hovering = player.getDigSpeed(stone, helper.absolutePos(HERE));
        helper.assertTrue(Math.abs(hovering - idle * 5.0F) < 0.001F, "five times, got " + idle + " then " + hovering);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void heldTaintEatsAwayAtItsCarrier(GameTestHelper helper) {
        Cow cow = helper.spawnWithNoFreeWill(EntityType.COW, HERE);
        ItemStack goo = new ItemStack(ModItems.TAINTED_GOO.get(), 64);
        for (int i = 0; i < 5000 && !cow.hasEffect(ModEffects.TAINT_POISON); i++) {
            goo.inventoryTick(helper.getLevel(), cow, 0, false);
        }
        helper.assertTrue(cow.hasEffect(ModEffects.TAINT_POISON), "the cow should have been tainted");
        helper.assertValueEqual(goo.getCount(), 64, "only a player's stack dissolves");

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        for (int i = 0; i < 5000 && goo.getCount() == 64; i++) {
            player.removeEffect(ModEffects.TAINT_POISON);
            goo.inventoryTick(helper.getLevel(), player, 0, false);
        }
        helper.assertValueEqual(goo.getCount(), 63, "and it loses one");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void richBagsSometimesHoldGear(GameTestHelper helper) {
        boolean gear = false;
        for (int i = 0; i < 4000 && !gear; i++) {
            ItemStack thing = LootBagItem.loot(helper.getLevel(), 2, helper.getLevel().random);
            gear = thing.getItem() instanceof ArmorItem || thing.is(Items.IRON_AXE);
        }
        helper.assertTrue(gear, "one roll in twenty should be gear");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void commonBagsNeverHoldGear(GameTestHelper helper) {
        for (int i = 0; i < 2000; i++) {
            helper.assertFalse(LootBagItem.loot(helper.getLevel(), 0, helper.getLevel().random).getItem() instanceof ArmorItem,
                    "no gear in a common bag");
        }
        helper.succeed();
    }
}
