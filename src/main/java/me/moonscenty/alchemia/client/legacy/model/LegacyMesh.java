package me.moonscenty.alchemia.client.legacy.model;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import org.joml.Vector3f;

/**
 * A mesh the original kept as an OBJ file and baked with its own loader, rather than through the game's.
 *
 * <p>Only what that loader read is read here: positions, texture corners, faces and the groups they fall in. Its
 * normals were not used, so they are worked out from each face instead. Texture corners are taken as written, top
 * down, as the original's loader took them; the game's own OBJ loader would want them turned over.
 *
 * <p>Each {@code g} line starts a group, as it started a part in the original's loader, which could then bake only
 * some of them: that is how a crystal shows more shards as it grows.
 *
 * <p>Faces are drawn as quads, since that is what the game's buffers take: a triangle is a quad with its last corner
 * twice, as the original baked it.
 */
public final class LegacyMesh {
    private final List<List<float[][]>> groups;
    private final List<String> names;

    private LegacyMesh(List<List<float[][]>> groups, List<String> names) {
        this.groups = groups.stream().map(List::copyOf).toList();
        this.names = List.copyOf(names);
    }

    /** Reads an OBJ file. Each face comes out as up to four corners of {@code x, y, z, u, v}. */
    public static LegacyMesh parse(String obj) {
        List<float[]> positions = new ArrayList<>();
        List<float[]> corners = new ArrayList<>();
        List<List<float[][]>> groups = new ArrayList<>();
        List<String> names = new ArrayList<>();
        List<float[][]> faces = null;
        int count = 0;
        for (String raw : obj.split("\\R")) {
            String[] words = raw.trim().split("\\s+");
            switch (words[0]) {
                case "v" -> positions.add(new float[] {Float.parseFloat(words[1]), Float.parseFloat(words[2]),
                        Float.parseFloat(words[3])});
                case "vt" -> corners.add(new float[] {Float.parseFloat(words[1]), Float.parseFloat(words[2])});
                case "g", "o" -> {
                    // a group with nothing in it yet takes the new name rather than leaving an empty part behind
                    String name = words.length > 1 ? words[1] : "";
                    if (faces == null || !faces.isEmpty()) {
                        faces = new ArrayList<>();
                        groups.add(faces);
                        names.add(name);
                    } else {
                        names.set(names.size() - 1, name);
                    }
                }
                case "f" -> {
                    int corners4 = Math.min(4, words.length - 1);
                    if (corners4 < 3) {
                        continue;
                    }
                    float[][] face = new float[corners4][];
                    for (int i = 0; i < corners4; i++) {
                        String[] refs = words[i + 1].split("/");
                        float[] at = positions.get(index(refs[0], positions.size()));
                        float[] uv = refs.length > 1 && !refs[1].isEmpty()
                                ? corners.get(index(refs[1], corners.size())) : new float[2];
                        face[i] = new float[] {at[0], at[1], at[2], uv[0], uv[1]};
                    }
                    if (faces == null) {
                        faces = new ArrayList<>();
                        groups.add(faces);
                        names.add("");
                    }
                    faces.add(face);
                    count++;
                }
                default -> {
                }
            }
        }
        if (count == 0) {
            throw new IllegalArgumentException("No faces");
        }
        for (int i = groups.size() - 1; i >= 0; i--) {
            if (groups.get(i).isEmpty()) {
                groups.remove(i);
                names.remove(i);
            }
        }
        return new LegacyMesh(groups, names);
    }

    /** OBJ counts from one, and from the end when negative. */
    private static int index(String ref, int size) {
        int n = Integer.parseInt(ref);
        return n < 0 ? size + n : n - 1;
    }

    /** How many groups the file had, in the order it had them. */
    public int groups() {
        return groups.size();
    }

    /** The faces of one group, each up to four corners of {@code x, y, z, u, v}. */
    public List<float[][]> faces(int group) {
        return groups.get(group);
    }

    /**
     * The same mesh with its texture corners turned over. The original read its meshes two ways: its own loader took
     * the corners as written, and the older loader it kept for its renderers' models turned them over as the game
     * does. Which one a mesh wants depends on which of the two drew it.
     */
    public LegacyMesh flippedV() {
        List<List<float[][]>> flipped = new ArrayList<>();
        for (List<float[][]> group : groups) {
            List<float[][]> faces = new ArrayList<>();
            for (float[][] face : group) {
                float[][] copy = new float[face.length][];
                for (int i = 0; i < face.length; i++) {
                    copy[i] = new float[] {face[i][0], face[i][1], face[i][2], face[i][3], 1.0F - face[i][4]};
                }
                faces.add(copy);
            }
            flipped.add(faces);
        }
        return new LegacyMesh(flipped, names);
    }

    /** Which group has this name, or -1. */
    public int group(String name) {
        return names.indexOf(name);
    }

    /** Draws the whole mesh in block units, as it was modelled. */
    public void render(PoseStack.Pose pose, VertexConsumer consumer, int colour, int light, int overlay) {
        for (int group = 0; group < groups.size(); group++) {
            render(group, pose, consumer, colour, light, overlay);
        }
    }

    /** Draws one group in block units, as it was modelled. */
    public void render(int group, PoseStack.Pose pose, VertexConsumer consumer, int colour, int light, int overlay) {
        Vector3f normal = new Vector3f();
        for (float[][] face : groups.get(group)) {
            normal(face, normal);
            for (int i = 0; i < 4; i++) {
                float[] corner = face[Math.min(i, face.length - 1)];
                consumer.addVertex(pose, corner[0], corner[1], corner[2])
                        .setColor(colour)
                        .setUv(corner[3], corner[4])
                        .setOverlay(overlay)
                        .setLight(light)
                        .setNormal(pose, normal.x, normal.y, normal.z);
            }
        }
    }

    /** The way a face looks out, from its first three corners. */
    public static Vector3f normal(float[][] face, Vector3f into) {
        float[] a = face[0];
        float[] b = face[1];
        float[] c = face[2];
        into.set(b[0] - a[0], b[1] - a[1], b[2] - a[2]).cross(c[0] - a[0], c[1] - a[1], c[2] - a[2]);
        if (into.lengthSquared() > 0) {
            into.normalize();
        }
        return into;
    }
}
