package me.moonscenty.alchemia.client.legacy;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.client.legacy.model.LegacyMesh;
import me.moonscenty.alchemia.client.legacy.model.LegacyModelBaker;
import net.minecraft.client.model.geom.ModelPart;

public final class LegacyCached<T> {
    private final Supplier<T> build;
    private int builtFrom = -1;
    @Nullable
    private T value;

    public LegacyCached(Supplier<T> build) {
        this.build = build;
    }

    public static LegacyCached<LegacyMesh> mesh(String key) {
        return new LegacyCached<>(() -> LegacyModels.mesh(key).orElse(null));
    }

    public static LegacyCached<LegacyMesh> flippedMesh(String key) {
        return new LegacyCached<>(() -> LegacyModels.mesh(key).map(LegacyMesh::flippedV).orElse(null));
    }

    public static LegacyCached<ModelPart> field(String key, String name) {
        return new LegacyCached<>(() -> LegacyModels.get(key).map(model -> LegacyModelBaker.field(model, name))
                .orElse(null));
    }

    public static LegacyCached<ModelPart[]> fields(String key, String... names) {
        return new LegacyCached<>(() -> LegacyModels.get(key).map(model -> LegacyModelBaker.fields(model, names))
                .orElse(null));
    }

    @Nullable
    public T get() {
        int generation = LegacyModels.generation();
        if (generation != builtFrom) {
            builtFrom = generation;
            value = build.get();
        }
        return value;
    }
}
