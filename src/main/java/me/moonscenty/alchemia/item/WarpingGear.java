package me.moonscenty.alchemia.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface WarpingGear {
    EquipmentSlot[] COUNTED = {EquipmentSlot.MAINHAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST,
            EquipmentSlot.LEGS, EquipmentSlot.FEET};

    int warp(ItemStack stack, Player holder);

    static int carried(Player holder) {
        int total = 0;
        for (ItemStack stack : Worn.stacks(holder, COUNTED)) {
            total += of(stack, holder);
        }
        return total;
    }

    static int of(ItemStack stack, Player holder) {
        if (stack.isEmpty()) {
            return 0;
        }
        int warp = stack.getOrDefault(me.moonscenty.alchemia.registry.ModDataComponents.WARP.get(), 0)
                + VoidGear.warp(stack);
        if (stack.getItem() instanceof WarpingGear gear) {
            warp += gear.warp(stack, holder);
        }
        return warp;
    }
}
