package me.moonscenty.alchemia.golem.seal;

import java.util.HashMap;
import java.util.Map;

import me.moonscenty.alchemia.golem.Golem;
import me.moonscenty.alchemia.golem.task.Task;
import me.moonscenty.alchemia.golem.task.Tasks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/** A seal that walks its area a block a tick and offers a task at every block there it wants worked. */
public abstract class AreaBlockSeal extends Seal {
    private int delay;
    /** Which block each of its tasks is for. */
    private final Map<Integer, Long> blocks = new HashMap<>();

    protected AreaBlockSeal(String key, int filterSize, int startsWithin, Toggle... toggles) {
        super(key, filterSize, toggles);
        this.delay = java.util.concurrent.ThreadLocalRandom.current().nextInt(startsWithin);
    }

    /** Whether the block there is one to send a golem to. */
    protected abstract boolean wanted(Level level, BlockPos at);

    /** Anything more a fresh task needs before it is offered. */
    protected void prepare(ServerLevel level, BlockPos at, Task task) {
    }

    @Override
    public void tick(ServerLevel level, SealEntity seal) {
        if (delay % 100 == 0) {
            blocks.keySet().removeIf(id -> Tasks.get(level, id) == null);
        }
        delay++;
        BlockPos at = seal.posInArea(delay);
        if (!blocks.containsValue(at.asLong()) && wanted(level, at)) {
            Task task = new Task(seal.pos(), at);
            task.setPriority(seal.priority());
            prepare(level, at, task);
            Tasks.add(level, task);
            blocks.put(task.id(), at.asLong());
        }
    }

    /** Whether the task is still one of its own, at a block it still wants worked. */
    protected boolean stillWanted(Level level, Task task) {
        return blocks.containsKey(task.id()) && wanted(level, task.pos());
    }

    /** Lets go of the task's block, so it can be offered again. */
    protected void forget(Task task) {
        blocks.remove(task.id());
    }

    @Override
    public boolean canGolemPerform(Golem golem, Task task) {
        if (stillWanted(golem.level(), task)) {
            return true;
        }
        task.setSuspended(true);
        return false;
    }

    @Override
    public void onTaskSuspension(ServerLevel level, Task task) {
        forget(task);
    }

    @Override
    public boolean canPlaceAt(Level level, BlockPos pos, Direction face) {
        return !level.isEmptyBlock(pos);
    }

    @Override
    public boolean usesArea() {
        return true;
    }
}
