package me.moonscenty.alchemia.gametest;

import java.util.List;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.crafting.ArcaneWandRecipe;
import me.moonscenty.alchemia.item.VisHolder;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModWandParts;
import me.moonscenty.alchemia.wand.Casting;
import me.moonscenty.alchemia.wand.FocusUpgrade;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Staves: wands on a staff core, which hold more, hit harder, work their focus cheaper, will not lie on a workbench,
 * and in the primal staff's case make every primal and lend the focus a step of potency.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class StaffTests {
    private static final String TEMPLATE = "empty_32x40x32";

    private static ItemStack staff() {
        return WandItem.of(ModWandParts.GREATWOOD_STAFF, ModWandParts.GOLD);
    }

    /** A staff core laid between two caps makes a staff, named as one and holding what the core holds. */
    @GameTest(template = TEMPLATE)
    public static void aStaffCoreMakesAStaff(GameTestHelper helper) {
        ItemStack cap = new ItemStack(ModItems.WAND_CAPS.get("gold").get());
        ItemStack core = new ItemStack(ModItems.WAND_RODS.get("greatwood_staff").get());
        CraftingInput bench = CraftingInput.of(3, 3, List.of(ItemStack.EMPTY, ItemStack.EMPTY, cap,
                ItemStack.EMPTY, core, ItemStack.EMPTY, cap, ItemStack.EMPTY, ItemStack.EMPTY));
        ItemStack made = ArcaneWandRecipe.INSTANCE.assemble(bench, helper.getLevel().registryAccess());

        helper.assertTrue(WandItem.isStaff(made), "what comes out should be a staff");
        helper.assertValueEqual(WandItem.capacity(made), 375, "holding what a greatwood staff holds");
        helper.assertValueEqual(((TranslatableContents) made.getHoverName().getContents()).getKey(),
                "item.alchemia.staff.named", "and called a staff");
        helper.succeed();
    }

    /** A staff is a club; a wand is not. */
    @GameTest(template = TEMPLATE)
    public static void aStaffHitsHarder(GameTestHelper helper) {
        double[] added = new double[1];
        staff().getAttributeModifiers().forEach(net.minecraft.world.entity.EquipmentSlot.MAINHAND,
                (attribute, modifier) -> {
                    if (attribute.is(Attributes.ATTACK_DAMAGE)) {
                        added[0] += modifier.amount();
                    }
                });
        helper.assertValueEqual(added[0], 6.0, "a staff adds six to a blow");
        helper.assertTrue(WandItem.of(ModWandParts.GREATWOOD, ModWandParts.GOLD).getAttributeModifiers()
                .modifiers().isEmpty(), "a wand adds nothing");
        helper.succeed();
    }

    /** What a staff's focus costs comes a tenth cheaper than the same working through a wand. */
    @GameTest(template = TEMPLATE)
    public static void aStaffWorksItsFocusCheaper(GameTestHelper helper) {
        Player bearer = helper.makeMockPlayer(GameType.SURVIVAL);
        AspectList price = AspectList.of(ModAspects.FIRE, 10);
        ItemStack wand = WandItem.brimming(WandItem.of(ModWandParts.GREATWOOD, ModWandParts.GOLD));
        ItemStack staff = WandItem.brimming(staff());
        WandItem item = (WandItem) ModItems.WAND.get();
        item.take(wand, price, bearer, VisHolder.Spending.WORKING);
        item.take(staff, price, bearer, VisHolder.Spending.WORKING);

        helper.assertValueEqual(250 - item.held(wand, ModAspects.FIRE), 10, "the wand pays ten");
        helper.assertValueEqual(375 - item.held(staff, ModAspects.FIRE), 9, "the staff pays nine");

        ItemStack fresh = WandItem.brimming(staff());
        item.take(fresh, price, bearer, VisHolder.Spending.CRAFTING);
        helper.assertValueEqual(375 - item.held(fresh, ModAspects.FIRE), 10, "but full price at a bench");
        helper.succeed();
    }

    /** A wooden table becomes a workbench under a wand, and not under a staff. */
    @GameTest(template = TEMPLATE)
    public static void aStaffWillNotMakeAWorkbench(GameTestHelper helper) {
        Player bearer = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos at = new BlockPos(2, 2, 2);
        helper.setBlock(at, ModBlocks.WOODEN_TABLE.get());
        bearer.setItemInHand(InteractionHand.MAIN_HAND, staff());
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(at)), Direction.UP,
                helper.absolutePos(at), false);
        helper.getBlockState(at).useItemOn(bearer.getMainHandItem(), helper.getLevel(), bearer,
                InteractionHand.MAIN_HAND, hit);
        helper.assertBlockPresent(ModBlocks.WOODEN_TABLE.get(), at);
        helper.succeed();
    }

    /** The primal staff lends whatever focus is on it a step of potency, and makes every primal. */
    @GameTest(template = TEMPLATE)
    public static void thePrimalStaffIsPotentAndMakesEveryPrimal(GameTestHelper helper) {
        ItemStack primal = WandItem.of(ModWandParts.PRIMAL_STAFF, ModWandParts.GOLD);
        WandItem.setFocus(primal, new ItemStack(ModItems.FOCI.get("shock").get()));
        ItemStack plain = WandItem.of(ModWandParts.SILVERWOOD_STAFF, ModWandParts.GOLD);
        WandItem.setFocus(plain, new ItemStack(ModItems.FOCI.get("shock").get()));
        helper.assertValueEqual(Casting.worked(primal, FocusUpgrade.POTENCY), 1, "one step of potency for free");
        helper.assertValueEqual(Casting.worked(plain, FocusUpgrade.POTENCY), 0, "which no other staff gives");

        Player bearer = helper.makeMockPlayer(GameType.SURVIVAL);
        WandItem item = (WandItem) ModItems.WAND.get();
        for (int i = 0; i < 60; i++) {
            item.inventoryTick(primal, helper.getLevel(), bearer, 0, false);
        }
        int kinds = 0;
        for (var aspect : ModAspects.primals()) {
            if (item.held(primal, aspect) > 0) {
                kinds++;
            }
        }
        helper.assertTrue(kinds > 1, "it should make more than one primal, made " + kinds);
        helper.succeed();
    }
}
