package me.moonscenty.alchemia.network;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BuyResearch(ResourceLocation research) implements CustomPacketPayload {
    public static final Type<BuyResearch> TYPE = new Type<>(Alchemia.id("buy_research"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BuyResearch> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, BuyResearch::research,
            BuyResearch::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
