package me.moonscenty.alchemia.player;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.registry.ModAspects;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/**
 * What a player has worked out so far.
 * <p>
 * Aspects have to be discovered before they can be read off an item; until then they show as a question mark. Everyone
 * starts knowing the six primals, since those are plain enough to see in anything.
 */
public record PlayerKnowledge(Set<Aspect> discoveredAspects, Set<ResourceLocation> completedResearch) {
    public static final Codec<PlayerKnowledge> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ModAspects.REGISTRY.byNameCodec().listOf().fieldOf("discovered_aspects")
                    .forGetter(knowledge -> List.copyOf(knowledge.discoveredAspects)),
            ResourceLocation.CODEC.listOf().fieldOf("completed_research")
                    .forGetter(knowledge -> List.copyOf(knowledge.completedResearch)))
            .apply(instance, (aspects, research) -> new PlayerKnowledge(new LinkedHashSet<>(aspects), new LinkedHashSet<>(research))));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerKnowledge> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(ModAspects.KEY).apply(ByteBufCodecs.list()),
            knowledge -> knowledge.discoveredAspects.stream().map(ModAspects.REGISTRY::wrapAsHolder).toList(),
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()),
            knowledge -> List.copyOf(knowledge.completedResearch),
            (aspects, research) -> new PlayerKnowledge(
                    aspects.stream().map(Holder::value).collect(Collectors.toCollection(LinkedHashSet::new)),
                    new LinkedHashSet<>(research)));

    /** A player who has only ever seen the obvious. */
    public static PlayerKnowledge fresh() {
        Set<Aspect> primals = new LinkedHashSet<>();
        ModAspects.primals().forEach(primal -> primals.add(primal.value()));
        return new PlayerKnowledge(primals, Set.of());
    }

    public boolean knows(Holder<Aspect> aspect) {
        return discoveredAspects.contains(aspect.value());
    }

    public boolean hasResearch(ResourceLocation research) {
        return completedResearch.contains(research);
    }

    /** The same knowledge with one more aspect in it, or this one if it was already known. */
    public PlayerKnowledge withAspect(Holder<Aspect> aspect) {
        if (knows(aspect)) {
            return this;
        }
        Set<Aspect> grown = new LinkedHashSet<>(discoveredAspects);
        grown.add(aspect.value());
        return new PlayerKnowledge(grown, completedResearch);
    }

    /** Discovers everything an item is made of at once, which is what a scan amounts to. */
    public PlayerKnowledge withAspects(AspectList aspects) {
        PlayerKnowledge result = this;
        for (Holder<Aspect> aspect : aspects.sortedByName()) {
            result = result.withAspect(aspect);
        }
        return result;
    }

    public PlayerKnowledge withResearch(ResourceLocation research) {
        if (hasResearch(research)) {
            return this;
        }
        Set<ResourceLocation> grown = new LinkedHashSet<>(completedResearch);
        grown.add(research);
        return new PlayerKnowledge(discoveredAspects, grown);
    }

    /** The same knowledge with the research cleared, keeping the aspects that have been seen. */
    public PlayerKnowledge withoutResearch() {
        return new PlayerKnowledge(discoveredAspects, Set.of());
    }

    /** The same knowledge back to only the obvious aspects, keeping the research that has been done. */
    public PlayerKnowledge withoutAspects() {
        return new PlayerKnowledge(fresh().discoveredAspects(), completedResearch);
    }

    /** Reads a player's knowledge, falling back to a fresh one on the client before the first sync arrives. */
    public static PlayerKnowledge of(Player player) {
        return player.getData(ModAttachments.KNOWLEDGE);
    }
}
