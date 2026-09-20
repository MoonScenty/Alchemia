package me.moonscenty.alchemia.research;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

/**
 * One side of the book once an entry is opened.
 * <p>
 * An entry is written as a run of these, and the reader turns through them two at a time. A page may be held back
 * behind another piece of research, which is how the original kept the awkward parts of a subject out of sight until
 * the reader had earned them.
 */
public interface ResearchPage {
    Codec<ResearchPage> CODEC = Type.CODEC.dispatch("type", ResearchPage::type, Type::codec);

    Type type();

    /** Research that has to be finished before this page is worth showing, if any. */
    Optional<ResourceLocation> gate();

    default boolean isVisibleTo(Predicate<ResourceLocation> hasResearch) {
        return gate().map(hasResearch::test).orElse(true);
    }

    /** A passage of the write-up, held as a translation key so it can be written in any language. */
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

    /** A recipe, laid out the way it is made rather than described in words. */
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

    /** What a page holds. Adding a kind means adding a record above and a case where pages are drawn. */
    enum Type implements StringRepresentable {
        TEXT("text", () -> Text.CODEC),
        RECIPE("recipe", () -> Recipe.CODEC);

        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

        private final String name;
        // deferred, because the records below name this enum in turn
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
