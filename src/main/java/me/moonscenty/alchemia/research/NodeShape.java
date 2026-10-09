package me.moonscenty.alchemia.research;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

/**
 * How much weight a node carries in the book, which is read off its plate rather than any text.
 * <p>
 * The plates are the original's: ordinary work sits on a square, the head of a branch on a round plate, and research
 * worth going out of your way for has brackets set round its square.
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

    NodeShape(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
