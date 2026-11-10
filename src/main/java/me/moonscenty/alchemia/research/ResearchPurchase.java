package me.moonscenty.alchemia.research;

import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

// Secondary research is learned straight from the book for experience levels; no note is taken.
public final class ResearchPurchase {
    private ResearchPurchase() {
    }

    public static boolean canBuy(PlayerKnowledge knowledge, ResourceLocation id, ResearchEntry entry) {
        return entry.secondary() && !knowledge.hasResearch(id) && entry.isAvailableTo(knowledge);
    }

    public static boolean affords(Player player, ResearchEntry entry) {
        return player.getAbilities().instabuild || player.experienceLevel >= entry.experience();
    }

    public static boolean buy(ServerPlayer player, ResourceLocation id) {
        ResearchEntry entry = player.registryAccess().registryOrThrow(ModResearch.ENTRY_KEY).get(id);
        if (entry == null || !canBuy(PlayerKnowledge.of(player), id, entry)) {
            player.displayClientMessage(Component.translatable("research.alchemia.cant_understand"), false);
            return false;
        }
        if (!affords(player, entry)) {
            return false;
        }
        if (!player.getAbilities().instabuild) {
            player.giveExperienceLevels(-entry.experience());
        }
        ResearchCompletion.complete(player, id, false);
        player.level().playSound(null, player.blockPosition(), ModSounds.LEARN.get(), SoundSource.PLAYERS, 0.75F, 1.0F);
        return true;
    }
}
