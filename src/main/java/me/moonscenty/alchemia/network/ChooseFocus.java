package me.moonscenty.alchemia.network;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * A focus was picked off the wheel: fit this one.
 *
 * <p>It names the focus rather than pointing at a square of the bag. The wheel is drawn from the client's own
 * copy of the bag, which can be a tick behind the server's, so a square number could point at the wrong thing;
 * a name is either carried or it is not, and the server looks for one among what is actually there.
 *
 * <p>The name is the kind and whatever is worked into it, as {@link me.moonscenty.alchemia.wand.Foci#name}
 * writes it. Two foci of the same name are the same focus for this purpose, so it does not matter which of them
 * the server picks up.
 */
public record ChooseFocus(String focus) implements CustomPacketPayload {
    public static final Type<ChooseFocus> TYPE = new Type<>(Alchemia.id("choose_focus"));

    /** Long enough for the longest a focus can be called: a kind and five ranks. */
    private static final int AT_MOST = 256;

    public static final StreamCodec<RegistryFriendlyByteBuf, ChooseFocus> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(AT_MOST), ChooseFocus::focus,
            ChooseFocus::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
