package me.moonscenty.alchemia.registry;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.particle.EssenceOptions;
import me.moonscenty.alchemia.particle.MarkOptions;
import me.moonscenty.alchemia.particle.MoteOptions;
import me.moonscenty.alchemia.particle.SparkleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** What the mod throws into the air. */
public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, Alchemia.MODID);

    /**
     * A soft round smudge of light, in whatever colour it was given.
     * <p>
     * Dust is a hard speck that falls; this gives out at its edge and rises. Both are asked for with a colour
     * and a size, but an options object is what decides which particle gets drawn, so they cannot share one.
     */
    public static final DeferredHolder<ParticleType<?>, ParticleType<MoteOptions>> MOTE =
            PARTICLES.register("mote", () -> new ParticleType<MoteOptions>(false) {
                @Override
                public MapCodec<MoteOptions> codec() {
                    return MoteOptions.CODEC;
                }

                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, MoteOptions> streamCodec() {
                    return MoteOptions.STREAM_CODEC;
                }
            });

    /** A spark of vis drawn through the air to whatever is pulling on it, as the original drew it. */
    public static final DeferredHolder<ParticleType<?>, ParticleType<SparkleOptions>> SPARKLE =
            register("sparkle", SparkleOptions.CODEC, SparkleOptions.STREAM_CODEC);

    /** A drop of essentia on its way to whatever drew on it, as the original drew it. */
    public static final DeferredHolder<ParticleType<?>, ParticleType<EssenceOptions>> ESSENCE =
            register("essence", EssenceOptions.CODEC, EssenceOptions.STREAM_CODEC);

    /** A sounding tool's mark on an ore it heard, as the original drew it. */
    public static final DeferredHolder<ParticleType<?>, ParticleType<MarkOptions>> MARK =
            register("mark", MarkOptions.CODEC, MarkOptions.STREAM_CODEC);

    private ModParticles() {
    }

    private static <T extends ParticleOptions> DeferredHolder<ParticleType<?>, ParticleType<T>> register(String name,
            MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> stream) {
        return PARTICLES.register(name, () -> new ParticleType<T>(false) {
            @Override
            public MapCodec<T> codec() {
                return codec;
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
                return stream;
            }
        });
    }
}
