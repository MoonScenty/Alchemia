package me.moonscenty.alchemia.wand;

import java.util.function.Supplier;

import com.mojang.serialization.Codec;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.registry.ModAspects;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

/**
 * Something worked into a focus at a manipulator, which the focus then carries for good.
 *
 * <p>Twenty-three of them. Nine are worth having on more than one focus -- a cheaper working, a harder one, a
 * wider one -- and the other fourteen belong to one focus apiece and change what it does rather than how much of
 * it there is. Which of them a focus will take is settled rank by rank in {@link FocusUpgrades}.
 *
 * <p>What is here is only what an upgrade <em>is</em>: its name, and the aspects its price is quoted in. How much
 * it costs depends on which rank it is going into, and that is the manipulator's arithmetic.
 *
 * <p>An enum rather than a registry, which is the one place this parts from the original. The original let an
 * addon make a new one because every focus was its own class and could answer for its own upgrades; here the
 * twelve foci are one item and what each upgrade does is written against that item, so a twenty-fourth would have
 * nowhere to say what it did.
 */
public enum FocusUpgrade implements StringRepresentable {
    // --- the nine that go on more than one focus -------------------------------------------------------
    /** Harder. About a fifth more per rank, though what that means is each focus's own business. */
    POTENCY("potency", () -> AspectList.of(ModAspects.AVERSION, 1)),
    /** Cheaper, by a tenth of the bill per rank. */
    FRUGAL("frugal", () -> AspectList.of(ModAspects.DESIRE, 1)),
    /** More out of what is broken, the way fortune and looting give more. */
    TREASURE("treasure", () -> AspectList.of(ModAspects.DESIRE, 1)),
    /** Wider, or more of them at once. */
    ENLARGE("enlarge", () -> AspectList.of(ModAspects.MOTION, 1)),
    /** Longer, for whatever the focus leaves behind on what it hits. */
    EXTEND("extend", () -> AspectList.of(ModAspects.EXCHANGE, 1)),
    /** The block comes up as it stood, rather than as what it breaks into. */
    SILK_TOUCH("silk_touch", () -> AspectList.of(ModAspects.DESIRE, 1)),
    /** The square it will work on is shown, and can be set rather than guessed. */
    ARCHITECT("architect", () -> AspectList.of(ModAspects.CRAFT, 1)),
    /** Whatever it sets alight burns far longer. */
    ALCHEMISTS_FIRE("alchemists_fire", () -> AspectList.of(ModAspects.ENERGY, 1).add(ModAspects.WATER, 1)),
    /** Whatever it touches is chilled as well as hurt. */
    ALCHEMISTS_FROST("alchemists_frost", () -> AspectList.of(ModAspects.COLD, 1).add(ModAspects.TRAP, 1)),

    // --- the fourteen that belong to one focus apiece --------------------------------------------------
    /** Fire: a ball to throw instead of a spray to hold down. */
    FIREBALL("fireball", () -> AspectList.of(ModAspects.DARKNESS, 1)),
    /** Fire: the spray drawn into a line that burns through whatever stands in it. */
    FIREBEAM("firebeam", () -> AspectList.of(ModAspects.ENERGY, 1).add(ModAspects.AIR, 1)),
    /** Frost: a volley of splinters instead of one spike. */
    SCATTERSHOT("scattershot", () -> AspectList.of(ModAspects.COLD, 1).add(ModAspects.AVERSION, 1)),
    /** Frost: one boulder, which bounces. */
    ICE_BOULDER("ice_boulder", () -> AspectList.of(ModAspects.COLD, 1).add(ModAspects.CRYSTAL, 1)),
    /** Shock: slower, harder, and it jumps to what is standing nearby. */
    CHAIN_LIGHTNING("chain_lightning", () -> AspectList.of(ModAspects.ENERGY, 1)),
    /** Shock: a ball lobbed at the ground, which leaves the ground live. */
    EARTH_SHOCK("earth_shock", () -> AspectList.of(ModAspects.ENERGY, 1)),
    /** Nine Hells: the bats always burst, and harder. */
    BAT_BOMBS("bat_bombs", () -> AspectList.of(ModAspects.ENERGY, 1).add(ModAspects.TRAP, 1)),
    /** Nine Hells: bigger bats that never burst and keep at it. */
    DEVIL_BATS("devil_bats", () -> AspectList.of(ModAspects.PROTECT, 1)),
    /** Nine Hells: bats that drink, and hand some of it back. */
    VAMPIRE_BATS("vampire_bats", () -> AspectList.of(ModAspects.DESIRE, 1).add(ModAspects.LIFE, 1)),
    /** Pech's curse: all three at once rather than one of them. */
    NIGHTSHADE("nightshade", () -> AspectList.of(ModAspects.LIFE, 1).add(ModAspects.FLUX, 1)
            .add(ModAspects.ENERGY, 1)),
    /** Primal: the orb goes straight at something instead of wandering. */
    SEEKER("seeker", () -> AspectList.of(ModAspects.SENSES, 1).add(ModAspects.MIND, 1)),
    /** Vis shard: when its mark is gone it finds another of the same kind. */
    PERSISTENT("persistent", () -> AspectList.of(ModAspects.PROTECT, 1).add(ModAspects.AVERSION, 1)
            .add(ModAspects.ENERGY, 1)),
    /** Excavation: ore sometimes comes up as a cluster, which smelts to twice as much. */
    DOWSING("dowsing", () -> AspectList.of(ModAspects.CRAFT, 1)),
    /** Grapple: it does not let go on arrival; letting go is the climber's business. */
    STICKY("sticky", () -> AspectList.of(ModAspects.TRAP, 1));

    public static final Codec<FocusUpgrade> CODEC = StringRepresentable.fromEnum(FocusUpgrade::values);
    public static final StreamCodec<io.netty.buffer.ByteBuf, FocusUpgrade> STREAM_CODEC =
            ByteBufCodecs.idMapper(which -> values()[which], FocusUpgrade::ordinal);

    private final String name;
    private final Supplier<AspectList> aspects;

    FocusUpgrade(String name, Supplier<AspectList> aspects) {
        this.name = name;
        this.aspects = aspects;
    }

    /**
     * What its price is quoted in, one of each.
     * <p>
     * Worked out when asked rather than when this class is read: the aspects are registry entries and this enum
     * is read while the registries are still being filled.
     */
    public AspectList aspects() {
        return aspects.get();
    }

    /** Its small picture, which the original kept in a folder of its own. */
    public ResourceLocation icon() {
        return Alchemia.id("textures/gui/focus_upgrade/" + name + ".png");
    }

    public String translationKey() {
        return "focus_upgrade." + Alchemia.MODID + "." + name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
