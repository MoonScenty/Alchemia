package me.moonscenty.alchemia.network;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ChangeFocus(boolean remove) implements CustomPacketPayload {
    public static final Type<ChangeFocus> TYPE = new Type<>(Alchemia.id("change_focus"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChangeFocus> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ChangeFocus::remove,
            ChangeFocus::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
