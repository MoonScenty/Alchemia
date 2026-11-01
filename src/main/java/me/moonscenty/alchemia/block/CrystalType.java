package me.moonscenty.alchemia.block;

import java.util.Locale;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.registry.ModAspects;
import net.minecraft.core.Holder;

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

    public boolean generatesNaturally() {
        return this != FLUX;
    }
}
