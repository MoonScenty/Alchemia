package me.moonscenty.alchemia.network;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

// source: attacker entity id, -1 none, -2 fall, -3 falling block
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
