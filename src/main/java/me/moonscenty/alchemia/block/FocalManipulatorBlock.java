package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.block.entity.FocalManipulatorBlockEntity;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The bench a focus is worked on.
 *
 * <p>It does one thing: it screws another upgrade into whatever focus is laid on it. The price is paid in levels
 * up front and in vis drawn out of the air over the following seconds, so a manipulator wants standing somewhere
 * the aura is thick, and it will sit there drawing for a while before anything comes of it.
 */
public class FocalManipulatorBlock extends BaseEntityBlock {
    public static final MapCodec<FocalManipulatorBlock> CODEC = simpleCodec(FocalManipulatorBlock::new);

    public FocalManipulatorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<FocalManipulatorBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FocalManipulatorBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, ModBlockEntities.FOCAL_MANIPULATOR.get(),
                        FocalManipulatorBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof FocalManipulatorBlockEntity bench)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            player.openMenu(bench, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Whatever was laid on it falls out, as it does from any bench that is broken with work on it. */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock())
                && level.getBlockEntity(pos) instanceof FocalManipulatorBlockEntity bench) {
            net.minecraft.world.Containers.dropContents(level, pos, bench);
        }
        super.onRemove(state, level, pos, replacement, moving);
    }
}
