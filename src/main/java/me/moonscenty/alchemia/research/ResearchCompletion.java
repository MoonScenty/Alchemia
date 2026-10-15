package me.moonscenty.alchemia.research;

import me.moonscenty.alchemia.player.ModAttachments;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.player.WarpData;
import me.moonscenty.alchemia.player.WarpHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/**
 * Learning a piece of research, the one way it is done, as the original's {@code completeResearch} did it: the
 * research is known, the warp it carries settles on the learner, half of it (rounded up) for good and the rest
 * sticking, whatever is learnt along with it is learnt too, and the learner is a little wiser for it in experience.
 * <p>
 * Notes, the Crimson Rites and what warp itself teaches all come through here. The command that hands out research
 * for testing does not: it is not learning anything.
 */
public final class ResearchCompletion {
    /** The experience the original gave for each thing learnt. */
    public static final int EXPERIENCE = 5;

    private ResearchCompletion() {
    }

    /**
     * Teaches the research if it is not already known.
     *
     * @return whether it was new
     */
    public static boolean complete(Player player, ResourceLocation id) {
        PlayerKnowledge knowledge = PlayerKnowledge.of(player);
        if (knowledge.hasResearch(id)) {
            return false;
        }
        player.setData(ModAttachments.KNOWLEDGE, knowledge.withResearch(id));
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

    /** Warp from learning: one point is for good; more is split, the larger half for good and the rest sticking. */
    public static void settle(Player player, int warp) {
        if (warp <= 0) {
            return;
        }
        int sticking = warp > 1 ? warp / 2 : 0;
        WarpHandler.add(player, WarpData.Kind.PERMANENT, warp - sticking);
        WarpHandler.add(player, WarpData.Kind.STICKY, sticking);
    }

    /** Whether everything a piece of research needs first is known. */
    private static boolean ready(Player player, ResourceLocation id) {
        PlayerKnowledge knowledge = PlayerKnowledge.of(player);
        return player.level().registryAccess().registryOrThrow(ModResearch.ENTRY_KEY).getOptional(id)
                .map(entry -> entry.parents().stream().allMatch(knowledge::hasResearch))
                .orElse(false);
    }
}
