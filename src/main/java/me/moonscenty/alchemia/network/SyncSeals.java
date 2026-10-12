package me.moonscenty.alchemia.network;

import java.util.List;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Every seal in the world a player has just come into, in place of whatever they knew of before. */
public record SyncSeals(List<CompoundTag> seals) implements CustomPacketPayload {
    public static final Type<SyncSeals> TYPE = new Type<>(Alchemia.id("sync_seals"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSeals> STREAM_CODEC =
            ByteBufCodecs.COMPOUND_TAG.apply(ByteBufCodecs.list()).map(SyncSeals::new, SyncSeals::seals).cast();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
