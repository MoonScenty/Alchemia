package me.moonscenty.alchemia.golem.seal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import me.moonscenty.alchemia.golem.task.Task;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class Provisioning {
    private static final Map<ResourceKey<Level>, List<Request>> REQUESTS = new ConcurrentHashMap<>();

    public record Request(SealEntity seal, ItemStack stack, boolean anyDamage) {
        boolean same(Request other) {
            return seal.pos().equals(other.seal.pos()) && anyDamage == other.anyDamage
                    && ItemStack.matches(stack, other.stack);
        }

        public Matching.Rule rule() {
            return anyDamage ? new Matching.Rule(false, true, false, false) : Matching.Rule.STRICT;
        }
    }

    private Provisioning() {
    }

    public static void request(Level level, SealEntity seal, ItemStack stack) {
        request(level, seal, stack, false);
    }

    public static void request(Level level, SealEntity seal, ItemStack stack, boolean anyDamage) {
        List<Request> list = REQUESTS.computeIfAbsent(level.dimension(), key -> new ArrayList<>());
        Request asked = new Request(seal, stack.copyWithCount(stack.getMaxStackSize()), anyDamage);
        synchronized (list) {
            if (list.stream().noneMatch(asked::same)) {
                list.add(asked);
            }
        }
    }

    public static void request(Level level, Task task, ItemStack stack) {
        request(level, task, stack, false);
    }

    public static void request(Level level, Task task, ItemStack stack, boolean anyDamage) {
        SealEntity seal = Seals.get(level, task.seal());
        if (seal != null) {
            request(level, seal, stack, anyDamage);
        }
    }

    public static List<Request> outstanding(Level level) {
        return REQUESTS.computeIfAbsent(level.dimension(), key -> new ArrayList<>());
    }

    public static void clear() {
        REQUESTS.clear();
    }
}
