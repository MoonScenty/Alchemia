package me.moonscenty.alchemia.research;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.moonscenty.alchemia.aspect.AspectList;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record ResearchEntry(
        ResourceKey<ResearchCategory> category,
        AspectList requirements,
        int column,
        int row,
        Holder<Item> icon,
        List<ResourceLocation> parents,
        List<ResearchPage> pages,
        boolean autoUnlock,
        NodeShape shape,
        int complexity,
        boolean hidden,
        int warp,
        List<ResourceLocation> siblings,
        List<ResourceLocation> clues,
        List<ResourceLocation> hiddenParents) {

    public static final Codec<ResearchEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(ModResearch.CATEGORY_KEY).fieldOf("category").forGetter(ResearchEntry::category),
            AspectList.CODEC.optionalFieldOf("requirements", AspectList.EMPTY).forGetter(ResearchEntry::requirements),
            Codec.INT.fieldOf("column").forGetter(ResearchEntry::column),
            Codec.INT.fieldOf("row").forGetter(ResearchEntry::row),
            BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("icon").forGetter(ResearchEntry::icon),
            ResourceLocation.CODEC.listOf().optionalFieldOf("parents", List.of()).forGetter(ResearchEntry::parents),
            ResearchPage.CODEC.listOf().optionalFieldOf("pages", List.of()).forGetter(ResearchEntry::pages),
            Codec.BOOL.optionalFieldOf("auto_unlock", false).forGetter(ResearchEntry::autoUnlock),
            NodeShape.CODEC.optionalFieldOf("shape", NodeShape.PLAIN).forGetter(ResearchEntry::shape),
            Codec.intRange(1, 3).optionalFieldOf("complexity", 1).forGetter(ResearchEntry::complexity),
            Codec.BOOL.optionalFieldOf("hidden", false).forGetter(ResearchEntry::hidden),
            Codec.intRange(0, 100).optionalFieldOf("warp", 0).forGetter(ResearchEntry::warp),
            ResourceLocation.CODEC.listOf().optionalFieldOf("siblings", List.of()).forGetter(ResearchEntry::siblings),
            ResourceLocation.CODEC.listOf().optionalFieldOf("clues", List.of()).forGetter(ResearchEntry::clues),
            ResourceLocation.CODEC.listOf().optionalFieldOf("hidden_parents", List.of())
                    .forGetter(ResearchEntry::hiddenParents))
            .apply(instance, ResearchEntry::new));

    public List<ResearchPage> pagesFor(java.util.function.Predicate<ResourceLocation> hasResearch) {
        return pages.stream().filter(page -> page.isVisibleTo(hasResearch)).toList();
    }

    public ItemStack iconStack() {
        return new ItemStack(icon);
    }

    public static Component displayName(ResourceLocation id) {
        return Component.translatable("research." + id.getNamespace() + "." + id.getPath());
    }

    public static Component description(ResourceLocation id) {
        return Component.translatable("research." + id.getNamespace() + "." + id.getPath() + ".description");
    }

    // hidden entries show once everything they wait on is met; with nothing to wait on, only once given
    public boolean isShownTo(me.moonscenty.alchemia.player.PlayerKnowledge knowledge, ResourceLocation id) {
        return knowledge.hasResearch(id) || !hidden || !onlyGiven() && isAvailableTo(knowledge);
    }

    public boolean onlyGiven() {
        return hidden && parents.isEmpty() && hiddenParents.isEmpty() && clues.isEmpty();
    }

    public boolean isAvailableTo(me.moonscenty.alchemia.player.PlayerKnowledge knowledge) {
        return parentsKnown(knowledge) && cluesMet(knowledge);
    }

    public boolean parentsKnown(me.moonscenty.alchemia.player.PlayerKnowledge knowledge) {
        return parents.stream().allMatch(knowledge::hasResearch)
                && hiddenParents.stream().allMatch(knowledge::hasResearch);
    }

    public boolean cluesMet(me.moonscenty.alchemia.player.PlayerKnowledge knowledge) {
        return clues.stream().allMatch(clue -> Clues.met(knowledge, clue));
    }

    public Optional<AspectList> missingFor(java.util.function.Predicate<Holder<me.moonscenty.alchemia.aspect.Aspect>> knows) {
        AspectList missing = AspectList.EMPTY;
        for (Holder<me.moonscenty.alchemia.aspect.Aspect> aspect : requirements.sortedByName()) {
            if (!knows.test(aspect)) {
                missing = missing.add(aspect, requirements.get(aspect));
            }
        }
        return missing.isEmpty() ? Optional.empty() : Optional.of(missing);
    }
}
