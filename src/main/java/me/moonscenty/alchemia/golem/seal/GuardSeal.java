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
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Guard: once a second, sends fighting golems at whatever is in its area that it is set against: monsters, and on
 * the advanced seal also animals or, where the server allows it, players. A golem sent goes for it and fights it out
 * as it would anything that struck it. The plain seal only ever minds monsters.
 */
public class GuardSeal extends Seal {
    private static final int MOBS = 0;
    private static final int ANIMALS = 1;
    private static final int PLAYERS = 2;

    private final boolean advanced;
    private int delay;

    public GuardSeal(boolean advanced) {
        super(advanced ? "guard_advanced" : "guard", 0, new Toggle("mob", true), new Toggle("animal", false),
                new Toggle("player", false));
        this.advanced = advanced;
        this.delay = java.util.concurrent.ThreadLocalRandom.current().nextInt(22);
    }

    @Override
    public void tick(ServerLevel level, SealEntity seal) {
        if (delay++ % 20 != 0) {
            return;
        }
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, seal.bounds())) {
            if (wanted(target)) {
                Task task = new Task(seal.pos(), target);
                task.setPriority(seal.priority());
                task.setLifespan(10);
                Tasks.add(level, task);
            }
        }
    }

    /** Whether it is set against this. Animals, to the original, were anything tame-natured, golems among them. */
    private boolean wanted(LivingEntity target) {
        if (!target.isAlive()) {
            return false;
        }
        if (toggle(MOBS) && target instanceof Enemy) {
            return true;
        }
        if (toggle(ANIMALS) && (target instanceof Animal || target instanceof AmbientCreature
                || target instanceof WaterAnimal || target instanceof AbstractGolem)) {
            return true;
        }
        return toggle(PLAYERS) && target instanceof Player && target.level().getServer() != null
                && target.level().getServer().isPvpAllowed();
    }

    /** The golem is set on it there and then; the fighting is its own business after that. */
    @Override
    public void onTaskStarted(ServerLevel level, Golem golem, Task task) {
        if (task.entity() instanceof LivingEntity target && wanted(target)) {
            golem.setTarget(target);
            golem.addRankXp(1);
        }
        task.setSuspended(true);
    }

    @Override
    public boolean onTaskCompletion(ServerLevel level, Golem golem, Task task) {
        task.setSuspended(true);
        return true;
    }

    @Override
    public boolean canGolemPerform(Golem golem, Task task) {
        return task.entity() != null && !golem.isAlliedTo(task.entity());
    }

    @Override
    public boolean canPlaceAt(Level level, BlockPos pos, Direction face) {
        return !level.isEmptyBlock(pos);
    }

    @Override
    public int[] categories() {
        return advanced ? new int[] {AREA, OPTIONS, PRIORITY, TRAITS} : new int[] {AREA, PRIORITY, TRAITS};
    }

    @Override
    public List<GolemTrait> required() {
        return advanced ? List.of(GolemTrait.FIGHTER, GolemTrait.SMART) : List.of(GolemTrait.FIGHTER);
    }

    @Override
    public boolean usesArea() {
        return true;
    }
}
