package me.moonscenty.alchemia.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModFeatures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class SpiderNestDecorator extends TreeDecorator {
    public static final MapCodec<SpiderNestDecorator> CODEC = Codec.floatRange(0.0F, 1.0F).fieldOf("chance")
            .xmap(SpiderNestDecorator::new, decorator -> decorator.chance);

    private static final int WEB_TRIES = 50;

    private final float chance;

    public SpiderNestDecorator(float chance) {
        this.chance = chance;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return ModFeatures.SPIDER_NEST.get();
    }

    @Override
    public void place(TreeDecorator.Context context) {
        if (!(context.level() instanceof WorldGenLevel level) || context.logs().isEmpty()) {
            return;
        }
        RandomSource random = context.random();
        if (random.nextFloat() >= chance) {
            return;
        }

        BlockPos base = corner(level, context);
        if (base == null) {
            return;
        }
        // original: cave spider spawner under the trunk, webs among the wood, dungeon chest beneath
        context.setBlock(base.below(), Blocks.SPAWNER.defaultBlockState());
        if (level.getBlockEntity(base.below()) instanceof SpawnerBlockEntity spawner) {
            spawner.setEntityId(EntityType.CAVE_SPIDER, random);
        }

        for (int i = 0; i < WEB_TRIES; i++) {
            BlockPos at = base.offset(random.nextInt(14) - 7, random.nextInt(10), random.nextInt(14) - 7);
            if (level.isEmptyBlock(at) && touchesWood(level, at)) {
                context.setBlock(at, Blocks.COBWEB.defaultBlockState());
            }
        }

        context.setBlock(base.below(2), Blocks.CHEST.defaultBlockState());
        RandomizableContainer.setBlockEntityLootTable(level, random, base.below(2), BuiltInLootTables.SIMPLE_DUNGEON);
    }

    // logs come sorted by height; the trunk grows out from its lowest north-west log
    private static BlockPos corner(WorldGenLevel level, TreeDecorator.Context context) {
        BlockPos corner = null;
        for (BlockPos log : context.logs()) {
            // the dirt put under the trunk is listed among the logs
            if (!level.getBlockState(log).is(ModBlocks.GREATWOOD.log().get())) {
                continue;
            }
            if (corner == null) {
                corner = log;
            } else if (log.getY() != corner.getY()) {
                break;
            } else if (log.getX() < corner.getX() || log.getX() == corner.getX() && log.getZ() < corner.getZ()) {
                corner = log;
            }
        }
        return corner;
    }

    private static boolean touchesWood(WorldGenLevel level, BlockPos at) {
        for (Direction side : Direction.values()) {
            BlockState next = level.getBlockState(at.relative(side));
            if (next.is(ModBlocks.GREATWOOD.log().get()) || next.is(ModBlocks.GREATWOOD.leaves().get())
                    || next.is(ModBlocks.SILVERWOOD.log().get()) || next.is(ModBlocks.SILVERWOOD.leaves().get())) {
                return true;
            }
        }
        return false;
    }
}
