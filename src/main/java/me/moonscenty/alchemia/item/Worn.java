package me.moonscenty.alchemia.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

public final class Worn {
    public static final EquipmentSlot[] ARMOUR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
            EquipmentSlot.FEET};

    private Worn() {
    }

    public static List<ItemStack> stacks(Player player, EquipmentSlot... slots) {
        List<ItemStack> stacks = new ArrayList<>();
        for (EquipmentSlot slot : slots) {
            stacks.add(player.getItemBySlot(slot));
        }
        stacks.addAll(baubles(player));
        return stacks;
    }

    public static List<ItemStack> baubles(Player player) {
        List<ItemStack> baubles = new ArrayList<>();
        CuriosApi.getCuriosInventory(player).ifPresent(curios -> {
            var equipped = curios.getEquippedCurios();
            for (int i = 0; i < equipped.getSlots(); i++) {
                baubles.add(equipped.getStackInSlot(i));
            }
        });
        return baubles;
    }
}
