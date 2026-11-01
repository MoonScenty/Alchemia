package me.moonscenty.alchemia.entity;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

// The original's AIAttackOnCollide: swings twice as often as vanilla's melee goal.
public class QuickMeleeGoal extends MeleeAttackGoal {
    // original's value
    private static final int SWINGS_EVERY = 10;

    public QuickMeleeGoal(PathfinderMob mob, double speed, boolean followEvenIfUnseen) {
        super(mob, speed, followEvenIfUnseen);
    }

    @Override
    protected int getAttackInterval() {
        return adjustedTickDelay(SWINGS_EVERY);
    }
}
