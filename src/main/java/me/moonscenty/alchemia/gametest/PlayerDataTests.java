package me.moonscenty.alchemia.gametest;

import java.util.HashSet;
import java.util.Set;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.player.WarpData;
import me.moonscenty.alchemia.player.WarpHandler;
import me.moonscenty.alchemia.player.effect.ModEffects;
import me.moonscenty.alchemia.registry.ModAspects;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class PlayerDataTests {
    private static final String TEMPLATE = "empty_32x40x32";

    @GameTest(template = TEMPLATE)
    public static void newPlayersKnowThePrimals(GameTestHelper helper) {
        PlayerKnowledge fresh = PlayerKnowledge.fresh();
        helper.assertTrue(fresh.discoveredAspects().size() == 6, "a new player should know the six primals, knew " + fresh.discoveredAspects().size());
        helper.assertTrue(fresh.knows(ModAspects.AIR), "aer is primal and should be known");
        helper.assertTrue(!fresh.knows(ModAspects.MAN), "humanus is compound and should not be");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void discoveringAnAspectSticks(GameTestHelper helper) {
        PlayerKnowledge knowledge = PlayerKnowledge.fresh().withAspect(ModAspects.SOUL);
        helper.assertTrue(knowledge.knows(ModAspects.SOUL), "spiritus should be known once discovered");
        helper.assertTrue(knowledge.withAspect(ModAspects.SOUL) == knowledge, "discovering it twice should change nothing");

        // scanning an item takes in everything it is made of at once
        AspectList scanned = AspectList.EMPTY.add(ModAspects.METAL, 4).add(ModAspects.TOOL, 2);
        PlayerKnowledge after = knowledge.withAspects(scanned);
        helper.assertTrue(after.knows(ModAspects.METAL) && after.knows(ModAspects.TOOL), "a scan should teach every aspect it found");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void knowledgeSurvivesSavingAndLoading(GameTestHelper helper) {
        PlayerKnowledge original = PlayerKnowledge.fresh().withAspect(ModAspects.FLUX).withResearch(Alchemia.id("basics"));

        Tag saved = PlayerKnowledge.CODEC.encodeStart(NbtOps.INSTANCE, original).getOrThrow();
        PlayerKnowledge loaded = PlayerKnowledge.CODEC.parse(NbtOps.INSTANCE, saved).getOrThrow();

        helper.assertTrue(loaded.discoveredAspects().size() == 7, "seven aspects should have come back, got " + loaded.discoveredAspects().size());
        helper.assertTrue(loaded.knows(ModAspects.FLUX), "vitium should have survived the trip");
        helper.assertTrue(loaded.hasResearch(Alchemia.id("basics")), "the research should have survived too");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void warpAddsUpAndWindsTheCounter(GameTestHelper helper) {
        WarpData warp = WarpData.NONE.add(WarpData.Kind.PERMANENT, 3).add(WarpData.Kind.TEMPORARY, 5);
        helper.assertTrue(warp.total() == 8, "total warp should be 8, was " + warp.total());
        helper.assertTrue(warp.lasting() == 3, "only the permanent warp lasts, was " + warp.lasting());
        helper.assertTrue(warp.counter() == 8, "gaining warp should wind the counter to 8, was " + warp.counter());

        // spending warp must not wind the counter, and must not dig below zero
        WarpData spent = warp.add(WarpData.Kind.TEMPORARY, -99);
        helper.assertTrue(spent.temporary() == 0, "temporary warp should have bottomed out at 0, was " + spent.temporary());
        helper.assertTrue(spent.counter() == 8, "spending warp should leave the counter alone, was " + spent.counter());

        Tag saved = WarpData.CODEC.encodeStart(NbtOps.INSTANCE, warp).getOrThrow();
        helper.assertTrue(WarpData.CODEC.parse(NbtOps.INSTANCE, saved).getOrThrow().equals(warp), "warp changed on the way through nbt");
        helper.succeed();
    }

    /**
     * A player carrying warp should eventually have something come of it, and the counter should unwind as it does.
     * The roll is kept separate from the player so it can be exercised without a real one.
     */
    @GameTest(template = TEMPLATE)
    public static void warpEventuallyCatchesUp(GameTestHelper helper) {
        RandomSource random = RandomSource.create(1234);
        WarpData warp = WarpData.NONE.add(WarpData.Kind.PERMANENT, 60);

        int rolls = 0;
        while (rolls < 500 && !WarpHandler.triggers(warp, random)) {
            rolls++;
        }
        helper.assertTrue(rolls < 500, "500 rolls at 60 warp should have brought something on");
        Alchemia.LOGGER.info("warp struck after {} rolls at counter {}", rolls, warp.counter());

        WarpData settled = WarpHandler.afterEvent(warp);
        helper.assertTrue(settled.counter() < warp.counter(), "the counter should unwind once an event fires");
        helper.assertTrue(settled.permanent() == 60, "permanent warp never leaves");
        helper.succeed();
    }

    /** No warp means no trouble, however many times the dice are rolled. */
    @GameTest(template = TEMPLATE)
    public static void aClearConscienceIsNeverTroubled(GameTestHelper helper) {
        RandomSource random = RandomSource.create(99);
        for (int roll = 0; roll < 500; roll++) {
            helper.assertTrue(!WarpHandler.triggers(WarpData.NONE, random), "a player with no warp should never be troubled");
        }

        // warp that has already been paid off leaves a counter but no warp, which also must not fire
        WarpData paidOff = WarpData.NONE.add(WarpData.Kind.TEMPORARY, 10).add(WarpData.Kind.TEMPORARY, -10);
        helper.assertTrue(paidOff.total() == 0, "the warp should be gone");
        for (int roll = 0; roll < 500; roll++) {
            helper.assertTrue(!WarpHandler.triggers(paidOff, random), "warp that is gone should not still bite");
        }
        helper.succeed();
    }

    /** Severity climbs with warp, so the nastier outcomes only come to the deeply warped. */
    @GameTest(template = TEMPLATE)
    public static void severityClimbsWithWarp(GameTestHelper helper) {
        int mild = WarpHandler.severityOf(WarpData.NONE.add(WarpData.Kind.TEMPORARY, 5));
        int dire = WarpHandler.severityOf(WarpData.NONE.add(WarpData.Kind.PERMANENT, 80));
        Alchemia.LOGGER.info("severity: 5 warp -> {}, 80 warp -> {}", mild, dire);

        helper.assertTrue(mild < dire, "more warp should mean worse outcomes");
        helper.assertTrue(dire <= 100, "severity should stay capped at 100, was " + dire);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void everyAilmentIsRegistered(GameTestHelper helper) {
        helper.assertTrue(ModEffects.ailments().size() == 7, "there should be seven ailments plus the ward");
        for (var ailment : ModEffects.ailments()) {
            helper.assertTrue(ailment.value().getCategory() == MobEffectCategory.HARMFUL, ailment.getId() + " should be harmful");
        }
        helper.assertTrue(ModEffects.WARP_WARD.value().getCategory() == MobEffectCategory.BENEFICIAL,
                "the ward is the one good thing here");
        helper.succeed();
    }

    /**
     * Every rung of the warp ladder has to land somewhere sensible. This walks the whole range, which is what would
     * have caught the two rungs that were originally mis-numbered.
     */
    @GameTest(template = TEMPLATE)
    public static void everyWarpRollLandsSomewhere(GameTestHelper helper) {
        Set<Holder<MobEffect>> seen = new HashSet<>();
        for (int roll = 0; roll <= 100; roll++) {
            WarpHandler.Outcome outcome = WarpHandler.outcomeFor(60, roll);
            helper.assertTrue(outcome.message() >= 0 && outcome.message() <= 14,
                    "roll " + roll + " pointed at whisper " + outcome.message() + ", which does not exist");
            outcome.effect().ifPresent(effect -> {
                helper.assertTrue(outcome.duration() > 0, "an ailment with no duration is no ailment");
                seen.add(effect);
            });
        }

        // the mild end must stay harmless and the deep end must not
        helper.assertTrue(WarpHandler.outcomeFor(60, 0).effect().isEmpty(), "the mildest roll should only whisper");
        helper.assertTrue(WarpHandler.outcomeFor(60, 16).effect().isPresent(), "roll 16 should bring the flux flu on");
        Alchemia.LOGGER.info("the warp ladder can inflict {} different effects", seen.size());
        helper.assertTrue(seen.size() >= 8, "the ladder should reach at least eight different effects, reached " + seen.size());
        helper.succeed();
    }

    /** How hard an ailment bites should follow the warp, not the roll that picked it. */
    @GameTest(template = TEMPLATE)
    public static void deeperWarpBitesHarder(GameTestHelper helper) {
        WarpHandler.Outcome mild = WarpHandler.outcomeFor(10, 16);
        WarpHandler.Outcome dire = WarpHandler.outcomeFor(90, 16);
        helper.assertTrue(mild.effect().equals(dire.effect()), "the same roll should pick the same ailment");
        helper.assertTrue(dire.amplifier() > mild.amplifier(),
                "more warp should mean a stronger dose, was " + mild.amplifier() + " then " + dire.amplifier());
        helper.assertTrue(dire.amplifier() <= 3, "the dose should stay capped at 3, was " + dire.amplifier());
        helper.succeed();
    }
}
