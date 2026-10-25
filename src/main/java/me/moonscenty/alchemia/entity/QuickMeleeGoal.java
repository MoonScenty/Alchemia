package me.moonscenty.alchemia.entity;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

/**
 * A melee goal that swings every half second, the original's {@code AIAttackOnCollide}, where the game's own swings
 * every second. The original gave it to nearly everything of its own that fights hand to hand: the cult, the eldritch
 * crab, guardian, warden and golem, the pech, the tainted beasts and taintacles, and golems. Only the taint crawler
 * fought at the game's own pace.
 */
public class QuickMeleeGoal extends MeleeAttackGoal {
    /** Ticks between swings, the original's. */
    private static final int SWINGS_EVERY = 10;

    public QuickMeleeGoal(PathfinderMob mob, double speed, boolean followEvenIfUnseen) {
        super(mob, speed, followEvenIfUnseen);
    }

    @Override
    protected int getAttackInterval() {
        return adjustedTickDelay(SWINGS_EVERY);
    }
}
