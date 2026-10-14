package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.entity.Champions;
import me.moonscenty.alchemia.entity.EldritchCrab;
import me.moonscenty.alchemia.entity.EldritchGuardian;
import me.moonscenty.alchemia.entity.EldritchOrb;
import me.moonscenty.alchemia.entity.InhabitedZombie;
import me.moonscenty.alchemia.entity.MindSpider;
import me.moonscenty.alchemia.player.ModAttachments;
import me.moonscenty.alchemia.player.WarpHandler;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** What comes out of the Outer Lands: the crab, the husk it lives in, the guardian, the mind spider. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class EldritchMobTests {
    private static final String TEMPLATE = "empty_32x40x32";

    private static <T extends Mob> T ordinary(GameTestHelper helper, T mob, BlockPos at) {
        mob.setData(ModAttachments.CHAMPION, Champions.ORDINARY);
        mob.moveTo(helper.absoluteVec(at.getBottomCenter()));
        mob.setNoAi(true);
        helper.getLevel().addFreshEntity(mob);
        return mob;
    }

    /** A shelled crab has five armour until it is half dead, and then its shell breaks. */
    @GameTest(template = TEMPLATE)
    public static void aCrabsShellBreaksAtHalfHealth(GameTestHelper helper) {
        EldritchCrab crab = ordinary(helper, new EldritchCrab(helper.getLevel()), new BlockPos(4, 2, 4));
        crab.setHelm(true);
        helper.assertValueEqual(crab.getArmorValue(), 5, "the shell's armour");
        crab.hurt(helper.getLevel().damageSources().genericKill(), 12.0F);
        helper.assertFalse(crab.hasHelm(), "the shell broke");
        helper.assertValueEqual(crab.getArmorValue(), 0, "and with it the armour");
        helper.succeed();
    }

    /** A husk wears the cult's helm, and dying lets a shelled crab out of it, and nothing else. */
    @GameTest(template = TEMPLATE, timeoutTicks = 40)
    public static void aHuskBurstsIntoACrab(GameTestHelper helper) {
        var level = helper.getLevel();
        InhabitedZombie husk = ordinary(helper, new InhabitedZombie(level), new BlockPos(8, 2, 8));
        husk.finalizeSpawn(level, level.getCurrentDifficultyAt(husk.blockPosition()), MobSpawnType.EVENT, null);
        helper.assertTrue(husk.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.CRIMSON.get("crimson_plate_helm")),
                "the husk wears the cult's helm");
        husk.hurt(level.damageSources().genericKill(), 1000.0F);
        AABB around = new AABB(helper.absolutePos(new BlockPos(8, 2, 8))).inflate(4.0);
        helper.succeedWhen(() -> {
            var crabs = level.getEntitiesOfClass(EldritchCrab.class, around);
            helper.assertValueEqual(crabs.size(), 1, "one crab came out");
            helper.assertTrue(crabs.getFirst().hasHelm(), "in its shell");
            helper.assertTrue(level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, around)
                    .isEmpty(), "and nothing was left behind");
        });
    }

    /** The guardian's orb hurts and weakens what is alive about it, and not the undead. */
    @GameTest(template = TEMPLATE, timeoutTicks = 60)
    public static void aGuardiansOrbSparesTheUndead(GameTestHelper helper) {
        var level = helper.getLevel();
        EldritchGuardian guardian = ordinary(helper, new EldritchGuardian(level), new BlockPos(2, 2, 8));
        Pig pig = ordinary(helper, EntityType.PIG.create(level), new BlockPos(9, 2, 8));
        Zombie zombie = ordinary(helper, EntityType.ZOMBIE.create(level), new BlockPos(9, 2, 9));
        float pigBefore = pig.getHealth();
        float zombieBefore = zombie.getHealth();
        EldritchOrb orb = new EldritchOrb(level, guardian);
        orb.moveTo(helper.absoluteVec(new Vec3(8.5, 2.5, 8.5)));
        orb.setDeltaMovement(0.3, 0.0, 0.0);
        level.addFreshEntity(orb);
        helper.succeedWhen(() -> {
            helper.assertTrue(pig.getHealth() < pigBefore, "the pig was hurt");
            helper.assertTrue(pig.hasEffect(MobEffects.WEAKNESS), "and weakened");
            helper.assertValueEqual(zombie.getHealth(), zombieBefore, "the zombie was not");
        });
    }

    /** A guardian and a crab are of one side; neither is the other's enemy. */
    @GameTest(template = TEMPLATE)
    public static void eldritchThingsAreOfOneSide(GameTestHelper helper) {
        var level = helper.getLevel();
        EldritchGuardian guardian = ordinary(helper, new EldritchGuardian(level), new BlockPos(4, 2, 4));
        EldritchCrab crab = ordinary(helper, new EldritchCrab(level), new BlockPos(8, 2, 4));
        helper.assertTrue(guardian.isAlliedTo(crab), "the guardian spares the crab");
        helper.succeed();
    }

    /** A spider warp shows one player bites for nothing and is worth nothing. */
    @GameTest(template = TEMPLATE)
    public static void anIllusorySpiderIsHarmless(GameTestHelper helper) {
        var level = helper.getLevel();
        MindSpider spider = ordinary(helper, new MindSpider(level), new BlockPos(4, 2, 4));
        spider.setHarmless(true);
        spider.setViewer("somebody");
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        float before = player.getHealth();
        helper.assertFalse(spider.doHurtTarget(player), "its bite does nothing");
        helper.assertValueEqual(player.getHealth(), before, "the player is unhurt");
        helper.succeed();
    }

    /** The deep rungs of warp call up guardians in a fog, and spiders. */
    @GameTest(template = TEMPLATE)
    public static void warpCallsUpTheOuterLands(GameTestHelper helper) {
        helper.assertTrue(WarpHandler.outcomeFor(60, 30).summons() == WarpHandler.Summons.GUARDIAN,
                "roll 30 brings a guardian");
        helper.assertTrue(WarpHandler.outcomeFor(60, 58).summons() == WarpHandler.Summons.ILLUSORY_SPIDERS,
                "roll 58 brings spiders only the player sees");
        helper.assertTrue(WarpHandler.outcomeFor(60, 90).summons() == WarpHandler.Summons.SPIDERS,
                "roll 90 brings real ones");
        helper.assertTrue(WarpHandler.outcomeFor(100, 95).summons() == WarpHandler.Summons.MANY_GUARDIANS,
                "and the deepest brings many guardians");
        helper.succeed();
    }
}
