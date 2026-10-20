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

/**
 * One thing a player can work out, and where it sits in the book.
 *
 * @param category     the branch it belongs to
 * @param requirements the aspects that have to go into solving it
 * @param column       where it sits across the page
 * @param row          where it sits down the page
 * @param icon         the item shown on the node
 * @param parents      research that must be finished first
 * @param pages        the write-up, turned through two at a time once the entry is opened
 * @param autoUnlock   true for the handful of entries a player simply starts with
 * @param shape        how much weight the node carries, which decides the plate it is drawn on
 * @param complexity   how hard the note is to work out, from 1 to 3
 * @param hidden       not in the book until everything it waits on is there; with nothing to wait on, not until
 *                     it is known, and then never worked out on the table but only come by some other way
 * @param warp         what learning it leaves behind: half of it, rounded up, for good, and the rest sticking
 * @param siblings     research learnt along with it, once whatever those need is known
 * @param clues        what has to be found out in the world first: aspects to know, or clues to find
 *                     (see {@link Clues})
 */
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
        List<ResourceLocation> clues) {

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
            ResourceLocation.CODEC.listOf().optionalFieldOf("clues", List.of()).forGetter(ResearchEntry::clues))
            .apply(instance, ResearchEntry::new));

    /** The pages this player has earned the right to see, in order. */
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

    /**
     * Whether this shows in the book for someone, as the original decided it. A hidden entry stays out of it until
     * everything it waits on, research and clues alike, is there; one that waits on nothing is not there until it
     * is known, having been come by some other way.
     */
    public boolean isShownTo(me.moonscenty.alchemia.player.PlayerKnowledge knowledge, ResourceLocation id) {
        return knowledge.hasResearch(id) || !hidden || !onlyGiven() && isAvailableTo(knowledge);
    }

    /** A hidden entry with nothing to wait on: only ever given, never worked out on the table. */
    public boolean onlyGiven() {
        return hidden && parents.isEmpty() && clues.isEmpty();
    }

    /** Whether the player has everything this entry needs before it can be attempted: its research and its clues. */
    public boolean isAvailableTo(me.moonscenty.alchemia.player.PlayerKnowledge knowledge) {
        return parents.stream().allMatch(knowledge::hasResearch) && cluesMet(knowledge);
    }

    /** Whether every clue it waits on has been found. */
    public boolean cluesMet(me.moonscenty.alchemia.player.PlayerKnowledge knowledge) {
        return clues.stream().allMatch(clue -> Clues.met(knowledge, clue));
    }

    /** The aspects a player still has to discover before this can be worked on. */
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
