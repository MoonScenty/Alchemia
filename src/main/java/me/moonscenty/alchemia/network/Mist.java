package me.moonscenty.alchemia.network;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record Mist(int ticks, boolean topUp) implements CustomPacketPayload {
    public static final Type<Mist> TYPE = new Type<>(Alchemia.id("mist"));
    public static final StreamCodec<RegistryFriendlyByteBuf, Mist> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, Mist::ticks,
            ByteBufCodecs.BOOL, Mist::topUp,
            Mist::new);

    public static final Mist WARP = new Mist(2400, false);
    public static final Mist GUARDIAN = new Mist(200, true);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
