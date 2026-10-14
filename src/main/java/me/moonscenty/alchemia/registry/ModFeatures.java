package me.moonscenty.alchemia.registry;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.worldgen.CrystalPatchFeature;
import me.moonscenty.alchemia.worldgen.NodeFeature;
import me.moonscenty.alchemia.worldgen.GreatwoodTrunkPlacer;
import me.moonscenty.alchemia.worldgen.SilverwoodTrunkPlacer;
import me.moonscenty.alchemia.worldgen.SphereFoliagePlacer;
import me.moonscenty.alchemia.worldgen.NodeInTreeDecorator;
import me.moonscenty.alchemia.worldgen.UndergrowthDecorator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
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
    public static final DeferredHolder<TreeDecoratorType<?>, TreeDecoratorType<NodeInTreeDecorator>> NODE_IN_TREE = TREE_DECORATORS.register("node_in_tree",
            () -> new TreeDecoratorType<>(NodeInTreeDecorator.CODEC));

    public static final DeferredHolder<Feature<?>, CrystalPatchFeature> CRYSTAL_PATCH = FEATURES.register("crystal_patch",
            () -> new CrystalPatchFeature(BlockStateConfiguration.CODEC));

    /** An eldritch ring, and a lesser crimson portal over bare ground. */
    public static final DeferredHolder<Feature<?>, me.moonscenty.alchemia.worldgen.EldritchRingFeature> ELDRITCH_RING =
            FEATURES.register("eldritch_ring",
                    () -> new me.moonscenty.alchemia.worldgen.EldritchRingFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, me.moonscenty.alchemia.worldgen.CultistPortalFeature> CULTIST_PORTAL =
            FEATURES.register("cultist_portal",
                    () -> new me.moonscenty.alchemia.worldgen.CultistPortalFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, NodeFeature> AURA_NODE = FEATURES.register("aura_node",
            () -> new NodeFeature(NoneFeatureConfiguration.CODEC));

    /** The magical forest's floor: its glimmering grass, and its vishrooms by the trees. */
    public static final DeferredHolder<Feature<?>, me.moonscenty.alchemia.worldgen.MagicalForestFloorFeature>
            AMBIENT_GRASS = FEATURES.register("ambient_grass",
                    () -> new me.moonscenty.alchemia.worldgen.MagicalForestFloorFeature(NoneFeatureConfiguration.CODEC,
                            true));
    public static final DeferredHolder<Feature<?>, me.moonscenty.alchemia.worldgen.MagicalForestFloorFeature>
            VISHROOMS_BY_TREES = FEATURES.register("vishrooms_by_trees",
                    () -> new me.moonscenty.alchemia.worldgen.MagicalForestFloorFeature(NoneFeatureConfiguration.CODEC,
                            false));
}
