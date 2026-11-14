package me.moonscenty.alchemia.worldgen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.moonscenty.alchemia.registry.ModFeatures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

// original: the old big-tree generator, run once per width; the greatwood runs it twice, stacked
public class BigTreeTrunkPlacer extends TrunkPlacer {
    public record Shape(int trunkSize, double trunkShare, double branchSlope, double leafDensity, List<Double> widths,
            int leafDepth, boolean loose, int crown) {
        public static final Codec<Shape> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(1, 2).fieldOf("trunk_size").forGetter(Shape::trunkSize),
                Codec.DOUBLE.fieldOf("trunk_share").forGetter(Shape::trunkShare),
                Codec.DOUBLE.fieldOf("branch_slope").forGetter(Shape::branchSlope),
                Codec.DOUBLE.fieldOf("leaf_density").forGetter(Shape::leafDensity),
                Codec.DOUBLE.listOf().fieldOf("widths").forGetter(Shape::widths),
                Codec.intRange(1, 8).fieldOf("leaf_depth").forGetter(Shape::leafDepth),
                Codec.BOOL.fieldOf("loose").forGetter(Shape::loose),
                Codec.INT.fieldOf("crown").forGetter(Shape::crown))
                .apply(instance, Shape::new));
    }

    public static final MapCodec<BigTreeTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> trunkPlacerParts(instance)
            .and(Shape.CODEC.fieldOf("shape").forGetter(placer -> placer.shape))
            .apply(instance, BigTreeTrunkPlacer::new));

    private final Shape shape;

    public BigTreeTrunkPlacer(int baseHeight, int heightRandA, int heightRandB, Shape shape) {
        super(baseHeight, heightRandA, heightRandB);
        this.shape = shape;
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return ModFeatures.BIG_TREE_TRUNK.get();
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader level,
            BiConsumer<BlockPos, BlockState> blockSetter, RandomSource random, int freeTreeHeight, BlockPos pos,
            TreeConfiguration config) {
        int limit = freeTreeHeight + shape.crown();
        int height = Math.min(limit - 1, (int) (limit * shape.trunkShare()));
        for (int x = 0; x < shape.trunkSize(); x++) {
            for (int z = 0; z < shape.trunkSize(); z++) {
                setDirtAt(level, blockSetter, random, pos.offset(x, -1, z), config);
            }
        }
        List<FoliagePlacer.FoliageAttachment> leaves = new ArrayList<>();
        for (int pass = 0; pass < shape.widths().size(); pass++) {
            grow(level, blockSetter, random, config, pos.above(pass * height), limit, height,
                    shape.widths().get(pass), leaves);
        }
        return leaves;
    }

    private void grow(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter, RandomSource random,
            TreeConfiguration config, BlockPos base, int limit, int height, double width,
            List<FoliagePlacer.FoliageAttachment> leaves) {
        int perLayer = Math.max(1, (int) (1.382 + Math.pow(shape.leafDensity() * limit / 13.0, 2.0)));
        int branchTop = base.getY() + height;
        List<BlockPos> nodes = new ArrayList<>();
        List<Integer> forks = new ArrayList<>();
        int top = limit - shape.leafDepth();
        nodes.add(base.above(top));
        forks.add(branchTop);
        int y = base.getY() + top - 1;
        for (int layer = top; layer >= 0; layer--, y--) {
            float size = layerSize(limit, layer);
            if (size < 0.0F) {
                continue;
            }
            for (int i = 0; i < perLayer; i++) {
                double reach = width * size * (random.nextFloat() + 0.328);
                double angle = random.nextFloat() * 2.0 * Math.PI;
                BlockPos node = new BlockPos(Mth.floor(reach * Math.sin(angle) + base.getX() + 0.5), y,
                        Mth.floor(reach * Math.cos(angle) + base.getZ() + 0.5));
                if (!clear(level, node, node.above(shape.leafDepth()))) {
                    continue;
                }
                double out = Math.sqrt(Mth.square(base.getX() - node.getX()) + Mth.square(base.getZ() - node.getZ()));
                double drop = node.getY() - out * shape.branchSlope();
                int fork = drop > branchTop ? branchTop : (int) drop;
                if (clear(level, new BlockPos(base.getX(), fork, base.getZ()), node)) {
                    nodes.add(node);
                    forks.add(fork);
                }
            }
        }
        for (int i = 0; i < nodes.size(); i++) {
            leaves.add(new FoliagePlacer.FoliageAttachment(nodes.get(i), 0, false));
            if (forks.get(i) - base.getY() >= limit * 0.2) {
                line(level, blockSetter, random, config, new BlockPos(base.getX(), forks.get(i), base.getZ()),
                        nodes.get(i));
            }
        }
        int[][] cells = shape.trunkSize() == 2 ? new int[][] {{0, 0}, {1, 0}, {1, 1}, {0, 1}} : new int[][] {{0, 0}};
        for (int[] cell : cells) {
            BlockPos foot = base.offset(cell[0], 0, cell[1]);
            line(level, blockSetter, random, config, foot, foot.above(height));
        }
    }

    private static float layerSize(int limit, int layer) {
        if (layer < limit * 0.3) {
            return -1.618F;
        }
        float half = limit / 2.0F;
        float off = half - layer;
        float size;
        if (off == 0.0F) {
            size = half;
        } else if (Math.abs(off) >= half) {
            size = 0.0F;
        } else {
            size = (float) Math.sqrt(half * half - off * off);
        }
        return size * 0.5F;
    }

    private boolean clear(LevelSimulatedReader level, BlockPos from, BlockPos to) {
        int[] delta = {to.getX() - from.getX(), to.getY() - from.getY(), to.getZ() - from.getZ()};
        int major = major(delta);
        if (delta[major] == 0) {
            return true;
        }
        int sign = delta[major] > 0 ? 1 : -1;
        for (int step = 0; step != delta[major] + sign; step += sign) {
            double t = step / (double) delta[major];
            BlockPos at = new BlockPos(
                    major == 0 ? from.getX() + step : Mth.floor(from.getX() + t * delta[0]),
                    major == 1 ? from.getY() + step : Mth.floor(from.getY() + t * delta[1]),
                    major == 2 ? from.getZ() + step : Mth.floor(from.getZ() + t * delta[2]));
            if (!level.isStateAtPosition(at, this::open)) {
                return false;
            }
        }
        return true;
    }

    private boolean open(BlockState state) {
        return state.isAir() || state.is(BlockTags.LEAVES) || shape.loose() && grown(state);
    }

    private static boolean grown(BlockState state) {
        return state.is(BlockTags.DIRT) || state.is(BlockTags.LOGS) || state.is(BlockTags.SAPLINGS)
                || state.is(Blocks.VINE);
    }

    private void line(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter, RandomSource random,
            TreeConfiguration config, BlockPos from, BlockPos to) {
        int[] delta = {to.getX() - from.getX(), to.getY() - from.getY(), to.getZ() - from.getZ()};
        int major = major(delta);
        if (delta[major] == 0) {
            return;
        }
        int sign = delta[major] > 0 ? 1 : -1;
        for (int step = 0; step != delta[major] + sign; step += sign) {
            double t = step / (double) delta[major];
            BlockPos at = new BlockPos(Mth.floor(from.getX() + t * delta[0] + 0.5),
                    Mth.floor(from.getY() + t * delta[1] + 0.5), Mth.floor(from.getZ() + t * delta[2] + 0.5));
            int dx = Math.abs(at.getX() - from.getX());
            int dz = Math.abs(at.getZ() - from.getZ());
            Direction.Axis axis = Math.max(dx, dz) == 0 ? Direction.Axis.Y
                    : dx >= dz ? Direction.Axis.X : Direction.Axis.Z;
            if (level.isStateAtPosition(at, state -> state.isAir() || state.is(BlockTags.LEAVES)
                    || state.is(BlockTags.REPLACEABLE_BY_TREES) || grown(state))) {
                blockSetter.accept(at, config.trunkProvider.getState(random, at)
                        .trySetValue(RotatedPillarBlock.AXIS, axis));
            }
        }
    }

    private static int major(int[] delta) {
        int major = 0;
        for (int i = 1; i < 3; i++) {
            if (Math.abs(delta[i]) > Math.abs(delta[major])) {
                major = i;
            }
        }
        return major;
    }
}
