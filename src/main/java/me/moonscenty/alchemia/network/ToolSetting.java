package me.moonscenty.alchemia.network;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * The setting key was pressed: turn whatever setting the thing in hand has to the next one.
 * <p>
 * A wand whose focus has an architect's eye is turned down a notch of reach; the shovel of the earthmover is
 * turned to its next way of laying. What the next setting is is not said here -- the server, which knows what
 * is actually in the hand, decides what pressing the key comes to.
 */
public record ToolSetting() implements CustomPacketPayload {
    public static final Type<ToolSetting> TYPE = new Type<>(Alchemia.id("tool_setting"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToolSetting> STREAM_CODEC =
            StreamCodec.unit(new ToolSetting());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
