package me.moonscenty.alchemia.wand;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

// half-widths of an architect area, and which of them the key changes (0 both, 1 first, 2 second)
public record ArchitectArea(int first, int second, int changing) {
    public static final Codec<ArchitectArea> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("first").forGetter(ArchitectArea::first),
            Codec.INT.fieldOf("second").forGetter(ArchitectArea::second),
            Codec.intRange(0, 2).fieldOf("changing").forGetter(ArchitectArea::changing))
            .apply(instance, ArchitectArea::new));
    public static final StreamCodec<ByteBuf, ArchitectArea> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ArchitectArea::first,
            ByteBufCodecs.VAR_INT, ArchitectArea::second,
            ByteBufCodecs.VAR_INT, ArchitectArea::changing,
            ArchitectArea::new);
}
