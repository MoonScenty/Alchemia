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

/**
 * What a sounding mark is asked for: how long it waits before it shows, so that the marks spread out from where the
 * tool struck as a wave.
 *
 * @param waits ticks before it shows
 */
public record MarkOptions(int waits) implements ParticleOptions {
    public static final MapCodec<MarkOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.INT.fieldOf("waits").forGetter(MarkOptions::waits))
            .apply(instance, MarkOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MarkOptions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MarkOptions::waits,
            MarkOptions::new);

    @Override
    public ParticleType<?> getType() {
        return ModParticles.MARK.get();
    }
}
