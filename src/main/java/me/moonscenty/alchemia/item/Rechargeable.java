package me.moonscenty.alchemia.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

/** Something a recharge pedestal or an amulet of vis can charge out of the aura. */
public interface Rechargeable {
    /** Draws up to this much into the stack from the aura here; whether anything went in. */
    boolean charge(ItemStack stack, ServerLevel level, BlockPos at, int amount);
}
