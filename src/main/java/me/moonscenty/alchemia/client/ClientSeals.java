package me.moonscenty.alchemia.client;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.golem.seal.SealEntity;
import me.moonscenty.alchemia.golem.seal.SealPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

/** The seals of the world the player is in, as the server last told of them. */
public final class ClientSeals {
    private static final Map<SealPos, SealEntity> SEALS = new ConcurrentHashMap<>();

    private ClientSeals() {
    }

    @Nullable
    public static SealEntity get(SealPos pos) {
        return SEALS.get(pos);
    }

    public static Collection<SealEntity> all() {
        return SEALS.values();
    }

    /** One seal as it now is, or gone. */
    public static void update(SealPos pos, @Nullable CompoundTag saved, HolderLookup.Provider registries) {
        SealEntity seal = saved == null ? null : SealEntity.load(saved, registries);
        if (seal == null) {
            SEALS.remove(pos);
        } else {
            SEALS.put(seal.pos(), seal);
        }
    }

    /** All of them, anew. */
    public static void replace(List<CompoundTag> saved, HolderLookup.Provider registries) {
        SEALS.clear();
        for (CompoundTag each : saved) {
            SealEntity seal = SealEntity.load(each, registries);
            if (seal != null) {
                SEALS.put(seal.pos(), seal);
            }
        }
    }
}
