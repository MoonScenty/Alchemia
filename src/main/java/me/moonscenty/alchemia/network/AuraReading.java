package me.moonscenty.alchemia.network;

import java.util.List;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

// what the thaumometer reads of the chunk: its base, which aspect is picked (0 for all), and the seven amounts
public record AuraReading(int base, int shown, List<Integer> amounts) implements CustomPacketPayload {
    public static final Type<AuraReading> TYPE = new Type<>(Alchemia.id("aura_reading"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AuraReading> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, AuraReading::base,
            ByteBufCodecs.VAR_INT, AuraReading::shown,
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), AuraReading::amounts,
            AuraReading::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
