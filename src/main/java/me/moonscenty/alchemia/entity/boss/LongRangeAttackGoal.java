package me.moonscenty.alchemia.entity.boss;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;

// Shoots only beyond a minimum range; nearer, the melee goal takes over.
public class LongRangeAttackGoal extends RangedAttackGoal {
    private final Mob mob;
    private final double nearest;
    // checked only before starting, so a target closing in mid-chase is still shot at
    private final boolean onlyToStart;

    public <T extends Mob & RangedAttackMob> LongRangeAttackGoal(T mob, double nearest, double speed, int fastest,
            int slowest, float range) {
        this(mob, nearest, false, speed, fastest, slowest, range);
    }

    private <T extends Mob & RangedAttackMob> LongRangeAttackGoal(T mob, double nearest, boolean onlyToStart,
            double speed, int fastest, int slowest, float range) {
        super(mob, speed, fastest, slowest, range);
        this.mob = mob;
        this.nearest = nearest;
        this.onlyToStart = onlyToStart;
    }

    public static <T extends Mob & RangedAttackMob> LongRangeAttackGoal toStart(T mob, double nearest, double speed,
            int fastest, int slowest, float range) {
        return new LongRangeAttackGoal(mob, nearest, true, speed, fastest, slowest, range);
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
        return super.canContinueToUse() && (onlyToStart || farEnough());
    }
}
