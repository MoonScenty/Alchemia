package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import java.util.List;

import me.moonscenty.alchemia.crafting.InfusionInput;
import me.moonscenty.alchemia.crafting.InfusionRecipe;
import me.moonscenty.alchemia.item.FortressArmorItem;
import me.moonscenty.alchemia.item.FortressArmourEvents;
import me.moonscenty.alchemia.item.HelmFitting;
import net.minecraft.world.item.Items;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * What fortress armour does once it is on somebody.
 * <p>
 * The arithmetic is worth pinning down because it is the one armour in the mod whose worth is not written on its
 * material: wearing more of it is worth more than the pieces are, and what the blow is made of changes how much
 * of it gets through.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class FortressArmourTests {
    private static final String TEMPLATE = "empty_32x40x32";

    /** Counting what is on, which everything else is worked out from. */
    @GameTest(template = TEMPLATE)
    public static void aSuitIsCountedPieceByPiece(GameTestHelper helper) {
        Player wearer = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertValueEqual(FortressArmourEvents.worn(wearer), 0, "nothing on");

        wearer.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.FORTRESS.get("fortress_chest").get()));
        helper.assertValueEqual(FortressArmourEvents.worn(wearer), 1, "one piece on");

        wearer.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.FORTRESS.get("fortress_helm").get()));
        wearer.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.FORTRESS.get("fortress_legs").get()));
        helper.assertValueEqual(FortressArmourEvents.worn(wearer), 3, "the whole suit, which is three");

        // somebody else's helmet is somebody else's helmet
        wearer.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.METAL_ARMOUR.get("void_helmet").get()));
        helper.assertValueEqual(FortressArmourEvents.worn(wearer), 2, "a void helm counts for nothing here");
        helper.succeed();
    }

    /**
     * Plate turns fire and blasts best, a blade next, and a working worst.
     * <p>
     * The leaning is the point, not the exact fractions: a steel shell has an answer to being hit and no answer
     * at all to a curse. Getting the order wrong would make the suit best at the thing it should be worst at.
     */
    @GameTest(template = TEMPLATE)
    public static void plateTurnsFireBestAndWorkingsWorst(GameTestHelper helper) {
        var types = helper.getLevel().registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE);
        DamageSource blow = new DamageSource(types.getHolderOrThrow(DamageTypes.GENERIC));
        DamageSource burning = new DamageSource(types.getHolderOrThrow(DamageTypes.IN_FIRE));
        DamageSource working = new DamageSource(types.getHolderOrThrow(DamageTypes.MAGIC));

        helper.assertTrue(FortressArmourEvents.turns(burning) > FortressArmourEvents.turns(blow),
                "fire is turned better than a blow");
        helper.assertTrue(FortressArmourEvents.turns(blow) > FortressArmourEvents.turns(working),
                "and a blow better than a working");

        // and a whole suit of it never turns everything, whatever the blow. Four rather than three, because
        // the count is written for four slots and a fourth piece may yet be made
        helper.assertTrue(4 * FortressArmourEvents.turns(burning) < 1.0F,
                "a whole suit is not immune even to what it turns best");
        helper.succeed();
    }
    /**
     * Goggles worked into a helm: the helm that went in is the helm that comes back.
     * <p>
     * That is the whole reason this is a fitting on the item rather than a new item. A fresh helm handed over
     * instead would take the wearer's enchantments and their half-worn plate with it.
     */
    @GameTest(template = TEMPLATE)
    public static void gogglesAreWorkedIntoTheHelmThatWentIn(GameTestHelper helper) {
        ItemStack helm = new ItemStack(ModItems.FORTRESS.get("fortress_helm").get());
        helm.setDamageValue(120);
        helper.assertTrue(FortressArmorItem.fitting(helm).isEmpty(), "a plain helm wears nothing");

        ItemStack fitted = FortressArmorItem.wearing(helm, HelmFitting.GOGGLES);
        helper.assertTrue(FortressArmorItem.fitting(fitted).orElse(null) == HelmFitting.GOGGLES,
                "and a fitted one wears the goggles");
        helper.assertValueEqual(fitted.getDamageValue(), 120, "with every dent it came in with");
        helper.assertTrue(FortressArmorItem.fitting(helm).isEmpty(), "the helm handed in is left alone");
        helper.succeed();
    }

    /**
     * An altar will not sell the same working twice.
     * <p>
     * A helm wears one thing. Without this the goggles could be worked in over and over, and when the masks
     * arrive one could be laid on top of another with only the last of them showing.
     */
    @GameTest(template = TEMPLATE)
    public static void aHelmTakesOnlyOneThing(GameTestHelper helper) {
        var found = helper.getLevel().getRecipeManager().byKey(Alchemia.id("helm_goggles")).orElse(null);
        helper.assertTrue(found != null, "the altar knows how to set lenses into a visor");

        ItemStack plain = new ItemStack(ModItems.FORTRESS.get("fortress_helm").get());
        ItemStack fitted = FortressArmorItem.wearing(plain, HelmFitting.GOGGLES);
        List<ItemStack> ring = List.of(new ItemStack(Items.SLIME_BALL),
                new ItemStack(ModItems.GOGGLES.get()));

        helper.assertTrue(((InfusionRecipe) found.value()).matches(new InfusionInput(plain, ring),
                helper.getLevel()), "a plain helm is taken");
        helper.assertFalse(((InfusionRecipe) found.value()).matches(new InfusionInput(fitted, ring),
                helper.getLevel()), "one already wearing them is not");

        // and a helm wearing a mask is no more free than one wearing the goggles
        ItemStack masked = FortressArmorItem.wearing(plain, HelmFitting.GRINNING_DEVIL);
        helper.assertFalse(((InfusionRecipe) found.value()).matches(new InfusionInput(masked, ring),
                helper.getLevel()), "nor is one already wearing a mask");
        helper.succeed();
    }

    /**
     * A mask is worth a little in a fight, and the goggles are worth nothing in one.
     * <p>
     * The original gave the masks a sliver of the set bonus and the goggles none, and the sliver was small
     * enough that nobody built a mask for it. Both halves of that are worth keeping: a mask that was worth
     * nothing would be a lie, and one worth a lot would make the other two faces a mistake.
     */
    @GameTest(template = TEMPLATE)
    public static void aMaskIsWorthASliverAndTheGogglesNothing(GameTestHelper helper) {
        var types = helper.getLevel().registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE);
        DamageSource blow = new DamageSource(types.getHolderOrThrow(DamageTypes.GENERIC));
        Player wearer = helper.makeMockPlayer(GameType.SURVIVAL);

        ItemStack helm = new ItemStack(ModItems.FORTRESS.get("fortress_helm").get());
        wearer.setItemSlot(EquipmentSlot.HEAD, helm);
        float bare = FortressArmourEvents.turnedAside(wearer, blow);

        wearer.setItemSlot(EquipmentSlot.HEAD, FortressArmorItem.wearing(helm, HelmFitting.GOGGLES));
        helper.assertTrue(FortressArmourEvents.turnedAside(wearer, blow) == bare,
                "the goggles turn nothing aside");

        wearer.setItemSlot(EquipmentSlot.HEAD, FortressArmorItem.wearing(helm, HelmFitting.ANGRY_GHOST));
        float masked = FortressArmourEvents.turnedAside(wearer, blow);
        helper.assertTrue(masked > bare, "a mask turns a little more");
        helper.assertTrue(masked < bare * 1.2F, "but only a little");
        helper.succeed();
    }
}
