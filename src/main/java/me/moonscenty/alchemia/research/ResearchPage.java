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

    // original's compound pages: a structure built from blocks, drawn layer by layer, top first;
    // air is left out of the picture
    record Construct(me.moonscenty.alchemia.aspect.AspectList cost, int wide, int tall, int deep,
            java.util.List<ResourceLocation> blocks, Optional<ResourceLocation> gate) implements ResearchPage {
        public static final MapCodec<Construct> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                me.moonscenty.alchemia.aspect.AspectList.CODEC.optionalFieldOf("cost",
                        me.moonscenty.alchemia.aspect.AspectList.EMPTY).forGetter(Construct::cost),
                Codec.INT.fieldOf("wide").forGetter(Construct::wide),
                Codec.INT.fieldOf("tall").forGetter(Construct::tall),
                Codec.INT.fieldOf("deep").forGetter(Construct::deep),
                ResourceLocation.CODEC.listOf().fieldOf("blocks").forGetter(Construct::blocks),
                ResourceLocation.CODEC.optionalFieldOf("gate").forGetter(Construct::gate))
                .apply(instance, Construct::new));

        @Override
        public Type type() {
            return Type.CONSTRUCT;
        }
    }

    // up to four aspects with what they are made of
    record Aspects(java.util.List<ResourceLocation> aspects, Optional<ResourceLocation> gate) implements ResearchPage {
        public static final MapCodec<Aspects> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.listOf().fieldOf("aspects").forGetter(Aspects::aspects),
                ResourceLocation.CODEC.optionalFieldOf("gate").forGetter(Aspects::gate))
                .apply(instance, Aspects::new));

        @Override
        public Type type() {
            return Type.ASPECTS;
        }
    }

    enum Type implements StringRepresentable {
        TEXT("text", () -> Text.CODEC),
        RECIPE("recipe", () -> Recipe.CODEC),
        CONSTRUCT("construct", () -> Construct.CODEC),
        ASPECTS("aspects", () -> Aspects.CODEC);

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
