package me.moonscenty.alchemia.research;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

public interface ResearchPage {
    Codec<ResearchPage> CODEC = Type.CODEC.dispatch("type", ResearchPage::type, Type::codec);

    Type type();

    Optional<ResourceLocation> gate();

    default boolean isVisibleTo(Predicate<ResourceLocation> hasResearch) {
        return gate().map(hasResearch::test).orElse(true);
    }

    record Text(String key, Optional<ResourceLocation> gate) implements ResearchPage {
        public static final MapCodec<Text> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("key").forGetter(Text::key),
                ResourceLocation.CODEC.optionalFieldOf("gate").forGetter(Text::gate))
                .apply(instance, Text::new));

        public Text(String key) {
            this(key, Optional.empty());
        }

        @Override
        public Type type() {
            return Type.TEXT;
        }
    }

    record Recipe(ResourceLocation recipe, Optional<ResourceLocation> gate) implements ResearchPage {
        public static final MapCodec<Recipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("recipe").forGetter(Recipe::recipe),
                ResourceLocation.CODEC.optionalFieldOf("gate").forGetter(Recipe::gate))
                .apply(instance, Recipe::new));

        public Recipe(ResourceLocation recipe) {
            this(recipe, Optional.empty());
        }

        @Override
        public Type type() {
            return Type.RECIPE;
        }
    }

    enum Type implements StringRepresentable {
        TEXT("text", () -> Text.CODEC),
        RECIPE("recipe", () -> Recipe.CODEC);

        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

        private final String name;
        // deferred: the records below reference this enum
        private final Supplier<MapCodec<? extends ResearchPage>> codec;

        Type(String name, Supplier<MapCodec<? extends ResearchPage>> codec) {
            this.name = name;
            this.codec = codec;
        }

        public MapCodec<? extends ResearchPage> codec() {
            return codec.get();
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
