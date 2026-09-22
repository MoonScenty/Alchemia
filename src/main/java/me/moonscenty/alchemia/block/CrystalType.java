package me.moonscenty.alchemia.block;

import java.util.Locale;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.registry.ModAspects;
import net.minecraft.core.Holder;

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

    /** The aspect a crystal of this kind is set from, and gives back when it wastes away. */
    public Holder<Aspect> aspect() {
        return switch (this) {
            case AIR -> ModAspects.AIR;
            case FIRE -> ModAspects.FIRE;
            case WATER -> ModAspects.WATER;
            case EARTH -> ModAspects.EARTH;
            case ORDER -> ModAspects.ORDER;
            case ENTROPY -> ModAspects.ENTROPY;
            case FLUX -> ModAspects.FLUX;
        };
    }

    /** Flux crystals only appear through corruption, never through world generation. */
    public boolean generatesNaturally() {
        return this != FLUX;
    }
}
