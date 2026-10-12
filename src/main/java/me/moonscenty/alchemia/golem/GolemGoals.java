package me.moonscenty.alchemia.golem;

import java.util.EnumSet;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.golem.seal.SealEntity;
import me.moonscenty.alchemia.golem.seal.Seals;
import me.moonscenty.alchemia.golem.task.Task;
import me.moonscenty.alchemia.golem.task.Tasks;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.pathfinder.Path;

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

    /**
     * Going to do a task for a seal. Every quarter second, when it has nothing in hand, it looks for the nearest task
     * it may do that it can get to, takes it, and heads for it. Near enough, it does it; if the seal says it is not
     * done yet it waits half a second and tries again. Stuck on the way for two seconds it wanders a little towards
     * the task and tries again, and after fifty seconds of not getting there it gives up.
     */
    abstract static class Goto extends Goal {
        protected final Golem golem;
        protected int counter = -1;
        protected double reach = 4.0;
        @Nullable
        protected BlockPos target;
        private int cooldown;
        private int pause;
        @Nullable
        private BlockPos lastStop;

        Goto(Golem golem) {
            this.golem = golem;
            setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            if (cooldown > 0) {
                cooldown--;
                return false;
            }
            cooldown = 5;
            if (golem.task() != null && !golem.task().suspended()) {
                return false;
            }
            target = null;
            boolean found = find();
            Task task = golem.task();
            if (found && task != null && golem.level() instanceof ServerLevel level) {
                SealEntity seal = Seals.get(level, task.seal());
                if (seal != null) {
                    seal.seal().onTaskStarted(level, golem, task);
                }
            }
            return found;
        }

        @Override
        public void start() {
            moveTo();
            counter = 0;
        }

        @Override
        public boolean canContinueToUse() {
            Task task = golem.task();
            return counter >= 0 && counter <= 1000 && task != null && !task.suspended();
        }

        @Override
        public void tick() {
            Task task = golem.task();
            if (task == null || pause-- > 0) {
                return;
            }
            if (distance(task) > reach) {
                task.setCompletion(false);
                counter++;
                if (counter % 40 == 0) {
                    if (golem.blockPosition().equals(lastStop)) {
                        Vec3 towards = DefaultRandomPos.getPosTowards(golem, 6, 4, Vec3.atLowerCornerOf(task.pos()),
                                Math.PI / 2.0);
                        if (towards != null) {
                            golem.getNavigation().moveTo(towards.x + 0.5, towards.y + 0.5, towards.z + 0.5,
                                    golem.moveSpeed());
                        }
                    } else {
                        moveTo();
                    }
                    lastStop = golem.blockPosition();
                }
            } else {
                Tasks.complete(task, golem);
                if (golem.task() != null && golem.task().completed()) {
                    if (counter >= 0) {
                        counter = 0;
                    }
                    pause = 0;
                } else {
                    pause = 10;
                    counter++;
                }
                counter--;
            }
        }

        @Override
        public void stop() {
            Task task = golem.task();
            if (task == null) {
                return;
            }
            if (!task.completed() && task.reserved()) {
                golem.level().broadcastEntityEvent(golem, (byte) 6);
            }
            if (task.completed() && !task.suspended()) {
                task.setSuspended(true);
            }
            task.setReserved(false);
        }

        /** How far it still has to go, squared. */
        protected abstract double distance(Task task);

        protected abstract void moveTo();

        protected abstract boolean find();

        /** Whether it may do a task at all, before asking whether it can get there. */
        protected boolean mayDo(Task task) {
            return golem.mayWork(Seals.get(golem.level(), task.seal())) && task.canBeDoneBy(golem);
        }

        /** Takes a task on. */
        protected void take(Task task) {
            golem.setTask(task);
            task.setReserved(true);
            golem.level().broadcastEntityEvent(golem, Golem.FOLLOWS);
        }
    }

    /** Going to a block for a seal: to whichever open side of it is nearest. */
    public static class GotoBlock extends Goto {
        public GotoBlock(Golem golem) {
            super(golem);
        }

        @Override
        public void tick() {
            super.tick();
            Task task = golem.task();
            if (task != null) {
                golem.getLookControl().setLookAt(task.pos().getX() + 0.5, task.pos().getY() + 0.5,
                        task.pos().getZ() + 0.5, 10.0F, golem.getMaxHeadXRot());
            }
        }

        @Override
        protected double distance(Task task) {
            BlockPos to = target == null ? task.pos() : target;
            return golem.distanceToSqr(Vec3.atLowerCornerOf(to));
        }

        @Override
        protected void moveTo() {
            Task task = golem.task();
            if (task == null) {
                return;
            }
            BlockPos to = target != null ? target : task.pos();
            golem.getNavigation().moveTo(to.getX() + 0.5, to.getY() + 0.5, to.getZ() + 0.5, golem.moveSpeed());
        }

        @Override
        protected boolean find() {
            for (Task task : Tasks.sortedFor(golem, Task.Kind.BLOCK)) {
                if (mayDo(task) && golem.isWithinRestriction(task.pos()) && reachable(task.pos())) {
                    target = beside(task.pos());
                    take(task);
                    return true;
                }
            }
            return false;
        }

        /** The open side of a block nearest the golem, if any side is open. */
        @Nullable
        private BlockPos beside(BlockPos pos) {
            double best = Double.MAX_VALUE;
            BlockPos nearest = null;
            for (Direction side : Direction.Plane.HORIZONTAL) {
                BlockPos at = pos.relative(side);
                if (!golem.level().getBlockState(at).blocksMotion()) {
                    double away = at.distToCenterSqr(golem.position());
                    if (away < best) {
                        best = away;
                        nearest = at;
                    }
                }
            }
            return nearest;
        }

        /** Whether it is close already, or can find a way to within a block and a half of it. */
        private boolean reachable(BlockPos pos) {
            if (golem.distanceToSqr(Vec3.atLowerCornerOf(pos)) < reach) {
                return true;
            }
            Path path = golem.getNavigation().createPath(pos, 0);
            if (path == null || path.getEndNode() == null) {
                return false;
            }
            BlockPos end = path.getEndNode().asBlockPos();
            return end.distSqr(pos) < 2.25;
        }
    }

    /** Going to something that moves for a seal, near enough to reach out and touch it. */
    public static class GotoEntity extends Goto {
        public GotoEntity(Golem golem) {
            super(golem);
        }

        @Override
        public void tick() {
            super.tick();
            Task task = golem.task();
            if (task != null && task.entity() != null) {
                golem.getLookControl().setLookAt(task.entity(), 10.0F, golem.getMaxHeadXRot());
            }
        }

        @Override
        protected double distance(Task task) {
            return task.entity() == null ? Double.MAX_VALUE : golem.distanceToSqr(task.entity());
        }

        @Override
        protected void moveTo() {
            Task task = golem.task();
            if (task != null && task.entity() != null) {
                golem.getNavigation().moveTo(task.entity(), golem.moveSpeed());
            }
        }

        @Override
        public boolean canContinueToUse() {
            Task task = golem.task();
            return super.canContinueToUse() && task.entity() != null && task.entity().isAlive();
        }

        @Override
        protected boolean find() {
            for (Task task : Tasks.sortedFor(golem, Task.Kind.ENTITY)) {
                Entity there = task.entity();
                if (there != null && there.isAlive() && mayDo(task) && golem.isWithinRestriction(there.blockPosition())
                        && reachable(there)) {
                    take(task);
                    float half = there.getBbWidth() / 2.0F;
                    reach = 3.5 + half * half;
                    return true;
                }
            }
            return false;
        }

        /** Whether it is close already, or can find a way to near enough it. */
        private boolean reachable(Entity there) {
            float half = there.getBbWidth() / 2.0F;
            double near = 3.5 + half * half;
            if (golem.distanceToSqr(there) < near) {
                return true;
            }
            Path path = golem.getNavigation().createPath(there, 0);
            if (path == null || path.getEndNode() == null) {
                return false;
            }
            int dx = path.getEndNode().x - net.minecraft.util.Mth.floor(there.getX());
            int dz = path.getEndNode().z - net.minecraft.util.Mth.floor(there.getZ());
            return dx * dx + dz * dz < near;
        }
    }
}
