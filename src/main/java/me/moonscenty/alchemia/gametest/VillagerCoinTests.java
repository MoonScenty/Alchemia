package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** A villager leaves a coin when it dies: a tenth of the time, and a tenth more for each level of looting. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class VillagerCoinTests {
    private static final String TEMPLATE = "empty_32x40x32";

    @GameTest(template = TEMPLATE)
    public static void aVillagerKilledWithLootingLeavesACoin(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        // nine levels of looting make it certain
        sword.enchant(helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.LOOTING), 9);
        player.setItemInHand(InteractionHand.MAIN_HAND, sword);
        Villager villager = helper.spawnWithNoFreeWill(EntityType.VILLAGER, new BlockPos(4, 2, 4));
        villager.hurt(helper.getLevel().damageSources().playerAttack(player), 1000.0F);
        boolean coin = !helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                new AABB(villager.blockPosition()).inflate(3), item -> item.getItem().is(ModItems.COIN.get())).isEmpty();
        helper.assertTrue(coin, "a coin was left");
        helper.succeed();
    }
}
