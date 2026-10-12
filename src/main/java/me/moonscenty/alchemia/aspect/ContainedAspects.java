package me.moonscenty.alchemia.aspect;

import net.minecraft.world.item.ItemStack;

/**
 * An item that holds essentia, which it is made of as well as whatever it is itself made of: the original counted
 * what such a thing holds into what it is worth, in a crucible and to a scan.
 */
public interface ContainedAspects {
    /** What this stack holds. */
    AspectList contained(ItemStack stack);
}
