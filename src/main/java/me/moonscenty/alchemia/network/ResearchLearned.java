package me.moonscenty.alchemia.network;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ResearchLearned(ResourceLocation research) implements CustomPacketPayload {
    public static final Type<ResearchLearned> TYPE = new Type<>(Alchemia.id("research_learned"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ResearchLearned> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, ResearchLearned::research,
            ResearchLearned::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
