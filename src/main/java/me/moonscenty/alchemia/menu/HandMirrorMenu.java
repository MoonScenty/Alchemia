package me.moonscenty.alchemia.menu;

import me.moonscenty.alchemia.block.entity.MirrorBlockEntity;
import me.moonscenty.alchemia.item.HandMirrorItem;
import me.moonscenty.alchemia.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Looking into a hand mirror: one square, and whatever is put in it goes straight through to the mirror it is
 * linked to. The hand mirror itself cannot be moved while it is open.
 */
public class HandMirrorMenu extends AbstractContainerMenu {
    private static final int SLOT_X = 80;
    private static final int SLOT_Y = 24;
    private static final int PACK_X = 8;
    private static final int PACK_Y = 84;
    private static final int BELT_Y = 142;
    private static final int PITCH = 18;

    private final Player player;
    private final int heldIn;
    private final Container through = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            sendThrough();
        }
    };

    public HandMirrorMenu(int id, Inventory inventory, RegistryFriendlyByteBuf extra) {
        this(id, inventory, extra.readVarInt());
    }

    public HandMirrorMenu(int id, Inventory inventory, int heldIn) {
        super(ModMenus.HAND_MIRROR.get(), id);
        this.player = inventory.player;
        this.heldIn = heldIn;
        addSlot(new Slot(through, 0, SLOT_X, SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return !(stack.getItem() instanceof HandMirrorItem);
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, PACK_X + column * PITCH, PACK_Y + row * PITCH));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, PACK_X + column * PITCH, BELT_Y) {
                @Override
                public boolean mayPickup(Player taker) {
                    return getContainerSlot() != heldIn;
                }
            });
        }
    }

    /** Which square of the belt the hand mirror is in, for the panel to mark. */
    public int heldIn() {
        return heldIn;
    }

    /** Whatever is in the square goes through, all at once, to come out of the mirror a piece at a time. */
    private void sendThrough() {
        ItemStack sending = through.getItem(0);
        if (sending.isEmpty() || player.level().isClientSide) {
            return;
        }
        ItemStack mirror = player.getInventory().getItem(heldIn);
        MirrorBlockEntity target = HandMirrorItem.target(player.level(), mirror);
        if (target == null) {
            HandMirrorItem.broken(player.level(), player, mirror);
            return;
        }
        target.receive(sending.copy());
        through.setItem(0, ItemStack.EMPTY);
        player.level().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS,
                0.1F, 1.0F);
    }

    @Override
    public void clicked(int slotId, int button, ClickType type, Player clicker) {
        if (type == ClickType.SWAP && button == heldIn) {
            return;
        }
        super.clicked(slotId, button, type, clicker);
    }

    @Override
    public ItemStack quickMoveStack(Player taker, int index) {
        Slot from = slots.get(index);
        if (!from.hasItem() || from.getItem().getItem() instanceof HandMirrorItem) {
            return ItemStack.EMPTY;
        }
        ItemStack held = from.getItem();
        ItemStack copy = held.copy();
        if (index == 0) {
            if (!moveItemStackTo(held, 1, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(held, 0, 1, false)) {
            return ItemStack.EMPTY;
        }
        if (held.isEmpty()) {
            from.set(ItemStack.EMPTY);
        } else {
            from.setChanged();
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player viewer) {
        return viewer.getInventory().getItem(heldIn).getItem() instanceof HandMirrorItem;
    }

    /** Anything left in the square when it is closed goes back to the one holding it. */
    @Override
    public void removed(Player closer) {
        super.removed(closer);
        clearContainer(closer, through);
    }
}
