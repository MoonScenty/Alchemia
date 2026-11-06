package me.moonscenty.alchemia.worldgen;

import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.registry.ModFeatures;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

// From the surface down to depth blocks under it; vanilla offsets stop at 16.
public class BelowSurfacePlacement extends PlacementModifier {
    public static final MapCodec<BelowSurfacePlacement> CODEC = Codec.intRange(0, 256).fieldOf("depth")
            .xmap(BelowSurfacePlacement::new, placement -> placement.depth);

    private final int depth;

    public BelowSurfacePlacement(int depth) {
        this.depth = depth;
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        int surface = context.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ());
        return Stream.of(pos.atY(surface - random.nextInt(depth + 1)));
    }

    @Override
    public PlacementModifierType<?> type() {
        return ModFeatures.BELOW_SURFACE.get();
    }
}
