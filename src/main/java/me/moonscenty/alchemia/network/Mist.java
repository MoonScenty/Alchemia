package me.moonscenty.alchemia.network;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * A fog coming down on one player: for so many ticks, or -- when it only tops up -- at least so many, as a guardian's
 * presence keeps a fog from lifting without lengthening one already there.
 */
public record Mist(int ticks, boolean topUp) implements CustomPacketPayload {
    public static final Type<Mist> TYPE = new Type<>(Alchemia.id("mist"));
    public static final StreamCodec<RegistryFriendlyByteBuf, Mist> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, Mist::ticks,
            ByteBufCodecs.BOOL, Mist::topUp,
            Mist::new);

    /** The fog a warp event brings: two minutes. */
    public static final Mist WARP = new Mist(2400, false);
    /** What standing near a guardian keeps up: ten seconds at least. */
    public static final Mist GUARDIAN = new Mist(200, true);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
