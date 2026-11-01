package me.moonscenty.alchemia.aspect;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mojang.serialization.Codec;

import me.moonscenty.alchemia.registry.ModAspects;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

public final class AspectList {
    public static final AspectList EMPTY = new AspectList(Map.of());

    // original's value
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
        // LinkedHashMap, not Map.copyOf: iteration order must stay stable between runs
        this.amounts = Collections.unmodifiableMap(new LinkedHashMap<>(amounts));
    }

    public static AspectList of(Holder<Aspect> aspect, int amount) {
        return amount > 0 ? new AspectList(Map.of(aspect, amount)) : EMPTY;
    }

    public static AspectList eachPrimal(int amount) {
        return of(ModAspects.AIR, amount)
                .add(ModAspects.EARTH, amount)
                .add(ModAspects.FIRE, amount)
                .add(ModAspects.WATER, amount)
                .add(ModAspects.ORDER, amount)
                .add(ModAspects.ENTROPY, amount);
    }

    public static AspectList read(CompoundTag tag, String key, HolderLookup.Provider registries) {
        return CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag.get(key))
                .result().orElse(EMPTY);
    }

    public static void write(CompoundTag tag, String key, AspectList list, HolderLookup.Provider registries) {
        CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), list)
                .result().ifPresent(written -> tag.put(key, written));
    }

    public boolean isEmpty() {
        return amounts.isEmpty();
    }

    public int size() {
        return amounts.size();
    }

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

    // each compound expands to both parents at full amount, so the primal total exceeds the input
    public AspectList toPrimals() {
        AspectList out = EMPTY;
        for (Holder<Aspect> aspect : sortedByName()) {
            int amount = get(aspect);
            List<Holder<Aspect>> parts = aspect.value().components().orElse(List.of());
            if (parts.isEmpty()) {
                out = out.add(aspect, amount);
                continue;
            }
            AspectList mixed = EMPTY;
            for (Holder<Aspect> part : parts) {
                mixed = mixed.add(part, amount);
            }
            out = out.add(mixed.toPrimals());
        }
        return out;
    }

    // weighted by compounding depth, so a little of a rare compound outranks a lot of a primal
    public AspectList cull() {
        AspectList result = this;
        while (result.size() > MAX_ASPECTS) {
            // ties broken by name, so the result doesn't depend on build order
            Holder<Aspect> weakest = result.amounts.keySet().stream()
                    .min(Comparator.<Holder<Aspect>>comparingDouble(result::weight)
                            .thenComparing(holder -> holder.value().tag()))
                    .orElseThrow();
            result = result.remove(weakest);
        }
        return result;
    }

    private double weight(Holder<Aspect> aspect) {
        int complexity = aspect.value().complexity();
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
