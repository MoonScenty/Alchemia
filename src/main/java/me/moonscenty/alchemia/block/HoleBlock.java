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

/**
 * A hole where a block used to be, which closes again on its own.
 *
 * <p>It is not a block anybody places or picks up: it is a gap held open by a working, and it is remembering what
 * was there so it can put it back. Nothing can be built in it and nothing comes of breaking it, because breaking
 * it would throw away the only copy of what it is standing in for.
 *
 * <p>Invisible, empty of collision, and dark. Walking into a wall and finding nothing there is the whole of it.
 */
public class HoleBlock extends BaseEntityBlock {
    public static final MapCodec<HoleBlock> CODEC = simpleCodec(HoleBlock::new);

    public HoleBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    /** There is nothing to draw. A hole is the absence of what was drawn before. */
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

    /**
     * What stood here, put back.
     * <p>
     * A hole that is taken out of the world some other way -- a piston, another hole, somebody in creative --
     * still has to give back what it was holding, or the block is simply gone.
     */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState to, boolean moving) {
        if (!state.is(to.getBlock()) && level.getBlockEntity(pos) instanceof HoleBlockEntity hole && to.isAir()) {
            hole.fillIn(level, pos);
        }
        super.onRemove(state, level, pos, to, moving);
    }

    /** Nothing is gained by hitting it, and a hole hit hard enough to break would take a block with it. */
    @Override
    public float getDestroyProgress(BlockState state, net.minecraft.world.entity.player.Player player,
            net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        return 0.0F;
    }

    /** Light passes through a hole, which is the only reason it is worth boring one. */
    @Override
    protected boolean propagatesSkylightDown(BlockState state, net.minecraft.world.level.BlockGetter level,
            BlockPos pos) {
        return true;
    }

    /** Equality by block alone, so that two holes side by side are the same sort of nothing. */
    public static boolean isHole(BlockState state) {
        return state.getBlock() instanceof HoleBlock;
    }

    /**
     * Whether there is anything here worth boring through.
     * <p>
     * Something solid and not already a hole. How hard it is is asked separately, because that needs a world to
     * ask in and this does not.
     */
    public static boolean borable(BlockState state) {
        return !state.isAir() && !state.canBeReplaced() && !isHole(state);
    }
}
