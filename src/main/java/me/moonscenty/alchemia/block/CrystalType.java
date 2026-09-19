package me.moonscenty.alchemia.block;

import java.util.Locale;

/**
 * The kinds of vis crystal. Each one corresponds to a primal aspect, except {@link #FLUX} which is the tainted form.
 */
public enum CrystalType {
    AIR,
    FIRE,
    WATER,
    EARTH,
    ORDER,
    ENTROPY,
    FLUX;

    public String getName() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** Flux crystals only appear through corruption, never through world generation. */
    public boolean generatesNaturally() {
        return this != FLUX;
    }
}
