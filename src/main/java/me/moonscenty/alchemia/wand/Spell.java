package me.moonscenty.alchemia.wand;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

/**
 * What a focus does when the wand it is fitted to is pointed at something and used.
 *
 * <p>This runs on the server and nowhere else. Whatever a spell wants seen -- a bolt, a beam, a shower of
 * sparks -- it sends out as particles or a sound, so that everybody watching sees the same thing rather than only
 * the one casting it.
 *
 * <p>Whoever is casting need not be a player. An autocaster is a machine with a focus bolted into it, and
 * the seven foci it will take are the ones that ask nothing of the caster but where they are pointed. The
 * rest look at a bag or at what somebody is standing on, and hand back nothing when there is nobody
 * holding them.
 *
 * <p>A spell says whether anything happened. Nothing happening is not a failure: a shock aimed at empty air is a
 * shock that found nothing to shock. But it is not paid for either, so a wand is never emptied into the sky.
 */
@FunctionalInterface
public interface Spell {
    /**
     * Casts it.
     *
     * @param aim what the wand is pointed at: an entity if one is in the way, else the block behind it, else
     *            nothing but the air at the end of the caster's reach
     * @param heldFor how many ticks the wand has been held down, or zero for a spell let off all at once
     * @return whether anything came of it, which is also whether it is paid for
     */
    boolean cast(ServerLevel level, LivingEntity caster, ItemStack wand, HitResult aim, int heldFor);

    /** A focus that does nothing yet. Every one of them began here. */
    Spell NOTHING = (level, caster, wand, aim, heldFor) -> false;
}
