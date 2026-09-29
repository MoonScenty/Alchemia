package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModTags;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The goggles, which are worth having for one reason.
 * <p>
 * An aura node is invisible without them, and whether they reveal is not written in the goggles at all -- it is
 * a tag the sight reads. A tag is easy to forget to fill in and nothing else in the mod would complain about it,
 * so this is the thing worth holding to.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class GogglesTests {
    private static final String TEMPLATE = "empty_32x40x32";

    /** They are in the list the sight reads, and ordinary headgear is not. */
    @GameTest(template = TEMPLATE)
    public static void nodesAreSeenThroughThem(GameTestHelper helper) {
        helper.assertTrue(new ItemStack(ModItems.GOGGLES.get()).is(ModTags.Items.REVEALS),
                "an aura node can be seen through them");
        helper.assertFalse(new ItemStack(Items.IRON_HELMET).is(ModTags.Items.REVEALS),
                "and not through an iron helmet");
        helper.succeed();
    }

    /** They are worn on the head, which nothing about extending the class says on its own. */
    @GameTest(template = TEMPLATE)
    public static void theyAreWornOnTheHead(GameTestHelper helper) {
        helper.assertTrue(new ItemStack(ModItems.GOGGLES.get()).is(ItemTags.HEAD_ARMOR),
                "they go on the head");
        helper.succeed();
    }
}
