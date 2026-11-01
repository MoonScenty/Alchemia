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

public record EssenceOptions(int colour, Vector3f target, int count) implements ParticleOptions {
    public static final MapCodec<EssenceOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.INT.fieldOf("color").forGetter(EssenceOptions::colour),
                    ExtraCodecs.VECTOR3F.fieldOf("target").forGetter(EssenceOptions::target),
                    Codec.INT.fieldOf("count").forGetter(EssenceOptions::count))
            .apply(instance, EssenceOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EssenceOptions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, EssenceOptions::colour,
            ByteBufCodecs.VECTOR3F, EssenceOptions::target,
            ByteBufCodecs.VAR_INT, EssenceOptions::count,
            EssenceOptions::new);

    @Override
    public ParticleType<?> getType() {
        return ModParticles.ESSENCE.get();
    }
}
