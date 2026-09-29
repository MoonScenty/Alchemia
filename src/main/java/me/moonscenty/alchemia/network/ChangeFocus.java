package me.moonscenty.alchemia.network;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * A key was pressed with a wand in hand: fit the next focus, or take off the one that is on it.
 * <p>
 * Which focus is next is not said here. The server knows what is in the player's bag and the client's copy of it
 * can be a tick behind, so the client says what was asked for and the server works out what that means.
 */
public record ChangeFocus(boolean remove) implements CustomPacketPayload {
    public static final Type<ChangeFocus> TYPE = new Type<>(Alchemia.id("change_focus"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChangeFocus> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ChangeFocus::remove,
            ChangeFocus::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
