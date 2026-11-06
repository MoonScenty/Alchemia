package me.moonscenty.alchemia.worldgen;

import java.util.Map;
import java.util.Set;

import com.mojang.serialization.Codec;

import me.moonscenty.alchemia.aura.node.AuraNode;
import me.moonscenty.alchemia.aura.node.NodeType;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.aura.AuraGeneration;
import me.moonscenty.alchemia.aura.AuraHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

// One node a chunk: over a temple that starts in it, otherwise 1 in 33 climbing up from the depths.
public class NodeFeature extends Feature<NoneFeatureConfiguration> {
    // the original's scattered features
    private static final Set<ResourceKey<Structure>> TEMPLES = Set.of(BuiltinStructures.DESERT_PYRAMID,
            BuiltinStructures.JUNGLE_TEMPLE, BuiltinStructures.SWAMP_HUT, BuiltinStructures.IGLOO);
    private static final int RARITY = 33;
    private static final int FLOOR = 8;
    // a third of the original's 256-high world
    private static final int LEAST_REACH = 85;
    // a tainted node arrives having already spoiled its chunk
    private static final int TAINT_ON_ARRIVAL = AuraGeneration.BASE;
    private static final int FIBRES = 16;

    public NodeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        BlockPos temple = templeIn(level, origin);
        if (temple != null) {
            spawn(level, temple.above(3), random, new ChunkPos(origin));
            return true;
        }
        if (random.nextInt(RARITY) != 0) {
            return false;
        }

        int x = origin.getX() + random.nextInt(16);
        int z = origin.getZ() + random.nextInt(16);
        int reach = Math.max(LEAST_REACH, level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z));
        BlockPos at = new BlockPos(x, FLOOR + random.nextInt(reach), z);
        while (!level.isEmptyBlock(at)) {
            at = at.above(2);
            if (at.getY() >= level.getMaxBuildHeight() || level.getBlockState(at).is(Blocks.BEDROCK)) {
                return false;
            }
        }
        spawn(level, at, random, new ChunkPos(origin));
        return true;
    }

    private static BlockPos templeIn(WorldGenLevel level, BlockPos origin) {
        var structures = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        for (Map.Entry<Structure, StructureStart> entry : level.getChunk(origin).getAllStarts().entrySet()) {
            StructureStart start = entry.getValue();
            if (start.isValid() && structures.getResourceKey(entry.getKey()).filter(TEMPLES::contains).isPresent()) {
                return start.getPieces().getFirst().getBoundingBox().getCenter();
            }
        }
        return null;
    }

    private static void spawn(WorldGenLevel level, BlockPos at, RandomSource random, ChunkPos home) {
        AuraNode node = new AuraNode(level.getLevel());
        node.moveTo(at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5, 0.0F, 0.0F);
        node.drawUp(random);
        level.addFreshEntity(node);

        if (node.type() != NodeType.TAINTED) {
            return;
        }
        AuraHandler.add(level, at, ModAspects.FLUX, TAINT_ON_ARRIVAL);
        BlockState fibre = ModBlocks.TAINT_FIBRE.get().defaultBlockState();
        for (int i = 0; i < FIBRES; i++) {
            BlockPos spot = at.offset(random.nextInt(16) - random.nextInt(16), random.nextInt(16) - random.nextInt(16),
                    random.nextInt(16) - random.nextInt(16));
            ChunkPos chunk = new ChunkPos(spot);
            if (Math.abs(chunk.x - home.x) > 1 || Math.abs(chunk.z - home.z) > 1 || level.isOutsideBuildHeight(spot)) {
                continue;
            }
            if (level.getBlockState(spot).canBeReplaced() && touchesSolid(level, spot)) {
                level.setBlock(spot, fibre, 2);
            }
        }
    }

    private static boolean touchesSolid(WorldGenLevel level, BlockPos pos) {
        for (Direction side : Direction.values()) {
            BlockPos next = pos.relative(side);
            if (level.getBlockState(next).isFaceSturdy(level, next, side.getOpposite())) {
                return true;
            }
        }
        return false;
    }
}
