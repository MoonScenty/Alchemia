package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.entity.ArcaneWorkbenchBlockEntity;
import me.moonscenty.alchemia.menu.ArcaneWorkbenchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class ArcaneWorkbenchPlainTests {
    private static final String TEMPLATE = "empty_32x40x32";

    @GameTest(template = TEMPLATE)
    public static void plainCraftingWorksWithoutAWand(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        SimpleContainer bench = new SimpleContainer(ArcaneWorkbenchBlockEntity.SIZE);
        for (int slot : new int[] {4, 5, 7, 8}) {
            bench.setItem(slot, new ItemStack(Items.OAK_PLANKS));
        }
        ArcaneWorkbenchMenu menu = new ArcaneWorkbenchMenu(0, player.getInventory(), bench,
                ContainerLevelAccess.create(helper.getLevel(), helper.absolutePos(new BlockPos(1, 1, 1))));
        helper.assertTrue(menu.plain().isPresent(), "four planks in a corner make a crafting table");
        helper.assertTrue(menu.cost().isEmpty(), "and cost no vis");
        menu.slotsChanged(bench);
        helper.assertTrue(menu.getSlot(ArcaneWorkbenchMenu.SLOT_RESULT).getItem().is(Items.CRAFTING_TABLE),
                "with no wand on the bench at all");
        helper.succeed();
    }
}
