package me.moonscenty.alchemia.client.legacy.model;

import java.util.ArrayList;
import java.util.List;

/**
 * One {@code ModelRenderer} of the original as its constructor left it: boxes, a pivot, a turn, and parts hung off
 * it. Plain numbers only, so it can be read without the game running.
 */
public final class LegacyPart {
    /** One {@code addBox}. The texture corner and mirroring are taken from the part as it stood at that call. */
    public record Box(int u, int v, float x, float y, float z, int width, int height, int depth, float grow,
            boolean mirror) {
    }

    /** The field the original kept it in, or a made-up name when it never had one. */
    String name;
    int u;
    int v;
    int textureWidth;
    int textureHeight;
    float x;
    float y;
    float z;
    float xRot;
    float yRot;
    float zRot;
    boolean mirror;
    boolean hidden;
    boolean shown = true;
    final List<Box> boxes = new ArrayList<>();
    final List<LegacyPart> children = new ArrayList<>();

    LegacyPart(String name, int u, int v, int textureWidth, int textureHeight) {
        this.name = name;
        this.u = u;
        this.v = v;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    public String name() {
        return name;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public float z() {
        return z;
    }

    public float xRot() {
        return xRot;
    }

    public float yRot() {
        return yRot;
    }

    public float zRot() {
        return zRot;
    }

    /** {@code isHidden}: the original never drew it, though it kept it. */
    public boolean hidden() {
        return hidden;
    }

    public int textureWidth() {
        return textureWidth;
    }

    public int textureHeight() {
        return textureHeight;
    }

    public List<Box> boxes() {
        return boxes;
    }

    public List<LegacyPart> children() {
        return children;
    }

    @Override
    public String toString() {
        return name + " @" + x + "," + y + "," + z + " rot " + xRot + "," + yRot + "," + zRot + " boxes " + boxes
                + (children.isEmpty() ? "" : " children " + children);
    }
}
