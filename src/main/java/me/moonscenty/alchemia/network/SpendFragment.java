package me.moonscenty.alchemia.network;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** The reader wants a knowledge fragment spent on the note at their table, for a point of each primal. */
public record SpendFragment() implements CustomPacketPayload {
    public static final Type<SpendFragment> TYPE = new Type<>(Alchemia.id("spend_fragment"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpendFragment> STREAM_CODEC =
            StreamCodec.unit(new SpendFragment());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
