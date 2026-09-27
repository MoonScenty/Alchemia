package me.moonscenty.alchemia.block;

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

/**
 * A length of pipe with a little room in it.
 * <p>
 * It carries like any other, but it also keeps a few points of its own, drawing them in whenever the run has any to
 * spare and giving them up as soon as anything further along wants them. A works that draws in bursts therefore has
 * something to draw on between them instead of waiting on whatever is boiling.
 */
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

    /** Says what it is sitting on. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BufferTubeBlockEntity buffer)) {
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

    /** What is in it, most of it first, written out as a list. */
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
        if (!state.is(replacement.getBlock()) && level.getBlockEntity(pos) instanceof BufferTubeBlockEntity buffer) {
            buffer.spill();
        }
        super.onRemove(state, level, pos, replacement, moving);
    }
}
