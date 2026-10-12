package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.block.entity.CentrifugeBlockEntity;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The essentia centrifuge. Everything it does is its block entity's; everything it looks like is drawn by its
 * renderer, a rotor between two plates, so the block itself draws nothing.
 */
public class CentrifugeBlock extends BaseEntityBlock {
    public static final MapCodec<CentrifugeBlock> CODEC = simpleCodec(CentrifugeBlock::new);

    public CentrifugeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<CentrifugeBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CentrifugeBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.CENTRIFUGE.get(), level.isClientSide
                ? CentrifugeBlockEntity::clientTick : CentrifugeBlockEntity::serverTick);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock()) && level.getBlockEntity(pos) instanceof CentrifugeBlockEntity spun) {
            spun.spill();
        }
        super.onRemove(state, level, pos, replacement, moving);
    }
}
