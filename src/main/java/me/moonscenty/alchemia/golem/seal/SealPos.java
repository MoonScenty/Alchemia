package me.moonscenty.alchemia.golem.seal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.StreamCodec;

/** Where a seal is: the block it is stuck to, and which face of it. */
public record SealPos(BlockPos pos, Direction face) {
    public static final Codec<SealPos> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(SealPos::pos),
            Direction.CODEC.fieldOf("face").forGetter(SealPos::face)
    ).apply(instance, SealPos::new));

    public static final StreamCodec<ByteBuf, SealPos> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SealPos::pos,
            Direction.STREAM_CODEC, SealPos::face,
            SealPos::new);

    public SealPos {
        pos = pos.immutable();
    }

    /** The block in front of the face the seal is on. */
    public BlockPos front() {
        return pos.relative(face);
    }
}
