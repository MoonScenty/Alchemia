package me.moonscenty.alchemia.menu;

import javax.annotation.Nullable;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class AlchemiaMenu extends AbstractContainerMenu {
    private static final int PITCH = 18;
    private static final int BELT_BELOW = 58;

    protected AlchemiaMenu(@Nullable MenuType<?> type, int id) {
        super(type, id);
    }

    @FunctionalInterface
    protected interface SlotMaker {
        Slot make(Inventory inventory, int index, int x, int y);
    }

    protected void addPlayerInventory(Inventory inventory, int x, int y) {
        addPlayerInventory(inventory, x, y, Slot::new);
    }

    protected void addPlayerInventory(Inventory inventory, int x, int y, SlotMaker maker) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(maker.make(inventory, column + row * 9 + 9, x + column * PITCH, y + row * PITCH));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(maker.make(inventory, column, x + column * PITCH, y + BELT_BELOW));
        }
    }

    protected ItemStack quickMoveBetween(int index, int machineSlots) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack held = slot.getItem();
        ItemStack copy = held.copy();
        if (index < machineSlots) {
            if (!moveItemStackTo(held, machineSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(held, 0, machineSlots, false)) {
            return ItemStack.EMPTY;
        }
        if (held.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }
}
