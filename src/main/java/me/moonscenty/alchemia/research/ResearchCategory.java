package me.moonscenty.alchemia.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public record ResearchCategory(ResourceLocation icon, ResourceLocation background, int sortOrder,
        java.util.Optional<ResourceLocation> unlockedBy) {
    public static final Codec<ResearchCategory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("icon").forGetter(ResearchCategory::icon),
            ResourceLocation.CODEC.fieldOf("background").forGetter(ResearchCategory::background),
            Codec.INT.optionalFieldOf("sort_order", 0).forGetter(ResearchCategory::sortOrder),
            ResourceLocation.CODEC.optionalFieldOf("unlocked_by").forGetter(ResearchCategory::unlockedBy))
            .apply(instance, ResearchCategory::new));

    public boolean isOpenTo(me.moonscenty.alchemia.player.PlayerKnowledge knowledge) {
        return unlockedBy.map(knowledge::hasResearch).orElse(true);
    }

    public static Component displayName(ResourceKey<ResearchCategory> key) {
        return Component.translatable("research_category." + key.location().getNamespace() + "." + key.location().getPath());
    }
}
