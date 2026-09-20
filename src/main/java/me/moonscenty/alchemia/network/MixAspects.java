package me.moonscenty.alchemia.network;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Two aspects the reader wants mixed into whatever they make between them. */
public record MixAspects(ResourceLocation one, ResourceLocation other) implements CustomPacketPayload {
    public static final Type<MixAspects> TYPE = new Type<>(Alchemia.id("mix_aspects"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MixAspects> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, MixAspects::one,
            ResourceLocation.STREAM_CODEC, MixAspects::other,
            MixAspects::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
