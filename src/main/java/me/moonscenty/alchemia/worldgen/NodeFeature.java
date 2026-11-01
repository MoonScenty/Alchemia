package me.moonscenty.alchemia.worldgen;

import com.mojang.serialization.Codec;

import me.moonscenty.alchemia.aura.node.AuraNode;
import me.moonscenty.alchemia.aura.node.NodeType;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.aura.AuraGeneration;
import me.moonscenty.alchemia.aura.AuraHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

// Climbs from FLOOR until it finds air, so most nodes end up in caves.
public class NodeFeature extends Feature<NoneFeatureConfiguration> {
    private static final int FLOOR = 8;
    private static final int CLIMB = 64;
    // a tainted node arrives having already spoiled its chunk
    private static final int TAINT_ON_ARRIVAL = AuraGeneration.BASE;

    public NodeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        int surface = Math.max(level.getHeight() / 3, level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG,
                origin.getX(), origin.getZ()));
        BlockPos at = new BlockPos(origin.getX(), FLOOR + random.nextInt(Math.max(1, surface - FLOOR)), origin.getZ());

        for (int step = 0; step < CLIMB && !level.isEmptyBlock(at); step++) {
            at = at.above(2);
        }
        if (!level.isEmptyBlock(at)) {
            return false;
        }

        AuraNode node = new AuraNode(level.getLevel());
        node.moveTo(at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5, 0.0F, 0.0F);
        node.drawUp(random);
        level.addFreshEntity(node);

        if (node.type() == NodeType.TAINTED) {
            AuraHandler.add(level, at, ModAspects.FLUX, TAINT_ON_ARRIVAL);
        }
        return true;
    }
}
