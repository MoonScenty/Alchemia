package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.registry.ModArmorMaterials;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class ItemValueTests {
    private static final String TEMPLATE = "empty_32x40x32";

    private static double bite(Item sword) {
        return new ItemStack(sword).getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,
                net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY).modifiers().stream()
                .filter(entry -> entry.attribute().is(Attributes.ATTACK_DAMAGE))
                .mapToDouble(entry -> entry.modifier().amount()).sum();
    }

    @GameTest(template = TEMPLATE)
    public static void armourAndToolsHaveTheOriginalValues(GameTestHelper helper) {
        helper.assertValueEqual(ModArmorMaterials.FORTRESS_LASTS, 40, "fortress lasts 40");
        helper.assertValueEqual(ModArmorMaterials.FORTRESS.value().enchantmentValue(), 25, "fortress takes 25");
        helper.assertValueEqual(ModArmorMaterials.GOGGLES.value().enchantmentValue(), 25, "goggles take 25");
        helper.assertValueEqual(ModArmorMaterials.TRAVELLER.value().enchantmentValue(), 25, "boots take 25");
        helper.assertTrue(ModArmorMaterials.CLOTH.value().repairIngredient().get()
                .test(new ItemStack(ModItems.ENCHANTED_FABRIC.get())), "a robe is mended with enchanted fabric");

        Item praetor = ModItems.CRIMSON.get("crimson_praetor_chest").get();
        helper.assertValueEqual(new ItemStack(praetor).getMaxDamage(),
                net.minecraft.world.item.ArmorItem.Type.CHESTPLATE.getDurability(45), "praetor plate lasts 45");
        helper.assertValueEqual(new ItemStack(praetor).getRarity(), Rarity.RARE, "and is rare");
        helper.assertValueEqual(new ItemStack(ModItems.WAND.get()).getRarity(), Rarity.UNCOMMON, "a wand is uncommon");
        helper.assertValueEqual(new ItemStack(ModItems.METAL_ARMOUR.get("void_chestplate").get()).getRarity(),
                Rarity.UNCOMMON, "so is void armour");

        helper.assertValueEqual(bite(ModItems.METAL_TOOLS.get("alchemium_sword").get()), 6.5,
                "an alchemium sword bites for 4 + 2.5");
        helper.assertValueEqual(bite(ModItems.CRIMSON_BLADE.get()), 7.5, "a crimson blade for 4 + 3.5");
        helper.assertValueEqual(bite(ModItems.METAL_TOOLS.get("alchemium_axe").get()), 8.5,
                "an axe keeps the 1.21 heft, 6 + 2.5");
        for (String hoe : java.util.List.of("alchemium_hoe", "void_hoe")) {
            helper.assertValueEqual(bite(ModItems.METAL_TOOLS.get(hoe).get()), 0.0, hoe + " hits like a fist");
        }
        helper.assertValueEqual(bite(ModItems.ELEMENTAL_TOOLS.get("elemental_hoe").get()), 0.0,
                "so does the elemental hoe");

        helper.assertValueEqual(new ItemStack(ModItems.LOOT_BAG_COMMON.get()).getMaxStackSize(), 16, "bags stack to 16");
        helper.assertValueEqual(new ItemStack(ModItems.PRIMAL_CHARM.get()).getMaxStackSize(), 1, "a charm alone");
        helper.assertValueEqual(new ItemStack(ModItems.RESEARCH_NOTES.get()).getMaxStackSize(), 64, "notes to 64");
        helper.succeed();
    }
}
