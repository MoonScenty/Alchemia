package me.moonscenty.alchemia.network;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * A runic shield taking a blow: whose, and from where -- the id of what struck, or -1 for nothing in particular,
 * -2 for a fall, -3 for something falling on them.
 */
public record ShieldRunes(int shielded, int from) implements CustomPacketPayload {
    public static final Type<ShieldRunes> TYPE = new Type<>(Alchemia.id("shield_runes"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShieldRunes> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ShieldRunes::shielded,
            ByteBufCodecs.INT, ShieldRunes::from,
            ShieldRunes::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
