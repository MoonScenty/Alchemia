package me.moonscenty.alchemia.gametest;

import java.util.List;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.crafting.InfusionInput;
import me.moonscenty.alchemia.crafting.InfusionRecipe;
import me.moonscenty.alchemia.crafting.ModRecipes;
import me.moonscenty.alchemia.item.VisDiscount;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.item.bauble.RunicGear;
import me.moonscenty.alchemia.item.bauble.RunicShield;
import me.moonscenty.alchemia.item.bauble.VerdantHeartItem;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModWandParts;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

/** Baubles: worn in Curios' slots, the runic shield they hold up, and what the others do. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class BaubleTests {
    private static final String TEMPLATE = "empty_32x40x32";

    private static void wear(Player player, String slot, ItemStack bauble) {
        CuriosApi.getCuriosInventory(player).orElseThrow().getStacksHandler(slot).orElseThrow().getStacks()
                .setStackInSlot(0, bauble);
    }

    /** A player has an amulet's slot, two rings' and a belt's. */
    @GameTest(template = TEMPLATE)
    public static void aPlayerHasBaubleSlots(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        var curios = CuriosApi.getCuriosInventory(player).orElseThrow();
        helper.assertTrue(curios.getStacksHandler("necklace").isPresent(), "an amulet's slot");
        helper.assertTrue(curios.getStacksHandler("ring").map(ring -> ring.getSlots() >= 2).orElse(false),
                "two rings'");
        helper.assertTrue(curios.getStacksHandler("belt").isPresent(), "and a belt's");
        helper.succeed();
    }

    /** What is worn holds up a shield, and the shield fills a point at a time out of air and earth. */
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void runicGearHoldsUpAShield(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.moveTo(helper.absoluteVec(new BlockPos(4, 2, 4).getBottomCenter()));
        BlockPos here = player.blockPosition();
        AuraHandler.add(helper.getLevel(), here, ModAspects.AIR, 500);
        AuraHandler.add(helper.getLevel(), here, ModAspects.EARTH, 500);
        wear(player, "ring", new ItemStack(ModItems.RUNIC_RING.get()));
        ItemStack chest = new ItemStack(ModItems.ROBES.get("cloth_chest").get());
        chest.set(ModDataComponents.RUNIC_HARDENING.get(), 2);
        player.setItemSlot(EquipmentSlot.CHEST, chest);
        RunicShield.markDirty(player);
        RunicShield.tick(player);
        helper.assertValueEqual(RunicShield.most(player), 7, "a ring of five and a robe hardened twice");
        helper.assertTrue(player.getAttributeValue(Attributes.MAX_ABSORPTION) >= 7, "and hearts that hold it");
        helper.succeedWhen(() -> {
            RunicShield.tick(player);
            helper.assertTrue(player.getAbsorptionAmount() >= 1.0F, "a point of shield came back");
        });
    }

    /** An apprentice's ring is five off its own primal and nothing off the rest. */
    @GameTest(template = TEMPLATE)
    public static void anApprenticesRingKnowsItsPrimal(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        wear(player, "ring", new ItemStack(ModItems.APPRENTICE_RINGS.get("fire").get()));
        helper.assertValueEqual(VisDiscount.worn(player, ModAspects.FIRE), 5, "five off fire");
        helper.assertValueEqual(VisDiscount.worn(player, ModAspects.WATER), 0, "nothing off water");
        helper.assertValueEqual(VisDiscount.worn(player), 0, "nothing off in general");
        helper.succeed();
    }

    /** Augmentation wants one salis more for every point there already is, and hardens by one. */
    @GameTest(template = TEMPLATE)
    public static void augmentationHardensRunicGear(GameTestHelper helper) {
        InfusionRecipe recipe = helper.getLevel().getRecipeManager().byKey(Alchemia.id("runic_augmentation"))
                .map(holder -> (InfusionRecipe) holder.value()).orElseThrow();
        ItemStack ring = new ItemStack(ModItems.RUNIC_RING.get());
        List<ItemStack> six = new java.util.ArrayList<>(List.of(new ItemStack(Items.DIAMOND)));
        for (int i = 0; i < 6; i++) {
            six.add(new ItemStack(ModItems.SALIS_MUNDUS.get()));
        }
        helper.assertTrue(recipe.matches(new InfusionInput(ring, six), helper.getLevel()),
                "a ring of five wants six salis");
        helper.assertFalse(recipe.matches(new InfusionInput(ring, six.subList(0, 3)), helper.getLevel()),
                "and not two");
        helper.assertValueEqual(recipe.essentiaFor(ring).get(ModAspects.ENERGY), 1024, "and doubles its essentia");
        ItemStack hardened = recipe.assemble(new InfusionInput(ring, six), helper.getLevel().registryAccess());
        helper.assertValueEqual(RunicGear.charge(hardened), 6, "it comes out holding six");
        helper.assertTrue(helper.getLevel().getRecipeManager().getAllRecipesFor(ModRecipes.INFUSION.get()).size() > 0,
                "among the altar's works");
        helper.succeed();
    }

    /** The verdant heart lifts poison, and spends ten for it. */
    @GameTest(template = TEMPLATE)
    public static void theVerdantHeartLiftsPoison(GameTestHelper helper) {
        Pig pig = helper.spawn(EntityType.PIG, new BlockPos(4, 2, 4));
        pig.addEffect(new MobEffectInstance(MobEffects.POISON, 200));
        ItemStack heart = new ItemStack(ModItems.VERDANT_HEART.get());
        ((VerdantHeartItem) heart.getItem()).curioTick(new SlotContext("ring", pig, 0, false, true), heart);
        helper.assertFalse(pig.hasEffect(MobEffects.POISON), "the poison is gone");
        helper.assertValueEqual(heart.getDamageValue(), 10, "for ten of its life");
        helper.succeed();
    }

    /** The amulet of vis charges the wand in the hotbar out of the aura. */
    @GameTest(template = TEMPLATE)
    public static void theAmuletChargesAWand(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.moveTo(helper.absoluteVec(new BlockPos(4, 2, 4).getBottomCenter()));
        for (var primal : ModAspects.primals()) {
            AuraHandler.add(helper.getLevel(), player.blockPosition(), primal, 500);
        }
        ItemStack wand = WandItem.of(ModWandParts.GREATWOOD, ModWandParts.GOLD);
        player.getInventory().setItem(0, wand);
        ItemStack amulet = new ItemStack(ModItems.VIS_AMULET.get());
        ModItems.VIS_AMULET.get().curioTick(new SlotContext("necklace", player, 0, false, true), amulet);
        int held = 0;
        for (var primal : ModAspects.primals()) {
            held += ((WandItem) wand.getItem()).held(wand, primal);
        }
        helper.assertTrue(held > 0, "the wand took some in");
        helper.succeed();
    }
}
