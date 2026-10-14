package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.entity.Champions;
import me.moonscenty.alchemia.entity.cult.Cultist;
import me.moonscenty.alchemia.entity.cult.CultistCleric;
import me.moonscenty.alchemia.entity.cult.CultistKnight;
import me.moonscenty.alchemia.entity.cult.CultistPortal;
import me.moonscenty.alchemia.entity.cult.GolemOrb;
import me.moonscenty.alchemia.item.CrimsonRitesItem;
import me.moonscenty.alchemia.player.ModAttachments;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.player.WarpData;
import me.moonscenty.alchemia.player.WarpHandler;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.research.ModResearch;
import me.moonscenty.alchemia.research.NoteRequests;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The Crimson Cult: its people, its portal, its book. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class CultTests {
    private static final String TEMPLATE = "empty_32x40x32";

    private static <T extends Mob> T ordinary(GameTestHelper helper, T mob, BlockPos at) {
        mob.setData(ModAttachments.CHAMPION, Champions.ORDINARY);
        mob.moveTo(helper.absoluteVec(at.getBottomCenter()));
        mob.setNoAi(true);
        helper.getLevel().addFreshEntity(mob);
        return mob;
    }

    /** A knight comes dressed in the crimson plate with a sword; a cleric in the robe with nothing in hand. */
    @GameTest(template = TEMPLATE)
    public static void cultistsComeDressed(GameTestHelper helper) {
        var level = helper.getLevel();
        CultistKnight knight = ordinary(helper, new CultistKnight(level), new BlockPos(4, 2, 4));
        knight.finalizeSpawn(level, level.getCurrentDifficultyAt(knight.blockPosition()), MobSpawnType.EVENT, null);
        helper.assertTrue(knight.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.CRIMSON.get("crimson_plate_chest")),
                "the knight wears the plate");
        helper.assertTrue(!knight.getMainHandItem().isEmpty(), "and carries a sword");

        CultistCleric cleric = ordinary(helper, new CultistCleric(level), new BlockPos(8, 2, 4));
        cleric.finalizeSpawn(level, level.getCurrentDifficultyAt(cleric.blockPosition()), MobSpawnType.EVENT, null);
        helper.assertTrue(cleric.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.CRIMSON.get("crimson_robe_chest")),
                "the cleric wears the robe");
        helper.assertTrue(cleric.getMainHandItem().isEmpty(), "and carries nothing");
        helper.assertTrue(knight.isAlliedTo(cleric) && cleric.isAlliedTo(knight), "and they are of one side");
        helper.succeed();
    }

    /** Hurt one and every other within reach turns on whatever did it. */
    @GameTest(template = TEMPLATE, timeoutTicks = 40)
    public static void aHurtCultistCallsTheOthers(GameTestHelper helper) {
        var level = helper.getLevel();
        CultistKnight hurt = new CultistKnight(level);
        hurt.setData(ModAttachments.CHAMPION, Champions.ORDINARY);
        hurt.moveTo(helper.absoluteVec(new BlockPos(4, 2, 4).getBottomCenter()));
        level.addFreshEntity(hurt);
        CultistCleric friend = ordinary(helper, new CultistCleric(level), new BlockPos(12, 2, 12));
        Zombie attacker = ordinary(helper, EntityType.ZOMBIE.create(level), new BlockPos(6, 2, 4));
        hurt.hurt(level.damageSources().mobAttack(attacker), 1.0F);
        helper.succeedWhen(() -> helper.assertTrue(friend.getTarget() == attacker, "the far one turned on it too"));
    }

    /** A portal lets a cultist through, and pays for it out of its own health. */
    @GameTest(template = TEMPLATE)
    public static void aPortalLetsTheCultThroughAtACost(GameTestHelper helper) {
        var level = helper.getLevel();
        CultistPortal portal = new CultistPortal(level);
        portal.moveTo(helper.absoluteVec(new BlockPos(8, 2, 8).getBottomCenter()));
        level.addFreshEntity(portal);
        float before = portal.getHealth();
        portal.letThrough(level);
        AABB around = new AABB(helper.absolutePos(new BlockPos(8, 2, 8))).inflate(3.0);
        helper.assertValueEqual(level.getEntitiesOfClass(Cultist.class, around).size(), 1, "one came through");
        helper.assertTrue(before - portal.getHealth() >= 5.0F, "and the portal is the worse for it");
        helper.succeed();
    }

    /** The cleric's orb hits as hard as the cleric. */
    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void aClericsOrbHitsWhatItIsThrownAt(GameTestHelper helper) {
        var level = helper.getLevel();
        CultistCleric cleric = ordinary(helper, new CultistCleric(level), new BlockPos(4, 2, 4));
        Zombie target = ordinary(helper, EntityType.ZOMBIE.create(level), new BlockPos(10, 2, 4));
        float before = target.getHealth();
        GolemOrb orb = new GolemOrb(level, cleric, target, true);
        orb.moveTo(cleric.getX() + 1.0, cleric.getEyeY(), cleric.getZ());
        orb.setDeltaMovement(new Vec3(0.2, 0.0, 0.0));
        level.addFreshEntity(orb);
        helper.succeedWhen(() -> helper.assertTrue(target.getHealth() < before, "the orb found it"));
    }

    /** Reading the rites teaches the cult's hidden lore and leaves warp; the table cannot teach it. */
    @GameTest(template = TEMPLATE)
    public static void theRitesTeachWhatTheTableCannot(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        var entry = helper.getLevel().registryAccess().registryOrThrow(ModResearch.ENTRY_KEY)
                .get(CrimsonRitesItem.RESEARCH);
        helper.assertTrue(entry != null && entry.hidden(), "the cult's research is hidden");
        helper.assertFalse(NoteRequests.isReady(PlayerKnowledge.fresh(), CrimsonRitesItem.RESEARCH, entry),
                "and no note can be written for it");
        player.setItemInHand(InteractionHand.MAIN_HAND, ModItems.CRIMSON_RITES.toStack());
        ModItems.CRIMSON_RITES.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(PlayerKnowledge.of(player).hasResearch(CrimsonRitesItem.RESEARCH), "the reader knows it");
        helper.assertValueEqual(WarpData.of(player).of(WarpData.Kind.PERMANENT), 3, "and is marked for it");
        helper.assertTrue(player.getMainHandItem().is(ModItems.CRIMSON_RITES.get()), "and the book is still there");
        helper.succeed();
    }

    /** One of the deep rungs of warp opens a portal. */
    @GameTest(template = TEMPLATE)
    public static void warpCanOpenAPortal(GameTestHelper helper) {
        helper.assertTrue(WarpHandler.outcomeFor(60, 85).summons() == WarpHandler.Summons.PORTAL,
                "roll 85 calls up a portal");
        helper.succeed();
    }
}
