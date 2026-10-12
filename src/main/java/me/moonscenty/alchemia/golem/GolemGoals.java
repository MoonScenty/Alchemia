package me.moonscenty.alchemia.golem;

import java.util.EnumSet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

/** What a golem gets up to on its own, as the original's golems did. */
public final class GolemGoals {
    private GolemGoals() {
    }

    /**
     * Keeps after its owner once it is ten blocks off, until it is two away; and if it is a dozen off and cannot
     * find a way, it simply turns up beside them, on any free ground in the ring round their feet.
     */
    public static class FollowOwner extends Goal {
        private final Golem golem;
        private final double speed;
        private final float from;
        private final float to;
        private LivingEntity owner;
        private int again;
        private float waterMalus;

        public FollowOwner(Golem golem, double speed, float from, float to) {
            this.golem = golem;
            this.speed = speed;
            this.from = from;
            this.to = to;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity master = golem.getOwner();
            if (master == null || master.isSpectator() || golem.distanceToSqr(master) < from * from) {
                return false;
            }
            owner = master;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return !golem.getNavigation().isDone() && golem.distanceToSqr(owner) > to * to;
        }

        @Override
        public void start() {
            again = 0;
            waterMalus = golem.getPathfindingMalus(PathType.WATER);
            golem.setPathfindingMalus(PathType.WATER, 0.0F);
        }

        @Override
        public void stop() {
            owner = null;
            golem.getNavigation().stop();
            golem.setPathfindingMalus(PathType.WATER, waterMalus);
        }

        @Override
        public void tick() {
            golem.getLookControl().setLookAt(owner, 10.0F, golem.getMaxHeadXRot());
            if (--again > 0) {
                return;
            }
            again = 10;
            if (golem.getNavigation().moveTo(owner, speed) || golem.isLeashed()
                    || golem.distanceToSqr(owner) < 144.0) {
                return;
            }
            BlockPos feet = owner.blockPosition();
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) < 2 && Math.abs(dz) < 2) {
                        continue;
                    }
                    BlockPos at = feet.offset(dx, 0, dz);
                    if (golem.level().getBlockState(at.below()).isFaceSturdy(golem.level(), at.below(),
                            net.minecraft.core.Direction.UP)
                            && !golem.level().getBlockState(at).isCollisionShapeFullBlock(golem.level(), at)
                            && !golem.level().getBlockState(at.above())
                                    .isCollisionShapeFullBlock(golem.level(), at.above())) {
                        golem.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, golem.getYRot(),
                                golem.getXRot());
                        golem.getNavigation().stop();
                        return;
                    }
                }
            }
        }
    }

    /**
     * Wanders back when it has strayed more than three blocks from home, looking every couple of seconds. From a
     * long way off it heads for somewhere on the way rather than straight there.
     */
    public static class GoHome extends Goal {
        private final Golem golem;
        private Vec3 to;
        private int idle = 10;

        public GoHome(Golem golem) {
            this.golem = golem;
            setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            if (idle > 0) {
                idle--;
                return false;
            }
            idle = 50;
            BlockPos home = golem.home();
            double away = golem.distanceToSqr(Vec3.atBottomCenterOf(home));
            if (away < 9.0) {
                return false;
            }
            if (away > 256.0) {
                to = DefaultRandomPos.getPosTowards(golem, 16, 7, Vec3.atBottomCenterOf(home), Math.PI / 2.0);
                return to != null;
            }
            to = Vec3.atBottomCenterOf(home);
            return true;
        }

        @Override
        public void start() {
            golem.getNavigation().moveTo(to.x, to.y, to.z, golem.moveSpeed());
        }

        @Override
        public boolean canContinueToUse() {
            return !golem.getNavigation().isDone()
                    && golem.distanceToSqr(Vec3.atBottomCenterOf(golem.home())) > 3.0;
        }

        @Override
        public void stop() {
            idle = 50;
        }
    }

    /** Turns on whatever has just hurt its owner. */
    public static class DefendOwner extends TargetGoal {
        private final Golem golem;
        private LivingEntity attacker;
        private int seen;

        public DefendOwner(Golem golem) {
            super(golem, false);
            this.golem = golem;
            setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            LivingEntity owner = golem.getOwner();
            if (owner == null) {
                return false;
            }
            attacker = owner.getLastHurtByMob();
            return owner.getLastHurtByMobTimestamp() != seen && canAttack(attacker, TargetingConditions.DEFAULT);
        }

        @Override
        public void start() {
            mob.setTarget(attacker);
            LivingEntity owner = golem.getOwner();
            if (owner != null) {
                seen = owner.getLastHurtByMobTimestamp();
            }
            super.start();
        }
    }

    /** Joins in on whatever its owner has just struck. */
    public static class AidOwner extends TargetGoal {
        private final Golem golem;
        private LivingEntity struck;
        private int seen;

        public AidOwner(Golem golem) {
            super(golem, false);
            this.golem = golem;
            setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            LivingEntity owner = golem.getOwner();
            if (owner == null) {
                return false;
            }
            struck = owner.getLastHurtMob();
            return owner.getLastHurtMobTimestamp() != seen && canAttack(struck, TargetingConditions.DEFAULT);
        }

        @Override
        public void start() {
            mob.setTarget(struck);
            LivingEntity owner = golem.getOwner();
            if (owner != null) {
                seen = owner.getLastHurtMobTimestamp();
            }
            super.start();
        }
    }
}
