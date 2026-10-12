package me.moonscenty.alchemia.golem.seal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.moonscenty.alchemia.golem.Golem;
import me.moonscenty.alchemia.golem.GolemTrait;
import me.moonscenty.alchemia.golem.task.Task;
import me.moonscenty.alchemia.golem.task.Tasks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

/**
 * Lumberjack: walks its area a block a tick, and sends smart golems with grinders to every log in it. A golem at a
 * tree takes it down a log at a time from the far end, and keeps at it until there is nothing left joined to the
 * log it was sent to.
 */
public class LumberSeal extends Seal {
    private int delay;
    /** Which log each of its tasks is for. */
    private final Map<Integer, Long> logs = new HashMap<>();

    public LumberSeal() {
        super("lumber", 0);
        this.delay = java.util.concurrent.ThreadLocalRandom.current().nextInt(33);
    }

    @Override
    public void tick(ServerLevel level, SealEntity seal) {
        if (delay % 100 == 0) {
            logs.keySet().removeIf(id -> Tasks.get(level, id) == null);
        }
        delay++;
        BlockPos at = seal.posInArea(delay);
        if (!logs.containsValue(at.asLong()) && Fieldwork.log(level, at)) {
            Task task = new Task(seal.pos(), at);
            task.setPriority(seal.priority());
            Tasks.add(level, task);
            logs.put(task.id(), at.asLong());
        }
    }

    /** One log off the far end each time; not done while there is any left. */
    @Override
    public boolean onTaskCompletion(ServerLevel level, Golem golem, Task task) {
        if (logs.containsKey(task.id()) && Fieldwork.log(level, task.pos())) {
            golem.swing(InteractionHand.MAIN_HAND);
            if (Fieldwork.breakFurthest(level, task.pos(), golem)) {
                task.setLifespan(Math.max(task.lifespan(), 10));
                golem.addRankXp(1);
                return false;
            }
            logs.remove(task.id());
        }
        task.setSuspended(true);
        return true;
    }

    @Override
    public boolean canGolemPerform(Golem golem, Task task) {
        if (logs.containsKey(task.id()) && Fieldwork.log(golem.level(), task.pos())) {
            return true;
        }
        task.setSuspended(true);
        return false;
    }

    @Override
    public void onTaskSuspension(ServerLevel level, Task task) {
        logs.remove(task.id());
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
        return List.of(GolemTrait.BREAKER, GolemTrait.SMART);
    }

    @Override
    public boolean usesArea() {
        return true;
    }
}
