package me.moonscenty.alchemia.aspect;

import java.util.List;
import java.util.Optional;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.registry.ModAspects;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * One of the essences everything in the world is made of.
 * <p>
 * Six aspects are primal and stand on their own; the rest are compounds, each born of exactly two others. The id is the
 * latin name the original used — {@code alchemia:aer}, {@code alchemia:terra} and so on.
 */
public class Aspect {
    private final int color;
    private final Blending blending;
    private final Optional<List<Holder<Aspect>>> components;

    /** How the icon is drawn. Dim aspects are painted flat so they do not glow. */
    public enum Blending {
        ADDITIVE,
        FLAT
    }

    private Aspect(int color, Blending blending, Optional<List<Holder<Aspect>>> components) {
        this.color = color;
        this.blending = blending;
        this.components = components;
    }

    /** A primal aspect, made of nothing else. */
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

    /** The two aspects this one is made of, or empty when it is primal. */
    public Optional<List<Holder<Aspect>>> components() {
        return components;
    }

    /** Whether this aspect is made partly of the given one. Primals are made of nothing, so they never are. */
    public boolean hasComponent(Holder<Aspect> other) {
        return components.map(parts -> parts.stream().anyMatch(part -> part.value() == other.value())).orElse(false);
    }

    public ResourceLocation id() {
        return ModAspects.REGISTRY.getKey(this);
    }

    /** The latin name, as in {@code aer}. */
    public String tag() {
        return id().getPath();
    }

    public Component displayName() {
        return Component.translatable(translationKey());
    }

    /** A short gloss of what the aspect stands for. */
    public Component description() {
        return Component.translatable(translationKey() + ".description");
    }

    public String translationKey() {
        return "aspect." + Alchemia.MODID + "." + tag();
    }

    /** The icon drawn for this aspect, white on transparent. */
    public ResourceLocation icon() {
        return Alchemia.id("textures/aspect/" + tag() + ".png");
    }

    /** How many primal aspects deep this one is, counting a primal as 0. Deeper aspects are rarer and worth more. */
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
