package me.moonscenty.alchemia.wand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.item.FocusItem;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModDataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import static me.moonscenty.alchemia.wand.FocusUpgrade.ALCHEMISTS_FIRE;
import static me.moonscenty.alchemia.wand.FocusUpgrade.ALCHEMISTS_FROST;
import static me.moonscenty.alchemia.wand.FocusUpgrade.ARCHITECT;
import static me.moonscenty.alchemia.wand.FocusUpgrade.BAT_BOMBS;
import static me.moonscenty.alchemia.wand.FocusUpgrade.CHAIN_LIGHTNING;
import static me.moonscenty.alchemia.wand.FocusUpgrade.DEVIL_BATS;
import static me.moonscenty.alchemia.wand.FocusUpgrade.DOWSING;
import static me.moonscenty.alchemia.wand.FocusUpgrade.EARTH_SHOCK;
import static me.moonscenty.alchemia.wand.FocusUpgrade.ENLARGE;
import static me.moonscenty.alchemia.wand.FocusUpgrade.EXTEND;
import static me.moonscenty.alchemia.wand.FocusUpgrade.FIREBALL;
import static me.moonscenty.alchemia.wand.FocusUpgrade.FIREBEAM;
import static me.moonscenty.alchemia.wand.FocusUpgrade.FRUGAL;
import static me.moonscenty.alchemia.wand.FocusUpgrade.ICE_BOULDER;
import static me.moonscenty.alchemia.wand.FocusUpgrade.NIGHTSHADE;
import static me.moonscenty.alchemia.wand.FocusUpgrade.PERSISTENT;
import static me.moonscenty.alchemia.wand.FocusUpgrade.POTENCY;
import static me.moonscenty.alchemia.wand.FocusUpgrade.SCATTERSHOT;
import static me.moonscenty.alchemia.wand.FocusUpgrade.SEEKER;
import static me.moonscenty.alchemia.wand.FocusUpgrade.SILK_TOUCH;
import static me.moonscenty.alchemia.wand.FocusUpgrade.STICKY;
import static me.moonscenty.alchemia.wand.FocusUpgrade.TREASURE;
import static me.moonscenty.alchemia.wand.FocusUpgrade.VAMPIRE_BATS;

/**
 * What is screwed into a focus: which upgrades it will take, what they cost, and what the focus becomes once
 * they are in it.
 *
 * <p>A focus has five ranks and takes one upgrade in each. Which ones are on offer depends on both the focus and
 * the rank, and the table below is the original's, cell for cell. The deeper ranks are where a focus is turned
 * into something else -- a spray of fire into a fireball, a bolt into a ball of lightning -- while the shallow
 * ones are the plain more-and-cheaper that nearly every focus will take.
 *
 * <p>Ranks are filled in order and never left with a gap, so what a focus carries is simply a list: the first
 * entry is rank one, and the length is how many ranks are spoken for.
 */
public final class FocusUpgrades {
    /** How many things one focus will hold. The original's five. */
    public static final int RANKS = 5;

    /** What a rank costs in levels, which is the same for every upgrade and goes up with the rank. */
    private static final int LEVELS_A_RANK = 6;
    /** What the first rank costs in vis, for one of each aspect the upgrade is quoted in. */
    private static final int VIS_AT_FIRST = 100;
    /** And how much dearer each rank after it is. */
    private static final double DEARER = 1.5;

    /** The nine that nearly everything takes, as the shallow ranks of most foci offer them. */
    private static final List<FocusUpgrade> PLAIN = List.of(FRUGAL, POTENCY);

    /**
     * Which upgrades each focus offers, rank by rank.
     * <p>
     * Keyed by what the focus is called rather than hung on {@link Focus}: it is a table and reads like one,
     * where sixty cells spread over a dozen registrations would not.
     */
    private static final Map<String, List<List<FocusUpgrade>>> OFFERED = offered();

    private FocusUpgrades() {
    }

