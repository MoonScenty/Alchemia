package me.moonscenty.alchemia.research;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

// The few recipes the original locked behind two pieces of knowledge.
public final class RecipeGates {
    private static final Map<ResourceLocation, List<ResourceLocation>> ALSO = Map.of(
            Alchemia.id("seal_pickup_advanced"), List.of(Alchemia.id("biothaumic_mind")),
            Alchemia.id("seal_fill_advanced"), List.of(Alchemia.id("biothaumic_mind")),
            Alchemia.id("seal_empty_advanced"), List.of(Alchemia.id("biothaumic_mind")),
            Alchemia.id("seal_guard_advanced"), List.of(Alchemia.id("biothaumic_mind")),
            Alchemia.id("ancient_pedestal"), List.of(Clues.ANCIENT_STONE),
            Alchemia.id("eldritch_pedestal"), List.of(Clues.ELDRITCH_STONE));

    private RecipeGates() {
    }

    public static List<ResourceLocation> also(ResourceLocation recipe) {
        return ALSO.getOrDefault(recipe, List.of());
    }

    public static boolean known(PlayerKnowledge knowledge, ResourceLocation recipe, Optional<ResourceLocation> research) {
        return research.map(knowledge::hasResearch).orElse(true) && also(recipe).stream().allMatch(knowledge::hasResearch);
    }

    public static boolean known(Player player, ResourceLocation recipe, Optional<ResourceLocation> research) {
        if (player == null) {
            return research.isEmpty() && also(recipe).isEmpty();
        }
        return known(PlayerKnowledge.of(player), recipe, research);
    }
}
