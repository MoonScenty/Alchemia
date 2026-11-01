package me.moonscenty.alchemia.golem.seal;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.world.item.ItemStack;

// in the original's listing order
public final class SealTypes {
    private static final Map<String, Supplier<Seal>> KINDS = new LinkedHashMap<>();

    static {
        KINDS.put("pickup", () -> new PickupSeal(false));
        KINDS.put("pickup_advanced", () -> new PickupSeal(true));
        KINDS.put("fill", () -> new FillSeal(false));
        KINDS.put("fill_advanced", () -> new FillSeal(true));
        KINDS.put("empty", () -> new EmptySeal(false));
        KINDS.put("empty_advanced", () -> new EmptySeal(true));
        KINDS.put("provider", ProvideSeal::new);
        KINDS.put("guard", () -> new GuardSeal(false));
        KINDS.put("guard_advanced", () -> new GuardSeal(true));
        KINDS.put("butcher", ButcherSeal::new);
        KINDS.put("harvest", HarvestSeal::new);
        KINDS.put("lumber", LumberSeal::new);
        KINDS.put("breaker", BreakerSeal::new);
        KINDS.put("use", UseSeal::new);
    }

    private SealTypes() {
    }

    public static List<String> keys() {
        return List.copyOf(KINDS.keySet());
    }

    @Nullable
    public static Seal make(String key) {
        Supplier<Seal> kind = KINDS.get(key);
        return kind == null ? null : kind.get();
    }

    public static ItemStack item(String key) {
        var item = ModItems.SEALS.get(key);
        return item == null ? ItemStack.EMPTY : new ItemStack(item.get());
    }
}
