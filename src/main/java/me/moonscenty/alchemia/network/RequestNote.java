package me.moonscenty.alchemia.network;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Asked for when a reader taps a subject they are ready to take on. The server decides whether they may have it;
 * this only says which one was tapped.
 */
public record RequestNote(ResourceLocation research) implements CustomPacketPayload {
    public static final Type<RequestNote> TYPE = new Type<>(Alchemia.id("request_note"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestNote> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, RequestNote::research,
            RequestNote::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
