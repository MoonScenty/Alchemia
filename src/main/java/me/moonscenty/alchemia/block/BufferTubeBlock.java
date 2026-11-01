package me.moonscenty.alchemia.block;

import me.moonscenty.alchemia.block.entity.Spills;
import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.block.entity.BufferTubeBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BufferTubeBlock extends TubeBlock {
    public static final MapCodec<BufferTubeBlock> CODEC = simpleCodec(BufferTubeBlock::new);

    public BufferTubeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BufferTubeBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BufferTubeBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BufferTubeBlockEntity buffer)) {
            return InteractionResult.PASS;
        }
        // empty hand only: answering a held item would block that item's own use
        if (!player.getMainHandItem().isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            player.displayClientMessage((buffer.held().isEmpty()
                    ? Component.translatable("block.alchemia.tube_buffer.empty")
                    : Component.translatable("block.alchemia.tube_buffer.holding",
                            names(buffer), buffer.held().total(), BufferTubeBlockEntity.CAPACITY))
                    .copy().withStyle(ChatFormatting.GRAY), true);
        }
        return level.isClientSide ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    private static MutableComponent names(BufferTubeBlockEntity buffer) {
        MutableComponent out = Component.empty();
        boolean first = true;
        for (Holder<Aspect> aspect : buffer.held().sortedByAmount()) {
            if (!first) {
                out.append(", ");
            }
            out.append(aspect.value().displayName());
            first = false;
        }
        return out;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        Spills.onRemove(state, replacement, level, pos);
        super.onRemove(state, level, pos, replacement, moving);
    }
}
