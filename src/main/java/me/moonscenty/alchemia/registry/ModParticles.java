package me.moonscenty.alchemia.registry;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.particle.MoteOptions;
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

    private ModParticles() {
    }
}
