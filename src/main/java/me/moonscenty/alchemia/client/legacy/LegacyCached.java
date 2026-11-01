package me.moonscenty.alchemia.client.legacy;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.client.legacy.model.LegacyMesh;
import me.moonscenty.alchemia.client.legacy.model.LegacyModelBaker;
import net.minecraft.client.model.geom.ModelPart;

/** Something built from the imported models, built again whenever {@link LegacyModels#generation()} moves on. */
public final class LegacyCached<T> {
    private final Supplier<T> build;
    private int builtFrom = -1;
    @Nullable
    private T value;

    public LegacyCached(Supplier<T> build) {
        this.build = build;
    }

    /** A mesh as imported, or null when there is none. */
    public static LegacyCached<LegacyMesh> mesh(String key) {
        return new LegacyCached<>(() -> LegacyModels.mesh(key).orElse(null));
    }

    /** A mesh with its texture corners turned over, or null when there is none. */
    public static LegacyCached<LegacyMesh> flippedMesh(String key) {
        return new LegacyCached<>(() -> LegacyModels.mesh(key).map(LegacyMesh::flippedV).orElse(null));
    }

    /** One field of an imported model, baked, or null when the model is not there. */
    public static LegacyCached<ModelPart> field(String key, String name) {
        return new LegacyCached<>(() -> LegacyModels.get(key).map(model -> LegacyModelBaker.field(model, name))
                .orElse(null));
    }

    /** Several fields of an imported model, baked, or null when the model is not there. */
    public static LegacyCached<ModelPart[]> fields(String key, String... names) {
        return new LegacyCached<>(() -> LegacyModels.get(key).map(model -> LegacyModelBaker.fields(model, names))
                .orElse(null));
    }

    /** Null when the supplier gave null. */
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
