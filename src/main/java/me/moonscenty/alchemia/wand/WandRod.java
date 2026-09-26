package me.moonscenty.alchemia.wand;

import java.util.Optional;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.registry.ModWandParts;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * The shaft of a wand: what decides how much vis it can hold.
 * <p>
 * Some rods are cut from something that makes one kind of vis on its own — a blaze rod is always a little warm —
 * and those trickle that aspect back in for nothing, up to half of what the rod can hold. It never fills a wand,
 * but it does mean a fire wand is never quite empty.
 */
public class WandRod {
    private final int capacity;
    private final int craftCost;
    private final Optional<Holder<Aspect>> trickle;

    private WandRod(int capacity, int craftCost, Optional<Holder<Aspect>> trickle) {
        this.capacity = capacity;
        this.craftCost = craftCost;
        this.trickle = trickle;
    }

    public static WandRod plain(int capacity, int craftCost) {
        return new WandRod(capacity, craftCost, Optional.empty());
    }

    public static WandRod trickling(int capacity, int craftCost, Holder<Aspect> aspect) {
        return new WandRod(capacity, craftCost, Optional.of(aspect));
    }

    /** How much of each primal the rod holds. */
    public int capacity() {
        return capacity;
    }

    /** What it costs to work this into a wand, in vis. Spent at the altar in a later step, not here. */
    public int craftCost() {
        return craftCost;
    }

    /** The aspect this rod makes on its own, if it makes one. */
    public Optional<Holder<Aspect>> trickle() {
        return trickle;
    }

    public ResourceLocation id() {
        return ModWandParts.RODS.getKey(this);
    }

    /** The layer drawn along the wand, as {@code alchemia:item/wand/rod_wood}. */
    public ResourceLocation texture() {
        return Alchemia.id("item/wand/rod_" + id().getPath());
    }

    public Component displayName() {
        return Component.translatable("wand_rod." + id().getNamespace() + "." + id().getPath());
    }
}
