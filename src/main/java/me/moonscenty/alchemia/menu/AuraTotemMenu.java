package me.moonscenty.alchemia.menu;

import me.moonscenty.alchemia.aura.ShardTuning;
import me.moonscenty.alchemia.block.entity.AuraTotemBlockEntity;
import me.moonscenty.alchemia.registry.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Standing at an aura totem: the pocket its shards burn from, and how long the one burning has left.
 */
public class AuraTotemMenu extends AbstractContainerMenu {
    // Where the pieces sit on the original's panel.
    private static final int SHARDS_X = 80;
    private static final int SHARDS_Y = 24;
    private static final int PACK_X = 8;
    private static final int PACK_Y = 84;
    private static final int BELT_Y = 142;
    private static final int PITCH = 18;

    private final ContainerData readings;
    private final Container totem;

    /** What the client builds. */
    public AuraTotemMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(1), new SimpleContainerData(AuraTotemBlockEntity.READINGS));
    }

    public AuraTotemMenu(int id, Inventory inventory, Container totem, ContainerData readings) {
        super(ModMenus.AURA_TOTEM.get(), id);
        this.totem = totem;
        this.readings = readings;
        checkContainerSize(totem, 1);
        addDataSlots(readings);

        addSlot(new Slot(totem, 0, SHARDS_X, SHARDS_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ShardTuning.isShard(stack);
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

    /** How much of the burning shard is left, as a share. */
    public float left() {
        return readings.get(AuraTotemBlockEntity.READING_TIME) / (float) AuraTotemBlockEntity.LASTS;
    }

    public int tuning() {
        return readings.get(AuraTotemBlockEntity.READING_TUNING);
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
        } else if (!ShardTuning.isShard(held) || !moveItemStackTo(held, 0, 1, false)) {
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
        return totem.stillValid(player);
    }
}
