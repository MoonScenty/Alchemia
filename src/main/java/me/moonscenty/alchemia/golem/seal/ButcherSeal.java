package me.moonscenty.alchemia.golem.seal;

import java.util.List;

import me.moonscenty.alchemia.golem.Golem;
import me.moonscenty.alchemia.golem.GolemTrait;
import me.moonscenty.alchemia.golem.task.Task;
import me.moonscenty.alchemia.golem.task.Tasks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;

/**
 * Butcher: every ten seconds, if there are more than two grown, untamed animals of a kind in its area, sends a golem
 * after one of them, and waits until that one is dealt with before looking again. A herd is thinned, never ended.
 */
public class ButcherSeal extends Seal {
    private int delay;
    private boolean waiting;

    public ButcherSeal() {
        super("butcher", 0);
        this.delay = java.util.concurrent.ThreadLocalRandom.current().nextInt(200);
    }

    @Override
    public void tick(ServerLevel level, SealEntity seal) {
        if (delay++ % 200 != 0 || waiting) {
            return;
        }
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, seal.bounds())) {
            if (!wanted(target)) {
                continue;
            }
            long herd = level.getEntitiesOfClass(target.getClass(), seal.bounds()).stream()
                    .filter(ButcherSeal::wanted).limit(3).count();
            if (herd > 2) {
                Task task = new Task(seal.pos(), target);
                task.setPriority(seal.priority());
                task.setLifespan(10);
                Tasks.add(level, task);
                waiting = true;
                break;
            }
        }
    }

    /** A grown animal nobody has tamed, that is no monster and no golem. */
    private static boolean wanted(LivingEntity target) {
        boolean animal = target instanceof Animal || target instanceof AmbientCreature || target instanceof WaterAnimal;
        return target.isAlive() && animal && !(target instanceof Enemy)
                && !(target instanceof TamableAnimal tame && tame.isTame()) && !(target instanceof AbstractGolem)
                && !(target instanceof Animal young && young.isBaby());
    }

    @Override
    public void onTaskStarted(ServerLevel level, Golem golem, Task task) {
        if (task.entity() instanceof LivingEntity target && wanted(target)) {
            golem.setTarget(target);
            golem.addRankXp(1);
        }
        task.setSuspended(true);
        waiting = false;
    }

    @Override
    public boolean onTaskCompletion(ServerLevel level, Golem golem, Task task) {
        task.setSuspended(true);
        waiting = false;
        return true;
    }

    @Override
    public void onTaskSuspension(ServerLevel level, Task task) {
        waiting = false;
    }

    @Override
    public boolean canGolemPerform(Golem golem, Task task) {
        return true;
    }

    @Override
    public void onRemoval(ServerLevel level, BlockPos pos, Direction face) {
        waiting = false;
    }

    @Override
    public boolean canPlaceAt(Level level, BlockPos pos, Direction face) {
        return !level.isEmptyBlock(pos);
    }

    @Override
    public int[] categories() {
        return new int[] {AREA, PRIORITY, TRAITS};
    }

    @Override
    public List<GolemTrait> required() {
        return List.of(GolemTrait.FIGHTER, GolemTrait.SMART);
    }

    @Override
    public boolean usesArea() {
        return true;
    }
}
