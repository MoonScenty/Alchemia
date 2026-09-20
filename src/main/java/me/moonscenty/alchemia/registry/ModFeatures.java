package me.moonscenty.alchemia.registry;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.worldgen.CrystalPatchFeature;
import me.moonscenty.alchemia.worldgen.GreatwoodTrunkPlacer;
import me.moonscenty.alchemia.worldgen.SilverwoodTrunkPlacer;
import me.moonscenty.alchemia.worldgen.SphereFoliagePlacer;
import me.moonscenty.alchemia.worldgen.UndergrowthDecorator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Alchemia.MODID);

    public static final DeferredRegister<TrunkPlacerType<?>> TRUNK_PLACERS = DeferredRegister.create(Registries.TRUNK_PLACER_TYPE, Alchemia.MODID);
    public static final DeferredRegister<FoliagePlacerType<?>> FOLIAGE_PLACERS = DeferredRegister.create(Registries.FOLIAGE_PLACER_TYPE, Alchemia.MODID);

    public static final DeferredHolder<TrunkPlacerType<?>, TrunkPlacerType<GreatwoodTrunkPlacer>> GREATWOOD_TRUNK = TRUNK_PLACERS.register("greatwood",
            () -> new TrunkPlacerType<>(GreatwoodTrunkPlacer.CODEC));
    public static final DeferredHolder<TrunkPlacerType<?>, TrunkPlacerType<SilverwoodTrunkPlacer>> SILVERWOOD_TRUNK = TRUNK_PLACERS.register("silverwood",
            () -> new TrunkPlacerType<>(SilverwoodTrunkPlacer.CODEC));
    public static final DeferredHolder<FoliagePlacerType<?>, FoliagePlacerType<SphereFoliagePlacer>> SPHERE_FOLIAGE = FOLIAGE_PLACERS.register("sphere",
            () -> new FoliagePlacerType<>(SphereFoliagePlacer.CODEC));

    public static final DeferredRegister<TreeDecoratorType<?>> TREE_DECORATORS = DeferredRegister.create(Registries.TREE_DECORATOR_TYPE, Alchemia.MODID);
    public static final DeferredHolder<TreeDecoratorType<?>, TreeDecoratorType<UndergrowthDecorator>> UNDERGROWTH = TREE_DECORATORS.register("undergrowth",
            () -> new TreeDecoratorType<>(UndergrowthDecorator.CODEC));

    public static final DeferredHolder<Feature<?>, CrystalPatchFeature> CRYSTAL_PATCH = FEATURES.register("crystal_patch",
            () -> new CrystalPatchFeature(BlockStateConfiguration.CODEC));
}
