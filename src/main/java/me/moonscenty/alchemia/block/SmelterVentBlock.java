package me.moonscenty.alchemia.block;

import java.util.Map;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SmelterVentBlock extends SmelterSideBlock {
    public static final MapCodec<SmelterVentBlock> CODEC = simpleCodec(SmelterVentBlock::new);
    private static final Map<Direction, VoxelShape> SHAPES = Map.of(
            Direction.NORTH, Block.box(2.0, 2.0, 0.0, 14.0, 14.0, 8.0),
            Direction.SOUTH, Block.box(2.0, 2.0, 8.0, 14.0, 14.0, 16.0),
            Direction.WEST, Block.box(0.0, 2.0, 2.0, 8.0, 14.0, 14.0),
            Direction.EAST, Block.box(8.0, 2.0, 2.0, 16.0, 14.0, 14.0));

    public SmelterVentBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends SmelterVentBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }
}
