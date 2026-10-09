package me.moonscenty.alchemia.client.legacy;

import java.util.Map;
import java.util.Optional;

import me.moonscenty.alchemia.client.legacy.model.LegacyModel;

/**
 * The models read out of the original's code at the last import, by the key {@link LegacyAssets#MODELS} gives them.
 *
 * <p>Nothing here outlives a reload: an import replaces the lot, and {@link #generation()} moves on so anything that
 * baked one of these knows to bake it again.
 */
public final class LegacyModels {
    private static volatile Map<String, LegacyModel> imported = Map.of();
    private static volatile int generation;

    private LegacyModels() {
    }

    /** Empty when no jar had it, or none was there to look in. */
    public static Optional<LegacyModel> get(String key) {
        return Optional.ofNullable(imported.get(key));
    }

    public static int generation() {
        return generation;
    }

    static void replace(Map<String, LegacyModel> models) {
        imported = Map.copyOf(models);
        generation++;
    }
}
