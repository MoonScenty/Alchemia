package me.moonscenty.alchemia.menu;

import me.moonscenty.alchemia.block.entity.SpaBlockEntity;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModMenus;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

/**
 * Standing at a spa: the pocket for bath salts, the tank, and the switch between mixing and pouring as it is.
 */
public class SpaMenu extends AbstractContainerMenu {
    /** The one button on the panel: mix, or pour what is in the tank as it is. */
    public static final int TOGGLE_MIXING = 0;

    // Where the pieces sit on the original's panel.
    private static final int SALTS_X = 65;
    private static final int SALTS_Y = 31;
    private static final int PACK_X = 8;
    private static final int PACK_Y = 84;
    private static final int BELT_Y = 142;
    private static final int PITCH = 18;

    private final Container spa;
    private final ContainerData readings;
    private final ContainerLevelAccess access;
    private final SpaBlockEntity block;

    /** What the client builds. */
    public SpaMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(1), new SimpleContainerData(SpaBlockEntity.READINGS),
                ContainerLevelAccess.NULL, null);
    }

    public SpaMenu(int id, Inventory inventory, SpaBlockEntity spa, ContainerData readings,
            ContainerLevelAccess access) {
        this(id, inventory, spa, readings, access, spa);
    }

    private SpaMenu(int id, Inventory inventory, Container spa, ContainerData readings, ContainerLevelAccess access,
            SpaBlockEntity block) {
        super(ModMenus.SPA.get(), id);
        this.spa = spa;
        this.readings = readings;
        this.access = access;
        this.block = block;
        checkContainerSize(spa, 1);
        addDataSlots(readings);

        addSlot(new Slot(spa, 0, SALTS_X, SALTS_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.BATH_SALTS.get());
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

    @Override
    public boolean clickMenuButton(Player player, int which) {
        if (which == TOGGLE_MIXING && block != null) {
            block.toggleMixing();
            return true;
        }
        return false;
    }

    public int amount() {
        return readings.get(SpaBlockEntity.READING_AMOUNT);
    }

    public Fluid fluid() {
        return BuiltInRegistries.FLUID.byId(readings.get(SpaBlockEntity.READING_FLUID));
    }

    public boolean mixing() {
        return readings.get(SpaBlockEntity.READING_MIXING) != 0;
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
        } else if (!held.is(ModItems.BATH_SALTS.get()) || !moveItemStackTo(held, 0, 1, false)) {
            return ItemStack.EMPTY;
        }
        if (held.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.SPA.get());
    }
}
