package me.moonscenty.alchemia.crafting;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * The serializer of a recipe that carries nothing of its own: it writes nothing and reads back the one instance.
 * The stream codec refuses any value but that instance, so the recipe must compare equal to it.
 */
public class UnitSerializer<T extends Recipe<?>> implements RecipeSerializer<T> {
    private final MapCodec<T> codec;
    private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

    public UnitSerializer(T instance) {
        this.codec = MapCodec.unit(instance);
        this.streamCodec = StreamCodec.unit(instance);
    }

    @Override
    public MapCodec<T> codec() {
        return codec;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
        return streamCodec;
    }
}
