package me.moonscenty.alchemia.menu;

import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.block.entity.EssentiaSmelterBlockEntity;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
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
 * Standing at a smelter: what goes in, what burns, and how far both have got.
 * <p>
 * Nothing comes out of it, so there is no result square. What the work makes rises out of the top into whatever is
 * stacked there, and the only sign of it here is the gauge filling up.
 */
public class EssentiaSmelterMenu extends AbstractContainerMenu {
    private static final int SMELTER_SLOTS = 2;

    // Where the pieces sit on the drawn panel.
    private static final int INPUT_X = 80;
    private static final int INPUT_Y = 17;
    private static final int FUEL_X = 80;
    private static final int FUEL_Y = 53;
    private static final int PACK_X = 8;
    private static final int PACK_Y = 84;
    private static final int BELT_Y = 142;
    private static final int PITCH = 18;

    private final Container smelter;
    private final ContainerData readings;
    private final ContainerLevelAccess access;
    /** Where the block is, so the screen can read what is dissolved in it: too much to send as numbers. */
    private final BlockPos at;

    /** What the client builds, from the position sent when the screen was opened. */
    public EssentiaSmelterMenu(int id, Inventory inventory, RegistryFriendlyByteBuf extra) {
        this(id, inventory, new SimpleContainer(SMELTER_SLOTS),
                new SimpleContainerData(EssentiaSmelterBlockEntity.READINGS),
                ContainerLevelAccess.NULL, extra.readBlockPos());
    }

    public EssentiaSmelterMenu(int id, Inventory inventory, Container smelter, ContainerData readings,
            ContainerLevelAccess access, BlockPos at) {
        super(ModMenus.ESSENTIA_SMELTER.get(), id);
        this.smelter = smelter;
        this.readings = readings;
        this.access = access;
        this.at = at;
        checkContainerSize(smelter, SMELTER_SLOTS);
        addDataSlots(readings);

        addSlot(new Slot(smelter, EssentiaSmelterBlockEntity.SLOT_INPUT, INPUT_X, INPUT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return smelter.canPlaceItem(EssentiaSmelterBlockEntity.SLOT_INPUT, stack);
            }
        });
        addSlot(new Slot(smelter, EssentiaSmelterBlockEntity.SLOT_FUEL, FUEL_X, FUEL_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return smelter.canPlaceItem(EssentiaSmelterBlockEntity.SLOT_FUEL, stack);
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

    public BlockPos at() {
        return at;
    }

    /** How much of the fuel is left, as a share. */
    public float burning() {
        int lasts = readings.get(EssentiaSmelterBlockEntity.READING_BURNS_FOR);
        return lasts <= 0 ? 0.0F : readings.get(EssentiaSmelterBlockEntity.READING_BURNING) / (float) lasts;
    }

    /** How far the thing being broken down has got. */
    public float cooked() {
        int takes = readings.get(EssentiaSmelterBlockEntity.READING_COOKS_FOR);
        return takes <= 0 ? 0.0F : readings.get(EssentiaSmelterBlockEntity.READING_COOKED) / (float) takes;
    }

    /** How full it is of what it has boiled off. */
    public float filled() {
        return Math.min(1.0F, readings.get(EssentiaSmelterBlockEntity.READING_HELD)
                / (float) EssentiaSmelterBlockEntity.CAPACITY);
    }

    public int held() {
        return readings.get(EssentiaSmelterBlockEntity.READING_HELD);
    }

    /** What is dissolved, read off the block itself rather than sent through the readings. */
    public AspectList dissolved(net.minecraft.world.level.Level level) {
        return level != null && level.getBlockEntity(at) instanceof EssentiaSmelterBlockEntity found
                ? found.held()
                : AspectList.EMPTY;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack held = slot.getItem();
        ItemStack copy = held.copy();

        if (index < SMELTER_SLOTS) {
            if (!moveItemStackTo(held, SMELTER_SLOTS, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(held, 0, SMELTER_SLOTS, false)) {
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
        return stillValid(access, player, ModBlocks.ESSENTIA_SMELTER.get());
    }
}
