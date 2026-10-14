package me.moonscenty.alchemia.entity.boss;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;

/**
 * Shooting, as the game's own goal does, but only at what is at least so far away: nearer than that, it is left to
 * whatever fights hand to hand.
 */
public class LongRangeAttackGoal extends RangedAttackGoal {
    private final Mob mob;
    private final double nearest;

    public <T extends Mob & RangedAttackMob> LongRangeAttackGoal(T mob, double nearest, double speed, int fastest,
            int slowest, float range) {
        super(mob, speed, fastest, slowest, range);
        this.mob = mob;
        this.nearest = nearest;
    }

    private boolean farEnough() {
        LivingEntity target = mob.getTarget();
        if (target == null) {
            return false;
        }
        if (!target.isAlive()) {
            mob.setTarget(null);
            return false;
        }
        return mob.distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ()) >= nearest * nearest;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && farEnough();
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && farEnough();
    }
}
