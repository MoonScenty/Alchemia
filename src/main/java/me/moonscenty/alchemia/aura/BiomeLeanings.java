package me.moonscenty.alchemia.aura;

import java.util.ArrayList;
import java.util.List;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.registry.ModAspects;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.Tags;

// The original's biome-type table, by the tags that stand in for its types.
public final class BiomeLeanings {
    public record Leaning(TagKey<Biome> where, float level, Holder<Aspect> aspect) {
    }

    public static final List<Leaning> ALL = List.of(
            new Leaning(Tags.Biomes.IS_AQUATIC, 1.0F, ModAspects.WATER),
            new Leaning(BiomeTags.IS_OCEAN, 1.0F, ModAspects.WATER),
            new Leaning(BiomeTags.IS_RIVER, 1.0F, ModAspects.WATER),
            new Leaning(Tags.Biomes.IS_WET, 1.0F, ModAspects.WATER),
            new Leaning(Tags.Biomes.IS_LUSH, 1.2F, ModAspects.WATER),

            new Leaning(Tags.Biomes.IS_HOT, 1.0F, ModAspects.FIRE),
            new Leaning(Tags.Biomes.IS_DESERT, 0.75F, ModAspects.FIRE),
            new Leaning(BiomeTags.IS_NETHER, 0.5F, ModAspects.FIRE),
            new Leaning(BiomeTags.IS_BADLANDS, 1.0F, ModAspects.FIRE),
            new Leaning(Tags.Biomes.IS_SPOOKY, 0.75F, ModAspects.FIRE),

            new Leaning(Tags.Biomes.IS_DENSE_VEGETATION, 1.0F, ModAspects.ORDER),
            new Leaning(Tags.Biomes.IS_SNOWY, 0.75F, ModAspects.ORDER),
            new Leaning(Tags.Biomes.IS_COLD, 0.75F, ModAspects.ORDER),
            new Leaning(Tags.Biomes.IS_ICY, 1.0F, ModAspects.ORDER),
            new Leaning(Tags.Biomes.IS_MUSHROOM, 1.3F, ModAspects.ORDER),
            new Leaning(Tags.Biomes.IS_MAGICAL, 1.0F, ModAspects.ORDER),

            new Leaning(Tags.Biomes.IS_CONIFEROUS_TREE, 1.0F, ModAspects.EARTH),
            new Leaning(BiomeTags.IS_FOREST, 1.2F, ModAspects.EARTH),
            new Leaning(Tags.Biomes.IS_SANDY, 0.75F, ModAspects.EARTH),
            new Leaning(BiomeTags.IS_BEACH, 0.75F, ModAspects.EARTH),
            new Leaning(BiomeTags.IS_JUNGLE, 1.2F, ModAspects.EARTH),

            new Leaning(BiomeTags.IS_SAVANNA, 0.75F, ModAspects.AIR),
            new Leaning(BiomeTags.IS_MOUNTAIN, 0.75F, ModAspects.AIR),
            new Leaning(BiomeTags.IS_HILL, 1.0F, ModAspects.AIR),
            new Leaning(Tags.Biomes.IS_PLAINS, 0.75F, ModAspects.AIR),
            new Leaning(BiomeTags.IS_END, 0.5F, ModAspects.AIR),

            new Leaning(Tags.Biomes.IS_DRY, 0.5F, ModAspects.ENTROPY),
            new Leaning(Tags.Biomes.IS_SPARSE_VEGETATION, 0.75F, ModAspects.ENTROPY),
            new Leaning(Tags.Biomes.IS_SWAMP, 1.2F, ModAspects.ENTROPY),
            new Leaning(Tags.Biomes.IS_WASTELAND, 0.5F, ModAspects.ENTROPY),
            new Leaning(Tags.Biomes.IS_DEAD, 0.25F, ModAspects.ENTROPY));

    private BiomeLeanings() {
    }

    // original's getRandomBiomeTag: one of the biome's types at random
    public static Holder<Aspect> anyOf(Holder<Biome> biome, RandomSource random) {
        List<Holder<Aspect>> found = new ArrayList<>();
        for (Leaning leaning : ALL) {
            if (biome.is(leaning.where())) {
                found.add(leaning.aspect());
            }
        }
        return found.isEmpty() ? null : found.get(random.nextInt(found.size()));
    }
}
