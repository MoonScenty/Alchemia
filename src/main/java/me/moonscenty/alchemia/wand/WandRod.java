package me.moonscenty.alchemia.wand;

import java.util.List;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModWandParts;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class WandRod {
    private final int capacity;
    private final int craftCost;
    private final List<Holder<Aspect>> trickles;
    private final boolean staff;
    private final boolean potent;
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

    public static WandRod staff(String look, int capacity, int craftCost) {
        return new WandRod(capacity, craftCost, List.of(), true, false, look);
    }

    public static WandRod tricklingStaff(String look, int capacity, int craftCost, Holder<Aspect> aspect) {
        return new WandRod(capacity, craftCost, List.of(aspect), true, false, look);
    }

    public static WandRod primalStaff(int capacity, int craftCost) {
        return new WandRod(capacity, craftCost, List.of(ModAspects.AIR, ModAspects.FIRE, ModAspects.WATER,
                ModAspects.EARTH, ModAspects.ORDER, ModAspects.ENTROPY), true, true, "primal");
    }

    public int capacity() {
        return capacity;
    }

    public int craftCost() {
        return craftCost;
    }

    public List<Holder<Aspect>> trickles() {
        return trickles;
    }

    public boolean staff() {
        return staff;
    }

    public boolean potent() {
        return potent;
    }

    public ResourceLocation id() {
        return ModWandParts.RODS.getKey(this);
    }

    public ResourceLocation texture() {
        return Alchemia.id("item/wand/rod_" + (look != null ? look : id().getPath()));
    }

    public Component displayName() {
        return Component.translatable("wand_rod." + id().getNamespace() + "." + id().getPath());
    }
}
