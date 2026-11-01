package me.moonscenty.alchemia.aura;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import net.minecraft.core.Holder;

public record AuraChunk(int base, AspectList aspects) {
    public static final AuraChunk NONE = new AuraChunk(0, AspectList.EMPTY);

    public static final Codec<AuraChunk> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("base").forGetter(AuraChunk::base),
            AspectList.CODEC.fieldOf("aspects").forGetter(AuraChunk::aspects))
            .apply(instance, AuraChunk::new));

    public int get(Holder<Aspect> aspect) {
        return aspects.get(aspect);
    }

    public boolean exists() {
        return base > 0;
    }

    public AuraChunk with(AspectList aspects) {
        return new AuraChunk(base, aspects);
    }

    public AuraChunk add(Holder<Aspect> aspect, int amount) {
        return with(aspects.add(aspect, amount));
    }

    public AuraChunk reduce(Holder<Aspect> aspect, int amount) {
        return with(aspects.reduce(aspect, amount));
    }
}
