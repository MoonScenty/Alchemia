package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Bags of loot. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class LootTests {
    private static final String TEMPLATE = "empty_32x40x32";

    /** A bag opened spills eight to twelve things, and is gone. */
    @GameTest(template = TEMPLATE)
    public static void aBagSpillsItsLoot(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.moveTo(helper.absoluteVec(new BlockPos(4, 2, 4).getBottomCenter()));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.LOOT_BAG_RARE.get()));
        ModItems.LOOT_BAG_RARE.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        int spilled = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                new AABB(player.blockPosition()).inflate(2.0)).size();
        helper.assertTrue(spilled >= 8 && spilled <= 12, "it spilled eight to twelve things: " + spilled);
        helper.assertTrue(player.getMainHandItem().isEmpty(), "and the bag is gone");
        helper.succeed();
    }
}
