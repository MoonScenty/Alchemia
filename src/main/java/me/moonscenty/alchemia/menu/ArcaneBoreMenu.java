package me.moonscenty.alchemia.menu;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.block.entity.ArcaneBoreBlockEntity;
import me.moonscenty.alchemia.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Standing at an arcane bore: its focus on the left, its pickaxe on the right, laid out as the original's. */
public class ArcaneBoreMenu extends AbstractContainerMenu {
    private static final int FOCUS_X = 26;
    private static final int PICK_X = 74;
    private static final int SLOTS_Y = 18;
    private static final int PACK_X = 8;
    private static final int PACK_Y = 59;
    private static final int BELT_Y = 117;
    private static final int PITCH = 18;

    @Nullable
    private final ArcaneBoreBlockEntity bore;
    private final Container held;

    public ArcaneBoreMenu(int id, Inventory inventory, RegistryFriendlyByteBuf extra) {
        this(id, inventory, inventory.player.level().getBlockEntity(extra.readBlockPos())
                instanceof ArcaneBoreBlockEntity found ? found : null);
    }

    public ArcaneBoreMenu(int id, Inventory inventory, @Nullable ArcaneBoreBlockEntity bore) {
        super(ModMenus.ARCANE_BORE.get(), id);
        this.bore = bore;
        this.held = bore == null ? new SimpleContainer(2) : bore;
        addSlot(new Slot(held, ArcaneBoreBlockEntity.SLOT_FOCUS, FOCUS_X, SLOTS_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ArcaneBoreBlockEntity.isFocus(stack);
            }
        });
        addSlot(new Slot(held, ArcaneBoreBlockEntity.SLOT_PICK, PICK_X, SLOTS_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ArcaneBoreBlockEntity.isPick(stack);
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

    @Nullable
    public ArcaneBoreBlockEntity bore() {
        return bore;
    }

    @Override
    public ItemStack quickMoveStack(Player taker, int index) {
        Slot from = slots.get(index);
        if (!from.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack held = from.getItem();
        ItemStack copy = held.copy();
        if (index < 2) {
            if (!moveItemStackTo(held, 2, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (ArcaneBoreBlockEntity.isFocus(held)) {
            if (!moveItemStackTo(held, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (ArcaneBoreBlockEntity.isPick(held)) {
            if (!moveItemStackTo(held, 1, 2, false)) {
                return ItemStack.EMPTY;
            }
        } else {
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
        return bore != null && bore.stillValid(viewer);
    }
}
