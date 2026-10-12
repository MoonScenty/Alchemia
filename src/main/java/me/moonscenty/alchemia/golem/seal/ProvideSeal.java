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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Provide: stuck on a container, once a second looks for a seal within sixty-four blocks that has asked for
 * something this one's filter lets by and that it has in store, and offers a short task to fetch it out for that
 * seal's golems, at that seal's priority. It can be set to hand out one at a time and to leave one behind. It wants
 * a smart golem; clumsy golems cannot.
 */
public class ProvideSeal extends Seal {
    private static final int SINGLE = 4;
    private static final int LEAVE = 5;
    private static final double REACH_SQUARED = 4096.0;

    private int delay;
    /** Which request each of its tasks is fetching for. */
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
            ItemStack stack = request == null ? null : request.stack().copy();
            if (stack != null && toggle(SINGLE)) {
                stack.setCount(1);
            }
            if (stack != null && toggle(LEAVE)) {
                int there = Matching.count(container, stack, Matching.Rule.STRICT);
                if (there <= stack.getCount()) {
                    stack = stack.copyWithCount(there - 1);
                }
            }
            if (stack != null && !stack.isEmpty() && golem.canCarry(stack)) {
                ItemStack left = golem.hold(Matching.extract(container, stack.copy(), Matching.Rule.STRICT));
                if (!left.isEmpty()) {
                    ItemStack back = Matching.insert(container, left);
                    if (!back.isEmpty()) {
                        golem.spawnAtLocation(back, 0.25F);
                    }
                }
                level.playSound(null, golem, SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL, 0.125F,
                        ((level.random.nextFloat() - level.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                golem.addRankXp(1);
                golem.swing(InteractionHand.MAIN_HAND);
            }
            fetching.remove(task.id());
        }
        task.setSuspended(true);
        return true;
    }

    /** Only a golem that may work the asking seal, has room, and is not carrying some already. */
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
