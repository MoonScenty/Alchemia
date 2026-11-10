package me.moonscenty.alchemia.gametest;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.item.AlchemometerItem;
import me.moonscenty.alchemia.item.ThaumostaticHarnessItem;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class HudRuleTests {
    private static final String TEMPLATE = "empty_32x40x32";

    @GameTest(template = TEMPLATE)
    public static void theAlchemometerStepsThroughAllThenEachAspect(GameTestHelper helper) {
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "gauge"));
        ItemStack gauge = new ItemStack(ModItems.ALCHEMOMETER.get());
        helper.assertValueEqual(AlchemometerItem.shown(gauge), 0, "it starts on the whole aura");
        for (int step = 1; step <= 7; step++) {
            AlchemometerItem.cycle(player, gauge);
            helper.assertValueEqual(AlchemometerItem.shown(gauge), step, "then one aspect at a time");
        }
        AlchemometerItem.cycle(player, gauge);
        helper.assertValueEqual(AlchemometerItem.shown(gauge), 0, "and back round to all of them");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void chargeLevelsReadAsTheOriginal(GameTestHelper helper) {
        ItemStack harness = new ItemStack(ModItems.THAUMOSTATIC_HARNESS.get());
        ThaumostaticHarnessItem.setFuel(harness, ThaumostaticHarnessItem.FUEL_MAX / 4);
        helper.assertTrue(Math.abs(((ThaumostaticHarnessItem) harness.getItem()).chargeLevel(harness) - 0.25F) < 0.001F,
                "a quarter tank");
        ItemStack wand = new ItemStack(ModItems.WAND.get());
        helper.assertTrue(((me.moonscenty.alchemia.item.WandItem) wand.getItem()).chargeLevel(wand) < 0.0F,
                "wands show no meter");
        helper.succeed();
    }
}
