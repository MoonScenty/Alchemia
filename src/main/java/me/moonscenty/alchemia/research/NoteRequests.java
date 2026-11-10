package me.moonscenty.alchemia.research;

import me.moonscenty.alchemia.item.ResearchNoteItem;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

// Everything is checked before paper and ink are spent.
public final class NoteRequests {
    private NoteRequests() {
    }

    public enum Result {
        WRITTEN("written"),
        ALREADY_CARRIED("already_carried"),
        NOT_READY("not_ready"),
        NO_PAPER("no_paper"),
        NO_INK("no_ink");

        private final String key;

        Result(String key) {
            this.key = key;
        }

        public Component message() {
            return Component.translatable("note.alchemia." + key);
        }

        public boolean worked() {
            return this == WRITTEN;
        }
    }

    public static boolean isReady(PlayerKnowledge knowledge, ResourceLocation id, ResearchEntry entry) {
        return !knowledge.hasResearch(id)
                && !entry.onlyGiven()
                // original: stubs take no note
                && !entry.requirements().isEmpty()
                && !entry.secondary()
                && entry.isAvailableTo(knowledge)
                && entry.missingFor(knowledge::knows).isEmpty();
    }

    public static Result give(ServerPlayer player, ResourceLocation id) {
        Registry<ResearchEntry> entries = player.registryAccess().registryOrThrow(ModResearch.ENTRY_KEY);
        ResearchEntry entry = entries.get(id);
        if (entry == null || !isReady(PlayerKnowledge.of(player), id, entry)) {
            return tell(player, Result.NOT_READY);
        }
        if (carriesNoteFor(player, id)) {
            return tell(player, Result.ALREADY_CARRIED);
        }

        int paper = findPaper(player);
        if (paper < 0) {
            return tell(player, Result.NO_PAPER);
        }
        int tools = findScribingTools(player);
        if (tools < 0) {
            return tell(player, Result.NO_INK);
        }

        player.getInventory().removeItem(paper, 1);
        ItemStack scribing = player.getInventory().getItem(tools);
        scribing.setDamageValue(scribing.getDamageValue() + 1);

        ItemStack note = new ItemStack(ModItems.RESEARCH_NOTES.get());
        note.set(ModDataComponents.RESEARCH_NOTE.get(), NoteGeneration.draw(id, entry, player.getRandom()));
        if (!player.getInventory().add(note)) {
            player.drop(note, false);
        }
        player.level().playSound(null, player.blockPosition(), ModSounds.LEARN.get(), SoundSource.PLAYERS, 0.75F, 1.0F);
        return tell(player, Result.WRITTEN);
    }

    private static Result tell(ServerPlayer player, Result result) {
        player.displayClientMessage(result.message(), true);
        return result;
    }

    private static boolean carriesNoteFor(ServerPlayer player, ResourceLocation id) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            ResearchNote note = stack.is(ModItems.RESEARCH_NOTES.get()) ? ResearchNoteItem.noteOn(stack) : null;
            if (note != null && note.research().equals(id)) {
                return true;
            }
        }
        return false;
    }

    private static int findPaper(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot).is(Items.PAPER)) {
                return slot;
            }
        }
        return -1;
    }

    private static int findScribingTools(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(ModItems.SCRIBING_TOOLS.get()) && stack.getDamageValue() < stack.getMaxDamage()) {
                return slot;
            }
        }
        return -1;
    }
}
