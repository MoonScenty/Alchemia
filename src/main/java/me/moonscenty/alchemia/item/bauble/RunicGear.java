package me.moonscenty.alchemia.item.bauble;

import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.registry.ModTags;
import net.minecraft.world.item.ItemStack;

public interface RunicGear {
    int runicCharge(ItemStack stack);

    default RunicShield.Upgrade upgrade(ItemStack stack) {
        return RunicShield.Upgrade.NONE;
    }

    static boolean augmentable(ItemStack stack) {
        return stack.is(ModTags.Items.RUNIC_AUGMENTABLE);
    }

    static int hardening(ItemStack stack) {
        return augmentable(stack) ? stack.getOrDefault(ModDataComponents.RUNIC_HARDENING.get(), 0) : 0;
    }

    static int charge(ItemStack stack) {
        int own = stack.getItem() instanceof RunicGear gear ? gear.runicCharge(stack) : 0;
        return own + hardening(stack);
    }
}
