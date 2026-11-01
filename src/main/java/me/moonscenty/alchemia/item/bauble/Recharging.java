package me.moonscenty.alchemia.item.bauble;

import me.moonscenty.alchemia.item.Rechargeable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

/**
 * What can be charged out of the aura a drop at a time -- a wand, the verdant heart band and the thaumostatic
 * harness -- as the recharge
 * pedestal and the amulet of vis charge it.
 */
public final class Recharging {
    private Recharging() {
    }

    public static boolean chargeable(ItemStack stack) {
        return stack.getItem() instanceof Rechargeable;
    }

    /** Draws this much into it from the aura here; whether anything went in. */
    public static boolean charge(ItemStack stack, ServerLevel level, BlockPos at, int amount) {
        return stack.getItem() instanceof Rechargeable item && item.charge(stack, level, at, amount);
    }
}
