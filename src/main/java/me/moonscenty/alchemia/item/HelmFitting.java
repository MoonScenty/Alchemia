package me.moonscenty.alchemia.item;

import com.mojang.serialization.Codec;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/**
 * Something worked into a fortress helm on the altar, which the helm then wears for good.
 *
 * <p>A helm takes one. That is the original's rule and the reason it is a choice rather than a list: the goggles
 * and the masks all go on the same face, and a helm that was wearing two of them would be wearing neither.
 *
 * <p>The three masks the original had will join this when they are built. The model read out of the jar already
 * has them, hidden, waiting to be shown.
 */
public enum HelmFitting implements StringRepresentable {
    /** Two alchemometers set into the visor. A helm with these on sees what the goggles see. */
    GOGGLES("goggles", "Goggles");

    public static final Codec<HelmFitting> CODEC = StringRepresentable.fromEnum(HelmFitting::values);
    public static final StreamCodec<io.netty.buffer.ByteBuf, HelmFitting> STREAM_CODEC =
            ByteBufCodecs.idMapper(which -> values()[which], HelmFitting::ordinal);

    private final String name;
    private final String part;

    HelmFitting(String name, String part) {
        this.name = name;
        this.part = part;
    }

    /** What the part is called in the model read out of the jar, so the renderer knows what to show. */
    public String part() {
        return part;
    }

    /** Whether a helm wearing this can see an aura node. Only one of them can, and that is what it is for. */
    public boolean reveals() {
        return this == GOGGLES;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