    private static Map<String, List<List<FocusUpgrade>>> offered() {
        Map<String, List<List<FocusUpgrade>>> table = new LinkedHashMap<>();
        table.put("fire", List.of(
                PLAIN,
                List.of(FRUGAL, POTENCY, ALCHEMISTS_FIRE),
                List.of(FRUGAL, POTENCY, FIREBALL, FIREBEAM),
                List.of(FRUGAL, POTENCY, ALCHEMISTS_FIRE),
                PLAIN));
        table.put("frost", List.of(
                List.of(FRUGAL, POTENCY, ALCHEMISTS_FROST),
                PLAIN,
                List.of(FRUGAL, POTENCY, SCATTERSHOT, ICE_BOULDER, ALCHEMISTS_FROST),
                PLAIN,
                List.of(FRUGAL, POTENCY, ALCHEMISTS_FROST)));
        table.put("shock", List.of(
                PLAIN,
                PLAIN,
                List.of(FRUGAL, POTENCY, CHAIN_LIGHTNING, EARTH_SHOCK),
                List.of(FRUGAL, POTENCY, ENLARGE),
                List.of(FRUGAL, POTENCY, ENLARGE)));
        table.put("excavation", List.of(
                List.of(FRUGAL, POTENCY, TREASURE),
                List.of(FRUGAL, POTENCY, ENLARGE),
                List.of(FRUGAL, POTENCY, TREASURE, DOWSING),
                List.of(FRUGAL, POTENCY, ENLARGE),
                List.of(FRUGAL, POTENCY, TREASURE, SILK_TOUCH)));
        table.put("grapple", List.of(
                PLAIN,
                PLAIN,
                List.of(FRUGAL, POTENCY, STICKY),
                PLAIN,
                PLAIN));
        table.put("hellbat", List.of(
                PLAIN,
                PLAIN,
                List.of(FRUGAL, POTENCY, BAT_BOMBS, DEVIL_BATS),
                PLAIN,
                List.of(FRUGAL, POTENCY, VAMPIRE_BATS)));
        table.put("pech", List.of(
                PLAIN,
                List.of(FRUGAL, POTENCY, EXTEND),
                PLAIN,
                List.of(FRUGAL, POTENCY, EXTEND),
                List.of(FRUGAL, POTENCY, NIGHTSHADE)));
        List<FocusUpgrade> hole = List.of(FRUGAL, ENLARGE, EXTEND);
        table.put("hole", List.of(hole, hole, hole, hole, hole));
        List<FocusUpgrade> thrift = List.of(FRUGAL);
        table.put("primal", List.of(thrift, thrift, List.of(FRUGAL, SEEKER), thrift, thrift));
        table.put("shard", List.of(
                PLAIN, PLAIN, PLAIN, PLAIN,
                List.of(FRUGAL, POTENCY, PERSISTENT)));
        table.put("trade", List.of(
                List.of(FRUGAL, ENLARGE),
                List.of(FRUGAL, ENLARGE),
                List.of(FRUGAL, ENLARGE, TREASURE, ARCHITECT),
                List.of(FRUGAL, ENLARGE),
                List.of(FRUGAL, ENLARGE, SILK_TOUCH)));
        List<FocusUpgrade> builder = List.of(FRUGAL, ENLARGE);
        table.put("builder", List.of(builder, builder, builder, builder, builder));
        // unmodifiableMap rather than Map.copyOf: this is read in order wherever it is written out
        return Collections.unmodifiableMap(table);
    }

    // --- what a focus is carrying -----------------------------------------------------------------------

    /** What is in this focus, rank by rank. Empty for a focus nobody has worked on. */
    public static List<FocusUpgrade> on(ItemStack focus) {
        return focus.getOrDefault(ModDataComponents.FOCUS_UPGRADES.get(), List.of());
    }

    /** How many of one upgrade are in it, which is how many ranks were spent on the same thing. */
    public static int level(ItemStack focus, FocusUpgrade upgrade) {
        int found = 0;
        for (FocusUpgrade fitted : on(focus)) {
            if (fitted == upgrade) {
                found++;
            }
        }
        return found;
    }

    public static boolean has(ItemStack focus, FocusUpgrade upgrade) {
        return level(focus, upgrade) > 0;
    }

    /** Which rank the next thing would go into, or nought when there is no room left. */
    public static int nextRank(ItemStack focus) {
        int filled = on(focus).size();
        return filled >= RANKS ? 0 : filled + 1;
    }

