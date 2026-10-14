package me.moonscenty.alchemia.block.eldritch;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** The lock on the great hall's door. A runed tablet turns it; nothing breaks it. */
public class EldritchLockBlock extends BaseEntityBlock {
    public static final MapCodec<EldritchLockBlock> CODEC = simpleCodec(EldritchLockBlock::new);

    public EldritchLockBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<EldritchLockBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (!held.is(ModItems.RUNED_TABLET.get()) || !(level.getBlockEntity(pos) instanceof EldritchLockBlockEntity lock)
                || !lock.waiting()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide) {
            lock.turn();
            held.consume(1, player);
            level.playSound(null, pos, ModSounds.RUNIC_SHIELD_CHARGE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EldritchLockBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.ELDRITCH_LOCK.get(), EldritchLockBlockEntity::tick);
    }
}
