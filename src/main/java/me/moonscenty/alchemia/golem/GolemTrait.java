package me.moonscenty.alchemia.golem;

import java.util.Locale;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.resources.ResourceLocation;

/**
 * What a golem is like, as its parts make it: each part brings some of these, and a golem is the sum of them.
 *
 * <p>Three pairs cancel. A deft hand on a clumsy frame is neither, a light frame made heavy is neither, and so for
 * fragile and armoured: whichever comes second takes the first away and does not stay itself.
 */
public enum GolemTrait {
    SMART,
    DEFT,
    CLUMSY,
    FIGHTER,
    WHEELED,
    FLYER,
    CLIMBER,
    HEAVY,
    LIGHT,
    FRAGILE,
    REPAIR,
    SCOUT,
    ARMORED,
    BRUTAL,
    FIREPROOF,
    BREAKER,
    HAULER,
    RANGED,
    BLASTPROOF;

    private GolemTrait opposite;

    static {
        pair(CLUMSY, DEFT);
        pair(HEAVY, LIGHT);
        pair(FRAGILE, ARMORED);
    }

    private static void pair(GolemTrait one, GolemTrait other) {
        one.opposite = other;
        other.opposite = one;
    }

    /** The trait this one cancels, if it is one of a pair. */
    public GolemTrait opposite() {
        return opposite;
    }

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "golem." + Alchemia.MODID + ".trait." + key();
    }

    public String descriptionKey() {
        return translationKey() + ".text";
    }

    /** The little tag the press shows it by, the original's. */
    public ResourceLocation icon() {
        return Alchemia.id("textures/gui/golem/tag_" + key() + ".png");
    }
}
