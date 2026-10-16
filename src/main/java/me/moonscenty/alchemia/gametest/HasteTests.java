package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.enchantment.ModEnchantments;
import me.moonscenty.alchemia.entity.PechTrades;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Haste: three levels, on boots and the harness and nothing else, and a mage pech sells the book. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class HasteTests {
    private static final String TEMPLATE = "empty_32x40x32";

    @GameTest(template = TEMPLATE)
    public static void hasteGoesOnBootsAndTheHarness(GameTestHelper helper) {
        var haste = helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(ModEnchantments.HASTE);
        helper.assertValueEqual(haste.value().getMaxLevel(), 3, "three levels");
        helper.assertTrue(haste.value().canEnchant(new ItemStack(Items.LEATHER_BOOTS)), "on boots");
        helper.assertTrue(haste.value().canEnchant(new ItemStack(ModItems.THAUMOSTATIC_HARNESS.get())),
                "on the harness");
        helper.assertFalse(haste.value().canEnchant(new ItemStack(Items.IRON_CHESTPLATE)), "not on a chestplate");

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack boots = new ItemStack(Items.IRON_BOOTS);
        ItemEnchantments.Mutable enchants = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        enchants.set(haste, 2);
        boots.set(net.minecraft.core.component.DataComponents.ENCHANTMENTS, enchants.toImmutable());
        helper.assertValueEqual(ModEnchantments.haste(player, boots), 2, "and is read back off them");

        boolean sold = PechTrades.list(1).stream().map(lot -> lot.makes().apply(helper.getLevel()))
                .anyMatch(stack -> stack.is(Items.ENCHANTED_BOOK)
                        && stack.get(net.minecraft.core.component.DataComponents.STORED_ENCHANTMENTS) != null
                        && stack.get(net.minecraft.core.component.DataComponents.STORED_ENCHANTMENTS)
                                .getLevel(haste) == 1);
        helper.assertTrue(sold, "a mage pech sells the book");
        helper.succeed();
    }
}
