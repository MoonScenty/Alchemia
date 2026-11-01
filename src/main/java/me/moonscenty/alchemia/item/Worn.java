package me.moonscenty.alchemia.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

/** What a player has on: the stacks in some equipment slots, and every bauble. */
public final class Worn {
    /** The four armour slots, head first. */
    public static final EquipmentSlot[] ARMOUR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
            EquipmentSlot.FEET};

    private Worn() {
    }

    /** The stacks in these slots, in this order, then every bauble. */
    public static List<ItemStack> stacks(Player player, EquipmentSlot... slots) {
        List<ItemStack> stacks = new ArrayList<>();
        for (EquipmentSlot slot : slots) {
            stacks.add(player.getItemBySlot(slot));
        }
        stacks.addAll(baubles(player));
        return stacks;
    }

    /** Every curios slot's stack, empty ones included. */
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
