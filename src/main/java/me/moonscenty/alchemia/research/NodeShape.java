package me.moonscenty.alchemia.research;

import com.mojang.serialization.Codec;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

/**
 * How much weight a node carries in the book, which is read off its plate rather than any text.
 * <p>
 * The three plates are the vanilla advancement frames, so a player already knows at a glance that the plain one is
 * ordinary work and the ragged one is not.
 */
public enum NodeShape implements StringRepresentable {
    /** Ordinary research: most of the book. */
    PLAIN("plain"),
    /** Worth going out of your way for. */
    SPECIAL("special"),
    /** The head of a branch, or something that changes how a branch is played. */
    MAJOR("major");

    public static final Codec<NodeShape> CODEC = StringRepresentable.fromEnum(NodeShape::values);

    private final String name;
    private final ResourceLocation open;
    private final ResourceLocation done;

    NodeShape(String name) {
        this.name = name;
        this.open = Alchemia.id("research/node_" + name);
        this.done = Alchemia.id("research/node_" + name + "_done");
    }

    /** The plate to draw, silver while the research is outstanding and gold once it is finished. */
    public ResourceLocation sprite(boolean known) {
        return known ? done : open;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
