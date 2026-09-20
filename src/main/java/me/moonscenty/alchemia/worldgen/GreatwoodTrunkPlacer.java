package me.moonscenty.alchemia.worldgen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.moonscenty.alchemia.registry.ModFeatures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

/**
 * A tall 2x2 trunk with long rising branches along its upper part. Every branch tip and the crown carry foliage.
 */
public class GreatwoodTrunkPlacer extends TrunkPlacer {
    public static final MapCodec<GreatwoodTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(
            instance -> trunkPlacerParts(instance).apply(instance, GreatwoodTrunkPlacer::new));

    public GreatwoodTrunkPlacer(int baseHeight, int heightRandA, int heightRandB) {
        super(baseHeight, heightRandA, heightRandB);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return ModFeatures.GREATWOOD_TRUNK.get();
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter,
            RandomSource random, int freeTreeHeight, BlockPos pos, TreeConfiguration config) {
        List<FoliagePlacer.FoliageAttachment> foliage = new ArrayList<>();

        for (int x = 0; x <= 1; x++) {
            for (int z = 0; z <= 1; z++) {
                setDirtAt(level, blockSetter, random, pos.offset(x, -1, z), config);
                for (int y = 0; y < freeTreeHeight; y++) {
                    placeLog(level, blockSetter, random, pos.offset(x, y, z), config);
                }
            }
        }

        int branches = 4 + freeTreeHeight / 4 + random.nextInt(3);
        int lowestBranch = Mth.ceil(freeTreeHeight * 0.4F);
        for (int i = 0; i < branches; i++) {
            int startY = lowestBranch + random.nextInt(Math.max(1, freeTreeHeight - 2 - lowestBranch));
            // spread the branches around the trunk, then jitter them
            double angle = (Math.PI * 2 * i / branches) + random.nextDouble() * 0.9;
            // lower branches reach further out
            float heightFraction = (float) (startY - lowestBranch) / Math.max(1, freeTreeHeight - lowestBranch);
            int length = 3 + Math.round((1 - heightFraction) * 3) + random.nextInt(2);
            foliage.add(placeBranch(level, blockSetter, random, pos.offset(0, startY, 0), angle, length, config));
        }

        foliage.add(new FoliagePlacer.FoliageAttachment(pos.above(freeTreeHeight - 1), 1, true));
        return foliage;
    }

    private FoliagePlacer.FoliageAttachment placeBranch(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter,
            RandomSource random, BlockPos start, double angle, int length, TreeConfiguration config) {
        double dx = Math.cos(angle);
        double dz = Math.sin(angle);
        Direction.Axis axis = Math.abs(dx) > Math.abs(dz) ? Direction.Axis.X : Direction.Axis.Z;
        double rise = 0.3 + random.nextDouble() * 0.3;

        BlockPos end = start;
        for (int step = 1; step <= length; step++) {
            // the trunk is two blocks wide, so measure from its center
            end = BlockPos.containing(start.getX() + 1 + dx * (step + 0.5), start.getY() + rise * step, start.getZ() + 1 + dz * (step + 0.5));
            placeLog(level, blockSetter, random, end, config, state -> state.trySetValue(RotatedPillarBlock.AXIS, axis));
        }
        return new FoliagePlacer.FoliageAttachment(end, 0, false);
    }
}
