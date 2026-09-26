package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.block.entity.ArcaneWorkbenchBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A workbench with a cloth laid over it and a place to set a wand down.
 * <p>
 * What is on it stays there when the screen is closed, wand and all: the charger that goes above the bench tops the
 * wand up while nobody is working, which it could not do if closing the screen swept the bench clear.
 */
public class ArcaneWorkbenchBlock extends BaseEntityBlock {
    public static final MapCodec<ArcaneWorkbenchBlock> CODEC = simpleCodec(ArcaneWorkbenchBlock::new);

    public ArcaneWorkbenchBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<ArcaneWorkbenchBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcaneWorkbenchBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ArcaneWorkbenchBlockEntity bench)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            player.openMenu(bench);
            level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 0.7F, 1.0F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock())
                && level.getBlockEntity(pos) instanceof ArcaneWorkbenchBlockEntity bench) {
            ArcaneWorkbenchBlockEntity.drop(level, pos, bench);
        }
        super.onRemove(state, level, pos, replacement, moving);
    }
}
