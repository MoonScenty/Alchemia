package me.moonscenty.alchemia.golem.seal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import me.moonscenty.alchemia.golem.Golem;
import me.moonscenty.alchemia.golem.GolemTrait;
import me.moonscenty.alchemia.golem.task.Task;
import me.moonscenty.alchemia.golem.task.Tasks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;

public class ProvideSeal extends Seal {
    private static final int SINGLE = 4;
    private static final int LEAVE = 5;
    private static final double REACH_SQUARED = 4096.0;

    private int delay;
    private final Map<Integer, Provisioning.Request> fetching = new HashMap<>();

    public ProvideSeal() {
        super("provider", 9, new Toggle("meta", true), new Toggle("nbt", true), new Toggle("ore", false),
                new Toggle("mod", false), new Toggle("single", false), new Toggle("leave", false));
        this.delay = java.util.concurrent.ThreadLocalRandom.current().nextInt(88);
    }

    @Override
    public void tick(ServerLevel level, SealEntity seal) {
        if (delay % 100 == 0) {
            fetching.keySet().removeIf(id -> Tasks.get(level, id) == null);
        }
        if (delay++ % 20 != 0) {
            return;
        }
        IItemHandler container = Matching.container(level, seal.pos().pos(), seal.pos().face());
        if (container == null) {
            return;
        }
        List<Provisioning.Request> outstanding = Provisioning.outstanding(level);
        synchronized (outstanding) {
            Iterator<Provisioning.Request> it = outstanding.iterator();
            while (it.hasNext()) {
                Provisioning.Request request = it.next();
                if (request.seal().pos().pos().distSqr(seal.pos().pos()) < REACH_SQUARED
                        && Matching.firstAllowed(filter(), blacklist(), List.of(request.stack()), rule()).found()
                        && Matching.count(container, request.stack(), Matching.Rule.STRICT)
                                > (toggle(LEAVE) ? 1 : 0)) {
                    Task task = new Task(seal.pos(), seal.pos().pos());
                    task.setPriority(request.seal().priority());
                    task.setLifespan(5);
                    Tasks.add(level, task);
                    fetching.put(task.id(), request);
                    it.remove();
                    break;
                }
            }
        }
    }

    @Override
    public boolean onTaskCompletion(ServerLevel level, Golem golem, Task task) {
        SealPos at = task.seal();
        IItemHandler container = Matching.container(level, at.pos(), at.face());
        if (container != null) {
            Provisioning.Request request = fetching.get(task.id());
            if (request != null) {
                ItemStack stack = request.stack().copy();
                if (toggle(SINGLE)) {
                    stack.setCount(1);
                }
                if (takeOut(level, golem, container, stack, Matching.Rule.STRICT, toggle(LEAVE))) {
                    golem.addRankXp(1);
                }
            }
            fetching.remove(task.id());
        }
        task.setSuspended(true);
        return true;
    }

    @Override
    public boolean canGolemPerform(Golem golem, Task task) {
        Provisioning.Request request = fetching.get(task.id());
        return request != null && golem.isWithinRestriction(request.seal().pos().pos())
                && golem.mayWork(request.seal()) && !golem.isCarrying(request.stack())
                && golem.canCarry(request.stack());
    }

    @Override
    public void onTaskSuspension(ServerLevel level, Task task) {
        fetching.remove(task.id());
    }

    @Override
    public boolean canPlaceAt(Level level, BlockPos pos, Direction face) {
        return Matching.container(level, pos, face) != null;
    }

    @Override
    public int[] categories() {
        return new int[] {FILTER, OPTIONS, PRIORITY, TRAITS};
    }

    @Override
    public List<GolemTrait> required() {
        return List.of(GolemTrait.SMART);
    }

    @Override
    public List<GolemTrait> forbidden() {
        return List.of(GolemTrait.CLUMSY);
    }
}
