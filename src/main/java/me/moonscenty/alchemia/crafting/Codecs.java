package me.moonscenty.alchemia.crafting;

import java.util.Optional;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.moonscenty.alchemia.aspect.AspectList;
import net.minecraft.resources.ResourceLocation;

/** The fields the two kinds of arcane recipe share, so they are written the same way in both files. */
final class Codecs {
    static final MapCodec<String> GROUP = com.mojang.serialization.Codec.STRING
            .optionalFieldOf("group", "");
    /** Left out of a recipe file means free. */
    static final MapCodec<AspectList> COST = AspectList.CODEC
            .optionalFieldOf("vis", AspectList.EMPTY);
    static final MapCodec<Optional<ResourceLocation>> RESEARCH = ResourceLocation.CODEC
            .optionalFieldOf("research");

    private Codecs() {
    }
}
