package me.moonscenty.alchemia.aura;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.registry.ModAspects;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

public record AuraTrait(float level, Holder<Aspect> aspect) {
    public static final AuraTrait ORDINARY = new AuraTrait(1.0F, ModAspects.ORDER);

    public static final Codec<AuraTrait> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("level").forGetter(AuraTrait::level),
            ModAspects.REGISTRY.holderByNameCodec().fieldOf("aspect").forGetter(AuraTrait::aspect))
            .apply(instance, AuraTrait::new));

    public static final DataMapType<Biome, AuraTrait> MAP = DataMapType
            .builder(Alchemia.id("aura"), Registries.BIOME, CODEC)
            .build();

    @EventBusSubscriber(modid = Alchemia.MODID)
    public static class Registration {
        @SubscribeEvent
        public static void register(RegisterDataMapTypesEvent event) {
            event.register(MAP);
        }
    }
}
