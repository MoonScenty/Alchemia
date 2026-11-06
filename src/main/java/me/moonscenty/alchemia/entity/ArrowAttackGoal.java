package me.moonscenty.alchemia.entity;

import java.util.EnumSet;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;

// The 1.8 ranged goal: never shoots beyond its range, and stands to aim only after a second in sight.
public class ArrowAttackGoal extends Goal {
    private static final int AIMS_AFTER = 20;

    protected final Mob mob;
    private final RangedAttackMob shooter;
    private final double speed;
    private final int fastest;
    private final int slowest;
    private final float range;
    private float turns = 30.0F;
    private LivingEntity target;
    private int seen;
    private int reload = -1;

    public <T extends Mob & RangedAttackMob> ArrowAttackGoal(T mob, double speed, int fastest, int slowest,
            float range) {
        this.mob = mob;
        this.shooter = mob;
        this.speed = speed;
        this.fastest = fastest;
        this.slowest = slowest;
        this.range = range;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public ArrowAttackGoal turning(float turns) {
        this.turns = turns;
        return this;
    }

    @Override
    public boolean canUse() {
        LivingEntity found = mob.getTarget();
        if (found == null || !found.isAlive()) {
            return false;
        }
        target = found;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse() || target != null && target.isAlive() && !mob.getNavigation().isDone();
    }

    @Override
    public void stop() {
        target = null;
        seen = 0;
        reload = -1;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        double distance = mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        boolean sees = mob.getSensing().hasLineOfSight(target);
        seen = sees ? seen + 1 : 0;
        if (distance <= range * range && seen >= AIMS_AFTER) {
            mob.getNavigation().stop();
        } else {
            mob.getNavigation().moveTo(target, speed);
        }
        mob.getLookControl().setLookAt(target, turns, 30.0F);
        float far = (float) Math.sqrt(distance) / range;
        if (--reload == 0) {
            if (distance > range * range || !sees) {
                return;
            }
            shooter.performRangedAttack(target, Mth.clamp(far, 0.1F, 1.0F));
            reload = Mth.floor(far * (slowest - fastest) + fastest);
        } else if (reload < 0) {
            reload = Mth.floor(far * (slowest - fastest) + fastest);
        }
    }
}
