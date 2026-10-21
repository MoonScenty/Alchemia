package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.item.VoidGear;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The void pickaxe, axe, shovel and hoe weaken what they are swung at; other tools do not. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class VoidToolTests {
    private static final String TEMPLATE = "empty_32x40x32";

    @GameTest(template = TEMPLATE)
    public static void voidToolsWeakenWhatTheyAreSwungAt(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        for (String tool : new String[] {"void_pickaxe", "void_axe", "void_shovel", "void_hoe"}) {
            Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(4, 2, 4));
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.METAL_TOOLS.get(tool).get()));
            VoidGear.swungAt(player, zombie);
            var weak = zombie.getEffect(MobEffects.WEAKNESS);
            helper.assertTrue(weak != null && weak.getDuration() == VoidGear.TOOLS_WEAKEN_FOR, tool + " weakens");
            zombie.discard();
        }
        Zombie spared = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(6, 2, 4));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_PICKAXE));
        VoidGear.swungAt(player, spared);
        helper.assertFalse(spared.hasEffect(MobEffects.WEAKNESS), "an iron pickaxe does not");
        spared.discard();
        helper.succeed();
    }
}
