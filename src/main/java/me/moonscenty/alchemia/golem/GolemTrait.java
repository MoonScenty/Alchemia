package me.moonscenty.alchemia.golem;

import java.util.Locale;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.resources.ResourceLocation;

// Paired traits cancel: whichever comes second removes the first and is not kept itself.
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

    public ResourceLocation icon() {
        return Alchemia.id("textures/gui/golem/tag_" + key() + ".png");
    }
}
