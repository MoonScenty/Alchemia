package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.entity.AngryZombie;
import me.moonscenty.alchemia.entity.ChampionModifier;
import me.moonscenty.alchemia.entity.Champions;
import me.moonscenty.alchemia.entity.FuriousZombie;
import me.moonscenty.alchemia.player.WarpData;
import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import me.moonscenty.alchemia.player.ModAttachments;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The angry zombies, their brains, and champions. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class MobTests {
    private static final String TEMPLATE = "empty_32x40x32";

    /** Spawns one already rolled ordinary, so that no test meets a champion by chance. */
    private static <T extends Mob> T ordinary(GameTestHelper helper, EntityType<T> type, BlockPos at) {
        T mob = type.create(helper.getLevel());
        mob.setData(ModAttachments.CHAMPION, Champions.ORDINARY);
        mob.moveTo(helper.absoluteVec(at.getBottomCenter()));
        mob.setNoAi(true);
        helper.getLevel().addFreshEntity(mob);
        return mob;
    }

    /** Tougher than a zombie, and a harder bite. */
    @GameTest(template = TEMPLATE)
    public static void anAngryZombieIsTougherThanAZombie(GameTestHelper helper) {
        AngryZombie zombie = ordinary(helper, ModEntities.ANGRY_ZOMBIE.get(), new BlockPos(4, 2, 4));
        helper.assertValueEqual(zombie.getMaxHealth(), 25.0F, "its health");
        helper.assertValueEqual(zombie.getAttributeValue(Attributes.ARMOR), 5.0, "its armour");
        helper.assertValueEqual(zombie.getAttributeBaseValue(Attributes.ATTACK_DAMAGE), 5.0, "its bite");
        helper.succeed();
    }

    /** Every blow makes it angrier, and the angrier it is the bigger it is and the harder it bites. */
    @GameTest(template = TEMPLATE)
    public static void aFuriousZombieGrowsWithItsAnger(GameTestHelper helper) {
        FuriousZombie zombie = ordinary(helper, ModEntities.FURIOUS_ZOMBIE.get(), new BlockPos(4, 2, 4));
        float before = zombie.getBbHeight();
        for (int i = 0; i < 5; i++) {
            zombie.invulnerableTime = 0;
            zombie.hurt(helper.getLevel().damageSources().generic(), 0.5F);
        }
        helper.runAfterDelay(3, () -> {
            helper.assertTrue(zombie.anger() > 1.4F, "five blows make it half again as angry");
            helper.assertTrue(zombie.getBbHeight() > before * 1.4F, "and that much taller");
            helper.assertTrue(zombie.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) > 9.0, "and its bite harder");
            helper.succeed();
        });
    }

    /** Eating a brain leaves warp behind, one way or the other. */
    @GameTest(template = TEMPLATE)
    public static void eatingABrainLeavesWarp(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ModItems.ZOMBIE_BRAIN.get().finishUsingItem(ModItems.ZOMBIE_BRAIN.toStack(), helper.getLevel(), player);
        helper.assertTrue(WarpData.of(player).total() > 0, "the eater has warp");
        helper.succeed();
    }

    /** A champion has a hundred more health, three times the bite, and a name that says so. */
    @GameTest(template = TEMPLATE)
    public static void aChampionIsStrongerAndNamed(GameTestHelper helper) {
        Zombie zombie = ordinary(helper, EntityType.ZOMBIE, new BlockPos(4, 2, 4));
        double bite = zombie.getAttributeValue(Attributes.ATTACK_DAMAGE);
        Champions.make(zombie, ChampionModifier.UNDYING);
        helper.assertValueEqual(zombie.getMaxHealth(), 120.0F, "its health");
        helper.assertValueEqual(zombie.getAttributeValue(Attributes.ATTACK_DAMAGE), bite * 3.0, "its bite");
        helper.assertTrue(zombie.hasCustomName(), "it has a name");
        helper.assertValueEqual(Champions.modifier(zombie), ChampionModifier.UNDYING, "what it is champion of");
        helper.succeed();
    }

    /** A warded champion has a ward of half its own health, and an armored one takes a quarter less. */
    @GameTest(template = TEMPLATE)
    public static void wardedAndArmoredChampionsTakeLess(GameTestHelper helper) {
        Zombie warded = ordinary(helper, EntityType.ZOMBIE, new BlockPos(4, 2, 4));
        Champions.make(warded, ChampionModifier.WARDED);
        helper.assertValueEqual(warded.getAbsorptionAmount(), 10.0F, "its ward");

        Zombie armored = ordinary(helper, EntityType.ZOMBIE, new BlockPos(8, 2, 4));
        Zombie plain = ordinary(helper, EntityType.ZOMBIE, new BlockPos(12, 2, 4));
        Champions.make(armored, ChampionModifier.ARMORED);
        Champions.make(plain, ChampionModifier.UNDYING);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        DamageSource hit = helper.getLevel().damageSources().playerAttack(player);
        float armoredBefore = armored.getHealth();
        float plainBefore = plain.getHealth();
        armored.hurt(hit, 10.0F);
        plain.hurt(hit, 10.0F);
        helper.assertTrue(armoredBefore - armored.getHealth() < plainBefore - plain.getHealth(),
                "the armored one lost less");
        helper.succeed();
    }
}
