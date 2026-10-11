package me.moonscenty.alchemia.network;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * A key was pressed with a wand in hand: set how far the focus on it works.
 * <p>
 * Only a focus with an architect's eye worked into it listens. How far it is set to is not said here -- the
 * client only asks for the next setting, and the server, which knows what the focus is and how far it can be
 * made to reach, decides what that comes to.
 */
public record SetReach() implements CustomPacketPayload {
    public static final Type<SetReach> TYPE = new Type<>(Alchemia.id("set_reach"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetReach> STREAM_CODEC =
            StreamCodec.unit(new SetReach());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
