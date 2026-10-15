package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.item.VoidGear;
import me.moonscenty.alchemia.item.WarpingGear;
import me.moonscenty.alchemia.player.CraftWarp;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.player.WarpData;
import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.research.ResearchCompletion;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import top.theillusivec4.curios.api.CuriosApi;

/** Step 14D-2: the warp of learning, of making, and of what is carried. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class WarpPolishTests {
    private static final String TEMPLATE = "empty_32x40x32";

    private static int warp(Player player, WarpData.Kind kind) {
        return WarpData.of(player).of(kind);
    }

    /** Warp from learning is split, the larger half for good; a single point is all for good. */
    @GameTest(template = TEMPLATE)
    public static void learningWarpIsSplit(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ResearchCompletion.settle(player, 3);
        helper.assertValueEqual(warp(player, WarpData.Kind.PERMANENT), 2, "two of three for good");
        helper.assertValueEqual(warp(player, WarpData.Kind.STICKY), 1, "and one that sticks");
        ResearchCompletion.settle(player, 1);
        helper.assertValueEqual(warp(player, WarpData.Kind.PERMANENT), 3, "a single point is for good");
        helper.assertValueEqual(warp(player, WarpData.Kind.STICKY), 1, "and none sticks");
        helper.succeed();
    }

    /** Learning the oculus leaves six, once; learning it again is nothing. */
    @GameTest(template = TEMPLATE)
    public static void researchCarriesItsWarp(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertTrue(ResearchCompletion.complete(player, Alchemia.id("oculus")), "learnt");
        helper.assertTrue(PlayerKnowledge.of(player).hasResearch(Alchemia.id("oculus")), "and known");
        helper.assertValueEqual(warp(player, WarpData.Kind.PERMANENT), 3, "three for good");
        helper.assertValueEqual(warp(player, WarpData.Kind.STICKY), 3, "three that stick");
        helper.assertFalse(ResearchCompletion.complete(player, Alchemia.id("oculus")), "not twice");
        helper.assertValueEqual(warp(player, WarpData.Kind.PERMANENT), 3, "and no more warp for it");
        helper.succeed();
    }

    /** Void metal carries warp, a robe more; the lodestone none; baubles count; the altar's warp counts. */
    @GameTest(template = TEMPLATE)
    public static void carriedWarpCountsVoidAndBaubles(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.SINISTER_STONE.get()));
        helper.assertValueEqual(WarpingGear.carried(player), 0, "the lodestone carries none");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.METAL_TOOLS.get("void_sword").get()));
        helper.assertValueEqual(WarpingGear.carried(player), 1, "a void sword carries one");
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.ROBES.get("void_robe_chest").get()));
        helper.assertValueEqual(WarpingGear.carried(player), 3, "a void robe two more");
        ItemStack ring = new ItemStack(ModItems.RUNIC_RING.get());
        ring.set(ModDataComponents.WARP.get(), 2);
        CuriosApi.getCuriosInventory(player).orElseThrow().getStacksHandler("ring").orElseThrow().getStacks()
                .setStackInSlot(0, ring);
        helper.assertValueEqual(WarpingGear.carried(player), 5, "and a ring the altar left warp in, two");
        helper.succeed();
    }

    /** Void gear mends itself wherever it is carried. */
    @GameTest(template = TEMPLATE)
    public static void voidGearMends(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack pick = new ItemStack(ModItems.METAL_TOOLS.get("void_pickaxe").get());
        pick.setDamageValue(10);
        ItemStack iron = new ItemStack(Items.IRON_PICKAXE);
        iron.setDamageValue(10);
        player.getInventory().setItem(20, pick);
        player.getInventory().setItem(21, iron);
        VoidGear.mendCarried(player);
        helper.assertValueEqual(pick.getDamageValue(), 9, "the void pick mends a point");
        helper.assertValueEqual(iron.getDamageValue(), 10, "iron does not");
        helper.succeed();
    }

    /** Making something the original marked leaves a point of sticking warp; making anything else, none. */
    @GameTest(template = TEMPLATE)
    public static void makingSomeThingsWarps(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        CraftWarp.made(player, new ItemStack(Items.STICK));
        helper.assertValueEqual(warp(player, WarpData.Kind.STICKY), 0, "a stick is nothing");
        CraftWarp.made(player, new ItemStack(ModItems.SINISTER_STONE.get()));
        CraftWarp.made(player, new ItemStack(ModItems.FOCI.get("hellbat").get()));
        helper.assertValueEqual(warp(player, WarpData.Kind.STICKY), 2, "the lodestone and the Nine Hells, one each");
        helper.succeed();
    }
}
