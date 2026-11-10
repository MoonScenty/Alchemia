package me.moonscenty.alchemia.worldgen;

import java.util.List;

import com.mojang.serialization.Codec;

import me.moonscenty.alchemia.aura.node.AuraNode;
import me.moonscenty.alchemia.aura.node.NodeType;
import me.moonscenty.alchemia.block.LootBlock;
import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

// Needs the Thaumcraft 5 jar for its shape. Placed by its middle, not the original's corner:
// at 18 wide, a corner origin would build past the feature's chunk reach.
public class MoundFeature extends Feature<NoneFeatureConfiguration> {
    // corner to middle
    public static final int HALF = 9;
    private static final BlockPos NODE = new BlockPos(9, 8, 9);
    private static final List<BlockPos> FOOTINGS = List.of(new BlockPos(9, 9, 9), new BlockPos(0, 9, 0),
            new BlockPos(18, 9, 0), new BlockPos(18, 9, 18), new BlockPos(0, 9, 18));
    private static final List<Block> GROUND = List.of(Blocks.STONE, Blocks.GRASS_BLOCK, Blocks.DIRT);

    public MoundFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        BlockPos corner = context.origin().offset(-HALF, -HALF, -HALF);
        WorldGenLevel level = context.level();
        if (MoundLayout.get().isEmpty() || !FOOTINGS.stream().allMatch(at -> footing(level, corner.offset(at)))) {
            return false;
        }
        return build(level, corner, context.random());
    }

    public static boolean build(WorldGenLevel level, BlockPos corner, RandomSource random) {
        MoundLayout layout = MoundLayout.get().orElse(null);
        if (layout == null) {
            return false;
        }
        for (MoundLayout.Placement placement : layout.blocks()) {
            level.setBlock(corner.offset(placement.offset()), placement.state(), Block.UPDATE_CLIENTS);
        }
        // stairs were placed straight; let them form corners now that everything is in
        for (MoundLayout.Placement placement : layout.blocks()) {
            if (placement.state().getBlock() instanceof StairBlock) {
                BlockPos at = corner.offset(placement.offset());
                BlockState there = level.getBlockState(at);
                if (there.getBlock() instanceof StairBlock) {
                    level.setBlock(at, Block.updateFromNeighbourShapes(there, level, at), Block.UPDATE_CLIENTS);
                }
            }
        }

        BlockPos chest = corner.offset(layout.chest());
        for (int side : new int[] {-2, 2}) {
            BlockPos at = chest.offset(-1, 0, side);
            boolean crate = random.nextFloat() < 0.3F;
            int rarity = LootBlock.rollRarity(random);
            level.setBlock(at, (crate ? ModBlocks.LOOT_CRATE : ModBlocks.LOOT_URN).get().defaultBlockState()
                    .setValue(LootBlock.RARITY, rarity), Block.UPDATE_CLIENTS);
        }
        boolean trap = random.nextInt(3) == 0;
        level.setBlock(chest, (trap ? Blocks.TRAPPED_CHEST : Blocks.CHEST).defaultBlockState()
                .setValue(ChestBlock.FACING, Direction.WEST), Block.UPDATE_CLIENTS);
        if (trap) {
            level.setBlock(chest.below(2), Blocks.TNT.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        RandomizableContainer.setBlockEntityLootTable(level, random, chest, BuiltInLootTables.SIMPLE_DUNGEON);
        // original's: one chest in five gets a second helping
        if (random.nextInt(5) == 0 && level.getBlockEntity(chest)
                instanceof net.minecraft.world.level.block.entity.ChestBlockEntity box) {
            secondHelping(level.getLevel(), box, chest, random);
        }

        List<EntityType<?>> keepers = List.of(EntityType.SKELETON, EntityType.ZOMBIE);
        for (int i = 0; i < layout.spawners().size(); i++) {
            BlockPos at = corner.offset(layout.spawners().get(i));
            level.setBlock(at, Blocks.SPAWNER.defaultBlockState(), Block.UPDATE_CLIENTS);
            if (level.getBlockEntity(at) instanceof SpawnerBlockEntity spawner) {
                spawner.setEntityId(keepers.get(i), random);
            }
        }

        BlockPos above = corner.offset(NODE);
        AuraNode node = new AuraNode(level.getLevel());
        node.moveTo(above.getX() + 0.5, above.getY() + 0.5, above.getZ() + 0.5, 0.0F, 0.0F);
        node.drawUp(random);
        node.setType(NodeType.DARK);
        node.setSize((int) (node.getSize() * 1.5F));
        level.addFreshEntity(node);
        return true;
    }

    static boolean footing(WorldGenLevel level, BlockPos at) {
        int up = 0;
        while (!level.isEmptyBlock(at.above(up)) && up < 3) {
            up++;
        }
        if (up > 2) {
            return false;
        }
        BlockPos top = at.above(up - 1);
        BlockState state = level.getBlockState(top);
        BlockState under = level.getBlockState(top.below());
        if (!level.isEmptyBlock(top.above())) {
            return false;
        }
        for (Block ground : GROUND) {
            if (state.is(ground)
                    || (state.is(Blocks.SNOW) || state.is(Blocks.SHORT_GRASS)) && under.is(ground)) {
                return true;
            }
        }
        return false;
    }

    public static void secondHelping(net.minecraft.server.level.ServerLevel level,
            net.minecraft.world.Container box, BlockPos at, net.minecraft.util.RandomSource random) {
        var table = level.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.SIMPLE_DUNGEON);
        table.fill(box, new net.minecraft.world.level.storage.loot.LootParams.Builder(level)
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN,
                        net.minecraft.world.phys.Vec3.atCenterOf(at))
                .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.CHEST), random.nextLong());
    }
}
