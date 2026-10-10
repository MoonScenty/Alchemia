package me.moonscenty.alchemia.particle;

import org.joml.Vector3f;

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

/**
 * What a vis sparkle is asked for: the colour of the vis it carries, and where it is headed.
 *
 * @param colour the vis's colour, as {@code 0xRRGGBB}
 * @param target where in the world it is drawn to
 */
public record SparkleOptions(int colour, Vector3f target) implements ParticleOptions {
    public static final MapCodec<SparkleOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.INT.fieldOf("color").forGetter(SparkleOptions::colour),
                    ExtraCodecs.VECTOR3F.fieldOf("target").forGetter(SparkleOptions::target))
            .apply(instance, SparkleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SparkleOptions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SparkleOptions::colour,
            ByteBufCodecs.VECTOR3F, SparkleOptions::target,
            SparkleOptions::new);

    @Override
    public ParticleType<?> getType() {
        return ModParticles.SPARKLE.get();
    }
}
