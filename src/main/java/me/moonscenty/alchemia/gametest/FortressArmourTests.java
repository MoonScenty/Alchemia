package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.item.FortressArmourEvents;
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
}
