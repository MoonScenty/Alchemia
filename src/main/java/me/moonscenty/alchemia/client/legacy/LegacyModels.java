package me.moonscenty.alchemia.client.legacy;

import java.util.Map;
import java.util.Optional;

import me.moonscenty.alchemia.client.legacy.model.LegacyMesh;
import me.moonscenty.alchemia.client.legacy.model.LegacyModel;

public final class LegacyModels {
    private static volatile Map<String, LegacyModel> imported = Map.of();
    private static volatile Map<String, LegacyMesh> meshes = Map.of();
    private static volatile int generation;

    private LegacyModels() {
    }

    public static Optional<LegacyModel> get(String key) {
        return Optional.ofNullable(imported.get(key));
    }

    public static Optional<LegacyMesh> mesh(String path) {
        return Optional.ofNullable(meshes.get(path));
    }

    public static int generation() {
        return generation;
    }

    static void replace(Map<String, LegacyModel> models, Map<String, LegacyMesh> read) {
        imported = Map.copyOf(models);
        meshes = Map.copyOf(read);
        generation++;
    }
}
