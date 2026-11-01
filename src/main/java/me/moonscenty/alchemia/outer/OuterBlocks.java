package me.moonscenty.alchemia.outer;

import java.util.function.BiConsumer;
import java.util.function.IntUnaryOperator;

import me.moonscenty.alchemia.block.taint.TaintFibreBlock;
import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;

/** Pieces the old structure builders shared, set down the way the original set them. */
public final class OuterBlocks {
    private OuterBlocks() {
    }

    /** The original's stairs by their old number: the low bits which way (east, west, south, north), four upside down. */
    public static BlockState stairs(Block stairs, int meta) {
        Direction facing = switch (meta & 3) {
            case 0 -> Direction.EAST;
            case 1 -> Direction.WEST;
            case 2 -> Direction.SOUTH;
            default -> Direction.NORTH;
        };
        return stairs.defaultBlockState().setValue(StairBlock.FACING, facing)
                .setValue(StairBlock.HALF, (meta & 4) != 0 ? Half.TOP : Half.BOTTOM);
    }

    /** Ancient stone stairs by their old number. */
    public static BlockState ancientStairs(int meta) {
        return stairs(ModBlocks.ANCIENT_STONE.stairs().get(), meta);
    }

    /** An obelisk: its capstone here and four blocks of body above. */
    public static void obelisk(BlockPos at, BiConsumer<BlockPos, BlockState> set) {
        set.accept(at, ModBlocks.ELDRITCH_OBELISK.get().defaultBlockState());
        for (int up = 1; up <= 4; up++) {
            set.accept(at.above(up), ModBlocks.ELDRITCH_OBELISK_BODY.get().defaultBlockState());
        }
    }

    /** Whether any block next to this one has a full face turned towards it. */
    public static boolean nextToSolid(BlockGetter level, BlockPos pos) {
        for (Direction side : Direction.values()) {
            BlockPos next = pos.relative(side);
            if (level.getBlockState(next).isFaceSturdy(level, next, side.getOpposite())) {
                return true;
            }
        }
        return false;
    }

    /** Taint fibre in an empty block against something solid, two times in three. */
    public static void fibre(LevelAccessor level, BlockPos at, IntUnaryOperator nextInt, int flags) {
        if (level.isEmptyBlock(at) && nextToSolid(level, at) && nextInt.applyAsInt(3) != 0) {
            level.setBlock(at, TaintFibreBlock.fitted(level, at, ModBlocks.TAINT_FIBRE.get().defaultBlockState()),
                    flags);
        }
    }
}
