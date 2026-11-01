package me.moonscenty.alchemia.worldgen;

import java.util.function.Consumer;

import com.mojang.datafixers.util.Pair;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.Region;
import terrablender.api.RegionType;

// Vanilla layout with the magical forest wherever an ordinary forest would be; the region's weight is in the config.
public class MagicalForestRegion extends Region {
    public MagicalForestRegion(int weight) {
        super(Alchemia.id("overworld"), RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry,
            Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        addModifiedVanillaOverworldBiomes(mapper, builder -> builder.replaceBiome(Biomes.FOREST,
                ModBiomes.MAGICAL_FOREST));
    }
}
