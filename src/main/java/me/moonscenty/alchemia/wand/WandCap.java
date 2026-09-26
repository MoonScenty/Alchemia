package me.moonscenty.alchemia.wand;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.registry.ModWandParts;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * The metal at each end of a wand: what decides how dearly it spends and how fast it fills.
 * <p>
 * Iron is the cheapest to come by and the worst at the work, spending a tenth more than the price asked. The rest
 * spend the price as it stands, and buy their keep with how quickly they draw the aura in instead.
 */
public record WandCap(float discount, int chargeBonus, int craftCost) {

    public ResourceLocation id() {
        return ModWandParts.CAPS.getKey(this);
    }

    /** The layer drawn at the ends of the wand, as {@code alchemia:item/wand/cap_iron}. */
    public ResourceLocation texture() {
        return Alchemia.id("item/wand/cap_" + id().getPath());
    }

    public Component displayName() {
        return Component.translatable("wand_cap." + id().getNamespace() + "." + id().getPath());
    }
}
