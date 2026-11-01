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
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FocusPouchMenu extends AlchemiaMenu {
    // original's layout
    private static final int FOCI_X = 37;
    private static final int FOCI_Y = 51;
    private static final int PACK_X = 8;
    private static final int PACK_Y = 151;
    private static final int PITCH = 18;

    private final Player player;
    private final InteractionHand hand;
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
        addPlayerInventory(inventory, PACK_X, PACK_Y, this::held);
    }

    public int locked() {
        return locked;
    }

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

    private void save() {
        // still loading the slots from the pouch; writing back now would save half a pouch
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

    @Override
    public boolean stillValid(Player taker) {
        return pouch().getItem() instanceof FocusPouchItem;
    }

    @Override
    public ItemStack quickMoveStack(Player taker, int index) {
        return quickMoveBetween(index, FocusPouchItem.SIZE);
    }

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
