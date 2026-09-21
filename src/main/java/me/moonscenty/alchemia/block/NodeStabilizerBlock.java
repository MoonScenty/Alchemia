package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.block.entity.NodeStabilizerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Holds a node still.
 * <p>
 * A node over one of these stops drifting, stops being drawn to its neighbours and cannot be dragged away, which is
 * what makes it safe to build anything around. Feeding it redstone lets go of it again.
 */
public class NodeStabilizerBlock extends BaseEntityBlock {
    public static final MapCodec<NodeStabilizerBlock> CODEC = simpleCodec(NodeStabilizerBlock::new);

    // the model is a full square base with a pyramid on it, twelve sixteenths tall
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 12, 16);

    public NodeStabilizerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<NodeStabilizerBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NodeStabilizerBlockEntity(pos, state);
    }

    /** It reaches for a node every tick, on both sides: the server to hold it, the client to draw the arms. */
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.NODE_STABILIZER.get(), NodeStabilizerBlockEntity::tick);
    }
}
