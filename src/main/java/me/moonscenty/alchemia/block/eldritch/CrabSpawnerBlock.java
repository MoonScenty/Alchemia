package me.moonscenty.alchemia.block.eldritch;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** A crab vent: a block of crust with a vent in one face, that breeds eldritch crabs. Hard to break, and gives nothing. */
public class CrabSpawnerBlock extends BaseEntityBlock {
    public static final MapCodec<CrabSpawnerBlock> CODEC = simpleCodec(CrabSpawnerBlock::new);

    public CrabSpawnerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<CrabSpawnerBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrabSpawnerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.CRAB_SPAWNER.get(), CrabSpawnerBlockEntity::tick);
    }
}
