package me.moonscenty.alchemia.research;

import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.player.WarpData;
import me.moonscenty.alchemia.player.WarpHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

// The single path for learning research (notes, rites, warp); the testing command bypasses it.
public final class ResearchCompletion {
    // original's value
    public static final int EXPERIENCE = 5;

    private ResearchCompletion() {
    }

    public static boolean complete(Player player, ResourceLocation id) {
        PlayerKnowledge knowledge = PlayerKnowledge.of(player);
        if (knowledge.hasResearch(id)) {
            return false;
        }
        PlayerKnowledge.update(player, known -> known.withResearch(id));
        player.displayClientMessage(Component.translatable("note.alchemia.learned", ResearchEntry.displayName(id)),
                false);
        player.giveExperiencePoints(EXPERIENCE);
        player.level().registryAccess().registryOrThrow(ModResearch.ENTRY_KEY).getOptional(id).ifPresent(entry -> {
            settle(player, entry.warp());
            for (ResourceLocation sibling : entry.siblings()) {
                if (ready(player, sibling)) {
                    complete(player, sibling);
                }
            }
        });
        return true;
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
        return player.level().registryAccess().registryOrThrow(ModResearch.ENTRY_KEY).getOptional(id)
                .map(entry -> entry.parentsKnown(knowledge))
                .orElse(false);
    }
}
