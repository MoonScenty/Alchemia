package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.item.Hover;
import me.moonscenty.alchemia.item.ThaumostaticHarnessItem;
import me.moonscenty.alchemia.item.VisDiscount;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The thaumostatic harness: switched on it lets its wearer fly and burns fuel; it charges from air and fire. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class HarnessTests {
    private static final String TEMPLATE = "empty_32x40x32";

    @GameTest(template = TEMPLATE)
    public static void harnessFliesOnFuel(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack harness = new ItemStack(ModItems.THAUMOSTATIC_HARNESS.get());
        player.setItemSlot(EquipmentSlot.CHEST, harness);
        Hover.toggle(player);
        helper.assertFalse(ThaumostaticHarnessItem.hovering(harness), "nothing to fly on, it stays off");
        ThaumostaticHarnessItem.setFuel(harness, 100);
        Hover.toggle(player);
        helper.assertTrue(ThaumostaticHarnessItem.hovering(harness), "switched on");
        Hover.tick(new PlayerTickEvent.Post(player));
        helper.assertTrue(player.getAbilities().mayfly, "the wearer may fly");
        helper.assertValueEqual(ThaumostaticHarnessItem.fuel(harness), 99, "a tick of fuel burnt");
        Hover.toggle(player);
        Hover.tick(new PlayerTickEvent.Post(player));
        helper.assertFalse(player.getAbilities().mayfly, "switched off, flight is taken back");
        helper.assertValueEqual(VisDiscount.worn(player, ModAspects.AIR), 3, "three off air");
        helper.assertValueEqual(VisDiscount.worn(player, ModAspects.FIRE), 1, "one off anything else");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void harnessChargesFromAirAndFire(GameTestHelper helper) {
        BlockPos at = helper.absolutePos(new BlockPos(4, 2, 4));
        AuraHandler.add(helper.getLevel(), at, ModAspects.AIR, 200);
        AuraHandler.add(helper.getLevel(), at, ModAspects.FIRE, 200);
        ItemStack harness = new ItemStack(ModItems.THAUMOSTATIC_HARNESS.get());
        helper.assertTrue(ThaumostaticHarnessItem.recharge(harness, helper.getLevel(), at, 5), "it takes a charge");
        helper.assertValueEqual(ThaumostaticHarnessItem.fuel(harness), 5 * ThaumostaticHarnessItem.PER_CHARGE,
                "a second for each point of air and fire");
        helper.succeed();
    }
}
