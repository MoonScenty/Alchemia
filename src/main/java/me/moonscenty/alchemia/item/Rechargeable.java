package me.moonscenty.alchemia.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

public interface Rechargeable {
    boolean charge(ItemStack stack, ServerLevel level, BlockPos at, int amount);
}
