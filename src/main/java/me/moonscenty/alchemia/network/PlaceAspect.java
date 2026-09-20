package me.moonscenty.alchemia.network;

import java.util.Optional;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.research.HexGrid;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Where the reader touched the board, and what they meant to write there. An empty aspect rubs the space out again.
 */
public record PlaceAspect(HexGrid.Hex at, Optional<ResourceLocation> aspect) implements CustomPacketPayload {
    public static final Type<PlaceAspect> TYPE = new Type<>(Alchemia.id("place_aspect"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlaceAspect> STREAM_CODEC = StreamCodec.composite(
            HexGrid.Hex.STREAM_CODEC, PlaceAspect::at,
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), PlaceAspect::aspect,
            PlaceAspect::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
