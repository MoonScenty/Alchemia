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

/**
 * A barrow: a grassy mound with a way down into it, a burial chamber of cobblestone, and two spawners -- skeletons
 * and zombies -- keeping a chest of dungeon loot, two urns or crates beside it, and a sinister node hanging above,
 * half as big again as its kind. One chest in three is a trap, with TNT under it.
 * <p>
 * The shape is the original's own, read out of the player's jar by {@link MoundLayout}; without the jar there is
 * no mound. It is put down nine below the ground, as the original did, and only where the ground at its middle and
 * its four corners is grass, earth or stone (or snow or grass on them) with air above.
 * <p>
 * The original put it with its corner on the spot it was given. It reaches eighteen blocks across, which from the
 * far side of a chunk is further than a feature may build, so here the spot is its middle.
 */
public class MoundFeature extends Feature<NoneFeatureConfiguration> {
    /** How far the middle is from the corner the original built from, across and up. */
    public static final int HALF = 9;
    /** Where the node hangs, from the corner. */
    private static final BlockPos NODE = new BlockPos(9, 8, 9);
    /** The places the ground must be right, from the corner: the middle and the four corners, all at ground level. */
    private static final List<BlockPos> FOOTINGS = List.of(new BlockPos(9, 9, 9), new BlockPos(0, 9, 0),
            new BlockPos(18, 9, 0), new BlockPos(18, 9, 18), new BlockPos(0, 9, 18));
    /** The ground a footing may stand on. */
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

    /** Puts the mound down with its corner here, whatever is there; false when there is no layout to build. */
    public static boolean build(WorldGenLevel level, BlockPos corner, RandomSource random) {
        MoundLayout layout = MoundLayout.get().orElse(null);
        if (layout == null) {
            return false;
        }
        for (MoundLayout.Placement placement : layout.blocks()) {
            level.setBlock(corner.offset(placement.offset()), placement.state(), Block.UPDATE_CLIENTS);
        }
        // the stairs were all set straight; let them turn their corners now that everything is in
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

    /**
     * Whether the ground is right here: within three blocks up there is air, and what is under that air is grass,
     * earth or stone, or snow or grass standing on one of them.
     */
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
}
