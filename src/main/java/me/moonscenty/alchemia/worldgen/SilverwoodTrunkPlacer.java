package me.moonscenty.alchemia.worldgen;

import java.util.List;
import java.util.function.BiConsumer;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.moonscenty.alchemia.registry.ModFeatures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

/**
 * A plus-shaped trunk that flares into roots at the base and into knots below the crown, topped by a single canopy.
 */
public class SilverwoodTrunkPlacer extends TrunkPlacer {
    public static final MapCodec<SilverwoodTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(
            instance -> trunkPlacerParts(instance).apply(instance, SilverwoodTrunkPlacer::new));

    public SilverwoodTrunkPlacer(int baseHeight, int heightRandA, int heightRandB) {
        super(baseHeight, heightRandA, heightRandB);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return ModFeatures.SILVERWOOD_TRUNK.get();
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter,
            RandomSource random, int freeTreeHeight, BlockPos pos, TreeConfiguration config) {
        setDirtAt(level, blockSetter, random, pos.below(), config);

        for (int y = 0; y < freeTreeHeight; y++) {
            placeLog(level, blockSetter, random, pos.above(y), config);
            for (Direction side : Direction.Plane.HORIZONTAL) {
                placeLog(level, blockSetter, random, pos.above(y).relative(side), config);
            }
        }
        placeLog(level, blockSetter, random, pos.above(freeTreeHeight), config);

        for (Direction side : Direction.Plane.HORIZONTAL) {
            // roots reaching out along the ground and into it
            BlockPos root = pos.relative(side, 2);
            placeLog(level, blockSetter, random, root, config, state -> state.trySetValue(RotatedPillarBlock.AXIS, side.getAxis()));
            placeLog(level, blockSetter, random, root.below(), config);

            // diagonal buttresses at the base and knots under the canopy
            BlockPos corner = pos.relative(side).relative(side.getClockWise());
            placeLog(level, blockSetter, random, corner, config);
            if (random.nextInt(3) != 0) {
                placeLog(level, blockSetter, random, corner.above(), config);
            }
            placeLog(level, blockSetter, random, corner.above(freeTreeHeight - 4), config);
            if (random.nextInt(3) == 0) {
                placeLog(level, blockSetter, random, corner.above(freeTreeHeight - 5), config);
            }
        }

        return List.of(new FoliagePlacer.FoliageAttachment(pos.above(freeTreeHeight - 3), 0, false));
    }
}
