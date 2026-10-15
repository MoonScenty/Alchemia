package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.block.entity.RechargePedestalBlockEntity;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A pedestal that fills a wand set on it from the aura, the way a wand fills itself in the hand but without the
 * holding.
 */
public class RechargePedestalBlock extends BaseEntityBlock {
    public static final MapCodec<RechargePedestalBlock> CODEC = simpleCodec(RechargePedestalBlock::new);

    /** The foot, the step on it and the column, which is what the model is made of. */
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 4, 16),
            Block.box(2, 4, 2, 14, 8, 14),
            Block.box(4, 8, 4, 12, 16, 12));

    public RechargePedestalBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<RechargePedestalBlock> codec() {
        return CODEC;
    }

    /** A wand, or anything else that takes vis back, in hand is set down on it, if there is nothing there already. */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof RechargePedestalBlockEntity pedestal)
                || !pedestal.getItem(0).isEmpty() || !me.moonscenty.alchemia.item.bauble.Recharging.chargeable(stack)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide) {
            pedestal.setItem(0, stack.split(1));
            pop(level, pos, 1.6F);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Takes back whatever is on it. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof RechargePedestalBlockEntity pedestal)
                || pedestal.getItem(0).isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            ItemStack taken = pedestal.removeItemNoUpdate(0);
            if (!player.getInventory().add(taken)) {
                player.drop(taken, false);
            }
            pop(level, pos, 1.5F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void pop(Level level, BlockPos pos, float pitch) {
        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.2F,
                ((level.random.nextFloat() - level.random.nextFloat()) * 0.7F + 1.0F) * pitch);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock())
                && level.getBlockEntity(pos) instanceof RechargePedestalBlockEntity pedestal) {
            net.minecraft.world.Containers.dropContents(level, pos, pedestal);
        }
        super.onRemove(state, level, pos, replacement, moving);
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
        return new RechargePedestalBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.RECHARGE_PEDESTAL.get(),
                RechargePedestalBlockEntity::tick);
    }
}
