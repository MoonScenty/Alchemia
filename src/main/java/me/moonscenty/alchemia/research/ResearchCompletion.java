package me.moonscenty.alchemia.research;

import java.util.Map;

import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.player.WarpData;
import me.moonscenty.alchemia.player.WarpHandler;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

// The single path for learning research (notes, rites, warp); the testing command bypasses it.
public final class ResearchCompletion {
    // original's value
    public static final int EXPERIENCE = 5;

    private ResearchCompletion() {
    }

    public static boolean complete(Player player, ResourceLocation id) {
        return complete(player, id, true);
    }

    // bought research comes without the experience a solved note gives
    public static boolean complete(Player player, ResourceLocation id, boolean experience) {
        if (PlayerKnowledge.of(player).hasResearch(id)) {
            return false;
        }
        announce(player, id);
        if (experience) {
            player.giveExperiencePoints(EXPERIENCE);
        }
        entries(player).getOptional(id).ifPresent(entry -> {
            for (ResourceLocation sibling : entry.siblings()) {
                if (!PlayerKnowledge.of(player).hasResearch(sibling) && ready(player, sibling)) {
                    announce(player, sibling);
                }
            }
        });
        return true;
    }

    // original: reading the book hands over every sibling still owed, parents or not
    public static void collectSiblings(Player player) {
        for (Map.Entry<ResourceKey<ResearchEntry>, ResearchEntry> entry : entries(player).entrySet()) {
            if (PlayerKnowledge.of(player).hasResearch(entry.getKey().location())) {
                for (ResourceLocation sibling : entry.getValue().siblings()) {
                    learn(player, sibling);
                }
            }
        }
    }

    private static void announce(Player player, ResourceLocation id) {
        learn(player, id);
        player.displayClientMessage(Component.translatable("note.alchemia.learned", ResearchEntry.displayName(id)),
                false);
    }

    private static void learn(Player player, ResourceLocation id) {
        if (!PlayerKnowledge.of(player).hasResearch(id)) {
            PlayerKnowledge.update(player, known -> known.withResearch(id));
            entries(player).getOptional(id).ifPresent(entry -> settle(player, entry.warp()));
        }
    }

    // one point is permanent; more is split, the larger half permanent and the rest sticky
    public static void settle(Player player, int warp) {
        if (warp <= 0) {
            return;
        }
        int sticking = warp > 1 ? warp / 2 : 0;
        WarpHandler.add(player, WarpData.Kind.PERMANENT, warp - sticking);
        WarpHandler.add(player, WarpData.Kind.STICKY, sticking);
    }

    private static boolean ready(Player player, ResourceLocation id) {
        PlayerKnowledge knowledge = PlayerKnowledge.of(player);
        return entries(player).getOptional(id)
                .map(entry -> entry.parentsKnown(knowledge))
                .orElse(false);
    }

    private static Registry<ResearchEntry> entries(Player player) {
        return player.level().registryAccess().registryOrThrow(ModResearch.ENTRY_KEY);
    }
}
