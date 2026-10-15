package me.moonscenty.alchemia.network;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** H was pressed: switch the thaumostatic harness on or off. The server decides whether there is fuel to. */
public record ToggleHover() implements CustomPacketPayload {
    public static final Type<ToggleHover> TYPE = new Type<>(Alchemia.id("toggle_hover"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleHover> STREAM_CODEC =
            StreamCodec.unit(new ToggleHover());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
