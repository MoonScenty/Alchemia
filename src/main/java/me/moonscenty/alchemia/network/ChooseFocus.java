package me.moonscenty.alchemia.network;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

// names the focus rather than a bag slot: the client's copy of the bag can lag a tick behind
public record ChooseFocus(String focus) implements CustomPacketPayload {
    public static final Type<ChooseFocus> TYPE = new Type<>(Alchemia.id("choose_focus"));

    private static final int AT_MOST = 256;

    public static final StreamCodec<RegistryFriendlyByteBuf, ChooseFocus> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(AT_MOST), ChooseFocus::focus,
            ChooseFocus::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
