package me.moonscenty.alchemia.wand;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.registry.ModWandParts;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public record WandCap(float discount, int chargeBonus, int craftCost) {

    public ResourceLocation id() {
        return ModWandParts.CAPS.getKey(this);
    }

    public ResourceLocation texture() {
        return Alchemia.id("item/wand/cap_" + id().getPath());
    }

    public Component displayName() {
        return Component.translatable("wand_cap." + id().getNamespace() + "." + id().getPath());
    }
}
