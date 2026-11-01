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

// Re-applied on every login, so entries added to the pack later reach existing players.
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
