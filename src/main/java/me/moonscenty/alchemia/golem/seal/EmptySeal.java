package me.moonscenty.alchemia.golem.seal;

import java.util.ArrayList;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Empty: stuck on a container, once a second offers a short task to take out the first thing in it the filter lets
 * by, and a golem with room takes as much of it as it can carry out through the face the seal is on. With its last
 * option set, it always leaves one behind. The advanced seal has nine filter entries and lets its options be set,
 * among them working through a whitelist one entry at a time, and wants a smart golem. Clumsy golems cannot.
 */
public class EmptySeal extends Seal {
    private static final int CYCLE = 4;
    private static final int LEAVE = 5;

    private final boolean advanced;
    private int delay;
    private int turn;
    /** What each of its tasks is to take out. */
    private final Map<Integer, ItemStack> wanted = new HashMap<>();

    public EmptySeal(boolean advanced) {
        super(advanced ? "empty_advanced" : "empty", advanced ? 9 : 1, new Toggle("meta", true),
                new Toggle("nbt", true), new Toggle("ore", false), new Toggle("mod", false),
                new Toggle("cycle", false), new Toggle("leave", false));
        this.advanced = advanced;
        this.delay = java.util.concurrent.ThreadLocalRandom.current().nextInt(30);
    }

    /** The filter to go by this time: with cycling on a whitelist, one entry at a time in turn. */
    private List<ItemStack> filterNow() {
        if (advanced && toggle(CYCLE) && !blacklist()) {
            List<ItemStack> entries = new ArrayList<>();
            for (ItemStack entry : filter()) {
                if (!entry.isEmpty()) {
                    entries.add(entry);
                }
            }
            if (!entries.isEmpty()) {
                return List.of(entries.get(Math.abs(turn % entries.size())));
            }
        }
        return filter();
    }

    @Override
    public void tick(ServerLevel level, SealEntity seal) {
        if (delay % 100 == 0) {
            wanted.keySet().removeIf(id -> Tasks.get(level, id) == null);
        }
        if (delay++ % 20 != 0) {
            return;
        }
        IItemHandler container = Matching.container(level, seal.pos().pos(), seal.pos().face());
        if (container == null) {
            return;
        }
        ItemStack stack = Matching.firstAllowed(filterNow(), blacklist(), container, rule(), toggle(LEAVE));
        if (!stack.isEmpty()) {
            Task task = new Task(seal.pos(), seal.pos().pos());
            task.setPriority(seal.priority());
            task.setLifespan(5);
            Tasks.add(level, task);
            wanted.put(task.id(), stack.copy());
        }
    }

    @Override
    public boolean onTaskCompletion(ServerLevel level, Golem golem, Task task) {
        SealPos at = task.seal();
        IItemHandler container = Matching.container(level, at.pos(), at.face());
        if (container != null) {
            ItemStack stack = wanted.get(task.id());
            if (stack != null && toggle(LEAVE)) {
                int there = Matching.count(container, stack, rule());
                if (there <= stack.getCount()) {
                    stack = stack.copyWithCount(there - 1);
                }
            }
            if (stack != null && !stack.isEmpty() && golem.canCarry(stack)) {
                ItemStack left = golem.hold(Matching.extract(container, stack.copy(), rule()));
                if (!left.isEmpty()) {
                    ItemStack back = Matching.insert(container, left);
                    if (!back.isEmpty()) {
                        golem.spawnAtLocation(back, 0.25F);
                    }
                }
                level.playSound(null, golem, SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL, 0.125F,
                        ((level.random.nextFloat() - level.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                golem.swing(InteractionHand.MAIN_HAND);
            }
            wanted.remove(task.id());
            turn++;
        }
        task.setSuspended(true);
        return true;
    }

    @Override
    public boolean canGolemPerform(Golem golem, Task task) {
        ItemStack stack = wanted.get(task.id());
        return stack != null && golem.canCarry(stack);
    }

    @Override
    public void onTaskSuspension(ServerLevel level, Task task) {
        wanted.remove(task.id());
    }

    @Override
    public boolean canPlaceAt(Level level, BlockPos pos, Direction face) {
        return Matching.container(level, pos, face) != null;
    }

    @Override
    public int[] categories() {
        return advanced ? new int[] {FILTER, OPTIONS, PRIORITY, TRAITS} : new int[] {FILTER, PRIORITY, TRAITS};
    }

    @Override
    public List<GolemTrait> required() {
        return advanced ? List.of(GolemTrait.SMART) : List.of();
    }

    @Override
    public List<GolemTrait> forbidden() {
        return List.of(GolemTrait.CLUMSY);
    }
}
