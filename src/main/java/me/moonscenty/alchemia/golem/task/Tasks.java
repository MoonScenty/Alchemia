package me.moonscenty.alchemia.golem.task;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.golem.Golem;
import me.moonscenty.alchemia.golem.seal.SealEntity;
import me.moonscenty.alchemia.golem.seal.Seals;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * Every task wanting doing, world by world. Like the original's, they are not kept when the game closes: the seals
 * that asked for them ask again soon enough.
 */
public final class Tasks {
    /** No world holds more than this; the eldest goes to make room. */
    private static final int LIMIT = 1000;
    /** Each task asked for is worth this many squared blocks of nearness per point of priority. */
    private static final double PRIORITY_WEIGHT = 256.0;

    private static final Map<ResourceKey<Level>, Map<Integer, Task>> TASKS = new ConcurrentHashMap<>();

    private Tasks() {
    }

    private static Map<Integer, Task> of(Level level) {
        return TASKS.computeIfAbsent(level.dimension(), key -> new ConcurrentHashMap<>());
    }

    public static void add(Level level, Task task) {
        Map<Integer, Task> tasks = of(level);
        if (tasks.size() > LIMIT) {
            Iterator<Integer> eldest = tasks.keySet().iterator();
            if (eldest.hasNext()) {
                eldest.next();
                eldest.remove();
            }
        }
        tasks.put(task.id(), task);
    }

    @Nullable
    public static Task get(Level level, int id) {
        return of(level).get(id);
    }

    public static List<Task> all(Level level) {
        return List.copyOf(of(level).values());
    }

    /**
     * The tasks of one kind a golem might take, nearest first, a point of priority counting for as much as sixteen
     * blocks of distance. Taken ones are left out, and those meant for some other golem.
     */
    public static List<Task> sortedFor(Golem golem, Task.Kind kind) {
        List<Task> out = new ArrayList<>();
        for (Task task : of(golem.level()).values()) {
            if (!task.reserved() && task.kind() == kind
                    && (task.golem() == null || task.golem().equals(golem.getUUID()))) {
                out.add(task);
            }
        }
        out.sort(Comparator.comparingDouble(task -> task.pos().distToCenterSqr(golem.position())
                - task.priority() * PRIORITY_WEIGHT));
        return out;
    }

    /** A golem has got there: the seal that asked says whether it was done. */
    public static void complete(Task task, Golem golem) {
        if (task.completed() || task.suspended() || !(golem.level() instanceof ServerLevel level)) {
            return;
        }
        SealEntity asked = Seals.get(level, task.seal());
        task.setCompletion(asked == null || asked.seal().onTaskCompletion(level, golem, task));
    }

    /** Once a second: ages every task, and lets go of those run out or suspended, telling their seals. */
    public static void clearSuspendedOrExpired(ServerLevel level) {
        Iterator<Task> it = of(level).values().iterator();
        while (it.hasNext()) {
            Task task = it.next();
            if (!task.suspended() && task.lifespan() > 0) {
                task.setLifespan(task.lifespan() - 1);
                continue;
            }
            it.remove();
            SealEntity asked = Seals.get(level, task.seal());
            if (asked != null) {
                asked.seal().onTaskSuspension(level, task);
            }
        }
    }

    /** Every task a seal here asked for is called off. */
    public static void suspendFor(Level level, me.moonscenty.alchemia.golem.seal.SealPos seal) {
        for (Task task : of(level).values()) {
            if (seal.equals(task.seal())) {
                task.setSuspended(true);
            }
        }
    }

    /** All of them, when the server stops. */
    public static void clear() {
        TASKS.clear();
    }
}
