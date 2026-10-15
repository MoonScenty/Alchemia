package me.moonscenty.alchemia.network;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** A crooked line of light from a block to something, in a colour: the original's block arc. */
public record BlockArc(BlockPos from, int to, float r, float g, float b) implements CustomPacketPayload {
    public static final Type<BlockArc> TYPE = new Type<>(Alchemia.id("block_arc"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BlockArc> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, BlockArc::from,
            ByteBufCodecs.VAR_INT, BlockArc::to,
            ByteBufCodecs.FLOAT, BlockArc::r,
            ByteBufCodecs.FLOAT, BlockArc::g,
            ByteBufCodecs.FLOAT, BlockArc::b,
            BlockArc::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** Sends one to everyone within 32 blocks of what it strikes. */
    public static void send(net.minecraft.server.level.ServerLevel level, BlockPos from,
            net.minecraft.world.entity.Entity to, float r, float g, float b) {
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayersNear(level, null, to.getX(), to.getY(), to.getZ(),
                32.0, new BlockArc(from, to.getId(), r, g, b));
    }
}
