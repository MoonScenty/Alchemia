package me.moonscenty.alchemia.golem.seal;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.golem.Golem;
import me.moonscenty.alchemia.golem.GolemTrait;
import me.moonscenty.alchemia.golem.task.Task;
import me.moonscenty.alchemia.golem.task.Tasks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Collect: once a second, sends a golem after one thing lying still in its area that the filter lets by, and the
 * golem picks up as much of it as it can carry. Clumsy golems cannot. The advanced seal has nine filter entries and
 * lets its options be set, and wants a smart golem.
 */
public class PickupSeal extends Seal {
    /** Which item each of its tasks is after, by world, so that no two seals send golems after the same thing. */
    private static final Map<ResourceKey<Level>, Map<Integer, Integer>> CHASED = new ConcurrentHashMap<>();

    private final boolean advanced;
    private int delay;

    public PickupSeal(boolean advanced) {
        super(advanced ? "pickup_advanced" : "pickup", advanced ? 9 : 1, new Toggle("meta", true),
                new Toggle("nbt", true), new Toggle("ore", false), new Toggle("mod", false));
        this.advanced = advanced;
        this.delay = java.util.concurrent.ThreadLocalRandom.current().nextInt(100);
    }

    private static Map<Integer, Integer> chased(Level level) {
        return CHASED.computeIfAbsent(level.dimension(), key -> new ConcurrentHashMap<>());
    }

    @Override
    public void tick(ServerLevel level, SealEntity seal) {
        if (delay++ % 20 != 0) {
            return;
        }
        Map<Integer, Integer> chased = chased(level);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, seal.bounds())) {
            if (item.onGround() && !item.hasPickUpDelay() && !item.getItem().isEmpty()
                    && !chased.containsValue(item.getId())
                    && Matching.firstAllowed(filter(), blacklist(), List.of(item.getItem()), rule()).found()) {
                Task task = new Task(seal.pos(), item);
                task.setPriority(seal.priority());
                chased.put(task.id(), item.getId());
                Tasks.add(level, task);
                break;
            }
        }
        if (delay % 100 != 0) {
            chased.values().removeIf(id -> {
                Entity there = level.getEntity(id);
                return there == null || !there.isAlive();
            });
        }
    }

    @Nullable
    private static ItemEntity item(Level level, Task task) {
        Integer id = chased(level).get(task.id());
        return id != null && level.getEntity(id) instanceof ItemEntity item ? item : null;
    }

    @Override
    public boolean onTaskCompletion(ServerLevel level, Golem golem, Task task) {
        ItemEntity item = item(level, task);
        if (item != null && !item.getItem().isEmpty()
                && Matching.firstAllowed(filter(), blacklist(), List.of(item.getItem()), rule()).found()) {
            ItemStack left = golem.hold(item.getItem());
            if (left.isEmpty()) {
                item.discard();
            } else {
                item.setItem(left);
            }
            level.playSound(null, golem, SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL, 0.125F,
                    ((level.random.nextFloat() - level.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            golem.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        }
        task.setSuspended(true);
        chased(level).remove(task.id());
        return true;
    }

    @Override
    public boolean canGolemPerform(Golem golem, Task task) {
        ItemEntity item = item(golem.level(), task);
        if (item == null || item.getItem().isEmpty()) {
            return false;
        }
        if (!item.isAlive()) {
            task.setSuspended(true);
            return false;
        }
        return golem.canCarry(item.getItem());
    }

    @Override
    public boolean canPlaceAt(Level level, BlockPos pos, Direction face) {
        return !level.isEmptyBlock(pos);
    }

    @Override
    public int[] categories() {
        return advanced ? new int[] {AREA, FILTER, OPTIONS, PRIORITY, TRAITS}
                : new int[] {AREA, FILTER, PRIORITY, TRAITS};
    }

    @Override
    public List<GolemTrait> required() {
        return advanced ? List.of(GolemTrait.SMART) : List.of();
    }

    @Override
    public List<GolemTrait> forbidden() {
        return List.of(GolemTrait.CLUMSY);
    }

    @Override
    public boolean usesArea() {
        return true;
    }
}
