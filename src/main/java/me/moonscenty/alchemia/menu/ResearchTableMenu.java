package me.moonscenty.alchemia.menu;

import java.util.Optional;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.block.entity.ResearchTableBlockEntity;
import me.moonscenty.alchemia.item.ResearchNoteItem;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModMenus;
import me.moonscenty.alchemia.registry.ModSounds;
import me.moonscenty.alchemia.research.HexGrid;
import me.moonscenty.alchemia.research.NoteSolving;
import me.moonscenty.alchemia.research.ResearchNote;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

    // Where the slots sit on the drawn panel: the original's places, so its panel and ours both fit
    private static final int TOOLS_X = 14;
    private static final int NOTES_X = 70;
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

    /** The original's research expertise and mastery, which make the table kinder to whoever has them. */
    public static final ResourceLocation RESEARCHER_1 = Alchemia.id("researcher1");
    public static final ResourceLocation RESEARCHER_2 = Alchemia.id("researcher2");

    public static boolean knows(Player player, ResourceLocation research) {
        return PlayerKnowledge.of(player).hasResearch(research);
    }

    /**
     * Lays an aspect down, or lifts one back off. Runs on the server: the client only says where it clicked.
     * <p>
     * The original's researcher perks are here: with mastery a point laid down is free one time in ten, and a point
     * rubbed out comes back one time in four with expertise or one in two with mastery.
     */
    public void place(Player player, HexGrid.Hex at, Optional<Holder<Aspect>> aspect) {
        ResearchNote note = note();
        if (note == null || !hasInk()) {
            return;
        }
        if (!NoteSolving.check(note, at, aspect).allowed()) {
            return;
        }

        Optional<Holder<Aspect>> was = note.cellAt(at).flatMap(ResearchNote.Cell::aspect);
        ResearchNote written = NoteSolving.place(note, at, aspect);
        float luck = player.getRandom().nextFloat();
        if (aspect.isPresent() && knows(player, RESEARCHER_2) && luck < 0.1F) {
            written = giveBack(player, written, aspect.get());
        } else if (aspect.isEmpty() && was.isPresent()
                && (knows(player, RESEARCHER_2) && luck < 0.5F || knows(player, RESEARCHER_1) && luck < 0.25F)) {
            written = giveBack(player, written, was.get());
        }
        if (NoteSolving.isSolved(written)) {
            written = NoteSolving.settle(written);
            // the table's own sound of a thing worked out, heard by everyone standing round it
            access.execute((level, pos) -> level.playSound(null, pos, ModSounds.LEARN.get(), SoundSource.BLOCKS,
                    1.0F, 1.0F));
        }
        write(written);
    }

    /** A point put back on the sheet, with the little chime the original gave it. */
    private ResearchNote giveBack(Player player, ResearchNote note, Holder<Aspect> aspect) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.2F,
                0.9F + player.getRandom().nextFloat() * 0.2F);
        return new ResearchNote(note.research(), note.budget().add(aspect, 1), note.cells(), note.complete());
    }

    /**
     * With research expertise, a knowledge fragment carried can be spent on the note for a point of every primal,
     * the original's fifth button.
     */
    public void spendFragment(Player player) {
        ResearchNote note = note();
        if (note == null || note.complete() || !hasInk() || !knows(player, RESEARCHER_1)) {
            return;
        }
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(ModItems.KNOWLEDGE_FRAGMENT.get())) {
                AspectList budget = note.budget();
                for (Holder<Aspect> primal : ModAspects.primals()) {
                    budget = budget.add(primal, 1);
                }
                write(new ResearchNote(note.research(), budget, note.cells(), note.complete()));
                stack.shrink(1);
                return;
            }
        }
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
