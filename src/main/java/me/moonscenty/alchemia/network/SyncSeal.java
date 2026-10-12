package me.moonscenty.alchemia.network;

import java.util.Optional;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.golem.seal.SealPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** One seal put up, changed or, with nothing said of it, taken down. */
public record SyncSeal(SealPos pos, Optional<CompoundTag> seal) implements CustomPacketPayload {
    public static final Type<SyncSeal> TYPE = new Type<>(Alchemia.id("sync_seal"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSeal> STREAM_CODEC = StreamCodec.composite(
            SealPos.STREAM_CODEC, SyncSeal::pos,
            ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG), SyncSeal::seal,
            SyncSeal::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
