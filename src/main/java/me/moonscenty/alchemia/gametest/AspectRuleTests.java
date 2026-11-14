package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aspect.Aspects;
import me.moonscenty.alchemia.block.entity.JarBlockEntity;
import me.moonscenty.alchemia.item.CrystallizedEssenceItem;
import me.moonscenty.alchemia.item.PhialItem;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModDamageTypes;
import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModWandParts;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class AspectRuleTests {
    private static final String TEMPLATE = "empty_32x40x32";

    @GameTest(template = TEMPLATE)
    public static void containersCountWhatIsInThem(GameTestHelper helper) {
        var fire = ModAspects.FIRE;
        helper.assertValueEqual(Aspects.of(CrystallizedEssenceItem.of(fire)).get(fire), 1, "a crystal holds one");
        ItemStack phial = PhialItem.filled(new ItemStack(ModItems.PHIAL.get()), fire);
        helper.assertValueEqual(Aspects.of(phial).get(fire) - Aspects.of(ModItems.PHIAL.get()).get(fire), 8, "a phial eight");
        ItemStack jar = new ItemStack(ModBlocks.JAR.get());
        jar.set(ModDataComponents.CONTENTS.get(), AspectList.of(fire, JarBlockEntity.CAPACITY / 2));
        helper.assertValueEqual(Aspects.of(jar).get(fire) - Aspects.of(jar.getItem()).get(fire),
                Math.min(64, JarBlockEntity.CAPACITY / 2), "a jar what was poured in");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void potionsCarryTheirEffects(GameTestHelper helper) {
        AspectList water = Aspects.of(PotionContents.createItemStack(Items.POTION, Potions.WATER));
        helper.assertValueEqual(water.get(ModAspects.WATER), 1, "water");
        helper.assertValueEqual(water.get(ModAspects.CRYSTAL), 1, "and glass");

        AspectList healing = Aspects.of(PotionContents.createItemStack(Items.POTION, Potions.STRONG_HEALING));
        helper.assertValueEqual(healing.get(ModAspects.LIFE), 6, "strong healing is three a level");
        helper.assertValueEqual(healing.get(ModAspects.ENERGY), 4, "and two of energy a level");

        AspectList splash = Aspects.of(PotionContents.createItemStack(Items.SPLASH_POTION, Potions.FIRE_RESISTANCE));
        helper.assertValueEqual(splash.get(ModAspects.ENTROPY), 2, "splashing is entropy");
        helper.assertValueEqual(splash.get(ModAspects.FIRE), 2, "fire resistance has its own mix");
        helper.assertValueEqual(splash.get(ModAspects.PROTECT), 1, "of fire and protection");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void wandsCarryThePriceOfTheirParts(GameTestHelper helper) {
        ItemStack wand = WandItem.of(ModWandParts.GREATWOOD, ModWandParts.GOLD);
        int cost = WandItem.rod(wand).craftCost() + WandItem.cap(wand).craftCost();
        AspectList carried = Aspects.of(wand);
        helper.assertTrue(carried.get(ModAspects.ENERGY) >= cost / 2, "energy of half the parts");
        helper.assertTrue(carried.get(ModAspects.TOOL) >= cost / 3, "and tool of a third");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void toolRanksMatchTheOriginal(GameTestHelper helper) {
        helper.assertValueEqual(Aspects.of(Items.SHEARS).get(ModAspects.TOOL), 3, "shears");
        helper.assertValueEqual(Aspects.of(Items.GOLDEN_HOE).get(ModAspects.TOOL), 1, "a gold hoe wears out at once");
        helper.assertValueEqual(Aspects.of(Items.DIAMOND_HOE).get(ModAspects.TOOL), 4, "a diamond hoe");
        helper.assertValueEqual(Aspects.of(ModItems.METAL_TOOLS.get("thaumium_pickaxe").get()).get(ModAspects.TOOL), 4,
                "an thaumium pickaxe digs like diamond");
        helper.assertValueEqual(Aspects.of(ModItems.METAL_TOOLS.get("void_pickaxe").get()).get(ModAspects.TOOL), 5,
                "and a void one better");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aCrucibleResultIsCatalystPlusTheRootOfWhatItTook(GameTestHelper helper) {
        AspectList coal = Aspects.of(Items.COAL);
        AspectList alumentum = Aspects.of(ModItems.ALUMENTUM.get());
        helper.assertValueEqual(alumentum.get(ModAspects.ENTROPY), coal.get(ModAspects.ENTROPY) + 1, "root of three is one");
        helper.assertValueEqual(alumentum.get(ModAspects.ENERGY), coal.get(ModAspects.ENERGY) + 1, "for each aspect");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void nitorComesOutSingleAndYellow(GameTestHelper helper) {
        var made = helper.getLevel().getRecipeManager().byKey(Alchemia.id("nitor")).orElseThrow();
        ItemStack result = made.value().getResultItem(helper.getLevel().registryAccess());
        helper.assertTrue(result.is(ModBlocks.NITOR.get(DyeColor.YELLOW).get().asItem()), "yellow, found " + result);
        helper.assertValueEqual(result.getCount(), 1, "one at a time");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void fuelsBurnAsLongAsTheOriginal(GameTestHelper helper) {
        helper.assertValueEqual(new ItemStack(ModItems.ALUMENTUM.get()).getBurnTime(RecipeType.SMELTING), 6400, "alumentum");
        helper.assertValueEqual(new ItemStack(ModBlocks.GREATWOOD.log().get()).getBurnTime(RecipeType.SMELTING), 400,
                "greatwood");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 60)
    public static void whatDissolvesLeavesCrystals(GameTestHelper helper) {
        for (int east = 0; east < 12; east++) {
            var cow = helper.spawn(EntityType.COW, new BlockPos(4 + east * 2, 2, 8));
            cow.hurt(ModDamageTypes.dissolve(helper.getLevel()), 1000.0F);
        }
        helper.succeedWhen(() -> helper.assertTrue(helper.getLevel()
                .getEntitiesOfClass(ItemEntity.class, helper.getBounds().inflate(2.0)).stream()
                .anyMatch(item -> item.getItem().is(ModItems.CRYSTALLIZED_ESSENCE.get())), "no crystals yet"));
    }
}
