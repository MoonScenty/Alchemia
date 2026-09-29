package me.moonscenty.alchemia.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.moonscenty.alchemia.registry.ModParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import org.joml.Vector3f;

/**
 * What a mote is asked for: a colour and a size.
 * <p>
 * The same two things a dust speck is asked for, and it would have been tempting to reuse that. It cannot be
 * reused: what an options object says when it is asked its type is what decides which particle is drawn, and a
 * dust options says dust however it is spawned.
 *
 * @param colour what the light is, from nought to one in each band
 * @param scale how big, where one is the size the drawing was made at
 */
public record MoteOptions(Vector3f colour, float scale) implements ParticleOptions {
    public static final MapCodec<MoteOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ExtraCodecs.VECTOR3F.fieldOf("color").forGetter(MoteOptions::colour),
                    Codec.FLOAT.fieldOf("scale").forGetter(MoteOptions::scale))
            .apply(instance, MoteOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MoteOptions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VECTOR3F, MoteOptions::colour,
            ByteBufCodecs.FLOAT, MoteOptions::scale,
            MoteOptions::new);

    @Override
    public ParticleType<?> getType() {
        return ModParticles.MOTE.get();
    }
}
