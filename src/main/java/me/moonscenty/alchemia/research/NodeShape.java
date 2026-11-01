package me.moonscenty.alchemia.research;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

public enum NodeShape implements StringRepresentable {
    PLAIN("plain"),
    SPECIAL("special"),
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