    /** Screws one in, at whichever rank is next. Nothing happens to a focus that is already full. */
    public static void apply(ItemStack focus, FocusUpgrade upgrade) {
        List<FocusUpgrade> fitted = on(focus);
        if (fitted.size() >= RANKS) {
            return;
        }
        List<FocusUpgrade> more = new ArrayList<>(fitted);
        more.add(upgrade);
        focus.set(ModDataComponents.FOCUS_UPGRADES.get(), List.copyOf(more));
    }

    // --- what it will take, and what that costs ---------------------------------------------------------

    /** What this kind of focus offers at this rank. Empty when the rank is out of range or the focus is not one. */
    public static List<FocusUpgrade> offers(ItemStack focus, int rank) {
        if (rank < 1 || rank > RANKS || !(focus.getItem() instanceof FocusItem)) {
            return List.of();
        }
        List<List<FocusUpgrade>> ranks = OFFERED.get(kind(focus));
        return ranks == null ? List.of() : ranks.get(rank - 1);
    }

    /**
     * What a rank offers this one player: the same, less the vampire bats until they have studied them, the one
     * upgrade the original kept behind a research.
     */
    public static List<FocusUpgrade> offers(ItemStack focus, int rank, net.minecraft.world.entity.player.Player who) {
        List<FocusUpgrade> all = offers(focus, rank);
        if (!all.contains(FocusUpgrade.VAMPIRE_BATS) || who == null || me.moonscenty.alchemia.player.PlayerKnowledge
                .of(who).hasResearch(me.moonscenty.alchemia.Alchemia.id(VAMPIRE_RESEARCH))) {
            return all;
        }
        return all.stream().filter(upgrade -> upgrade != FocusUpgrade.VAMPIRE_BATS).toList();
    }

    /** The research the vampire bats are kept behind. */
    public static final String VAMPIRE_RESEARCH = "vampire_bats";

    /** Whether this focus would take this upgrade now, at whichever rank is next. */
    public static boolean takes(ItemStack focus, FocusUpgrade upgrade) {
        int rank = nextRank(focus);
        return rank > 0 && offers(focus, rank).contains(upgrade);
    }

    /**
     * What a rank costs in vis: a hundred of each aspect the upgrade is quoted in at the first rank, half as
     * much again at each rank after, and the whole lot broken back down into primals.
     */
    public static AspectList price(FocusUpgrade upgrade, int rank) {
        int each = VIS_AT_FIRST;
        for (int deeper = 1; deeper < rank; deeper++) {
            each = (int) (each * DEARER);
        }
        AspectList quoted = AspectList.EMPTY;
        for (var aspect : upgrade.aspects().sortedByName()) {
            quoted = quoted.add(aspect, each);
        }
        return quoted.toPrimals();
    }

    /** What a rank costs the one asking for it, in levels. */
    public static int levels(int rank) {
        return rank * LEVELS_A_RANK;
    }

    // --- what the focus becomes -------------------------------------------------------------------------

    /**
     * The focus as it actually is, once what is screwed into it is taken into account.
     * <p>
     * Most upgrades leave these four alone and change what the working does instead; the ones here are the ones
     * that turn a focus into a different focus. A spray of fire that has become a fireball is thrown rather than
     * held down, costs eight times as much and waits a second -- it is only still the fire focus by name.
     *
     * <p>The price of a hand's working is not here. Frugal is a discount at the till, not a different price.
     */
    public static Focus tuned(ItemStack focus) {
        if (!(focus.getItem() instanceof FocusItem item)) {
            return null;
        }
        Focus base = item.base();
        List<FocusUpgrade> fitted = on(focus);
        if (fitted.isEmpty()) {
            return base;
        }
        return switch (kind(focus)) {
            case "fire" -> fire(base, focus);
            case "frost" -> frost(base, focus);
            case "shock" -> shock(base, focus);
            case "excavation" -> excavation(base, focus);
            case "hellbat" -> hellbat(base, focus);
            case "pech" -> has(focus, NIGHTSHADE)
                    ? base.costing(AspectList.of(ModAspects.AIR, 1).add(ModAspects.FIRE, 1)
                            .add(ModAspects.EARTH, 1).add(ModAspects.ORDER, 1)
                            .add(ModAspects.ENTROPY, 1).add(ModAspects.WATER, 1))
                    : base;
            case "shard" -> has(focus, PERSISTENT)
                    ? base.costing(AspectList.of(ModAspects.FIRE, 1).add(ModAspects.ENTROPY, 1)
                            .add(ModAspects.WATER, 1).add(ModAspects.AIR, 1))
                    : base;
            case "trade" -> has(focus, SILK_TOUCH)
                    ? base.costing(base.cost().add(allButEarth()))
                    : base;
            default -> base;
        };
    }

