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
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ResearchTableMenu extends AlchemiaMenu {
    public static final int SLOT_TOOLS = ResearchTableBlockEntity.SLOT_TOOLS;
    public static final int SLOT_NOTES = ResearchTableBlockEntity.SLOT_NOTES;
    private static final int TABLE_SLOTS = 2;

    private static final int TOOLS_X = 14;
    private static final int NOTES_X = 70;
    private static final int TABLE_Y = 10;
    private static final int PACK_X = 48;
    private static final int PACK_Y = 175;

    private final Container table;
    private final ContainerLevelAccess access;

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

        addPlayerInventory(inventory, PACK_X, PACK_Y);
    }

    public ItemStack noteStack() {
        return getSlot(SLOT_NOTES).getItem();
    }

    public ResearchNote note() {
        return ResearchNoteItem.noteOn(noteStack());
    }

    public ItemStack toolStack() {
        return getSlot(SLOT_TOOLS).getItem();
    }

    public boolean hasInk() {
        ItemStack tools = toolStack();
        return !tools.isEmpty() && tools.getDamageValue() < tools.getMaxDamage() - 1;
    }

    public static final ResourceLocation RESEARCHER_1 = Alchemia.id("researcher1");
    public static final ResourceLocation RESEARCHER_2 = Alchemia.id("researcher2");

    public static boolean knows(Player player, ResourceLocation research) {
        return PlayerKnowledge.of(player).hasResearch(research);
    }

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
            access.execute((level, pos) -> level.playSound(null, pos, ModSounds.LEARN.get(), SoundSource.BLOCKS,
                    1.0F, 1.0F));
        }
        write(written);
    }

    private ResearchNote giveBack(Player player, ResearchNote note, Holder<Aspect> aspect) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.2F,
                0.9F + player.getRandom().nextFloat() * 0.2F);
        return new ResearchNote(note.research(), note.budget().add(aspect, 1), note.cells(), note.complete());
    }

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
        return quickMoveBetween(index, TABLE_SLOTS);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.RESEARCH_TABLE.get());
    }
}
