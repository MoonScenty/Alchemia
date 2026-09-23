package me.moonscenty.alchemia.menu;

import java.util.Optional;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.block.entity.ResearchTableBlockEntity;
import me.moonscenty.alchemia.item.ResearchNoteItem;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.registry.ModMenus;
import me.moonscenty.alchemia.research.HexGrid;
import me.moonscenty.alchemia.research.NoteSolving;
import me.moonscenty.alchemia.research.ResearchNote;
import net.minecraft.core.Holder;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Working at a research table: the tools and the note laid out on it, with the reader's own pockets below.
 */
public class ResearchTableMenu extends AbstractContainerMenu {
    public static final int SLOT_TOOLS = ResearchTableBlockEntity.SLOT_TOOLS;
    public static final int SLOT_NOTES = ResearchTableBlockEntity.SLOT_NOTES;
    private static final int TABLE_SLOTS = 2;

    // Where the slots sit on the drawn panel.
    private static final int TOOLS_X = 19;
    private static final int NOTES_X = 72;
    private static final int TABLE_Y = 10;
    private static final int PACK_X = 48;
    private static final int PACK_Y = 175;
    private static final int BELT_Y = 233;
    private static final int PITCH = 18;

    private final Container table;
    private final ContainerLevelAccess access;

    /** What the client builds: the table is a stand-in, filled in by the slots syncing across. */
    public ResearchTableMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(TABLE_SLOTS), ContainerLevelAccess.NULL);
    }

    public ResearchTableMenu(int id, Inventory inventory, Container table, ContainerLevelAccess access) {
        super(ModMenus.RESEARCH_TABLE.get(), id);
        this.table = table;
        this.access = access;
        checkContainerSize(table, TABLE_SLOTS);

        addSlot(new Slot(table, SLOT_TOOLS, TOOLS_X, TABLE_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return table.canPlaceItem(SLOT_TOOLS, stack);
            }
        });
        addSlot(new Slot(table, SLOT_NOTES, NOTES_X, TABLE_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return table.canPlaceItem(SLOT_NOTES, stack);
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

    public ItemStack noteStack() {
        return getSlot(SLOT_NOTES).getItem();
    }

    /** The note being worked through, or nothing if the desk is bare. */
    public ResearchNote note() {
        return ResearchNoteItem.noteOn(noteStack());
    }

    public ItemStack toolStack() {
        return getSlot(SLOT_TOOLS).getItem();
    }

    /** Whether there is ink left to write with. A dry quill leaves the board untouchable. */
    public boolean hasInk() {
        ItemStack tools = toolStack();
        return !tools.isEmpty() && tools.getDamageValue() < tools.getMaxDamage() - 1;
    }

    /**
     * Lays an aspect down, or lifts one back off. Runs on the server: the client only says where it clicked.
     */
    public void place(Player player, HexGrid.Hex at, Optional<Holder<Aspect>> aspect) {
        ResearchNote note = note();
        if (note == null || !hasInk()) {
            return;
        }
        if (!NoteSolving.check(note, at, aspect).allowed()) {
            return;
        }

        ResearchNote written = NoteSolving.place(note, at, aspect);
        if (NoteSolving.isSolved(written)) {
            written = NoteSolving.settle(written);
        }
        write(written);
    }

    /** Mixes two of what is on the sheet into the one thing they make between them. */
    public void mix(Player player, Holder<Aspect> one, Holder<Aspect> other) {
        ResearchNote note = note();
        if (note == null || !hasInk()) {
            return;
        }
        Optional<Holder<Aspect>> result = NoteSolving.mixOf(
                player.registryAccess().registryOrThrow(ModAspects.KEY), one, other);
        if (!NoteSolving.checkMix(note, one, other, result).allowed()) {
            return;
        }
        write(NoteSolving.mix(note, one, other, result.get()));
    }

    /** Every change to the note costs a point of ink, which is what keeps guessing from being free. */
    private void write(ResearchNote note) {
        noteStack().set(ModDataComponents.RESEARCH_NOTE.get(), note);
        if (table instanceof ResearchTableBlockEntity desk) {
            desk.useInk();
        }
        table.setChanged();
        broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack held = slot.getItem();
        ItemStack copy = held.copy();
        if (index < TABLE_SLOTS) {
            if (!moveItemStackTo(held, TABLE_SLOTS, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(held, 0, TABLE_SLOTS, false)) {
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
        return stillValid(access, player, ModBlocks.RESEARCH_TABLE.get());
    }
}
