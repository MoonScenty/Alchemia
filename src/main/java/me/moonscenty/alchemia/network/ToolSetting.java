package me.moonscenty.alchemia.network;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ToolSetting() implements CustomPacketPayload {
    public static final Type<ToolSetting> TYPE = new Type<>(Alchemia.id("tool_setting"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToolSetting> STREAM_CODEC =
            StreamCodec.unit(new ToolSetting());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
