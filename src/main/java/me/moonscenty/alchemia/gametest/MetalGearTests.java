package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.enchantment.InfusionEnchantment;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * What our two metals are worth once they are forged.
 * <p>
 * The numbers came from the original and the shapes came from vanilla, so what is worth holding to is not any one
 * of them but the relation between them: that neither metal is simply the better one, and that what is forged is
 * a pickaxe as far as everything else is concerned.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class MetalGearTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final BlockPos HERE = new BlockPos(8, 2, 8);

    private static ItemStack tool(String name) {
        return new ItemStack(ModItems.METAL_TOOLS.get(name).get());
    }

    /**
     * A forged pickaxe is a pickaxe to everything that asks.
     * <p>
     * Extending the class is not the same as saying so: vanilla, and the workings an altar puts on tools, both
     * ask the tag. A pickaxe missing from it is a pickaxe nobody else can see.
     */
    @GameTest(template = TEMPLATE)
    public static void whatIsForgedIsWhatItLooksLike(GameTestHelper helper) {
        helper.assertTrue(tool("alchemium_pickaxe").is(ItemTags.PICKAXES), "a pickaxe is in the pickaxes");
        helper.assertTrue(tool("void_sword").is(ItemTags.SWORDS), "and a sword in the swords");
        helper.assertTrue(new ItemStack(ModItems.METAL_ARMOUR.get("void_helmet").get()).is(ItemTags.HEAD_ARMOR),
                "and a helmet is worn on the head");

        // which is what lets an altar put its workings on them, the same as on anything vanilla forged
        helper.assertTrue(InfusionEnchantment.DESTRUCTIVE.fits(tool("alchemium_pickaxe")),
                "so an altar will work destructive into it");
        helper.assertTrue(InfusionEnchantment.ESSENCE.fits(tool("void_sword")),
                "and essence into the sword");
        helper.succeed();
    }

    /**
     * Neither metal is simply the better one.
     * <p>
     * Void cuts faster and hits harder; alchemium lasts more than three times as long and takes enchantments
     * twice as well. If that ever stops being true one of them has become the only one worth forging.
     */
    @GameTest(template = TEMPLATE)
    public static void neitherMetalIsSimplyTheBetterOne(GameTestHelper helper) {
        ItemStack steady = tool("alchemium_pickaxe");
        ItemStack sharp = tool("void_pickaxe");

        helper.assertTrue(sharp.getItem().getDestroySpeed(sharp, Blocks.STONE.defaultBlockState())
                        > steady.getItem().getDestroySpeed(steady, Blocks.STONE.defaultBlockState()),
                "void cuts faster");
        helper.assertTrue(steady.getMaxDamage() > sharp.getMaxDamage() * 3,
                "and alchemium outlasts it more than threefold: "
                        + steady.getMaxDamage() + " against " + sharp.getMaxDamage());
        helper.assertTrue(steady.getEnchantmentValue() > sharp.getEnchantmentValue(),
                "and takes an enchantment better");

        // and both reach past iron, which is the whole reason for forging either
        ItemStack iron = new ItemStack(Items.IRON_PICKAXE);
        helper.assertTrue(steady.getItem().getDestroySpeed(steady, Blocks.STONE.defaultBlockState())
                        > iron.getItem().getDestroySpeed(iron, Blocks.STONE.defaultBlockState()),
                "even the steady one beats iron");
        helper.succeed();
    }

    /** A suit is worth more the further up the body it goes, and void stops more of a blow than alchemium. */
    @GameTest(template = TEMPLATE)
    public static void voidStopsMoreAndAlchemiumLastsLonger(GameTestHelper helper) {
        ItemStack steady = new ItemStack(ModItems.METAL_ARMOUR.get("alchemium_chestplate").get());
        ItemStack sharp = new ItemStack(ModItems.METAL_ARMOUR.get("void_chestplate").get());

        helper.assertTrue(steady.getMaxDamage() > sharp.getMaxDamage(),
                "an alchemium suit outlasts a void one: " + steady.getMaxDamage() + " against " + sharp.getMaxDamage());
        helper.succeed();
    }
}
