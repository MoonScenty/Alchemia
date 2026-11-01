package me.moonscenty.alchemia.item;

import com.mojang.serialization.Codec;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

// a helm takes one fitting; original's rule
public enum HelmFitting implements StringRepresentable {
    GOGGLES("goggles", "Goggles"),
    GRINNING_DEVIL("grinning_devil", "Mask_0"),
    ANGRY_GHOST("angry_ghost", "Mask_1"),
    SIPPING_FIEND("sipping_fiend", "Mask_2");

    public static final Codec<HelmFitting> CODEC = StringRepresentable.fromEnum(HelmFitting::values);
    public static final StreamCodec<io.netty.buffer.ByteBuf, HelmFitting> STREAM_CODEC =
            ByteBufCodecs.idMapper(which -> values()[which], HelmFitting::ordinal);

    private final String name;
    private final String part;

    HelmFitting(String name, String part) {
        this.name = name;
        this.part = part;
    }

    public String part() {
        return part;
    }

    public boolean reveals() {
        return this == GOGGLES;
    }

    public boolean isMask() {
        return this != GOGGLES;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
