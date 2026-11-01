package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.aura.ShardTuning;
import me.moonscenty.alchemia.block.entity.DioptraBlockEntity;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class DioptraBlock extends BaseEntityBlock {
    public static final MapCodec<DioptraBlock> CODEC = simpleCodec(DioptraBlock::new);
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

    public DioptraBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(ENABLED, true));
    }

    @Override
    protected MapCodec<DioptraBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ENABLED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(ENABLED, !context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block from, BlockPos fromPos,
            boolean moving) {
        boolean enabled = !level.hasNeighborSignal(pos);
        if (enabled != state.getValue(ENABLED)) {
            level.setBlock(pos, state.setValue(ENABLED, enabled), Block.UPDATE_ALL);
        }
        super.neighborChanged(state, level, pos, from, fromPos, moving);
    }

    // the whole top face counts as the crystal (original used a small hitbox)
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (hit.getDirection() != Direction.UP || hand != InteractionHand.MAIN_HAND
                || !(level.getBlockEntity(pos) instanceof DioptraBlockEntity dioptra)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        int was = dioptra.tuning();
        int now = ShardTuning.of(stack);
        if (was == ShardTuning.NONE && now == ShardTuning.NONE) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide) {
            ItemStack old = ShardTuning.shard(was);
            if (!old.isEmpty() && !player.getInventory().add(old)) {
                player.drop(old, false);
            }
            if (now != ShardTuning.NONE) {
                stack.consume(1, player);
            }
            dioptra.tune(now);
            var shown = ShardTuning.aspect(now);
            if (shown != null) {
                player.displayClientMessage(Component.translatable("block.alchemia.dioptra.showing",
                        shown.value().displayName()), true);
            } else if (now == ShardTuning.BALANCED) {
                player.displayClientMessage(Component.translatable("block.alchemia.dioptra.strongest"), true);
            }
            level.playSound(null, pos, ModSounds.CRYSTAL.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock()) && level.getBlockEntity(pos) instanceof DioptraBlockEntity dioptra) {
            ItemStack shard = ShardTuning.shard(dioptra.tuning());
            if (!shard.isEmpty()) {
                popResource(level, pos, shard);
            }
        }
        super.onRemove(state, level, pos, replacement, moving);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof DioptraBlockEntity dioptra ? dioptra.signal() : 0;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DioptraBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.DIOPTRA.get(), DioptraBlockEntity::tick);
    }
}
