package me.moonscenty.alchemia.entity.boss;

import me.moonscenty.alchemia.entity.ArrowAttackGoal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.RangedAttackMob;

// Starts only beyond a minimum range; once started it keeps on while a path remains, even if the target closes in.
public class LongRangeAttackGoal extends ArrowAttackGoal {
    private final double nearest;

    public <T extends Mob & RangedAttackMob> LongRangeAttackGoal(T mob, double nearest, double speed, int fastest,
            int slowest, float range) {
        super(mob, speed, fastest, slowest, range);
        this.nearest = nearest;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        if (target != null && !target.isAlive()) {
            mob.setTarget(null);
        }
        return super.canUse() && mob.distanceToSqr(target.getX(), target.getBoundingBox().minY,
                target.getZ()) >= nearest * nearest;
    }
}
