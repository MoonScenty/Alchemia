package me.moonscenty.alchemia.aura;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import net.minecraft.core.Holder;

/**
 * The magic standing in the air over one chunk.
 * <p>
 * A chunk has a {@code base}, being as much of any one aspect as the land there will hold, and what is actually in
 * it at the moment. Drawing on the aura lowers what is there; it comes back by seeping in from better-off chunks
 * nearby, so somewhere worked hard stays thin until its neighbours can feed it.
 *
 * @param base    the most of any single aspect this chunk will hold
 * @param aspects what is in the air now
 */
public record AuraChunk(int base, AspectList aspects) {
    public static final AuraChunk NONE = new AuraChunk(0, AspectList.EMPTY);

    public static final Codec<AuraChunk> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("base").forGetter(AuraChunk::base),
            AspectList.CODEC.fieldOf("aspects").forGetter(AuraChunk::aspects))
            .apply(instance, AuraChunk::new));

    public int get(Holder<Aspect> aspect) {
        return aspects.get(aspect);
    }

    /** Whether this chunk has been worked out at all, as opposed to never having been visited. */
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
