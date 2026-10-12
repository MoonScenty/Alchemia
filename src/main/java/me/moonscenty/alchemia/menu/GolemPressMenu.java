package me.moonscenty.alchemia.menu;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.block.entity.GolemPressBlockEntity;
import me.moonscenty.alchemia.golem.GolemDesign;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModMenus;
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

/**
 * Standing at a golem press: the slot the golem drops into, and the gauge of how much more it has to press. The
 * choosing is all the screen's; it only says what it chose when somebody asks for it to be made.
 */
public class GolemPressMenu extends AbstractContainerMenu {
    // Where the pieces sit on the original's panel.
    private static final int SLOT_X = 160;
    private static final int SLOT_Y = 104;
    private static final int PACK_X = 24;
    private static final int PACK_Y = 142;
    private static final int BELT_Y = 200;
    private static final int PITCH = 18;

    private final Container press;
    private final ContainerData readings;
    private final ContainerLevelAccess access;
    @Nullable
    private final GolemPressBlockEntity block;

    /** What the client builds. */
    public GolemPressMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(1), new SimpleContainerData(GolemPressBlockEntity.READINGS),
                ContainerLevelAccess.NULL, null);
    }

    public GolemPressMenu(int id, Inventory inventory, GolemPressBlockEntity press, ContainerData readings,
            ContainerLevelAccess access) {
        this(id, inventory, press, readings, access, press);
    }

    private GolemPressMenu(int id, Inventory inventory, Container press, ContainerData readings,
            ContainerLevelAccess access, @Nullable GolemPressBlockEntity block) {
        super(ModMenus.GOLEM_PRESS.get(), id);
        this.press = press;
        this.readings = readings;
        this.access = access;
        this.block = block;
        checkContainerSize(press, 1);
        addDataSlots(readings);

        addSlot(new Slot(press, 0, SLOT_X, SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
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

    /** Asked for by the screen: press this golem, if its maker knows how and has what it takes. */
    public boolean start(GolemDesign design, Player player) {
        return block != null && design.rank() == 0 && design.known(player) && block.start(design, player);
    }

    /** How many measures of machina are left to press in. */
    public int cost() {
        return readings.get(GolemPressBlockEntity.READING_COST);
    }

    public int maxCost() {
        return readings.get(GolemPressBlockEntity.READING_MAX);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack held = slot.getItem();
        ItemStack copy = held.copy();
        // the slot is only for taking golems out of
        if (index != 0 || !moveItemStackTo(held, 1, slots.size(), true)) {
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
        return stillValid(access, player, ModBlocks.GOLEM_PRESS.get());
    }
}
