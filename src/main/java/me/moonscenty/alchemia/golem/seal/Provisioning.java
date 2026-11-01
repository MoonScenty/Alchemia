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

    public record Request(SealEntity seal, ItemStack stack) {
        boolean same(Request other) {
            return seal.pos().equals(other.seal.pos()) && ItemStack.matches(stack, other.stack);
        }
    }

    private Provisioning() {
    }

    public static void request(Level level, SealEntity seal, ItemStack stack) {
        List<Request> list = REQUESTS.computeIfAbsent(level.dimension(), key -> new ArrayList<>());
        Request asked = new Request(seal, stack.copyWithCount(stack.getMaxStackSize()));
        synchronized (list) {
            if (list.stream().noneMatch(asked::same)) {
                list.add(asked);
            }
        }
    }

    public static void request(Level level, Task task, ItemStack stack) {
        SealEntity seal = Seals.get(level, task.seal());
        if (seal != null) {
            request(level, seal, stack);
        }
    }

    public static List<Request> outstanding(Level level) {
        return REQUESTS.computeIfAbsent(level.dimension(), key -> new ArrayList<>());
    }

    public static void clear() {
        REQUESTS.clear();
    }
}
