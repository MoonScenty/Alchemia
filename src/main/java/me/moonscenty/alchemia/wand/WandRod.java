package me.moonscenty.alchemia.wand;

import java.util.List;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModWandParts;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * The shaft of a wand: what decides how much vis it can hold.
 * <p>
 * Some rods are cut from something that makes one kind of vis on its own — a blaze rod is always a little warm —
 * and those trickle that aspect back in for nothing, up to half of what the rod can hold. It never fills a wand,
 * but it does mean a fire wand is never quite empty. The primal staff makes all six, one at a time.
 * <p>
 * A staff core is a rod too, a long one, and what is built on it is a staff: it holds more, hits like a club, and
 * makes what is worked through its focus a tenth cheaper, but it is too long to lay on a workbench.
 */
public class WandRod {
    private final int capacity;
    private final int craftCost;
    private final List<Holder<Aspect>> trickles;
    private final boolean staff;
    private final boolean potent;
    /** Which rod's picture it wears along its length: a staff core wears the rod it was made from. */
    private final String look;

    private WandRod(int capacity, int craftCost, List<Holder<Aspect>> trickles, boolean staff, boolean potent,
            String look) {
        this.capacity = capacity;
        this.craftCost = craftCost;
        this.trickles = trickles;
        this.staff = staff;
        this.potent = potent;
        this.look = look;
    }

    public static WandRod plain(int capacity, int craftCost) {
        return new WandRod(capacity, craftCost, List.of(), false, false, null);
    }

    public static WandRod trickling(int capacity, int craftCost, Holder<Aspect> aspect) {
        return new WandRod(capacity, craftCost, List.of(aspect), false, false, null);
    }

    /** A staff core cut from a rod of the given stuff, wearing that rod's picture. */
    public static WandRod staff(String look, int capacity, int craftCost) {
        return new WandRod(capacity, craftCost, List.of(), true, false, look);
    }

    public static WandRod tricklingStaff(String look, int capacity, int craftCost, Holder<Aspect> aspect) {
        return new WandRod(capacity, craftCost, List.of(aspect), true, false, look);
    }

    /** The primal staff: it makes all six primals, and lends a focus a step of potency. */
    public static WandRod primalStaff(int capacity, int craftCost) {
        return new WandRod(capacity, craftCost, List.of(ModAspects.AIR, ModAspects.FIRE, ModAspects.WATER,
                ModAspects.EARTH, ModAspects.ORDER, ModAspects.ENTROPY), true, true, "primal");
    }

    /** How much of each primal the rod holds. */
    public int capacity() {
        return capacity;
    }

    /** What it costs to work this into a wand, in vis. Spent at the altar in a later step, not here. */
    public int craftCost() {
        return craftCost;
    }

    /** The aspects this rod makes on its own, if it makes any. */
    public List<Holder<Aspect>> trickles() {
        return trickles;
    }

    /** Whether what is built on it is a staff rather than a wand. */
    public boolean staff() {
        return staff;
    }

    /** Whether it lends whatever focus is fitted to it a step of potency. */
    public boolean potent() {
        return potent;
    }

    public ResourceLocation id() {
        return ModWandParts.RODS.getKey(this);
    }

    /** The layer drawn along the wand, as {@code alchemia:item/wand/rod_wood}. */
    public ResourceLocation texture() {
        return Alchemia.id("item/wand/rod_" + (look != null ? look : id().getPath()));
    }

    public Component displayName() {
        return Component.translatable("wand_rod." + id().getNamespace() + "." + id().getPath());
    }
}
