package me.moonscenty.alchemia.registry;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.particle.EssenceOptions;
import me.moonscenty.alchemia.particle.MarkOptions;
import me.moonscenty.alchemia.particle.SparkleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, Alchemia.MODID);

    public static final DeferredHolder<ParticleType<?>, ParticleType<SparkleOptions>> SPARKLE =
            register("sparkle", SparkleOptions.CODEC, SparkleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, ParticleType<EssenceOptions>> ESSENCE =
            register("essence", EssenceOptions.CODEC, EssenceOptions.STREAM_CODEC);

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
