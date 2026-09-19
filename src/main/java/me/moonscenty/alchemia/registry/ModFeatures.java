package me.moonscenty.alchemia.registry;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.worldgen.CrystalPatchFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Alchemia.MODID);

    public static final DeferredHolder<Feature<?>, CrystalPatchFeature> CRYSTAL_PATCH = FEATURES.register("crystal_patch",
            () -> new CrystalPatchFeature(BlockStateConfiguration.CODEC));
}
