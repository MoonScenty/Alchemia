package me.moonscenty.alchemia.client.legacy.model;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import org.joml.Vector3f;

/**
 * A mesh the original kept as an OBJ file and baked with its own loader, rather than through the game's.
 *
 * <p>Only what that loader read is read here: positions, texture corners and faces. Its normals were not used, so
 * they are worked out from each face instead. Texture corners are taken as written, top down, as the original's
 * loader took them; the game's own OBJ loader would want them turned over.
 *
 * <p>Faces are drawn as quads, since that is what the game's buffers take: a triangle is a quad with its last corner
 * twice, as the original baked it.
 */
public final class LegacyMesh {
    private final List<float[][]> faces;

    private LegacyMesh(List<float[][]> faces) {
        this.faces = List.copyOf(faces);
    }

    /** Reads an OBJ file. Each face comes out as up to four corners of {@code x, y, z, u, v}. */
    public static LegacyMesh parse(String obj) {
        List<float[]> positions = new ArrayList<>();
        List<float[]> corners = new ArrayList<>();
        List<float[][]> faces = new ArrayList<>();
        for (String raw : obj.split("\\R")) {
            String[] words = raw.trim().split("\\s+");
            switch (words[0]) {
                case "v" -> positions.add(new float[] {Float.parseFloat(words[1]), Float.parseFloat(words[2]),
                        Float.parseFloat(words[3])});
                case "vt" -> corners.add(new float[] {Float.parseFloat(words[1]), Float.parseFloat(words[2])});
                case "f" -> {
                    int count = Math.min(4, words.length - 1);
                    float[][] face = new float[count][];
                    for (int i = 0; i < count; i++) {
                        String[] refs = words[i + 1].split("/");
                        float[] at = positions.get(index(refs[0], positions.size()));
                        float[] uv = refs.length > 1 && !refs[1].isEmpty()
                                ? corners.get(index(refs[1], corners.size())) : new float[2];
                        face[i] = new float[] {at[0], at[1], at[2], uv[0], uv[1]};
                    }
                    if (count >= 3) {
                        faces.add(face);
                    }
                }
                default -> {
                }
            }
        }
        if (faces.isEmpty()) {
            throw new IllegalArgumentException("No faces");
        }
        return new LegacyMesh(faces);
    }

    /** OBJ counts from one, and from the end when negative. */
    private static int index(String ref, int size) {
        int n = Integer.parseInt(ref);
        return n < 0 ? size + n : n - 1;
    }

    /** Draws the mesh in block units, as it was modelled. */
    public void render(PoseStack.Pose pose, VertexConsumer consumer, int colour, int light, int overlay) {
        Vector3f normal = new Vector3f();
        for (float[][] face : faces) {
            float[] a = face[0];
            float[] b = face[1];
            float[] c = face[2];
            normal.set(b[0] - a[0], b[1] - a[1], b[2] - a[2])
                    .cross(c[0] - a[0], c[1] - a[1], c[2] - a[2]);
            if (normal.lengthSquared() > 0) {
                normal.normalize();
            }
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
}
