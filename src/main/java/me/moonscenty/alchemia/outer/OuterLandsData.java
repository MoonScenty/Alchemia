package me.moonscenty.alchemia.outer;

import java.util.OptionalLong;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.worldgen.ModBiomes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

/**
 * The Outer Lands as datapack content: a dimension a hundred and twenty-eight blocks deep, with no sky light, no
 * beds and no anchors, the time stood still, and one land in it.
 */
public final class OuterLandsData {
    private OuterLandsData() {
    }

    public static void dimensionType(BootstrapContext<DimensionType> context) {
        context.register(OuterLands.TYPE, new DimensionType(OptionalLong.of(6000L), false, false, false, false, 1.0,
                false, false, 0, 128, 128, BlockTags.INFINIBURN_OVERWORLD, Alchemia.id("outer_lands"), 0.0F,
                new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 7), 0)));
    }

    public static void levelStem(BootstrapContext<LevelStem> context) {
        var types = context.lookup(Registries.DIMENSION_TYPE);
        var biomes = context.lookup(Registries.BIOME);
        context.register(OuterLands.STEM, new LevelStem(types.getOrThrow(OuterLands.TYPE),
                new OuterLandsChunkGenerator(new FixedBiomeSource(biomes.getOrThrow(ModBiomes.ELDRITCH)))));
    }
}
