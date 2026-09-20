package me.moonscenty.alchemia.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.moonscenty.alchemia.registry.ModFeatures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

/**
 * Scatters a plant across the ground around the foot of a tree.
 */
public class UndergrowthDecorator extends TreeDecorator {
    public static final MapCodec<UndergrowthDecorator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockStateProvider.CODEC.fieldOf("plant").forGetter(decorator -> decorator.plant),
            TagKey.codec(Registries.BLOCK).fieldOf("can_grow_on").forGetter(decorator -> decorator.canGrowOn),
            Codec.intRange(1, 64).fieldOf("tries").forGetter(decorator -> decorator.tries),
            Codec.intRange(1, 16).fieldOf("radius").forGetter(decorator -> decorator.radius))
            .apply(instance, UndergrowthDecorator::new));

    /** How far up and down a sample looks for the surface, so the plants follow sloping ground. */
    private static final int VERTICAL_REACH = 3;

    private final BlockStateProvider plant;
    private final TagKey<Block> canGrowOn;
    private final int tries;
    private final int radius;

    public UndergrowthDecorator(BlockStateProvider plant, TagKey<Block> canGrowOn, int tries, int radius) {
        this.plant = plant;
        this.canGrowOn = canGrowOn;
        this.tries = tries;
        this.radius = radius;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return ModFeatures.UNDERGROWTH.get();
    }

    @Override
    public void place(TreeDecorator.Context context) {
        if (context.logs().isEmpty()) {
            return;
        }

        // the logs are sorted by height, so the first one sits at the foot of the trunk
        BlockPos base = context.logs().getFirst();
        RandomSource random = context.random();
        for (int i = 0; i < tries; i++) {
            BlockPos sample = base.offset(
                    random.nextInt(radius) - random.nextInt(radius),
                    0,
                    random.nextInt(radius) - random.nextInt(radius));
            BlockPos pos = findSurface(context, sample);
            if (pos != null) {
                context.setBlock(pos, plant.getState(random, pos));
            }
        }
    }

    /** The highest open spot near {@code around} whose ground the plant can take root in, or null if there is none. */
    private BlockPos findSurface(TreeDecorator.Context context, BlockPos around) {
        for (int y = VERTICAL_REACH; y >= -VERTICAL_REACH; y--) {
            BlockPos pos = around.above(y);
            if (context.isAir(pos) && context.level().isStateAtPosition(pos.below(), state -> state.is(canGrowOn))) {
                return pos;
            }
        }
        return null;
    }
}
