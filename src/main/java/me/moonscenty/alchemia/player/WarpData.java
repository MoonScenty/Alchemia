package me.moonscenty.alchemia.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

// Permanent never leaves, sticky can be cleansed, temporary wears off; the counter decides when events fire.
public record WarpData(int permanent, int sticky, int temporary, int counter) {
    public static final WarpData NONE = new WarpData(0, 0, 0, 0);

    public static final Codec<WarpData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("permanent", 0).forGetter(WarpData::permanent),
            Codec.INT.optionalFieldOf("sticky", 0).forGetter(WarpData::sticky),
            Codec.INT.optionalFieldOf("temporary", 0).forGetter(WarpData::temporary),
            Codec.INT.optionalFieldOf("counter", 0).forGetter(WarpData::counter))
            .apply(instance, WarpData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, WarpData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, WarpData::permanent,
            ByteBufCodecs.VAR_INT, WarpData::sticky,
            ByteBufCodecs.VAR_INT, WarpData::temporary,
            ByteBufCodecs.VAR_INT, WarpData::counter,
            WarpData::new);

    public int total() {
        return permanent + sticky + temporary;
    }

    public int lasting() {
        return permanent + sticky;
    }

    public enum Kind {
        PERMANENT,
        STICKY,
        TEMPORARY
    }

    public int of(Kind kind) {
        return switch (kind) {
            case PERMANENT -> permanent;
            case STICKY -> sticky;
            case TEMPORARY -> temporary;
        };
    }

    // gaining warp also winds the counter up by the same amount
    public WarpData add(Kind kind, int amount) {
        int gained = Math.max(amount, -of(kind));
        WarpData changed = switch (kind) {
            case PERMANENT -> new WarpData(permanent + gained, sticky, temporary, counter);
            case STICKY -> new WarpData(permanent, sticky + gained, temporary, counter);
            case TEMPORARY -> new WarpData(permanent, sticky, temporary + gained, counter);
        };
        return gained > 0 ? changed.withCounter(counter + gained) : changed;
    }

    public WarpData withCounter(int value) {
        return new WarpData(permanent, sticky, temporary, Math.max(0, value));
    }

    public static WarpData of(Player player) {
        return player.getData(ModAttachments.WARP);
    }
}
