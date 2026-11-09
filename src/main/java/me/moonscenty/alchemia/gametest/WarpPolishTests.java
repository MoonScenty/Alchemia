package me.moonscenty.alchemia.gametest;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.item.VoidGear;
import me.moonscenty.alchemia.item.WarpingGear;
import me.moonscenty.alchemia.player.CraftWarp;
import me.moonscenty.alchemia.player.ModAttachments;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.player.WarpData;
import me.moonscenty.alchemia.player.WarpHandler;
import me.moonscenty.alchemia.player.effect.ModEffects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.research.ResearchCompletion;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.EffectCures;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import top.theillusivec4.curios.api.CuriosApi;

@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class WarpPolishTests {
    private static final String TEMPLATE = "empty_32x40x32";

    private static int warp(Player player, WarpData.Kind kind) {
        return WarpData.of(player).of(kind);
    }

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

    private static ServerPlayer stranger(GameTestHelper helper) {
        return FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "warp_test"));
    }

    @GameTest(template = TEMPLATE)
    public static void everyCheckWearsAPointOff(GameTestHelper helper) {
        ServerPlayer player = stranger(helper);
        player.setData(ModAttachments.WARP, new WarpData(0, 0, 5, 0));
        WarpHandler.check(player);
        helper.assertValueEqual(warp(player, WarpData.Kind.TEMPORARY), 4, "no event needed");
        helper.assertValueEqual(WarpData.of(player).counter(), 0, "and the counter stays put");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aMomentOfClarity(GameTestHelper helper) {
        ServerPlayer player = stranger(helper);
        player.setData(ModAttachments.WARP, new WarpData(0, 2, 0, 30));
        WarpHandler.inflict(player, 90, 76);
        helper.assertValueEqual(warp(player, WarpData.Kind.STICKY), 1, "a point of warp lifts");
        helper.assertValueEqual(WarpData.of(player).counter(), 30, "and the counter stays put");
        helper.assertTrue(WarpHandler.outcomeFor(90, 75).effect().isPresent(), "its neighbour is still the hunger");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void milkOnlyCuresTheBlur(GameTestHelper helper) {
        ServerPlayer player = stranger(helper);
        WarpHandler.inflict(player, 60, 14);
        WarpHandler.inflict(player, 60, 34);
        player.removeEffectsCuredBy(EffectCures.MILK);
        helper.assertTrue(player.hasEffect(ModEffects.FLUX_FLU), "the flu stays");
        helper.assertFalse(player.hasEffect(ModEffects.BLURRED_VISION), "the blur goes");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theDeadlyGazeWithersWhatItSees(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Vec3 eye = helper.absoluteVec(new Vec3(16.5, 2.0, 8.5));
        player.moveTo(eye.x, eye.y, eye.z, 0.0F, 0.0F);
        player.addEffect(new MobEffectInstance(ModEffects.DEADLY_GAZE, 200, 0));
        Pig seen = helper.spawn(EntityType.PIG, new BlockPos(16, 2, 13));
        Pig behind = helper.spawn(EntityType.PIG, new BlockPos(16, 2, 4));
        WarpHandler.gaze(player);
        helper.assertTrue(seen.hasEffect(MobEffects.WITHER), "what is looked at withers");
        helper.assertTrue(seen.getTarget() == player, "and turns on the looker");
        helper.assertFalse(behind.hasEffect(MobEffects.WITHER), "what is behind does not");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void onlyFleshEasesTheHunger(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.addEffect(new MobEffectInstance(ModEffects.UNNATURAL_HUNGER, 5000, 2));
        WarpHandler.ate(player, new ItemStack(Items.BREAD));
        helper.assertValueEqual(player.getEffect(ModEffects.UNNATURAL_HUNGER).getAmplifier(), 2, "bread does nothing");
        WarpHandler.ate(player, new ItemStack(Items.ROTTEN_FLESH));
        helper.assertValueEqual(player.getEffect(ModEffects.UNNATURAL_HUNGER).getAmplifier(), 1, "a level off");
        helper.assertValueEqual(player.getEffect(ModEffects.UNNATURAL_HUNGER).getDuration(), 4400, "and half a minute");
        WarpHandler.ate(player, new ItemStack(ModItems.ZOMBIE_BRAIN.get()));
        WarpHandler.ate(player, new ItemStack(Items.ROTTEN_FLESH));
        helper.assertFalse(player.hasEffect(ModEffects.UNNATURAL_HUNGER), "the last bite ends it");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void alchediarrheaLeavesGoo(GameTestHelper helper) {
        Pig pig = helper.spawn(EntityType.PIG, new BlockPos(8, 2, 8));
        pig.setNoAi(true);
        for (int i = 0; i < 400 && pig.level().isEmptyBlock(pig.blockPosition()); i++) {
            ModEffects.ALCHEDIARRHEA.value().applyEffectTick(pig, 0);
        }
        helper.assertTrue(pig.level().getBlockState(pig.blockPosition()).is(ModBlocks.FLUX_GOO.get()), "goo at its feet");
        helper.assertTrue(ModEffects.ALCHEDIARRHEA.value().shouldApplyEffectTickThisTick(20, 0), "once a second");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void sunScornedHealsInTheDark(GameTestHelper helper) {
        helper.assertTrue(ModEffects.SUN_SCORNED.value().shouldApplyEffectTickThisTick(40, 0), "every two seconds");
        helper.assertFalse(ModEffects.SUN_SCORNED.value().shouldApplyEffectTickThisTick(20, 0), "not every one");
        helper.succeed();
    }

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
