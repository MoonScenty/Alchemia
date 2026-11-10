package me.moonscenty.alchemia.network;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record WarpVignette(int ticks) implements CustomPacketPayload {
    public static final Type<WarpVignette> TYPE = new Type<>(Alchemia.id("warp_vignette"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WarpVignette> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, WarpVignette::ticks,
            WarpVignette::new);

    public static final WarpVignette EVENT = new WarpVignette(100);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
