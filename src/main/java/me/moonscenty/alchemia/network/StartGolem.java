package me.moonscenty.alchemia.network;

import io.netty.buffer.ByteBuf;
import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.golem.GolemDesign;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** The golem somebody has picked at a press, to be pressed. */
public record StartGolem(GolemDesign design) implements CustomPacketPayload {
    public static final Type<StartGolem> TYPE = new Type<>(Alchemia.id("start_golem"));
    public static final StreamCodec<ByteBuf, StartGolem> STREAM_CODEC =
            GolemDesign.STREAM_CODEC.map(StartGolem::new, StartGolem::design);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
