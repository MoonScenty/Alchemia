package me.moonscenty.alchemia.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

/**
 * A creature of the taint. They never turn on one another, and count one another as their own side.
 */
public interface TaintedMob {
    /** Whether a tainted creature would ever go for this kind of thing: anything but another of its own. */
    static boolean mayAttack(EntityType<?> type, Entity sample) {
        return !(sample instanceof TaintedMob);
    }

    /** Whether this is on the taint's side. */
    static boolean allied(Entity other) {
        return other instanceof TaintedMob;
    }
}
