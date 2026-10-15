package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.CrystalType;
import me.moonscenty.alchemia.item.BoneBowItem;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The bow of bone: drawn in half the time, harder arrows, a bow's enchantments, and primal arrows first. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class BoneBowTests {
    private static final String TEMPLATE = "empty_32x40x32";

    @GameTest(template = TEMPLATE)
    public static void drawsInHalfTheTime(GameTestHelper helper) {
        helper.assertValueEqual(BoneBowItem.power(BoneBowItem.FULL_DRAW), 1.0F, "full at ten ticks");
        helper.assertValueEqual(BoneBowItem.power(5), net.minecraft.world.item.BowItem.getPowerForTime(10),
                "five ticks of it are ten of a bow");
        ItemStack bow = new ItemStack(ModItems.BONE_BOW.get());
        helper.assertValueEqual(bow.getMaxDamage(), 512, "it lasts 512 shots");
        helper.assertTrue(bow.is(ItemTags.BOW_ENCHANTABLE), "and takes a bow's enchantments");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void arrowsHitHarder(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack bow = new ItemStack(ModItems.BONE_BOW.get());
        AbstractArrow plain = net.minecraft.world.item.ArrowItem.class.cast(Items.ARROW)
                .createArrow(helper.getLevel(), new ItemStack(Items.ARROW), player, bow);
        double before = plain.getBaseDamage();
        ModItems.BONE_BOW.get().customArrow(plain, new ItemStack(Items.ARROW), bow);
        helper.assertTrue(Math.abs(plain.getBaseDamage() - before - BoneBowItem.HARDER) < 1.0E-6,
                "half a point harder");
        player.getInventory().setItem(0, new ItemStack(Items.ARROW, 4));
        player.getInventory().setItem(9, new ItemStack(ModItems.PRIMAL_ARROWS.get(CrystalType.AIR).get(), 4));
        helper.assertTrue(player.getProjectile(bow).is(ModItems.PRIMAL_ARROWS.get(CrystalType.AIR).get()),
                "and it reaches for primal arrows first, as a bow does");
        helper.succeed();
    }
}
