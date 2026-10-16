package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.enchantment.ModEnchantments;
import me.moonscenty.alchemia.enchantment.Repair;
import me.moonscenty.alchemia.entity.PechTrades;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Repair: two levels, on the original's own gear only and never with unbreaking, mending out of the aura. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class RepairTests {
    private static final String TEMPLATE = "empty_32x40x32";

    @GameTest(template = TEMPLATE)
    public static void repairGoesOnThaumaturgicalGearAlone(GameTestHelper helper) {
        var enchantments = helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        var repair = enchantments.getHolderOrThrow(ModEnchantments.REPAIR);
        helper.assertValueEqual(repair.value().getMaxLevel(), 2, "two levels");
        helper.assertTrue(repair.value().canEnchant(new ItemStack(ModItems.METAL_TOOLS.get("alchemium_pickaxe").get())),
                "on an alchemium pickaxe");
        helper.assertTrue(repair.value().canEnchant(new ItemStack(ModItems.GOGGLES.get())), "on the goggles");
        helper.assertFalse(repair.value().canEnchant(new ItemStack(Items.IRON_PICKAXE)), "not on an iron pickaxe");
        helper.assertFalse(Enchantment.areCompatible(repair, enchantments.getHolderOrThrow(Enchantments.UNBREAKING)),
                "not with unbreaking");

        boolean sold = PechTrades.list(1).stream().map(lot -> lot.makes().apply(helper.getLevel()))
                .anyMatch(stack -> stack.is(Items.ENCHANTED_BOOK) && stack.get(DataComponents.STORED_ENCHANTMENTS) != null
                        && stack.get(DataComponents.STORED_ENCHANTMENTS).getLevel(repair) == 1);
        helper.assertTrue(sold, "a mage pech sells the book");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void repairMendsOutOfTheAura(GameTestHelper helper) {
        var repair = helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(ModEnchantments.REPAIR);
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        ItemStack pick = new ItemStack(ModItems.METAL_TOOLS.get("alchemium_pickaxe").get());
        pick.setDamageValue(10);
        helper.assertFalse(Repair.mend(pick.copy(), helper.getLevel(), pos, helper.getLevel().random),
                "nothing without the enchantment");

        ItemEnchantments.Mutable enchants = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        enchants.set(repair, 2);
        pick.set(DataComponents.ENCHANTMENTS, enchants.toImmutable());
        me.moonscenty.alchemia.aspect.AspectList full = me.moonscenty.alchemia.aspect.AspectList.EMPTY;
        for (var primal : me.moonscenty.alchemia.registry.ModAspects.primals()) {
            full = full.add(primal, 500);
        }
        helper.getLevel().getChunkAt(pos).setData(me.moonscenty.alchemia.aura.ModAuraAttachment.AURA,
                new me.moonscenty.alchemia.aura.AuraChunk(500, full));
        int before = AuraHandler.at(helper.getLevel(), pos).aspects().total();
        helper.assertTrue(Repair.mend(pick, helper.getLevel(), pos, helper.getLevel().random), "mends");
        helper.assertValueEqual(pick.getDamageValue(), 8, "two points at level two");
        helper.assertValueEqual(AuraHandler.at(helper.getLevel(), pos).aspects().total(), before - 2, "paid out of the aura");
        helper.succeed();
    }
}
