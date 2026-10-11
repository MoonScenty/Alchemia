package me.moonscenty.alchemia.menu;

import me.moonscenty.alchemia.entity.Autocaster;
import me.moonscenty.alchemia.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * The inside of an autocaster: one square for the focus, and the four things it may be told to mind.
 *
 * <p>The turret is its own container of one, so the square is the turret's hand and nothing has to be copied in
 * or written back. The four toggles are not slots and not a packet of their own either -- they are the menu's own
 * buttons, and what they are set to rides along on the entity, which the screen can see as well as the server.
 */
public class AutocasterMenu extends AbstractContainerMenu {
    /** Where the original's container put the focus square and the player's own bag. */
    private static final int FOCUS_X = 36;
    private static final int FOCUS_Y = 29;
    private static final int PACK_X = 8;
    private static final int PACK_Y = 84;
    private static final int BELT_Y = 142;
    private static final int PITCH = 18;

    private final Autocaster turret;

    /** Opened from the client's side, where all that came across the wire is which entity it was. */
    public AutocasterMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, found(inventory, buffer.readVarInt()));
    }

    public AutocasterMenu(int id, Inventory inventory, Autocaster turret) {
        super(ModMenus.AUTOCASTER.get(), id);
        this.turret = turret;

        addSlot(new Slot(turret, 0, FOCUS_X, FOCUS_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return Autocaster.takes(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, PACK_X + column * PITCH, PACK_Y + row * PITCH));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, PACK_X + column * PITCH, BELT_Y));
        }
    }

    /** The turret this window belongs to, which the screen reads its health and its toggles off. */
    public Autocaster turret() {
        return turret;
    }

    private static Autocaster found(Inventory inventory, int id) {
        return inventory.player.level().getEntity(id) instanceof Autocaster turret ? turret : null;
    }

    /** The four buttons, numbered as the bits they set. */
    @Override
    public boolean clickMenuButton(Player player, int which) {
        if (turret == null || !turret.stillValid(player) || which < 0 || which > Autocaster.FRIENDS) {
            return false;
        }
        turret.mind(which, !turret.minds(which));
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return turret != null && turret.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack held = slot.getItem();
        ItemStack copy = held.copy();

        if (index == 0) {
            if (!moveItemStackTo(held, 1, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(held, 0, 1, false)) {
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
