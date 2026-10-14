package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.eldritch.EldritchLockBlockEntity;
import me.moonscenty.alchemia.entity.Champions;
import me.moonscenty.alchemia.entity.boss.EldritchGolem;
import me.moonscenty.alchemia.entity.boss.EldritchWarden;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The great halls: the lock a runed tablet turns, and what wakes behind it. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class BossTests {
    private static final String TEMPLATE = "empty_32x40x32";

    /** A runed tablet turns a lock; nothing else does. */
    @GameTest(template = TEMPLATE)
    public static void aTabletTurnsTheLock(GameTestHelper helper) {
        BlockPos at = new BlockPos(16, 2, 16);
        helper.setBlock(at, ModBlocks.ELDRITCH_LOCK.get());
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(ModItems.RUNED_TABLET.get()));
        helper.useBlock(at, player);
        helper.assertTrue(helper.getBlockEntity(at) instanceof EldritchLockBlockEntity lock && !lock.waiting(),
                "the lock turns");
        helper.assertTrue(player.getMainHandItem().isEmpty(), "and keeps the tablet");
        helper.succeed();
    }

    /** The Warden wakes slowly, untouchable while it does, with a shield two thirds of its health deep. */
    @GameTest(template = TEMPLATE)
    public static void theWardenWakesShielded(GameTestHelper helper) {
        EldritchWarden warden = ModEntities.ELDRITCH_WARDEN.get().create(helper.getLevel());
        warden.moveTo(helper.absoluteVec(new BlockPos(16, 2, 16).getBottomCenter()));
        warden.finalizeSpawn(helper.getLevel(), helper.getLevel().getCurrentDifficultyAt(warden.blockPosition()),
                MobSpawnType.EVENT, null);
        helper.getLevel().addFreshEntity(warden);
        helper.assertTrue(warden.spawnTimer() == EldritchWarden.RISING, "it is rising");
        helper.assertTrue(warden.getAbsorptionAmount() >= 264.0F - 1.0F, "behind its shield: "
                + warden.getAbsorptionAmount());
        helper.assertFalse(warden.hurt(helper.getLevel().damageSources().generic(), 10.0F), "untouchable as it wakes");
        helper.assertTrue(Champions.isChampion(warden) && warden.hasCustomName(), "a named champion");
        warden.discard();
        helper.succeed();
    }

    /** The blow that would kill the Construct takes its head instead; and no blow does a boss more than thirty-five. */
    @GameTest(template = TEMPLATE)
    public static void theConstructLosesItsHead(GameTestHelper helper) {
        EldritchGolem golem = ModEntities.ELDRITCH_GOLEM.get().create(helper.getLevel());
        golem.moveTo(helper.absoluteVec(new BlockPos(16, 2, 16).getBottomCenter()));
        helper.getLevel().addFreshEntity(golem);
        golem.setSpawnTimer(0);
        float before = golem.getHealth();
        golem.hurt(helper.getLevel().damageSources().generic(), 100.0F);
        helper.assertTrue(before - golem.getHealth() <= 35.0F + 0.01F, "a blow is held to thirty-five");
        golem.setHealth(10.0F);
        golem.invulnerableTime = 0;
        golem.hurt(helper.getLevel().damageSources().generic(), 30.0F);
        helper.assertTrue(golem.isAlive() && golem.isHeadless(), "it lost its head, not its life");
        golem.discard();
        helper.succeed();
    }
}
