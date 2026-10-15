package me.moonscenty.alchemia.menu;

import me.moonscenty.alchemia.block.entity.FocalManipulatorBlockEntity;
import me.moonscenty.alchemia.item.FocusItem;
import me.moonscenty.alchemia.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * The window on a focal manipulator: one square for the focus, and a button for every upgrade the next rank
 * offers.
 *
 * <p>The bench itself is the container, so the focus stays on it when the window is closed and goes on being
 * worked on. The buttons are the menu's own: which upgrade was picked is the button's number, and the bench
 * answers for whether it will take it.
 */
public class FocalManipulatorMenu extends AbstractContainerMenu {
    /** Where the original's container put the focus and the player's own bag. */
    private static final int FOCUS_X = 88;
    private static final int FOCUS_Y = 60;
    private static final int PACK_X = 16;
    private static final int PACK_Y = 151;
    private static final int BELT_Y = 209;
    private static final int PITCH = 18;

    private final FocalManipulatorBlockEntity bench;

    public FocalManipulatorMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, found(inventory, buffer.readBlockPos()));
    }

    public FocalManipulatorMenu(int id, Inventory inventory, FocalManipulatorBlockEntity bench) {
        super(ModMenus.FOCAL_MANIPULATOR.get(), id);
        this.bench = bench;

        addSlot(new Slot(bench, 0, FOCUS_X, FOCUS_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof FocusItem;
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

    public FocalManipulatorBlockEntity bench() {
        return bench;
    }

    private static FocalManipulatorBlockEntity found(Inventory inventory, BlockPos at) {
        BlockEntity there = inventory.player.level().getBlockEntity(at);
        return there instanceof FocalManipulatorBlockEntity bench ? bench : null;
    }

    /** The buttons, numbered as the upgrade they stand for. */
    @Override
    public boolean clickMenuButton(Player player, int which) {
        var upgrades = me.moonscenty.alchemia.wand.FocusUpgrade.values();
        if (bench == null || which < 0 || which >= upgrades.length) {
            return false;
        }
        if (bench.start(upgrades[which], player)) {
            return true;
        }
        // refused: not enough levels, or nothing on the bench that could take it
        if (!player.level().isClientSide) {
            player.level().playSound(null, bench.getBlockPos(),
                    me.moonscenty.alchemia.registry.ModSounds.CRAFT_FAIL.get(),
                    net.minecraft.sounds.SoundSource.BLOCKS, 0.33F, 1.0F);
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return bench != null && bench.stillValid(player);
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