    /** A fireball is thrown and waits; a beam is still held down but costs order to keep it straight. */
    private static Focus fire(Focus base, ItemStack focus) {
        if (has(focus, FIREBALL)) {
            return base.costing(AspectList.of(ModAspects.FIRE, 8).add(ModAspects.ENTROPY, 8))
                    .letGo().cooldown(1000).inTurrets(32.0F, 3.0F);
        }
        if (has(focus, FIREBEAM)) {
            return base.costing(AspectList.of(ModAspects.FIRE, 2).add(ModAspects.ORDER, 2))
                    .inTurrets(16.0F, 0.0F);
        }
        return base;
    }

    /** Either of frost's two takes twice as long to come back, and a volley flies flat enough to aim. */
    private static Focus frost(Focus base, ItemStack focus) {
        if (has(focus, SCATTERSHOT)) {
            return base.costing(AspectList.of(ModAspects.WATER, 2).add(ModAspects.FIRE, 1)
                            .add(ModAspects.ENTROPY, 1).add(ModAspects.AIR, 2))
                    .cooldown(500).inTurrets(20.0F, 1.0F);
        }
        if (has(focus, ICE_BOULDER)) {
            return base.costing(AspectList.of(ModAspects.WATER, 2).add(ModAspects.FIRE, 1)
                            .add(ModAspects.ENTROPY, 1).add(ModAspects.EARTH, 2))
                    .cooldown(500);
        }
        return base;
    }

    private static Focus shock(Focus base, ItemStack focus) {
        if (has(focus, CHAIN_LIGHTNING)) {
            return base.costing(AspectList.of(ModAspects.AIR, 8).add(ModAspects.WATER, 8)).cooldown(500);
        }
        if (has(focus, EARTH_SHOCK)) {
            return base.costing(AspectList.of(ModAspects.AIR, 8).add(ModAspects.EARTH, 8))
                    .cooldown(1000).inTurrets(20.0F, 5.0F);
        }
        return base;
    }

    /** Digging wider costs more earth; taking the block whole, or finding a cluster in it, costs other things. */
    private static Focus excavation(Focus base, ItemStack focus) {
        AspectList cost = base.cost().add(ModAspects.EARTH, level(focus, ENLARGE));
        if (has(focus, SILK_TOUCH)) {
            return base.costing(cost.add(allButEarth()));
        }
        if (has(focus, DOWSING)) {
            return base.costing(cost.add(ModAspects.FIRE, 1).add(ModAspects.ORDER, 1));
        }
        return base.costing(cost);
    }

    private static Focus hellbat(Focus base, ItemStack focus) {
        if (has(focus, BAT_BOMBS)) {
            return base.costing(AspectList.of(ModAspects.FIRE, 5).add(ModAspects.ENTROPY, 10)
                    .add(ModAspects.AIR, 5));
        }
        if (has(focus, DEVIL_BATS)) {
            return base.costing(AspectList.of(ModAspects.FIRE, 5).add(ModAspects.ENTROPY, 5)
                    .add(ModAspects.AIR, 5).add(ModAspects.EARTH, 5));
        }
        return base;
    }

    /** One of each of the other five, which is what the original charged for taking a block up whole. */
    private static AspectList allButEarth() {
        return AspectList.of(ModAspects.AIR, 1).add(ModAspects.FIRE, 1).add(ModAspects.WATER, 1)
                .add(ModAspects.ORDER, 1).add(ModAspects.ENTROPY, 1);
    }

    /** What a focus is called, which is its registry name without the word focus in front of it. */
    private static String kind(ItemStack focus) {
        return BuiltInRegistries.ITEM.getKey(focus.getItem()).getPath().replaceFirst("^focus_", "");
    }
}
