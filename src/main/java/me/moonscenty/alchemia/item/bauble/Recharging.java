package me.moonscenty.alchemia.item.bauble;

import me.moonscenty.alchemia.item.WandItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

/**
 * What can be charged out of the aura a drop at a time -- a wand, and the verdant heart band -- as the recharge
 * pedestal and the amulet of vis charge it.
 */
public final class Recharging {
    private Recharging() {
    }

    public static boolean chargeable(ItemStack stack) {
        return stack.getItem() instanceof WandItem || stack.getItem() instanceof VerdantHeartItem;
    }

    /** Draws this much into it from the aura here; whether anything went in. */
    public static boolean charge(ItemStack stack, ServerLevel level, BlockPos at, int amount) {
        if (stack.getItem() instanceof WandItem) {
            return WandItem.chargeFrom(stack, level, at, amount) != null;
        }
        if (stack.getItem() instanceof VerdantHeartItem) {
            return VerdantHeartItem.recharge(stack, level, at, amount);
        }
        return false;
    }
}
