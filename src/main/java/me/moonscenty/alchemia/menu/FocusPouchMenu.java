package me.moonscenty.alchemia.menu;

import java.util.ArrayList;
import java.util.List;

import me.moonscenty.alchemia.item.FocusPouchItem;
import me.moonscenty.alchemia.registry.ModMenus;
import me.moonscenty.alchemia.registry.ModTags;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * The inside of a focus pouch.
 *
 * <p>The pouch is not a block, so there is nowhere in the world for its contents to live: the slots are a working
 * copy that is written back onto the item every time anything moves. That is more writing than a chest does, but a
 * pouch holds eighteen things at most and the alternative is a bag that forgets itself when the game is closed
 * mid-shuffle.
 *
 * <p>The pouch being edited cannot be moved while it is open. Its own slot in the bag is nailed down, so there is
 * no way to shift-click the pouch into itself or to drop it and leave the window looking at nothing.
 */
public class FocusPouchMenu extends AbstractContainerMenu {
    // where the slots sit on the drawn panel; tools/gen_pouch_gui.py prints these
    private static final int FOCI_X = 35;
    private static final int FOCI_Y = 18;
    private static final int PACK_X = 8;
    private static final int PACK_Y = 84;
    private static final int BELT_Y = 142;
    private static final int PITCH = 18;

    private final Player player;
    private final InteractionHand hand;
    /** Which slot of the bag the pouch is in, or -1 when it is in the off hand and so not on the screen. */
    private final int locked;
    private final Container inside;

    public FocusPouchMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, buffer.readEnum(InteractionHand.class));
    }

    public FocusPouchMenu(int id, Inventory inventory, InteractionHand hand) {
        super(ModMenus.FOCUS_POUCH.get(), id);
        this.player = inventory.player;
        this.hand = hand;
        this.locked = hand == InteractionHand.MAIN_HAND ? inventory.selected : -1;
        this.inside = loaded();

        for (int row = 0; row < FocusPouchItem.DOWN; row++) {
            for (int column = 0; column < FocusPouchItem.ACROSS; column++) {
                addSlot(new FocusSlot(inside, column + row * FocusPouchItem.ACROSS,
                        FOCI_X + column * PITCH, FOCI_Y + row * PITCH));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(held(inventory, column + row * 9 + 9, PACK_X + column * PITCH, PACK_Y + row * PITCH));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(held(inventory, column, PACK_X + column * PITCH, BELT_Y));
        }
    }

    /** The pouch being edited, taken from the hand it was opened with rather than remembered as a stack. */
    private ItemStack pouch() {
        return player.getItemInHand(hand);
    }

    private Container loaded() {
        SimpleContainer container = new SimpleContainer(FocusPouchItem.SIZE) {
            @Override
            public void setChanged() {
                super.setChanged();
                save();
            }
        };
        for (int slot = 0; slot < FocusPouchItem.SIZE; slot++) {
            container.setItem(slot, FocusPouchItem.item(pouch(), slot));
        }
        return container;
    }

    /** Writes the slots back onto the item. */
    private void save() {
        // the slots are still being filled from the pouch; writing them back now would be writing half a pouch
        if (inside == null) {
            return;
        }
        ItemStack pouch = pouch();
        if (!(pouch.getItem() instanceof FocusPouchItem)) {
            return;
        }
        List<ItemStack> items = new ArrayList<>(FocusPouchItem.SIZE);
        for (int slot = 0; slot < FocusPouchItem.SIZE; slot++) {
            items.add(inside.getItem(slot).copy());
        }
        FocusPouchItem.setContents(pouch, items);
    }

    /** A slot of the player's own bag, with the pouch's own square nailed down while it is open. */
    private Slot held(Inventory inventory, int index, int x, int y) {
        if (index != locked) {
            return new Slot(inventory, index, x, y);
        }
        return new Slot(inventory, index, x, y) {
            @Override
            public boolean mayPickup(Player taker) {
                return false;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        };
    }

    @Override
    public void removed(Player taker) {
        super.removed(taker);
        save();
    }

    /** Only while the pouch is still in the hand it was opened from. */
    @Override
    public boolean stillValid(Player taker) {
        return pouch().getItem() instanceof FocusPouchItem;
    }

    @Override
    public ItemStack quickMoveStack(Player taker, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack held = slot.getItem();
        ItemStack copy = held.copy();

        if (index < FocusPouchItem.SIZE) {
            if (!moveItemStackTo(held, FocusPouchItem.SIZE, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(held, 0, FocusPouchItem.SIZE, false)) {
            return ItemStack.EMPTY;
        }

        if (held.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    /** A square that takes a focus and nothing else, one to a square. */
    private class FocusSlot extends Slot {
        FocusSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(ModTags.Items.FOCI);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public void setChanged() {
            super.setChanged();
            save();
        }
    }
}
