package me.moonscenty.alchemia.worldgen;

import java.time.LocalDate;
import java.time.Month;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.moonscenty.alchemia.registry.ModFeatures;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

// Biome modifiers apply as the server starts, which is when the original read the calendar.
public record HalloweenSpawnsModifier(HolderSet<Biome> biomes, MobSpawnSettings.SpawnerData spawner) implements BiomeModifier {
    public static final MapCodec<HalloweenSpawnsModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(HalloweenSpawnsModifier::biomes),
            MobSpawnSettings.SpawnerData.CODEC.fieldOf("spawner").forGetter(HalloweenSpawnsModifier::spawner))
            .apply(instance, HalloweenSpawnsModifier::new));

    public static boolean isHalloween(LocalDate day) {
        return day.getMonth() == Month.OCTOBER && day.getDayOfMonth() == 31;
    }

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase == Phase.ADD && biomes.contains(biome) && isHalloween(LocalDate.now())) {
            builder.getMobSpawnSettings().addSpawn(spawner.type.getCategory(), spawner);
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return ModFeatures.HALLOWEEN_SPAWNS.get();
    }
}
