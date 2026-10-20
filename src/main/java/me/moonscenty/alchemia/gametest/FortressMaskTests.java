package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.item.FortressArmorItem;
import me.moonscenty.alchemia.item.FortressMasks;
import me.moonscenty.alchemia.item.HelmFitting;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The fortress helm's masks: what each one does for whoever wears it. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class FortressMaskTests {
    private static final String TEMPLATE = "empty_32x40x32";

    private static Player masked(GameTestHelper helper, HelmFitting mask) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack helm = FortressArmorItem.wearing(new ItemStack(ModItems.FORTRESS.get("fortress_helm").get()), mask);
        player.setItemSlot(EquipmentSlot.HEAD, helm);
        return player;
    }

    @GameTest(template = TEMPLATE)
    public static void theAngryGhostWithersWhatStrikesIt(GameTestHelper helper) {
        Player player = masked(helper, HelmFitting.ANGRY_GHOST);
        player.moveTo(helper.absoluteVec(new net.minecraft.world.phys.Vec3(4, 2, 4)));
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new net.minecraft.core.BlockPos(5, 2, 4));
        // a blow of ten or more is sure to answer
        player.hurt(helper.getLevel().damageSources().mobAttack(zombie), 12.0F);
        helper.assertTrue(zombie.hasEffect(MobEffects.WITHER), "the ghost withered what struck");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theSippingFiendDrinksFromWhatItStrikes(GameTestHelper helper) {
        Player player = masked(helper, HelmFitting.SIPPING_FIEND);
        player.setHealth(10.0F);
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new net.minecraft.core.BlockPos(5, 2, 4));
        zombie.hurt(helper.getLevel().damageSources().playerAttack(player), 12.0F);
        helper.assertValueEqual(player.getHealth(), 11.0F, "a point drunk back");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theGrinningDevilEasesWarp(GameTestHelper helper) {
        Player devil = masked(helper, HelmFitting.GRINNING_DEVIL);
        int eased = FortressMasks.easing(devil);
        helper.assertTrue(eased >= 2 && eased <= 5, "two to five off the roll, was " + eased);
        helper.assertValueEqual(FortressMasks.easing(masked(helper, HelmFitting.ANGRY_GHOST)), 0,
                "and no other mask does it");
        helper.succeed();
    }
}
