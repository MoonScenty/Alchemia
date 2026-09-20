package me.moonscenty.alchemia.aspect;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mojang.serialization.Codec;

import me.moonscenty.alchemia.registry.ModAspects;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

/**
 * How much of each aspect something holds. Immutable; every operation hands back a new list.
 */
public final class AspectList {
    public static final AspectList EMPTY = new AspectList(Map.of());

    /** At most this many aspects survive {@link #cull()}, matching the original. */
    public static final int MAX_ASPECTS = 6;

    public static final Codec<AspectList> CODEC = Codec
            .unboundedMap(ModAspects.REGISTRY.holderByNameCodec(), ExtraCodecs.POSITIVE_INT)
            .xmap(AspectList::new, list -> list.amounts);

    public static final StreamCodec<RegistryFriendlyByteBuf, AspectList> STREAM_CODEC = ByteBufCodecs
            .<RegistryFriendlyByteBuf, Holder<Aspect>, Integer, Map<Holder<Aspect>, Integer>>map(LinkedHashMap::new,
                    ByteBufCodecs.holderRegistry(ModAspects.KEY), ByteBufCodecs.VAR_INT)
            .map(AspectList::new, list -> list.amounts);

    private final Map<Holder<Aspect>, Integer> amounts;

    private AspectList(Map<Holder<Aspect>, Integer> amounts) {
        this.amounts = Map.copyOf(amounts);
    }

    public static AspectList of(Holder<Aspect> aspect, int amount) {
        return amount > 0 ? new AspectList(Map.of(aspect, amount)) : EMPTY;
    }

    public boolean isEmpty() {
        return amounts.isEmpty();
    }

    /** How many different aspects are present. */
    public int size() {
        return amounts.size();
    }

    /** Everything added together, which is what a container or a wand charge is measured in. */
    public int total() {
        return amounts.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int get(Holder<Aspect> aspect) {
        return amounts.getOrDefault(aspect, 0);
    }

    public boolean contains(Holder<Aspect> aspect) {
        return amounts.containsKey(aspect);
    }

    public Map<Holder<Aspect>, Integer> asMap() {
        return amounts;
    }

    public List<Holder<Aspect>> sortedByName() {
        return amounts.keySet().stream().sorted(Comparator.comparing(holder -> holder.value().tag())).toList();
    }

    /** Largest first, ties broken by name so the order never wobbles between frames. */
    public List<Holder<Aspect>> sortedByAmount() {
        return amounts.keySet().stream()
                .sorted(Comparator.<Holder<Aspect>>comparingInt(this::get).reversed()
                        .thenComparing(holder -> holder.value().tag()))
                .toList();
    }

    public AspectList add(Holder<Aspect> aspect, int amount) {
        if (amount <= 0) {
            return this;
        }
        Map<Holder<Aspect>, Integer> merged = new LinkedHashMap<>(amounts);
        merged.merge(aspect, amount, Integer::sum);
        return new AspectList(merged);
    }

    public AspectList add(AspectList other) {
        if (other.isEmpty()) {
            return this;
        }
        Map<Holder<Aspect>, Integer> merged = new LinkedHashMap<>(amounts);
        other.amounts.forEach((aspect, amount) -> merged.merge(aspect, amount, Integer::sum));
        return new AspectList(merged);
    }

    /** Keeps whichever amount is larger rather than adding them, as the original's merge did. */
    public AspectList mergeMax(Holder<Aspect> aspect, int amount) {
        if (amount <= 0) {
            return this;
        }
        Map<Holder<Aspect>, Integer> merged = new LinkedHashMap<>(amounts);
        merged.merge(aspect, amount, Math::max);
        return new AspectList(merged);
    }

    public AspectList remove(Holder<Aspect> aspect) {
        if (!amounts.containsKey(aspect)) {
            return this;
        }
        Map<Holder<Aspect>, Integer> reduced = new LinkedHashMap<>(amounts);
        reduced.remove(aspect);
        return new AspectList(reduced);
    }

    /** Takes some away, dropping the aspect once nothing is left. */
    public AspectList reduce(Holder<Aspect> aspect, int amount) {
        int left = get(aspect) - amount;
        return left > 0 ? withAmount(aspect, left) : remove(aspect);
    }

    public AspectList withAmount(Holder<Aspect> aspect, int amount) {
        if (amount <= 0) {
            return remove(aspect);
        }
        Map<Holder<Aspect>, Integer> changed = new LinkedHashMap<>(amounts);
        changed.put(aspect, amount);
        return new AspectList(changed);
    }

    /** Scales every amount, dropping anything that rounds away to nothing. */
    public AspectList scale(float factor) {
        Map<Holder<Aspect>, Integer> scaled = new LinkedHashMap<>();
        amounts.forEach((aspect, amount) -> {
            int value = (int) (amount * factor);
            if (value > 0) {
                scaled.put(aspect, value);
            }
        });
        return new AspectList(scaled);
    }

    public AspectList capAt(int max) {
        Map<Holder<Aspect>, Integer> capped = new LinkedHashMap<>();
        amounts.forEach((aspect, amount) -> capped.put(aspect, Math.min(max, amount)));
        return new AspectList(capped);
    }

    /**
     * Trims the list down to {@link #MAX_ASPECTS}, dropping the least interesting aspect each time.
     * <p>
     * A plain amount would always favour the primals, since compounds break down into them and so are rarer. Each
     * aspect is therefore weighed up by how deeply compounded it is, so a little of something intricate outranks a lot
     * of something plain.
     */
    public AspectList cull() {
        AspectList result = this;
        while (result.size() > MAX_ASPECTS) {
            Holder<Aspect> weakest = result.amounts.keySet().stream()
                    .min(Comparator.comparingDouble(result::weight))
                    .orElseThrow();
            result = result.remove(weakest);
        }
        return result;
    }

    private double weight(Holder<Aspect> aspect) {
        int complexity = aspect.value().complexity();
        // primals count for a little less, and every further step of compounding for a little more
        return get(aspect) * (complexity == 0 ? 0.9 : Math.pow(1.1, complexity));
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof AspectList list && amounts.equals(list.amounts);
    }

    @Override
    public int hashCode() {
        return amounts.hashCode();
    }

    @Override
    public String toString() {
        return sortedByAmount().stream()
                .map(aspect -> aspect.value().tag() + " x" + get(aspect))
                .reduce((a, b) -> a + ", " + b)
                .map(joined -> "AspectList[" + joined + "]")
                .orElse("AspectList[]");
    }
}
