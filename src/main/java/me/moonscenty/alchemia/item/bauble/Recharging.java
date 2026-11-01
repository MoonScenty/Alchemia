package me.moonscenty.alchemia.item.bauble;

import me.moonscenty.alchemia.item.Rechargeable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

public final class Recharging {
    private Recharging() {
    }

    public static boolean chargeable(ItemStack stack) {
        return stack.getItem() instanceof Rechargeable;
    }

    public static boolean charge(ItemStack stack, ServerLevel level, BlockPos at, int amount) {
        return stack.getItem() instanceof Rechargeable item && item.charge(stack, level, at, amount);
    }
}
