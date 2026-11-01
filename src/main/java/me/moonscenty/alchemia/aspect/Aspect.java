package me.moonscenty.alchemia.aspect;

import java.util.List;
import java.util.Optional;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.registry.ModAspects;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class Aspect {
    private final int color;
    private final Blending blending;
    private final Optional<List<Holder<Aspect>>> components;

    public enum Blending {
        ADDITIVE,
        FLAT
    }

    private Aspect(int color, Blending blending, Optional<List<Holder<Aspect>>> components) {
        this.color = color;
        this.blending = blending;
        this.components = components;
    }

    public static Aspect primal(int color, Blending blending) {
        return new Aspect(color, blending, Optional.empty());
    }

    public static Aspect compound(int color, Holder<Aspect> first, Holder<Aspect> second) {
        return compound(color, Blending.ADDITIVE, first, second);
    }

    public static Aspect compound(int color, Blending blending, Holder<Aspect> first, Holder<Aspect> second) {
        return new Aspect(color, blending, Optional.of(List.of(first, second)));
    }

    public int color() {
        return color;
    }

    public Blending blending() {
        return blending;
    }

    public boolean isPrimal() {
        return components.isEmpty();
    }

    public Optional<List<Holder<Aspect>>> components() {
        return components;
    }

    public boolean hasComponent(Holder<Aspect> other) {
        return components.map(parts -> parts.stream().anyMatch(part -> part.value() == other.value())).orElse(false);
    }

    public ResourceLocation id() {
        return ModAspects.REGISTRY.getKey(this);
    }

    public String tag() {
        return id().getPath();
    }

    public Component displayName() {
        return Component.translatable(translationKey());
    }

    public Component description() {
        return Component.translatable(translationKey() + ".description");
    }

    public String translationKey() {
        return "aspect." + Alchemia.MODID + "." + tag();
    }

    public ResourceLocation icon() {
        return Alchemia.id("textures/aspect/" + tag() + ".png");
    }

    public int complexity() {
        return components.map(parts -> 1 + parts.stream()
                .mapToInt(part -> part.value().complexity())
                .max()
                .orElse(0)).orElse(0);
    }

    @Override
    public String toString() {
        return "Aspect[" + (ModAspects.REGISTRY.containsValue(this) ? tag() : "unregistered") + "]";
    }
}
