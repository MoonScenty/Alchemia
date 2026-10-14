package me.moonscenty.alchemia.item.bauble;

import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.registry.ModTags;
import net.minecraft.world.item.ItemStack;

/**
 * Something that holds up a runic shield while it is worn: how much shield it is worth, before any hardening runic
 * augmentation has worked into it.
 * <p>
 * Every piece of the mod's armour and every bauble can be augmented, as in the original, even the ones worth
 * nothing of their own; what may be augmented is a tag, and only the baubles made for it have a worth of their own.
 */
public interface RunicGear {
    /** The shield this is worth before hardening. */
    int runicCharge(ItemStack stack);

    /** What upgrade the shield gets from this, if any. */
    default RunicShield.Upgrade upgrade(ItemStack stack) {
        return RunicShield.Upgrade.NONE;
    }

    /** Whether runic augmentation will take to this at all. */
    static boolean augmentable(ItemStack stack) {
        return stack.is(ModTags.Items.RUNIC_AUGMENTABLE);
    }

    /** How many times this has been hardened. */
    static int hardening(ItemStack stack) {
        return augmentable(stack) ? stack.getOrDefault(ModDataComponents.RUNIC_HARDENING.get(), 0) : 0;
    }

    /** All the shield this is worth: its own, and its hardening. */
    static int charge(ItemStack stack) {
        int own = stack.getItem() instanceof RunicGear gear ? gear.runicCharge(stack) : 0;
        return own + hardening(stack);
    }
}
