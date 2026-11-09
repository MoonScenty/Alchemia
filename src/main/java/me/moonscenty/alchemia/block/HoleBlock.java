package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.block.entity.HoleBlockEntity;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class HoleBlock extends BaseEntityBlock {
    public static final MapCodec<HoleBlock> CODEC = simpleCodec(HoleBlock::new);

    public HoleBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HoleBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, ModBlockEntities.HOLE.get(), HoleBlockEntity::serverTick);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState to, boolean moving) {
        if (!state.is(to.getBlock()) && level.getBlockEntity(pos) instanceof HoleBlockEntity hole && to.isAir()) {
            hole.fillIn(level, pos);
        }
        super.onRemove(state, level, pos, to, moving);
    }

    @Override
    public float getDestroyProgress(BlockState state, net.minecraft.world.entity.player.Player player,
            net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        return 0.0F;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, net.minecraft.world.level.BlockGetter level,
            BlockPos pos) {
        return true;
    }

    public static boolean isHole(BlockState state) {
        return state.getBlock() instanceof HoleBlock;
    }

    public static boolean borable(BlockState state) {
        return !state.isAir() && !state.canBeReplaced() && !isHole(state) && !refuses(state);
    }

    // original's blacklist
    private static boolean refuses(BlockState state) {
        return state.is(net.minecraft.tags.BlockTags.BEDS) || state.is(net.minecraft.tags.BlockTags.DOORS)
                || state.is(net.minecraft.world.level.block.Blocks.PISTON)
                || state.is(net.minecraft.world.level.block.Blocks.STICKY_PISTON)
                || state.is(net.minecraft.world.level.block.Blocks.PISTON_HEAD)
                || state.is(net.minecraft.world.level.block.Blocks.MOVING_PISTON)
                || state.is(me.moonscenty.alchemia.registry.ModBlocks.INFERNAL_FURNACE.get());
    }
}
