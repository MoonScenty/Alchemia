package me.moonscenty.alchemia.golem.seal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Things seals have asked to be brought to them: a seal that needs something to work with puts in for a stack of
 * it, and a providing seal near enough with some of it in store sends a golem to fetch it.
 */
public final class Provisioning {
    private static final Map<ResourceKey<Level>, List<Request>> REQUESTS = new ConcurrentHashMap<>();

    /** One seal wanting a full stack of something. */
    public record Request(SealEntity seal, ItemStack stack) {
        boolean same(Request other) {
            return seal.pos().equals(other.seal.pos()) && ItemStack.matches(stack, other.stack);
        }
    }

    private Provisioning() {
    }

    /** Puts in for a full stack of a thing, unless the same seal has already put in for the same. */
    public static void request(Level level, SealEntity seal, ItemStack stack) {
        List<Request> list = REQUESTS.computeIfAbsent(level.dimension(), key -> new ArrayList<>());
        Request asked = new Request(seal, stack.copyWithCount(stack.getMaxStackSize()));
        synchronized (list) {
            if (list.stream().noneMatch(asked::same)) {
                list.add(asked);
            }
        }
    }

    /** What is outstanding in a world, which a provider may take one of off the list. */
    public static List<Request> outstanding(Level level) {
        return REQUESTS.computeIfAbsent(level.dimension(), key -> new ArrayList<>());
    }

    public static void clear() {
        REQUESTS.clear();
    }
}
