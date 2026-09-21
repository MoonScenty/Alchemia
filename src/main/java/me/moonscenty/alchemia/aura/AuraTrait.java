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

/**
 * What the land itself does to the magic over it.
 * <p>
 * The original read this off its own biome dictionary; here it is a data map keyed by biome tags, so a pack can say
 * what its own biomes are like without touching any code.
 *
 * @param level  how much aura the land holds, as a share of the usual amount
 * @param aspect the primal this land leans towards, which it holds more of than the rest
 */
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
