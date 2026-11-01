package me.moonscenty.alchemia.menu;

import javax.annotation.Nullable;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** A menu with the player's own inventory laid out under the machine's slots. */
public abstract class AlchemiaMenu extends AbstractContainerMenu {
    private static final int PITCH = 18;
    /** How far below the top of the pack the hotbar sits. */
    private static final int BELT_BELOW = 58;

    protected AlchemiaMenu(@Nullable MenuType<?> type, int id) {
        super(type, id);
    }

    /** Makes one of the player's slots, for a menu whose own pockets behave differently. */
    @FunctionalInterface
    protected interface SlotMaker {
        Slot make(Inventory inventory, int index, int x, int y);
    }

    /** The three rows of the pack with their top left corner at (x, y), and the hotbar under them. */
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

    /**
     * Shift-click between the machine's first so many slots and the player's. What a machine slot will take is up to
     * that slot's own mayPlace.
     */
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
