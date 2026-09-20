package me.moonscenty.alchemia.research;

import java.util.Map;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.player.ModAttachments;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Hands out the entries that are never researched, only had.
 * <p>
 * Each branch opens with something the reader is taken to already know, and the rest of the branch hangs off it. That
 * is handed over on every login rather than once, so an entry added to the pack later still reaches players who were
 * already playing.
 */
@EventBusSubscriber(modid = Alchemia.MODID)
public class StartingResearch {
    private StartingResearch() {
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            grant(player);
        }
    }

    public static void grant(ServerPlayer player) {
        Registry<ResearchEntry> entries = player.registryAccess().registryOrThrow(ModResearch.ENTRY_KEY);
        PlayerKnowledge before = PlayerKnowledge.of(player);
        PlayerKnowledge after = fold(before, entries);
        if (after != before) {
            player.setData(ModAttachments.KNOWLEDGE, after);
        }
    }

    /**
     * The knowledge with every entry that starts unlocked folded into it. Kept apart from the player so it can be
     * exercised on its own.
     */
    public static PlayerKnowledge fold(PlayerKnowledge knowledge, Registry<ResearchEntry> entries) {
        PlayerKnowledge result = knowledge;
        for (Map.Entry<ResourceKey<ResearchEntry>, ResearchEntry> entry : entries.entrySet()) {
            if (entry.getValue().autoUnlock()) {
                result = result.withResearch(entry.getKey().location());
            }
        }
        return result;
    }
}
