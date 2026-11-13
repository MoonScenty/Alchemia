package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.entity.PechTrades;
import me.moonscenty.alchemia.golem.seal.Matching;
import me.moonscenty.alchemia.golem.seal.Provisioning;
import me.moonscenty.alchemia.item.HandMirrorItem;
import me.moonscenty.alchemia.registry.ModEntities;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class LooseEndTests {
    private static final String TEMPLATE = "empty_32x40x32";

    @GameTest(template = TEMPLATE)
    public static void theOreDictionaryOptionMatchesOnlyNamedTags(GameTestHelper helper) {
        Matching.Rule ore = new Matching.Rule(false, false, true, false);
        helper.assertTrue(ore.alike(new ItemStack(Items.IRON_ORE), new ItemStack(Items.DEEPSLATE_IRON_ORE)),
                "two iron ores share c:ores/iron");
        helper.assertFalse(ore.alike(new ItemStack(Items.IRON_INGOT), new ItemStack(Items.GOLD_INGOT)),
                "c:ingots alone does not make iron gold");
        helper.assertFalse(ore.alike(new ItemStack(Items.BREAD), new ItemStack(Items.ROTTEN_FLESH)),
                "nor does c:foods make bread rotten flesh");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void constructsAndGolemsDoNotDrown(GameTestHelper helper) {
        for (EntityType<?> type : java.util.List.of(ModEntities.GOLEM.get(), ModEntities.AUTOMATED_CROSSBOW.get(),
                ModEntities.ADVANCED_AUTOMATED_CROSSBOW.get(), ModEntities.AUTOCASTER.get(),
                ModEntities.ELDRITCH_AUTOCASTER.get())) {
            LivingEntity made = (LivingEntity) type.create(helper.getLevel());
            helper.assertTrue(made != null && made.canBreatheUnderwater(), type + " breathes under water");
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aPechPrizesTheEnchantedApple(GameTestHelper helper) {
        helper.assertValueEqual(PechTrades.value(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE)), 2,
                "an enchanted apple is a golden apple to a pech");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aUseSealWithoutMetadataTakesAnyDamage(GameTestHelper helper) {
        ItemStack fresh = new ItemStack(Items.IRON_PICKAXE);
        ItemStack worn = fresh.copy();
        worn.set(DataComponents.DAMAGE, 40);
        helper.assertTrue(new Provisioning.Request(null, fresh, true).rule().alike(fresh, worn),
                "any damage will do");
        helper.assertFalse(new Provisioning.Request(null, fresh, false).rule().alike(fresh, worn),
                "a strict request wants the same damage");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aMirrorNamesItsWorld(GameTestHelper helper) {
        helper.assertValueEqual(((TranslatableContents) HandMirrorItem.where(Level.OVERWORLD).getContents()).getKey(),
                "dimension.minecraft.overworld", "the overworld by name, not by id");
        helper.succeed();
    }
}
