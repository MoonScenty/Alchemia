package me.moonscenty.alchemia.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

public interface Rechargeable {
    boolean charge(ItemStack stack, ServerLevel level, BlockPos at, int amount);

    // 0 to 1 for the charge meters and tooltip; below 0 means it shows none
    default float chargeLevel(ItemStack stack) {
        return -1.0F;
    }
}
